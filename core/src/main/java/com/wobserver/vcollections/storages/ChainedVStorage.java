package com.wobserver.vcollections.storages;

import com.wobserver.vcollections.keygenerators.IKeyGenerator;
import java.util.*;
import java.util.function.Consumer;
import com.wobserver.vcollections.keygenerators.IAccessKeyGenerator;

/**
 * Chains hte com.wobserver.vcollections.storages passed to supervise by this type of virtual storage
 * <p>
 * Advantage: you can add limitations to each storage, but the overall storage will be the accumulated limitations
 *
 * @param <K>
 * @param <V>
 */
public class ChainedVStorage<K, V> implements IStorage<K, V>, IAccessKeyGenerator<K> {

	private List<IStorage<K, V>> storages;
	private Long capacity = 0L;
	private Long entries = 0L;
	private IKeyGenerator<K> keyGenerator;

	/**
	 * @param initialEntries the initial entries for the chained storage will be
	 * @param maxCapacity    The maxmimal capacity of this storage
	 * @param keyGenerator   keygenerator
	 * @param storages
	 * @throws NotAvailableStorage   - if the number of underlying storage is less than 1
	 *                               - if the maxCapacity is less than 1
	 * @throws IllegalStateException - if there is no storage, which does not have a limitation
	 *                               - if the unlimited storage is not the last one
	 *                               - if there is not available space in the underlying com.wobserver.vcollections.storages
	 */
	public ChainedVStorage(Map<K, V> initialEntries, long maxCapacity, IKeyGenerator<K> keyGenerator, IStorage<K, V>... storages) {
		if (storages == null || storages.length < 1) {
			throw new NotAvailableStorage("Number of underlying storage must be greater than 0");
		}
		this.storages = Arrays.asList(storages);
		Optional<Long> availableSpace = this.storages.stream().map(s -> s.capacity()).reduce((r, v) -> r + v);
		if (availableSpace.get() < 1L) {
			throw new IllegalStateException("There is no available space in the storage");
		}
		Optional<IStorage<K, V>> firstUnlimited = this.storages.stream().filter(s -> s.capacity() == NO_MAX_SIZE).findFirst();
		if (!firstUnlimited.isPresent()) {
			throw new IllegalStateException("There must be a storage, which does not have a limitation");
		}
		IStorage<K, V> lastStorage = this.storages.get(this.storages.size() - 1);
		if (lastStorage != firstUnlimited.get()) {
			throw new IllegalStateException("Only the last storage amongst the defined one can be unlimited");
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
	 * @see ChainedVStorage#ChainedVStorage(Map, long, IKeyGenerator, IStorage[])
	 */
	public ChainedVStorage(IKeyGenerator<K> keyGenerator, IStorage<K, V>... storages) {
		this(null, NO_MAX_SIZE, keyGenerator, storages);
	}

	/**
	 * {@code maxCapacity} defaults to {@link IStorage#NO_MAX_SIZE}.
	 *
	 * @see ChainedVStorage#ChainedVStorage(Map, long, IKeyGenerator, IStorage[])
	 */
	public ChainedVStorage(Long maxCapacity, IKeyGenerator<K> keyGenerator, IStorage<K, V>... storages) {
		this(null, maxCapacity, keyGenerator, storages);
	}

	/**
	 * {@code maxCapacity} defaults to {@link IStorage#NO_MAX_SIZE}.
	 *
	 * @see ChainedVStorage#ChainedVStorage(Map, long, IKeyGenerator, IStorage[])
	 */
	public ChainedVStorage(Long maxCapacity, IStorage<K, V>... storages) {
		this(null, maxCapacity, null, storages);
	}

	/**
	 * {@code maxCapacity} defaults to {@link IStorage#NO_MAX_SIZE}.
	 *
	 * @see ChainedVStorage#ChainedVStorage(Map, long, IKeyGenerator, IStorage[])
	 */
	public ChainedVStorage(IStorage<K, V>... storages) {
		this(null, NO_MAX_SIZE, null, storages);
	}

	/**
	 * Sets the keygenerator
	 *
	 * @param value
	 */
	@Override
	public void setKeyGenerator(IKeyGenerator<K> value) {
		this.keyGenerator = value;
	}

	@Override
	public IKeyGenerator<K> getKeyGenerator() {
		return this.keyGenerator;
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
		for (IStorage<K, V> storage : this.storages) {
			if (storage.has(key)) {
				return storage.read(key);
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
		IStorage<K, V> firstAvailable = null;
		for (IStorage<K, V> storage : this.storages) {
			if (storage.has(key)) {
				storage.update(key, value);
				return;
			}
			if (storage.isFull()) {
				continue;
			}
			if (firstAvailable == null) {
				firstAvailable = storage;
			}
		}
		if (firstAvailable == null) {
			throw new IllegalStateException("There must be one storage underlying, which has infinite capacity");
		}
		if (this.isFull()) {
			throw new OutOfSpaceException("Storage is full, new element cannot be inserted");
		}
		firstAvailable.update(key, value);
		++this.entries;
	}

	/**
	 * Deletes the key value pair from the storage
	 *
	 * @param key
	 * @return
	 */
	@Override
	public void delete(Object key) {
		for (IStorage<K, V> storage : this.storages) {
			if (storage.has(key)) {
				storage.delete(key);
				--this.entries;
				return;
			}
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
	 * Clear all underlying com.wobserver.vcollections.storages and reset the number of entries
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
		IStorage<K, V> storage1 = null;
		IStorage<K, V> storage2 = null;
		for (IStorage<K, V> storage : this.storages) {
			if (storage.has(key1)) {
				storage1 = storage;
				if (storage2 != null) {
					break;
				}
			}
			if (storage.has(key2)) {
				storage2 = storage;
				if (storage1 != null) {
					break;
				}
			}
		}

		if (storage1 == null || storage2 == null) {
			String message = String.format("key %s or key %s is not found in any storage", key1.toString(), key2.toString());
			throw new KeyNotFoundException(message);
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

	/**
	 * Returns with an iterator iterates through the key value pairs of the underlying com.wobserver.vcollections.storages
	 *
	 * @return
	 */
	@Override
	public Iterator<Map.Entry<K, V>> iterator() {
		return new SequentialStorageIterator();
	}

	private class SequentialStorageIterator<E> implements Iterator<Map.Entry<K, V>> {
		private boolean modified = false;
		private boolean checked = false;
		private int consumedStorages = 0;
		private Iterator<Map.Entry<K, V>> actual;

		SequentialStorageIterator() {
			IStorage<K, V> storage = ChainedVStorage.this.storages.get(this.consumedStorages);
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
			this.modified = false;
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
			if (ChainedVStorage.this.storages.size() <= this.consumedStorages + 1) {
				return;
			}
			++this.consumedStorages;
			IStorage<K, V> storage = ChainedVStorage.this.storages.get(this.consumedStorages);
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
