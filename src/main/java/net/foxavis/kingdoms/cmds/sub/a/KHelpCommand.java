package net.foxavis.kingdoms.cmds.sub.a;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.KingdomCommand;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class KHelpCommand extends KingdomSubcommand {

	public KHelpCommand() {
		super("help", "Shows the list of subcommands", "help [p:<page> l:<per page>]", null);
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		try {
			int page = Arrays.stream(args) // Get the page from the arguments, or default to 1
					.filter(item -> item.startsWith("p:")) // Filter out any arguments that don't start with "p:"
					.map(item -> item.substring(2)) // Remove the "p:" from the argument
					.map(Integer::parseInt) // Parse the argument as an integer
					.findAny() // Get the first value in the list
					.orElse(1); // If there are no values in the list, default to 1
			int limit = Arrays.stream(args) // Get the limit from the arguments, or default to 10
					.filter(item -> item.startsWith("l:")) // Filter out any arguments that don't start with "l:"
					.map(item -> item.substring(2)) // Remove the "l:" from the argument
					.map(Integer::parseInt) // Parse the argument as an integer
					.findAny() // Get the first value in the list
					.orElse(10); // If there are no values in the list, default to 10

			// Make sure the limit is between 1 and 20
			if(limit < 1 || limit > 20) {
				sender.sendMessage(Component.text("The limit must be between 1 and 20.").color(NamedTextColor.RED));
				return true;
			}

			// Get the list of subcommands and calculate the max page
			List<KingdomSubcommand> subcommands = KingdomCommand.getInstance().getSubcommands().stream().sorted(Comparator.comparing(KingdomSubcommand::getName)).toList();
			final int maxPage = (int) Math.ceil(subcommands.size() / (double) limit);

			// Make sure the page is between 1 and the max page
			if(page > maxPage) page = maxPage;

			// If there are no subcommands, send an error message
			if(maxPage == 0) {
				sender.sendMessage(Component.text("There are no subcommands available.").color(NamedTextColor.RED));
				return true;
			}

			// Send the list of subcommands
			sender.sendMessage(Component.text("-- Page " + page + "/" + maxPage + " --").color(NamedTextColor.GOLD));
			for(int i = (page - 1) * limit; i < page * limit; i++) {
				if(i >= subcommands.size()) break;
				KingdomSubcommand subcommand = subcommands.get(i);
//				sender.sendMessage(ChatColor.GOLD + "/k " + ChatColor.YELLOW + subcommand.getUsage() + ChatColor.RESET + " - " + ChatColor.GRAY + subcommand.getDescription());
				sender.sendMessage(Component.text("/k ").color(NamedTextColor.GOLD)
						.append(Component.text(subcommand.getUsage()).color(NamedTextColor.YELLOW).clickEvent(ClickEvent.suggestCommand("/k " + subcommand.getUsage().split(" ")[0])))
						.append(Component.text(" -").color(NamedTextColor.LIGHT_PURPLE))
						.append(Component.text(" " + subcommand.getDescription()).color(NamedTextColor.GRAY)));
			}

			// Return true to indicate that the command was executed successfully
			return true;
		} catch (NumberFormatException e) {
			// If there was an error parsing the arguments, send an error message
			sender.sendMessage(Component.text("One or more arguments were invalid. Please check your syntax and try again.").color(NamedTextColor.RED));
			return true;
		}
	}

	@Nullable @Override protected List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		List<KingdomSubcommand> subcommands = KingdomCommand.getInstance().getSubcommands();

		int limit = Arrays.stream(args) // Get the limit from the arguments, or default to 10
				.filter(item -> item.startsWith("l:")) // Filter out any arguments that don't start with "l:"
				.map(item -> item.substring(2)) // Remove the "l:" from the argument
				.map(item -> { // Try to parse the argument as an integer, or return null if it fails
					try {
						return Integer.parseInt(item);
					} catch (NumberFormatException e) {
						return null;
					}
				})
				.filter(Objects::nonNull) // Remove any null values from the list
				.findAny() // Get the first value in the list
				.orElse(10); // If there are no values in the list, default to 10
		int maxPage = (int) Math.ceil(subcommands.size() / (double) limit);

		// Show the current argument options for each type of argument
		String current = args[args.length - 1];
		if(current.startsWith("p:")) return List.of("<page: 1-" + maxPage + "> [using limit: " + limit + "]");
		if(current.startsWith("l:")) return List.of("<limit per page: 1-20>");

		// Remove any options that have already been used, so they don't show up in tab complete
		List<String> options = new ArrayList<>(List.of("l:", "p:"));
		options.removeIf(item -> Arrays.stream(args).anyMatch(arg -> arg.startsWith(item)));
		return options;
	}
}