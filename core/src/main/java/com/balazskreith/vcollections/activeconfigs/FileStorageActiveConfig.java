package com.balazskreith.vcollections.activeconfigs;

import com.balazskreith.vcollections.adapters.Adapter;
import com.balazskreith.vcollections.adapters.SerDe;
import java.nio.file.Path;
import java.util.function.Supplier;

public class FileStorageActiveConfig<K, V> extends AbstractStorageActiveConfig<K, V> {

	public final Adapter<K, String> keyAdapter;

	public final SerDe<V> valueSerde;

	public final Path path;

	public FileStorageActiveConfig(long capacity,
								   Supplier<K> keySupplier,
								   SerDe<V> valueSerde,
								   Adapter<K, String> keyAdapter,
								   Path path) {
		super(capacity, keySupplier, valueSerde);
		this.keyAdapter = keyAdapter;
		this.valueSerde = valueSerde;
		this.path = path;
	}

	public FileStorageActiveConfig(AbstractStorageActiveConfig<K, V> storageConfig,
								   Adapter<K, String> keyAdapter,
								   Path path) {
		this(storageConfig.capacity,
				storageConfig.keySupplier,
				storageConfig.valueSerDe,
				keyAdapter,
				path
		);
	}

}
