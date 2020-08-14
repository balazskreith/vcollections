package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.activeconfigs.ReplicatedStoragesActiveConfig;
import com.balazskreith.vcollections.builders.passiveconfigs.AnyStoragePassiveConfig;
import com.balazskreith.vcollections.builders.passiveconfigs.ReplicatedStoragesPassiveConfig;
import com.balazskreith.vcollections.storages.IStorage;
import com.balazskreith.vcollections.storages.ReplicatedStorages;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a {@link ReplicatedStoragesBuilder} based on the configuration
 */
public class ReplicatedStoragesBuilder extends AbstractStorageBuilder {

	private final AnyStorageConfigAdapter anyStorageConfigAdapter;

	public ReplicatedStoragesBuilder(ObjectMapper objectMapper) {
		super(objectMapper);
		this.anyStorageConfigAdapter = new AnyStorageConfigAdapter();
	}

	public ReplicatedStoragesBuilder() {
		this(new ObjectMapper());
	}

	@Override
	public <K, V> IStorage<K, V> build() {
		ReplicatedStoragesConfigAdapter<K, V> configAdapter = new ReplicatedStoragesConfigAdapter<>(this.getObjectMapper());
		ReplicatedStoragesPassiveConfig passiveConfig = this.getObjectMapper().convertValue(this.getConfigs(), ReplicatedStoragesPassiveConfig.class);
		ReplicatedStoragesActiveConfig<K, V> activeConfig = configAdapter.convert(passiveConfig);
		IStorage<K, V> result = new ReplicatedStorages<>(activeConfig);
		return result;
	}

	public ReplicatedStoragesBuilder withStorageBuilders(StorageBuilder... storageBuilders) {
		for (int i = 0; i < storageBuilders.length; ++i) {
			StorageBuilder source = storageBuilders[i];
			this.withStorageBuilder(source);
		}
		return this;
	}

	public ReplicatedStoragesBuilder withStorageBuilder(StorageBuilder source) {
		List<Map<String, Object>> storageConfigs = this.getConfigs().get(ReplicatedStoragesPassiveConfig.STORAGES_FIELD_NAME);
		if (storageConfigs == null) {
			storageConfigs = new LinkedList<>();
		}
		AnyStoragePassiveConfig passiveConfig = this.anyStorageConfigAdapter.doDeConvert(source);
		AnyStorageBuilder anyStorageBuilder = new AnyStorageBuilder(this.getObjectMapper()).withConfiguration(passiveConfig);
		Map<String, Object> anyStorageConfig = anyStorageBuilder.getConfigurations();
		storageConfigs.add(anyStorageConfig);
		this.getConfigs().set(ReplicatedStoragesPassiveConfig.STORAGES_FIELD_NAME, storageConfigs);
		return this;
	}
}
