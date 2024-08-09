package net.foxavis.kingdoms.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class Teleporter {

	public static void teleportPlayer(@NotNull Plugin plugin, @NotNull Player player, @NotNull Location location) {
		Location stayLoc = player.getLocation();

		new BukkitRunnable() {
			private int timer = 10;

			@Override public void run() {
				if(timer < -1) {
					player.sendActionBar(Component.text("Teleported!").color(NamedTextColor.GREEN));
					player.teleport(location);
					player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1, 0.5f);
					cancel();
					return;
				}

				Location curLoc = player.getLocation();
				if(stayLoc.getBlockX() != curLoc.getBlockX() || stayLoc .getBlockY() != curLoc.getBlockY() || stayLoc.getBlockZ() != curLoc.getBlockZ()) {
					player.sendActionBar(Component.text("Teleport cancelled, you failed to stay still!").color(NamedTextColor.RED));
					player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1, 0.5f);
					cancel();
					return;
				}

				if(timer % 2 == 0) {
					player.sendActionBar(Component.text("Teleporting in " + (timer / 2) + "...").color(NamedTextColor.GRAY));
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
				}
				timer--;
			}
		}.runTaskTimer(plugin, 0L, 10L);
	}

}
