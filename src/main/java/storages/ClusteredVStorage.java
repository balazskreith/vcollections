package storages;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Chains hte storages passed to supervise by this type of virtual storage
 * <p>
 * Advantage: you can add limitations to each storage, but the overall storage will be the accumulated limitations
 *
 * @param <K>
 * @param <V>
 */
public class ClusteredVStorage<K, V> implements IStorage<K, V>, ISetKeyGenerator<K> {

	protected List<IStorage<K, V>> storages;
	private Long capacity = 0L;
	private Long entries = 0L;
	private Supplier<K> keyGenerator;

	/**
	 * @param initialEntries the initial entries for the chained storage will be
	 * @param maxCapacity    The maxmimal capacity of this storage
	 * @param keyGenerator   keygenerator
	 * @param storages
	 * @throws NotAvailableStorage   - if the number of underlying storage is less than 1
	 *                               - if the maxCapacity is less than 1
	 *                               - if there is not available space in the underlying storages
	 * @throws IllegalStateException - if there is no storage, which does not have a limitation
	 *                               - if the unlimited storage is not the last one
	 */
	public ClusteredVStorage(Map<K, V> initialEntries, long maxCapacity, Supplier<K> keyGenerator, IStorage<K, V>... storages) {
		if (storages == null || storages.length < 1) {
			throw new NotAvailableStorage("Number of underlying storage must be greater than 0");
		}
		this.storages = Arrays.asList(storages);
		long notInfiniteStorageCapacity = this.storages.stream().filter(s -> s.capacity() != NO_MAX_SIZE).count();
		if (0 < notInfiniteStorageCapacity) {
			throw new IllegalStateException("All underlying storage must not have limitations.");
		}
		if (maxCapacity < 1 && maxCapacity != NO_MAX_SIZE) {
			throw new IllegalStateException("Max capacity is either unbound, or must be greater than 0");
		}
		this.entries = this.storages.stream().map(s -> s.entries()).reduce((r, v) -> r + v).get();
		this.capacity = maxCapacity;
		this.keyGenerator = keyGenerator;
		if (initialEntries != null) {
			initialEntries.entrySet().forEach(entry -> this.update(entry.getKey(), entry.getValue()));
		}
	}

	/**
	 * {@code maxCapacity} defaults to {@link IStorage#NO_MAX_SIZE}.
	 *
	 * @see ChainedVStorage#ChainedVStorage(Map, long, Supplier, IStorage[])
	 */
	public ClusteredVStorage(Supplier<K> keyGenerator, IStorage<K, V>... storages) {
		this(null, NO_MAX_SIZE, keyGenerator, storages);
	}

	/**
	 * {@code maxCapacity} defaults to {@link IStorage#NO_MAX_SIZE}.
	 *
	 * @see ChainedVStorage#ChainedVStorage(Map, long, Supplier, IStorage[])
	 */
	public ClusteredVStorage(Long maxCapacity, IStorage<K, V>... storages) {
		this(null, maxCapacity, null, storages);
	}

	/**
	 * {@code maxCapacity} defaults to {@link IStorage#NO_MAX_SIZE}.
	 *
	 * @see ChainedVStorage#ChainedVStorage(Map, long, Supplier, IStorage[])
	 */
	public ClusteredVStorage(IStorage<K, V>... storages) {
		this(null, NO_MAX_SIZE, null, storages);
	}

	/**
	 * {@code maxCapacity} defaults to {@link IStorage#NO_MAX_SIZE}.
	 *
	 * @see ClusteredVStorage#ClusteredVStorage(Map, long, Supplier, IStorage[])
	 */
	public ClusteredVStorage(Long maxCapacity, Supplier<K> keyGenerator, IStorage<K, V>... storages) {
		this(null, maxCapacity, keyGenerator, storages);
	}

	/**
	 * Sets the keygenerator
	 *
	 * @param value
	 */
	@Override
	public void setKeyGenerator(Supplier<K> value) {
		this.keyGenerator = value;
	}

	/**
	 * Gets the number of entries currently available in the storage
	 *
	 * @return
	 */
	@Override
	public Long entries() {
		return this.entries;
	}

	/**
	 * Adds the value to the first not full storage
	 *
	 * @param value
	 * @return
	 */
	@Override
	public K create(V value) {
		if (this.isFull()) {
			throw new OutOfSpaceException();
		}
		K key = this.keyGenerator.get();
		this.update(key, value);
		return key;
	}

