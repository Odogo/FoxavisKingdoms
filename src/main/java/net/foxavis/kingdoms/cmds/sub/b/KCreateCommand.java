package net.foxavis.kingdoms.cmds.sub.b;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.foxavis.kingdoms.util.KingdomException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.logging.Level;

public class KCreateCommand extends KingdomSubcommand {

	public KCreateCommand() {
		super("create", "Create a kingdom", "create <name>", null);
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(!(sender instanceof Player player)) {
			sender.sendMessage(Component.text("You must be an in-game player to use this command.").color(NamedTextColor.RED));
			return true;
		}

		if(args.length == 0) {
			player.sendMessage(Component.text("You must specify a name for your kingdom.").color(NamedTextColor.RED));
			return true;
		}

		if(Kingdom.fetchKingdom(player) != null) {
			player.sendMessage(Component.text("You are already a member of a kingdom. Leave your current kingdom before creating a new one.").color(NamedTextColor.RED));
			return true;
		}

		String name = String.join(" ", args);
		if(Kingdom.fetchKingdom(name) != null) {
			player.sendMessage(Component.text("A kingdom with that name already exists.").color(NamedTextColor.RED));
			return true;
		}

		try {
			Kingdom kingdom = new Kingdom(player, name);
			player.sendMessage(Component.text("You have created the kingdom " + kingdom.getName() + ".").color(NamedTextColor.GREEN));

			plugin.getServer().broadcast(Component.text(player.getName() + " has created the kingdom " + kingdom.getName() + ".").color(NamedTextColor.YELLOW));
		} catch (KingdomException e) {
			player.sendMessage(Component.text(e.getMessage()).color(NamedTextColor.RED));
			plugin.getLogger().log(Level.WARNING, "An error occurred while creating a kingdom.", e);
		}
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		return List.of("<name>");
	}

}
