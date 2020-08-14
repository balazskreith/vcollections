package com.balazskreith.vcollections.adapters;

import java.io.Closeable;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface Serializer<T> extends Converter<T, byte[]>, Closeable {

	Logger logger = LoggerFactory.getLogger(Serializer.class);

	byte[] serialize(T data) throws IOException;

	@Override
	default byte[] convert(T data) {
		byte[] result;
		try {
			result = this.serialize(data);
		} catch (IOException e) {
			logger.error("Error during conversion", e);
			result = null;
		}
		return result;
	}

	@Override
	default void close() {

	}
}
