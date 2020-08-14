package com.balazskreith.vcollections.adapters;

import com.balazskreith.vcollections.storages.SerializationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class POJOSerDe<T> implements SerDe<T> {
	static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final Class<T> klass;

	public POJOSerDe(Class<T> klass) {
		this.klass = klass;
	}

	@Override
	public T deserialize(byte[] data) throws IOException {
		if (data == null) {
			return null;
		}

		try {
			return OBJECT_MAPPER.readValue(data, this.klass);
		} catch (final IOException e) {
			throw new SerializationException(e);
		}
	}

	@Override
	public byte[] serialize(T data) throws IOException {
		if (data == null) {
			return null;
		}

		try {
			return OBJECT_MAPPER.writeValueAsBytes(data);
		} catch (final Exception e) {
			throw new SerializationException("Error serializing JSON message", e);
		}
	}
}
