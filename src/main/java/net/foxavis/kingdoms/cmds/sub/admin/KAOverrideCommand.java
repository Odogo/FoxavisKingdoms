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

public class KAOverrideCommand extends KingdomSubcommand {

	public KAOverrideCommand() {
		super("admin override", "Bypass kingdom restrictions", "admin override", "foxavis.kingdoms.admin.override");
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(!(sender instanceof Player player)) {
			sender.sendMessage(Component.text("You must be an in-game player to use this command.").color(NamedTextColor.RED));
			return true;
		}

		KingdomPlayer kPlayer = KingdomPlayer.fetchPlayer(player);
		if(kPlayer == null) kPlayer = new KingdomPlayer(player);

		if(kPlayer.isOverriding()) {
			kPlayer.setOverriding(false);
			sender.sendMessage(Component.text("You are no longer overriding kingdom restrictions.").color(NamedTextColor.GREEN));
		} else {
			kPlayer.setOverriding(true);
			sender.sendMessage(Component.text("You are now overriding kingdom restrictions.").color(NamedTextColor.GREEN));
		}
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		return List.of();
	}
}