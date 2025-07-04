package de.ancash.loki.exception;

import de.ancash.loki.plugin.AbstractLokiPlugin;

/**
 * Called after a dependency for a {@link AbstractLokiPlugin} cannot be satisfied
 */
public class UnknownDependencyException extends RuntimeException {
	
	private static final long serialVersionUID = -795771244683646309L;

	public UnknownDependencyException(Throwable throwable) {
		super(throwable);
	}

	public UnknownDependencyException(String message) {
		super(message);
	}

	public UnknownDependencyException(Throwable throwable, String message) {
		super(message, throwable);
	}

	public UnknownDependencyException() {
	}
}
