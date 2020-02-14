package com.wobserver.vcollections.storages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import com.wobserver.vcollections.keygenerators.KeyGeneratorFactory;

class CachedStorageTest implements StorageTest<CachedStorage<String, String>> {

	private IStorage<String, String> makeStorage(long maxSize, boolean onCreate, boolean onRead, boolean onUpdate, String... items) {
		Map<String, String> pairs = new HashMap<>();
		if (items != null) {
			for (int i = 0; i + 1 < items.length; i += 2) {
				String key = items[i];
				String value = items[i + 1];
				pairs.put(key, value);
			}
		}

		CachedStorage<String, String> result = new CachedStorage<>(String.class,
				new MemoryStorage<String, String>(new KeyGeneratorFactory().make(String.class), pairs, maxSize),
				new MemoryStorage<String, String>(new KeyGeneratorFactory().make(String.class), null, IStorage.NO_MAX_SIZE)
		);

		result.doCache(onCreate, onRead, onUpdate);
		return result;
	}

	@Override
	public IStorage<String, String> makeStorage(long maxSize, String... items) {
		return makeStorage(maxSize, false, true, false, items);
	}

	private Stream<Map.Entry<String, CachedStorage<String, String>>> iCacheStream(String... items) {
		AtomicReference<Integer> indexHolder = new AtomicReference<>(0);
		Supplier<Map.Entry<String, CachedStorage<String, String>>> cacheSupplier = () -> {
			int index = indexHolder.getAndUpdate(v -> v + 1);
			Boolean onCreate = (index & 1) == 0;
			Boolean onRead = (index & 2) == 0;
			Boolean onUpdate = (index & 4) == 0;

			String configuration = String.format("ICache configuration: onCreate: %s, onRead: %s, onUpdate: %s",
					onCreate, onRead, onUpdate);
			CachedStorage<String, String> iCache = (CachedStorage<String, String>)
					makeStorage(IStorage.NO_MAX_SIZE, onCreate, onRead, onUpdate, items);

			return new AbstractMap.SimpleEntry(configuration, iCache);

		};
		return Stream.generate(cacheSupplier).limit(8);

	}

	/**
	 * <p>Given</p>: Given a cached storage, in which the superset has data of ("k1", "v1"), but the subset has no data,
	 *
	 * <p>When</p>: we update("k1", "nv1")
	 *
	 * <p>Then</p>: reading the cache on k1 returns nv1
	 */
	@Test
	public void shouldContainsUpdatedValue() {
		iCacheStream().forEach(tuple -> {
			// Given
			String configuration = tuple.getKey();
			CachedStorage<String, String> cachedStorage = tuple.getValue();

			// When
			cachedStorage.update("k1", "nv1");

			// Then
			assertEquals("nv1", cachedStorage.read("k1"), configuration);
		});
	}


	/**
	 * <p>Given</p>: Given a cached storage, in which the superset has data of ("k1", "v1"), but the subset has no data,
	 *
	 * <p>When</p>: we update("k1", "nv1")
	 *
	 * <p>Then</p>: reading the cache on k1 returns nv1
	 */
	@Test
	public void shouldNotContainsDeletedValue() {
		iCacheStream("k1", "v1").forEach(tuple -> {
			// Given
			String configuration = tuple.getKey();
			CachedStorage<String, String> cachedStorage = tuple.getValue();

			// When
			cachedStorage.delete("k1");

			// Then
			assertFalse(cachedStorage.has("k1"), configuration);
		});
	}


	/**
	 * <p>Given</p>: Given a cached storage, in which the superset has data of ("k1", "v1"), but the subset has no data,
	 * <b>onCreate</b> false
	 * <b>onRead</b> true
	 * <b>onUpdate</b> false
	 *
	 * <p>When</p>: we do create, read, update
	 * <b>and</b> we flush the cache
	 *
	 * <b>Then</b> misses equals to 0
	 * <b>and</b> hits equals to 0
	 */
	@Test
	public void shouldResetHitsAndMisses() {
		// Given
		CachedStorage<String, String> cachedStorage = (CachedStorage<String, String>) makeStorage("k1", "v1");

		// When
		cachedStorage.read("k1");
		cachedStorage.update("k1", "v1");
		cachedStorage.create("v2");
		cachedStorage.flush();

		// Then
		assertEquals(0, cachedStorage.hits());
		assertEquals(0, cachedStorage.misses());

	}

	/**
	 * <p>Given</p>: Given a cached storage, in which the superset has data of ("k1", "v1"), but the subset has no data,
	 * <b>onCreate</b> true
	 * <b>onRead</b> fa;se
	 * <b>onUpdate</b> false
	 *
	 * <p>When</p>: we create, and then updated
	 *
	 * <p>Then</p>: hits equals to 1
	 * <b>and</b> misses equals to 0
	 */
	@Test
	public void shouldHaveTheUpdatedValue() {
		// Given
		CachedStorage<String, String> cachedStorage = (CachedStorage<String, String>) makeStorage(IStorage.NO_MAX_SIZE,
				true, false, true);

		// When
		String key = cachedStorage.create("v1");
		cachedStorage.update(key, "nv1");

		// Then
		assertEquals("nv1", cachedStorage.read(key));
	}


