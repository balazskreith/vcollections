package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.activeconfigs.LinkedListStorageActiveConfig;
import com.balazskreith.vcollections.adapters.SerDe;
import com.balazskreith.vcollections.builders.passiveconfigs.LinkedListStoragePassiveConfig;
import com.balazskreith.vcollections.storages.LinkedListStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public abstract class LinkedListStorageSerDe<K, V> implements SerDe<LinkedListStorage<K, V>> {
	private final ObjectMapper mapper;
	private final LinkedListStoragesConfigAdapter<K, V> configAdapter;

	protected LinkedListStorageSerDe() {
		this.mapper = new ObjectMapper();
		this.configAdapter = new LinkedListStoragesConfigAdapter<>(mapper);
	}

	@Override
	public LinkedListStorage<K, V> deserialize(byte[] data) throws IOException {
		LinkedListStoragePassiveConfig passiveConfig = this.mapper.readValue(data, LinkedListStoragePassiveConfig.class);
		LinkedListStorageActiveConfig<K, V> activeConfig = this.configAdapter.convert(passiveConfig);
		LinkedListStorage<K, V> result = new LinkedListStorage<>(activeConfig);
		return result;
	}

	@Override
	public byte[] serialize(LinkedListStorage<K, V> storage) throws IOException {
		LinkedListStorageActiveConfig<K, V> activeConfig = storage.getConfig();
		LinkedListStoragePassiveConfig passiveConfig = this.configAdapter.deconvert(activeConfig);
		byte[] result = this.mapper.writeValueAsBytes(passiveConfig);
		return result;
	}

}
