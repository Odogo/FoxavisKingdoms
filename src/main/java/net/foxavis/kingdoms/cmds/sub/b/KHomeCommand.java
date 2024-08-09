package net.foxavis.kingdoms.cmds.sub.b;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.enums.KingdomPerms;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.foxavis.kingdoms.util.Teleporter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KHomeCommand extends KingdomSubcommand {

	public KHomeCommand() {
		super("home", "Teleport to your kingdom's home", "home [<set|unset>]", null);
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(!(sender instanceof Player player)) {
			sender.sendMessage(Component.text("You must be an in-game player to use this command.").color(NamedTextColor.RED));
			return true;
		}

		Kingdom kingdom = Kingdom.fetchKingdom(player);
		if(kingdom == null) {
			player.sendMessage(Component.text("You are not a member of a kingdom.").color(NamedTextColor.RED));
			return true;
		}

		if(args.length == 0) {
			// user wants to teleport to the home
			if(!kingdom.hasPermission(player, KingdomPerms.CMD_HOME)) {
				player.sendMessage(Component.text("You do not have permission to teleport to the kingdom's home.").color(NamedTextColor.RED));
				return true;
			}

			if(kingdom.getHome() == null) {
				player.sendMessage(Component.text("There is no defined kingdom home.").color(NamedTextColor.RED));
				return true;
			}

			Location teleportLoc = kingdom.getHome().toBukkit();
			if(teleportLoc == null) {
				player.sendMessage(Component.text("The kingdom's home is in an unloaded world.").color(NamedTextColor.RED));
				return true;
			}

			Teleporter.teleportPlayer(plugin, player, teleportLoc);
			return true;
		}

		if(!kingdom.hasPermission(player, KingdomPerms.CMD_SETHOME)) {
			player.sendMessage(Component.text("You do not have permission to set the kingdom's home.").color(NamedTextColor.RED));
			return true;
		}

		if(args[0].equalsIgnoreCase("set")) {
			kingdom.setHome(player.getLocation());
			player.sendMessage(Component.text("You have set the kingdom's home to your current location.").color(NamedTextColor.GREEN));

			kingdom.sendKingdomMessage(Component.text("The kingdom's home has been set by " + player.getName() + " at " + player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ() + ".").color(NamedTextColor.GREEN));
		} else if(args[0].equalsIgnoreCase("unset")) {
			kingdom.setHome(null);
			player.sendMessage(Component.text("You have unset the kingdom's home.").color(NamedTextColor.GREEN));

			kingdom.sendKingdomMessage(Component.text("The kingdom's home has been unset by " + player.getName() + ".").color(NamedTextColor.RED));
		} else {
			player.sendMessage(Component.text("Invalid argument. Use 'set' or 'unset'.").color(NamedTextColor.RED));
		}
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(args.length == 1) return List.of("set", "unset");
		return List.of();
	}
}