	/**
	 * <p>Given</p>: Given a cached storage, in which the superset has data of ("k1", "v1"), but the subset has no data,
	 * <b>onCreate</b> true
	 * <b>onRead</b> fa;se
	 * <b>onUpdate</b> false
	 *
	 * <p>When</p>: we create, and then we read
	 *
	 * <p>Then</p>: hits equals to 1
	 * <b>and</b> misses equals to 0
	 */
	@Test
	public void shouldHaveHitAfterCreate() {
		// Given
		CachedStorage<String, String> cachedStorage = (CachedStorage<String, String>) makeStorage(IStorage.NO_MAX_SIZE,
				true, false, false);

		// When
		String key = cachedStorage.create("v1");
		cachedStorage.read(key);

		// Then
		assertEquals(1, cachedStorage.hits());
		assertEquals(0, cachedStorage.misses());
	}

	/**
	 * <p>Given</p>: Given a cached storage, in which the superset has data of ("k1", "v1"), but the subset has no data,
	 * <b>onCreate</b> false
	 * <b>onRead</b> true
	 * <b>onUpdate</b> true
	 *
	 * <p>When</p>: we create, and then we read
	 *
	 * <p>Then</p>: hits equals to 0
	 * <b>and</b> misses equals to 1
	 */
	@Test
	public void shouldNotHaveHitAfterCreate() {
		// Given
		CachedStorage<String, String> cachedStorage = (CachedStorage<String, String>) makeStorage(IStorage.NO_MAX_SIZE,
				false, true, true);

		// When
		String key = cachedStorage.create("v1");
		cachedStorage.read(key);

		// Then
		assertEquals(0, cachedStorage.hits());
		assertEquals(1, cachedStorage.misses());
	}

	/**
	 * <p>Given</p>: Given a cached storage, in which the superset has data of ("k1", "v1"), but the subset has no data,
	 * <b>onCreate</b> false
	 * <b>onRead</b> true
	 * <b>onUpdate</b> false
	 *
	 * <p>When</p>:we read, and then we read
	 *
	 * <p>Then</p>: hits equals to 1
	 * <b>and</b> misses equals to 1
	 */
	@Test
	public void shouldHaveHitAfterRead() {
		// Given
		CachedStorage<String, String> cachedStorage = (CachedStorage<String, String>) makeStorage(IStorage.NO_MAX_SIZE,
				false, true, false, "k1", "v1");

		// When
		cachedStorage.read("k1");
		cachedStorage.read("k1");

		// Then
		assertEquals(1, cachedStorage.hits());
		assertEquals(1, cachedStorage.misses());
	}

	/**
	 * <p>Given</p>: Given a cached storage, in which the superset has data of ("k1", "v1"), but the subset has no data,
	 * <b>onCreate</b> true
	 * <b>onRead</b> false
	 * <b>onUpdate</b> true
	 *
	 * <p>When</p>:we read, and then we read
	 *
	 * <p>Then</p>: hits equals to 0
	 * <b>and</b> misses equals to 2
	 */
	@Test
	public void shouldNotHaveHitAfterRead() {
		// Given
		CachedStorage<String, String> cachedStorage = (CachedStorage<String, String>) makeStorage(IStorage.NO_MAX_SIZE,
				true, false, true, "k1", "v1");

		// When
		cachedStorage.read("k1");
		cachedStorage.read("k1");

		// Then
		assertEquals(0, cachedStorage.hits());
		assertEquals(2, cachedStorage.misses());
	}

	/**
	 * <p>Given</p>: Given a cached storage, in which the superset has data of ("k1", "v1"), but the subset has no data,
	 * <b>onCreate</b> false
	 * <b>onRead</b> fa;se
	 * <b>onUpdate</b> true
	 *
	 * <p>When</p>: we update, and then we read
	 *
	 * <p>Then</p>: hits equals to 1
	 * <b>and</b> misses equals to 0
	 */
	@Test
	public void shouldHaveHitAfterUpdate() {
		// Given
		CachedStorage<String, String> cachedStorage = (CachedStorage<String, String>) makeStorage(IStorage.NO_MAX_SIZE,
				false, false, true);

		// When
		String key = "k1";
		cachedStorage.update(key, "v1");
		cachedStorage.read(key);

		// Then
		assertEquals(1, cachedStorage.hits());
		assertEquals(0, cachedStorage.misses());

	}

	/**
	 * <p>Given</p>: Given a cached storage, in which the superset has data of ("k1", "v1"), but the subset has no data,
	 * <b>onCreate</b> true
	 * <b>onRead</b> true
	 * <b>onUpdate</b> false
	 *
	 * <p>When</p>: we update, and then we read
	 *
	 * <p>Then</p>: hits equals to 0
	 * <b>and</b> misses equals to 1
	 */
	@Test
	public void shouldNotHaveHitAfterUpdate() {
		// Given
		CachedStorage<String, String> cachedStorage = (CachedStorage<String, String>) makeStorage(IStorage.NO_MAX_SIZE,
				true, true, false);

		// When
		String key = "k1";
		cachedStorage.update(key, "v1");
		cachedStorage.read(key);

		// Then
		assertEquals(0, cachedStorage.hits());
		assertEquals(1, cachedStorage.misses());

	}
}