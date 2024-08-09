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

import java.text.DecimalFormat;
import java.util.List;

public class KMoneyCommand extends KingdomSubcommand {

	public KMoneyCommand() {
		super("money", "Manage your kingdom's money", "money", null);
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

		DecimalFormat df = new DecimalFormat("$#,##0.00");
		if(args.length == 0 || args[0].equalsIgnoreCase("balance")) {
			if(!kingdom.hasPermission(player, KingdomPerms.CMD_BALANCE)) {
				player.sendMessage(Component.text("You do not have permission to view the kingdom's balance.").color(NamedTextColor.RED));
				return true;
			}

			player.sendMessage(
					Component.text("There is currently").color(NamedTextColor.YELLOW)
							.append(Component.text(" " + df.format(kingdom.getBalance()) + " ").color(NamedTextColor.GOLD))
							.append(Component.text("in your kingdom's coffers.").color(NamedTextColor.YELLOW))
			);
		} else if(args[0].equalsIgnoreCase("deposit")) {
			if(!kingdom.hasPermission(player, KingdomPerms.CMD_DEPOSIT)) {
				player.sendMessage(Component.text("You do not have permission to deposit to the kingdom's balance.").color(NamedTextColor.RED));
				return true;
			}

			if(args.length < 2) {
				player.sendMessage(Component.text("You must specify an amount to deposit.").color(NamedTextColor.RED));
				return true;
			}

			double amount;
			try {
				amount = Double.parseDouble(args[1]);
			} catch(NumberFormatException e) {
				player.sendMessage(Component.text("The specified amount is not a valid number.").color(NamedTextColor.RED));
				return true;
			}

			if(amount <= 0) {
				player.sendMessage(Component.text("You must deposit a positive amount.").color(NamedTextColor.RED));
				return true;
			}

			if(!plugin.getEconomy().has(player, amount)) {
				player.sendMessage(Component.text("You do not have enough money to deposit that amount.").color(NamedTextColor.RED));
				return true;
			}

			plugin.getEconomy().withdrawPlayer(player, amount);
			kingdom.depositMoney(amount);
			player.sendMessage(
					Component.text("You have deposited").color(NamedTextColor.YELLOW)
							.append(Component.text(" " + df.format(amount) + " ").color(NamedTextColor.GOLD))
							.append(Component.text("into your kingdom's coffers.").color(NamedTextColor.YELLOW))
			);
		} else if(args[0].equalsIgnoreCase("withdraw")) {
			if(!kingdom.hasPermission(player, KingdomPerms.CMD_WITHDRAW)) {
				player.sendMessage(Component.text("You do not have permission to withdraw from the kingdom's balance.").color(NamedTextColor.RED));
				return true;
			}

			if(args.length < 2) {
				player.sendMessage(Component.text("You must specify an amount to withdraw.").color(NamedTextColor.RED));
				return true;
			}

			double amount;
			try {
				amount = Double.parseDouble(args[1]);
			} catch(NumberFormatException e) {
				player.sendMessage(Component.text("The specified amount is not a valid number.").color(NamedTextColor.RED));
				return true;
			}

			if(amount <= 0) {
				player.sendMessage(Component.text("You must withdraw a positive amount.").color(NamedTextColor.RED));
				return true;
			}

			if(amount > kingdom.getBalance()) {
				player.sendMessage(Component.text("Your kingdom does not have enough money to withdraw that amount.").color(NamedTextColor.RED));
				return true;
			}

			plugin.getEconomy().depositPlayer(player, amount);
			kingdom.withdrawMoney(amount);
			player.sendMessage(
					Component.text("You have withdrawn").color(NamedTextColor.YELLOW)
							.append(Component.text(" " + df.format(amount) + " ").color(NamedTextColor.GOLD))
							.append(Component.text("from your kingdom's coffers.").color(NamedTextColor.YELLOW))
			);
		}
		return true;
	}

	@Override protected @Nullable List<String> tabComplete(FoxavisKingdoms plugin, CommandSender sender, String[] args) {
		if(args.length == 1) return List.of("balance", "deposit", "withdraw");
		else if(args.length == 2 && (args[0].equalsIgnoreCase("deposit") || args[0].equalsIgnoreCase("withdraw"))) return List.of("<amount>");
		return null;
	}
}