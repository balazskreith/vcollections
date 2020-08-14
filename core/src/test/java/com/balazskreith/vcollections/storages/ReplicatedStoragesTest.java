package com.balazskreith.vcollections.storages;

import com.balazskreith.vcollections.keygenerators.KeyGeneratorFactory;
import java.util.HashMap;
import java.util.Map;

class ReplicatedStoragesTest implements StorageTest<String, String, ReplicatedStorages<String, String>> {

	@Override
	public String toKey(String key) {
		return key;
	}

	@Override
	public String toValue(String value) {
		return value;
	}

	@Override
	public IStorage<String, String> makeStorage(long maxSize, Map.Entry<String, String>... entries) {
		Map<String, String> pairs = new HashMap<>();
		if (entries != null) {
			for (Map.Entry<String, String> entry : entries) {
				pairs.put(entry.getKey(), entry.getValue());
			}
		}
		IStorage<String, String> result =
				new ReplicatedStorages<>(pairs, maxSize,
						new KeyGeneratorFactory().make(String.class),
						new MemoryStorage<String, String>(null, null, IStorage.NO_MAX_SIZE),
						new MemoryStorage<String, String>(null, null, IStorage.NO_MAX_SIZE)
				);

		return result;
	}
}