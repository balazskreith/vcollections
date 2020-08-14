package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.activeconfigs.CachedStorageActiveConfig;
import com.balazskreith.vcollections.builders.passiveconfigs.AnyStoragePassiveConfig;
import com.balazskreith.vcollections.builders.passiveconfigs.CachedStoragePassiveConfig;
import com.balazskreith.vcollections.storages.CachedStorage;
import com.balazskreith.vcollections.storages.IStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 *
 */
public class CachedStorageBuilder extends AbstractStorageBuilder {
	private final AnyStorageConfigAdapter anyStorageConfigAdapter;

	public CachedStorageBuilder() {
		this(new ObjectMapper());

	}

	public CachedStorageBuilder(ObjectMapper objectMapper) {
		super(objectMapper);
		this.anyStorageConfigAdapter = new AnyStorageConfigAdapter();
	}


	@Override
	public <K, V> IStorage<K, V> build() {
		CachedStorageConfigAdapter<K, V> configAdapter = new CachedStorageConfigAdapter<>(this.getObjectMapper());
		CachedStoragePassiveConfig passiveConfig = this.getObjectMapper().convertValue(this.getConfigs(), CachedStoragePassiveConfig.class);
		CachedStorageActiveConfig<K, V> activeConfig = configAdapter.convert(passiveConfig);
		IStorage<K, V> result = new CachedStorage<>(activeConfig);
		return result;
	}

	public CachedStorageBuilder withSupersetBuilder(StorageBuilder storageBuilder) {
		AnyStoragePassiveConfig passiveConfig = this.anyStorageConfigAdapter.doDeConvert(storageBuilder);
		AnyStorageBuilder anyStorageBuilder = new AnyStorageBuilder(this.getObjectMapper()).withConfiguration(passiveConfig);
		Map<String, Object> anyStorageConfig = anyStorageBuilder.getConfigurations();
		this.getConfigs().set(CachedStoragePassiveConfig.SUPERSET_FIELD_NAME, anyStorageConfig);
		return this;
	}

	public CachedStorageBuilder withSubsetBuilder(StorageBuilder storageBuilder) {
		AnyStoragePassiveConfig passiveConfig = this.anyStorageConfigAdapter.doDeConvert(storageBuilder);
		AnyStorageBuilder anyStorageBuilder = new AnyStorageBuilder(this.getObjectMapper()).withConfiguration(passiveConfig);
		Map<String, Object> anyStorageConfig = anyStorageBuilder.getConfigurations();
		this.getConfigs().set(CachedStoragePassiveConfig.SUBSET_FIELD_NAME, anyStorageConfig);
		return this;
	}

	public CachedStorageBuilder withCacheOnCreate(boolean value) {
		this.getConfigs().set(CachedStoragePassiveConfig.CACHE_ON_CREATE_FIELD_NAME, value);
		return this;
	}

	public CachedStorageBuilder withCacheOnRead(boolean value) {
		this.getConfigs().set(CachedStoragePassiveConfig.CACHE_ON_READ_FIELD_NAME, value);
		return this;
	}

	public CachedStorageBuilder withCacheOnUpdate(boolean value) {
		this.getConfigs().set(CachedStoragePassiveConfig.CACHE_ON_UPDATE_FIELD_NAME, value);
		return this;
	}


}
