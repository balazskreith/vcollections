package com.balazskreith.vcollections.storages;

import com.balazskreith.vcollections.activeconfigs.MemoryStorageActiveConfig;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class MemoryStorage<K, V> implements IStorage<K, V> {

	private Map<K, V> map;
	private final MemoryStorageActiveConfig<K, V> config;


	public MemoryStorage(MemoryStorageActiveConfig<K, V> config) {
		this.config = Objects.requireNonNull(config);
		this.map = new HashMap<>();
	}

	@Override
	public Long entries() {
		return Long.valueOf(this.map.size());
	}

	@Override
	public Long capacity() {
		return this.config.capacity;
	}

	@Override
	public boolean isFull() {
		if (this.config.capacity == NO_MAX_SIZE) {
			return false;
		}
		return this.config.capacity <= this.map.size();
	}

	public MemoryStorageActiveConfig<K, V> getConfig() {
		return this.config;
	}

	@Override
	public void clear() {
		this.map.clear();
	}

	@Override
	public boolean has(Object key) {
		return this.map.containsKey(key);
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	@Override
	public K create(V value) {
		if (this.config.keySupplier == null) {
			throw new NullPointerException();
		}
		if (this.isFull()) {
			throw new OutOfSpaceException();
		}
		K key = this.config.keySupplier.get();
		this.map.put(key, value);
		return key;
	}

	@Override
	public V read(Object key) {
		return this.map.get(key);
	}

	@Override
	public void update(K key, V value) {
		if (this.isFull() && !this.has(key)) {
			throw new OutOfSpaceException();
		}
		this.map.put(key, value);
	}

	@Override
	public void swap(K key1, K key2) {
		if (!this.has(key1)) {
			throw new KeyNotFoundException("key" + key1.toString() + " does not exists.");
		}
		if (!this.has(key2)) {
			throw new KeyNotFoundException("key" + key2.toString() + " does not exists.");
		}
		this.map.put(key1, this.map.put(key2, this.map.get(key1)));
	}

	@Override
	public void delete(Object key) {
		this.map.remove(key);
	}

	@Override
	public Iterator<Map.Entry<K, V>> iterator() {
		return this.map.entrySet().iterator();
	}
}
