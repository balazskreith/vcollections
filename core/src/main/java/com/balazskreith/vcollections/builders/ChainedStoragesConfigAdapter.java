package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.activeconfigs.AbstractStorageActiveConfig;
import com.balazskreith.vcollections.activeconfigs.ChainedStoragesActiveConfig;
import com.balazskreith.vcollections.builders.passiveconfigs.ChainedStoragesPassiveConfig;
import com.balazskreith.vcollections.storages.MemoryStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a builder responsible for building a {@link MemoryStorage}.
 */
public class ChainedStoragesConfigAdapter<K, V> extends AbstractStorageConfigAdapter<K, V, ChainedStoragesPassiveConfig, ChainedStoragesActiveConfig<K, V>> {
	private final ObjectMapper mapper;

	public ChainedStoragesConfigAdapter(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public ChainedStoragesConfigAdapter() {
		this(new ObjectMapper());
	}

	@Override
	protected ChainedStoragesActiveConfig<K, V> doConvert(ChainedStoragesPassiveConfig source) {
		AbstractStorageActiveConfig<K, V> abstractStorageActiveConfig = this.getAbstractStorageActiveConfig(source);
		List<StorageBuilder> storageBuilders =
				source.storages.stream()
						.map(config -> new AnyStorageBuilder(mapper).withConfiguration(config))
						.collect(Collectors.toList());
		ChainedStoragesActiveConfig<K, V> result = new ChainedStoragesActiveConfig<>(
				abstractStorageActiveConfig,
				storageBuilders);
		return result;
	}

	@Override
	protected ChainedStoragesPassiveConfig doDeConvert(ChainedStoragesActiveConfig<K, V> source) {
		ChainedStoragesPassiveConfig result = new ChainedStoragesPassiveConfig();
		this.setupAbstractStoragePassiveConfig(result, source);
		result.storages = source.storageBuilders.stream().map(StorageBuilder::getConfigurations).collect(Collectors.toList());
		return result;
	}

}
