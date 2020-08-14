package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.activeconfigs.AbstractStorageActiveConfig;
import com.balazskreith.vcollections.activeconfigs.ReplicatedStoragesActiveConfig;
import com.balazskreith.vcollections.builders.passiveconfigs.ReplicatedStoragesPassiveConfig;
import com.balazskreith.vcollections.storages.MemoryStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a builder responsible for building a {@link MemoryStorage}.
 */
public class ReplicatedStoragesConfigAdapter<K, V> extends AbstractStorageConfigAdapter<K, V, ReplicatedStoragesPassiveConfig, ReplicatedStoragesActiveConfig<K, V>> {
	private final ObjectMapper mapper;

	public ReplicatedStoragesConfigAdapter(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public ReplicatedStoragesConfigAdapter() {
		this(new ObjectMapper());
	}

	@Override
	protected ReplicatedStoragesActiveConfig<K, V> doConvert(ReplicatedStoragesPassiveConfig source) {
		List<StorageBuilder> storageBuilders =
				source.storages.stream()
						.map(config -> new AnyStorageBuilder(mapper).withConfiguration(config))
						.collect(Collectors.toList());
		AbstractStorageActiveConfig<K, V> abstractStorageActiveConfig = this.getAbstractStorageActiveConfig(source);
		ReplicatedStoragesActiveConfig<K, V> result = new ReplicatedStoragesActiveConfig<>(
				abstractStorageActiveConfig,
				storageBuilders);
		return result;
	}

	@Override
	protected ReplicatedStoragesPassiveConfig doDeConvert(ReplicatedStoragesActiveConfig<K, V> source) {
		ReplicatedStoragesPassiveConfig result = new ReplicatedStoragesPassiveConfig();
		this.setupAbstractStoragePassiveConfig(result, source);
		result.storages = source.storageBuilders.stream().map(StorageBuilder::getConfigurations).collect(Collectors.toList());
		return result;
	}

}
