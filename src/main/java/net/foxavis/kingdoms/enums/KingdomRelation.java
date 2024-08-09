package net.foxavis.kingdoms.enums;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.Nullable;

public enum KingdomRelation {

	AT_WAR("At War", "at war with you", "at war with you", "a kingdom at war", "kingdoms at war", NamedTextColor.DARK_RED),
	ENEMY("Enemy" , "an enemy", "enemies", "an enemy kingdom", "enemy kingdoms", NamedTextColor.RED),
	NEUTRAL("Neutral", "someone neutral to you", "those neutral to you", "a neutral faction", "neutral factions", null),
	TRUCE("Truce", "someone in truce with you", "those in truce with you", "a kingdom in truce", "kingdoms in truce", NamedTextColor.DARK_AQUA),
	ALLY("Ally", "an ally", "allies", "an allied kingdom", "allied kingdoms", NamedTextColor.LIGHT_PURPLE);

	// -- Fields --

	private final String name;
	private final String descOnePlayer;
	private final String descManyPlayer;
	private final String descOneFaction;
	private final String descManyFaction;

	@Nullable private final TextColor playerNameColor;

	// -- Methods --

	public int value() { return this.ordinal(); }

	public String getName() { return name; }
	public String getDescOnePlayer() { return descOnePlayer; }
	public String getDescManyPlayer() { return descManyPlayer; }
	public String getDescOneFaction() { return descOneFaction; }
	public String getDescManyFaction() { return descManyFaction; }
	public @Nullable TextColor getPlayerNameColor() { return playerNameColor; }

	// -- Constructor --

	KingdomRelation(String name, String descOnePlayer, String descManyPlayer, String descOneFaction, String descManyFaction, @Nullable TextColor playerNameColor) {
		this.name = name;
		this.descOnePlayer = descOnePlayer;
		this.descManyPlayer = descManyPlayer;
		this.descOneFaction = descOneFaction;
		this.descManyFaction = descManyFaction;
		this.playerNameColor = playerNameColor;
	}

	// -- Comparisons --

	public boolean isAtLeast(KingdomRelation rel) { return this.value() >= rel.value(); }
	public boolean isMoreThan(KingdomRelation rel) { return this.value() > rel.value(); }

	public boolean isAtMost(KingdomRelation rel) { return this.value() <= rel.value(); }
	public boolean isLessThan(KingdomRelation rel) { return this.value() < rel.value(); }
}