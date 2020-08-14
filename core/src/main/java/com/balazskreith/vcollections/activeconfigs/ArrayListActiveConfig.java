package com.balazskreith.vcollections.activeconfigs;

import com.balazskreith.vcollections.adapters.Adapter;
import com.balazskreith.vcollections.builders.StorageBuilder;

public class ArrayListActiveConfig<K, V> {

	public final StorageBuilder storageBuilder;

	public final Adapter<K, Long> keyAdapter;

	public ArrayListActiveConfig(
			StorageBuilder storageBuilder,
			Adapter<K, Long> keyAdapter
	) {
		this.storageBuilder = storageBuilder;
		this.keyAdapter = keyAdapter;
	}
}
