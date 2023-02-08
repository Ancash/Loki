package de.ancash.loki;

import java.io.File;

import de.ancash.loki.impl.SimpleLokiPluginManagerImpl;

public class Main {

	
	public static void main(String[] args) throws InterruptedException {
		SimpleLokiPluginManagerImpl manager  =new SimpleLokiPluginManagerImpl(new File("plugins"));
		manager.unload();
		manager.loadJars();
		manager.loadPlugins();
	}
	
}
