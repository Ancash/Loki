package de.ancash.loki.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
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
	private final JarFile jarFile;
	
	public LokiPluginLoader(Logger logger, Class<T> pluginClazz, File file) throws InvalidPluginException, IOException {
		this.file = file;
		this.jarFile = new JarFile(file);
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
		try {
			Enumeration<JarEntry> iter = jarFile.entries();
			while(iter.hasMoreElements()) {
				e = iter.nextElement();
				jarEntries.add(e);
				if(e.getName().equals("loki.yml"))
					description = new LokiPluginDescription(jarFile.getInputStream(e));
			}
		} catch (IOException e1) {
			throw new InvalidPluginException("Could not load" + (e != null ? ": " + e.getName() : "") + ": " + e1.getMessage());
		}
		if(description == null || !description.isValid())
			throw new InvalidPluginException("No/Invalid loki.yml found in " + file.getName());
		try {
			clazzLoader = new LokiPluginClassLoader<>(logger, pluginClazz, this, getClass().getClassLoader(), file, filterClassEntries());
		} catch (IOException e1) {
			throw new InvalidPluginException(e1);
		}
	}
	
	private List<String> filterClassEntries() {
		return jarEntries.stream().filter(e -> e.getName().endsWith(".class")).map(e -> e.getName().replaceAll("/", "\\.")).map(className -> className.substring(0, className.lastIndexOf('.'))).collect(Collectors.toList());
	}
	
	
	public void check() throws InvalidPluginException {
		if(clazzLoader == null) {
			loadJarEntries();
			loadClasses();
		}
	}

	public List<JarEntry> getEntries() {
		return jarEntries;
	}
	
	public InputStream getInputStream(JarEntry e) throws IOException {
		return jarFile.getInputStream(e);
		
	}
	
	public LokiPluginClassLoader<T> getClassLoader() {
		return clazzLoader;
	}
	
	/**
	 * 
	 * @return the plugin description (loki.yml)
	 */
	public LokiPluginDescription getDescription() {
		return description;
	}
	
	/**
	 * See {@link LokiPluginURLClassLoader#loadClasses}
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
		try {
			clazzLoader.nullifyStaticFields();
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | IOException e) {
			logger.log(Level.SEVERE, "Could not nullify static fields", e);
		}
		clazzLoader = null;
		jarEntries.clear();
		plugin = null;
	}
}