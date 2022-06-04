package de.ancash.loki.exception;

import de.ancash.loki.plugin.AbstractLokiPlugin;

/**
 * Thrown when an error occured during loading a {@link AbstractLokiPlugin}
 * e.g. wrong loki.yml
 */
public class InvalidPluginException extends Exception {

	private static final long serialVersionUID = 2178561272776145069L;

	public InvalidPluginException(Throwable cause) {
		super(cause);
	}

	public InvalidPluginException() {
	}

	public InvalidPluginException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidPluginException(String message) {
		super(message);
	}
}