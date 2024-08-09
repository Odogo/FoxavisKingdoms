package net.foxavis.kingdoms.cmds.sub.e;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.enums.KingdomPerms;
import net.foxavis.kingdoms.enums.KingdomRelation;
import net.foxavis.kingdoms.objects.PendingRelation;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KAllyCommand extends KingdomSubcommand {

	public KAllyCommand() {
		super("ally", "Ally another kingdom", "ally <kingdom>", null);
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
			player.sendMessage(Component.text("You must specify a kingdom to ally with.").color(NamedTextColor.RED));
			return true;
		}

		Kingdom target = Kingdom.fetchKingdom(String.join(" ", args));
		if(target == null) {
			player.sendMessage(Component.text("The specified kingdom does not exist.").color(NamedTextColor.RED));
			return true;
		}

		if(kingdom.equals(target)) {
			player.sendMessage(Component.text("You cannot ally with your own kingdom.").color(NamedTextColor.RED));
			return true;
		}

		if(kingdom.isAlly(target)) {
			player.sendMessage(Component.text("Your kingdom is already allied with this kingdom.").color(NamedTextColor.RED));
			return true;
		}

		if(kingdom.isAtWar(target)) {
			player.sendMessage(Component.text("Your kingdom is currently at war with this kingdom. If you wish to end the war, request a truce instead.").color(NamedTextColor.RED));
			return true;
		}

		if(PendingRelation.hasPendingRelation(kingdom, target, KingdomRelation.ALLY)) {
			player.sendMessage(Component.text("Your kingdom has already requested an alliance with this kingdom.").color(NamedTextColor.RED));
			return true;
		}

		if(PendingRelation.hasPendingRelation(target, kingdom, KingdomRelation.ALLY)) {
			// Accept the alliance, as the target kingdom has already requested it
			PendingRelation pending = PendingRelation.getPendingRelation(target, kingdom, KingdomRelation.ALLY);
			PendingRelation.removePendingRelation(pending);

			kingdom.setRelation(target, KingdomRelation.ALLY);
			target.setRelation(kingdom, KingdomRelation.ALLY);

			player.sendMessage(Component.text("You've accepted the ally request with ").color(NamedTextColor.GREEN)
					.append(Component.text(target.getName()).color(NamedTextColor.YELLOW)));


			kingdom.sendKingdomMessage(
					Component.text(player.getName()).color(NamedTextColor.GOLD).append(
							Component.text(" has accepted the alliance request with ").color(NamedTextColor.GRAY)
									.append(Component.text(target.getName()).color(NamedTextColor.YELLOW)
					))
			);

			target.sendKingdomMessage(
					Component.text("Your kingdom is now allied with ").color(NamedTextColor.GREEN)
							.append(Component.text(kingdom.getName()).color(NamedTextColor.YELLOW))
			);
			return true;
		}

		PendingRelation pending = new PendingRelation(kingdom.getKingdomId(), target.getKingdomId(), KingdomRelation.ALLY);
		PendingRelation.addPendingRelation(pending);

		player.sendMessage(Component.text("You have requested an alliance with the kingdom of ").color(NamedTextColor.GREEN)
				.append(Component.text(target.getName()).color(NamedTextColor.YELLOW))
		);

		kingdom.sendKingdomMessage(
				Component.text(player.getName()).color(NamedTextColor.YELLOW)
						.append(Component.text(" has requested an alliance with ").color(NamedTextColor.GRAY))
						.append(Component.text(target.getName()).color(NamedTextColor.YELLOW))
		);
		target.sendKingdomMessage(
				Component.text(player.getName()).color(NamedTextColor.YELLOW)
						.append(Component.text(" has requested an alliance with your kingdom.").color(NamedTextColor.GRAY))
		);
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		return Kingdom.getKingdomIndex().values().stream()
				.filter(name -> name.startsWith(String.join(" ", args)))
				.map(name -> name.split(" ")[args.length - 1])
				.toList();
	}
}