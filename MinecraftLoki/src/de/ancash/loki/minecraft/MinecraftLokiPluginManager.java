package de.ancash.loki.minecraft;

import java.io.File;

import de.ancash.loki.AbstractLokiPluginManager;
import de.ancash.loki.plugin.LokiPluginLoader;

public class MinecraftLokiPluginManager extends AbstractLokiPluginManager<MinecraftLokiPlugin>{

	private final Loki pl;
	
	public MinecraftLokiPluginManager(Loki pl, File dir) {
		super(pl.getLogger(), MinecraftLokiPlugin.class, dir);	
		this.pl = pl;
	}

	@Override
	public void onJarLoaded(LokiPluginLoader<MinecraftLokiPlugin> arg0) {
		
	}

	@Override
	public void onPluginLoaded(LokiPluginLoader<MinecraftLokiPlugin> arg0) {
		pl.getLogger().info("Loaded plugin " + arg0.getDescription().getName() + " version " + arg0.getDescription().getVersion() + " by " + arg0.getDescription().getAuthor());
		arg0.getPlugin().onLoad();
	}

	@Override
	public void onPluginUnload(LokiPluginLoader<MinecraftLokiPlugin> arg0) {
		pl.getLogger().info("Disabling " + arg0.getDescription().getName() + " version " + arg0.getDescription().getVersion() + " by " + arg0.getDescription().getAuthor());
		arg0.getPlugin().onDisable();
	}
}