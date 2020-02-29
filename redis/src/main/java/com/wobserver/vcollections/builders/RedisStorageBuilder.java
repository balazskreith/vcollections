package com.wobserver.vcollections.builders;

import com.wobserver.vcollections.storages.IStorage;
import com.wobserver.vcollections.storages.RedisMapper;
import com.wobserver.vcollections.storages.RedisStorage;
import io.lettuce.core.RedisURI;
import java.util.Map;
import java.util.function.Function;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class RedisStorageBuilder extends AbstractStorageBuilder {

	public static final String KEY_CONVERTER_CLASS_CONFIG_KEY = "keyConverter";
	public static final String MAPPER_CONFIGURATION_CONFIG_KEY = "mapper";
	public static final String URI_CONFIGURATION_CONFIG_KEY = "URI";

	/**
	 * Build a {@link RedisMapper}
	 *
	 * @param <K> the type of the key
	 * @param <V> the type of the value
	 * @return A {@link RedisMapper} that built.
	 */
	public <K, V> IStorage<K, V> build() {
		Config config = this.convertAndValidate(Config.class);
		Function<Object, K> keyConverter;
		RedisMapper<K, V> mapper = new RedisMapperBuilder().withConfiguration(config.mapper).build();
		RedisStorage<K, V> result;

		if (config.keyConverter != null) {
			keyConverter = this.invoke(config.keyConverter);
		} else {
			keyConverter = obj -> (K) obj;
		}

		RedisURIBuilder uriBuilder = new RedisURIBuilder();
		if (config.URI != null) {
			uriBuilder.withConfiguration(config.URI);
		}

		RedisURI uri = uriBuilder.build();
		result = new RedisStorage<>(uri, mapper, config.expirationInS, config.capacity, keyConverter);
		return result;
	}

	/**
	 * Sets the key converter class name to instantiate when building a
	 * {@link com.wobserver.vcollections.storages.RedisStorage}
	 *
	 * @param className the name of the class will be instantiated
	 * @return A {@link RedisStorageBuilder} to set options further
	 */
	public RedisStorageBuilder withKeyConverter(String className) {
		this.configs.put(KEY_CONVERTER_CLASS_CONFIG_KEY, className);
		return this;
	}

	/**
	 * Sets the configuration for the Mapper used to encode and decode keys and values for Redis
	 *
	 * @param value the configuration map for the "{@link RedisMapper}
	 * @return A {@link RedisStorageBuilder} to set options further
	 */
	public RedisStorageBuilder withMapper(Map<String, Object> value) {
		this.configs.put(MAPPER_CONFIGURATION_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets the configuration for the URI used to create a client for Redis
	 *
	 * @param value the configuration URI for the "{@link RedisMapper}
	 * @return A {@link RedisStorageBuilder} to set options further
	 */
	public RedisStorageBuilder withURI(Map<String, Object> value) {
		this.configs.put(URI_CONFIGURATION_CONFIG_KEY, value);
		return this;
	}


	public static class Config extends AbstractStorageBuilder.Config {

		public String keyConverter;

		@NotNull
		public Map<String, Object> mapper;

		public Map<String, Object> URI = null;

		@Min(value = 0)
		public int expirationInS = 0;

	}
}
