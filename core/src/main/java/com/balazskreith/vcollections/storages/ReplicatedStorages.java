package com.balazskreith.vcollections.storages;

import com.balazskreith.vcollections.activeconfigs.ClusteredStoragesActiveConfig;
import com.balazskreith.vcollections.activeconfigs.ReplicatedStoragesActiveConfig;
import com.balazskreith.vcollections.builders.StorageBuilder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReplicatedStorages<K, V> implements IStorage<K, V> {

	public static <TK, TV> void synchronise(IStorage<TK, TV>... storages) {
		Map<TK, IStorage<TK, TV>> keyToStorage = new HashMap<>();
		for (int i = 0; i < storages.length; ++i) {
			IStorage<TK, TV> storage = storages[i];
			for (Iterator<Map.Entry<TK, TV>> it = storage.iterator(); it.hasNext(); ) {
				Map.Entry<TK, TV> entry = it.next();
				TK key = entry.getKey();
				if (!keyToStorage.containsKey(key)) {
					keyToStorage.put(key, storage);
				}
			}
		}

		for (Iterator<Map.Entry<TK, IStorage<TK, TV>>> it = keyToStorage.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<TK, IStorage<TK, TV>> entry = it.next();
			TK key = entry.getKey();
			TV value = null;
			IStorage<TK, TV> storage = entry.getValue();
			for (IStorage<TK, TV> subject : storages) {
				if (storage == subject) {
					continue;
				}
				if (!subject.has(key)) {
					if (value == null) {
						value = storage.read(key);
					}
					subject.update(key, value);
				}
			}
		}
	}

	private final ReplicatedStoragesActiveConfig<K, V> config;
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
	public ReplicatedStorages(ReplicatedStoragesActiveConfig<K, V> config) {
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
		for (IStorage<K, V> storage : this.storages) {
			storage.swap(key1, key2);
		}
	}

	/**
	 * Returns with an iterator iterates through the key value pairs of the underlying com.wobserver.vcollections.storages
	 *
	 * @return
	 */
	@Override
	public Iterator<Map.Entry<K, V>> iterator() {
		return new ReplicatedStoragesIterator();
	}

	private class ReplicatedStoragesIterator<E> implements Iterator<Map.Entry<K, V>> {
		private Iterator<Map.Entry<K, V>> iterator;
		private Map.Entry<K, V> actual = null;

		ReplicatedStoragesIterator() {
			this.iterator = ReplicatedStorages.this.storages.get(0).iterator();
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
			for (IStorage<K, V> storage : ReplicatedStorages.this.storages) {
				storage.delete(this.actual.getKey());
			}
		}
	}
}
