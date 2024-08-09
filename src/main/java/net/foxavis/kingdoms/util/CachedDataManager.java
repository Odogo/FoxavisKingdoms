package net.foxavis.kingdoms.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class CachedDataManager<K, V> {

	public static List<CachedDataManager<?, ?>> caches = new ArrayList<>();

	public static void shutdownAll() {
		for(CachedDataManager<?, ?> cache : caches) {
			cache.shutdown();
		}
	}

	/**
	 * Reads a file and returns its contents
	 * @param file The file to read
	 * @return The contents of the file
	 */
	@Nullable
	public static String readFile(File file) {
		if (!file.exists()) return null;
		if (file.isDirectory()) throw new IllegalArgumentException("file is a directory.");

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			StringBuilder builder = new StringBuilder();

			String line;
			while ((line = reader.readLine()) != null)
				builder.append(line);

			if (builder.toString().isEmpty() || builder.toString().isBlank()) return null;
			return builder.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Writes content to a file
	 * @param file    The file to write to
	 * @param content The content to write
	 */
	public static void writeToFile(File file, String content) {
		if (file.isDirectory()) throw new IllegalArgumentException("file is a directory.");

		if (!file.exists()) {
			try {
				if (!file.createNewFile())
					throw new RuntimeException("failed to create file.");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(content);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private final Map<K, V> cache;
	private final Map<K, Long> accessTimes;

	private final long expirationTime;
	private final ScheduledExecutorService executorService;

	public CachedDataManager(long expirationTime, TimeUnit timeUnit) {
		this.cache = new ConcurrentHashMap<>();
		this.accessTimes = new ConcurrentHashMap<>();

		this.expirationTime = timeUnit.toMillis(expirationTime);
		this.executorService = Executors.newSingleThreadScheduledExecutor();

		caches.add(this);

		startCacheExpire();
	}

	/**
	 * Gets the data stored in the cache, or loads it from the source if it doesn't exist
	 * @param key The key to get the data for
	 * @return The data stored in the cache or loaded from the source
	 */
	@Nullable public V getData(K key) {
		if(cache.containsKey(key)) {
			accessTimes.put(key, System.currentTimeMillis());
			return cache.get(key);
		} else {
			V value = loadFromSource(key);
			if(value == null) return null;

			cache.put(key, value);
			accessTimes.put(key, System.currentTimeMillis());
			return value;
		}
	}

	/**
	 * Checks if the cache contains data for a key
	 * @param key The key to check
	 * @return Whether the cache contains data for the key
	 */
	public boolean containsData(K key) {
		return cache.containsKey(key);
	}

	/**
	 * Sets data in the cache and saves it to the source
	 * @param key The key to set the data for
	 * @param value The value to set
	 */
	public void setData(K key, V value) {
		cache.put(key, value);
		accessTimes.put(key, System.currentTimeMillis());
		saveToSource(key, value);
	}

	/**
	 * Removes data from the cache
	 * @param key The key to remove the data for
	 */
	public void removeData(K key) {
		saveToSource(key, cache.get(key));
		cache.remove(key);
		accessTimes.remove(key);
	}

	/**
	 * Deletes data from the cache and the source
	 * <p>
	 *     <b>WARNING: </b> This action is irreversible! Use with caution!
	 * </p>
	 * @param key The key to delete the data for
	 */
	public void deleteData(K key) {
		removeData(key);
		deleteFromSource(key);
	}

	@Nullable protected abstract V loadFromSource(@NotNull K key);
	protected abstract void saveToSource(@NotNull K key, @NotNull V value);
	protected abstract void deleteFromSource(@NotNull K key);

	/**
	 * Checks if the cache should expire for a key
	 * @param key The key to check
	 * @return Whether the cache should expire for the key
	 */
	private boolean shouldExpire(K key) {
		return System.currentTimeMillis() - accessTimes.get(key) > expirationTime;
	}

	private void startCacheExpire() {
		executorService.scheduleAtFixedRate(() -> {
			for(K key : new ArrayList<>(cache.keySet())) {
				if(shouldExpire(key))
					removeData(key);
			}
		}, expirationTime, expirationTime, TimeUnit.MILLISECONDS);
	}

	public void shutdown() {
		executorService.shutdown();

		cache.keySet().forEach(this::removeData);
	}
}