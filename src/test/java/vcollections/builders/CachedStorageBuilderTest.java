package vcollections.builders;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import vcollections.storages.IStorage;

class CachedStorageBuilderTest extends AbstractBuilderTest {

	public static Map<String, Object> createConfig(String keyType, Long subsetCapacity) {
		Map<String, Object> result = new HashMap<>();
		if (keyType != null) {
			result.put(CachedStorageBuilder.KEY_TYPE_CONFIG_KEY, keyType);
		}
		result.put(CachedStorageBuilder.SUBSET_CONFIG_KEY, AbstractBuilderTest.makeMap(
				StorageBuilder.BUILDER_CONFIG_KEY, "memoryStorage",
				StorageBuilder.CONFIGURATION_CONFIG_KEY, MemoryStorageBuilderTest.createConfig(null, subsetCapacity)
		));
		result.put(CachedStorageBuilder.SUPERSET_CONFIG_KEY, AbstractBuilderTest.makeMap(
				StorageBuilder.BUILDER_CONFIG_KEY, "memoryStorage"
		));
		return result;
	}

	@Test
	public void t() {
		AtomicBoolean v = new AtomicBoolean(false);
		t(v);
		System.out.println(v.get());
	}

	private void t(AtomicBoolean v) {
		v.set(true);
	}

	/**
	 * <b>Given</b>: A well formatted Map for limited capacity memory storage
	 *
	 * <b>When</b>: a build method is called
	 *
	 * <b>Then</b>: the result is a memory storage with the limited capacity
	 */
	@Test
	public void shouldBuildTheStorage() {
		// Given
		Long subsetCapacity = 3L;
		Map<String, Object> configuration = createConfig("java.lang.String", subsetCapacity);
		IStorageBuilder storageBuilder = new CachedStorageBuilder<>();

		// When
		IStorage<UUID, String> storage = storageBuilder
				.withConfiguration(configuration)
				.build();

		// Then
//		assertEquals(capacity, storage.capacity());
//		assertNotNull(storage.create("value"));
	}
}