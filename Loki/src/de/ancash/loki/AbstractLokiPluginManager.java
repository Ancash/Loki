
package de.ancash.loki;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.ancash.loki.exception.InvalidPluginException;
import de.ancash.loki.exception.UnknownDependencyException;
import de.ancash.loki.plugin.AbstractLokiPlugin;
import de.ancash.loki.plugin.LokiPluginClassLoader;
import de.ancash.loki.plugin.LokiPluginLoader;

public abstract class AbstractLokiPluginManager<T extends AbstractLokiPlugin> {

	private final File dir;
	private final Class<T> pluginClazz;
	private final List<T> loadedPlugins = new ArrayList<>();
	private final HashMap<String, LokiPluginLoader<T>> pluginLoadersByName = new HashMap<>();
	private final Logger logger;

	public AbstractLokiPluginManager(Logger logger, Class<T> pluginClazz, File dir) {
		this.logger = logger;
		this.pluginClazz = pluginClazz;
		this.dir = dir;
	}

	/**
	 * Loading all jars in the given directory
	 * 
	 * @return
	 */
	private void loadJars() {
		File[] files = dir.listFiles();
		for (File jar : files)
			try {
				LokiPluginLoader<T> u = new LokiPluginLoader<>(pluginClazz, jar);
				u.loadJarEntries();
				u.loadClasses();
				pluginLoadersByName.put(u.getDescription().getName(), u);
				onJarLoaded(u);
			} catch (InvalidPluginException e) {
				logger.severe("Could not load plugin " + jar.getName() + ": " + e.getMessage());
			}
	}

	/**
	 * Load specific jar file
	 * 
	 * @param file
	 * @return
	 */
	public LokiPluginLoader<T> loadJar(File file) {
		try {
			LokiPluginLoader<T> u = new LokiPluginLoader<>(pluginClazz, file);
			u.loadJarEntries();
			u.loadClasses();
			pluginLoadersByName.put(u.getDescription().getName(), u);
			onJarLoaded(u);
			return u;
		} catch (InvalidPluginException e) {
			logger.severe("Could not load plugin " + file.getName() + ": " + e.getMessage());
			return null;
		}
	}

	/**
	 * Called after a jar in {@link #loadJars} has been loaded.
	 * 
	 * @param t
	 */
	public abstract void onJarLoaded(LokiPluginLoader<T> t);

	/**
	 * Called after a new instance of the main class of the
	 * {@link AbstractLokiPlugin} has been created
	 * 
	 * @param t {@link LokiPluginLoader} responsible for plugin
	 */
	public abstract void onPluginLoaded(LokiPluginLoader<T> t);

	/**
	 * Necessary to remove all references to {@link AbstractLokiPlugin}
	 * 
	 * @param t
	 */
	public abstract void onPluginUnload(LokiPluginLoader<T> t);

