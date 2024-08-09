package net.foxavis.kingdoms.cmds.sub.a;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class KListCommand extends KingdomSubcommand {

	public KListCommand() {
		super("list", "Lists all kingdoms", "list [f:<name filter> l:<per page (1-20)> p:<page>]", null);
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		try {
			int page = Arrays.stream(args) // Get the page from the arguments, or default to 1
					.filter(item -> item.startsWith("p:")) // Filter out any arguments that don't start with "p:"
					.map(item -> item.substring(2)) // Remove the "p:" from the argument
					.map(Integer::parseInt) // Parse the argument as an integer
					.findAny().orElse(1); // If there are no values in the list, default to 1
			int limit = Arrays.stream(args) // Get the limit from the arguments, or default to 10
					.filter(item -> item.startsWith("l:")) // Filter out any arguments that don't start with "l:"
					.map(item -> item.substring(2)) // Remove the "l:" from the argument
					.map(Integer::parseInt) // Parse the argument as an integer
					.findAny().orElse(10); // If there are no values in the list, default to 10
			String filter = Arrays.stream(args) // Get the filter from the arguments, or default to null
					.filter(item -> item.startsWith("f:")) // Filter out any arguments that don't start with "f:"
					.map(item -> item.substring(2)) // Remove the "f:" from the argument
					.findAny().orElse(null); // If there are no values in the list, default to null

			// Make sure the limit is between 1 and 20
			if (limit < 1 || limit > 20) {
				sender.sendMessage(Component.text("The limit must be between 1 and 20.").color(NamedTextColor.RED));
				return true;
			}

			// Get the list of kingdoms and calculate the max page
			List<String> kingdoms = new ArrayList<>(Kingdom.getKingdomIndex().values());
			if (filter != null) kingdoms.removeIf(item -> !item.toLowerCase().startsWith(filter.toLowerCase()));

			// Make sure the page is between 1 and the max page
			int maxPage = (int) Math.ceil(kingdoms.size() / (double) limit);
			if (page > maxPage) page = maxPage;

			// If there are no kingdoms, send an error message
			if (maxPage == 0) {
				if (filter == null) sender.sendMessage(Component.text("There are no kingdoms.").color(NamedTextColor.RED));
				else sender.sendMessage(Component.text("There are no kingdoms with that filter.").color(NamedTextColor.RED));
				return true;
			}

			// Send the list of kingdoms
			sender.sendMessage(Component.text("-- Page " + page + "/" + maxPage + " --").color(NamedTextColor.GOLD));
			for (int i = (page - 1) * limit; i < page * limit; i++) {
				if (i >= kingdoms.size()) break;
				Kingdom kingdom = Kingdom.fetchKingdom(kingdoms.get(i));
				if(kingdom == null) continue;
				if(kingdom.getMembers().isEmpty()) continue;

				sender.sendMessage(Component.text(kingdom.getName()).color(NamedTextColor.YELLOW)
						.append(Component.text(": ").color(NamedTextColor.WHITE))
						.append(Component.text(kingdom.getDescription() != null ? kingdom.getDescription() : "None set").color(NamedTextColor.WHITE))
						.append(Component.text(" (").color(NamedTextColor.GRAY))
						.append(Component.text(kingdom.getAllOnlineMembers().size() + "").color(NamedTextColor.GREEN))
						.append(Component.text("/").color(NamedTextColor.GRAY))
						.append(Component.text(kingdom.getAllMembers().size() + "").color(NamedTextColor.GOLD))
						.append(Component.text(")").color(NamedTextColor.GRAY)));
			}

			// Return true to indicate that the command was executed successfully
			return true;
		} catch (NumberFormatException e) {
			// If there was an error parsing the arguments, send an error message
			sender.sendMessage(Component.text("One or more arguments were invalid. Please check your syntax and try again.").color(NamedTextColor.RED));
			return true;
		}
	}

	@Nullable @Override
	protected List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
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
				.findAny().orElse(10); // If there are no values in the list, default to 10

		// Make sure the limit is between 1 and 20
		int maxPage = (int) Math.ceil(Kingdom.getKingdomIndex().size() / (double) limit);

		// Get the current argument, and return the options
		String current = args[args.length - 1];
		if(current.startsWith("f:")) return List.of("<name filter>");
		if(current.startsWith("l:")) return List.of("<limit (1-20)>");
		if(current.startsWith("p:")) return List.of("<page (1-" + maxPage + ")> [using limit: " + limit + "]");

		// Show the options, remove if they are in use already
		List<String> options = new ArrayList<>(List.of("f:", "l:", "p:"));
		options.removeIf(item -> Arrays.stream(args).anyMatch(arg -> arg.startsWith(item)));
		return options;
	}
}