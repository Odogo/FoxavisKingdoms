package net.foxavis.kingdoms.util;

import net.foxavis.kingdoms.FoxavisKingdoms;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * <p>An abstract class handling the retrieving and storage of data into/from an internal cache and the filesystem.</p>
 * <p>Under the hood, this is just a Map that will store data with a few added bonuses. Those being:</p>
 * <ul>
 *     <li>Fetch times and automatic expiry;</li>
 *     <li>Saving/Fetching from file system (depending on how implemented, could work with databases if set up properly); and</li>
 *     <li>Caching, yaknow the whole point of this class.</li>
 * </ul>
 * @param <K> The key for each value
 * @param <V> The value for each key
 * @author Kyomi
 */
public abstract class CachedDataManager<K, V> {

	// --- Static --- \\

	/** The raw list of all CDMs */
	private static final List<CachedDataManager<?, ?>> caches = new CopyOnWriteArrayList<>();

	private static final Logger logger = FoxavisKingdoms.getLoggerInstance();

	/**
	 * Returns a list of all created CDMs.
	 * @return the list of created CDMs.
	 */
	public static List<CachedDataManager<?, ?>> getCaches() { return caches; }

	/**
	 * Shuts down all created CDMs.
	 */
	public static void shutdownAll() {
		caches.forEach(CachedDataManager::shutdown);
	}

	/**
	 * Reads a file from the file system and returns its contents
	 * @param file the file to read
	 * @return the contents of the file
	 * @throws IllegalArgumentException if the given file is a directory
	 */
	@Nullable public static String readContents(@NotNull File file) {
		if(!file.exists()) return null;
		if(file.isDirectory()) throw new IllegalArgumentException("file is a directory.");

		try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
			StringBuilder builder = new StringBuilder();

			String line;
			while((line = reader.readLine()) != null)
				builder.append(line).append(System.lineSeparator());

			if(builder.toString().isEmpty() || builder.toString().isBlank()) return null;
			return builder.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Writes a string into a file, creates the file if it doesn't exist.
	 * @param file the file, or representation of a new file
	 * @param contents the contents to write to the file
	 * @throws IllegalArgumentException if the given file is a directory
	 * @throws IOException if the file failed to create, a failed attempt to create a file, or a failed attempt to write to the file
	 */
	public static void writeContents(@NotNull File file, @NotNull String contents) throws IOException {
		if(file.isDirectory()) throw new IllegalArgumentException("file is a directory");

		if(!file.exists()) {
			file.getParentFile().mkdirs();
			if(!file.createNewFile())
				throw new IOException("failed to create the file");
		}

		try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(contents);
		}
	}

	// --- Object-based --- \\
	// -- Fields -- \\

	private final String identifier;

	private final Map<K, V> cache;
	private final Map<K, Long> accessTime;

	private final long expirationTime;
	private final ScheduledExecutorService executorService;

	/**
	 * Creates a new CachedDataManager (CDM) that data will expire with the given parameters
	 * @param expirationTime the number at which it will expire
	 * @param timeUnit the time unit (seconds, minutes, hours, etc.)
	 */
	public CachedDataManager(long expirationTime, TimeUnit timeUnit) {
		this.cache = new ConcurrentHashMap<>();
		this.accessTime = new ConcurrentHashMap<>();

		this.expirationTime = timeUnit.toMillis(expirationTime);
		this.executorService = Executors.newSingleThreadScheduledExecutor();

		caches.add(this);
		startCacheExpiry();

		identifier = this.getClass().getSimpleName();

		logger.info("[" + identifier + "] Created CachedDataManager with expiration time of " + expirationTime + " " + timeUnit.name());
	}

	/**
	 * <p>Fetches the value by using the given key.</p>
	 * <p>
	 *     This is where you implement how the data is fetched from the source, being a database or filesystem.
	 *     This allows for easier implementations of other systems and easier control of what the CDM is doing.
	 * </p>
	 * @param key The key to fetch the value from the source
	 * @return The value from the source using the key, or null if a value does not exist for key.
	 */
	@Nullable protected abstract V loadFromSource(@NotNull K key);

	/**
	 * <p>Stores the value based on the given key into the source</p>
	 * <p>
	 *     This is where you implement how the data is stored into the source, being a database or filesystem.
	 *     This allows for easier implementation of other systems and easier control of what the CDM is doing.
	 * </p>
	 * @param key The key to use when setting the value
	 * @param value The value to set associated with the key
	 */
	protected abstract void saveToSource(@NotNull K key, @NotNull V value);

	/**
	 * <p>Deletes the the value using the key from the source.</p>
	 * <p>This action should be irreversible, as this action should remove the value associated with key.</p>
	 * <p>
	 *     This is where you implement how the data is stored into the source, being a database or filesystem.
	 *     This allows for easier implementation of other systems and easier control of what the CDM is doing.
	 * </p>
	 * @param key The key to use to delete the value.
	 */
	protected abstract void deleteFromSource(@NotNull K key);

