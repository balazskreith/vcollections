package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.activeconfigs.MemoryStorageActiveConfig;
import com.balazskreith.vcollections.builders.passiveconfigs.MemoryStoragePassiveConfig;
import com.balazskreith.vcollections.storages.IStorage;
import com.balazskreith.vcollections.storages.MemoryStorage;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Represents a builder responsible for building a {@link MemoryStorage}.
 */
public class MemoryStorageBuilder extends AbstractStorageBuilder {

	public MemoryStorageBuilder(ObjectMapper objectMapper) {
		super(objectMapper);
	}

	public MemoryStorageBuilder() {
		this(new ObjectMapper());
	}

	/**
	 * Builds an {@link MemoryStorage} based on the provided configurations.
	 *
	 * @param <K> The type of the key for the {@link IStorage}.
	 * @param <V> The type of the value for the {@link IStorage}.
	 * @return Returns an {@link MemoryStorage} set up with the given configurations
	 */
	@Override
	public <K, V> IStorage<K, V> build() {
		MemoryStorageConfigAdapter<K, V> configAdapter = new MemoryStorageConfigAdapter<>();
		MemoryStoragePassiveConfig config = this.getObjectMapper().convertValue(this.getConfigs(), MemoryStoragePassiveConfig.class);
		MemoryStorageActiveConfig<K, V> activeConfig = configAdapter.convert(config);
		MemoryStorage<K, V> result = new MemoryStorage<>(activeConfig);
		return result;
	}
}
