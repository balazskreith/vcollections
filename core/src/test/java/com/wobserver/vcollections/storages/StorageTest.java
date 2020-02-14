package com.wobserver.vcollections.storages;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test a storage for general capabilities according to the IStorage interface
 * The target storage needs to inherit this one and overwrite the makeStorage protected methods to test the actual storage
 */

public interface StorageTest<T extends IStorage<? super String, ? super String>> {

	IStorage<String, String> makeStorage(long maxSize, String... items);

	default IStorage<String, String> makeStorage(String... items) {
		return makeStorage(IStorage.NO_MAX_SIZE, items);
	}


	/**
	 * <p>Given</p>: An empty storage
	 *
	 * <p>When</p>: A create method is called with "value" value
	 *
	 * <p>Then</p> Then read operation can read "value" with a generated key
	 * <b>and</b> the entries is 1
	 * <b>and</b> the isEmpty false
	 * <b>and</b> has(generated key) returns true
	 */
	@Test
	default void shouldCreateAnEntry() {
		// Given
		String value = "value";
		IStorage<String, String> storage = makeStorage();

		// When
		String key = storage.create(value);

		// Then
		assertEquals(value, storage.read(key));
		assertEquals(1, storage.entries());
		assertFalse(storage.isEmpty());
		assertTrue(storage.has(key));
	}


	/**
	 * <p>Given</p>: A filled storage with (key,value) pair
	 *
	 * <p>When</p>: We read key
	 *
	 * <p>Then</p> The result is a value
	 */
	@Test
	default void shouldReadASingleEntry() {
		// Given
		String value = "value";
		IStorage<String, String> storage = makeStorage();

		// When
		String key = storage.create(value);

		// Then
		assertEquals(value, storage.read(key));
	}

	/**
	 * <p>Given</p>: A filled storage with (key,value) pair
	 *
	 * <p>When</p>: We read nkey, which does not exists
	 *
	 * <p>Then</p> The result is a null value
	 */
	@Test
	default void shouldReadNullForNotExistedKey() {
		// Given
		String value = "value";
		IStorage<String, String> storage = makeStorage();

		// When
		String key = storage.create(value);

		// Then
		assertNull(storage.read("non" + key));
	}


	/**
	 * <p>Given</p>: A filled storage with (key,value) pair
	 *
	 * <p>When</p>: We read null
	 *
	 * <p>Then</p> The result is a null value
	 * <b>and</b> the storage has the key
	 */
	@Test
	default void shouldReadNullForExistedKey() {
		// Given
		String value = null;
		IStorage<String, String> storage = makeStorage();

		// When
		String key = storage.create(value);

		// Then
		assertNull(storage.read(key));
		assertTrue(storage.has(key));
	}

	/**
	 * <p>Given</p>: An empty storage
	 *
	 * <p>Then</p> The result of read is null
	 * <b>and</b> has(null) returns false
	 */
	@Test
	default void shouldNotHaveKey() {
		// Given
		IStorage<String, String> storage = makeStorage();

		// Then
		assertNull(storage.read("key"));
		assertFalse(storage.has("key"));
		assertTrue(storage.isEmpty());
	}

	/**
	 * <p>Given</p>: A storage with a (null, value) pair
	 *
	 * <p>When</p>: We read null
	 *
	 * <p>Then</p> The result is value
	 * <b>and</b> has(null) returns true
	 */
	@Test
	default void shouldReadKey() {
		// Given
		String value = "value";
		IStorage<String, String> storage = makeStorage();

		// When
		storage.update(null, value);

		// Then
		assertEquals(value, storage.read(null));
		assertTrue(storage.has(null));
	}


	/**
	 * <p>Given</p>: A storage with (key1, null), (key2, value2), (null, nvalue)
	 *
	 * <p>When</p>: We update (key1, value1)
	 *
	 * <p>Then</p> the read key1 returns value1
	 */
	@Test
	default void shouldUpdateKey() {
		// Given
		String[] values = {"key1", null, "key2", "value2", null, "nvalue"};
		IStorage<String, String> storage = makeStorage(values);

		// When
		storage.update("key1", "value1");

		// Then
		assertEquals("value1", storage.read("key1"));
	}


