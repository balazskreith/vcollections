package com.balazskreith.vcollections.storages;

public class KeyUniquenessViolationException extends RuntimeException {
	public KeyUniquenessViolationException() {
		super();
	}

	public KeyUniquenessViolationException(String message) {
		super(message);
	}
}
