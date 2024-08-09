package net.foxavis.kingdoms.cmds.sub;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.KingdomCommand;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class KAdminCommand extends KingdomSubcommand {

	public KAdminCommand() {
		super("admin", "Shows the admin commands", "admin", "foxavis.kingdoms.admin");
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		List<KingdomSubcommand> adminCommands = KingdomCommand.getInstance().getSubcommands()
				.stream()
				.filter(subcommand -> subcommand.getPermission() != null && subcommand.getPermission().startsWith("foxavis.kingdoms.admin"))
				.filter(subcommand -> !subcommand.getName().equalsIgnoreCase("admin"))
				.toList();

		if(args.length == 0) {
			KingdomSubcommand adminHelpCommand = adminCommands.stream()
					.filter(subcommand -> subcommand.getName().equalsIgnoreCase("admin help"))
					.findAny()
					.orElse(null);

			if (adminHelpCommand == null) {
				sender.sendMessage(Component.text("An error occurred while executing this command, try again or contact an admin!").color(NamedTextColor.RED));
				return true;
			}

			return adminHelpCommand.executeSubcommand(plugin, sender, args);
		}

		KingdomSubcommand subcommand = adminCommands.stream()
				.filter(item -> item.getName().equalsIgnoreCase("admin " + args[0]))
				.findAny()
				.orElse(null);

		if(subcommand == null) {
			sender.sendMessage(Component.text("That subcommand does not exist, try /k admin help for a list of subcommands.").color(NamedTextColor.RED));
			return true;
		}

		if(subcommand.getPermission() != null && !sender.hasPermission(subcommand.getPermission())) {
			sender.sendMessage(Component.text("You do not have permission to use this subcommand.").color(NamedTextColor.RED));
			return true;
		}

		return subcommand.executeSubcommand(plugin, sender, Arrays.copyOfRange(args, 1, args.length));
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(args.length == 0) return null;

		List<KingdomSubcommand> adminCommands = KingdomCommand.getInstance().getSubcommands()
				.stream()
				.filter(subcommand -> subcommand.getPermission() != null && subcommand.getPermission().startsWith("foxavis.kingdoms.admin"))
				.filter(subcommand -> !subcommand.getName().equalsIgnoreCase("admin"))
				.toList();

		if(args.length == 1) {
			return adminCommands.stream()
					.map(KingdomSubcommand::getName)
					.map(name -> Stream.of(name.split(" ")).reduce((first, second) -> second).orElse(""))
					.filter(name -> name.startsWith(args[0]))
					.toList();
		}

		KingdomSubcommand subcommand = adminCommands.stream()
				.filter(item -> item.getName().equalsIgnoreCase("admin " + args[0]))
				.findAny()
				.orElse(null);

		if(subcommand == null) return null;
		if(subcommand.getPermission() != null && !sender.hasPermission(subcommand.getPermission())) return null;

		return subcommand.tabCompleteSubcommand(plugin, sender, Arrays.copyOfRange(args, 1, args.length));
	}
}