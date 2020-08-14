package com.balazskreith.vcollections.builders;

/**
 * A common exception thrown if an invalid configuration is detected
 */
public class InvalidConfigurationException extends RuntimeException {
	/**
	 * Constructs {@link this}  exception.
	 */
	public InvalidConfigurationException() {
		super();
	}

	/**
	 * Constructs {@link this} exception with a message.
	 * @param message The message the exception will hold.
	 */
	public InvalidConfigurationException(String message) {
		super(message);
	}

	/**
	 * Constructs {@link this} exception with a message.
	 * @param t An exception thrown originally and this exception should embed it
	 * @param message The message the exception will hold.
	 *
	 */
	public InvalidConfigurationException(Throwable t, String message) {
		super(message, t);
	}
}
