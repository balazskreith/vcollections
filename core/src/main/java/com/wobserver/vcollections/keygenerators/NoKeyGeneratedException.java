package com.wobserver.vcollections.keygenerators;

/**
 * Thrown exception indicate the missing of a Keygenerator
 */
public class NoKeyGeneratedException extends RuntimeException {
	/**
	 * Constructs an exception indicate the lack of Keygenerator
	 */
	public NoKeyGeneratedException() {
		super();
	}

	/**
	 *
	 * @param message message of the exception provide more detail about the error
	 */
	public NoKeyGeneratedException(String message) {
		super(message);
	}
}
