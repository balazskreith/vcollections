package com.balazskreith.vcollections.adapters;

public interface SerDe<T> extends Serializer<T>, Deserializer<T> {

	@Override
	default void close() {

	}

	default Serializer<T> getSerializer() {
		return this;
	}

	default Deserializer<T> getDeserializer() {
		return this;
	}
}
