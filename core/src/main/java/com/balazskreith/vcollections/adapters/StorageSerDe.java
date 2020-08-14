package com.balazskreith.vcollections.adapters;

import com.balazskreith.vcollections.builders.StorageBuilder;
import com.balazskreith.vcollections.activeconfigs.MemoryStorageActiveConfig;
import com.balazskreith.vcollections.storages.IStorage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;

public class StorageSerDe<K, V> implements SerDe<IStorage<K, V>> {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Override
	public byte[] serialize(IStorage<K, V> storage) throws IOException {
		MemoryStorageActiveConfig config = storage.getConfig();
		return OBJECT_MAPPER.writeValueAsBytes(config);
	}

	@Override
	public IStorage<K, V> deserialize(byte[] data) throws IOException {
		Map<String, Object> config = OBJECT_MAPPER.readValue(
				data, new TypeReference<Map<String, Object>>() {
				});
		return new StorageBuilder().withConfiguration(config).build();
	}

}
