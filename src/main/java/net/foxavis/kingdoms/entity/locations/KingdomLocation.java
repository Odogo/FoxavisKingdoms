package net.foxavis.kingdoms.entity.locations;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * This class is a serializable version of the {@link Location} as locations cannot
 * be serialized (through Gson) due to a {@link java.lang.reflect.InaccessibleObjectException} being thrown.
 */
public class KingdomLocation {

	@NotNull private String world;
	private double x, y, z;

	@Nullable private Float yaw, pitch;

	/**
	 * Creates a pseudo-location based on the world's name and it's position values
	 * @param world The world's name
	 * @param x The X coordinate
	 * @param y The Y coordinate
	 * @param z The Z coordinate
	 * @param yaw The yaw value (LR rotation)
	 * @param pitch The pitch value (UD rotation)
	 */
	public KingdomLocation(@NotNull String world, double x, double y, double z, float yaw, float pitch) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	/**
	 * Creates a pseudo-location based on a {@link World} object and it's position values
	 * @param world The given world
	 * @param x The X coordinate
	 * @param y The Y coordinate
	 * @param z The Z coordinate
	 * @param yaw The yaw value (LR rotation)
	 * @param pitch The pitch value (UD rotation)
	 */
	public KingdomLocation(@NotNull World world, double x, double y, double z, float yaw, float pitch) {
		this(world.getName(), x, y, z, yaw, pitch);
	}

	/**
	 * Creates a pseudo-location based on the world's name, and only it's X/Y/Z cords
	 * @param world The given world
	 * @param x The X coordinate
	 * @param y The Y coordinate
	 * @param z The Z coordinate
	 */
	public KingdomLocation(@NotNull String world, double x, double y, double z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = null;
		this.pitch = null;
	}

	/**
	 * Creates a pseudo-location based on a {@link World} object, and only its X/Y/Z cords
	 * @param world The given world
	 * @param x The X coordinate
	 * @param y The Y coordinate
	 * @param z The Z coordinate
	 */
	public KingdomLocation(@NotNull World world, double x, double y, double z) {
		this(world.getName(), x, y, z);
	}

	/**
	 * Creates a pseudo-location based on a {@link Location}
	 * @param location The given {@link Location} object
	 */
	public KingdomLocation(@NotNull Location location) {
		this(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
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
	 * Returns the X cord of the location
	 * @return The X coordinate
	 */
	public double getX() { return x; }

	/**
	 * Returns the Y cord of the location
	 * @return The Y coordinate
	 */
	public double getY() { return y; }

	/**
	 * Returns the Z cord of the location
	 * @return The Z coordinate
	 */
	public double getZ() { return z; }

	/**
	 * Returns the Yaw of the location
	 * @return The Yaw location, or null if none was specified
	 */
	public @Nullable Float getYaw() { return yaw; }

	/**
	 * Returns the Pitch of the location
	 * @return The Pitch location, or null if none was specified
	 */
	public @Nullable Float getPitch() { return pitch;}

	/**
	 * Converts this object into a more acceptable {@link Location} object
	 * @return A clone of this object as a {@link Location}
	 */
	public Location getBukkitLocation() {
		Location returnable = new Location(getBukkitWorld(), x, y, z);
		if(getYaw() != null) returnable.setYaw(getYaw());
		if(getPitch() != null) returnable.setPitch(getPitch());
		return returnable;
	}
	/**
	 * Sets the world name to use in this location
	 * @param world The world name to set
	 * @apiNote Use {@link #setWorld(World)} instead to ensure world name consistency
	 */
	public void setWorld(@NotNull String world) { this.world = world; }

	/**
	 * Sets the world's name using a Bukkit world.
	 * @param world A Bukkit world
	 */
	public void setWorld(@NotNull World world) { this.world = world.getName(); }

	/**
	 * Sets the X cord of the location
	 * @param x The new X cord
	 */
	public void setX(double x) { this.x = x; }

	/**
	 * Sets the Y cord of the location
	 * @param y The new Y cord
	 */
	public void setY(double y) { this.y = y; }

	/**
	 * Sets the Z cord of the location
	 * @param z The new Z cord
	 */
	public void setZ(double z) { this.z = z; }

	/**
	 * Sets the Yaw of this location
	 * @param yaw The new yaw, or null to not set one
	 */
	public void setYaw(@Nullable Float yaw) { this.yaw = yaw; }

	/**
	 * Sets the Pitch of this location
	 * @param pitch The new pitch, or null to not set one
	 */
	public void setPitch(@Nullable Float pitch) { this.pitch = pitch; }

	@Override public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof KingdomLocation that)) return false;

		return Double.compare(x, that.x) == 0 && Double.compare(y, that.y) == 0 && Double.compare(z, that.z) == 0 && world.equals(that.world) && Objects.equals(yaw, that.yaw) && Objects.equals(pitch, that.pitch);
	}

	@Override public int hashCode() {
		int result = world.hashCode();
		result = 31 * result + Double.hashCode(x);
		result = 31 * result + Double.hashCode(y);
		result = 31 * result + Double.hashCode(z);
		result = 31 * result + Objects.hashCode(yaw);
		result = 31 * result + Objects.hashCode(pitch);
		return result;
	}
}