package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.activeconfigs.VLinkedListActiveConfig;
import com.balazskreith.vcollections.adapters.SerDe;
import com.balazskreith.vcollections.builders.fieldadapters.ObjectMaker;
import com.balazskreith.vcollections.builders.passiveconfigs.VLinkedListPassiveConfig;
import com.balazskreith.vcollections.storages.MemoryStorage;
import com.balazskreith.vcollections.storages.SerializationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

/**
 * Represents a builder responsible for building a {@link MemoryStorage}.
 */
public class VLinkedListConfigAdapter<K, V> extends AbstractConfigAdapter<VLinkedListPassiveConfig, VLinkedListActiveConfig<K, V>> {
	private final ObjectMapper mapper;

	public VLinkedListConfigAdapter(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public VLinkedListConfigAdapter() {
		this(new ObjectMapper());
	}

	@Override
	protected VLinkedListActiveConfig<K, V> doConvert(VLinkedListPassiveConfig source) {
		LinkedListStorageBuilder linkedListStorageBuilder = new LinkedListStorageBuilder(mapper).withConfiguration(source.storage);
		K head = null;
		K tail = null;
		SerDe<K> keySerDe = null;
		if (source.keySerDe != null) {
			keySerDe = new ObjectMaker<SerDe<K>>().convert(source.keySerDe);
		}
		if (source.head != null || source.tail != null) {
			if (source.head == null || source.tail == null) {
				throw new IllegalStateException("Head and Tail are required to be provided.");
			}
			if (keySerDe == null) {
				throw new IllegalStateException("If head and tail are provided, keySerDe must be provided too");
			}
			try {
				head = keySerDe.deserialize(source.head.getBytes());
				tail = keySerDe.deserialize(source.tail.getBytes());
			} catch (IOException e) {
				throw new SerializationException(e);
			}
		}


		VLinkedListActiveConfig<K, V> result = new VLinkedListActiveConfig<>(
				linkedListStorageBuilder,
				keySerDe,
				head,
				tail
		);
		return result;
	}


	@Override
	protected VLinkedListPassiveConfig doDeConvert(VLinkedListActiveConfig<K, V> source) {
//		VLinkedListPassiveConfig result = new VLinkedListPassiveConfig();
//		result.storage = source.storageBuilder.getConfigurations();
//		if (source.storageBuilder.)
//		result.tail = 
//				this.setupAbstractStoragePassiveConfig(result, source);
//		result.keysStorage = source.keysStorageBuilder.getConfigurations();
//		result.valuesStorage = source.valuesStorageBuilder.getConfigurations();
//		return result;
		return null;
	}

}
