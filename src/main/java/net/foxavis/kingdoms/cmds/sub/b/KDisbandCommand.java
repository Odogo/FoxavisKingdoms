package net.foxavis.kingdoms.cmds.sub.b;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.enums.KingdomPerms;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KDisbandCommand extends KingdomSubcommand {

	private final List<UUID> confirmation;

	public KDisbandCommand() {
		super("disband", "Disband your kingdom", "disband", null);

		confirmation = new ArrayList<>();
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

		if(!kingdom.hasPermission(player, KingdomPerms.CMD_DISBAND)) {
			player.sendMessage(Component.text("You do not have permission to disband your kingdom. If you wish to leave, use /k leave").color(NamedTextColor.RED));
			return true;
		}

		if(kingdom.isPersistent()) {
			player.sendMessage(Component.text("You cannot disband a persistent kingdom. If you wish to leave a persistent kingdom, use /k leave").color(NamedTextColor.RED));
			return true;
		}

		if(confirmation.contains(player.getUniqueId())) {
			confirmation.remove(player.getUniqueId());

			kingdom.sendKingdomMessage(Component.text(player.getName()).color(NamedTextColor.YELLOW).append(Component.text(" has disbanded the kingdom. It was nice to be your advisor..").color(NamedTextColor.RED)));
			player.sendMessage(Component.text("You have disbanded your kingdom.").color(NamedTextColor.RED));

			kingdom.disband();
			return true;
		} else {
			confirmation.add(player.getUniqueId());
			player.sendMessage(Component.text("Are you sure you want to disband your kingdom? This action is irreversible. If you are sure, run this command again.").color(NamedTextColor.RED));
		}
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		return List.of();
	}
}