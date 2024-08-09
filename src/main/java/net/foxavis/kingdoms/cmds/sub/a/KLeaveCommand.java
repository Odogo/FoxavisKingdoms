package net.foxavis.kingdoms.cmds.sub.a;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.objects.KingdomPlayer;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KLeaveCommand extends KingdomSubcommand {

	public KLeaveCommand() {
		super("leave", "Leave your current kingdom", "leave", null);
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

		if(!kingdom.isPersistent() && kingdom.isLeader(player)) {
			player.sendMessage(Component.text("You cannot leave a kingdom you lead. Disband or declare a new leader before leaving.").color(NamedTextColor.RED));
			return true;
		}

		kingdom.removeMember(player);
		player.sendMessage(Component.text("You have left " + kingdom.getName() + ". You are your own being now.").color(NamedTextColor.GREEN));
		kingdom.sendKingdomMessage(Component.text(player.getName() + " has left the kingdom for better pasture..").color(NamedTextColor.YELLOW));

		KingdomPlayer kPlayer = KingdomPlayer.fetchPlayer(player);
		if(kPlayer == null) kPlayer = new KingdomPlayer(player);
		kPlayer.setKingdomId(null);
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		return null;
	}
}