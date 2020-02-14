package com.wobserver.vcollections.storages;

public class KeyUniquenessViolationException extends RuntimeException {
	public KeyUniquenessViolationException() {
		super();
	}

	public KeyUniquenessViolationException(String message) {
		super(message);
	}
}
