package net.foxavis.kingdoms.cmds.sub.admin;

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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class KAForceJoinCommand extends KingdomSubcommand {

	public KAForceJoinCommand() {
		super("admin forcejoin", "Force a player to join a kingdom", "admin forcejoin <kingdom> [player]", "foxavis.kingdoms.admin.forcejoin");
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(args.length < 1) {
			sender.sendMessage(Component.text("You must specify a kingdom and optionally a player to force join.").color(NamedTextColor.RED));
			return true;
		}

		Player player = null;
		Kingdom kingdom = Kingdom.fetchKingdom(String.join(" ", args));
		if(kingdom == null) {
			kingdom = Kingdom.fetchKingdom(String.join(" ", Arrays.copyOfRange(args, 0, args.length - 1)));
			if(kingdom == null) {
				sender.sendMessage(Component.text("The kingdom specified does not exist.").color(NamedTextColor.RED));
				return true;
			}

			player = plugin.getServer().getPlayer(args[args.length - 1]);
			if(player == null) {
				sender.sendMessage(Component.text("The player specified does not exist.").color(NamedTextColor.RED));
				return true;
			}
		}

		if(player == null && !(sender instanceof Player)) {
			sender.sendMessage(Component.text("You must be an in-game player to force join a player.").color(NamedTextColor.RED));
			return true;
		} else if(player == null) {
			player = (Player) sender;
		}

		kingdom.sendKingdomMessage(Component.text(player.getName() + " has been forced to join the kingdom.").color(NamedTextColor.YELLOW));

		kingdom.addMember(player);
		if(player.getUniqueId().equals(((Player) sender).getUniqueId())) {
			sender.sendMessage(Component.text("You have joined the kingdom " + kingdom.getName() + ".").color(NamedTextColor.GREEN));
		} else {
			sender.sendMessage(Component.text("You have forced " + player.getName() + " to join the kingdom " + kingdom.getName() + ".").color(NamedTextColor.GREEN));
			player.sendMessage(Component.text("You have been forced to join the kingdom " + kingdom.getName() + ".").color(NamedTextColor.GREEN));
		}

		KingdomPlayer kPlayer = KingdomPlayer.fetchPlayer(player);
		if(kPlayer == null) kPlayer = new KingdomPlayer(player);
		kPlayer.setKingdomId(kingdom.getKingdomId());

		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(args.length == 0) return null;
		if(args.length == 1) {
			return Kingdom.getKingdomIndex().values().stream()
					.filter(name -> name.startsWith(String.join(" ", args)))
					.map(name -> name.split(" ")[args.length - 1])
					.toList();
		}

		String kName = Stream.of(args).limit(args.length - 1).reduce((a, b) -> a + " " + b).orElse("");
		Kingdom kingdom = Kingdom.fetchKingdom(kName);
		if(kingdom == null) {
			return Kingdom.getKingdomIndex().values().stream()
					.filter(name -> name.startsWith(kName))
					.map(name -> name.split(" ")[args.length - 1])
					.toList();
		} else {
			return Bukkit.getOnlinePlayers().stream()
					.map(Player::getName).filter(name -> name.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).toList();
		}
	}
}