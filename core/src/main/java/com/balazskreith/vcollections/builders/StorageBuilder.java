package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.adapters.SerDe;
import com.balazskreith.vcollections.storages.IStorage;
import java.util.Map;
import java.util.function.Supplier;

public interface StorageBuilder {

	<K, V> IStorage<K, V> build();

	StorageBuilder withConfiguration(Map<String, Object> configuration);

	Map<String, Object> getConfigurations();

	<T> T getConfiguration(String key);

	<T> StorageBuilder withValueSerDe(SerDe<T> valueSerDe);

	StorageBuilder withValueSerDe(String klassName);

	<T> SerDe<T> getValueSerDe();

	StorageBuilder withCapacity(long capacity);

	<T> StorageBuilder withKeySupplier(Supplier<T> keySupplier);

	StorageBuilder withKeySupplier(String klassName);
	
	
}
