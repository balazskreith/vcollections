package com.balazskreith.vcollections.activeconfigs;

import com.balazskreith.vcollections.adapters.SerDe;
import java.util.function.Supplier;

public class LRUMemoryStorageActiveConfig<K, V> extends AbstractStorageActiveConfig<K, V> {

	public final int retentionInMs;
	public final Supplier<Long> timeInMsProvider;

	public LRUMemoryStorageActiveConfig(long capacity,
										Supplier<K> keySupplier,
										SerDe<V> valueSerDe,
										int retentionInMs,
										Supplier<Long> timeInMsProvider) {
		super(capacity, keySupplier, valueSerDe);
		this.retentionInMs = retentionInMs;
		this.timeInMsProvider = timeInMsProvider;
	}

	public LRUMemoryStorageActiveConfig(AbstractStorageActiveConfig<K, V> storageConfig,
										int retentionInMs,
										Supplier<Long> timeInMsProvider) {
		this(storageConfig.capacity,
				storageConfig.keySupplier,
				storageConfig.valueSerDe,
				retentionInMs,
				timeInMsProvider
		);
	}

}
