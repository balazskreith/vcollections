package com.wobserver.vcollections.storages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wobserver.vcollections.keygenerators.KeyGeneratorFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.io.TempDir;

/**
 * Test a storage for general capabilities according to the IStorage interface
 * The target storage needs to inherit this one and overwrite the makeStorage protected methods to test the actual storage
 */

public class FileStorageTest implements StorageTest<FileStorage<String, String>> {

	@TempDir
	File tempDir;

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
		IStorage<String, String> result = null;
		try {
			result = new FileStorage<>(SimpleMapperFactory.make(String.class, String.class), String.class, new ObjectMapper(), tempDir.getPath(), new KeyGeneratorFactory().make(String.class), maxSize);
			IStorage<String, String> finalResult = result;
			pairs.entrySet().forEach(entry -> finalResult.update(entry.getKey(), entry.getValue()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

}
