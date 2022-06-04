package de.ancash.loki.plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.ancash.loki.LokiPluginDescription;
import de.ancash.loki.exception.InvalidPluginException;

public class LokiPluginLoader<T extends AbstractLokiPlugin> {

	private final File file;
	private final List<JarEntry> jarEntries = new ArrayList<>();
	private T plugin;
	private LokiPluginClassLoader<T> clazzLoader;
	private LokiPluginDescription description;
	private final Class<T> pluginClazz;
	private final Logger logger;
	
	public LokiPluginLoader(Logger logger, Class<T> pluginClazz, File file) throws InvalidPluginException {
		this.file = file;
		this.logger = logger;
		this.pluginClazz = pluginClazz;
	}

	/**
	 * @return file
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * Loads the jar
	 * 
	 * @throws InvalidPluginException
	 */
	public void loadJarEntries() throws InvalidPluginException {
		JarEntry e = null;
		try(JarFile f = new JarFile(file)) {
			Enumeration<JarEntry> iter = f.entries();
			while(iter.hasMoreElements()) {
				e = iter.nextElement();
				jarEntries.add(e);
				if(e.getName().equals("loki.yml"))
					description = new LokiPluginDescription(f.getInputStream(e));
			}
		} catch (IOException e1) {
			throw new InvalidPluginException("Could not load" + (e != null ? ": " + e.getName() : "") + ": " + e1.getMessage());
		}
		if(description == null || !description.isValid())
			throw new InvalidPluginException("No/Invalid loki.yml found in " + file.getName());
		try {
			clazzLoader = new LokiPluginClassLoader<>(pluginClazz, this, getClass().getClassLoader(), file, filterClassEntries());
		} catch (MalformedURLException e1) {
			throw new InvalidPluginException(e1);
		}
		logger.info("Loaded plugin " + description.getName() + " version " + description.getVersion() + " by " + description.getAuthor());
		
	}
	
	private List<String> filterClassEntries() {
		return jarEntries.stream().filter(e -> e.getName().endsWith(".class")).map(e -> e.getName().replaceAll("/", "\\.")).map(className -> className.substring(0, className.lastIndexOf('.'))).collect(Collectors.toList());
	}

	/**
	 * 
	 * @return the plugin description (loki.yml)
	 */
	public LokiPluginDescription getDescription() {
		return description;
	}
	
	/**
	 * See {@link LokiPluginClassLoader#loadClasses}
	 * 
	 * @throws InvalidPluginException
	 */
	public void loadClasses() throws InvalidPluginException {
		clazzLoader.loadClasses();
	}
	
	public void newInstance() throws InvalidPluginException {
		plugin = clazzLoader.newInstance();
	}
	
	public T getPlugin() {
		return plugin;
	}

	public void unload() {
		clazzLoader = null;
		jarEntries.clear();
		description = null;
		plugin = null;
	}
}