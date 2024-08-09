package net.foxavis.kingdoms.enums;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.swing.UIManager.put;

public enum KingdomRank {

	RECRUIT("Recruit", "a recruit in the kingdom", "recruits in the kingdom"),
	MEMBER("Member", "a member in the kingdom", "members in the kingdom"),
	OFFICER("Officer", "an officer in your kingdom", "officers in the kingdom"),
	LEADER("Leader", "the kingdom's leader", "kingdom leaders");

	// -- Fields -- \\
	private final String name;
	private final String describeOne;
	private final String describeMany;

	// -- Constructor -- \\
	KingdomRank(String name, String describeOne, String describeMany) {
		this.name = name;
		this.describeOne = describeOne;
		this.describeMany = describeMany;
	}

	// -- Getters -- \\
	public String getName() { return name; }
	public String getDescribeOne() { return describeOne; }
	public String getDescribeMany() { return describeMany; }

	// -- Methods -- \\
	public int value() { return this.ordinal(); }

	@Nullable public KingdomRank next() {
		if(this.value() == KingdomRank.values().length - 1) return null;
		return KingdomRank.values()[this.value() + 1];
	}

	@Nullable public KingdomRank previous() {
		if(this.value() == 0) return null;
		return KingdomRank.values()[this.value() - 1];
	}

	// -- Comparisons -- \\
//	public boolean isAtLeast(KingdomRank rank) { return this.value() >= rank.value(); }
//	public boolean isMoreThan(KingdomRank rank) { return this.value() > rank.value(); }
//
//	public boolean isAtMost(KingdomRank rank) { return this.value() <= rank.value(); }
//	public boolean isLessThan(KingdomRank rank) { return this.value() < rank.value(); }

	/**
	 * Checks if this rank is LOWER than the given rank
	 * @param rank The rank to compare
	 * @return True if this rank is lower than the given rank, false otherwise
	 */
	public boolean isLessThan(KingdomRank rank) { return this.value() < rank.value(); }

	/**
	 * Checks if this rank is HIGHER than the given rank
	 * @param rank The rank to compare
	 * @return True if this rank is higher than this rank, false otherwise
	 */
	public boolean isGreaterThan(KingdomRank rank) { return this.value() > rank.value(); }

	/**
	 * Checks if this rank is EQUAL or LOWER than the given rank
	 * @param rank The rank to compare
	 * @return True if this rank is equal or lower than this rank, false otherwise
	 */
	public boolean isAtLeast(KingdomRank rank) { return this.value() <= rank.value(); }

	/**
	 * Checks if this rank is EQUAL or HIGHER than this rank
	 * @param rank The rank to compare
	 * @return True if this rank is equal or higher than this rank, false otherwise
	 */
	public boolean isAtMost(KingdomRank rank) { return this.value() >= rank.value(); }

	// -- Static -- \\
	public static final Map<KingdomRank, List<KingdomPerms>> Default = new HashMap<>() {{
		put(KingdomRank.RECRUIT, List.of(
				// Recruit
				KingdomPerms.WORLD_DOORS, KingdomPerms.WORLD_SWITCHES, KingdomPerms.CMD_HOME, KingdomPerms.CMD_WARPS, KingdomPerms.CMD_DEPOSIT, KingdomPerms.CMD_BALANCE
		));
		put(KingdomRank.MEMBER, List.of(
				// Recruit
				KingdomPerms.WORLD_DOORS, KingdomPerms.WORLD_SWITCHES, KingdomPerms.CMD_HOME, KingdomPerms.CMD_WARPS, KingdomPerms.CMD_DEPOSIT, KingdomPerms.CMD_BALANCE,

				// Member
				KingdomPerms.WORLD_CONTAINER, KingdomPerms.WORLD_BUILD, KingdomPerms.CMD_MANAGE_WARPS
		));
		put(KingdomRank.OFFICER, List.of(
				// Recruit
				KingdomPerms.WORLD_DOORS, KingdomPerms.WORLD_SWITCHES, KingdomPerms.CMD_HOME, KingdomPerms.CMD_WARPS, KingdomPerms.CMD_DEPOSIT, KingdomPerms.CMD_BALANCE,

				// Member
				KingdomPerms.WORLD_CONTAINER, KingdomPerms.WORLD_BUILD, KingdomPerms.CMD_MANAGE_WARPS,

				// Officer
				KingdomPerms.CMD_DESCRIPTION, KingdomPerms.CMD_MOTD, KingdomPerms.CMD_TAG, KingdomPerms.CMD_INVITE, KingdomPerms.CMD_KICK, KingdomPerms.CMD_TERRITORY,
				KingdomPerms.CMD_WITHDRAW, KingdomPerms.CMD_FLAGS, KingdomPerms.CMD_RELATIONS, KingdomPerms.CMD_PERMS, KingdomPerms.CMD_SETHOME
		));
		put(KingdomRank.LEADER, Arrays.asList(KingdomPerms.values())); // Leader has permission for everything
	}};
}
