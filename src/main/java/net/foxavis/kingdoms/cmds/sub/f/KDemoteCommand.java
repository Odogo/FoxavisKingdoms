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

public class KDemoteCommand extends KingdomSubcommand {

	public KDemoteCommand() {
		super("demote", "Demote a member of your kingdom", "demote <player>", null);
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

		KingdomRank playerRank = kingdom.getRank(player), targetRank = kingdom.getRank(target);
		if(playerRank.isLessThan(targetRank)) {
			player.sendMessage(Component.text("You cannot demote a player of higher rank than you.").color(NamedTextColor.RED));
			return true;
		}

		KingdomRank prevRank = targetRank.previous();
		if(prevRank == null) {
			player.sendMessage(Component.text("You cannot demote this player any further. Use /k kick if you wish to remove them.").color(NamedTextColor.RED));
			return true;
		}

		kingdom.setRank(target, prevRank, false);
		player.sendMessage(Component.text("You have successfully demoted " + target.getName() + " to " + prevRank.getName() + ".").color(NamedTextColor.GREEN));
		target.sendMessage(Component.text("You have been demoted to " + prevRank.getName() + ".").color(NamedTextColor.RED));

		kingdom.sendKingdomMessage(Component.text(player.getName()).color(NamedTextColor.GOLD)
				.append(Component.text(" has demoted ").color(NamedTextColor.GRAY))
				.append(Component.text(target.getName()).color(NamedTextColor.RED))
				.append(Component.text(" to ").color(NamedTextColor.GRAY))
				.append(Component.text(prevRank.getName()).color(NamedTextColor.RED))
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