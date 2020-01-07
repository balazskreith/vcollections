package vcollections.storages;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import vcollections.keygenerators.IKeyGenerator;
import vcollections.keygenerators.IAccessKeyGenerator;

public class LRUMemoryStorage<K, V> implements IStorage<K, V>, IAccessKeyGenerator<K> {

	private static final int NO_RETENTION_TIME = -1;
	private IKeyGenerator<K> keyGenerator = null;
	private int retentionInMs;
	private Long capacity = NO_MAX_SIZE;
	private LinkedHashMap<K, Item> map = new LinkedHashMap<>() {
		@Override
		protected boolean removeEldestEntry(Map.Entry<K, Item> eldest) {
			if (LRUMemoryStorage.this.capacity == NO_MAX_SIZE) {
				return false;
			}
			return LRUMemoryStorage.this.capacity < this.size();
		}
	};

	public LRUMemoryStorage(Long capacity, int retentionInMs) {
		this.capacity = capacity;
		this.retentionInMs = retentionInMs;
	}

	public LRUMemoryStorage(Long capacity) {
		this.capacity = capacity;
		this.retentionInMs = NO_RETENTION_TIME;
	}

	public LRUMemoryStorage(int retentionInMs) {
		this.retentionInMs = retentionInMs;
	}

	@Override
	public Long entries() {
		return Long.valueOf(this.map.size());
	}

	@Override
	public Long capacity() {
		return Long.valueOf(this.capacity);
	}

	@Override
	public K create(V value) {
		if (this.keyGenerator == null) {
			throw new NullPointerException();
		}
		K key = this.keyGenerator.get();
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
		if (this.retentionInMs == NO_RETENTION_TIME) {
			return result;
		}
		long elapsed = System.currentTimeMillis() - item.created;
		if (this.retentionInMs < elapsed) {
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
		if (this.retentionInMs == NO_RETENTION_TIME) {
			return true;
		}
		Item item = this.map.get(key);
		long elapsed = System.currentTimeMillis() - item.created;
		if (this.retentionInMs < elapsed) {
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
		if (this.capacity == NO_MAX_SIZE) {
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

	@Override
	public void setKeyGenerator(IKeyGenerator<K> value) {
		this.keyGenerator = value;
	}

	@Override
	public IKeyGenerator<K> getKeyGenerator() {
		return this.keyGenerator;
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