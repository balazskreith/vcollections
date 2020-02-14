package com.wobserver.vcollections.storages.query;

import java.util.LinkedList;
import java.util.List;
import com.wobserver.vcollections.storages.IStorage;

public abstract class StorageQuery<T, K, V> implements ListQuery<T, V> {

	private final IStorage<K, V> storage;

	public StorageQuery(IStorage<K, V> storage) {
		this.storage = storage;
	}

	@Override
	public List<V> apply(T value) {
		List<K> keys = this.map(value);
		if (keys == null) {
			return null;
		}
		List<V> results = new LinkedList<>();
		for (K key : keys) {
			V item = this.storage.read(key);
			results.add(item);
		}
		return results;
	}

	protected abstract List<K> map(T value);
}
