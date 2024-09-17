package net.foxavis.kingdoms.entity.locations;

import net.foxavis.kingdoms.entity.KingdomHeld;
import net.foxavis.kingdoms.entity.Territorial;
import net.foxavis.kingdoms.entity.primary.Kingdom;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

/**
 * A sector in a Kingdom, where the Kingdom's chunks are combined into a single object.
 * Do note, the associated Kingdom could have more than one sector and this sector could represent the Kingdom's entire territory.
 */
public class ClaimSector implements Territorial, KingdomHeld {

	@NotNull private UUID owningKingdom;
	@NotNull private HashSet<KingdomChunk> chunks;

	/**
	 * Creates an empty sector associated with a Kingdom.
	 * @param owningKingdom The Kingdom that owns this sector.
	 */
	public ClaimSector(@NotNull UUID owningKingdom) {
		this.owningKingdom = owningKingdom;
		this.chunks = new HashSet<>();
	}

//	public ClaimSector(Kingdom owningKingdom) {
//		this(owningKingdom.getUniqueId());
//	}

	/**
	 * Creates a sector with a collection of chunks associated with a Kingdom.
	 * @param owningKingdom The Kingdom that owns this sector.
	 * @param chunks The chunks that are part of this sector.
	 */
	public ClaimSector(@NotNull UUID owningKingdom, @NotNull Collection<KingdomChunk> chunks) {
		this(owningKingdom);
		this.chunks = new HashSet<>(chunks);
	}

	// -- KingdomHeld methods --
	@Override public Kingdom getOwningKingdom() { return null; } // TODO: Implement using the cache
	@Override public void setOwningKingdom(Kingdom kingdom) { this.owningKingdom = null; } // TODO: Implement using the Kingdom's UUID

	// -- Territorial methods --
	@Override public @NotNull HashSet<KingdomChunk> getTerritory() { return chunks; }

	@Override public boolean isInTerritory(@NotNull KingdomChunk chunk) { return chunks.contains(chunk); }
	@Override public boolean isInTerritory(Chunk chunk) { return chunks.contains(new KingdomChunk(chunk)); }
	@Override public boolean isInTerritory(Location location) { return chunks.contains(new KingdomChunk(location)); }

	@Override public boolean claimTerritory(KingdomChunk chunk) { return chunks.add(chunk); }
	@Override public boolean claimTerritory(Chunk chunk) { return chunks.add(new KingdomChunk(chunk)); }
	@Override public boolean claimTerritory(Location location) { return chunks.add(new KingdomChunk(location)); }

	@Override public void unclaimTerritory(KingdomChunk chunk) { chunks.remove(chunk); }
	@Override public void unclaimTerritory(Chunk chunk) { chunks.remove(new KingdomChunk(chunk)); }
	@Override public void unclaimTerritory(Location location) { chunks.remove(new KingdomChunk(location)); }

	// -- Object methods --
	@Override public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ClaimSector that)) return false;

		return owningKingdom.equals(that.owningKingdom) && chunks.equals(that.chunks);
	}

	@Override public int hashCode() {
		int result = owningKingdom.hashCode();
		result = 31 * result + chunks.hashCode();
		return result;
	}
}