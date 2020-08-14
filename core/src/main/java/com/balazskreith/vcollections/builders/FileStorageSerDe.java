package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.activeconfigs.FileStorageActiveConfig;
import com.balazskreith.vcollections.adapters.SerDe;
import com.balazskreith.vcollections.builders.passiveconfigs.FileStoragePassiveConfig;
import com.balazskreith.vcollections.storages.FileStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public abstract class FileStorageSerDe<K, V> implements SerDe<FileStorage<K, V>> {
	private final ObjectMapper mapper;
	private final FileStorageConfigAdapter<K, V> configAdapter;

	protected FileStorageSerDe() {
		this.mapper = new ObjectMapper();
		this.configAdapter = new FileStorageConfigAdapter<>();
	}

	@Override
	public FileStorage<K, V> deserialize(byte[] data) throws IOException {
		FileStoragePassiveConfig passiveConfig = this.mapper.readValue(data, FileStoragePassiveConfig.class);
		FileStorageActiveConfig<K, V> activeConfig = this.configAdapter.convert(passiveConfig);
		FileStorage<K, V> result = new FileStorage<>(activeConfig);
		return result;
	}

	@Override
	public byte[] serialize(FileStorage<K, V> storage) throws IOException {
		FileStorageActiveConfig<K, V> activeConfig = storage.getConfig();
		FileStoragePassiveConfig passiveConfig = this.configAdapter.deconvert(activeConfig);
		byte[] result = this.mapper.writeValueAsBytes(passiveConfig);
		return result;
	}

}
