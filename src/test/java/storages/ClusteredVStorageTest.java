package storages;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ClusteredVStorageTest implements StorageTest<ClusteredVStorage<String, String>> {

	private IStorage<String, String> makeStorage(IStorage<String, String> storage1, IStorage<String, String> storage2, Map<String, String> initialEntries, long maxSize) {
		IStorage<String, String> result = new ClusteredVStorage<String, String>(initialEntries, maxSize, SimpleKeyGeneratorFactory.make(String.class), storage1, storage2);
		return result;
	}

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
		IStorage<String, String> result = makeStorage(
				new MemoryStorage<String, String>(),
				new MemoryStorage<String, String>(),
				pairs,
				maxSize
		);

		return result;
	}

	/**
	 * <p>Given</p>: two storages, with limited capacity
	 *
	 * <p>When</p>: We try to construct a chain storage
	 *
	 * <p>Then</p> it throws {@link IllegalStateException}
	 */
	@Test
	public void shouldNotCreateForTwoUnlimitedStorages() {
		// Given
		IStorage<String, String> storage1 = new MemoryStorage<String, String>(1L);
		IStorage<String, String> storage2 = new MemoryStorage<String, String>(1L);

		// When
		Assertions.assertThrows(IllegalStateException.class, () -> {
			IStorage<String, String> chainStorage = new ClusteredVStorage<>(SimpleKeyGeneratorFactory.make(String.class), storage1, storage2);
		});
	}

	/**
	 * <p>Given</p>: two storages, with inf capacities
	 *
	 * <p>When</p>: We try to construct a chain storage, with 0 capacity
	 *
	 * <p>Then</p> it throws {@link IllegalStateException}
	 */
	@Test
	public void shouldNotCreateIfTheFirstStorageIsUnlimited() {
		// Given
		IStorage<String, String> storage1 = new MemoryStorage<String, String>();
		IStorage<String, String> storage2 = new MemoryStorage<String, String>();

		// When
		Assertions.assertThrows(IllegalStateException.class, () -> {
			IStorage<String, String> chainStorage = new ClusteredVStorage<>(0L, SimpleKeyGeneratorFactory.make(String.class), storage1, storage2);
		});
	}

	/**
	 * <p>Given</p>: no storages
	 *
	 * <p>When</p>: We try to construct a chain storage
	 *
	 * <p>Then</p> it throws {@link NotAvailableStorage}
	 */
	@Test
	public void shouldNotCreateWithoutStorage() {
		// Given

		// When
		Assertions.assertThrows(NotAvailableStorage.class, () -> {
			IStorage<String, String> chainStorage = new ClusteredVStorage<>(SimpleKeyGeneratorFactory.make(String.class));
		});
	}

}