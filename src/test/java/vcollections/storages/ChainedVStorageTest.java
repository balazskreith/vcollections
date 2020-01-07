package vcollections.storages;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import vcollections.keygenerators.KeyGeneratorFactory;

/**
 * Test a storage for general capabilities according to the IStorage interface
 * The target storage needs to inherit this one and overwrite the makeStorage protected methods to test the actual storage
 */

public class ChainedVStorageTest implements StorageTest<ChainedVStorage<String, String>> {

	private IStorage<String, String> makeStorage(IStorage<String, String> storage1, IStorage<String, String> storage2, Map<String, String> initialEntries, long maxSize) {
		IStorage<String, String> result = new ChainedVStorage<>(initialEntries, maxSize, new KeyGeneratorFactory().make(String.class), storage1, storage2);
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
				new MemoryStorage<String, String>(null, null, 2L),
				new MemoryStorage<String, String>(null, null, IStorage.NO_MAX_SIZE),
				pairs,
				maxSize
		);

		return result;
	}


	/**
	 * <p>Given</p>: two vcollections.storages, with unlimited capacity
	 *
	 * <p>When</p>: We try to construct a chain storage
	 *
	 * <p>Then</p> it throws {@link IllegalStateException}
	 */
	@Test
	public void shouldNotCreateForTwoUnlimitedStorages() {
		// Given
		IStorage<String, String> storage1 = new MemoryStorage<String, String>(null, null, null);
		IStorage<String, String> storage2 = new MemoryStorage<String, String>(null, null, null);

		// When
		Assertions.assertThrows(IllegalStateException.class, () -> {
			IStorage<String, String> chainStorage = new ChainedVStorage<>(new KeyGeneratorFactory().make(String.class), storage1, storage2);
		});
	}

	/**
	 * <p>Given</p>: two vcollections.storages, with (inf, 1) capacities
	 *
	 * <p>When</p>: We try to construct a chain storage
	 *
	 * <p>Then</p> it throws {@link IllegalStateException}
	 */
	@Test
	public void shouldNotCreateIfTheFirstStorageIsUnlimited() {
		// Given
		IStorage<String, String> storage1 = new MemoryStorage<String, String>(null, null, null);
		IStorage<String, String> storage2 = new MemoryStorage<String, String>(null, null, 1L);

		// When
		Assertions.assertThrows(IllegalStateException.class, () -> {
			IStorage<String, String> chainStorage = new ChainedVStorage<>(new KeyGeneratorFactory().make(String.class), storage1, storage2);
		});
	}

	/**
	 * <p>Given</p>: no vcollections.storages
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
			IStorage<String, String> chainStorage = new ChainedVStorage<>(new KeyGeneratorFactory().make(String.class));
		});
	}

	/**
	 * <p>Given</p>: storage with a capacity of 0
	 *
	 * <p>When</p>: We try to construct a chain storage
	 *
	 * <p>Then</p> it throws {@link IllegalStateException}
	 */
	@Test
	public void shouldNotCreateWithZeroStorage() {
		// Given
		IStorage<String, String> storage = new MemoryStorage<String, String>(new KeyGeneratorFactory().make(String.class), null, 0L);

		// When
		Assertions.assertThrows(IllegalStateException.class, () -> {
			IStorage<String, String> chainStorage = new ChainedVStorage<>(new KeyGeneratorFactory().make(String.class), storage);
		});
	}

	/**
	 * <p>Given</p>: an empty chain storage with underlying storage capacities of (10, inf)
	 *
	 * <p>When</p>: we call create with "value" and save the generated key
	 *
	 * <p>Then</p> storage1 has key
	 * <p>Then</p> storage2 does not have key
	 */
	@Test
	public void shouldCreateValues() {
		// Given
		IStorage<String, String> storage1 = new MemoryStorage<String, String>(null, null, 10L);
		IStorage<String, String> storage2 = new MemoryStorage<String, String>(null, null, null);
		IStorage<String, String> chainStorage = new ChainedVStorage<>(new KeyGeneratorFactory().make(String.class), storage1, storage2);

		// When
		String key = chainStorage.create("value");

		// Then
		assertTrue(storage1.has(key));
		assertFalse(storage2.has(key));
	}


	/**
	 * <p>Given</p>: a chain storage with underlying storage capacities of (2, inf), setup with ("k1", "v1"), ("k2", "v2"), ("k3", "v3")
	 *
	 * <p>When</p>:we call update ("k1", "nv1")
	 *
	 * <p>Then</p>: sequntial storage read "k1" is "nv1"
	 * <b>and</b> chain storage has "k1"
	 * <b>and</b> storage2 does not have "k1"
	 */
	@Test
	public void shouldNotUpdateNewKVIfITIsFull() {
		// Given
		IStorage<String, String> storage1 = new MemoryStorage<String, String>(null, null, 2L);
		IStorage<String, String> storage2 = new MemoryStorage<String, String>(null, null, null);
		IStorage<String, String> chainStorage = new ChainedVStorage<>(new KeyGeneratorFactory().make(String.class), storage1, storage2);
		chainStorage.update("k1", "v1");
		chainStorage.update("k2", "v2");
		chainStorage.update("k3", "v3");

		// When
		chainStorage.update("k1", "nv1");

		// Then
		assertEquals("nv1", storage1.read("k1"));
		assertTrue(storage1.has("k1"));
		assertFalse(storage2.has("k1"));
	}

	/**
	 * <p>Given</p>: a chain storage with maximal capacity of 3
	 * <b>and</b> underlying storage capacities of (2, inf), setup with ("k1", "v1"), ("k2", "v2"), ("k3", "v3")
	 *
	 * <p>When</p>: we call update ("k4", "nv4")
	 *
	 * <p>Then</p> an exception is thrown with {@link OutOfSpaceException}
	 * <b>and</b> chain storage does not have "k4"
	 */
	@Test
	public void shouldUpdateTheSameStorage() {
		// Given
		IStorage<String, String> storage1 = new MemoryStorage<String, String>(null, null, 2L);
		IStorage<String, String> storage2 = new MemoryStorage<String, String>(null, null, null);
		IStorage<String, String> chainStorage = new ChainedVStorage<>(3L, new KeyGeneratorFactory().make(String.class), storage1, storage2);
		chainStorage.update("k1", "v1");
		chainStorage.update("k2", "v2");
		chainStorage.update("k3", "v3");

		// When
		Assertions.assertThrows(OutOfSpaceException.class, () -> {
			chainStorage.update("k4", "v4");
		});
	}


	/**
	 * <p>Given</p>: a chain storage with maximal capacity of 3
	 * <b>and</b> underlying storage capacities of (2, inf), setup with ("k1", "v1"), ("k2", "v2"), ("k3", "v3")
	 *
	 * <p>When</p>: we call delete ("k3")
	 *
	 * <p>Then</p> isFull is false
	 * <p>and</p> storage2 does not have k3
	 * <b>and</b> entries are 2
	 */
	@Test
	public void shouldNotFullAfterDelete() {
		// Given
		IStorage<String, String> storage1 = new MemoryStorage<String, String>(null, null, 2L);
		IStorage<String, String> storage2 = new MemoryStorage<String, String>(null, null, null);
		IStorage<String, String> chainStorage = new ChainedVStorage<>(3L, new KeyGeneratorFactory().make(String.class), storage1, storage2);
		chainStorage.update("k1", "v1");
		chainStorage.update("k2", "v2");
		chainStorage.update("k3", "v3");

		// When
		chainStorage.delete("k3");

		// Then
		assertFalse(storage2.has("k3"));
		assertEquals(2L, chainStorage.entries());
		assertFalse(chainStorage.isFull());
	}

	/**
	 * <p>Given</p>: a chain storage with maximal capacity of 3
	 * <b>and</b> underlying storage capacities of (2, inf), setup with ("k1", "v1"), ("k2", "v2"), ("k3", "v3")
	 *
	 * <p>When</p>: we call delete ("k4")
	 *
	 * <p>Then</p> isFull is true
	 * <b>and</b> entries are 3
	 */
	@Test
	public void shouldFullAfterDeleteNotExistingKey() {
		// Given
		IStorage<String, String> storage1 = new MemoryStorage<String, String>(null, null, 2L);
		IStorage<String, String> storage2 = new MemoryStorage<String, String>(null, null, null);
		IStorage<String, String> chainStorage = new ChainedVStorage<>(3L, new KeyGeneratorFactory().make(String.class), storage1, storage2);
		chainStorage.update("k1", "v1");
		chainStorage.update("k2", "v2");
		chainStorage.update("k3", "v3");

		// When
		chainStorage.delete("k4");

		// Then
		assertEquals(3L, chainStorage.entries());
		assertTrue(chainStorage.isFull());
	}

	/**
	 * <p>Given</p>: a chain storage with maximal capacity of 3
	 * <b>and</b> underlying storage capacities of (2, inf), setup with ("k1", "v1"), ("k2", "v2"), ("k3", "v3")
	 *
	 * <p>When</p>: we call delete ("k2")
	 * <b>and</b> we call create("v4")
	 *
	 * <p>Then</p> isFull is true
	 * <b>and</b> entries are 3
	 * <b>and</b> storage1 has the generated key for v4
	 */
	@Test
	public void shouldHaveSpaceAfterDelete() {
		// Given
		IStorage<String, String> storage1 = new MemoryStorage<String, String>(null, null, 2L);
		IStorage<String, String> storage2 = new MemoryStorage<String, String>(null, null, null);
		IStorage<String, String> chainStorage = new ChainedVStorage<>(3L, new KeyGeneratorFactory().make(String.class), storage1, storage2);
		chainStorage.update("k1", "v1");
		chainStorage.update("k2", "v2");
		chainStorage.update("k3", "v3");

		// When
		chainStorage.delete("k2");
		String key = chainStorage.create("v4");

		// Then
		assertEquals(3L, chainStorage.entries());
		assertTrue(chainStorage.isFull());
		assertTrue(chainStorage.has(key));
	}

	/**
	 * <p>Given</p>: a chain storage with maximal capacity of 3
	 * <b>and</b> underlying storage capacities of (2, inf), setup with ("k1", "v1"), ("k2", "v2"), ("k3", "v3")
	 *
	 * <p>When</p>: we call swap ("k2", "k3")
	 *
	 * <p>Then</p> isFull is true
	 * <b>and</b> entries are 3
	 * <b>and</b> storage1
	 * <b>and</b> storage2 has "k2"
	 */
	@Test
	public void shouldSwapOnDifferentStorage() {
		// Given
		IStorage<String, String> storage1 = new MemoryStorage<String, String>(null, null, 2L);
		IStorage<String, String> storage2 = new MemoryStorage<String, String>(null, null, null);
		IStorage<String, String> chainStorage = new ChainedVStorage<>(3L, new KeyGeneratorFactory().make(String.class), storage1, storage2);
		chainStorage.update("k1", "v1");
		chainStorage.update("k2", "v2");
		chainStorage.update("k3", "v3");

		// When
		chainStorage.swap("k2", "k3");

		// Then
		assertEquals("v2", chainStorage.read("k3"));
		assertEquals("v3", chainStorage.read("k2"));
	}


}