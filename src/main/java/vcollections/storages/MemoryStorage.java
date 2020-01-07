package vcollections.storages;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import vcollections.keygenerators.IKeyGenerator;
import vcollections.keygenerators.IAccessKeyGenerator;

public class MemoryStorage<K, V> implements IStorage<K, V>, IAccessKeyGenerator<K> {

	private IKeyGenerator<K> keyGenerator;
//	private Supplier<K> keyGenerator;
	private Map<K, V> map;
	private final Long maxSize;


	public MemoryStorage(IKeyGenerator<K> keyGenerator, Map<K, V> map, Long maxSize) {
		this.map = new HashMap<>();
		this.maxSize = maxSize == null ? IStorage.NO_MAX_SIZE : maxSize;
		this.keyGenerator = keyGenerator;
		if (map != null) {
			this.map.putAll(map);
		}
	}

//	public MemoryStorage(Supplier<K> keyGenerator, Long maxSize) {
//		this(keyGenerator, new HashMap<>(), maxSize);
//	}
//
//	public MemoryStorage(Supplier<K> keyGenerator) {
//		this(keyGenerator, new HashMap<>(), NO_MAX_SIZE);
//	}
//
//	public MemoryStorage(Map<K, V> items, Long maxSize) {
//		this(null, items, maxSize);
//	}
//
//	public MemoryStorage(Long maxSize) {
//		this(null, new HashMap<>(), maxSize);
//	}
//
//	public MemoryStorage() {
//		this(null, new HashMap<>(), NO_MAX_SIZE);
//	}

	@Override
	public Long entries() {
		return Long.valueOf(this.map.size());
	}

	@Override
	public Long capacity() {
		return this.maxSize;
	}

	@Override
	public boolean isFull() {
		if (this.maxSize == NO_MAX_SIZE) {
			return false;
		}
		return this.maxSize <= this.map.size();
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
		if (this.keyGenerator == null) {
			throw new NullPointerException();
		}
		if (this.isFull()) {
			throw new OutOfSpaceException();
		}
		K key = this.keyGenerator.get();
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

	@Override
	public void setKeyGenerator(IKeyGenerator<K> value) {
		this.keyGenerator = value;
	}

	@Override
	public IKeyGenerator<K> getKeyGenerator() {
		return this.keyGenerator;
	}

}
