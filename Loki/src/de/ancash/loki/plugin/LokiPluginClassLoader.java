package de.ancash.loki.plugin;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.ancash.loki.exception.InvalidPluginException;
import sun.misc.Unsafe;

/**
 * Responsible for loading all classes and creating new main class instances
 * @param <T>
 */
public class LokiPluginClassLoader<T extends AbstractLokiPlugin> extends URLClassLoader{
	
	private final List<String> classEntries;
	private final Map<String, Class<?>> classesByName = new HashMap<>();
	private LokiPluginLoader<T> loader;
	private final Class<T> clazz;
	@SuppressWarnings("unused")
	private final Logger logger;
	
	public LokiPluginClassLoader(Logger logger, Class<T> clazz, LokiPluginLoader<T> loader, ClassLoader parent, File file, List<String> classEntries) throws MalformedURLException {
		super(new URL[] {file.toURI().toURL()}, parent);
		this.classEntries = classEntries;
		this.clazz = clazz;
		this.logger = logger;
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
				try {
					Class.forName(className);
				} catch(ClassNotFoundException e) {
					classesByName.put(className, loadClass(className));
				}
			} catch(ClassNotFoundException | NoClassDefFoundError e) {
				//logger.warning(String.format("Could not find class %s", className));
			}
	}
	
	/**
	 * Creates a new instance for the main class
	 * of a {@link AbstractLokiPlugin}
	 * 
	 * @return new plugin main class instance
	 * @throws InvalidPluginException
	 */
	@SuppressWarnings({ "unchecked" })
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
	
	public void nullifyStaticFields() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field unsafeField;
		unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
		unsafeField.setAccessible(true);
		Unsafe unsafe = (Unsafe) unsafeField.get(null);
		for(Class<?> clazz : classesByName.values()) {
			Field[] fields = null;
			try {
				fields = clazz.getDeclaredFields();
			} catch(Throwable e) {
				continue;
			}
			for(int i = 0; i<fields.length; i++) {
				try {
					Field field = fields[i];
					if(Modifier.isStatic(field.getModifiers()) && !field.getType().isPrimitive()) {
						if(Modifier.isFinal(field.getModifiers())) {
							Object staticFieldBase = unsafe.staticFieldBase(field);
							long staticFieldOffset = unsafe.staticFieldOffset(field);
							unsafe.putObject(staticFieldBase, staticFieldOffset, null);
						} else {
							field.setAccessible(true);
							field.set(null, field.getType().cast(null));
						}
					}
				} catch(Throwable ex) {
					
				}
			}
		}
		loader = null;
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