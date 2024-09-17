package net.foxavis.kingdoms.entity;

import net.foxavis.kingdoms.entity.primary.Kingdom;

/**
 * An interface to declare that an object is controlled or held by a specific kingdom.
 * @author Kyomi
 */
public interface KingdomHeld {

	/**
	 * Returns the kingdom that holds this object.
	 * @return The kingdom who owns this object.
	 */
	Kingdom getOwningKingdom();

	/**
	 * Sets a kingdom to hold this object, replacing the previous owner.
	 * @param kingdom The kingdom to set as the owner.
	 */
	void setOwningKingdom(Kingdom kingdom);

}