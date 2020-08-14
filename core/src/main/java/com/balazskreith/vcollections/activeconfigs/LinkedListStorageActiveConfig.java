package com.balazskreith.vcollections.activeconfigs;

import com.balazskreith.vcollections.builders.StorageBuilder;

public class LinkedListStorageActiveConfig<K, V> extends AbstractStorageActiveConfig<K, V> {

	public final StorageBuilder keysStorageBuilder;
	public final StorageBuilder valuesStorageBuilder;
	
	public LinkedListStorageActiveConfig(
			AbstractStorageActiveConfig abstractStorageActiveConfig,
			StorageBuilder keysStorageBuilder,
			StorageBuilder valuesStorageBuilder

	) {
		super(abstractStorageActiveConfig);
		this.keysStorageBuilder = keysStorageBuilder;
		this.valuesStorageBuilder = valuesStorageBuilder;
	}
}
