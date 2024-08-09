package net.foxavis.kingdoms.cmds.obj;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class KingdomSubcommand {

	private final String name;
	private final String usage;
	@Nullable private final String description;
	@Nullable private final String permission;

	/**
	 * Creates a new subcommand, with all options specified.
	 * @param name The name of the subcommand
	 * @param description The description of the subcommand
	 * @param usage The usage of the subcommand
	 * @param permission The permission of the subcommand, or null if there is no permission
	 */
	public KingdomSubcommand(String name, @Nullable String description, String usage, @Nullable String permission) {
		this.name = name;
		this.usage = usage;

		this.description = description;
		this.permission = permission;
	}

	public KingdomSubcommand(String name, @Nullable String description, String usage) { this(name, description, usage, null); }
	public KingdomSubcommand(String name, @Nullable String description) { this(name, description, name, null); }
	public KingdomSubcommand(String name) { this(name, null, name, null); }

	/**
	 * Executes the command, after checking if the sender has permission.
	 * <p>This is what the command handler should use.</p>
	 * @param plugin The plugin instance
	 * @param sender The sender of the command
	 * @param args The arguments of the command
	 * @return Whether the command was executed successfully
	 */
	public boolean executeSubcommand(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if (permission != null && !sender.hasPermission(permission)) {
			sender.sendMessage(Component.text("You do not have permission to execute this command.").color(NamedTextColor.RED));
			return true;
		}

		return execute(plugin, sender, args);
	}

	/**
	 * Generates a list of possible completions for the command, before checking if the sender has permission.
	 * <p>This is what the command handler should use.</p>
	 * @param plugin The plugin instance
	 * @param sender The sender of the command
	 * @param args The arguments of the command
	 * @return A list of possible completions for the command
	 */
	@Nullable public List<String> tabCompleteSubcommand(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if (permission != null && !sender.hasPermission(permission)) {
			return null;
		}

		return tabComplete(plugin, sender, args);
	}

	/**
	 * Executes the command, this is called after checking if the sender has permission, if applicable.
	 * <p><b>Note:</b> The arguments are shifted over to not include the subcommand's name.</p>
	 * @param plugin The plugin instance
	 * @param sender The sender of the command
	 * @param args The arguments of the command
	 * @return Whether the command was executed successfully
	 */
	protected abstract boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args);

	/**
	 * Generates a list of possible completions for the command, this is called after checking if the sender has permission, if applicable.
	 * <p><b>Note:</b> The arguments are shifted over to not include the subcommand's name.</p>
	 * @param plugin The plugin instance
	 * @param sender The sender of the command
	 * @param args The arguments of the command
	 * @return A list of possible completions for the command
	 */
	@Nullable protected abstract List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args);

	/**
	 * Gets the name of the subcommand.
	 * @return The name of the subcommand
	 */
	public String getName() { return name; }

	/**
	 * Gets the usage of the subcommand.
	 * @return The usage of the subcommand
	 */
	public String getUsage() { return usage; }

	/**
	 * Gets the description of the subcommand.
	 * @return The description of the subcommand, or null if there is no description
	 */
	@Nullable public String getDescription() { return description; }

	/**
	 * Gets the permission of the subcommand.
	 * @return The permission of the subcommand, or null if there is no permission
	 */
	@Nullable public String getPermission() { return permission; }

	@Override public String toString() {
		return "KingdomSubcommand{" +
			   "name='" + name + '\'' +
			   ", usage='" + usage + '\'' +
			   ", description='" + description + '\'' +
			   ", permission='" + permission + '\'' +
			   '}';
	}
}