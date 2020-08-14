package com.balazskreith.vcollections.storages;

import com.balazskreith.vcollections.activeconfigs.ClusteredStoragesActiveConfig;
import com.balazskreith.vcollections.builders.StorageBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Chains hte com.wobserver.vcollections.storages passed to supervise by this type of virtual storage
 * <p>
 * Advantage: you can add limitations to each storage, but the overall storage will be the accumulated limitations
 *
 * @param <K>
 * @param <V>
 */
public class ClusteredStorages<K, V> implements IStorage<K, V> {

	private final ClusteredStoragesActiveConfig<K, V> config;
	private transient final List<IStorage<K, V>> storages;
	private transient long entries;

	/**
	 * Create a {@link ClusteredStorages} based on the {@link ClusteredStoragesActiveConfig}.
	 *
	 * @param config the active config the based on the storage can be instantiated
	 * @throws NotAvailableStorage   if no storages has been specified
	 * @throws IllegalStateException If any underlying storage has a limitation, or the capacity of the {@link ClusteredStorages} is not
	 *                               applicable.
	 */
	public ClusteredStorages(ClusteredStoragesActiveConfig<K, V> config) {
		if (config.storageBuilders == null || config.storageBuilders.size() < 1) {
			throw new NotAvailableStorage("Number of underlying storage must be greater than 0");
		}
		this.config = config;
		this.storages = this.config.storageBuilders.stream().map(StorageBuilder::<K, V>build).collect(Collectors.toList());
		long notInfiniteStorageCapacity = this.storages.stream().filter(s -> s.capacity() != NO_MAX_SIZE).count();
		if (0 < notInfiniteStorageCapacity) {
			throw new IllegalStateException("All underlying storage must not have limitations.");
		}
		if (this.config.capacity < 1 && this.config.capacity != NO_MAX_SIZE) {
			throw new IllegalStateException("Max capacity is either unbound, or must be greater than 0");
		}
		this.entries = this.storages.stream().map(IStorage::entries).reduce(0L, (result, entries) -> result + entries);
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
		K key = this.config.keySupplier.get();
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
		if (this.config.capacity == NO_MAX_SIZE) {
			return false;
		}
		return this.config.capacity <= this.entries;
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
	 * Returns with an iterator iterates through the key value pairs of the underlying com.wobserver.vcollections.storages
	 *
	 * @return
	 */
	@Override
	public Iterator<Map.Entry<K, V>> iterator() {
		return new ClusteredStoragesIterator();
	}

	private class ClusteredStoragesIterator<E> implements Iterator<Map.Entry<K, V>> {

		private boolean checked = false;
		private int consumedStorages = 0;
		private Iterator<Map.Entry<K, V>> actual;

		ClusteredStoragesIterator() {
			IStorage<K, V> storage = ClusteredStorages.this.storages.get(this.consumedStorages);
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
			if (ClusteredStorages.this.storages.size() <= this.consumedStorages + 1) {
				return;
			}
			++this.consumedStorages;
			IStorage<K, V> storage = ClusteredStorages.this.storages.get(this.consumedStorages);
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
