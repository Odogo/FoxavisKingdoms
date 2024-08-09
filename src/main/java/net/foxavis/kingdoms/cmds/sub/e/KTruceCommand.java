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

public class KTruceCommand extends KingdomSubcommand {

	public KTruceCommand() {
		super("truce", "Request a truce with a kingdom", "truce <kingdom>", null);
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
			player.sendMessage(Component.text("You must specify a kingdom to request a truce with.").color(NamedTextColor.RED));
			return true;
		}

		Kingdom target = Kingdom.fetchKingdom(String.join(" ", args));
		if(target == null) {
			player.sendMessage(Component.text("The specified kingdom does not exist.").color(NamedTextColor.RED));
			return true;
		}

		if(kingdom.equals(target)) {
			player.sendMessage(Component.text("You cannot request a truce with your own kingdom.").color(NamedTextColor.RED));
			return true;
		}

		if(kingdom.isTruce(target)) {
			player.sendMessage(Component.text("Your kingdom is already in a truce with this kingdom.").color(NamedTextColor.RED));
			return true;
		}

		if(!kingdom.isAtWar(target)) {
			player.sendMessage(Component.text("Your kingdom is not at war with this kingdom.").color(NamedTextColor.RED));
			return true;
		}

		if(PendingRelation.hasPendingRelation(kingdom, target, KingdomRelation.TRUCE)) {
			player.sendMessage(Component.text("Your kingdom already has a pending relation with this kingdom.").color(NamedTextColor.RED));
			return true;
		}

		if(PendingRelation.hasPendingRelation(target, kingdom, KingdomRelation.TRUCE)) {
			// Accept the truce, as the target kingdom has already requested it
			PendingRelation pending = PendingRelation.getPendingRelation(target, kingdom, KingdomRelation.TRUCE);
			PendingRelation.removePendingRelation(pending);

			kingdom.setRelation(target, KingdomRelation.TRUCE);
			target.setRelation(kingdom, KingdomRelation.TRUCE);

			player.sendMessage(Component.text("You've accepted the truce request with ").color(NamedTextColor.GREEN)
					.append(Component.text(target.getName())).color(NamedTextColor.YELLOW));

			kingdom.sendKingdomMessage(Component.text(player.getName()).color(NamedTextColor.YELLOW)
					.append(Component.text(" has accepted the truce request with ").color(NamedTextColor.GREEN))
					.append(Component.text(target.getName()).color(NamedTextColor.AQUA)));

			target.sendKingdomMessage(Component.text(player.getName()).color(NamedTextColor.YELLOW)
					.append(Component.text(", a representative of \"").color(NamedTextColor.GRAY))
					.append(Component.text(kingdom.getName()).color(NamedTextColor.AQUA))
					.append(Component.text("\", has accepted the truce request with your kingdom. The war has ended.").color(NamedTextColor.GRAY)));
			return true;
		}

		PendingRelation pending = new PendingRelation(kingdom.getKingdomId(), target.getKingdomId(), KingdomRelation.TRUCE);
		PendingRelation.addPendingRelation(pending);

		player.sendMessage(Component.text("You've requested a truce with ").color(NamedTextColor.GREEN)
				.append(Component.text(target.getName())).color(NamedTextColor.YELLOW));

		kingdom.sendKingdomMessage(Component.text(player.getName()).color(NamedTextColor.YELLOW)
				.append(Component.text(" has requested a truce with ").color(NamedTextColor.GREEN))
				.append(Component.text(target.getName()).color(NamedTextColor.AQUA)));

		target.sendKingdomMessage(Component.text(player.getName()).color(NamedTextColor.YELLOW)
				.append(Component.text(", a representative of \"").color(NamedTextColor.GRAY))
				.append(Component.text(kingdom.getName()).color(NamedTextColor.AQUA))
				.append(Component.text("\", has requested a truce with your kingdom. They wish to cease the ongoing war.").color(NamedTextColor.GRAY)));
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		return Kingdom.getKingdomIndex().values().stream()
				.filter(name -> name.startsWith(String.join(" ", args)))
				.map(name -> name.split(" ")[args.length - 1])
				.toList();
	}
}