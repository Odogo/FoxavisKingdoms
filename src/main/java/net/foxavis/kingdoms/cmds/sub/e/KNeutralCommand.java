package net.foxavis.kingdoms.cmds.sub.e;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.enums.KingdomPerms;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KNeutralCommand extends KingdomSubcommand {

	public KNeutralCommand() {
		super("neutral", "Declare a kingdom as neutral", "neutral <kingdom>", null);
	}

	@Override protected boolean execute(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(!(sender instanceof Player player)) {
			sender.sendMessage(Component.text("You must be an in-game player to use this command.").color(NamedTextColor.RED));
			return true;
		}

		Kingdom kingdom = Kingdom.fetchKingdom(player);
		if(kingdom == null) {
			player.sendMessage(Component.text("You must be a member of a kingdom to use this command.").color(NamedTextColor.RED));
			return true;
		}

		if(!kingdom.hasPermission(player, KingdomPerms.CMD_RELATIONS)) {
			player.sendMessage(Component.text("You do not have permission to manage kingdom relations.").color(NamedTextColor.RED));
			return true;
		}

		if(args.length == 0) {
			player.sendMessage(Component.text("You must specify a kingdom to declare neutrality with.").color(NamedTextColor.RED));
			return true;
		}

		Kingdom target = Kingdom.fetchKingdom(String.join(" ", args));
		if(target == null) {
			player.sendMessage(Component.text("The specified kingdom does not exist.").color(NamedTextColor.RED));
			return true;
		}

		if(kingdom.equals(target)) {
			player.sendMessage(Component.text("You cannot declare neutrality with yourself.").color(NamedTextColor.RED));
			return true;
		}

		if(kingdom.isAtWar(target)) {
			player.sendMessage(Component.text("You cannot declare neutrality with a kingdom you are at war with.").color(NamedTextColor.RED));
			return true;
		}

		if(kingdom.isNeutral(target)) {
			player.sendMessage(Component.text("You are already neutral with that kingdom.").color(NamedTextColor.RED));
			return true;
		}

		if(kingdom.isAlly(target) || kingdom.isTruce(target)) {
			target.setRelation(kingdom, null);

			target.sendKingdomMessage(Component.text(kingdom.getName()).color(NamedTextColor.YELLOW)
					.append(Component.text(" has declared neutrality with your kingdom. You are no longer " + (kingdom.isAlly(target) ? "allied" : "truced") + " with them.").color(NamedTextColor.GRAY))
			);
		} else {
			target.sendKingdomMessage(Component.text(kingdom.getName()).color(NamedTextColor.YELLOW)
					.append(Component.text(", who were previously declare you an ")
							.append(Component.text("enemy").color(NamedTextColor.RED))
							.append(Component.text(", has declared neutrality with your kingdom.").color(NamedTextColor.GRAY))
					));
		}

		kingdom.setRelation(target, null);

		player.sendMessage(Component.text("You have declared neutrality with " + target.getName() + ".").color(NamedTextColor.GREEN));
		kingdom.sendKingdomMessage(Component.text(player.getName()).color(NamedTextColor.YELLOW)
				.append(Component.text(" has declared neutrality with " + target.getName() + ".").color(NamedTextColor.GRAY)
		));

		target.sendKingdomMessage(Component.text(kingdom.getName()).color(NamedTextColor.YELLOW)
				.append(Component.text(" has declared neutrality with your kingdom.").color(NamedTextColor.GRAY)
		));
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		return Kingdom.getKingdomIndex().values().stream()
				.filter(name -> name.startsWith(String.join(" ", args)))
				.map(name -> name.split(" ")[args.length - 1])
				.toList();
	}
}