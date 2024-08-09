package net.foxavis.kingdoms.cmds.sub.f;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.enums.KingdomPerms;
import net.foxavis.kingdoms.enums.KingdomRank;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KPromoteCommand extends KingdomSubcommand {

	public KPromoteCommand() {
		super("promote", "Promote a member of your kingdom", "promote <player>", null);
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage(Component.text("You must be an in-game player to use this command.").color(NamedTextColor.RED));
			return true;
		}

		Kingdom kingdom = Kingdom.fetchKingdom(player);
		if (kingdom == null) {
			player.sendMessage(Component.text("You must be a member of a kingdom to use this command.").color(NamedTextColor.RED));
			return true;
		}

		if (!kingdom.hasPermission(player, KingdomPerms.MANAGE_MEMBERS)) {
			player.sendMessage(Component.text("You do not have permission to manage kingdom members.").color(NamedTextColor.RED));
			return true;
		}

		if (args.length == 0) {
			player.sendMessage(Component.text("You must specify a player to promote.").color(NamedTextColor.RED));
			return true;
		}

		Player target = plugin.getServer().getPlayer(args[0]);
		if (target == null) {
			player.sendMessage(Component.text("The specified player is not online.").color(NamedTextColor.RED));
			return true;
		}

		if (!kingdom.isMember(target)) {
			player.sendMessage(Component.text("The specified player is not a member of your kingdom.").color(NamedTextColor.RED));
			return true;
		}

		if (player.getUniqueId().equals(target.getUniqueId())) {
			player.sendMessage(Component.text("You cannot promote yourself.").color(NamedTextColor.RED));
			return true;
		}

		if (kingdom.getLeader().equals(target.getUniqueId())) {
			player.sendMessage(Component.text("The leader cannot be promoted, they are the highest rank in the kingdom.").color(NamedTextColor.RED));
			return true;
		}

		KingdomRank playerRank = kingdom.getRank(player), targetRank = kingdom.getRank(target);
		KingdomRank nextRank = targetRank.next();

		if(nextRank == null) {
			player.sendMessage(Component.text("You cannot promote this player any further.").color(NamedTextColor.RED));
			return true;
		}

		if(nextRank.isAtMost(playerRank)) {
			player.sendMessage(Component.text("You cannot promote someone to a rank higher than your own.").color(NamedTextColor.RED));
			return true;
		}

		kingdom.setRank(target, nextRank, false);
		target.sendMessage(Component.text("You have been promoted to " + nextRank.getName() + ".").color(NamedTextColor.GREEN));
		player.sendMessage(Component.text("You have promoted " + target.getName() + " to " + nextRank.getName() + ".").color(NamedTextColor.GREEN));

		kingdom.sendKingdomMessage(Component.text(player.getName()).color(NamedTextColor.GOLD)
				.append(Component.text(" has promoted ").color(NamedTextColor.GRAY))
				.append(Component.text(target.getName()).color(NamedTextColor.GOLD))
				.append(Component.text(" to " + nextRank.getName() + ".").color(NamedTextColor.GRAY))
		);
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(!(sender instanceof Player player)) return null;
		Kingdom kingdom = Kingdom.fetchKingdom(player);
		if(kingdom == null) return null;

		if(!kingdom.hasPermission(player, KingdomPerms.MANAGE_MEMBERS)) return null;

		return kingdom.getAllOnlineMembers().stream()
				.filter(member -> kingdom.getRank(member).isLessThan(kingdom.getRank(player)))
				.map(Player::getName)
				.toList();
	}
}