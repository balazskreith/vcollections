package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.activeconfigs.MemoryStorageActiveConfig;
import com.balazskreith.vcollections.builders.fieldadapters.ObjectMaker;
import com.balazskreith.vcollections.builders.passiveconfigs.MemoryStoragePassiveConfig;
import com.balazskreith.vcollections.storages.MemoryStorage;
import java.util.function.Supplier;

/**
 * Represents a builder responsible for building a {@link MemoryStorage}.
 */
public class MemoryStorageConfigAdapter<K, V> extends AbstractStorageConfigAdapter<K, V, MemoryStoragePassiveConfig, MemoryStorageActiveConfig<K, V>> {
	private final ObjectMaker<Supplier<K>> keySupplierAdapter = new ObjectMaker<Supplier<K>>();

	public MemoryStorageConfigAdapter() {
	}

	@Override
	protected MemoryStorageActiveConfig<K, V> doConvert(MemoryStoragePassiveConfig source) {
		long capacity = source.capacity;
		Supplier<K> keySupplier = this.keySupplierAdapter.convert(source.keySupplier);
		MemoryStorageActiveConfig result = new MemoryStorageActiveConfig(capacity, keySupplier);
		return result;
	}

	@Override
	protected MemoryStoragePassiveConfig doDeConvert(MemoryStorageActiveConfig<K, V> source) {
		MemoryStoragePassiveConfig result = new MemoryStoragePassiveConfig();
		this.setupAbstractStoragePassiveConfig(result, source);
		return result;
	}
}
