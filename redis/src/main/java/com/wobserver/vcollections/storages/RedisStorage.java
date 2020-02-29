package com.wobserver.vcollections.storages;

import com.wobserver.vcollections.keygenerators.IAccessKeyGenerator;
import com.wobserver.vcollections.keygenerators.IKeyGenerator;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.RedisURI;
import io.lettuce.core.ScanArgs;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class RedisStorage<K, V> implements IStorage<K, V>, IAccessKeyGenerator<K> {

	private IKeyGenerator<K> keyGenerator;
	private final int expirationInS;
	private final long capacity;
	private final K nullKey;
	private final V nullValue;
	private final Function<Object, K> keyConverter;
	private final CapacityChecker<K> capacityChecker;
	private final RedisConnection<K, V> connection;
	private Function<K, V> getter;
	private BiConsumer<K, V> setter;

	public RedisStorage(RedisURI uri, RedisMapper<K, V> mapper, int expirationInS, long capacity, K nullKey, V nullValue, Function<Object, K> keyConverter) {
		this.capacity = capacity;
		this.expirationInS = expirationInS;
		this.nullKey = nullKey;
		this.nullValue = nullValue;
		this.keyConverter = keyConverter;
		this.connection = new RedisConnection<>(uri, mapper);
		this.capacityChecker = new CapacityChecker<>(this, capacity);
		if (0 < expirationInS) {
			this.setter = (key, value) -> this.connection.sync().setex(key, expirationInS, value);
		} else {
			this.setter = this.connection.sync()::set;
		}
	}

	@Override
	public boolean isEmpty() {
		return this.entries() == 0;
	}

	@Override
	public boolean isFull() {
		if (this.capacity == NO_MAX_SIZE) {
			return false;
		}
		return this.capacity <= this.entries();
	}

	@Override
	public Long entries() {
		return this.connection.sync().dbsize();
	}

	@Override
	public Long capacity() {
		return this.capacity;
	}

	@Override
	public K create(V value) {
		this.capacityChecker.checkForCreate();

		if (this.keyGenerator == null) {
			throw new UnsupportedOperationException("Create operation without keyGenerator is not supported.");
		}

		K key = this.keyGenerator.get();
		this.update(key, value);
		return key;
	}

	@Override
	public V read(Object keyObject) {
		K key;
		if (keyObject == null) {
			key = this.nullKey;
		} else {
			key = this.keyConverter.apply(keyObject);
		}
		V value = this.connection.sync().get(key);
		if (this.nullValue.equals(value)) {
			return null;
		} else {
			return value;
		}
	}

	@Override
	public void update(K key, V value) {
		this.capacityChecker.checkForUpdate(key);

		if (key == null) {
			key = this.nullKey;
		}
		if (value == null) {
			value = nullValue;
		}
		this.connection.sync().set(key, value);
	}

	@Override
	public boolean has(Object keyObject) {
		K key;
		if (keyObject == null) {
			key = this.nullKey;
		} else {
			key = this.keyConverter.apply(keyObject);
		}
		return this.connection.sync().exists(key) == 1;
	}

	@Override
	public void delete(Object keyObject) {
		K key;
		if (keyObject == null) {
			key = this.nullKey;
		} else {
			key = this.keyConverter.apply(keyObject);
		}
		this.connection.sync().del(key);
	}

	@Override
	public void swap(K key1, K key2) {
		if (key1 == null) {
			key1 = this.nullKey;
		}
		if (key2 == null) {
			key2 = this.nullKey;
		}

		if (!this.has(key1) || !this.has(key2)) {
			throw new KeyNotFoundException();
		}
		V value1 = this.read(key1);
		this.connection.sync().multi();
		this.connection.sync().del(key1);
		this.connection.sync().rename(key2, key1);
		this.connection.sync().set(key2, value1);
		this.connection.sync().exec();

	}

	@Override
	public void clear() {
		this.connection.async().flushall();
	}

	@Override
	public Iterator<Map.Entry<K, V>> iterator() {
		return new RedisIterator();
	}

	@Override
	public void setKeyGenerator(IKeyGenerator<K> value) {
		this.keyGenerator = value;
	}

	@Override
	public IKeyGenerator<K> getKeyGenerator() {
		return this.keyGenerator;
	}

	private class RedisIterator implements Iterator<Map.Entry<K, V>> {
		private KeyScanCursor<K> cursor;
		private Queue<K> keys;

		RedisIterator() {
			this.keys = null;
			this.cursor = connection.sync().scan(ScanArgs.Builder.limit(50));
			this.keys = new LinkedList<>(cursor.getKeys());
		}

		public final boolean hasNext() {
			return !this.keys.isEmpty() || !this.cursor.isFinished();
		}

		@Override
		public Map.Entry<K, V> next() {
			if (this.keys.isEmpty()) {
				this.cursor = RedisStorage.this.connection.sync().scan(cursor);
			}
			K key = this.keys.poll();
			V value = RedisStorage.this.read(key);
			if (RedisStorage.this.nullKey.equals(key)) {
				key = null;
			}
			return new AbstractMap.SimpleEntry<>(key, value);
		}

		@Override
		public final void remove() {

		}

		@Override
		public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {

		}
	}
}
