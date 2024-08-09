package net.foxavis.kingdoms.cmds.sub.admin;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.objects.KingdomPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KAChatSpyCommand extends KingdomSubcommand {

	public KAChatSpyCommand() {
		super("admin chatspy", "Toggle chat spy for all kingdoms.", "admin chatspy", "foxavis.kingdoms.admin.chatspy");
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(!(sender instanceof Player player)) {
			sender.sendMessage(Component.text("You must be an in-game player to use this command.").color(NamedTextColor.RED));
			return true;
		}

		KingdomPlayer kPlayer = KingdomPlayer.fetchPlayer(player);
		if(kPlayer == null) kPlayer = new KingdomPlayer(player);

		if(kPlayer.isSocialSpying()) {
			kPlayer.setSocialSpying(false);
			player.sendMessage(Component.text("Social spy disabled.").color(NamedTextColor.GREEN));
		} else {
			kPlayer.setSocialSpying(true);
			player.sendMessage(Component.text("Social spy enabled.").color(NamedTextColor.GREEN));
		}
		return true;
 	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		return List.of();
	}
}