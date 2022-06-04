package de.ancash.loki;

import java.io.File;

import de.ancash.loki.impl.SimpleLokiPluginManagerImpl;

public class Main {

	
	public static void main(String[] args) throws InterruptedException {
		SimpleLokiPluginManagerImpl manager  =new SimpleLokiPluginManagerImpl(new File("plugins"));
		while(true) {
			manager.reload();
			Thread.sleep(1000);
		}
	}
	
}
