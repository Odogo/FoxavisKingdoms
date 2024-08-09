package net.foxavis.kingdoms.objects.kingdoms;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KingdomChunk implements Comparable<KingdomChunk> {

	// -- Static --
	/**
	 * Translate a location object into a KingdomChunk object.
	 *
	 * @param location the location to translate
	 * @return a KingdomChunk that's applicable at this location's chunk
	 * @throws NullPointerException if location's world is null
	 */
	public static KingdomChunk from(@NotNull Location location) throws NullPointerException {
		if(location.getWorld() == null) throw new NullPointerException("world in location cannot be null");
		return new KingdomChunk(location.getWorld(), location.getChunk().getX(), location.getChunk().getZ());
	}

	/**
	 * Translate a Bukkit chunk into a KingdomChunk
	 *
	 * @param chunk the chunk to translate
	 * @return a KingdomChunk that's applicable at this chunk
	 */
	public static KingdomChunk from(@NotNull Chunk chunk) {
		return new KingdomChunk(chunk.getWorld(), chunk.getX(), chunk.getZ());
	}

	// -- Fields --
	private String world;
	private int x, z;

	// -- Constructors --
	public KingdomChunk(@NotNull String world, int x, int z) {
		this.world = world;
		this.x = x;
		this.z = z;
	}

	public KingdomChunk(@NotNull World world, int x, int z) {
		this.world = world.getName();
		this.x = x;
		this.z = z;
	}

	// -- Methods --
	// - Getters -

	/**
	 * Returns the world name given with this object
	 * @return the world name
	 */
	public String getWorldName() { return world; }

	/**
	 * Returns the Bukkit/Spigot world object from the world name
	 * @return the world object, null if it does not exist
	 */
	@Nullable public World getWorld() { return Bukkit.getWorld(getWorldName()); }

	/**
	 * Returns the X chunk coordinate stored
	 * @return the x coordinate
	 */
	public int getX() { return x; }

	/**
	 * Returns the Z coordinate stored
	 * @return the z coordinate
	 */
	public int getZ() { return z; }

	/**
	 * Using the {@link #getWorld()} method, gets the Bukkit chunk in the world.
	 * @return the chunk in world, null if world is null
	 */
	@Nullable public Chunk getChunk() {
		if(getWorld() == null) return null;
		return getWorld().getChunkAt(x, z);
	}

	// - Setters -

	/**
	 * Sets the world object to a new world
	 * @param world the new world
	 */
	public void setWorld(World world) { this.world = world.getName(); }

	/**
	 * Sets the world name to a different world name
	 * @param world the new world name
	 * @apiNote Best to use {@link #setWorld(World)} instead, since it's a real World object instead
	 */
	public void setWorld(String world) { this.world = world; }

	/**
	 * Sets the X object to a new X coordinate
	 * @param x the new x coordinate
	 */
	public void setX(int x) { this.x = x; }

	/**
	 * Sets the Z object to a new Z coordinate
	 * @param z the new z coordinate
	 */
	public void setZ(int z) { this.z = z; }

	// - Hashing & Comparison -

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		KingdomChunk that = (KingdomChunk) o;

		if (x != that.x) return false;
		if (z != that.z) return false;
		return world.equals(that.world);
	}

	@Override public int hashCode() {
		int result = world.hashCode();
		result = 31 * result + x;
		result = 31 * result + z;
		return result;
	}

	/**
	 * Compares the current {@link KingdomChunk} object with another.
	 * <p>In exactly this order, the objects are compared by: <code>worldName</code>, then <code>X</code>, and finally <code>Z</code></p>
	 * @param o the object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object
	 *         is less than, equal to, or greater than the specified object.
	 */
	@Override public int compareTo(@NotNull KingdomChunk o) {
		int comp = getWorldName().compareTo(o.getWorldName());
		if(comp != 0) return comp;

		comp = Integer.compare(getX(), o.getX());
		if(comp != 0) return comp;

		return Integer.compare(getZ(), o.getZ());
	}
}