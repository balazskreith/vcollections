package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.activeconfigs.CachedStorageActiveConfig;
import com.balazskreith.vcollections.adapters.SerDe;
import com.balazskreith.vcollections.builders.passiveconfigs.CachedStoragePassiveConfig;
import com.balazskreith.vcollections.storages.CachedStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public abstract class CachedStorageSerDe<K, V> implements SerDe<CachedStorage<K, V>> {
	private final ObjectMapper mapper;
	private final CachedStorageConfigAdapter<K, V> configAdapter;

	protected CachedStorageSerDe() {
		this.mapper = new ObjectMapper();
		this.configAdapter = new CachedStorageConfigAdapter<>(this.mapper);
	}

	@Override
	public CachedStorage<K, V> deserialize(byte[] data) throws IOException {
		CachedStoragePassiveConfig passiveConfig = this.mapper.readValue(data, CachedStoragePassiveConfig.class);
		CachedStorageActiveConfig<K, V> activeConfig = this.configAdapter.convert(passiveConfig);
		CachedStorage<K, V> result = new CachedStorage<>(activeConfig);
		return result;
	}

	@Override
	public byte[] serialize(CachedStorage<K, V> storage) throws IOException {
		CachedStorageActiveConfig<K, V> activeConfig = storage.getConfig();
		CachedStoragePassiveConfig passiveConfig = this.configAdapter.deconvert(activeConfig);
		byte[] result = this.mapper.writeValueAsBytes(passiveConfig);
		return result;
	}

}
