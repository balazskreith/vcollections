package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.activeconfigs.AbstractStorageActiveConfig;
import com.balazskreith.vcollections.activeconfigs.ClusteredStoragesActiveConfig;
import com.balazskreith.vcollections.builders.passiveconfigs.ClusteredStoragesPassiveConfig;
import com.balazskreith.vcollections.storages.MemoryStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a builder responsible for building a {@link MemoryStorage}.
 */
public class ClusteredStoragesConfigAdapter<K, V> extends AbstractStorageConfigAdapter<K, V, ClusteredStoragesPassiveConfig, ClusteredStoragesActiveConfig<K, V>> {
	private final ObjectMapper mapper;

	public ClusteredStoragesConfigAdapter(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public ClusteredStoragesConfigAdapter() {
		this(new ObjectMapper());
	}

	@Override
	protected ClusteredStoragesActiveConfig<K, V> doConvert(ClusteredStoragesPassiveConfig source) {
		List<StorageBuilder> storageBuilders =
				source.storages.stream()
						.map(config -> new AnyStorageBuilder(mapper).withConfiguration(config))
						.collect(Collectors.toList());
		AbstractStorageActiveConfig<K, V> abstractStorageActiveConfig = this.getAbstractStorageActiveConfig(source);
		ClusteredStoragesActiveConfig<K, V> result = new ClusteredStoragesActiveConfig<>(
				abstractStorageActiveConfig,
				storageBuilders);
		return result;
	}

	@Override
	protected ClusteredStoragesPassiveConfig doDeConvert(ClusteredStoragesActiveConfig<K, V> source) {
		ClusteredStoragesPassiveConfig result = new ClusteredStoragesPassiveConfig();
		this.setupAbstractStoragePassiveConfig(result, source);
		result.storages = source.storageBuilders.stream().map(StorageBuilder::getConfigurations).collect(Collectors.toList());
		return result;
	}

}
