package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.activeconfigs.AbstractStorageActiveConfig;
import com.balazskreith.vcollections.activeconfigs.LinkedListStorageActiveConfig;
import com.balazskreith.vcollections.builders.passiveconfigs.LinkedListStoragePassiveConfig;
import com.balazskreith.vcollections.storages.MemoryStorage;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Represents a builder responsible for building a {@link MemoryStorage}.
 */
public class LinkedListStoragesConfigAdapter<K, V> extends AbstractStorageConfigAdapter<K, V, LinkedListStoragePassiveConfig, LinkedListStorageActiveConfig<K, V>> {
	private final ObjectMapper mapper;

	public LinkedListStoragesConfigAdapter(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public LinkedListStoragesConfigAdapter() {
		this(new ObjectMapper());
	}

	@Override
	protected LinkedListStorageActiveConfig<K, V> doConvert(LinkedListStoragePassiveConfig source) {
		AbstractStorageActiveConfig<K, V> abstractStorageActiveConfig = this.getAbstractStorageActiveConfig(source);
		StorageBuilder valuesStorageBuilder = new AnyStorageBuilder(mapper).withConfiguration(source.valuesStorage);
		StorageBuilder keysStorageBuilder = new AnyStorageBuilder(mapper).withConfiguration(source.keysStorage);
		LinkedListStorageActiveConfig<K, V> result = new LinkedListStorageActiveConfig<>(
				abstractStorageActiveConfig,
				keysStorageBuilder,
				valuesStorageBuilder
		);
		return result;
	}


	@Override
	protected LinkedListStoragePassiveConfig doDeConvert(LinkedListStorageActiveConfig<K, V> source) {
		LinkedListStoragePassiveConfig result = new LinkedListStoragePassiveConfig();
		this.setupAbstractStoragePassiveConfig(result, source);
		result.keysStorage = source.keysStorageBuilder.getConfigurations();
		result.valuesStorage = source.valuesStorageBuilder.getConfigurations();
		return result;
	}

}
