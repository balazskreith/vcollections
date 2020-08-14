package com.balazskreith.vcollections.storages;

import com.balazskreith.vcollections.activeconfigs.CachedStorageActiveConfig;
import java.util.Iterator;
import java.util.Map;

public class CachedStorage<K, V> implements IStorage<K, V>, Cache {

	private final CachedStorageActiveConfig<K, V> config;
	private final transient IStorage<K, V> superset;
	private final transient IStorage<K, V> subset;

	private transient long hits = 0;
	private transient long misses = 0;


	public CachedStorage(CachedStorageActiveConfig<K, V> config) {
		this.config = config;
		this.superset = config.superset.build();
		this.subset = config.subset.build();
	}

	@Override
	public Long entries() {
		return this.superset.entries();
	}

	@Override
	public Long capacity() {
		return this.superset.capacity();
	}

	@Override
	public K create(V value) {
		K result = this.superset.create(value);
		if (this.config.cacheOnCreate) {
			this.subset.update(result, value);
		}
		return result;
	}

	@Override
	public V read(Object key) {
		if (this.subset.has(key)) {
			++hits;
			return this.subset.read(key);
		}
		if (!this.superset.has(key)) {
			return null;
		}
		++misses;
		V result = this.superset.read(key);
		if (this.config.cacheOnRead) {
			K castedKey = (K) key;
			this.subset.update(castedKey, result);
		}
		return result;
	}

	@Override
	public void update(K key, V value) {
		this.superset.update(key, value);
		if (this.config.cacheOnUpdate) {
			this.subset.update(key, value);
		} else {
			this.subset.delete(key);
		}
	}

	@Override
	public void delete(Object key) {
		this.superset.delete(key);
		this.subset.delete(key);
	}

	@Override
	public boolean has(Object key) {
		if (this.subset.has(key)) {
			return true;
		}

		return this.superset.has(key);
	}

	@Override
	public boolean isEmpty() {
		return this.superset.isEmpty();
	}

	@Override
	public boolean isFull() {
		return this.superset.isFull();
	}

	@Override
	public void clear() {
		this.flush();
		this.superset.clear();
	}

	@Override
	public void swap(K key1, K key2) {
		this.subset.delete(key1);
		this.subset.delete(key2);
		this.superset.swap(key1, key2);
	}

	@Override
	public Iterator<Map.Entry<K, V>> iterator() {
		return this.superset.iterator();
	}

	@Override
	public void flush() {
		this.subset.clear();
		this.hits = this.misses = 0;
	}

	@Override
	public long hits() {
		return this.hits;
	}

	@Override
	public long misses() {
		return this.misses;
	}

	public CachedStorageActiveConfig<K, V> getConfig() {
		return this.config;
	}
}
