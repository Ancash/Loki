package de.ancash.loki.plugin;

import de.ancash.loki.AbstractLokiPluginManager;
import de.ancash.loki.LokiPluginDescription;

/**
 * Abstract plugin class to implement custom methods which are called on certain
 * event e.g {@link AbstractLokiPluginManager#onPluginLoaded(LokiPluginLoader)},
 * {@link AbstractLokiPluginManager#onPluginUnload(LokiPluginLoader)}
 */
public abstract class AbstractLokiPlugin {

	public ClassLoader getClassLoader() {
		return getClass().getClassLoader();
	}
	
	public LokiPluginDescription getDescription() {
		return ((LokiPluginClassLoader<?>) getClassLoader()).getLoader().getDescription();
	}
}