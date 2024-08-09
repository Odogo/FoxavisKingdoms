package net.foxavis.kingdoms.objects.kingdoms;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KingdomLocation implements Comparable<KingdomLocation> {

	// -- Static --
	/**
	 * Translates a {@link Location} from Bukkit to a {@link KingdomLocation}
	 * @param location the {@link Location} in question
	 * @return the new {@link KingdomLocation}
	 * @throws NullPointerException if {@link Location#getWorld()} is null
	 */
	public static KingdomLocation fromBukkit(@NotNull Location location) {
		if(location.getWorld() == null) throw new NullPointerException("world in location cannot be null");
		return new KingdomLocation(location.getWorld(), location.getX(), location.getY(), location.getZ());
	}

	// -- Fields --
	private String world;
	private double x, y, z;

	// -- Constructors --
	public KingdomLocation(String world, double x, double y, double z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public KingdomLocation(@NotNull World world, double x, double y, double z) {
		this(world.getName(), x, y, z);
	}

	public KingdomLocation(@NotNull Location location) {
		this(location.getWorld(), location.getX(), location.getY(), location.getZ());
	}

	// -- Methods --
	// - Getters -
	/**
	 * Gets the stored world name
	 * @return the world name
	 */
	public String getWorldName() { return world; }

	/**
	 * Gets the {@link World} object associated with what is stored in {@link #getWorldName()}
	 * @return the associated {@link World} object, or null
	 * @see Bukkit#getWorld(String)
	 */
	@Nullable public World getWorld() { return Bukkit.getWorld(world); }

	/**
	 * Gets the stored X value
	 * @return the x value
	 */
	public double getX() { return x; }

	/**
	 * Gets the stored Y value
	 * @return the Y value
	 */
	public double getY() { return y; }

	/**
	 * Gets the stored Z value
	 * @return the Z value
	 */
	public double getZ() { return z; }

	/**
	 * Translates the current {@link KingdomLocation} object to a Bukkit {@link Location}
	 * @return a {@link Location} based on the values from this object, null if {@link #getWorld()} is null
	 */
	@Nullable public Location toBukkit() {
		if(getWorld() == null) return null;
		return new Location(getWorld(), x, y, z);
	}

	// - Setters -
	/**
	 * Sets the world name to the object
	 * @param world the new world name
	 */
	public void setWorldName(String world) { this.world = world; }

	/**
	 * Sets the {@link World} (realistically just the name) to the object
	 * @param world the world object
	 */
	public void setWorld(@NotNull World world) { this.world = world.getName(); }

	/**
	 * Sets the X value
	 * @param x the new X value
	 */
	public void setX(double x) { this.x = x; }

	/**
	 * Sets the Y value
	 * @param y the new Y value
	 */
	public void setY(double y) { this.y = y; }

	/**
	 * Sets the Z value
	 * @param z the new Z value
	 */
	public void setZ(double z) { this.z = z; }

	// - Comparison & Hashing -
	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof KingdomLocation that)) return false;

		if (Double.compare(x, that.x) != 0) return false;
		if (Double.compare(y, that.y) != 0) return false;
		if (Double.compare(z, that.z) != 0) return false;
		return world.equals(that.world);
	}

	@Override public int hashCode() {
		int result;
		result = world.hashCode();
		result = 31 * result + Double.hashCode(x);
		result = 31 * result + Double.hashCode(y);
		result = 31 * result + Double.hashCode(z);
		return result;
	}

	/**
	 * Compares the current {@link KingdomLocation} with another.
	 * <p>In exactly this order, the objects are compared by: <code>worldName</code>, then <code>X</code>, then <code>Y</code>, and finally <code>Z</code></p>
	 * @param o the object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object
	 *         is less than, equal to, or greater than the specified object.
	 */
	@Override public int compareTo(@NotNull KingdomLocation o) {
		int comp = getWorldName().compareTo(o.getWorldName());
		if(comp != 0) return comp;

		comp = Double.compare(getX(), o.getX());
		if(comp != 0) return comp;

		comp = Double.compare(getY(), o.getY());
		if(comp != 0) return comp;

		return Double.compare(getZ(), o.getZ());
	}
}