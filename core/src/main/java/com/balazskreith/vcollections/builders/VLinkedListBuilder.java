package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.VLinkedList;
import com.balazskreith.vcollections.activeconfigs.VLinkedListActiveConfig;
import com.balazskreith.vcollections.builders.passiveconfigs.VLinkedListPassiveConfig;
import com.balazskreith.vcollections.storages.IStorage;
import com.balazskreith.vcollections.storages.MemoryStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

/**
 * Represents a builder responsible for building a {@link MemoryStorage}.
 */
public class VLinkedListBuilder extends AbstractBuilder {

	public VLinkedListBuilder(ObjectMapper objectMapper) {
		super(objectMapper);
	}

	public VLinkedListBuilder() {
		this(new ObjectMapper());
	}

	/**
	 * Builds an {@link MemoryStorage} based on the provided configurations.
	 *
	 * @param <K> The type of the key for the {@link IStorage}.
	 * @param <V> The type of the value for the {@link IStorage}.
	 * @return Returns an {@link MemoryStorage} set up with the given configurations
	 */
	public <K, V> List<V> build() {
		VLinkedListConfigAdapter<K, V> configAdapter = new VLinkedListConfigAdapter<>();
		VLinkedListPassiveConfig config = this.getObjectMapper().convertValue(this.getConfigs(),
				VLinkedListPassiveConfig.class);
		VLinkedListActiveConfig<K, V> activeConfig = configAdapter.convert(config);
		VLinkedList<K, V> result = new VLinkedList<K, V>(activeConfig);
		return result;
	}

	/**
	 * Puts all configuration to the inside holder in the builder.
	 * These configurations are used when the {@link this#build()} is called.
	 *
	 * @param configs The provided configurations
	 * @return {@link this} builder.
	 */
	public VLinkedListBuilder withConfiguration(Map<String, Object> configs) {
		this.configs.putAll(configs);
		return this;
	}


	public Map<String, Object> getConfigurations() {
		return this.getConfigs();
	}

	public VLinkedListBuilder withKeysStorageBuilder(StorageBuilder storageBuilder) {
		return this.withKeysStorageConfiguration(storageBuilder.getConfigurations());
	}

	public VLinkedListBuilder withKeysStorageConfiguration(Map<String, Object> storageConfiguration) {
		this.getConfigs().put(VLinkedListPassiveConfig.STORAGE_CONFIGURATION, storageConfiguration);
		return this;
	}

	public <T> T getConfiguration(String key) {
		return this.getConfigs().get(key);
	}

}
