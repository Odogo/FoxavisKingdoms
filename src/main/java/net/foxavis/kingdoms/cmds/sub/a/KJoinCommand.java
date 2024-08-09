package net.foxavis.kingdoms.cmds.sub.a;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.enums.KingdomFlags;
import net.foxavis.kingdoms.objects.KingdomPlayer;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KJoinCommand extends KingdomSubcommand {

	public KJoinCommand() {
		super("join", "Join a kingdom", "join <kingdom>", null);
	}


	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(!(sender instanceof Player player)) {
			sender.sendMessage(Component.text("You must be an in-game player to use this command.").color(NamedTextColor.RED));
			return true;
		}

		if(args.length == 0) {
			sender.sendMessage(Component.text("You must specify a kingdom to join.").color(NamedTextColor.RED));
			return true;
		}

		Kingdom kingdom = Kingdom.fetchKingdom(String.join(" ", args));
		if(kingdom == null) {
			sender.sendMessage(Component.text("That kingdom does not exist.").color(NamedTextColor.RED));
			return true;
		}

		if(kingdom.isMember(player)) {
			sender.sendMessage(Component.text("You are already a member of that kingdom.").color(NamedTextColor.RED));
			return true;
		}

		if(Kingdom.fetchKingdom(player) != null) {
			sender.sendMessage(Component.text("You are already a member of a kingdom. Leave your current kingdom before joining another.").color(NamedTextColor.RED));
			return true;
		}

		if (!kingdom.getFlag(KingdomFlags.OPEN)) {
			if (!kingdom.isInvited(player) || kingdom.hasInviteExpired(player)) {
				if (kingdom.getInvites().containsKey(player.getUniqueId())) kingdom.revokeInvitation(player);

				sender.sendMessage(Component.text("You must be invited to join that kingdom.").color(NamedTextColor.RED));
				return true;
			}
		}

		if (kingdom.getInvites().containsKey(player.getUniqueId())) kingdom.revokeInvitation(player);

		kingdom.sendKingdomMessage(Component.text(player.getName() + " has joined the kingdom.").color(NamedTextColor.GREEN));
		kingdom.addMember(player);
		sender.sendMessage(Component.text("You have joined the kingdom " + kingdom.getName() + ".").color(NamedTextColor.GREEN));

		KingdomPlayer kPlayer = KingdomPlayer.fetchPlayer(player);
		if(kPlayer == null) kPlayer = new KingdomPlayer(player);
		kPlayer.setKingdomId(kingdom.getKingdomId());
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		return Kingdom.getKingdomIndex().values().stream()
				.filter(name -> name.startsWith(String.join(" ", args)))
				.map(name -> name.split(" ")[args.length - 1])
				.toList();
	}
}