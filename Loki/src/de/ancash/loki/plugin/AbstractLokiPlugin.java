package de.ancash.loki.plugin;

import de.ancash.loki.AbstractLokiPluginManager;

/**
 * Abstract plugin class to implement custom methods which are called on certain
 * event e.g {@link AbstractLokiPluginManager#onPluginLoaded(LokiPluginLoader)},
 * {@link AbstractLokiPluginManager#onPluginUnload(LokiPluginLoader)}
 */
public abstract class AbstractLokiPlugin {

	public final long id = System.nanoTime();

	@Override
	public final boolean equals(Object arg0) {
		if(arg0 == null)
			return false;
		if(!(arg0 instanceof AbstractLokiPlugin))
			return false;
		return ((AbstractLokiPlugin) arg0).id == id;
	}
}