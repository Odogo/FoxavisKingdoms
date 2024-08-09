package net.foxavis.kingdoms.cmds.sub.admin;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.objects.KingdomPlayer;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class KAForceKickCommand extends KingdomSubcommand {

	public KAForceKickCommand() {
		super("admin forcekick", "Force a player to leave a kingdom", "admin forcekick <player>", "foxavis.kingdoms.admin.forcekick");
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(args.length < 1) {
			sender.sendMessage(Component.text("You must specify a player to force kick.").color(NamedTextColor.RED));
			return true;
		}

		OfflinePlayer player = plugin.getServer().getOfflinePlayer(args[0]);
		KingdomPlayer kPlayer = KingdomPlayer.fetchPlayer(player);
		if(kPlayer == null) {
			sender.sendMessage(Component.text("The player has never joined the server before!").color(NamedTextColor.RED));
			return true;
		}

		Kingdom kingdom = Kingdom.fetchKingdom(kPlayer.getKingdomId());
		if(kingdom == null) {
			sender.sendMessage(Component.text("The player is not a member of a kingdom.").color(NamedTextColor.RED));
			return true;
		}

		kingdom.kickMember(player);
		sender.sendMessage(Component.text("You have forced " + player.getName() + " to leave the kingdom.").color(NamedTextColor.GREEN));

		if(player.getPlayer() != null) {
			player.getPlayer().sendMessage(Component.text("You have been forced to leave ").color(NamedTextColor.RED).append(Component.text(kingdom.getName())).append(Component.text(".")).color(NamedTextColor.RED));
		}

		kingdom.sendKingdomMessage(Component.text(player.getName() + " has been forced to leave the kingdom.").color(NamedTextColor.YELLOW));
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(args.length == 0) return null;
		return Bukkit.getOnlinePlayers().stream()
				.map(Player::getName)
				.filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
				.toList();
	}
}