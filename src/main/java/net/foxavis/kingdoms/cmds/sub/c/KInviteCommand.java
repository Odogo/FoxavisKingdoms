package net.foxavis.kingdoms.cmds.sub.c;

import net.foxavis.kingdoms.FoxavisKingdoms;
import net.foxavis.kingdoms.cmds.obj.KingdomSubcommand;
import net.foxavis.kingdoms.enums.KingdomPerms;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class KInviteCommand extends KingdomSubcommand {

	// /k invite - shows help
	// /k invite list - shows all invitations
	// /k invite add <player> - invites a player to the kingdom
	// /k invite remove <player> - removes an invitation

	public KInviteCommand() {
		super("invite", "Invite a player to your kingdom.", "invite", null);
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

		if(args.length == 0) { sendHelp(player); return true; }

		if(!kingdom.hasPermission(player, KingdomPerms.CMD_INVITE)) {
			player.sendMessage(Component.text("You do not have permission to manage invitations.").color(NamedTextColor.RED));
			return true;
		}

		String subcommand = args[0].toLowerCase();
		switch(subcommand) {
			case "list": listInvitations(plugin, player, kingdom, Arrays.copyOfRange(args, 1, args.length)); break;
			case "add": addInvitation(plugin, player, kingdom, Arrays.copyOfRange(args, 1, args.length)); break;
			case "remove": removeInvitation(plugin, player, kingdom, Arrays.copyOfRange(args, 1, args.length)); break;
			default: sendHelp(player); break;
		}
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		return switch(args.length) {
			case 1 -> List.of("list", "add", "remove");
			case 2 -> {
				if(args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
					yield Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).toList();
				}
				yield List.of();
			}
			default -> List.of();
		};
	}

	private void sendHelp(Player player) {
		player.sendMessage(Component.text("-- /k invite Help --").color(NamedTextColor.GOLD));
		player.sendMessage(Component.text("/k invite list").color(NamedTextColor.YELLOW).append(Component.text(" - List all invitations").color(NamedTextColor.GRAY)));
		player.sendMessage(Component.text("/k invite add <player>").color(NamedTextColor.YELLOW).append(Component.text(" - Invite a player to the kingdom").color(NamedTextColor.GRAY)));
		player.sendMessage(Component.text("/k invite remove <player>").color(NamedTextColor.YELLOW).append(Component.text(" - Remove an invitation").color(NamedTextColor.GRAY)));
		player.sendMessage(Component.text("-- /k invite Help --").color(NamedTextColor.GOLD));
	}

	private void listInvitations(FoxavisKingdoms plugin, Player player, Kingdom kingdom, String[] args) {
		Map<UUID, Long> invites = kingdom.getInvites();
		if(invites.isEmpty()) {
			player.sendMessage(Component.text("You have no pending invitations.").color(NamedTextColor.RED));
			return;
		}

		player.sendMessage(Component.text("-- Kingdom Invitations --").color(NamedTextColor.GOLD));
		invites.forEach((uuid, time) -> {
			OfflinePlayer invited = plugin.getServer().getOfflinePlayer(uuid);
			String name = (invited.getName() == null ? "Unknown" : invited.getName());
			String timeString = formatTime(time);

			player.sendMessage(Component.text(name).color(NamedTextColor.YELLOW).append(Component.text(" was invited ").color(NamedTextColor.GRAY)).append(Component.text(timeString).color(NamedTextColor.YELLOW)).append(Component.text(" ago").color(NamedTextColor.GRAY)));
		});
		player.sendMessage(Component.text("-- Kingdom Invitations --").color(NamedTextColor.GOLD));
	}

	private void addInvitation(FoxavisKingdoms plugin, Player player, Kingdom kingdom, String[] args) {
		Player target = plugin.getServer().getPlayer(args[0]);
		if(target == null) {
			player.sendMessage(Component.text("That player is not online.").color(NamedTextColor.RED));
			return;
		}

		if(kingdom.isMember(target)) {
			player.sendMessage(Component.text("That player is already a member of your kingdom.").color(NamedTextColor.RED));
			return;
		}

		if(kingdom.isInvited(target)) {
			player.sendMessage(Component.text("That player has already been invited to your kingdom.").color(NamedTextColor.RED));
			return;
		}

		if(Kingdom.fetchKingdom(target) != null) {
			player.sendMessage(Component.text("That player is already a member of a kingdom.").color(NamedTextColor.RED));
			return;
		}

		kingdom.invitePlayer(target);
		player.sendMessage(Component.text("You have invited " + target.getName() + " to your kingdom.").color(NamedTextColor.GREEN));
		target.sendMessage(Component.text("You have been invited to join " + kingdom.getName() + ". Type /k join <kingdom> to join.").color(NamedTextColor.GREEN));

		kingdom.sendKingdomMessage(Component.text(player.getName() + " has invited " + target.getName() + " to the kingdom.").color(NamedTextColor.YELLOW));
	}

	private void removeInvitation(FoxavisKingdoms plugin, Player player, Kingdom kingdom, String[] args) {
		Player target = plugin.getServer().getPlayer(args[0]);
		if(target == null) {
			player.sendMessage(Component.text("That player is not online.").color(NamedTextColor.RED));
			return;
		}

		if(!kingdom.isInvited(target)) {
			player.sendMessage(Component.text("That player has not been invited to your kingdom.").color(NamedTextColor.RED));
			return;
		}

		kingdom.revokeInvitation(target);
		player.sendMessage(Component.text("You have removed the invitation for " + target.getName() + ".").color(NamedTextColor.GREEN));
		target.sendMessage(Component.text("Your invitation to join " + kingdom.getName() + " has been revoked.").color(NamedTextColor.RED));

		kingdom.sendKingdomMessage(Component.text(player.getName() + " has revoked the invitation for " + target.getName() + ".").color(NamedTextColor.YELLOW));
	}

	private String formatTime(long time) {
		long current = System.currentTimeMillis();
		long diff = time - current;

		long days = TimeUnit.MILLISECONDS.toDays(diff);
		long hours = TimeUnit.MILLISECONDS.toHours(diff) - TimeUnit.DAYS.toHours(days);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) - TimeUnit.HOURS.toMinutes(hours) - TimeUnit.DAYS.toMinutes(days);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(diff) - TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.DAYS.toSeconds(days);

		StringBuilder builder = new StringBuilder();
		if(days > 0) builder.append(days).append("d ");
		if(hours > 0) builder.append(hours).append("h ");
		if(minutes > 0) builder.append(minutes).append("m ");
		builder.append(seconds).append("s");

		return builder.toString();
	}
}