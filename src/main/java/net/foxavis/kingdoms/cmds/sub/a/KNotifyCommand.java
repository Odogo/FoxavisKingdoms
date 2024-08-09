package net.foxavis.kingdoms.cmds.sub.a;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.objects.KingdomPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class KNotifyCommand extends KingdomSubcommand {

	public KNotifyCommand() {
		super("notify", "Toggle territory warnings", "notify [on/off]", null);
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(!(sender instanceof Player player)) {
			sender.sendMessage(Component.text("You must be an in-game player to use this command.").color(NamedTextColor.RED));
			return true;
		}

		KingdomPlayer kPlayer = KingdomPlayer.fetchPlayer(player);
		if(kPlayer == null) kPlayer = new KingdomPlayer(player);

		if(args.length == 1) {
			if(args[0].equalsIgnoreCase("on")) {
				kPlayer.setTerritoryInfoNotifs(true);
				player.sendMessage(Component.text("Territory warnings are now enabled.").color(NamedTextColor.GREEN));
			} else if(args[0].equalsIgnoreCase("off")) {
				kPlayer.setTerritoryInfoNotifs(false);
				player.sendMessage(Component.text("Territory warnings are now disabled.").color(NamedTextColor.GREEN));
			}
			return true;
		}

		kPlayer.setTerritoryInfoNotifs(!kPlayer.wantsTerritoryInfoNotifs());
		player.sendMessage(Component.text("Territory warnings are now " + (kPlayer.wantsTerritoryInfoNotifs() ? "enabled" : "disabled") + ".").color(NamedTextColor.GREEN));
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(args.length != 1) return null;
		return Stream.of("on", "off").filter(s -> s.startsWith(args[0])).toList();
	}
}