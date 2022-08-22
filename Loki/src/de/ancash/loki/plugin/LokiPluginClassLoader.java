package de.ancash.loki.plugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.ancash.loki.exception.InvalidPluginException;
import sun.misc.Unsafe;


/**
 * Responsible for loading all classes and creating new main class instances
 * 
 * @param <T>
 */
public class LokiPluginClassLoader<T extends AbstractLokiPlugin> extends ClassLoader {

	private final Map<String, Class<?>> classesByName = new HashMap<>();
	private LokiPluginLoader<T> loader;
	private final Class<T> clazz;
	@SuppressWarnings("unused")
	private final Logger logger;
	private final Map<String, Object> classBytesByName = new HashMap<>();

	public LokiPluginClassLoader(Logger logger, Class<T> clazz, LokiPluginLoader<T> loader, ClassLoader parent,
			File file, List<String> classEntries) throws IOException {
		super(parent);
		for (JarEntry e : loader.getEntries().stream().filter(e -> e.getName().endsWith(".class"))
				.collect(Collectors.toSet())) {

			InputStream in = loader.getInputStream(e);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[2048];
			int read = 0;
			while ((read = in.read(buf)) > 0) {
				out.write(buf, 0, read);
			}
			classBytesByName.put(e.getName().replaceAll("/", "\\.").replaceAll(".class", ""), out.toByteArray());
		}
		this.clazz = clazz;
		this.logger = logger;
		this.loader = loader;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		Optional<JarEntry> opt = loader.getEntries().stream().filter(entry -> entry.getName().equals(name)).findAny();
		if(!opt.isPresent())
			return super.getResourceAsStream(name);
		try {
			return loader.getInputStream(opt.get());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Loads all given classes
	 * 
	 * @throws InvalidPluginException
	 */
	public void loadClasses() throws InvalidPluginException {
		int cnt = 0;
		while(classesByName.size() != classBytesByName.size() && cnt < classBytesByName.size()) {
			for (Entry<String, Object> entry : classBytesByName.entrySet()) {
				if(classesByName.containsKey(entry.getKey()))
					continue;
				try {
					try {
						Class.forName(entry.getKey());
					} catch (ClassNotFoundException e) {
						classesByName.put(entry.getKey(), defineClass(entry.getKey(), (byte[]) entry.getValue(), 0, ((byte[]) entry.getValue()).length));
					}
				} catch (NoClassDefFoundError e) {
					//logger.warning(String.format("Could not find class %s", entry.getKey()));
				}
			}
			cnt++;
		}
		
		for(Class<?> unresolved : classesByName.values())
			resolveClass(unresolved);
	}
	
	/**
	 * Creates a new instance for the main class of a {@link AbstractLokiPlugin}
	 * 
	 * @return new plugin main class instance
	 * @throws InvalidPluginException
	 */
	@SuppressWarnings({ "unchecked" })
	public T newInstance() throws InvalidPluginException {
		Class<?> jarClass;

		try {
			jarClass = Class.forName(loader.getDescription().getMain(), true, this);
		} catch (ClassNotFoundException e) {
			throw new InvalidPluginException("Main class " + loader.getDescription().getMain() + " not found");
		}

		Class<?> pluginClass;

		try {
			pluginClass = jarClass.asSubclass(clazz);
		} catch (ClassCastException e) {
			throw new InvalidPluginException(
					"Main class" + loader.getDescription().getMain() + " does not extend " + clazz);
		}

		try {
			return (T) pluginClass.newInstance();
		} catch (InstantiationException e) {
			throw new InvalidPluginException("Abnormal plugin type", e);
		} catch (IllegalAccessException e) {
			throw new InvalidPluginException("No public constructor", e);
		}
	}

	public void nullifyStaticFields() throws NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException, IOException {
		Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
		unsafeField.setAccessible(true);
		Unsafe unsafe = (Unsafe) unsafeField.get(null);
		for (Class<?> clazz : classesByName.values()) {
			Field[] fields = null;
			try {
				fields = clazz.getDeclaredFields();
			} catch (Throwable e) {
				continue;
			}
			for (int i = 0; i < fields.length; i++) {
				try {
					Field field = fields[i];
					if (Modifier.isStatic(field.getModifiers()) && !field.getType().isPrimitive()) {
						field.setAccessible(true);
						if (Modifier.isFinal(field.getModifiers())) {
							unsafe.putObject(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field), null);
						} else {
							field.set(null, null);
						}
						field.setAccessible(false);
					}
				} catch (Throwable ex) {

				}
			}
		}
		unsafeField.setAccessible(false);
		loader = null;
		classBytesByName.clear();
		classesByName.clear();
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
	
	@Override
	public String toString() {
		return super.toString() + "(" + loader.getDescription().getName() + ")";
	}
}