package net.foxavis.kingdoms.entity;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the relationship between two kingdoms.
 * <p>
 *     The relationship between two kingdoms can be one of five types: At War, Enemy, Neutral, Truce, or Ally.
 *     Each of these types has a display-friendly name, a description of the relationship when referring to a single player,
 *     a description of the relationship when referring to multiple players, a description of the relationship when referring to a single kingdom,
 *     a description of the relationship when referring to multiple kingdoms, and a color that the name of the relationship should be displayed in.
 * </p>
 * @see net.foxavis.kingdoms.entity.primary.Kingdom
 * @author Kyomi
 */
public enum KingdomRelation {

	AT_WAR("At War", "at war", "at war", "a kingdom at war", "kingdoms at war", NamedTextColor.DARK_RED),
	ENEMY("Enemy", "an enemy", "enemies", "an enemy kingdom", "enemy kingdoms", NamedTextColor.RED),
	NEUTRAL("Neutral", "neutral", "neutral", "a neutral kingdom", "neutral kingdoms", null),
	TRUCE("Truce", "in a truce", "in a truce", "a kingdom in a truce", "kingdoms in a truce", NamedTextColor.DARK_AQUA),
	ALLY("Ally", "an ally", "allies", "an allied kingdom", "allied kingdoms", NamedTextColor.LIGHT_PURPLE);

	// -- Fields -- \\
	private final String name;
	private final String descriptionOnePlayer;
	private final String descriptionManyPlayers;
	private final String descriptionOneKingdom;
	private final String descriptionManyKingdoms;

	@Nullable private final TextColor nameColor;

	// -- Constructor -- \\
	KingdomRelation(String name, String descOnePlayer, String descManyPlayers, String descOneKingdom, String descManyKingdoms, @Nullable TextColor nameColor) {
		this.name = name;
		this.descriptionOnePlayer = descOnePlayer;
		this.descriptionManyPlayers = descManyPlayers;
		this.descriptionOneKingdom = descOneKingdom;
		this.descriptionManyKingdoms = descManyKingdoms;
		this.nameColor = nameColor;
	}

	// -- Getters -- \\

	/**
	 * Returns the value of the enum constant.
	 * @return The value of the enum constant.
	 * @see KingdomRelation#ordinal()
	 */
	public int value() { return this.ordinal(); }

	/**
	 * Returns a display-friendly name for the relation. This should be displayed to the player.
	 * @return The display-friendly name for the relation.
	 */
	public String getName() { return this.name; }

	/**
	 * Returns a description of the relation when referring to a single player.
	 * @return The description of the relation when referring to a single player.
	 */
	public String getDescriptionOnePlayer() { return this.descriptionOnePlayer; }

	/**
	 * Returns a description of the relation when referring to multiple players.
	 * @return The description of the relation when referring to multiple players.
	 */
	public String getDescriptionManyPlayers() { return this.descriptionManyPlayers; }

	/**
	 * Returns a description of the relation when referring to a single kingdom.
	 * @return The description of the relation when referring to a single kingdom.
	 */
	public String getDescriptionOneKingdom() { return this.descriptionOneKingdom; }

	/**
	 * Returns a description of the relation when referring to multiple kingdoms.
	 * @return The description of the relation when referring to multiple kingdoms.
	 */
	public String getDescriptionManyKingdoms() { return this.descriptionManyKingdoms; }

	/**
	 * Returns the color that the name of the relation should be displayed in.
	 * @return The color that the name of the relation should be displayed in.
	 */
	@Nullable public TextColor getNameColor() { return this.nameColor; }

	/**
	 * Translates a TextComponent representing the name of a player into a hoverable TextComponent that describes the player's relation to the kingdom.
	 * @param player The player whose name is being translated.
	 * @return A hoverable TextComponent that describes the player's relation to the kingdom, with the player's name as the text.
	 */
	public TextComponent translatePlayerName(Player player) {
		return this.nameColor == null ?
				Component.text(player.getName()) :
				Component.text(player.getName())
						.color(this.nameColor)
						.hoverEvent(
								HoverEvent.showText(
										Component.text()
												.append(Component.text("This player is ", NamedTextColor.YELLOW))
												.append(Component.text(this.descriptionOnePlayer, this.nameColor))
												.append(Component.text(" to your kingdom.", NamedTextColor.YELLOW))
								)
						);
	}
}