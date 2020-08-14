package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.storages.RedisStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.balazskreith.vcollections.storages.RedisMapper;
import java.util.Map;
import javax.validation.constraints.NotNull;

public class RedisMapperBuilder extends AbstractBuilder {

	public static final String KEY_CLASS_CONFIG_KEY = "keyClass";
	public static final String VALUE_CLASS_CONFIG_KEY = "valueClass";
	public static final String OBJECTMAPPER_CLASS_CONFIG_KEY = "objectMapper";

	/**
	 * Build a {@link RedisMapper}
	 *
	 * @param <K> the type of the key
	 * @param <V> the type of the value
	 * @return A {@link RedisMapper} that built.
	 */
	public <K, V> RedisMapper<K, V> build() {
		Config config = this.convertAndValidate(Config.class);
		ObjectMapper objectMapper;
		Class<K> keyType = this.getClassFor(config.keyClass);
		Class<V> valueType = this.getClassFor(config.valueClass);
		RedisMapper<K, V> result;
		if (config.objectMapper != null) {
			objectMapper = this.invoke(config.objectMapper);
		} else {
			objectMapper = new ObjectMapper();
		}

		result = new RedisMapper<>(keyType, valueType, objectMapper);
		return result;
	}

	/**
	 * Puts all configuration to the inside holder in the builder.
	 * These configurations are used when the {@link this#build()} is called.
	 *
	 * @param configs The provided configurations
	 * @return {@link this} builder.
	 */
	public RedisMapperBuilder withConfiguration(Map<String, Object> configs) {
		this.configs.putAll(configs);
		return this;
	}

	/**
	 * Sets the key type class used for
	 * {@link RedisStorage}
	 *
	 * @param className the name of the class of the key
	 * @return A {@link RedisMapperBuilder} to set options further
	 */
	public RedisMapperBuilder withKeyType(String className) {
		this.configs.put(KEY_CLASS_CONFIG_KEY, className);
		return this;
	}

	/**
	 * Sets the value type class used for
	 * {@link RedisStorage}
	 *
	 * @param className the name of the class of the value
	 * @return A {@link RedisMapperBuilder} to set options further
	 */
	public RedisMapperBuilder withValueType(String className) {
		this.configs.put(VALUE_CLASS_CONFIG_KEY, className);
		return this;
	}

	/**
	 * Sets the {@link com.fasterxml.jackson.databind.ObjectMapper} will be used to build
	 * {@link RedisStorage}
	 *
	 * @param className the name of the class of the ObjectMapper will be instantiated
	 * @return A {@link RedisMapperBuilder} to set options further
	 */
	public RedisMapperBuilder withObjectMapper(String className) {
		this.configs.put(OBJECTMAPPER_CLASS_CONFIG_KEY, className);
		return this;
	}

	public static class Config {

		@NotNull
		public String keyClass;

		@NotNull
		public String valueClass;

		public String objectMapper = null;
	}
}