	/**
	 * Fetches the data from the cache, if the key-value pair exists. Otherwise, attempts to load it from the source.
	 * @param key The key to fetch from the cache or the source.
	 * @return The value associated with key, or null if the source does not have 
	 */
	@Nullable public V fetchData(@NotNull K key) {
		V value = cache.get(key);
		if(value != null) {
			accessTime.put(key, System.currentTimeMillis());
			logger.fine("[" + identifier + "] Cache hit for key: " + key);
		} else {
			logger.fine("[" + identifier + "] Cache miss for key: " + key + ". Loading from source...");
			value = loadFromSource(key);
			if(value == null) {
				logger.warning("[" + identifier + "] No data found for key: " + key);
				return null;
			}

			cache.put(key, value);
			accessTime.put(key, System.currentTimeMillis());
			logger.fine("[" + identifier + "] Data loaded from source for key: " + key);
		}
		return value;
	}

	/**
	 * Checks if the cache of the CDM has any data regarding the given key.
	 * <p><b>NOTE: </b>This does not check the source. This is only checking if we have anything in the cache.</p>
	 * @param key The key we should be checking
	 * @return True, if the cache has any data regarding the given key, otherwise false.
	 */
	public boolean hasData(@NotNull K key) {
		return cache.containsKey(key);
	}

	/**
	 * Stores data into the cache and saves a copy to the source.
	 * @param key The key to set the given value for
	 * @param value The data, or value, to set with the given key
	 */
	public void storeData(K key, V value) {
		cache.put(key, value);
		accessTime.put(key, System.currentTimeMillis());
		saveToSource(key, value);
		logger.fine("[" + identifier + "] Data stored for key: " + key);
	}

	/**
	 * Settles all data inside the cache to the source. All data settled will be removed from the cache as well.
	 */
	public void settleData() {
		cache.forEach((key, value) -> {
			saveToSource(key, value);
			logger.fine("[" + identifier + "] Data settled for key: " + key);
		});
		cache.clear();
		accessTime.clear();
		logger.info("[" + identifier + "] All cache data settled to source, cache cleared.");
	}

	/**
	 * Settles a specific value using its key. Data settled will be removed from the cache as well.
	 * @param key The key to settle its data
	 * @return True if the data was in the cache and was saved, false if given key has no data in the cache.
	 */
	public boolean settleData(@NotNull K key) {
		V value = cache.remove(key);
		if(value == null) {
			logger.fine("[" + identifier + "] No data found for key: " + key);
			return false;
		}

		accessTime.remove(key);
		saveToSource(key, cache.get(key));
		logger.fine("[" + identifier + "] Data settled and removed from cache for key: " + key);
		return true;
	}

	/**
	 * Destroys or deletes the data at given key from both cache and source.
	 * <p>
	 *     <b>WARNING:</b> While using this method, any and all data regarding given key will be destroyed and removed from the cache and source.
	 *     <p>This action is irreversible if the data is not stored elsewhere or a reference already exists to it (that is not already garbage collected)</p>
	 * </p>
	 * @param key the key to destroy the data of
	 */
	public void destroyData(@NotNull K key) {
		cache.remove(key);
		accessTime.remove(key);
		deleteFromSource(key);
		logger.info("[" + identifier + "] Data destroyed for key: " + key);
	}

	/**
	 * Checks if the given key's data in the cache should expire and be removed.
	 * @param key The key to check if it's expired
	 * @return True, if the data in the cache has expired, false otherwise.
	 */
	protected boolean shouldExpire(K key) {
		return System.currentTimeMillis() - accessTime.get(key) > expirationTime;
	}

	/**
	 * Starts the Executor service to begin checking data if it has expired or not.
	 * <b>This is called at the creation of each CDM and should NEVER be called again.</b>
	 */
	private void startCacheExpiry() {
		executorService.scheduleAtFixedRate(() -> {
			try {
				cache.keySet().stream()
						.filter(this::shouldExpire)
						.forEach(key -> {
							settleData(key);
							logger.fine("[" + identifier + "] Cache entry expired and settled for key: " + key);
						});
			} catch (Exception e) {
				logger.severe("[" + identifier + "] An error occurred while checking for expired data: " + e.getMessage());
			}
		}, expirationTime, expirationTime, TimeUnit.MILLISECONDS);
		logger.info("[" + identifier + "] Cache expiry service started.");
	}

	/**
	 * Shuts down the executor service and settles all data within the CDM's cache.
	 * This should be executed when the program (or in this case, plugin) is terminated/disabled.
	 */
	public void shutdown() {
		executorService.shutdown();
		executorService.close();

		settleData();
	}
}