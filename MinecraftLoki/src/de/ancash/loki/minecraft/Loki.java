package de.ancash.loki.minecraft;

import java.io.File;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import de.ancash.loki.LokiPluginDescription;
import de.ancash.loki.exception.InvalidPluginException;

public class Loki extends JavaPlugin {

	private static Loki INSTANCE;

	private final File dir = new File("plugins/Loki/plugins");
	private final MinecraftLokiPluginManager lokiPluginManager = new MinecraftLokiPluginManager(this, dir);

	@Override
	public void onLoad() {
		INSTANCE = this;
		if (!dir.exists())
			dir.mkdirs();
		getLogger().info(String.format("Loading plugins in %s...", dir.getPath()));
		lokiPluginManager.load();
	}

	@Override
	public void onEnable() {
		getLogger().info("Enabling plugins");
		lokiPluginManager.getPlugins().forEach(l -> {
			getLogger().info("Enabling " + l.getDescription().getName() + " version " + l.getDescription().getVersion() + " by " + l.getDescription().getAuthor());
			l.onEnable();
		});
	}

	@Override
	public void onDisable() {
		unload();
	}

	private void reload() {
		unload();
		load();
	}

	private void load() {
		getLogger().info(String.format("Loading plugins in %s...", dir.getPath()));
		lokiPluginManager.load();
		lokiPluginManager.getPlugins().forEach(l -> {
			getLogger().info("Enabling " + l.getDescription().getMain() + " v" + l.getDescription().getVersion());
			l.onEnable();
		});
	}

	private void unload() {
		getLogger().info("Disabling plugins and unregistering all listeners");
		HandlerList.unregisterAll(INSTANCE);
		lokiPluginManager.unload();
		getLogger().info("Plugins disabled!");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0)
			return false;
		String cmd = args[0].toLowerCase();
		switch (cmd) {
		case "reload":
			if (!sender.hasPermission("loki.reload"))
				return false;
			if (args.length == 1) {
				getLogger().info("Reloading all plugins...");
				reload();
			} else
				for (int i = 1; i < args.length; i++) {
					String pluginName = args[i];
					if (!lokiPluginManager.isPluginLoaded(pluginName))
						sender.sendMessage(String.format("§cCould not find plugin '%s'", pluginName));
					else
						try {
							unregisterAll();
							lokiPluginManager.reloadPlugin(pluginName);
						} catch (InvalidPluginException e) {
							sender.sendMessage(
									String.format("§cCould not reload '%s': %s", pluginName, e.getMessage()));
							e.printStackTrace();
						}
				}
			return true;
		case "unload":
			if (!sender.hasPermission("loki.unload"))
				return false;

			if (args.length == 1) {
				getLogger().info("Unloading  all plugins...");
				unload();

			} else
				for (int i = 1; i < args.length; i++) {
					String pluginName = args[i];
					if (!lokiPluginManager.isPluginLoaded(pluginName))
						sender.sendMessage(String.format("§cCould not find plugin '%s'", pluginName));
					else
						try {
							lokiPluginManager.unloadPlugin(pluginName);
							unregisterAll();
						} catch (InvalidPluginException e) {
							getLogger().log(Level.SEVERE, String.format("Error while disablin %s", pluginName), e);
						}
				}
			return true;
		case "load":
			if (!sender.hasPermission("loki.load"))
				return false;
			if (args.length == 1) {
				getLogger().info("Loading all plugins...");
				load();
			} else
				for (int i = 1; i < args.length; i++)
					try {
						lokiPluginManager.loadPlugin(args[i]);
						((MinecraftLokiPlugin) lokiPluginManager.getPlugin(args[i])).onEnable();
					} catch (InvalidPluginException e) {
						sender.sendMessage(String.format("§cCould not load plugin '%s': %s", args[i], e.getMessage()));
						e.printStackTrace();
					}
			return true;
		case "plugins":
			if (!sender.hasPermission("loki.plugins"))
				return false;
			sender.sendMessage(String.join(", ", lokiPluginManager.getPlugins().stream().map(MinecraftLokiPlugin::getDescription).map(LokiPluginDescription::getName).collect(Collectors.toList())));
			return true;
		default:
			break;
		}
		return false;
	}

	private void unregisterAll() {
		HandlerList.unregisterAll(this);
		Bukkit.getScheduler().cancelTasks(this);
		Bukkit.getServicesManager().unregisterAll(this);
		Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(this);
		Bukkit.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
		de.ancash.libs.org.bukkit.event.HandlerList.unregisterAll(this);
	}
	
	public static Loki getInstance() {
		return INSTANCE;
	}
}