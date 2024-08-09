package net.foxavis.kingdoms;

import com.google.common.reflect.ClassPath;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.foxavis.kingdoms.cmds.KingdomCommand;
import net.foxavis.kingdoms.listeners.ChatListener;
import net.foxavis.kingdoms.listeners.PlayerListener;
import net.foxavis.kingdoms.listeners.ProtectionListener;
import net.foxavis.kingdoms.objects.kingdoms.Kingdom;
import net.foxavis.kingdoms.util.CachedDataManager;
import net.foxavis.kingdoms.util.KingdomException;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class FoxavisKingdoms extends JavaPlugin {

	private static FoxavisKingdoms instance;
	private static Gson GSONInstance;

	public static FoxavisKingdoms getInstance() { return instance; }
	public static Gson getGSON() { return GSONInstance; }

	private Economy economy = null;

	@Override public void onEnable() {
		instance = this;
		GSONInstance = new GsonBuilder().setPrettyPrinting().create();

		try {
			register();
		} catch (KingdomException e) {
			getLogger().log(Level.SEVERE, e.getMessage(), e);
			getServer().getPluginManager().disablePlugin(this);
		}
	}

	@Override public void onDisable() {
		Kingdom.saveKingdomIndex(); // Save the kingdom index.
		CachedDataManager.shutdownAll(); // Shutdown all the cached data managers.
	}

	private void register() throws KingdomException {
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if(rsp == null) throw new KingdomException("Vault dependency not found.");
		economy = rsp.getProvider();

		registerEvents();
		registerCommands();
	}

	private void registerEvents() {
		PluginManager pm = getServer().getPluginManager();

		pm.registerEvents(new ChatListener(this), this);
		pm.registerEvents(new PlayerListener(this), this);
		pm.registerEvents(new ProtectionListener(this), this);
	}

	private void registerCommands() {
		Objects.requireNonNull(getCommand("kingdom")).setExecutor(new KingdomCommand(this));
	}

	public Set<Class<?>> findClasses(String packageName) throws IOException {
		URLClassLoader classLoader = new URLClassLoader(
				new URL[] { getFile().toURI().toURL() },
				this.getClass().getClassLoader()
		);
		return ClassPath.from(classLoader)
				.getAllClasses()
				.stream()
				.filter(clazz -> clazz.getPackageName().startsWith(packageName))
				.map(ClassPath.ClassInfo::load)
				.collect(Collectors.toSet());
	}

	public Economy getEconomy() { return economy; }
}