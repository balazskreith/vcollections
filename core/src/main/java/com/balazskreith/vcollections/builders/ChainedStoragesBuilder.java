package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.activeconfigs.ChainedStoragesActiveConfig;
import com.balazskreith.vcollections.builders.passiveconfigs.AnyStoragePassiveConfig;
import com.balazskreith.vcollections.builders.passiveconfigs.ChainedStoragesPassiveConfig;
import com.balazskreith.vcollections.storages.ChainedStorages;
import com.balazskreith.vcollections.storages.IStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a {@link ChainedStoragesBuilder} based on the configuration
 */
public class ChainedStoragesBuilder extends AbstractStorageBuilder {

	private final AnyStorageConfigAdapter anyStorageConfigAdapter;

	public ChainedStoragesBuilder(ObjectMapper objectMapper) {
		super(objectMapper);
		this.anyStorageConfigAdapter = new AnyStorageConfigAdapter();
	}

	public ChainedStoragesBuilder() {
		this(new ObjectMapper());
	}

	@Override
	public <K, V> IStorage<K, V> build() {
		ChainedStoragesConfigAdapter<K, V> configAdapter = new ChainedStoragesConfigAdapter<>(this.getObjectMapper());
		ChainedStoragesPassiveConfig passiveConfig = this.getObjectMapper().convertValue(this.getConfigs(), ChainedStoragesPassiveConfig.class);
		ChainedStoragesActiveConfig<K, V> activeConfig = configAdapter.convert(passiveConfig);
		IStorage<K, V> result = new ChainedStorages<>(activeConfig);
		return result;
	}

	public ChainedStoragesBuilder withStorageBuilders(StorageBuilder... storageBuilders) {
		for (int i = 0; i < storageBuilders.length; ++i) {
			StorageBuilder source = storageBuilders[i];
			this.withStorageBuilder(source);
		}
		return this;
	}

	public ChainedStoragesBuilder withStorageBuilder(StorageBuilder source) {
		List<Map<String, Object>> storageConfigs = this.getConfigs().get(ChainedStoragesPassiveConfig.STORAGES_FIELD_NAME);
		if (storageConfigs == null) {
			storageConfigs = new LinkedList<>();
		}
		AnyStoragePassiveConfig passiveConfig = this.anyStorageConfigAdapter.doDeConvert(source);
		AnyStorageBuilder anyStorageBuilder = new AnyStorageBuilder(this.getObjectMapper()).withConfiguration(passiveConfig);
		Map<String, Object> anyStorageConfig = anyStorageBuilder.getConfigurations();
		storageConfigs.add(anyStorageConfig);
		this.getConfigs().set(ChainedStoragesPassiveConfig.STORAGES_FIELD_NAME, storageConfigs);
		return this;
	}

}
