package storages;

import java.util.*;
import java.util.function.Supplier;

public class ReplicatedVStorage<K, V> implements IStorage<K, V>, ISetKeyGenerator<K> {

	private Supplier<K> keyGenerator;
	private List<IStorage<K, V>> storages;
	private Long capacity;
	private long entries;

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
	public ReplicatedVStorage(Map<K, V> initialEntries, long maxCapacity, Supplier<K> keyGenerator, IStorage<K, V>... storages) {
		if (storages == null || storages.length < 1) {
			throw new NotAvailableStorage("Number of underlying storage must be greater than 0");
		}
		this.storages = Arrays.asList(storages);
		Optional<IStorage<K, V>> firstUnlimited = this.storages.stream().filter(s -> s.capacity() != NO_MAX_SIZE).findFirst();
		if (!firstUnlimited.isEmpty()) {
			throw new IllegalStateException("All of the underlying storage must have unbounded capacity");
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
	 * @see ReplicatedVStorage#ReplicatedVStorage(Map, long, Supplier, IStorage[])
	 */
	public ReplicatedVStorage(Supplier<K> keyGenerator, IStorage<K, V>... storages) {
		this(null, NO_MAX_SIZE, keyGenerator, storages);
	}

	/**
	 * {@code maxCapacity} defaults to {@link IStorage#NO_MAX_SIZE}.
	 *
	 * @see ReplicatedVStorage#ReplicatedVStorage(Map, long, Supplier, IStorage[])
	 */
	public ReplicatedVStorage(Long maxCapacity, IStorage<K, V>... storages) {
		this(null, maxCapacity, null, storages);
	}

	/**
	 * {@code maxCapacity} defaults to {@link IStorage#NO_MAX_SIZE}.
	 *
	 * @see ReplicatedVStorage#ReplicatedVStorage(Map, long, Supplier, IStorage[])
	 */
	public ReplicatedVStorage(IStorage<K, V>... storages) {
		this(null, NO_MAX_SIZE, null, storages);
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
	 * Reads the value from the first storage has the key
	 * If the key has not been found the storage returns null.
	 *
	 * <b>Note</b>, that it returns null if the value to the key is null.
	 *
	 * @param key
	 * @return
	 */
	@Override
	public V read(Object key) {
		for (IStorage<K, V> storage : this.storages) {
			if (storage.has(key)) { // fixme
				return storage.read(key);
				// pluggable actions in case of inconsistency or throw an exception
			}
		}
		return null;
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
		boolean inserted = false;
		if (!this.has(key)) {
			if (this.isFull()) {
				throw new OutOfSpaceException("Storage is full, new element cannot be inserted");
			}
			inserted = true;
		}
		for (IStorage<K, V> storage : this.storages) {
			storage.update(key, value);
		}
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
		boolean deleted = false;
		boolean proceed = false;
		for (IStorage<K, V> storage : this.storages) {
			if (!proceed) {
				deleted = storage.has(key);
				proceed = true;
			}
			storage.delete(key);
		}
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
		for (IStorage<K, V> storage : this.storages) {
			if (storage.has(key)) {
				return true;
			}
		}
		return false;
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
		for (IStorage<K, V> storage : this.storages) {
			storage.swap(key1, key2);
		}
	}

	/**
	 * Returns with an iterator iterates through the key value pairs of the underlying storages
	 *
	 * @return
	 */
	@Override
	public Iterator<Map.Entry<K, V>> iterator() {
		return new ReplicatedVStorage.SequentialStorageIterator();
	}

	private class SequentialStorageIterator<E> implements Iterator<Map.Entry<K, V>> {
		private Iterator<Map.Entry<K, V>> iterator;
		private Map.Entry<K, V> actual = null;

		SequentialStorageIterator() {
			this.iterator = ReplicatedVStorage.this.storages.get(0).iterator();
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public Map.Entry<K, V> next() {
			this.actual = this.iterator.next();
			return this.actual;
		}

		@Override
		public void remove() {
			if (this.actual == null) {
				return;
			}
			for (IStorage<K, V> storage : ReplicatedVStorage.this.storages) {
				storage.delete(this.actual.getKey());
			}
		}
	}
}
