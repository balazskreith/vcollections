package com.wobserver.vcollections.builders;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.wobserver.vcollections.storages.IStorage;

class MemoryStorageBuilderTest extends AbstractBuilderTest {

	public static Map<String, Object> createConfig(String keyGenerator, Long capacity) {
		return createConfig(keyGenerator, capacity, false, 0, 0);
	}

	public static Map<String, Object> createConfig(String keyGenerator, Long capacity, boolean storageTest, int minSize, int maxSize) {
		Map<String, Object> result = new HashMap<>();
		if (capacity != null) {
			result.put(MemoryStorageBuilder.CAPACITY_CONFIG_KEY, capacity);
		}
		if (keyGenerator != null) {
			result.put(KeyGeneratorBuilder.KEY_GENERATOR_CONFIG_KEY,
					Map.of(KeyGeneratorBuilder.CLASS_CONFIG_KEY, keyGenerator,
							KeyGeneratorBuilder.STORAGE_TEST_CONFIG_KEY, storageTest,
							KeyGeneratorBuilder.MIN_KEY_SIZE_CONFIG_KEY, minSize,
							KeyGeneratorBuilder.MAX_KEY_SIZE_CONFIG_KEY, maxSize));
		}
		return result;
	}

	/**
	 * <b>Given</b>: A well formatted Config for limited capacity memory storage, and for keygeneration
	 *
	 * <b>When</b>: a build method is called
	 *
	 * <b>Then</b>: the result is a memory storage with the limited capacity
	 */
	@Test
	public void shouldBuildTheStorageWithFullConfigurations() {
		// Given
		Long capacity = 3L;
		Map<String, Object> configuration = createConfig("java.util.UUID", capacity);
		IStorageBuilder storageBuilder = new MemoryStorageBuilder();

		// When
		IStorage<UUID, String> storage = storageBuilder
				.withConfiguration(configuration)
				.build();

		// Then
		assertEquals(capacity, storage.capacity());
		assertNotNull(storage.create("value"));
	}

	/**
	 * <b>Given</b>: A well formatted Config for limited capacity memory storage, without key generator
	 *
	 * <b>When</b>: a build method is called
	 *
	 * <b>Then</b>: the result is a memory storage with the limited capacity, but without key generator
	 */
	@Test
	public void shouldBuildTheStorageWithPartialConfigurations_1() {
		// Given
		Long capacity = 3L;
		Map<String, Object> configuration = createConfig(null, capacity);
		IStorageBuilder storageBuilder = new MemoryStorageBuilder();

		// When
		IStorage<UUID, String> storage = storageBuilder
				.withConfiguration(configuration)
				.build();

		// Then
		assertEquals(capacity, storage.capacity());
		assertThrows(NullPointerException.class, () -> storage.create("value"));
	}

	/**
	 * <b>Given</b>: A well formatted Config for key generator, without limitation
	 *
	 * <b>When</b>: a build method is called
	 *
	 * <b>Then</b>: the result is a memory storage without capacity, but with key generator
	 */
	@Test
	public void shouldBuildTheStorageWithPartialConfigurations_2() {
		// Given
		Map<String, Object> configuration = createConfig("java.util.UUID", null);
		IStorageBuilder storageBuilder = new MemoryStorageBuilder();

		// When
		IStorage<UUID, String> storage = storageBuilder
				.withConfiguration(configuration)
				.build();

		// Then
		assertEquals(IStorage.NO_MAX_SIZE, storage.capacity());
		assertNotNull(storage.create("value"));
	}

	/**
	 * <b>Given</b>: A created storagebuilder without any conffiguration
	 *
	 * <b>When</b>: a build method is called
	 *
	 * <b>Then</b>: the result is a memory storage is without capacity and key generator
	 */
	@Test
	public void shouldBuildTheStorageWithoutConfigurations() {
		// Given
		IStorageBuilder storageBuilder = new MemoryStorageBuilder();

		// When
		IStorage<UUID, String> storage = storageBuilder
				.build();

		// Then
		assertEquals(IStorage.NO_MAX_SIZE, storage.capacity());
		assertThrows(NullPointerException.class, () -> storage.create("value"));
	}
}