	/**
	 * Deletes the key value pair from the storage
	 *
	 * @param key
	 * @return
	 */
	@Override
	public V read(Object key) {
		IStorage<K, V> storage = this.select(key);
		return storage.read(key);
	}

	/**
	 * If any storage has the key, then it will updates that
	 * if no one has it, than the first not full will have it.
	 *
	 * @param key
	 * @param value
	 * @throws OutOfSpaceException - if the element is new, and the storage is full
	 */
	@Override
	public void update(K key, V value) {
		IStorage<K, V> storage = this.select(key);
		boolean inserted = false;
		if (!storage.has(key)) {
			if (this.isFull()) {
				throw new OutOfSpaceException();
			}
			inserted = true;
		}

		storage.update(key, value);

		if (inserted) {
			++this.entries;
		}
	}

	/**
	 * Deletes the key value pair from the storage
	 *
	 * @param key
	 * @return
	 */
	@Override
	public void delete(Object key) {
		IStorage<K, V> storage = this.select(key);
		boolean deleted = storage.has(key);
		storage.delete(key);
		if (deleted) {
			--this.entries;
		}
	}

	/**
	 * If any storage has the value it returns true, otherwise it returns false
	 *
	 * @param key
	 * @return
	 */
	@Override
	public boolean has(Object key) {
		IStorage<K, V> storage = this.select(key);
		return storage.has(key);
	}

	/**
	 * Returns true if the number of entries is equal to 0, false otherwise
	 *
	 * @return
	 */
	@Override
	public boolean isEmpty() {
		return this.entries == 0;
	}

	/**
	 * Returns true if the number of entries has reached the capacity.
	 * If the capacity has no limitation it always returns false.
	 *
	 * @return
	 */
	@Override
	public boolean isFull() {
		if (this.capacity == NO_MAX_SIZE) {
			return false;
		}
		return this.capacity <= this.entries;
	}

	/**
	 * Clear all underlying storages and reset the number of entries
	 */
	@Override
	public void clear() {
		for (IStorage<K, V> storage : this.storages) {
			storage.clear();
		}
		this.entries = 0L;
	}


	/**
	 * @param key1
	 * @param key2
	 */
	@Override
	public void swap(K key1, K key2) {
		IStorage<K, V> storage1 = this.select(key1);
		IStorage<K, V> storage2 = this.select(key2);

		if (storage1 == null || storage2 == null) {
			String message = String.format("key %s or key %s is not found in any storage", key1.toString(), key2.toString());
			throw new UnsupportedOperationException(message);
		}
		if (storage1 == storage2) {
			storage1.swap(key1, key2);
			return;
		}
		V value1 = storage1.read(key1);
		V value2 = storage2.read(key2);
		storage1.update(key1, value2);
		storage2.update(key2, value1);
	}


	private IStorage<K, V> select(Object key) {
		if (key == null) {
			return this.storages.get(0);
		}
		int index = Math.abs(key.hashCode()) % this.storages.size();
		return this.storages.get(index);
	}

	/**
	 * Returns with an iterator iterates through the key value pairs of the underlying storages
	 *
	 * @return
	 */
	@Override
	public Iterator<Map.Entry<K, V>> iterator() {
		return new ClusteredVStorageIterator();
	}

	private class ClusteredVStorageIterator<E> implements Iterator<Map.Entry<K, V>> {

		private boolean checked = false;
		private int consumedStorages = 0;
		private Iterator<Map.Entry<K, V>> actual;

		ClusteredVStorageIterator() {
			IStorage<K, V> storage = ClusteredVStorage.this.storages.get(this.consumedStorages);
			actual = storage.iterator();
		}

		@Override
		public boolean hasNext() {
			this.check();
			return actual.hasNext();
		}

		@Override
		public Map.Entry<K, V> next() {
			this.check();
			Map.Entry<K, V> result = this.actual.next();
			this.checked = false;
			return result;
		}

		private void check() {
			if (this.checked) {
				return;
			}
			if (actual.hasNext()) {
				return;
			}
			if (ClusteredVStorage.this.storages.size() <= this.consumedStorages + 1) {
				return;
			}
			++this.consumedStorages;
			IStorage<K, V> storage = ClusteredVStorage.this.storages.get(this.consumedStorages);
			actual = storage.iterator();
			this.checked = true;
		}

		@Override
		public void remove() {
			this.actual.remove();
		}

		@Override
		public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
			this.actual.forEachRemaining(action);
		}
	}
}
