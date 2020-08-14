package com.balazskreith.vcollections.storages;

import com.balazskreith.vcollections.activeconfigs.LRUMemoryStorageActiveConfig;
import com.balazskreith.vcollections.builders.passiveconfigs.LRUMemoryStoragePassiveConfig;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUMemoryStorage<K, V> implements IStorage<K, V> {

	private final LRUMemoryStorageActiveConfig<K, V> config;

	private LinkedHashMap<K, Item> map = new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<K, Item> eldest) {
			if (LRUMemoryStorage.this.config.capacity == NO_MAX_SIZE) {
				return false;
			}
			return LRUMemoryStorage.this.config.capacity < this.size();
		}
	};

	public LRUMemoryStorage(LRUMemoryStorageActiveConfig<K, V> config) {
		this.config = config;
	}

	@Override
	public Long entries() {
		return Long.valueOf(this.map.size());
	}

	@Override
	public Long capacity() {
		return Long.valueOf(this.config.capacity);
	}

	@Override
	public K create(V value) {
		if (this.config.keySupplier == null) {
			throw new NullPointerException();
		}
		K key = this.config.keySupplier.get();
		Item item = new Item(value);
		this.map.put(key, item);
		return key;
	}

	@Override
	public V read(Object key) {
		Item item = this.map.get(key);
		if (item == null) {
			return null;
		}
		V result = item.value;
		if (this.config.retentionInMs == LRUMemoryStoragePassiveConfig.NO_RETENTION_TIME) {
			return result;
		}
		long elapsed = this.config.timeInMsProvider.get() - item.created;
		if (this.config.retentionInMs < elapsed) {
			this.map.remove(key);
			return null;
		}
		return result;
	}

	@Override
	public void update(K key, V value) {
		Item item = new Item(value);
		this.map.put(key, item);
	}

	@Override
	public void delete(Object key) {
		this.map.remove(key);
	}

	@Override
	public boolean has(Object key) {
		if (!this.map.containsKey(key)) {
			return false;
		}
		if (this.config.retentionInMs == LRUMemoryStoragePassiveConfig.NO_RETENTION_TIME) {
			return true;
		}
		Item item = this.map.get(key);
		long elapsed = System.currentTimeMillis() - item.created;
		if (this.config.retentionInMs < elapsed) {
			this.map.remove(key);
			return false;
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	@Override
	public boolean isFull() {
		if (this.config.capacity == NO_MAX_SIZE) {
			return false;
		}
		return this.capacity() <= this.entries();
	}

	@Override
	public void clear() {
		this.map.clear();
	}

	@Override
	public void swap(K key1, K key2) {
		if (!this.has(key1)) {
			throw new KeyNotFoundException("key " + key1.toString() + " does not exists.");
		}
		if (!this.has(key2)) {
			throw new KeyNotFoundException("key " + key2.toString() + " does not exists.");
		}
		this.map.put(key1, this.map.put(key2, this.map.get(key1)));
	}

	@Override
	public Iterator<Map.Entry<K, V>> iterator() {
		return new Iterator<Map.Entry<K, V>>() {
			Iterator<Map.Entry<K, Item>> iterator = LRUMemoryStorage.this.map.entrySet().iterator();

			@Override
			public boolean hasNext() {
				return this.iterator.hasNext();
			}

			@Override
			public Map.Entry<K, V> next() {
				Map.Entry<K, Item> entry = iterator.next();
				return new AbstractMap.SimpleEntry(entry.getKey(), entry.getValue().value);
			}
		};
	}

	private class Item {
		public final long created;
		public final V value;

		private Item(V value) {
			this.created = System.currentTimeMillis();
			this.value = value;
		}
	}

}