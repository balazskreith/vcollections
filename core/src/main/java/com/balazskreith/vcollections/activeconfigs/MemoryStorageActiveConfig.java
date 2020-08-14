package com.balazskreith.vcollections.activeconfigs;

import java.util.function.Supplier;

public class MemoryStorageActiveConfig<K, V> extends AbstractStorageActiveConfig<K, V> {

	public MemoryStorageActiveConfig(long capacity, Supplier<K> keySupplier) {
		super(capacity, keySupplier, null);
	}


}
