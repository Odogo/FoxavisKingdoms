package net.foxavis.kingdoms.cmds.sub.admin;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.objects.KingdomPlayer;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KAPowerBoostCommand extends KingdomSubcommand {

	public KAPowerBoostCommand() {
		super("admin powerboost", "Boost a player/kingdom's power", "admin powerboost <player/kingdom> <amount>", "foxavis.kingdoms.admin.powerboost");
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		double amount = 1;
		if(args.length < 2) {
			sender.sendMessage(Component.text("You must specify a player/kingdom and an amount to boost their power by.").color(NamedTextColor.RED));
			return true;
		}

		try {
			amount = Double.parseDouble(args[args.length - 1]);
		} catch (NumberFormatException e) {
			sender.sendMessage(Component.text("The amount specified is not a valid number.").color(NamedTextColor.RED));
			return true;
		}

		if(args[0].startsWith("p:")) {
			String playerName = args[0].substring(2);
			Player player = plugin.getServer().getPlayer(playerName);
			if(player == null) {
				sender.sendMessage(Component.text("The player specified is not online.").color(NamedTextColor.RED));
				return true;
			}

			KingdomPlayer kPlayer = KingdomPlayer.fetchPlayer(player);
			if(kPlayer == null) kPlayer = new KingdomPlayer(player);

			kPlayer.setPowerBoost(amount);

			String powerPercent = String.format("%.2f", kPlayer.getPowerBoost() * 100);
			sender.sendMessage(Component.text("You have boosted " + playerName + "'s power to " + powerPercent + ".").color(NamedTextColor.GREEN));
		} else {
			Kingdom kingdom = Kingdom.fetchKingdom(String.join(" ", Arrays.copyOfRange(args, 0, args.length - 1)));
			if(kingdom == null) {
				sender.sendMessage(Component.text("The kingdom specified does not exist.").color(NamedTextColor.RED));
				return true;
			}

			kingdom.setPowerBoost(amount);

			String powerPercent = String.format("%.2f", kingdom.getPowerBoost() * 100);
			sender.sendMessage(Component.text("You have boosted " + kingdom.getName() + "'s power to " + powerPercent + ".").color(NamedTextColor.GREEN));
		}
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(args.length == 0) return null;
		if(args.length == 1) {
			List<String> completions = new ArrayList<>(plugin.getServer().getOnlinePlayers().stream()
					.map(OfflinePlayer::getName)
					.map(name -> "p:" + name)
					.filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
					.toList());
			completions.addAll(Kingdom.getKingdomIndex().values().stream()
					.filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
					.toList());
			return completions;
		}
		return List.of();
	}
}