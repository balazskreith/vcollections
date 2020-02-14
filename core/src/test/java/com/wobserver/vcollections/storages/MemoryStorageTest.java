package com.wobserver.vcollections.storages;

import java.util.HashMap;
import java.util.Map;
import com.wobserver.vcollections.keygenerators.KeyGeneratorFactory;

/**
 * Test a storage for general capabilities according to the IStorage interface
 * The target storage needs to inherit this one and overwrite the makeStorage protected methods to test the actual storage
 */

public class MemoryStorageTest implements StorageTest<MemoryStorage<String, String>> {

	@Override
	public IStorage<String, String> makeStorage(long maxSize, String... items) {
		Map<String, String> pairs = new HashMap<>();
		if (items != null) {
			for (int i = 0; i + 1 < items.length; i += 2) {
				String key = items[i];
				String value = items[i + 1];
				pairs.put(key, value);
			}
		}
		IStorage<String, String> result = new MemoryStorage<>(new KeyGeneratorFactory().make(String.class), pairs, maxSize);
		return result;
	}

}
