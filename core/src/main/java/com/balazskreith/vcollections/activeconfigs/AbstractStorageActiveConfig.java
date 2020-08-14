package com.balazskreith.vcollections.activeconfigs;

import com.balazskreith.vcollections.adapters.SerDe;
import java.util.function.Supplier;

public class AbstractStorageActiveConfig<K, V> {

	public final long capacity;

	public final Supplier<K> keySupplier;

	public final SerDe<V> valueSerDe;


	public AbstractStorageActiveConfig(long capacity, Supplier<K> keySupplier, SerDe<V> valueSerDe) {
		this.capacity = capacity;
		this.keySupplier = keySupplier;
		this.valueSerDe = valueSerDe;
	}

	/**
	 * Copy constructor
	 *
	 * @param source
	 */
	public AbstractStorageActiveConfig(AbstractStorageActiveConfig<K, V> source) {
		this(source.capacity, source.keySupplier, source.valueSerDe);
	}
}
