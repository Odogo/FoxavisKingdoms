package net.foxavis.kingdoms.entity;

import net.foxavis.kingdoms.entity.primary.Kingdom;

/**
 * An interface to declare that an object is
 */
public interface KingdomHeld {

	Kingdom getOwningKingdom();

	void setOwningKingdom(Kingdom kingdom);

}