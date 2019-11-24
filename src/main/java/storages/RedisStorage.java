package storages;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RedisStorage<K, V> implements IStorage<K, V> {

	private static final int MAX_TRIES = 3;

	private long maxSize = NO_MAX_SIZE;
	private final ObjectMapper keyMapper;
	private final Class<K> keyType;
	private final ObjectMapper valueMapper;
	private final Class<V> valueType;
	private Supplier<K> keyGenerator;
	private final RedisClient redisClient;
	private final RedisCommands<String, String> syncCommands;
	private final RedisAsyncCommands<String, String> asyncCommands;
	private long length;
	private final int expirationInS;

	public RedisStorage(RedisClient redisClient, Class<K> keyType, ObjectMapper keyMapper, Class<V> valueType, ObjectMapper valueMapper, int expirationInS) {
		this.keyType = keyType;
		this.keyMapper = keyMapper;
		this.valueType = valueType;
		this.valueMapper = valueMapper;
		this.redisClient = redisClient;
		this.expirationInS = expirationInS;

		StatefulRedisConnection<String, String> commandConnection = redisClient.connect();
		this.syncCommands = commandConnection.sync();
		this.asyncCommands = commandConnection.async();
		this.length = this.syncCommands.dbsize();
	}

	@Override
	public boolean isEmpty() {
		return this.length == 0;
	}

	@Override
	public boolean isFull() {
		if (this.maxSize == NO_MAX_SIZE) {
			return false;
		}
		return this.maxSize < this.length;
	}

	@Override
	public Long entries() {
		return this.length;
	}

	@Override
	public Long capacity() {
		return this.maxSize;
	}

	@Override
	public K create(V value) {
		if (this.keyGenerator == null) {
			throw new UnsupportedOperationException("Create operation without keyGenerator is not supported.");
		}
		K key = this.keyGenerator.get();
		this.update(key, value);
		return key;
	}

	@Override
	public V read(Object key) {
		String serializedValue = this.syncCommands.get(key.toString());
		if (serializedValue == null) {
			return null;
		}
		try {
			return this.valueMapper.readValue(serializedValue, this.valueType);
		} catch (
				IOException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public void update(K key, V value) {
		ByteArrayOutputStream valueStream = new ByteArrayOutputStream();
		try {
			this.valueMapper.writeValue(valueStream, value);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.asyncCommands.set(key.toString(), valueStream.toString());
	}

	@Override
	public boolean has(Object key) {
		ByteArrayOutputStream keyStream = new ByteArrayOutputStream();
		try {
			this.keyMapper.writeValue(keyStream, key);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return this.syncCommands.exists(keyStream.toString()) == 1;
	}

	@Override
	public void delete(Object key) {
		this.asyncCommands.del(key.toString());
	}

	@Override
	public void swap(K key1, K key2) {
		V value1 = this.read(key1);
		ByteArrayOutputStream key1Stream = new ByteArrayOutputStream();
		ByteArrayOutputStream key2Stream = new ByteArrayOutputStream();
		ByteArrayOutputStream value1Stream = new ByteArrayOutputStream();
		try {
			this.keyMapper.writeValue(key1Stream, key1);
			this.keyMapper.writeValue(key2Stream, key2);
			this.valueMapper.writeValue(value1Stream, value1);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.asyncCommands.del(key1Stream.toString());
		this.asyncCommands.rename(key2Stream.toString(), key1Stream.toString());
		this.asyncCommands.set(key2Stream.toString(), value1Stream.toString());
	}

	@Override
	public void clear() {
		this.asyncCommands.flushall();
	}

	@Override
	public Iterator<Map.Entry<K, V>> iterator() {
		return new RedisIterator();
	}

	// TODO: this is not working now, because we need an implementaiton to the current client we use
	private class RedisIterator implements Iterator<Map.Entry<K, V>> {
		private int consumedKeys;
		private String cursor;
		private List<String> keys;

		RedisIterator() {
			this.consumedKeys = 0;
			this.keys = null;

		}

		public final boolean hasNext() {
			if (this.keys == null) { // we have not started
				return true;
			}
			if (this.consumedKeys < this.keys.size()) { // we have some keys to consume
				return true;
			}
			return !this.cursor.equals("0"); // is this the last cursor?
		}

		@Override
		public Map.Entry<K, V> next() {
			K key = null;
			V value = null;
			if (this.keys == null || this.keys.size() <= this.consumedKeys) {
				if (this.keys != null) {
					this.keys.clear();
				}
				this.consumedKeys = 0;
			}
			String keyString = this.keys.get(this.consumedKeys);
			try {
				key = RedisStorage.this.keyMapper.readValue(keyString.getBytes(), RedisStorage.this.keyType);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			return new AbstractMap.SimpleEntry<K, V>(key, value);
		}

		@Override
		public final void remove() {

		}

		@Override
		public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {

		}

	}

}
