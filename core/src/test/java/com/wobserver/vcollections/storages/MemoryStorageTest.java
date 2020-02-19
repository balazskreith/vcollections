package com.wobserver.vcollections.storages;

import com.wobserver.vcollections.keygenerators.KeyGeneratorFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Test a storage for general capabilities according to the IStorage interface
 * The target storage needs to inherit this one and overwrite the makeStorage protected methods to test the actual storage
 */

public class MemoryStorageTest implements StorageTest<String, String, MemoryStorage<String, String>> {

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
		IStorage<String, String> result = new MemoryStorage<>(new KeyGeneratorFactory().make(String.class), pairs, maxSize);
		return result;
	}
}
