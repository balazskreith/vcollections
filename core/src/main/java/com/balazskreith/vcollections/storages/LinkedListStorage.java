package com.balazskreith.vcollections.storages;

import com.balazskreith.vcollections.VLinkedListNode;
import com.balazskreith.vcollections.VLinkedListNodeKeys;
import com.balazskreith.vcollections.activeconfigs.LinkedListStorageActiveConfig;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class LinkedListStorage<K, V> implements IStorage<K, VLinkedListNode<K, V>> {

	private final IStorage<K, VLinkedListNodeKeys<K>> keys;
	private final IStorage<K, V> values;
	private final LinkedListStorageActiveConfig<K, V> config;


	public LinkedListStorage(LinkedListStorageActiveConfig<K, V> config) {
		this.config = Objects.requireNonNull(config);
		this.keys = config.keysStorageBuilder.build();
		this.values = config.valuesStorageBuilder.build();
	}

	@Override
	public Long entries() {
		return this.values.entries();
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
		return this.config.capacity <= this.values.entries();
	}

	public LinkedListStorageActiveConfig<K, V> getConfig() {
		return this.config;
	}

	@Override
	public void clear() {
		this.values.clear();
		this.keys.clear();
	}

	@Override
	public boolean has(Object key) {
		return this.keys.has(key);
	}

	@Override
	public boolean isEmpty() {
		return this.keys.isEmpty();
	}

	@Override
	public K create(VLinkedListNode<K, V> node) {
		if (this.config.keySupplier == null) {
			throw new NullPointerException();
		}
		if (this.isFull()) {
			throw new OutOfSpaceException();
		}
		K key = this.config.keySupplier.get();
		VLinkedListNodeKeys<K> keys = new VLinkedListNodeKeys<K>(node.prev, node.next);
		this.keys.update(key, keys);
		this.values.update(key, node.value);
		return key;
	}

	@Override
	public VLinkedListNode<K, V> read(Object key) {
		VLinkedListNodeKeys<K> keys = this.keys.read(key);
		if (keys == null) {
			return null;
		}
		V value = this.values.read(key);
		if (value == null) {
			throw new IllegalStateException("Key and Value must exists in both storage (keysStorage, and valuesStorage)");
		}
		return new VLinkedListNode<K, V>(keys.prev, keys.next, value);
	}

	@Override
	public void update(K key, VLinkedListNode<K, V> node) {
		if (this.isFull() && !this.has(key)) {
			throw new OutOfSpaceException();
		}
		VLinkedListNodeKeys<K> keys = new VLinkedListNodeKeys<K>(node.prev, node.next);
		this.keys.update(key, keys);
		this.values.update(key, node.value);
	}

	@Override
	public void swap(K key1, K key2) {
		if (!this.has(key1)) {
			throw new KeyNotFoundException("key" + key1.toString() + " does not exists.");
		}
		if (!this.has(key2)) {
			throw new KeyNotFoundException("key" + key2.toString() + " does not exists.");
		}
		this.keys.swap(key1, key2);
		this.values.swap(key1, key2);
	}

	@Override
	public void delete(Object key) {
		this.keys.delete(key);
		this.values.delete(key);
	}

	@Override
	public Iterator<Map.Entry<K, VLinkedListNode<K, V>>> iterator() {
		Iterator<Map.Entry<K, VLinkedListNodeKeys<K>>> keysIt = this.keys.iterator();
		return new Iterator<Map.Entry<K, VLinkedListNode<K, V>>>() {
			@Override
			public boolean hasNext() {
				return keysIt.hasNext();
			}

			@Override
			public Map.Entry<K, VLinkedListNode<K, V>> next() {
				Map.Entry<K, VLinkedListNodeKeys<K>> keysEntry = keysIt.next();
				K key = keysEntry.getKey();
				return new Map.Entry<K, VLinkedListNode<K, V>>() {
					@Override
					public K getKey() {
						return key;
					}

					@Override
					public VLinkedListNode<K, V> getValue() {
						return LinkedListStorage.this.read(key);
					}

					@Override
					public VLinkedListNode<K, V> setValue(VLinkedListNode<K, V> value) {
						LinkedListStorage.this.update(key, value);
						return value;
					}
				};
			}
		};
	}
}
