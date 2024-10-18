package net.foxavis.kingdoms.entity.primary;

import net.foxavis.kingdoms.entity.Describable;
import net.foxavis.kingdoms.entity.KingdomFlag;
import net.foxavis.kingdoms.entity.KingdomHeld;
import net.foxavis.kingdoms.entity.Territorial;
import net.foxavis.kingdoms.entity.locations.KingdomChunk;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents a settlement in a Kingdom. Settlements are owned by a Kingdom and have their own territory.
 * <p>
 *     Settlements can have their own flags that override the Kingdom's default flags. If a flag is not overridden,
 *     the default flag from the Kingdom will be used.
 * </p>
 * <p>
 *     Settlements can also have a description, which is a short description of the settlement.
 *     This can be used to describe the settlement to players.
 *     If a description is not set, the description will be null.
 *     The description can be set to null to remove the description.
 * </p>
 * @see KingdomHeld
 * @see Describable
 * @see Territorial
 * @author Kyomi
 */
public class Settlement implements KingdomHeld, Describable, Territorial {

	@NotNull private UUID owningKingdom;

	@NotNull private String name;
	@Nullable private String description;

	@NotNull private final HashSet<KingdomChunk> territory;
	@Nullable private Map<KingdomFlag, Boolean> overrideFlags;

	/**
	 * Creates a settlement under the owning Kingdom with the specified name.
	 * @param kingdom The Kingdom that owns this settlement.
	 * @param name The name of the settlement.
	 */
	public Settlement(@NotNull Kingdom kingdom, @NotNull String name) {
		this.owningKingdom = kingdom.getKingdomId(); // TODO: Implement using the Kingdom's UUID
		this.name = name;

		this.description = null;
		this.territory = new HashSet<>();
		this.overrideFlags = null;
	}

	// -- KingdomHeld methods --
	@Override public Kingdom getOwningKingdom() { return Kingdom.getCache().fetchData(owningKingdom); }
	@Override public void setOwningKingdom(Kingdom kingdom) { this.owningKingdom = kingdom.getKingdomId(); }

	// -- Describable methods --
	@Override public @NotNull String getName() { return name; }
	@Override public void setName(@NotNull String name) { this.name = name; }

	@Override public @Nullable String getDescription() { return description; }
	@Override public void setDescription(@Nullable String description) { this.description = description; }

	// -- Territorial methods --
	@Override public @NotNull HashSet<KingdomChunk> getTerritory() { return territory; }

	@Override public boolean isInTerritory(@NotNull KingdomChunk chunk) { return territory.contains(chunk); }
	@Override public boolean isInTerritory(Chunk chunk) { return territory.contains(new KingdomChunk(chunk)); }
	@Override public boolean isInTerritory(Location location) { return territory.contains(new KingdomChunk(location)); }

	@Override public boolean claimTerritory(KingdomChunk chunk) { return territory.add(chunk); }
	@Override public boolean claimTerritory(Chunk chunk) { return territory.add(new KingdomChunk(chunk)); }
	@Override public boolean claimTerritory(Location location) { return territory.add(new KingdomChunk(location)); }

	@Override public void unclaimTerritory(KingdomChunk chunk) { territory.remove(chunk); }
	@Override public void unclaimTerritory(Chunk chunk) { territory.remove(new KingdomChunk(chunk)); }
	@Override public void unclaimTerritory(Location location) { territory.remove(new KingdomChunk(location)); }

	// -- Settlement methods --
	// - Chunk Flags -
	/**
	 * Returns the flags that are overridden for this settlement.
	 * <p>
	 *     <b>Note: </b>
	 *     You must check if a flag exists inside of the map, as it may not exist.
	 *     If it does not exist, the default flag should be used from the kingdom.
	 * </p>
	 * @return The flags that are overridden for this settlement.
	 */
	public @Nullable Map<KingdomFlag, Boolean> getOverrideFlags() { return overrideFlags; }

	/**
	 * Checks if the flag exists in the overridden flags.
	 * @param flag The flag to check.
	 * @return Whether the flag exists in the overridden flags.
	 */
	public boolean isOverridingFlag(KingdomFlag flag) {
		if(overrideFlags == null) return false;
		return overrideFlags.containsKey(flag);
	}

	/**
	 * Gets the raw flag state, if exists, from the overridden flags.
	 * @param flag The flag to get the state of.
	 * @return The raw flag state, if exists, from the overridden flags. Otherwise, null.
	 */
	public @Nullable Boolean getFlagOverrideState(KingdomFlag flag) {
		if(overrideFlags == null) return null;
		return overrideFlags.get(flag);
	}

	/**
	 * Get the flag state for the specified flag.
	 * <p>
	 *     If the flag is overridden, the overridden flag state will be returned.
	 *     Otherwise, the default flag state from the kingdom will be returned.
	 * </p>
	 * @param flag The flag to get the state of.
	 * @return The flag state for the specified flag.
	 */
	public boolean getFlag(@NotNull KingdomFlag flag) {
		if(overrideFlags == null) return getOwningKingdom().getFlagState(flag);
		return overrideFlags.getOrDefault(flag, getOwningKingdom().getFlagState(flag));
	}

	/**
	 * Mass set the overridden flags for this settlement.
	 * @param overrideFlags The flags to override for this settlement.
	 */
	public void setOverrideFlags(@Nullable Map<KingdomFlag, Boolean> overrideFlags) {
		this.overrideFlags = overrideFlags;
	}

	/**
	 * Set the overridden flag for this settlement.
	 * @param flag The flag to override.
	 * @param state The state to override the flag with.
	 */
	public void setFlagOverride(KingdomFlag flag, boolean state) {
		if(overrideFlags == null) overrideFlags = new HashMap<>();
		overrideFlags.put(flag, state);
	}

	/**
	 * Remove the overridden flag for this settlement. If the flag does not exist, nothing will happen.
	 * @param flag The flag to remove the override for.
	 */
	public void removeFlagOverride(KingdomFlag flag) {
		if(overrideFlags == null) return;
		overrideFlags.remove(flag);
		if(overrideFlags.isEmpty()) overrideFlags = null;
	}

	@Override public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Settlement that)) return false;

		return owningKingdom.equals(that.owningKingdom)
			   && name.equals(that.name)
			   && Objects.equals(description, that.description)
			   && territory.equals(that.territory)
			   && Objects.equals(overrideFlags, that.overrideFlags);
	}

	@Override public int hashCode() {
		int result = owningKingdom.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + Objects.hashCode(description);
		result = 31 * result + territory.hashCode();
		result = 31 * result + Objects.hashCode(overrideFlags);
		return result;
	}
}