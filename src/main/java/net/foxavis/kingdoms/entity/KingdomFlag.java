package net.foxavis.kingdoms.entity;

/**
 * This enum represents the flags that can be set on a kingdom or settlement (if applicable).
 * Each flag has a human-readable name, a description of what it does, a default value, and whether it can be changed by a non-admin player.
 * @see net.foxavis.kingdoms.entity.primary.Kingdom
 * @see net.foxavis.kingdoms.entity.primary.Settlement
 * @author Kyomi
 */
public enum KingdomFlag {

	// -- Flags -- \\
	OPEN("Open", "Can the kingdom be joined without an invitation?", false, true, true),
	MONSTERS("Monsters", "Can monsters spawn in the kingdom?", true, true, true),
	ANIMALS("Animals", "Can animals spawn in the kingdom?", true, true, true),
	FRIENDLY_FIRE("Friendly Fire", "Can kingdom members hurt each other?", false, true, true),
	EXPLOSIONS("Explosions", "Can explosions occur in the kingdom?", true, true, true),

	// -- Admin Flags -- \\ (cannot be changed on settlements)
	PERMANENT("Permanent", "Is the kingdom permanent, or will it be disbanded after a period of inactivity?", false, false, false),
	PEACEFUL("Peaceful", "Is the Kingdom peaceful, no player may PVP if so.", false, false, false),
	INFINITE_POWER("Infinite Power", "Does the Kingdom have infinite power?", false, false, false);

	private final String readableName; // The name of this flag
	private final String description; // A description of what this flag does

	private final boolean value; // The default value of this flag
	private final boolean isEditable; // Whether this flag can be changed by a non-admin player
	private final boolean settlementable; // Whether this flag can be changed on settlements

	/**
	 * Creates a new kingdom flag with the given properties.
	 * @param readableName The name of this flag
	 * @param description A description of what this flag does
	 * @param value The default value of this flag
	 * @param isEditable Whether this flag can be changed by a non-admin player
	 * @param settlementable Whether this flag can be changed on settlements
	 */
	KingdomFlag(String readableName, String description, boolean value, boolean isEditable, boolean settlementable) {
		this.readableName = readableName;
		this.description = description;
		this.value = value;
		this.isEditable = isEditable;
		this.settlementable = settlementable;
	}

	/**
	 * Returns a human-readable name of this flag, for display purposes.
	 * @return The name of this flag.
	 */
	public String getReadableName() { return readableName; }

	/**
	 * Returns the description of this flag, describing what it does.
	 * @return The description of this flag.
	 */
	public String getDescription() { return description; }

	/**
	 * Returns the default value of this flag when a kingdom is created.
	 * @return The default value of this flag.
	 */
	public boolean defaultValue() { return value; }

	/**
	 * Returns whether this flag can be changed by a non-admin player.
	 * @return Is this flag editable by non-admin players?
	 */
	public boolean isEditable() { return isEditable; }

	/**
	 * Returns whether this flag can be changed on settlements.
	 * @return Can this flag be changed on settlements?
	 */
	public boolean isSettlementable() { return settlementable; }
}
