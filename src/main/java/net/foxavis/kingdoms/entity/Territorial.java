package net.foxavis.kingdoms.entity;

import net.foxavis.kingdoms.entity.locations.KingdomChunk;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.HashSet;

/**
 * An interface to declare that an object can claim chunks and hold territory within the world.
 * <p>
 *     When implementing this interface, the implementing class should provide a {@link java.util.HashSet<KingdomChunk>}
 *     to store the chunks that are claimed by the territory. This set should be used to determine if a chunk is
 *     claimed by the territory, and to add or remove chunks from the territory.
 * </p>
 * <p>
 *     Please do not use any {@link java.util.List} implementations to store the territory, as the order of the chunks
 *     is not important and a {@link java.util.HashSet} is more efficient for checking if a chunk is claimed.
 * </p>
 * @author Kyomi
 */
public interface Territorial {

	/**
	 * Returns a list of all chunks currently claimed by this territory.
	 * @return A list of all claimed chunks associated with this territory.
	 */
	HashSet<KingdomChunk> getTerritory();

	/**
	 * Checks if the specified {@link KingdomChunk} is claimed by this territory.
	 * @param chunk The chunk to check.
	 * @return True if the chunk is claimed, false otherwise.
	 */
	boolean isInTerritory(KingdomChunk chunk);

	/**
	 * Checks if the specified {@link Chunk} is claimed by this territory.
	 * @param chunk The chunk to check.
	 * @return True if the chunk is claimed, false otherwise.
	 */
	boolean isInTerritory(Chunk chunk);

	/**
	 * Checks if the chunk corresponding to the specified {@link Location} is claimed by this territory.
	 * @param location The location to check.
	 * @return True if the chunk containing the location is claimed, false otherwise.
	 */
	boolean isInTerritory(Location location);

	/**
	 * Adds the specified chunk to the list of chunks claimed by this territory
	 * @param chunk The chunk to be claimed and associated with this territory.
	 * @return True, if the chunk was claimed, false if it's already claimed by this territory.
	 */
	boolean claimTerritory(KingdomChunk chunk);

	/**
	 * Adds the specified chunk to the list of chunks claimed by this territory.
	 * @param chunk The chunk to be claimed and associated with this territory.
	 * @return True, if the chunk was claimed, false if it's already claimed by this territory.
	 */
	boolean claimTerritory(Chunk chunk);

	/**
	 * Claims the chunk corresponding to the specified location and associates it with this territory.
	 * @param location The location from which to claim the corresponding chunk.
	 * @return True, if the chunk was claimed, false if it's already claimed by this territory.
	 */
	boolean claimTerritory(Location location);

	/**
	 * Removes the specified chunk from the list of chunks claimed by this territory.
	 * @param chunk The chunk to be unclaimed and disassociated from this territory.
	 */
	void unclaimTerritory(KingdomChunk chunk);

	/**
	 * Removes the specified chunk from the list of chunks claimed by this territory.
	 *
	 * @param chunk The chunk to be unclaimed and disassociated from this territory.
	 */
	void unclaimTerritory(Chunk chunk);

	/**
	 * Unclaims the chunk corresponding to the specified location, disassociating it from this territory.
	 *
	 * @param location The location from which to unclaim the corresponding chunk.
	 */
	void unclaimTerritory(Location location);

}
