package com.wobserver.vcollections.storages;

import com.wobserver.vcollections.builders.RedisMapperBuilder;
import com.wobserver.vcollections.keygenerators.KeyGeneratorFactory;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import redis.embedded.RedisServer;

class RedisStorageTest implements StorageTest<String, String, RedisStorage<String, String>> {

	private static final int redisPort = 6367;

	private RedisServer redisServer;

	@BeforeEach
	public void startRedis() throws IOException {
		redisServer = new RedisServer(redisPort);
		redisServer.start();

	}

	@AfterEach
	public void stopRedis() {
		redisServer.stop();
	}

	@Override
	public String toKey(String key) {
		if (key == null) {
			return "null";
		}
		return key;
	}

	@Override
	public String toValue(String value) {
		if (value == null) {
			return "null";
		}
		return value;
	}

	@Override
	public IStorage<String, String> makeStorage(long maxSize, Map.Entry<String, String>... entries) {
		return makeStorage(maxSize, 0, entries);

	}

	public IStorage<String, String> makeStorage(long maxSize, int retentionInS, Map.Entry<String, String>... entries) {
		RedisURI redisURI = RedisURI.builder()
				.withHost("localhost")
				.withPort(redisPort)
				.withClientName("test")
				.build();
		RedisMapper<String, String> mapper = new RedisMapperBuilder()
				.withKeyType(String.class.getName())
				.withValueType(String.class.getName())
				.build();
		RedisClient client = RedisClient.create(redisURI);
		StatefulRedisConnection<String, String> commandConnection = client.connect(mapper);
		RedisCommands<String, String> syncCommands = commandConnection.sync();
		BiConsumer<String, String> setCommand = (key, value) -> {
			key = toKey(key);
			value = toValue(value);
			syncCommands.set(key, value);
		};
		if (entries != null) {
			for (Map.Entry<String, String> entry : entries) {
				setCommand.accept(entry.getKey(), entry.getValue());
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
				Object::toString);

		result.setKeyGenerator(new KeyGeneratorFactory().make(String.class));
		return result;
	}

}