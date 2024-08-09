package net.foxavis.kingdoms.cmds.sub.c;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.enums.KingdomPerms;
import net.foxavis.kingdoms.objects.KingdomPlayer;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class KKickCommand extends KingdomSubcommand {

	public KKickCommand() {
		super("kick", "Kick a player from your kingdom", "kick <player>", null);
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage(Component.text("You must be an in-game player to use this command.").color(NamedTextColor.RED));
			return true;
		}

		Kingdom kingdom = Kingdom.fetchKingdom(player);
		if (kingdom == null) {
			player.sendMessage(Component.text("You are not a member of a kingdom.").color(NamedTextColor.RED));
			return true;
		}

		if (!kingdom.hasPermission(player, KingdomPerms.CMD_KICK)) {
			player.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
			return true;
		}

		if (args.length == 0) {
			player.sendMessage(Component.text("You must specify a player to kick.").color(NamedTextColor.RED));
			return true;
		}

		OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[0]);
		if (!kingdom.isMember(target)) {
			player.sendMessage(Component.text("That player is not a member of your kingdom.").color(NamedTextColor.RED));
			return true;
		}

		kingdom.kickMember(target);
		player.sendMessage(Component.text("You have kicked " + target.getName() + " from your kingdom.").color(NamedTextColor.GREEN));
		kingdom.sendKingdomMessage(Component.text(player.getName() + " has kicked " + target.getName() + " from the kingdom.").color(NamedTextColor.YELLOW));

		if(target.getPlayer() != null) {
			target.getPlayer().sendMessage(Component.text("You have been kicked from ").color(NamedTextColor.RED).append(Component.text(kingdom.getName())).append(Component.text(".")).color(NamedTextColor.RED));
		}

		KingdomPlayer kPlayer = KingdomPlayer.fetchPlayer(player);
		if(kPlayer == null) kPlayer = new KingdomPlayer(player);
		kPlayer.setKingdomId(null);
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(!(sender instanceof Player player)) return List.of();
		Kingdom kingdom = Kingdom.fetchKingdom(player);
		if(kingdom == null) return List.of();

		return kingdom.getAllMembers().stream()
				.filter(member -> !member.getUniqueId().equals(player.getUniqueId()))
				.map(OfflinePlayer::getName)
				.filter(Objects::nonNull)
				.filter(name -> name.startsWith(args[0]))
				.toList();
	}
}
