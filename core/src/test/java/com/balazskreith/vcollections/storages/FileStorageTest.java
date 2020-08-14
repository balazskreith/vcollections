package com.balazskreith.vcollections.storages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.balazskreith.vcollections.keygenerators.KeyGeneratorFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test a storage for general capabilities according to the IStorage interface
 * The target storage needs to inherit this one and overwrite the makeStorage protected methods to test the actual storage
 */

public class FileStorageTest implements StorageTest<String, String, FileStorage<String, String>> {

	@TempDir
	File tempDir;

	@Override
	public String toKey(String key) {
		if (key == null) {
			return "null";
		}
		return key;
	}

	@Override
	public String toValue(String value) {
		if (value == null) {
			return "null";
		}
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
		IStorage<String, String> result = null;
		try {
			result = new FileStorage<>(DefaultMapperFactory.make(String.class, String.class), String.class, new ObjectMapper(), tempDir.getPath(), new KeyGeneratorFactory().make(String.class), maxSize);
			IStorage<String, String> finalResult = result;
			pairs.entrySet().forEach(entry -> finalResult.update(entry.getKey(), entry.getValue()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
