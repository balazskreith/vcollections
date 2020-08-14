package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.activeconfigs.AbstractStorageActiveConfig;
import com.balazskreith.vcollections.activeconfigs.CachedStorageActiveConfig;
import com.balazskreith.vcollections.builders.passiveconfigs.CachedStoragePassiveConfig;
import com.balazskreith.vcollections.storages.MemoryStorage;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Represents a builder responsible for building a {@link MemoryStorage}.
 */
public class CachedStorageConfigAdapter<K, V> extends AbstractStorageConfigAdapter<K, V, CachedStoragePassiveConfig,
		CachedStorageActiveConfig<K,
				V>> {
	private final ObjectMapper mapper;

	public CachedStorageConfigAdapter(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public CachedStorageConfigAdapter() {
		this(new ObjectMapper());
	}

	@Override
	protected CachedStorageActiveConfig<K, V> doConvert(CachedStoragePassiveConfig source) {
		AbstractStorageActiveConfig<K, V> abstractStorageActiveConfig = this.getAbstractStorageActiveConfig(source);
		StorageBuilder supersetBuilder = new AnyStorageBuilder(this.mapper).withConfiguration(source.superset);
		StorageBuilder subsetBuilder = new AnyStorageBuilder(this.mapper).withConfiguration(source.subset);
		CachedStorageActiveConfig<K, V> result = new CachedStorageActiveConfig<>(
				abstractStorageActiveConfig,
				supersetBuilder,
				subsetBuilder,
				source.cacheOnCreate,
				source.cacheOnRead,
				source.cacheOnUpdate
		);
		return result;
	}

	@Override
	protected CachedStoragePassiveConfig doDeConvert(CachedStorageActiveConfig<K, V> source) {
		CachedStoragePassiveConfig result = new CachedStoragePassiveConfig();
		this.setupAbstractStoragePassiveConfig(result, source);
		result.cacheOnCreate = source.cacheOnCreate;
		result.cacheOnRead = source.cacheOnRead;
		result.cacheOnUpdate = source.cacheOnUpdate;
		result.subset = source.subset.getConfigurations();
		result.superset = source.superset.getConfigurations();
		return null;
	}

}
