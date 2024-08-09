package net.foxavis.kingdoms.cmds.sub.admin;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.enums.KingdomRank;
import net.foxavis.kingdoms.objects.KingdomPlayer;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class KAForceRankCommand extends KingdomSubcommand {

	public KAForceRankCommand() {
		super("admin forcerank", "Force a player to have a rank in a kingdom", "admin forcerank <player> <rank>", "foxavis.kingdoms.admin.forcerank");
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(args.length != 2) {
			sender.sendMessage(Component.text("You must specify a player and a rank to force rank.").color(NamedTextColor.RED));
			return true;
		}

		OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[0]);
		KingdomPlayer kPlayer = KingdomPlayer.fetchPlayer(target);
		if(kPlayer == null) {
			sender.sendMessage(Component.text("The player has never joined the server before!").color(NamedTextColor.RED));
			return true;
		}

		if(kPlayer.getKingdomId() == null) {
			sender.sendMessage(Component.text("The player is not a member of a kingdom.").color(NamedTextColor.RED));
			return true;
		}

		Kingdom kingdom = Kingdom.fetchKingdom(kPlayer.getKingdomId());
		if(kingdom == null) {
			sender.sendMessage(Component.text("The player is not a member of a kingdom.").color(NamedTextColor.RED));
			kPlayer.setKingdomId(null);
			return true;
		}

		try {
			KingdomRank rank = KingdomRank.valueOf(args[1].toUpperCase());
			kingdom.setRank(target, rank, true);
			sender.sendMessage(Component.text("You have forced " + target.getName() + " to be rank " + rank.getName() + " in the kingdom.").color(NamedTextColor.GREEN));
			if(target.getPlayer() != null) {
				target.getPlayer().sendMessage(Component.text("You have been forced to be rank " + rank.getName() + " in the kingdom.").color(NamedTextColor.GREEN));
			}
			kingdom.sendKingdomMessage(Component.text(target.getName() + " has been forced to be rank " + rank.getName() + " in the kingdom.").color(NamedTextColor.YELLOW));
		} catch(IllegalArgumentException e) {
			sender.sendMessage(Component.text("Invalid rank specified.").color(NamedTextColor.RED));
		}
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(args.length == 0) return null;
		if(args.length == 1) {
			return plugin.getServer().getOnlinePlayers().stream()
					.map(OfflinePlayer::getName)
					.filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
					.toList();
		}
		return Stream.of(KingdomRank.values())
				.map(KingdomRank::name)
				.filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
				.toList();
	}
}