	/**
	 * <p>Given</p>: A storage with (key1, null), (key2, value2), (null, nvalue)
	 *
	 * <p>When</p>: We update (null, new-nvalue)
	 *
	 * <p>Then</p> the read null returns new-nvalue
	 */
	@Test
	default void shouldUpdateNullKey() {
		// Given
		String[] values = {"key1", null, "key2", "value2", null, "nvalue"};
		IStorage<String, String> storage = makeStorage(values);

		// When
		storage.update(null, "new-value1");

		// Then
		assertEquals("new-value1", storage.read(null));
	}

	/**
	 * <p>Given</p>: an empty storage
	 *
	 * <p>When</p>: We update (key3, value3)
	 *
	 * <p>Then</p> the read key3 returns value3
	 * <b>and</b> the entries is 1
	 * <b>and</b> isEmpty is false
	 */
	@Test
	default void shouldAddNewEntryByUpdating() {
		// Given
		IStorage<String, String> storage = makeStorage();

		// When
		storage.update("key3", "value3");

		// Then
		assertEquals("value3", storage.read("key3"));
		assertEquals(1L, storage.entries());
		assertFalse(storage.isEmpty());
	}

	/**
	 * <p>Given</p>: A storage with (key1, null), (key2, value2), (null, nvalue)
	 *
	 * <p>When</p>: We update (key2, null)
	 *
	 * <p>Then</p> the read key2 returns null
	 */
	@Test
	default void shouldUpdateWithNull() {
		// Given
		String[] values = {"key1", null, "key2", "value2", null, "nvalue"};
		IStorage<String, String> storage = makeStorage(values);

		// When
		storage.update("key2", null);

		// Then
		assertNull(storage.read("key2"));
	}

	/**
	 * <p>Given</p>: A storage with (key1, null), (key2, value2), (null, nvalue)
	 *
	 * <p>When</p>: We update (null, null)
	 *
	 * <p>Then</p> the read null returns null
	 * <b>and</b> the entries is 3
	 */
	@Test
	default void shouldUpdateNullWithNull() {
		// Given
		String[] values = {"key1", null, "key2", "value2", null, "nvalue"};
		IStorage<String, String> storage = makeStorage(values);

		// When
		storage.update(null, null);

		// Then
		assertNull(storage.read(null));
		assertTrue(storage.has(null));
	}

	/**
	 * <p>Given</p>: A storage with (key1, null), (key2, value2), (null, nvalue)
	 *
	 * <p>When</p>: We delete key1
	 *
	 * <p>Then</p> the read key1 returns null
	 * <b>and</b> has(key1) returns false
	 * <b>and</b> the entries is 2
	 */
	@Test
	default void shouldDeleteKey() {
		// Given
		String[] values = {"key1", null, "key2", "value2", null, "nvalue"};
		IStorage<String, String> storage = makeStorage(values);

		// When
		storage.delete("key1");

		// Then
		assertNull(storage.read("key1"));
		assertFalse(storage.has("key1"));
		assertEquals(2, storage.entries());
	}

	/**
	 * <p>Given</p>: A storage with (key1, null), (key2, value2), (null, nvalue)
	 *
	 * <p>When</p>: We delete null key
	 *
	 * <p>Then</p> the read key1 returns null
	 * <b>and</b> has(key1) returns false
	 * <b>and</b> the entries is 2
	 */
	@Test
	default void shouldDeleteNullKey() {
		// Given
		String[] values = {"key1", null, "key2", "value2", null, "nvalue"};
		IStorage<String, String> storage = makeStorage(values);

		// When
		storage.delete(null);

		// Then
		assertNull(storage.read(null));
		assertFalse(storage.has(null));
		assertEquals(2, storage.entries());
	}

	/**
	 * <p>Given</p>: A storage with (key1, null), (key2, value2), (null, nvalue)
	 *
	 * <p>When</p>: We delete key3 key
	 *
	 * <p>Then</p> no exception is thrown
	 * <b>and</b> has(key3) returns false
	 * <b>and</b> the entries is 3
	 */
	@Test
	default void shouldDeleteNotExistentKey() {
		// Given
		String[] values = {"key1", null, "key2", "value2", null, "nvalue"};
		IStorage<String, String> storage = makeStorage(values);

		// When
		storage.delete("key3");

		// Then
		assertNull(storage.read("key3"));
		assertFalse(storage.has("key3"));
		assertEquals(3, storage.entries());
	}


