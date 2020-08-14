package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.activeconfigs.MemoryStorageActiveConfig;
import com.balazskreith.vcollections.adapters.SerDe;
import com.balazskreith.vcollections.builders.passiveconfigs.MemoryStoragePassiveConfig;
import com.balazskreith.vcollections.storages.MemoryStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public abstract class MemoryStorageSerDe<K, V> implements SerDe<MemoryStorage<K, V>> {
	private final ObjectMapper mapper;
	private final MemoryStorageConfigAdapter<K, V> configAdapter;

	protected MemoryStorageSerDe() {
		this.mapper = new ObjectMapper();
		this.configAdapter = new MemoryStorageConfigAdapter<>();
	}

	@Override
	public MemoryStorage<K, V> deserialize(byte[] data) throws IOException {
		MemoryStoragePassiveConfig passiveConfig = this.mapper.readValue(data, MemoryStoragePassiveConfig.class);
		MemoryStorageActiveConfig<K, V> activeConfig = this.configAdapter.convert(passiveConfig);
		MemoryStorage<K, V> result = new MemoryStorage<>(activeConfig);
		return result;
	}

	@Override
	public byte[] serialize(MemoryStorage<K, V> storage) throws IOException {
		MemoryStorageActiveConfig<K, V> activeConfig = storage.getConfig();
		MemoryStoragePassiveConfig passiveConfig = this.configAdapter.deconvert(activeConfig);
		byte[] result = this.mapper.writeValueAsBytes(passiveConfig);
		return result;
	}

}
