package net.foxavis.kingdoms.cmds.sub.c;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.enums.KingdomPerms;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.foxavis.kingdoms.objects.kingdoms.KingdomLocation;
import net.foxavis.kingdoms.util.Teleporter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class KWarpCommand extends KingdomSubcommand {

	public KWarpCommand() {
		super("warp", "Warp to a kingdom warp or make one", "warp <warpName|list|add <name>|remove <name>>", null);
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(!(sender instanceof Player player)) {
			sender.sendMessage(Component.text("You must be an in-game player to use this command.").color(NamedTextColor.RED));
			return true;
		}

		Kingdom kingdom = Kingdom.fetchKingdom(player);
		if(kingdom == null) {
			player.sendMessage(Component.text("You must be a member of a kingdom to use this command.").color(NamedTextColor.RED));
			return true;
		}

		if(args.length == 0) {
			player.sendMessage(Component.text("You must specify a warp name.").color(NamedTextColor.RED));
			player.sendMessage(Component.text("A warp GUI will be implemented at a later date!").color(NamedTextColor.GRAY));
			return true;
		}

		String warpName = String.join(" ", args);
		if(warpName.split(" ")[0].equalsIgnoreCase("list")) {
			if(!kingdom.hasPermission(player, KingdomPerms.CMD_WARPS)) {
				player.sendMessage(Component.text("You do not have permission to view warps.").color(NamedTextColor.RED));
				return true;
			}

			player.sendMessage(Component.text("Valid warp locations:").color(NamedTextColor.GRAY));
			for(String warp : kingdom.getWarps().keySet()) {
				player.sendMessage(Component.join(JoinConfiguration.separator(Component.text(", ").color(NamedTextColor.GRAY)),
						Component.text(warp).color(NamedTextColor.AQUA)
				));
			}
		} else if(warpName.split(" ")[0].equalsIgnoreCase("add")) {
			if(!kingdom.hasPermission(player, KingdomPerms.CMD_MANAGE_WARPS)) {
				player.sendMessage(Component.text("You do not have permission to manage warps.").color(NamedTextColor.RED));
				return true;
			}

			warpName = String.join(" ", args).replace("add ", "");
			if(kingdom.isValidWarp(warpName)) {
				player.sendMessage(Component.text("A warp with that name already exists.").color(NamedTextColor.RED));
				return true;
			}

			kingdom.setWarp(warpName, player.getLocation());
			player.sendMessage(Component.text("You have set the warp " + warpName + ".").color(NamedTextColor.GREEN));

			kingdom.sendKingdomMessage(Component.text(player.getName()).color(NamedTextColor.YELLOW)
					.append(Component.text(" has set the warp ").color(NamedTextColor.GRAY))
					.append(Component.text(warpName).color(NamedTextColor.AQUA))
					.append(Component.text(" at ").color(NamedTextColor.GRAY))
					.append(Component.text(player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ()).color(NamedTextColor.AQUA))
					.append(Component.text(" in world ").color(NamedTextColor.GRAY))
					.append(Component.text(player.getWorld().getName()).color(NamedTextColor.AQUA))
			);
		} else if(warpName.split(" ")[0].equalsIgnoreCase("remove")) {
			if(!kingdom.hasPermission(player, KingdomPerms.CMD_MANAGE_WARPS)) {
				player.sendMessage(Component.text("You do not have permission to manage warps.").color(NamedTextColor.RED));
				return true;
			}

			warpName = String.join(" ", args).replace("remove ", "");
			if(!kingdom.isValidWarp(warpName)) {
				player.sendMessage(Component.text("That warp does not exist.").color(NamedTextColor.RED));
				return true;
			}

			kingdom.removeWarp(warpName);
			player.sendMessage(Component.text("You have removed the warp " + warpName + ".").color(NamedTextColor.GREEN));

			kingdom.sendKingdomMessage(Component.text(player.getName()).color(NamedTextColor.YELLOW)
					.append(Component.text(" has removed the warp ").color(NamedTextColor.GRAY))
					.append(Component.text(warpName).color(NamedTextColor.AQUA))
			);
		} else {
			if(!kingdom.hasPermission(player, KingdomPerms.CMD_WARPS)) {
				player.sendMessage(Component.text("You do not have permission to teleport to a warp.").color(NamedTextColor.RED));
				return true;
			}

			if(!kingdom.isValidWarp(warpName)) {
				player.sendMessage(Component.text("That warp does not exist.").color(NamedTextColor.RED));
				return true;
			}

			KingdomLocation kLoc = kingdom.getWarp(warpName);
			if(kLoc == null) {
				player.sendMessage(Component.text("An error occurred while fetching the warp location.").color(NamedTextColor.RED));
				return true;
			}

			Location loc = kLoc.toBukkit();
			if(loc == null) {
				player.sendMessage(Component.text("This warp is located in an unloaded world and should be removed!").color(NamedTextColor.RED));
				return true;
			}

			Teleporter.teleportPlayer(plugin, player, loc);
		}
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(!(sender instanceof Player player)) return null;

		Kingdom kingdom = Kingdom.fetchKingdom(player);
		if(kingdom == null) return null;

		String warpName = String.join(" ", args);
		List<String> warps = kingdom.getWarps().keySet().stream().toList();

		if(args.length == 1) {
			return Stream.concat(warps.stream(), Stream.of("list", "add", "remove"))
					.filter(warp -> warp.startsWith(args[0]))
					.toList();
		} else if(args.length == 2) {
			if(args[0].equalsIgnoreCase("remove")) {
				return warps.stream()
						.filter(warp -> warp.startsWith(warpName.substring("remove ".length())))
						.toList();
			} else if(args[0].equalsIgnoreCase("add")) {
				return List.of("<name>");
			} else {
				return warps.stream()
						.filter(warp -> warp.startsWith(warpName))
						.map(warp -> warp.substring(warpName.length()))
						.toList();
			}
		} else {
			if(args[0].equalsIgnoreCase("add")) {
				return List.of("<name>");
			} else if(args[0].equalsIgnoreCase("remove")) {
				return warps.stream()
						.filter(warp -> warp.startsWith(warpName.substring("remove ".length())))
						.map(warp -> warp.substring(warpName.substring("remove ".length()).length()))
						.toList();
			} else {
				return warps.stream()
						.filter(warp -> warp.startsWith(warpName))
						.map(warp -> warp.substring(warpName.length()))
						.toList();
			}
		}
	}
}