package net.foxavis.kingdoms.cmds.sub.e;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.enums.KingdomPerms;
import net.foxavis.kingdoms.enums.KingdomRelation;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

public class KWarCommand extends KingdomSubcommand {

	public KWarCommand() {
		super("war", "Declare war on a kingdom", "war <kingdom>", null);
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
			player.sendMessage(Component.text("You must specify a kingdom to declare war on.").color(NamedTextColor.RED));
			return true;
		}

		Kingdom target = Kingdom.fetchKingdom(String.join(" ", args));
		if(target == null) {
			player.sendMessage(Component.text("The specified kingdom does not exist.").color(NamedTextColor.RED));
			return true;
		}

		if(kingdom.equals(target)) {
			player.sendMessage(Component.text("You cannot declare war on your own kingdom.").color(NamedTextColor.RED));
			return true;
		}

		if(kingdom.isAtWar(target)) {
			player.sendMessage(Component.text("Your kingdom is already at war with this kingdom.").color(NamedTextColor.RED));
			return true;
		}

		if(kingdom.isTruce(target)) {
			player.sendMessage(Component.text("You cannot declare war on a kingdom you are in a truce with.").color(NamedTextColor.RED));
			return true;
		}

		kingdom.setRelation(target, KingdomRelation.AT_WAR);
		target.setRelation(kingdom, KingdomRelation.AT_WAR);

		player.sendMessage(Component.text("You have declared war on " + target.getName() + ".").color(NamedTextColor.GREEN));

		kingdom.sendKingdomMessage(Component.text(player.getName()).color(NamedTextColor.GOLD)
				.append(Component.text(" has declared war on ").color(NamedTextColor.YELLOW))
				.append(Component.text(target.getName()).color(NamedTextColor.GOLD)));
		target.sendKingdomMessage(Component.text(kingdom.getName()).color(NamedTextColor.YELLOW)
				.append(Component.text(" has declared war on your kingdom.").color(NamedTextColor.GRAY)));

		Title kingdomTitle = Title.title(
				Component.text("WAR").color(NamedTextColor.RED).decorate(TextDecoration.BOLD),
				Component.text("At ").color(NamedTextColor.GRAY)
						.append(Component.text("WAR").color(NamedTextColor.RED).decorate(TextDecoration.BOLD))
						.append(Component.text(" with ").color(NamedTextColor.GRAY))
						.append(Component.text(target.getName()).color(NamedTextColor.GRAY)),
				Title.Times.times(Duration.ofMillis(750), Duration.ofSeconds(5), Duration.ofSeconds(2)));

		Title targetTitle = Title.title(
				Component.text("WAR").color(NamedTextColor.RED).decorate(TextDecoration.BOLD),
				Component.text("At ").color(NamedTextColor.GRAY)
						.append(Component.text("WAR").color(NamedTextColor.RED).decorate(TextDecoration.BOLD))
						.append(Component.text(" with ").color(NamedTextColor.GRAY))
						.append(Component.text(kingdom.getName()).color(NamedTextColor.GRAY)),
				Title.Times.times(Duration.ofMillis(750), Duration.ofSeconds(5), Duration.ofSeconds(2)));

		kingdom.getAllOnlineMembers().forEach(p -> {
			p.showTitle(kingdomTitle);
			p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 0);
		});

		target.getAllOnlineMembers().forEach(p -> {
			p.showTitle(targetTitle);
			p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 0);
		});

		plugin.getServer().broadcast(Component.text(kingdom.getName()).color(NamedTextColor.YELLOW)
				.append(Component.text(" has declared war on ").color(NamedTextColor.GRAY))
				.append(Component.text(target.getName()).color(NamedTextColor.YELLOW))
				.append(Component.text("!")).color(NamedTextColor.GRAY));
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		return Kingdom.getKingdomIndex().values().stream()
				.filter(name -> name.startsWith(String.join(" ", args)))
				.map(name -> name.split(" ")[args.length - 1])
				.toList();
	}
}