package net.foxavis.kingdoms.enums;

import java.util.HashMap;
import java.util.Map;

public enum KingdomFlags {

	/** An "Open" kingdom means that anyone may join, invitation or not.  */
	OPEN("can the kingdom be joined without an invitation?"),

	/** If active, kingdom members can attack another kingdom member. */
	FRIENDLY_FIRE("can members hurt each other?"),

	/** If active, no one may attack inside your kingdom land. */
	PACIFISM("can anyone attack anyone in the kingdom?");

	// -- Fields --
	private final String description;

	// -- Constructor --
	KingdomFlags(String description) {
		this.description = description;
	}

	// -- Methods --
	public String getDesc() {
		return description;
	}

	// -- Static --
	public static final Map<KingdomFlags, Boolean> Default = new HashMap<>() {{
		put(OPEN, false);
		put(FRIENDLY_FIRE, false);
		put(PACIFISM, false);
	}};
}