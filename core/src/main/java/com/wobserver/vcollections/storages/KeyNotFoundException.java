package com.wobserver.vcollections.storages;

public class KeyNotFoundException extends RuntimeException {
	public KeyNotFoundException() {
		super();
	}

	public KeyNotFoundException(String message) {
		super(message);
	}
}
