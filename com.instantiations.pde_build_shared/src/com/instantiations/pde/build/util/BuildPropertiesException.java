package com.instantiations.pde.build.util;

/**
 * Exception used by {@link BuildProperties}
 */
public class BuildPropertiesException extends RuntimeException
{
	private static final long serialVersionUID = -3163394993823113876L;

	public BuildPropertiesException() {
		super();
	}

	public BuildPropertiesException(String message) {
		super(message);
	}

	public BuildPropertiesException(Throwable cause) {
		super(cause);
	}

	public BuildPropertiesException(String message, Throwable cause) {
		super(message, cause);
	}
}
