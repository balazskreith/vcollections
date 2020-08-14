package com.balazskreith.vcollections.storages;

public class SerializationException extends RuntimeException {
	public SerializationException(String errorMessage, Exception e) {
		super(errorMessage, e);
	}

	public SerializationException(Exception e) {
		super(e);
	}
}
