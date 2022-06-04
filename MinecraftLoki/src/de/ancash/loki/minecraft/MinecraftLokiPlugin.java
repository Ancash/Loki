package de.ancash.loki.minecraft;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import de.ancash.loki.LokiPluginDescription;
import de.ancash.loki.plugin.AbstractLokiPlugin;
import de.ancash.loki.plugin.LokiPluginClassLoader;

public class MinecraftLokiPlugin extends AbstractLokiPlugin {

	private final Set<Listener> listeners = new HashSet<>();
	@SuppressWarnings("unchecked")
	private final LokiPluginClassLoader<MinecraftLokiPlugin> loader = (LokiPluginClassLoader<MinecraftLokiPlugin>) getClass()
			.getClassLoader();
	private final File pluginFolder = new File("plugins/Loki/plugins/" + loader.getLoader().getDescription().getName());

	public Logger getLogger() {
		return Bukkit.getLogger();
	}

	public final void registerListener(Listener l) {
		if (listeners.add(l))
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

	public InputStream getResource(String filename) {
		if (filename == null) {
			throw new IllegalArgumentException("Filename cannot be null");
		} else {
			try {
				URL url = loader.getResource(filename);
				if (url == null) {
					return null;
				} else {
					URLConnection connection = url.openConnection();
					connection.setUseCaches(false);
					return connection.getInputStream();
				}
			} catch (IOException var4) {
				return null;
			}
		}
	}

	public File getPluginFolder() {
		return pluginFolder;
	}
}