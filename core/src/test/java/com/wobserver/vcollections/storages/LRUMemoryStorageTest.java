package com.wobserver.vcollections.storages;

import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import com.wobserver.vcollections.keygenerators.KeyGeneratorFactory;

class LRUMemoryStorageTest implements StorageTest<LRUMemoryStorage<String, String>> {

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
		LRUMemoryStorage<String, String> result = new LRUMemoryStorage<>(maxSize);
		pairs.forEach(result::update);
		result.setKeyGenerator(new KeyGeneratorFactory().make(String.class));
		return result;
	}

	@Override
	public void shouldThrowOutOfSpaceExceptionAfterCreate() {
		// This test for this storage is not valied
	}

	@Override
	public void shouldThrowOutOfSpaceExceptionAfterUpdate() {
		// This test for this storage is not valied
	}

	/**
	 * <b>Given</b>: an LRU memory storage with limited capacity, fully enlisted
	 *
	 * <b>When</b>: we retrieve some keys, and putt a new one
	 *
	 * <b>Then</b>: the not used one will go away
	 */
	@Test
	public void shouldRemoveNotUsedItems() {
		// Given
		IStorage<String, String> storage = makeStorage(2, "key1", "value1", "key2", "value2");

		// When
		storage.read("key2");
		storage.update("key3", "value3");

		// Then
		assertFalse(storage.has("key1"));
	}

	/**
	 * <b>Given</b>: an empty lru memopy storage with retention time
	 *
	 * <b>When</b>: we add an item
	 * <b>and</b> wait for more than the retention time
	 *
	 * <b>Then</b>: the added item is not found in the storage
	 */
	@Test
	public void shouldRemoveOldItems() throws InterruptedException {
		// Given
		IStorage<String, String> storage = new LRUMemoryStorage<>(100);

		// When
		storage.update("key", "value");
		Thread.sleep(200);

		// Then
		assertFalse(storage.has("key"));
	}
}