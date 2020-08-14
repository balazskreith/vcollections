package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.adapters.SerDe;
import com.balazskreith.vcollections.storages.IStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public abstract class StorageSerDe<K, V> implements SerDe<IStorage<K, V>> {
	private final ObjectMapper objectMapper;
	private final String builder;
	private Adapter<>
	protected StorageSerDe(ObjectMapper objectMapper, String builder) {
		this.builder = builder;
		this.objectMapper = objectMapper;
	}

	@Override
	public IStorage<K, V> deserialize(byte[] data) throws IOException {
		return null;
	}

	@Override
	public byte[] serialize(IStorage<K, V> data) throws IOException {
		return new byte[0];
	}
	
}
