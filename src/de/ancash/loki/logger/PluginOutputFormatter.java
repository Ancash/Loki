package de.ancash.loki.logger;

import de.ancash.loki.AbstractLokiPluginManager;
import de.ancash.loki.plugin.LokiPluginClassLoader;
import de.ancash.misc.io.IFormatter;

public class PluginOutputFormatter extends IFormatter{

	@SuppressWarnings("nls")
	public static final String PLUGIN_NAME = "$lpn$";
	private final AbstractLokiPluginManager<?> manager;
	private final String notFound;
	
	public PluginOutputFormatter(String format, AbstractLokiPluginManager<?> manager, String notFound) {
		super(format, true);
		this.manager = manager;
		this.notFound = notFound;
	}

	@SuppressWarnings("nls")
	@Override
	public String formatExtra(String format) {
		LokiPluginClassLoader<?> cl = null;
		for(StackTraceElement stc : Thread.currentThread().getStackTrace()) {
			cl = manager.matchClass(stc.getClassName());
			if(cl != null)
				break;
		}
		if(cl == null) 
			return format.replace(PLUGIN_NAME, notFound);
		return format.replace(PLUGIN_NAME, cl.getLoader().getDescription().getName());
	}
	
}
