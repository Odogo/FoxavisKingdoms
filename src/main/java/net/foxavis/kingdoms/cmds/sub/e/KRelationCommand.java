package net.foxavis.kingdoms.cmds.sub.e;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.enums.KingdomPerms;
import net.foxavis.kingdoms.enums.KingdomRelation;
import net.foxavis.kingdoms.objects.PendingRelation;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class KRelationCommand extends KingdomSubcommand {

	public KRelationCommand() {
		super("relation", "Manage kingdom relations", "relation <list,add,remove,accept,deny>", null);
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

		if(!kingdom.hasPermission(player, KingdomPerms.CMD_RELATIONS)) {
			player.sendMessage(Component.text("You do not have permission to manage kingdom relations.").color(NamedTextColor.RED));
			return true;
		}

		if(args.length == 0) {
			player.sendMessage(Component.text("-- /k relation Help --").color(NamedTextColor.GOLD));
			player.sendMessage(Component.text("/k relation list").color(NamedTextColor.YELLOW).append(Component.text(" - List all active relations.").color(NamedTextColor.GRAY)));
			player.sendMessage(Component.text("/k relation wishes").color(NamedTextColor.YELLOW).append(Component.text(" - List all pending relation wishes.").color(NamedTextColor.GRAY)));
			player.sendMessage(Component.text("/k ally <kingdom>").color(NamedTextColor.YELLOW).append(Component.text(" - Request an alliance with a kingdom.").color(NamedTextColor.GRAY)));
			player.sendMessage(Component.text("/k truce <kingdom>").color(NamedTextColor.YELLOW).append(Component.text(" - Request a truce with a kingdom").color(NamedTextColor.GRAY)));
			player.sendMessage(Component.text("/k neutral <kingdom>").color(NamedTextColor.YELLOW).append(Component.text(" - Declare a kingdom as neutral").color(NamedTextColor.GRAY)));
			player.sendMessage(Component.text("/k enemy <kingdom>").color(NamedTextColor.YELLOW).append(Component.text(" - Declare a kingdom as an enemy of yours").color(NamedTextColor.GRAY)));
			player.sendMessage(Component.text("/k war <kingdom>").color(NamedTextColor.YELLOW).append(Component.text(" - Declare war on a kingdom").color(NamedTextColor.GRAY)));
			player.sendMessage(Component.text("-- /k relation Help --").color(NamedTextColor.GOLD));
			return true;
		}

		if(args[0].equalsIgnoreCase("list")) {
			Map<UUID, KingdomRelation> relations = kingdom.getRelations();
			if(relations.isEmpty()) {
				player.sendMessage(Component.text("You have no relations with any kingdoms.").color(NamedTextColor.RED));
				return true;
			}

			player.sendMessage(Component.text("-- " + kingdom.getName() + " Relations --").color(NamedTextColor.GOLD));
			for(KingdomRelation relation : relations.values()) {
				List<UUID> kingdoms = relations.entrySet().stream().filter(entry -> entry.getValue().equals(relation)).map(Map.Entry::getKey).toList();
				player.sendMessage(Component.text(relation.name()).color(NamedTextColor.YELLOW).append(Component.text(": ").color(NamedTextColor.WHITE))
						.append(Component.text(String.join(", ", kingdoms.stream().map(Kingdom::fetchKingdom).filter(Objects::nonNull).map(Kingdom::getName).toList())).color(NamedTextColor.YELLOW)));
			}
			player.sendMessage(Component.text("-- " + kingdom.getName() + " Relations --").color(NamedTextColor.GOLD));
		} else if(args[0].equalsIgnoreCase("wishes")) {
			List<PendingRelation> wishes = PendingRelation.getPendingRelations().stream().filter(relation -> relation.getRequestingKingdomId().equals(kingdom.getKingdomId())).toList();
			if(wishes.isEmpty()) {
				player.sendMessage(Component.text("You have no pending relation wishes.").color(NamedTextColor.RED));
				return true;
			}

			player.sendMessage(Component.text("-- " + kingdom.getName() + " Pending Wishes --").color(NamedTextColor.GOLD));
			for(PendingRelation wish : wishes) {
				player.sendMessage(Component.text(wish.getRelation().name()).color(NamedTextColor.YELLOW).append(Component.text(": ").color(NamedTextColor.WHITE))
						.append(Component.text(Objects.requireNonNull(wish.getTargetKingdom()).getName()).color(NamedTextColor.YELLOW)));
			}
			player.sendMessage(Component.text("-- " + kingdom.getName() + " Pending Wishes --").color(NamedTextColor.GOLD));
		} else {
			player.sendMessage(Component.text("Invalid subcommand.").color(NamedTextColor.RED));
		}

		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(args.length == 1) {
			return List.of("list", "wishes");
		}

		return List.of();
	}

}
