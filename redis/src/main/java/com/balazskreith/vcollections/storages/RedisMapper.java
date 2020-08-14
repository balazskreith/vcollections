package com.balazskreith.vcollections.storages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import java.io.IOException;
import java.nio.ByteBuffer;

public class RedisMapper<K, V> implements RedisCodec<K, V> {
	private final Class<K> keyType;
	private final Class<V> valueType;
	private final ObjectMapper mapper;
	private final ByteArrayCodec byteArrayCodec;

	public RedisMapper(Class<K> keyType, Class<V> valueType, ObjectMapper mapper) {
		this.keyType = keyType;
		this.valueType = valueType;
		this.mapper = mapper;
		this.byteArrayCodec = new ByteArrayCodec();
	}

	@Override
	public K decodeKey(ByteBuffer byteBuffer) {
		byte[] bytes = this.byteArrayCodec.decodeKey(byteBuffer);
		return this.decode(bytes, this.keyType);
	}

	@Override
	public V decodeValue(ByteBuffer byteBuffer) {
		byte[] bytes = this.byteArrayCodec.decodeValue(byteBuffer);
		return this.decode(bytes, this.valueType);
	}

	@Override
	public ByteBuffer encodeKey(K key) {
		byte[] bytes = this.encode(key);
		return this.byteArrayCodec.encodeKey(bytes);
	}

	@Override
	public ByteBuffer encodeValue(V value) {
		byte[] bytes = this.encode(value);
		return this.byteArrayCodec.encodeKey(bytes);
	}

	public Class<K> getKeyType() {
		return this.keyType;
	}

	public Class<V> getValueType() {
		return this.valueType;
	}

	private <T> T decode(byte[] bytes, Class<T> type) {
		T result;
		try {
			result = this.mapper.readValue(bytes, type);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	private <T> byte[] encode(T value) {
		byte[] result;
		try {
			result = this.mapper.writeValueAsBytes(value);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
}