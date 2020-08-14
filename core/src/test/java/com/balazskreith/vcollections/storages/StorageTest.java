package com.balazskreith.vcollections.storages;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Array;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test a storage for general capabilities according to the IStorage interface
 * The target storage needs to inherit this one and overwrite the makeStorage protected methods to test the actual storage
 */

public interface StorageTest<K, V, T extends IStorage<K, V>> {

	IStorage<K, V> makeStorage(long maxSize, Map.Entry<K, V>... entries);

	default IStorage<K, V> makeStorage(Map.Entry<K, V>... entries) {
		return makeStorage(IStorage.NO_MAX_SIZE, entries);
	}

	K toKey(String key);

	V toValue(String value);

	default Map.Entry<K, V> toEntry(String keyString, String valueString) {
		K key = toKey(keyString);
		V value = toValue(valueString);
		return new AbstractMap.SimpleEntry(key, value);
	}

	default Map.Entry<K, V>[] toEntries(String... items) {
		List<Map.Entry<K, V>> entries = new ArrayList<>();
		if (items != null) {
			for (int i = 0; i + 1 < items.length; i += 2) {
				String key = items[i];
				String value = items[i + 1];
				Map.Entry<K, V> entry = toEntry(key, value);
				entries.add(entry);
			}
		}
		Map.Entry<K, V>[] result = (Map.Entry<K, V>[]) Array.newInstance(Map.Entry.class, entries.size());
		return entries.toArray(result);
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
		IStorage<K, V> storage = makeStorage();

		// When
		V value = toValue("value");
		K key = storage.create(value);

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
		IStorage<K, V> storage = makeStorage();

		// When
		V value = toValue("value");
		K key = storage.create(value);

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
		IStorage<K, V> storage = makeStorage();

		// When
		V value = toValue("value");
		K key = storage.create(value);

		// Then
		assertNull(storage.read(toKey("non" + key)));
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
		IStorage<K, V> storage = makeStorage();

		// When
		V value = toValue(null);
		K key = storage.create(value);

		// Then
		assertEquals(value, storage.read(key));
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
		IStorage<K, V> storage = makeStorage();

		// Then
		assertNull(storage.read(toKey("key")));
		assertFalse(storage.has(toKey("key")));
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
		IStorage<K, V> storage = makeStorage();

		// When
		Map.Entry<K, V> entry = toEntry(null, null);
		storage.update(entry.getKey(), entry.getValue());

		// Then
		assertEquals(entry.getValue(), storage.read(entry.getKey()));
		assertTrue(storage.has(entry.getKey()));
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
		Map.Entry<K, V>[] entries = toEntries("key1", null, "key2", "value2", null, "nvalue");
		IStorage<K, V> storage = makeStorage(entries);

		// When
		Map.Entry<K, V> entry = toEntry(entries[0].getKey().toString(), "value1");
		K key = entry.getKey();
		V newValue = entry.getValue();
		storage.update(key, newValue);

		// Then
		assertEquals(newValue, storage.read(key));
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
		Map.Entry<K, V>[] entries = toEntries("key1", null, "key2", "value2", null, "nvalue");
		IStorage<K, V> storage = makeStorage(entries);

		// When
		Map.Entry<K, V> entry = toEntry(null, "new-value1");
		K key = entry.getKey();
		V value = entry.getValue();
		storage.update(key, value);

		// Then
		assertEquals(value, storage.read(key));
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
		IStorage<K, V> storage = makeStorage();

		// When
		Map.Entry<K, V> entry = toEntry("key3", "value3");
		K key = entry.getKey();
		V value = entry.getValue();
		storage.update(key, value);

		// Then
		assertEquals(value, storage.read(key));
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
		Map.Entry<K, V>[] entries = toEntries("key1", null, "key2", "value2", null, "nvalue");
		IStorage<K, V> storage = makeStorage(entries);

		// When
		Map.Entry<K, V> entry = toEntry("key2", null);
		K key = entry.getKey();
		V value = entry.getValue();
		storage.update(key, value);

		// Then
		assertEquals(value, storage.read(key));
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
		Map.Entry<K, V>[] entries = toEntries("key1", null, "key2", "value2", null, "nvalue");
		IStorage<K, V> storage = makeStorage(entries);

		// When
		Map.Entry<K, V> entry = toEntry(null, null);
		K nullKey = entry.getKey();
		V nullValue = entry.getValue();
		storage.update(nullKey, nullValue);

		// Then
		assertEquals(nullValue, storage.read(nullKey));
		assertTrue(storage.has(nullKey));
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
		Map.Entry<K, V>[] entries = toEntries("key1", null, "key2", "value2", null, "nvalue");
		IStorage<K, V> storage = makeStorage(entries);

		// When
		K key = toKey("key1");
		storage.delete(key);

		// Then
		assertNull(storage.read(key));
		assertFalse(storage.has(key));
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
		Map.Entry<K, V>[] entries = toEntries("key1", null, "key2", "value2", null, "nvalue");
		IStorage<K, V> storage = makeStorage(entries);

		// When
		K key = toKey(null);
		storage.delete(key);

		// Then
		assertNull(storage.read(key));
		assertFalse(storage.has(key));
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
		Map.Entry<K, V>[] entries = toEntries("key1", null, "key2", "value2", null, "nvalue");
		IStorage<K, V> storage = makeStorage(entries);

		// When
		K key = toKey("key3");
		storage.delete(key);

		// Then
		assertNull(storage.read(key));
		assertFalse(storage.has(key));
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
		Map.Entry<K, V>[] entries = toEntries("key1", null, "key2", "value2", null, "nvalue");
		IStorage<K, V> storage = makeStorage(entries);

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
		Map.Entry<K, V>[] entries = toEntries("key1", null, "key2", "value2", null, "nvalue");
		IStorage<K, V> storage = makeStorage(entries);

		// When
		Map.Entry<K, V> entry1 = entries[0];
		Map.Entry<K, V> entry2 = entries[1];
		storage.swap(entry1.getKey(), entry2.getKey());

		// Then
		assertEquals(entry2.getValue(), storage.read(entry1.getKey()));
		assertEquals(entry1.getValue(), storage.read(entry2.getKey()));
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
		Map.Entry<K, V>[] entries = toEntries("key1", null, "key2", "value2", null, "nvalue");
		IStorage<K, V> storage = makeStorage(entries);

		// When
		Map.Entry<K, V> entry1 = entries[0];
		Map.Entry<K, V> entry2 = entries[2];
		storage.swap(entry1.getKey(), entry2.getKey());

		// Then
		assertEquals(entry2.getValue(), storage.read(entry1.getKey()));
		assertEquals(entry1.getValue(), storage.read(entry2.getKey()));
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
		Map.Entry<K, V>[] entries = toEntries("key1", null, "key2", "value2");
		IStorage<K, V> storage = makeStorage(entries);

		// When
		Assertions.assertThrows(KeyNotFoundException.class, () -> {
			storage.swap(toKey("nokey"), toKey("key1"));
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
		Map.Entry<K, V>[] entries = toEntries("key1", null, "key2", "value2");
		IStorage<K, V> storage = makeStorage(entries);

		// When
		Assertions.assertThrows(KeyNotFoundException.class, () -> {
			storage.swap(toKey("key1"), toKey("nokey"));
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
		Map.Entry<K, V>[] entries = toEntries("key1", null, "key2", "value2", null, "nvalue");
		IStorage<K, V> storage = makeStorage(entries);
		Map<K, V> read = new HashMap<>();

		// When
		for (Iterator<Map.Entry<K, V>> it = storage.iterator(); it.hasNext(); ) {
			Map.Entry<K, V> entry = it.next();
			read.put(entry.getKey(), entry.getValue());
		}

		// Then
		assertEquals(entries[0].getValue(), read.get(entries[0].getKey()));
		assertEquals(entries[1].getValue(), read.get(entries[1].getKey()));
		assertEquals(entries[2].getValue(), read.get(entries[2].getKey()));
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
		Map.Entry<K, V>[] entries = toEntries("key1", null, "key2", "value2", null, "nvalue");
		IStorage<K, V> storage = makeStorage(entries);
		Map<K, V> read = new HashMap<>();
		Iterator<Map.Entry<K, V>> it = storage.iterator();
		Map.Entry<K, V> item;

		// When
		it.next();
		it.remove();
		item = it.next();
		read.put(item.getKey(), item.getValue());
		item = it.next();
		read.put(item.getKey(), item.getValue());

		// Then
		assertEquals(null, read.get(toKey("key1")));
		assertEquals(entries[1].getValue(), read.get(entries[1].getKey()));
		assertEquals(entries[2].getValue(), read.get(entries[2].getKey()));
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
		Map.Entry<K, V>[] entries = toEntries("key1", null, "key2", "value2", null, "nvalue");
		IStorage<K, V> storage = makeStorage(4L, entries);

		// When
		storage.create(toValue("value4"));

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
		Map.Entry<K, V>[] entries = toEntries("key1", null, "key2", "value2", null, "nvalue");
		IStorage<K, V> storage = makeStorage(3L, entries);

		// When
		Assertions.assertThrows(OutOfSpaceException.class, () -> {
			storage.create(toValue("value4"));
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
		Map.Entry<K, V>[] entries = toEntries("key1", null, "key2", "value2", null, "nvalue");
		IStorage<K, V> storage = makeStorage(3L, entries);

		// When
		Map.Entry<K, V> entry = toEntry("key1", "value1");
		storage.update(entry.getKey(), entry.getValue());

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
		Map.Entry<K, V>[] entries = toEntries("key1", null, "key2", "value2", null, "nvalue");
		IStorage<K, V> storage = makeStorage(3L, entries);

		// When
		Assertions.assertThrows(OutOfSpaceException.class, () -> {
			Map.Entry<K, V> entry = toEntry("key3", "value4");
			K key = entry.getKey();
			V value = entry.getValue();
			storage.update(key, value);
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
		Map.Entry<K, V>[] entries = toEntries("key1", null, "key2", "value2", null, "nvalue");
		IStorage<K, V> storage = makeStorage(3L, entries);

		// When
		storage.delete(toKey("key2"));

		// Then
		assertEquals(2, storage.entries());
		assertFalse(storage.isFull());
	}
}
