package com.wobserver.vcollections.storages;

import java.util.Iterator;
import java.util.Map;

public class CachedStorage<K, V> implements IStorage<K, V>, ICache {

	private final IStorage<K, V> superset;
	private final IStorage<K, V> subset;
	private final Class<K> keyType;
	private long hits = 0;
	private long misses = 0;

	private boolean onCreate = false;
	private boolean onRead = true;
	private boolean onUpdate = false;

	public CachedStorage(Class<K> keyType, IStorage<K, V> superset, IStorage<K, V> subset) {
		this.superset = superset;
		this.subset = subset;
		this.keyType = keyType;
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
		if (this.onCreate) {
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
		if (this.onRead) {
			if (keyType.isInstance(key)) {
				K castedKey = (K) key;
				this.subset.update(castedKey, result);
			}
		}
		return result;
	}

	@Override
	public void update(K key, V value) {
		this.superset.update(key, value);
		if (this.onUpdate) {
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

	@Override
	public void doCache(boolean onCreate, boolean onRead, boolean onUpdate) {
		this.onCreate = onCreate;
		this.onRead = onRead;
		this.onUpdate = onUpdate;
	}
}
