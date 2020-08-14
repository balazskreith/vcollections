package com.balazskreith.vcollections.activeconfigs;

import com.balazskreith.vcollections.adapters.SerDe;
import com.balazskreith.vcollections.builders.LinkedListStorageBuilder;

public class VLinkedListActiveConfig<K, V> {

	public final LinkedListStorageBuilder storageBuilder;
	public final SerDe<K> serDe;
	public final K head;
	public final K tail;

	public VLinkedListActiveConfig(
			LinkedListStorageBuilder storageBuilder,
			SerDe<K> serDe,
			K head,
			K tail
	) {
		this.storageBuilder = storageBuilder;
		this.serDe = serDe;
		this.head = head;
		this.tail = tail;
	}
}
