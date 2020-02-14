package com.wobserver.vcollections.storages;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;

// TODO: Refine!
class RedisConnection<K, V> {
	private RedisClient client;
	private StatefulRedisConnection<K, V> statefulRedisConnection;
	private final RedisURI uri;
	private RedisCommands<K, V> syncCommands;
	private RedisAsyncCommands<K, V> asyncCommands;
	private final RedisMapper<K, V> mapper;

	public RedisConnection(RedisURI uri, RedisMapper<K, V> mapper) {
		this.uri = uri;
		this.mapper = mapper;
	}

	public void start() {
		this.connect();
	}

	public void stop() {

	}

	public RedisCommands<K, V> sync() {
		if (this.syncCommands == null || !this.syncCommands.isOpen()) {
			this.connect();
		}
		return this.syncCommands;
	}

	public RedisAsyncCommands<K, V> async() {
		if (this.asyncCommands == null || !this.syncCommands.isOpen()) {
			this.connect();
		}
		return this.asyncCommands;
	}

	private void connect() {
		this.client = RedisClient.create(this.uri);
		this.statefulRedisConnection = client.connect(this.mapper);
		this.syncCommands = statefulRedisConnection.sync();
		this.asyncCommands = statefulRedisConnection.async();
	}


}