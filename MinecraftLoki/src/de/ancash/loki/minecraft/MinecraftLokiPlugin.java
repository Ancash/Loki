package de.ancash.loki.minecraft;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import de.ancash.loki.LokiPluginDescription;
import de.ancash.loki.plugin.AbstractLokiPlugin;
import de.ancash.loki.plugin.LokiPluginClassLoader;

public class MinecraftLokiPlugin extends AbstractLokiPlugin{

	private final Set<Listener> listeners = new HashSet<>();
	@SuppressWarnings("unchecked")
	private final LokiPluginClassLoader<MinecraftLokiPlugin> loader = (LokiPluginClassLoader<MinecraftLokiPlugin>) getClass().getClassLoader();
	
	public Logger getLogger() {
		return Bukkit.getLogger();
	}
	
	public final void registerListener(Listener l) {
		if(listeners.add(l))
			Bukkit.getPluginManager().registerEvents(l, Loki.getInstance());
	}
	
	public void onLoad() {
		
	}
	
	public void onEnable() {
		
	}
	
	public void onDisable() {
		
	}
	
	public LokiPluginDescription getDescription() {
		return loader.getLoader().getDescription();
	}
	
	public void info(String str) {
		getLogger().info(String.format("[%s] %s", getDescription().getName(), str));
	}
	
	public void warning(String str) {
		getLogger().warning(String.format("[%s] %s", getDescription().getName(), str));
	}
	
	public void severe(String str) {
		getLogger().severe(String.format("[%s] %s", getDescription().getName(), str));
	}
}