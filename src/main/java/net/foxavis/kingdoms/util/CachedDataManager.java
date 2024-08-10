package net.foxavis.kingdoms.util;

import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
 */
public abstract class CachedDataManager<K, V> {

	// --- Static --- \\

	/** The raw list of all CDMs */
	private static List<CachedDataManager<?, ?>> caches = new ArrayList<>();

	/**
	 * Returns a list of all created CDMs.
	 * @return the list of created CDMs.
	 */
	public static List<CachedDataManager<?, ?>> getCaches() { return caches; }

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
			while((line = reader.readLine()) != null) builder.append(line);

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
			if(!file.createNewFile())
				throw new IOException("failed to create the file");
		}

		try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(contents);
		}
	}

	// --- Object-based --- \\
	// -- Fields -- \\

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
	}

	/**
	 * <p>Fetches the value by using the given key.</p>
	 * <p>
	 *     This is where you implement how the data is fetched from the source, being the database or filesystem.
	 *     This allows for easier implementations of other systems, bn
	 * </p>
	 *
	 * @param key
	 * @return
	 */
	@Nullable public abstract V loadFromSource(@NotNull K key);


}
