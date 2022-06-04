package de.ancash.loki.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ancash.loki.exception.InvalidPluginException;

/**
 * Responsible for loading all classes and creating new main class instances
 * @param <T>
 */
public class LokiPluginClassLoader<T extends AbstractLokiPlugin> extends URLClassLoader{

	private final List<String> classEntries;
	private final Map<String, Class<?>> classesByName = new HashMap<>();
	private final LokiPluginLoader<T> loader;
	private final Class<T> clazz;
	
	public LokiPluginClassLoader(Class<T> clazz, LokiPluginLoader<T> loader, ClassLoader parent, File file, List<String> classEntries) throws MalformedURLException {
		super(new URL[] {file.toURI().toURL()}, parent);
		this.classEntries = classEntries;
		this.clazz = clazz;
		this.loader = loader;
	}
	
	/**
	 * Loads all given classes
	 * 
	 * @throws InvalidPluginException
	 */
	public void loadClasses() throws InvalidPluginException {
		for(String className : classEntries)
			try {
				classesByName.put(className, loadClass(className));
			} catch(ClassNotFoundException e) {
				throw new InvalidPluginException("Could not load class " + className, e);
			}
	}
	
	/**
	 * Creates a new instance for the main class
	 * of a {@link AbstractLokiPlugin}
	 * 
	 * @return new plugin main class instance
	 * @throws InvalidPluginException
	 */
	@SuppressWarnings("unchecked")
	public T newInstance() throws InvalidPluginException {
		Class<?> jarClass;
		
		try {
			jarClass = Class.forName(loader.getDescription().getMain(), true, this);
		} catch(ClassNotFoundException e) {
			throw new InvalidPluginException("Main class " + loader.getDescription().getMain() + " not found");
		}
		
		Class<?> pluginClass;
		
		try {
			pluginClass = jarClass.asSubclass(clazz);
		} catch(ClassCastException e) {
			throw new InvalidPluginException("Main class" + loader.getDescription().getMain() + " does not extend " + clazz);
		}
		
		try {
			return (T) pluginClass.newInstance();
		} catch (InstantiationException e) {
			throw new InvalidPluginException("Abnormal plugin type", e);
		} catch (IllegalAccessException e) {
			throw new InvalidPluginException("No public constructor", e);
		}
	}
	
	public LokiPluginLoader<T> getLoader() {
		return loader;
	}
	
	/**
	 * Get a class from the {@link AbstractLokiPlugin}
	 * 
	 * @param name
	 * @return class
	 */
	public Class<?> getClazz(String name) {
		return classesByName.get(name);
	}
}