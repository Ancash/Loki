package de.ancash.loki.impl;

import java.io.File;
import java.util.logging.Logger;

import de.ancash.loki.AbstractLokiPluginManager;
import de.ancash.loki.plugin.LokiPluginLoader;

public class SimpleLokiPluginManagerImpl extends AbstractLokiPluginManager<SimpleLokiPluginImpl>{

	public SimpleLokiPluginManagerImpl(File dir) {
		super(Logger.getGlobal(), SimpleLokiPluginImpl.class, dir);
	}

	@Override
	public void onJarLoaded(LokiPluginLoader<SimpleLokiPluginImpl> t) {
		
	}

	@Override
	public void onPluginLoaded(LokiPluginLoader<SimpleLokiPluginImpl> t) {
		try {
			t.getPlugin().onEnable();
		} catch(Throwable ex) {
			System.err.println("Could not enable " + t.getDescription().getName() + " " + t.getDescription().getVersion());
			ex.printStackTrace();
		}
	}

	@Override
	public void onPluginUnload(LokiPluginLoader<SimpleLokiPluginImpl> t) {
		try {
			t.getPlugin().onDisable();
		} catch(Throwable ex) {
			System.err.println("Could not disable " + t.getDescription().getName() + " " + t.getDescription().getVersion());
			ex.printStackTrace();
		}
	}
}