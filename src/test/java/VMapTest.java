import static org.junit.jupiter.api.Assertions.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import storages.IStorage;
import storages.MemoryStorage;

class VMapTest {

	private Map<String, String> makeMap(long maxCapacity, String... items) {
		HashMap<String, String> initialItems = new HashMap<>();
		if (items != null) {
			for (Long i = 0L; i + 1 < items.length; i += 2) {
				String key = items[i.intValue()];
				String value = items[Long.valueOf(i + 1).intValue()];
				initialItems.put(key, value);
			}
		}
		IStorage<String, String> storage = new MemoryStorage<>(initialItems, maxCapacity);
		return new VMap<>(storage);
	}

	private Map<String, String> makeMap(String... items) {
		return this.makeMap(IStorage.NO_MAX_SIZE, items);
	}

	/**
	 * size
	 * remove, putIfAbsent, replaceAll, putAll, remove, put, containsKey
	 *
	 * forEach, getOrDefault, entrySet, values, keySet, get, containsValue
	 */

	/**
	 * <b>Given</b>: a filled map
	 *
	 * <b>When</b>: we run keySet
	 *
	 * <b>Then</b>: we got a set of keys
	 */
	@Test
	public void shouldGetValues() {
		// Given
		Map<String, String> map = this.makeMap("key", "value", "key2", null);

		// When
		assertEquals("value", map.get("key"));
		assertNull(map.get("key2"));
		assertNull(map.getOrDefault("key2", "default"));
		assertEquals("default", map.getOrDefault("key3", "default"));
	}

	/**
	 * <b>Given</b>: a filled map
	 *
	 * <b>When</b>: we run keySet
	 *
	 * <b>Then</b>: we got a set of keys
	 */
	@Test
	public void shouldGetSetOfValues() {
		// Given
		Map<String, String> map = this.makeMap("key", "value", "key2", "value2");

		// When
		Collection<String> values = map.values();

		// Then
		assertTrue(values.contains("value"));
		assertFalse(values.contains("key"));
	}

	/**
	 * <b>Given</b>: a filled map
	 *
	 * <b>When</b>: we run keySet
	 *
	 * <b>Then</b>: we got a set of keys
	 */
	@Test
	public void shouldGetSetOfKeys() {
		// Given
		Map<String, String> map = this.makeMap("key", "value", "key2", "value2");

		// When
		Set<String> keys = map.keySet();

		// Then
		assertFalse(keys.contains("value"));
		assertTrue(keys.contains("key"));
	}

	/**
	 * <b>Given</b>: a filled map
	 *
	 * <b>When</b>: we run containsValue
	 *
	 * <b>Then</b>: we got value if we retrieve key
	 */
	@Test
	public void shouldContainsValue() {
		// Given
		Map<String, String> map = this.makeMap("key", "value", "key2", "value2");

		// When

		// Then
		assertFalse(map.containsValue("key"));
		assertTrue(map.containsValue("value"));
	}

	/**
	 * <b>Given</b>: a filled map
	 *
	 * <b>When</b>: we put a key value pair
	 *
	 * <b>Then</b>: we got value if we retrieve key
	 */
	@Test
	public void shouldContainsKey() {
		// Given
		Map<String, String> map = this.makeMap("key", "value", "key2", "value2");

		// When

		// Then
		assertTrue(map.containsKey("key"));
		assertFalse(map.containsKey("value"));
	}

	/**
	 * <b>Given</b>: a filled map
	 *
	 * <b>When</b>: we replace value if the key is "key"
	 *
	 * <b>Then</b>: we got value if we retrieve key
	 */
	@Test
	public void shouldReplaceAll() {
		// Given
		Map<String, String> map = this.makeMap("key", "value", "key2", "value2");

		// When
		map.replaceAll((k, v) -> k.equals("key") ? "replaced" : v);

		// Then
		assertEquals("replaced", map.get("key"));
		assertEquals("value2", map.get("key2"));
	}

	/**
	 * <b>Given</b>: an empty map
	 *
	 * <b>When</b>: we put a key value pair
	 *
	 * <b>Then</b>: we got value if we retrieve key
	 */
	@Test
	public void shouldPutIfAbsent() {
		// Given
		Map<String, String> map = this.makeMap("key", "value");

		// When
		map.putIfAbsent("key", "value2");
		map.putIfAbsent("key2", "value2");

		// Then
		assertEquals("value", map.get("key"));
		assertEquals("value2", map.get("key2"));
	}

	/**
	 * <b>Given</b>: an empty map
	 *
	 * <b>When</b>: we put a key value pair
	 *
	 * <b>Then</b>: we got value if we retrieve key
	 */
	@Test
	public void shouldRemove() {
		// Given
		Map<String, String> map = this.makeMap("key", "remove");

		// When
		map.remove("key");

		// Then
		assertNotEquals("value", map.get("key"));
		assertEquals(0, map.size());
		assertTrue(map.isEmpty());
	}

	/**
	 * <b>Given</b>: an empty map
	 *
	 * <b>When</b>: we put a key value pair
	 *
	 * <b>Then</b>: we got value if we retrieve key
	 */
	@Test
	public void shouldPut() {
		// Given
		Map<String, String> map = this.makeMap();

		// When
		map.put("key", "value");

		// Then
		assertEquals("value", map.get("key"));
	}

	/**
	 * <b>Given</b>: an empty map
	 *
	 * <b>When</b>: we put key value pairs
	 *
	 * <b>Then</b>: we got value if we retrieve key
	 */
	@Test
	public void shouldPutAll() {
		// Given
		Map<String, String> map = this.makeMap();

		// When
		map.putAll(this.makeMap("key", "value", "key2", "value2"));

		// Then
		assertEquals("value", map.get("key"));
		assertEquals("value2", map.get("key2"));
	}

	/**
	 * <b>Given</b>: a list filled with ("value', null, "value2")
	 *
	 * <b>When</b>: we clear
	 *
	 * <b>Then</b>: we check if the list is empty
	 * <b>and</b> size is 0
	 */
	@Test
	public void shouldClear() {
		// Given
		Map<String, String> map = this.makeMap("1", "value", "2", "value2");

		// When
		map.clear();

		// Then
		assertEquals(0, map.size());
		assertTrue(map.isEmpty());
	}

	/**
	 * isEmpty, size
	 * remove, putIfAbsent, replaceAll, clear, putAll, remove, put, containsKey
	 *
	 * forEach, getOrDefault, entrySet, values, keySet, get, containsValue
	 */
}