	/**
	 * <p>Given</p>: A storage with (key1, null), (key2, value2), (null, nvalue)
	 *
	 * <p>When</p>: We clear the storage
	 *
	 * <b>Then</b> the entries is 0
	 * <b>and</b> the isEmpty true
	 */
	@Test
	default void shouldClearTheStorage() {
		// Given
		String[] values = {"key1", null, "key2", "value2", null, "nvalue"};
		IStorage<String, String> storage = makeStorage(values);

		// When
		storage.clear();

		// Then
		assertTrue(storage.isEmpty());
		assertEquals(0, storage.entries());
	}

	/**
	 * <p>Given</p>: A storage with (key1, null), (key2, value2), (null, nvalue)
	 *
	 * <p>When</p>: We swap key1, key2
	 *
	 * <b>Then</b> the read(key1, key2) will be (value2, null)
	 */
	@Test
	default void shouldSwapKeys() {
		// Given
		String[] values = {"key1", null, "key2", "value2", null, "nvalue"};
		IStorage<String, String> storage = makeStorage(values);

		// When
		storage.swap("key1", "key2");

		// Then
		assertEquals("value2", storage.read("key1"));
		assertEquals(null, storage.read("key2"));
	}

	/**
	 * <p>Given</p>: A storage with (key1, null), (key2, value2), (null, nvalue)
	 *
	 * <p>When</p>: We swap null, key2
	 *
	 * <b>Then</b> the read(null, key2) will be (value2, nvalue)
	 */
	@Test
	default void shouldSwapNullKey() {
		// Given
		String[] values = {"key1", null, "key2", "value2", null, "nvalue"};
		IStorage<String, String> storage = makeStorage(values);

		// When
		storage.swap("key1", null);

		// Then
		assertEquals("nvalue", storage.read("key1"));
		assertEquals(null, storage.read(null));
	}

	/**
	 * <p>Given</p>: A storage with (key1, null), (key2, value2)
	 *
	 * <p>When</p>: We swap a key, which does not exist
	 *
	 * <b>Then</b> KeyNotFound has been thrown
	 */
	@Test
	default void shouldThrowExceptionIfKeyNotExistingAtSwap1() {
		// Given
		String[] values = {"key1", null, "key2", "value2"};
		IStorage<String, String> storage = makeStorage(values);

		// When
		Assertions.assertThrows(KeyNotFoundException.class, () -> {
			storage.swap("nokey", "key1");
		});

		// Then
	}

	/**
	 * <p>Given</p>: A storage with (key1, null), (key2, value2)
	 *
	 * <p>When</p>: We swap a key, which does not exist
	 *
	 * <b>Then</b> KeyNotFound has been thrown
	 */
	@Test
	default void shouldThrowExceptionIfKeyNotExistingAtSwap2() {
		// Given
		String[] values = {"key1", null, "key2", "value2"};
		IStorage<String, String> storage = makeStorage(values);

		// When
		Assertions.assertThrows(KeyNotFoundException.class, () -> {
			storage.swap("key1", "nokey");
		});

		// Then
	}


	/**
	 * <p>Given</p>: A storage with (key1, null), (key2, value2), (null, nvalue)
	 *
	 * <p>When</p>: We iterate it through
	 * <b>and</b> save the iteration elements into a list
	 *
	 * <b>Then</b> the saved list contains the pairs (key1, null), (key2, value2), (null, nvalue)
	 */
	@Test
	default void shouldIterateValues() {
		// Given
		String[] values = {"key1", null, "key2", "value2", null, "nvalue"};
		IStorage<String, String> storage = makeStorage(values);
		Map<String, String> read = new HashMap<>();

		// When
		for (Iterator<Map.Entry<String, String>> it = storage.iterator(); it.hasNext(); ) {
			Map.Entry<String, String> entry = it.next();
			read.put(entry.getKey(), entry.getValue());
		}

		// Then
		assertEquals(null, read.get("key1"));
		assertEquals("value2", read.get("key2"));
		assertEquals("nvalue", read.get(null));
		assertEquals(3, read.size());
	}


	/**
	 * <p>Given</p>: A storage with (key1, null), (key2, value2), (null, nvalue)
	 *
	 * <p>When</p>: We iterate it through
	 * <b>and</b>  remove the first element
	 *
	 * <b>Then</b> the saved list contains the pairs (key2, value2), (null, nvalue)
	 * <b>and</b> the entries is 2
	 */

