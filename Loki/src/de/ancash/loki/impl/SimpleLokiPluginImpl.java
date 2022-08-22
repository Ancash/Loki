package de.ancash.loki.impl;

import java.io.InputStream;

import de.ancash.loki.plugin.AbstractLokiPlugin;
import de.ancash.loki.plugin.LokiPluginClassLoader;

public class SimpleLokiPluginImpl extends AbstractLokiPlugin{

	private final LokiPluginClassLoader<?> loader = (LokiPluginClassLoader<?>) getClass().getClassLoader();
	
	public void onEnable() {
		
	}
	
	public void onDisable() {
		
	}
	
	public InputStream getResource(String path) {
		return loader.getResourceAsStream(path);
	}
}