	private void loadPlugins() {
		Map<String, File> plugins = new HashMap<>();
		Set<String> loadedPlugins = new HashSet<>();
		Map<String, Collection<String>> dependencies = new HashMap<>();
		Map<String, Collection<String>> softDependencies = new HashMap<>();

		for (Entry<String, LokiPluginLoader<T>> entry : pluginLoadersByName.entrySet()) {
			LokiPluginDescription description = entry.getValue().getDescription();
			File replacedFile = (File) plugins.put(description.getName(), entry.getValue().getFile());
			if (replacedFile != null) {
				logger.severe(String.format("Ambiguous plugin name `%s' for files `%s' and `%s' in `%s'",
						description.getName(), entry.getValue().getFile().getPath(), replacedFile.getPath(),
						dir.getPath()));
			}

			Collection<String> softDependencySet = description.getSoftDepend();
			if (softDependencySet != null && !softDependencySet.isEmpty()) {
				if (softDependencies.containsKey(description.getName())) {
					((Collection<String>) softDependencies.get(description.getName())).addAll(softDependencySet);
				} else {
					softDependencies.put(description.getName(), new LinkedList<>(softDependencySet));
				}
			}

			Collection<String> dependencySet = description.getDepend();
			if (dependencySet != null && !dependencySet.isEmpty()) {
				dependencies.put(description.getName(), new LinkedList<>(dependencySet));
			}

			Collection<String> loadBeforeSet = description.getLoadBefore();
			if (loadBeforeSet != null && !loadBeforeSet.isEmpty()) {
				Iterator<String> var18 = loadBeforeSet.iterator();

				while (var18.hasNext()) {
					String loadBeforeTarget = (String) var18.next();
					if (softDependencies.containsKey(loadBeforeTarget)) {
						((Collection<String>) softDependencies.get(loadBeforeTarget)).add(description.getName());
					} else {
						Collection<String> shortSoftDependency = new LinkedList<>();
						shortSoftDependency.add(description.getName());
						softDependencies.put(loadBeforeTarget, shortSoftDependency);
					}
				}
			}
		}

		while (true) {
			File file;
			boolean missingDependency;
			start: do {
				String plugin;
				Iterator<String> pluginIterator;
				do {
					if (plugins.isEmpty())
						return;

					missingDependency = true;
					pluginIterator = plugins.keySet().iterator();

					while (pluginIterator.hasNext()) {
						plugin = (String) pluginIterator.next();
						Iterator<String> softDependencyIterator;
						String dependency;
						if (dependencies.containsKey(plugin)) {
							softDependencyIterator = ((Collection<String>) dependencies.get(plugin)).iterator();

							while (softDependencyIterator.hasNext()) {
								dependency = (String) softDependencyIterator.next();
								if (loadedPlugins.contains(dependency)) {
									softDependencyIterator.remove();
								} else if (!plugins.containsKey(dependency)) {
									missingDependency = false;
									file = (File) plugins.get(plugin);
									pluginIterator.remove();
									softDependencies.remove(plugin);
									dependencies.remove(plugin);
									logger.log(Level.SEVERE,
											"Could not load '" + file.getPath() + "' in folder '" + dir.getPath() + "'",
											new UnknownDependencyException(dependency));
									break;
								}
							}

							if (dependencies.containsKey(plugin)
									&& ((Collection<String>) dependencies.get(plugin)).isEmpty()) {
								dependencies.remove(plugin);
							}
						}

						if (softDependencies.containsKey(plugin)) {
							softDependencyIterator = ((Collection<String>) softDependencies.get(plugin)).iterator();

							while (softDependencyIterator.hasNext()) {
								dependency = (String) softDependencyIterator.next();
								if (!plugins.containsKey(dependency)) {
									softDependencyIterator.remove();
								}
							}

							if (((Collection<String>) softDependencies.get(plugin)).isEmpty()) {
								softDependencies.remove(plugin);
							}
						}

						if (!dependencies.containsKey(plugin) && !softDependencies.containsKey(plugin)
								&& plugins.containsKey(plugin)) {
							file = (File) plugins.get(plugin);
							pluginIterator.remove();
							missingDependency = false;

							try {
								loadPlugin(plugin);
								loadedPlugins.add(plugin);
								break;
							} catch (InvalidPluginException var21) {
								logger.log(Level.SEVERE,
										"Could not load '" + file.getPath() + "' in folder '" + dir.getPath() + "'",
										var21);
							}
						}
					}
				} while (!missingDependency);

				pluginIterator = plugins.keySet().iterator();

				while (true) {
					do {
						if (!pluginIterator.hasNext())
							continue start;

						plugin = (String) pluginIterator.next();
					} while (dependencies.containsKey(plugin));

					softDependencies.remove(plugin);
					missingDependency = false;
					file = (File) plugins.get(plugin);
					pluginIterator.remove();

					try {
						loadPlugin(plugin);
						loadedPlugins.add(plugin);
						break;
					} catch (InvalidPluginException var22) {
						logger.log(Level.SEVERE,
								"Could not load '" + file.getPath() + "' in folder '" + dir.getPath() + "'", var22);
					}
				}
			} while (!missingDependency);

			softDependencies.clear();
			dependencies.clear();
			Iterator<File> failedPluginIterator = plugins.values().iterator();

			while (failedPluginIterator.hasNext()) {
				file = (File) failedPluginIterator.next();
				failedPluginIterator.remove();
				logger.log(Level.SEVERE, "Could not load '" + file.getPath() + "' in folder '" + dir.getPath()
						+ "': circular dependency detected");
			}
		}
	}

