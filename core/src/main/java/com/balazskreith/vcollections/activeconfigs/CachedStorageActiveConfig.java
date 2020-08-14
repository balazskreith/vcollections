package com.balazskreith.vcollections.activeconfigs;

import com.balazskreith.vcollections.adapters.SerDe;
import com.balazskreith.vcollections.builders.StorageBuilder;
import java.util.function.Supplier;

public class CachedStorageActiveConfig<K, V> extends AbstractStorageActiveConfig<K, V> {

	public final StorageBuilder superset;

	public final StorageBuilder subset;

	public final boolean cacheOnCreate;

	public final boolean cacheOnRead;

	public final boolean cacheOnUpdate;

	public CachedStorageActiveConfig(long capacity,
									 Supplier<K> keySupplier,
									 SerDe<V> valueSerDe,
									 StorageBuilder superset,
									 StorageBuilder subset,
									 boolean cacheOnCreate,
									 boolean cacheOnRead,
									 boolean cacheOnUpdate
	) {
		super(capacity, keySupplier, valueSerDe);
		this.superset = superset;
		this.subset = subset;
		this.cacheOnCreate = cacheOnCreate;
		this.cacheOnRead = cacheOnRead;
		this.cacheOnUpdate = cacheOnUpdate;
	}

	public CachedStorageActiveConfig(AbstractStorageActiveConfig<K, V> storageConfig,
									 StorageBuilder superset,
									 StorageBuilder subset,
									 boolean cacheOnCreate,
									 boolean cacheOnRead,
									 boolean cacheOnUpdate
	) {
		this(storageConfig.capacity,
				storageConfig.keySupplier,
				storageConfig.valueSerDe,
				superset,
				subset,
				cacheOnCreate,
				cacheOnRead,
				cacheOnUpdate
		);
	}
}
