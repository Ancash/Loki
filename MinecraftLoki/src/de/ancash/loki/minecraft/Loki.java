package de.ancash.loki.minecraft;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class Loki extends JavaPlugin{
	
	private static Loki INSTANCE;
	
	private final File dir = new File("plugins/Loki/plugins");
	private final MinecraftLokiPluginManager lokiPluginManager = new MinecraftLokiPluginManager(this, dir);
	
	@Override
	public void onLoad() {
		INSTANCE = this;
		if(!dir.exists())
			dir.mkdirs();
		getLogger().info(String.format("Loading plugins in %s...", dir.getPath()));
		lokiPluginManager.load();
	}
	
	@Override
	public void onEnable() {
		getLogger().info("Enabling plugins");
		lokiPluginManager.getPlugins().forEach(l -> {
			getLogger().info("Enabling " + l.getDescription().getMain() + " v" + l.getDescription().getVersion());
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
		if(args.length == 0)
			return false;
		String cmd = args[0].toLowerCase();
		switch (cmd) {
		case "reload":
			if(sender.hasPermission("loki.reload")) {
				getLogger().info("Reloading all plugins...");
				reload();
				return true;
			}
			return false;
		case "unload":
			if(sender.hasPermission("loki.unload")) {
				getLogger().info("Unloading  all plugins...");
				unload();
				return true;
			}
			return false;
		case "load":
			if(sender.hasPermission("loki.load")) {
				getLogger().info("Loading all plugins...");
				load();
				return true;
			}
			return false;
		default:
			break;
		}
		return false;
	}
	
	public static Loki getInstance() {
		return INSTANCE;
	}
}