	/**
	 * Calls {@link AbstractLokiPluginManager#load()}
	 * 
	 * @return
	 */
	public void reload() {
		load();
	}

	/**
	 * Calls in order: {@link AbstractLokiPluginManager#unload()}
	 * {@link AbstractLokiPluginManager#loadJars()}
	 * {@link AbstractLokiPluginManager#loadPlugins()}
	 * 
	 * @return this
	 */
	public void load() {
		unload();
		loadJars();
		loadPlugins();
	}

	/**
	 * @see AbstractLokiPluginManager#loadPlugin(LokiPluginLoader)
	 * @param name
	 * @throws InvalidPluginException
	 */
	public void loadPlugin(String name) throws InvalidPluginException {
		loadPlugin(pluginLoadersByName.get(name));
	}
	
	/**
	 * Creates a new instance of a already loaded plugin jar with the given name
	 * 
	 * @param l loader responsible for plugin
	 * @throws InvalidPluginException
	 */
	public void loadPlugin(LokiPluginLoader<T> l) throws InvalidPluginException {
		if(l.getDescription() != null && isPluginLoaded(l.getDescription().getName()))
			throw new InvalidPluginException("Plugin already loaded");
		l.check();
		pluginLoadersByName.put(l.getDescription().getName(), l);
		l.newInstance();
		loadedPlugins.add(l.getPlugin());
		onPluginLoaded(l);
	}

	/**
	 * @param name plugin name
	 * @return whether loaded or not
	 */
	public boolean isPluginLoaded(String name) {
		return pluginLoadersByName.containsKey(name) && pluginLoadersByName.get(name).getPlugin() != null;
	}

	/**
	 * @see AbstractLokiPluginManager#unloadPlugin(LokiPluginLoader)
	 * @param name plugin name
	 */
	public void unloadPlugin(String name) {
		unloadPlugin(pluginLoadersByName.get(name));
	}

	/**
	 * Unloads the given plugin
	 * @param l plugin to unload
	 */
	public void unloadPlugin(LokiPluginLoader<T> l) {
		onPluginUnload(l);
		AbstractLokiPlugin pl = l.getPlugin();
		pluginLoadersByName.get(l.getDescription().getName()).unload();
		loadedPlugins.remove(pl);
		Runtime.getRuntime().gc();
	}
	
	public AbstractLokiPlugin getPlugin(String name) {
		return pluginLoadersByName.get(name).getPlugin();
	}
	
	/**
	 * 
	 * @return all loaded plugins
	 */
	public List<T> getPlugins() {
		return Collections.unmodifiableList(loadedPlugins);
	}

	/**
	 * @see AbstractLokiPluginManager#reloadPlugin(LokiPluginLoader)
	 * @param name
	 * @throws InvalidPluginException 
	 */
	public void reloadPlugin(String name) throws InvalidPluginException {
		reloadPlugin(pluginLoadersByName.get(name));
	}
	
	public void reloadPlugin(LokiPluginLoader<T> l) throws InvalidPluginException {
		unloadPlugin(l);
		loadPlugin(l);
	}
	
	/**
	 * Removes all references to all loaded {@link AbstractLokiPlugin} and
	 * {@link LokiPluginLoader}. If a {@link AbstractLokiPlugin} has references
	 * outside of this package, it's necessary to remove these
	 * ({@link AbstractLokiPluginManager#onPluginUnload}) in order for the gc to be
	 * able to unload all classes.
	 * 
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	public void unload() {
		while (!loadedPlugins.isEmpty()) {
			unloadPlugin(
					((LokiPluginClassLoader<T>) loadedPlugins.get(loadedPlugins.size() - 1).getClass().getClassLoader())
							.getLoader().getDescription().getName());
		}
	}
}