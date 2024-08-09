package net.foxavis.kingdoms.cmds;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class KingdomCommand implements TabExecutor {

	private static KingdomCommand instance = null;
	public static KingdomCommand getInstance() { return instance; }

	private final FoxavisKingdoms plugin;
	private final List<KingdomSubcommand> subcommands;

	public KingdomCommand(FoxavisKingdoms plugin) {
		instance = this;

		this.plugin = plugin;
		this.subcommands = new ArrayList<>();

		registerSubcommands();
	}

	public List<KingdomSubcommand> getSubcommands() { return subcommands; }

	@Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if(args.length == 0) {
			KingdomSubcommand subcommand = subcommands.stream()
					.filter(item -> item.getName().equalsIgnoreCase("help"))
					.findAny()
					.orElse(null);
			if(subcommand == null) {
				sender.sendMessage(Component.text("An error occurred while executing this command, try again or contact an admin!").color(NamedTextColor.RED));
				return true;
			}

			return subcommand.executeSubcommand(plugin, sender, args);
		}

		KingdomSubcommand subcommand = subcommands.stream()
				.filter(item -> item.getName().equalsIgnoreCase(args[0]))
				.findAny()
				.orElse(null);
		if(subcommand == null) {
			sender.sendMessage(Component.text("That subcommand does not exist, try ").color(NamedTextColor.RED)
					.append(Component.text("/k help").color(NamedTextColor.YELLOW))
					.append(Component.text(" for a list of subcommands.").color(NamedTextColor.RED)));
			return true;
		}

		if(subcommand.getPermission() != null && !sender.hasPermission(subcommand.getPermission())) {
			sender.sendMessage(Component.text("You do not have permission to use this command!").color(NamedTextColor.RED));
			return true;
		}

		return subcommand.executeSubcommand(plugin, sender, Arrays.copyOfRange(args, 1, args.length));
	}

	@Nullable @Override public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if(args.length == 0) return null;
		if(args.length == 1)
			return subcommands
					.stream()
					.map(KingdomSubcommand::getName)
					.filter(item -> !item.startsWith("admin "))
					.filter(item -> item.startsWith(args[0]))
					.toList();

		KingdomSubcommand subcommand = subcommands
				.stream()
				.filter(item -> item.getName().equalsIgnoreCase(args[0]))
				.findAny()
				.orElse(null);
		if(subcommand == null) return null;

		if(subcommand.getPermission() != null && !sender.hasPermission(subcommand.getPermission())) return null;

		return subcommand.tabCompleteSubcommand(plugin, sender, Arrays.copyOfRange(args, 1, args.length));
	}

	private void registerSubcommands() {
		final String packageName = "net.foxavis.kingdoms.cmds.sub";
		try {
			for (Class<?> clazz : plugin.findClasses(packageName)) {
				if (KingdomSubcommand.class.isAssignableFrom(clazz)) {
					KingdomSubcommand subcommand = (KingdomSubcommand) clazz.getConstructor().newInstance();
					subcommands.add(subcommand);
				}
			}
		} catch (Exception e) {
			plugin.getLogger().log(Level.WARNING, "Failed to register subcommands", e);
		}
	}
}
