package com.balazskreith.vcollections.activeconfigs;

import com.balazskreith.vcollections.adapters.SerDe;
import com.balazskreith.vcollections.builders.StorageBuilder;
import java.util.List;
import java.util.function.Supplier;

public class ReplicatedStoragesActiveConfig<K, V> extends AbstractStorageActiveConfig<K, V> {

	public final List<StorageBuilder> storageBuilders;

	public ReplicatedStoragesActiveConfig(long capacity,
										  Supplier<K> keySupplier,
										  SerDe<V> valueSerDe,
										  List<StorageBuilder> storageBuilders
	) {
		super(capacity, keySupplier, valueSerDe);
		this.storageBuilders = storageBuilders;
	}

	public ReplicatedStoragesActiveConfig(AbstractStorageActiveConfig<K, V> storageConfig,
										  List<StorageBuilder> storageBuilders
	) {
		this(storageConfig.capacity,
				storageConfig.keySupplier,
				storageConfig.valueSerDe,
				storageBuilders
		);
	}
}
