package net.foxavis.kingdoms.entity.locations;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class is a serializable version of the {@link Chunk} as chunks cannot
 * be serialized (through Gson) due to a {@link java.lang.reflect.InaccessibleObjectException} being thrown.
 */
public class KingdomChunk {

	@NotNull private String world; // The world name of this chunk
	private int x; // The X cord of the chunk
	private int z; // The Z cord of the chunk

	/**
	 * Creates a pseudo-chunk using the world's name and x/z cords
	 * @param world The world name, as a string
	 * @param x The X cord of the chunk
	 * @param z The Z cord of the chunk
	 */
	public KingdomChunk(@NotNull String world, int x, int z) {
		this.world = world.intern();
		this.x = x;
		this.z = z;
	}

	/**
	 * Creates a pseudo-chunk using an actual world and x/z cords
	 * @param world The world
	 * @param x The X cord of the chunk
	 * @param z The Z cord of the chunk
	 */
	public KingdomChunk(@NotNull World world, int x, int z) {
		this(world.getName(), x, z);
	}

	/**
	 * Creates a serializable chunk using an actual {@link Chunk} object
	 * @param chunk A Bukkit Chunk that will be converted
	 */
	public KingdomChunk(@NotNull Chunk chunk) {
		this(chunk.getWorld(), chunk.getX(), chunk.getZ());
	}

	/**
	 * Creates a serializable chunk using a {@link Location} object
	 * @param location A Bukkit location that will be converted
	 */
	public KingdomChunk(@NotNull Location location) {
		this(location.getChunk());
	}

	/**
	 * Returns the world name that's stored from this representation
	 * @return The world name of this chunk representation
	 */
	public @NotNull String getWorld() { return world; }

	/**
	 * Returns the Bukkit object of the world using its name.
	 * @return The Bukkit World object of the stored name, or null if that world no longer exists (invalidating this object)
	 */
	public @Nullable World getBukkitWorld() { return Bukkit.getWorld(world); }

	/**
	 * Returns the X cord of the chunk.
	 * <p><b>NOTE:</b> This is not to be confused with an X cord for a block. A chunk cord and block cord run off different numbering schemes.</p>
	 * @return The chunk X coordinate
	 */
	public int getX() { return x; }

	/**
	 * Returns the Z cord of the chunk.
	 * <p><b>NOTE:</b> This is not to be confused with an Z cord for a block. A chunk cord and block cord run off different numbering schemes.</p>
	 * @return The chunk Z coordinate
	 */
	public int getZ() { return z; }

	/**
	 * Using the {@link #getBukkitWorld()} method, gets a {@link Chunk} objects from the specified x/z cords.
	 * @return A Bukkit Chunk object in the world at the x/z cords in this object.
	 */
	public @Nullable Chunk getWorldChunk() { return (getBukkitWorld() == null ? null : getBukkitWorld().getChunkAt(x, z)); }

	/**
	 * Sets the world's name using a new world's name, assuming that's what's given.
	 * @param world A potential world's name to be used as a replacement
	 */
 	public void setWorld(@NotNull String world) { this.world = world; }

	/**
	 * Sets the world's name using a Bukkit world.
	 * @param world A Bukkit world
	 */
	public void setWorld(@NotNull World world) { this.world = world.getName(); }

	/**
	 * Sets the X chunk cord that's stored in this object
	 * @param x The new X chunk cord
	 */
	public void setX(int x) { this.x = x; }

	/**
	 * Sets the Z chunk cord that's stored in this object
	 * @param z The new Z chunk cord
	 */
	public void setZ(int z) { this.z = z; }

	@Override public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof KingdomChunk that)) return false;

		return x == that.x && z == that.z && world.equals(that.world);
	}

	@Override public int hashCode() {
		int result = world.hashCode();
		result = 31 * result + x;
		result = 31 * result + z;
		return result;
	}

	@Override public String toString() { return "KingdomChunk{world:" + world + ", x:" + x + ", z:" + z + "}"; }
}
