package com.wobserver.vcollections.builders;

import com.wobserver.vcollections.storages.IStorage;
import java.util.Map;
import java.util.function.Function;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class MongoStorageBuilder extends AbstractStorageBuilder {

	public static final String KEY_CONVERTER_CLASS_CONFIG_KEY = "keyConverter";
	public static final String NULLKEY_CLASS_CONFIG_KEY = "nullKey";
	public static final String NULLVALUE_CLASS_CONFIG_KEY = "nullValue";
	public static final String MAPPER_CONFIGURATION_CONFIG_KEY = "mapper";
	public static final String URI_CONFIGURATION_CONFIG_KEY = "URI";


	/**
	 * Sets the username to the builder.
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link MongoURIServerBuilder}.
	 */
	public MongoURIServerBuilder withUsername(String value) {
		this.configs.put(USERNAME_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets the password to the builder.
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link MongoURIServerBuilder}.
	 */
	public MongoURIServerBuilder withPassword(String value) {
		this.configs.put(PASSWORD_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Build a {@link com.wobserver.vcollections.storages.MongoStorage}
	 *
	 * @param <K> the type of the key
	 * @param <V> the type of the value
	 * @return A {@link com.wobserver.vcollections.storages.MongoStorage} that built.
	 */
	public <K, V> IStorage<K, V> build() {
		Config config = this.convertAndValidate(Config.class);
		K nullKey;
		V nullValue;
		Function<V, K> keyConverter;
		return result;
	}

	/**
	 * Sets the configuration for the URI used to create a client for Redis
	 *
	 * @param value the configuration URI for the "{@link RedisMapper}
	 * @return A {@link MongoStorageBuilder} to set options further
	 */
	public MongoStorageBuilder withURI(Map<String, Object> value) {
		this.configs.put(URI_CONFIGURATION_CONFIG_KEY, value);
		return this;
	}


	public static class Config extends AbstractStorageBuilder.Config {

		public String keyConverter;

		public String nullKey;

		public String nullValue;

		@NotNull
		public Map<String, Object> mapper;

		public Map<String, Object> URI = null;

		@Min(value = 0)
		public int expirationInS = 0;

	}
}
