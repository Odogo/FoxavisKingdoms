package net.foxavis.kingdoms.entity;

public enum KingdomFlag {

	OPEN("Open", "Can the kingdom be joined without an invitation?", false, true),
	MONSTERS("Monsters", "Can monsters spawn in the kingdom?", true, true),
	ANIMALS("Animals", "Can animals spawn in the kingdom?", true, true),
	FRIENDLY_FIRE("Friendly Fire", "Can kingdom members hurt each other?", false, true),
	EXPLOSIONS("Explosions", "Can explosions occur in the kingdom?", true, true),

	PERMANENT("Permanent", "Is the kingdom permanent, or will it be disbanded after a period of inactivity?", false, false),
	PEACEFUL("Peaceful", "Is the Kingdom peaceful, no player may PVP if so.", false, false),
	INFINITE_POWER("Infinite Power", "Does the Kingdom have infinite power?", false, false);

	private final String readableName; // The name of this flag
	private final String description; // A description of what this flag does
	private final boolean value; // The default value of this flag
	private final boolean isEditable; // Whether this flag can be changed by a non-admin player

	/**
	 * Creates a new kingdom flag with the given properties.
	 * @param readableName The name of this flag
	 * @param description A description of what this flag does
	 * @param value The default value of this flag
	 * @param isEditable Whether this flag can be changed by a non-admin player
	 */
	KingdomFlag(String readableName, String description, boolean value, boolean isEditable) {
		this.readableName = readableName;
		this.description = description;
		this.value = value;
		this.isEditable = isEditable;
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
}
