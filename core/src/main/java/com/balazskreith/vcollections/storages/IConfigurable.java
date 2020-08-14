package com.balazskreith.vcollections.storages;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Noone should be configurable!
 * @param <K>
 * @param <V>
 */
@Deprecated()
public interface IConfigurable<K, V> {

	void setKeyGenerator(Supplier<K> value);

	void setCapacity(Long capacity);

	void setEntries(Map<K, V> items);

}
