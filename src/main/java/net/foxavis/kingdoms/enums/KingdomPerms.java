package net.foxavis.kingdoms.enums;

public enum KingdomPerms {

	// Total: 24

	// -- World-based Permissions -- (4)
	WORLD_CONTAINER("use containers (chest, furnaces, etc.) in the territory"),
	WORLD_DOORS("use doors in the territory"),
	WORLD_SWITCHES("use buttons and levers in the territory"),
	WORLD_BUILD("place/break blocks in the territory"),

	// -- Commands -- (20)
	// - Display - (3)
	CMD_NAME("rename the kingdom"),
	CMD_DESCRIPTION("change the description"),
	CMD_MOTD("change the message of the day"),
	CMD_TAG("change the tag of the kingdom"),

	// - Members - (4)
	CMD_INVITE("manage invitations of the kingdom"),
	CMD_RESIGN("resign from the kingdom"), // leader only, leader is allowed to select a replacement
	CMD_DISBAND("disband the kingdom"), // leader only
	CMD_KICK("evict a player from the kingdom"),
	MANAGE_MEMBERS("manage kingdom member's rank"),

	// - Territory - (5)
	CMD_TERRITORY("manage kingdom territory"),
	CMD_HOME("teleport to the kingdom's home"),
	CMD_SETHOME("sets the kingdom's home"),
	CMD_WARPS("teleport to any of the kingdom's warps"),
	CMD_MANAGE_WARPS("manage kingdom warps"),

	// - Money, Taxes & Banking - (5)
	CMD_DEPOSIT("deposit/donate to the kingdom's riches"),
	CMD_WITHDRAW("withdraw from the kingdom's riches"),
	CMD_BALANCE("view the amount of kingdom riches"),

	// - Misc (maps) - (3)
	CMD_PERMS("manage kingdom permissions of those below you"),
	CMD_FLAGS("manage kingdom flags"),
	CMD_RELATIONS("manage kingdom relations");

	// -- Fields --
	private final String description;

	// -- Constructor --
	KingdomPerms(String description) {
		this.description = description;
	}

	// -- Methods --
	public String getDesc() { return description; }
}