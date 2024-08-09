package net.foxavis.kingdoms.cmds.sub.a;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.objects.KingdomPlayer;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KPlayerCommand extends KingdomSubcommand {

	public KPlayerCommand() {
		super("player", "Show info about a player", "player <player>");
	}


	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		Player target = plugin.getServer().getPlayer(args[0]);
		if(target == null || !target.isOnline()) {
			sender.sendMessage(Component.text("That player is not online.").color(NamedTextColor.RED));
			return true;
		}

		KingdomPlayer kingdomPlayer = KingdomPlayer.fetchPlayer(target);
		if(kingdomPlayer == null) {
			sender.sendMessage(Component.text("Invalid data on target, suggests target has never joined before. Please contact an admin.").color(NamedTextColor.RED));
			return true;
		}

		Kingdom kingdom = Kingdom.fetchKingdom(target);
		boolean isInKingdom = kingdom != null;

		Component playerName = (isInKingdom ? kingdom.getTag().colorIfAbsent(NamedTextColor.GRAY).appendSpace(): Component.empty())
				.append(Component.text(target.getName()).color(NamedTextColor.YELLOW));

		sender.sendMessage(Component.text("-- Player: ").color(NamedTextColor.GOLD).append(playerName).append(Component.text(" --").color(NamedTextColor.GOLD)));
		sender.sendMessage(Component.text("Power: ").color(NamedTextColor.GOLD).append(Component.text(kingdomPlayer.getPower() + "/" + kingdomPlayer.getMaxPower()).color(NamedTextColor.WHITE)));
		sender.sendMessage(Component.text("Kingdom: ").color(NamedTextColor.GOLD).append(Component.text(isInKingdom ? kingdom.getName() : "None").color(NamedTextColor.WHITE)));
		if(isInKingdom) {
			sender.sendMessage(Component.text("Rank: ").color(NamedTextColor.GOLD).append(Component.text(kingdom.getRank(target).getName()).color(NamedTextColor.WHITE)));
		}
		if(kingdomPlayer.getPowerBoost() != 1) {
			sender.sendMessage(Component.text("Power Boosted: ").color(NamedTextColor.GOLD).append(Component.text(kingdomPlayer.getPowerBoost() + "x").color(NamedTextColor.WHITE)));
		}
		sender.sendMessage(Component.text("-- Player: ").color(NamedTextColor.GOLD).append(playerName).append(Component.text(" --").color(NamedTextColor.GOLD)));
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase())).toList();
	}
}