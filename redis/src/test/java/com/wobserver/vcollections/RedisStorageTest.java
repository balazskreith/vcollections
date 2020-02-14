package com.wobserver.vcollections;

import com.wobserver.vcollections.builders.RedisMapperBuilder;
import com.wobserver.vcollections.keygenerators.KeyGeneratorFactory;
import com.wobserver.vcollections.storages.IStorage;
import com.wobserver.vcollections.storages.RedisMapper;
import com.wobserver.vcollections.storages.RedisStorage;
import com.wobserver.vcollections.storages.StorageTest;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import redis.embedded.RedisServer;

class RedisStorageTest implements StorageTest<RedisStorage<String, String>> {

	private static final int redisPort = 6367;

	private RedisServer redisServer;

//	@BeforeAll
//	public static void startRedis() throws IOException {
//		redisServer = new RedisServer(redisPort);
//		redisServer.start();
//	}

	@BeforeEach
	public void startRedis() throws IOException {
		redisServer = new RedisServer(redisPort);
		redisServer.start();
	}

	@AfterEach
	public void stopRedis() {
		redisServer.stop();
	}

//	@BeforeEach
//	public void flushRedis() throws IOException {
//		RedisURI redisURI = RedisURI.builder()
//				.withHost("localhost")
//				.withPort(redisPort)
//				.withClientName("test")
//				.build();
//		RedisMapper<String, String> mapper = new RedisMapperBuilder()
//				.withKeyType(String.class.getName())
//				.withValueType(String.class.getName())
//				.build();
//		RedisClient client = RedisClient.create(redisURI);
//		StatefulRedisConnection<String, String> commandConnection = client.connect(mapper);
//		RedisCommands<String, String> syncCommands = commandConnection.sync();
//		syncCommands.flushall();
//	}
//
//	@AfterAll
//	public static void stopRedis() {
//		redisServer.stop();
//	}

	@Override
	public IStorage<String, String> makeStorage(long maxSize, String... items) {
		return makeStorage(maxSize, 0, items);

	}

	public IStorage<String, String> makeStorage(long maxSize, int retentionInS, String... items) {
		RedisURI redisURI = RedisURI.builder()
				.withHost("localhost")
				.withPort(redisPort)
				.withClientName("test")
				.build();
		RedisMapper<String, String> mapper = new RedisMapperBuilder()
				.withKeyType(String.class.getName())
				.withValueType(String.class.getName())
				.build();
		String nullKey = "null";
		String nullValue = "null";
		RedisClient client = RedisClient.create(redisURI);
		StatefulRedisConnection<String, String> commandConnection = client.connect(mapper);
		RedisCommands<String, String> syncCommands = commandConnection.sync();
		BiConsumer<String, String> setCommand = (key, value) -> {
			if (key == null) {
				key = nullKey;
			}
			if (value == null) {
				value = nullValue;
			}
			syncCommands.set(key, value);
		};
		if (items != null) {
			for (int i = 0; i + 1 < items.length; i += 2) {
				String key = items[i];
				String value = items[i + 1];
				setCommand.accept(key, value);
			}
		}
		KeyScanCursor<String> cursor = syncCommands.scan(ScanArgs.Builder.limit(50));

		while (true) {
			List<String> keys = cursor.getKeys();
			keys.forEach(key -> {
				System.out.println(key + ": " + syncCommands.get(key));
			});
			if (cursor.isFinished()) {
				break;
			}
			cursor = syncCommands.scan(cursor);
		}

		RedisStorage<String, String> result = null;
		redisURI = RedisURI.builder()
				.withHost("localhost")
				.withPort(redisPort)
				.withClientName("test-2")
				.build();

		result = new RedisStorage<String, String>(
				redisURI,
				mapper,
				retentionInS,
				maxSize,
				nullKey,
				nullValue,
				Object::toString);

		result.setKeyGenerator(new KeyGeneratorFactory().make(String.class));
		return result;
	}

}