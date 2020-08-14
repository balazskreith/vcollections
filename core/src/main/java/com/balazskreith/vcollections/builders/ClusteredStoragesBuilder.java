package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.activeconfigs.ClusteredStoragesActiveConfig;
import com.balazskreith.vcollections.builders.passiveconfigs.AnyStoragePassiveConfig;
import com.balazskreith.vcollections.builders.passiveconfigs.ClusteredStoragesPassiveConfig;
import com.balazskreith.vcollections.storages.ClusteredStorages;
import com.balazskreith.vcollections.storages.IStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a {@link ReplicatedStoragesBuilder} based on the configuration
 */
public class ClusteredStoragesBuilder extends AbstractStorageBuilder {

	private final AnyStorageConfigAdapter anyStorageConfigAdapter;

	public ClusteredStoragesBuilder(ObjectMapper objectMapper) {
		super(objectMapper);
		this.anyStorageConfigAdapter = new AnyStorageConfigAdapter();
	}

	public ClusteredStoragesBuilder() {
		this(new ObjectMapper());
	}

	@Override
	public <K, V> IStorage<K, V> build() {
		ClusteredStoragesConfigAdapter<K, V> configAdapter = new ClusteredStoragesConfigAdapter<>(this.getObjectMapper());
		ClusteredStoragesPassiveConfig passiveConfig = this.getObjectMapper().convertValue(this.getConfigs(), ClusteredStoragesPassiveConfig.class);
		ClusteredStoragesActiveConfig<K, V> activeConfig = configAdapter.convert(passiveConfig);
		IStorage<K, V> result = new ClusteredStorages<>(activeConfig);
		return result;
	}

	public ClusteredStoragesBuilder withStorageBuilders(StorageBuilder... storageBuilders) {
		for (int i = 0; i < storageBuilders.length; ++i) {
			StorageBuilder source = storageBuilders[i];
			this.withStorageBuilder(source);
		}
		return this;
	}

	public ClusteredStoragesBuilder withStorageBuilder(StorageBuilder source) {
		List<Map<String, Object>> storageConfigs = this.getConfigs().get(ClusteredStoragesPassiveConfig.STORAGES_FIELD_NAME);
		if (storageConfigs == null) {
			storageConfigs = new LinkedList<>();
		}
		AnyStoragePassiveConfig passiveConfig = this.anyStorageConfigAdapter.doDeConvert(source);
		AnyStorageBuilder anyStorageBuilder = new AnyStorageBuilder(this.getObjectMapper()).withConfiguration(passiveConfig);
		Map<String, Object> anyStorageConfig = anyStorageBuilder.getConfigurations();
		storageConfigs.add(anyStorageConfig);
		this.getConfigs().set(ClusteredStoragesPassiveConfig.STORAGES_FIELD_NAME, storageConfigs);
		return this;
	}
}