	@Test
	@Disabled(value = "FileStorage DirectoryStream Iterator throws NoSuchElement exception because it was not implemented there.")
	default void shouldIterateValuesAndRemoveFirst() {
		// Given
		String[] values = {"key1", null, "key2", "value2", null, "nvalue"};
		IStorage<String, String> storage = makeStorage(values);
		Map<String, String> read = new HashMap<>();
		Iterator<Map.Entry<String, String>> it = storage.iterator();
		Map.Entry<String, String> item;

		// When
		it.next();
		it.remove();
		item = it.next();
		read.put(item.getKey(), item.getValue());
		item = it.next();
		read.put(item.getKey(), item.getValue());

		// Then
		assertEquals(null, read.get("key1"));
		assertEquals("value2", read.get("key2"));
		assertEquals("nvalue", read.get(null));
		assertEquals(2, read.size());
	}


	/**
	 * TESTS WITH LIMITED SIZE!
	 */

	/**
	 * <p>Given</p>: A storage with a random keygenerator
	 * <b>and</b> with a maximum size of 4 and (key1, null), (key2, value2), (null, nvalue) pairs
	 *
	 * <p>When</p>: We create one element
	 *
	 * <b>Then</b> the storage is Full
	 * <b>and</b> the entries is 4
	 */
	@Test
	default void shouldBeFull() {
		// Given
		String[] values = {"key1", null, "key2", "value2", null, "nvalue"};
		IStorage<String, String> storage = makeStorage(4L, values);

		// When
		storage.create("value4");

		// Then
		assertEquals(4, storage.entries());
		assertTrue(storage.isFull());

	}

	/**
	 * <p>Given</p>: A storage with a random keygenerator
	 * <b>and</b> with a maximum size of 3 and (key1, null), (key2, value2), (null, nvalue) pairs
	 *
	 * <p>When</p>: We create one element
	 *
	 * <b>Then</b> {@link OutOfSpaceException} is thrown
	 * <b>and</b> the entries is 3 if we check after the exception
	 */
	@Test
	default void shouldThrowOutOfSpaceExceptionAfterCreate() {
		// Given
		String[] values = {"key1", null, "key2", "value2", null, "nvalue"};
		IStorage<String, String> storage = makeStorage(3L, values);

		// When
		Assertions.assertThrows(OutOfSpaceException.class, () -> {
			storage.create("value4");
		});

		// Then
		assertEquals(3, storage.entries());
	}


	/**
	 * <p>Given</p>: A storage with a random keygenerator
	 * <b>and</b> with a maximum size of 3 and (key1, null), (key2, value2), (null, nvalue) pairs
	 *
	 * <p>When</p>: We update an element does existed before
	 *
	 * <b>Then</b> the entries is 3
	 * <b>and</b> isFull is true
	 */
	@Test
	default void shouldNotThrowOutOfSpaceExceptionAfterUpdate() {
		// Given
		String[] values = {"key1", null, "key2", "value2", null, "nvalue"};
		IStorage<String, String> storage = makeStorage(3L, values);

		// When
		storage.update("key1", "value1");

		// Then
		assertEquals(3, storage.entries());
		assertTrue(storage.isFull());
	}

	/**
	 * <p>Given</p>: A storage with a random keygenerator
	 * <b>and</b> with a maximum size of 3 and (key1, null), (key2, value2), (null, nvalue) pairs
	 *
	 * <p>When</p>: We update an element does not existed before
	 *
	 * <b>Then</b> {@link OutOfSpaceException} is thrown
	 * <b>and</b> the entries is 3 if we check after the exception
	 */
	@Test
	default void shouldThrowOutOfSpaceExceptionAfterUpdate() {
		// Given
		String[] values = {"key1", null, "key2", "value2", null, "nvalue"};
		IStorage<String, String> storage = makeStorage(3L, values);

		// When
		Assertions.assertThrows(OutOfSpaceException.class, () -> {
			storage.update("key3", "value4");
		});

		// Then
		assertEquals(3, storage.entries());
	}

	/**
	 * <p>Given</p>: A storage with a random keygenerator
	 * <b>and</b> with a maximum size of 3 and (key1, null), (key2, value2), (null, nvalue) pairs
	 *
	 * <p>When</p>: We delete null
	 *
	 * <b>Then</b> the entries is 2
	 * <b>and</b> isFull is false
	 */
	@Test
	default void shouldDeleteNull() {
		// Given
		String[] values = {"key1", null, "key2", "value2", null, "nvalue"};
		IStorage<String, String> storage = makeStorage(3L, values);

		// When
		storage.delete("key2");

		// Then
		assertEquals(2, storage.entries());
		assertFalse(storage.isFull());
	}
}
