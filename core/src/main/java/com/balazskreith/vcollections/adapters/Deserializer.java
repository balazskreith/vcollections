package com.balazskreith.vcollections.adapters;

import java.io.Closeable;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface Deserializer<T> extends Deconverter<T, byte[]>, Closeable {

	Logger logger = LoggerFactory.getLogger(Deserializer.class);

	T deserialize(byte[] data) throws IOException;

	@Override
	default T deconvert(byte[] data) {
		T result;
		try {
			result = this.deserialize(data);
		} catch (IOException e) {
			logger.error("Error during deconversion", e);
			result = null;
		}
		return result;
	}

	@Override
	default void close() {

	}

}
