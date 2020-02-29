package com.wobserver.vcollections.builders;

import com.wobserver.vcollections.keygenerators.IKeyGenerator;
import java.util.Map;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import com.wobserver.vcollections.keygenerators.KeyGeneratorFactory;

/**
 * Builds a {@link IKeyGenerator} with the provided configurations
 */
public class KeyGeneratorBuilder extends AbstractBuilder {
	
	public static final String KEY_GENERATOR_CONFIG_KEY = "keyGenerator";
	public static final String CLASS_CONFIG_KEY = "klass";
	public static final String STORAGE_TEST_CONFIG_KEY = "storageTest";
	public static final String MIN_KEY_SIZE_CONFIG_KEY = "minSize";
	public static final String MAX_KEY_SIZE_CONFIG_KEY = "maxSize";
	private KeyGeneratorFactory factory;
	private boolean storageTest = false;

	/**
	 * Constructs a builder builds {@link IKeyGenerator}
	 */
	public KeyGeneratorBuilder() {
		this.factory = new KeyGeneratorFactory();
	}

	/**
	 * Adds configurations, based on which a {@link IKeyGenerator} is built.
	 * @param configs A map contains the configuration we use to build a keygenerator
	 * @return {@link this} to provide further configurations.
	 */
	public KeyGeneratorBuilder withConfiguration(Map<String, Object> configs) {
		this.configs.putAll(configs);
		return this;
	}

	/**
	 * Builds an {@link IKeyGenerator} for the provided type of key.
	 * @param <T> The type of the key we generate a {@link IKeyGenerator} for.
	 * @return An {@link IKeyGenerator}.
	 */
	public <T> IKeyGenerator<T> build() {
		IKeyGenerator<T> result;
		Config config = this.convertAndValidate(Config.class);
		this.storageTest = config.storageTest;
		result = this.factory.make(config.klass, config.minSize, config.maxSize);
		if (result == null) {
			throw new NullPointerException(String.format("KeyGenerator cannot be created with the given parameter class: {}, minSize: {}, maxSize: {}, storageTest: {}",
					config.klass, config.minSize, config.maxSize, this.storageTest));
		}
		return result;
	}

	/**
	 * Indicate if the storage should be tested against the generated key.
	 * @return True if the storage should be tested against the generated key, false otherwise
	 */
	public boolean isStorageTest() {
		return this.storageTest;
	}

	/**
	 * The common configuration class for validating the provided configuration.
	 */
	public static class Config {
		/**
		 * Either type of the key for which a built in {@link IKeyGenerator} exists
		 * or a class of builder results an {@link IKeyGenerator}.
		 */
		@NotNull
		public String klass;

		/**
		 * Defines if the generated keygenerator has to be tested
		 * against the existing key the target storage has.
		 * default: false
		 */
		public boolean storageTest = false;

		/**
		 * The minimal size of the generated keys
		 */
		@Min(value = 0)
		public int minSize = 0;

		/**
		 * The maximal size of the generated keys.
		 */
		@Min(value = 0)
		public int maxSize = 0;
	}

}
