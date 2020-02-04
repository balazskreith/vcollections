package vcollections.builders;

import java.util.Map;
import javax.validation.constraints.NotNull;
import vcollections.keygenerators.IKeyGenerator;
import vcollections.keygenerators.KeyGeneratorFactory;

public class KeyGeneratorBuilder extends AbstractBuilder {
	public static class Config {
		@NotNull
		public String klass;

		public boolean storageTest = false;

		public int minSize = 0;

		public int maxSize = 0;
	}

	public static final String KEY_GENERATOR_CONFIG_KEY = "keyGenerator";
	public static final String CLASS_CONFIG_KEY = "klass";
	public static final String STORAGE_TEST_CONFIG_KEY = "storageTest";
	public static final String MIN_KEY_SIZE_CONFIG_KEY = "minSize";
	public static final String MAX_KEY_SIZE_CONFIG_KEY = "maxSize";
	private static final String CONFIGURED_KEY_GENERATOR_CONFIG_KEY = "configuredKeyGenerator";
	private KeyGeneratorFactory factory;
	private boolean storageTest = false;

	public KeyGeneratorBuilder() {
		this.factory = new KeyGeneratorFactory();
	}

	public KeyGeneratorBuilder withConfiguration(Map<String, Object> configs) {
		this.configs.putAll(configs);
		return this;
	}

	/**
	 * If this param is set, the normal config will be ignored
	 *
	 * @param keyGenerator
	 * @param <T>
	 * @return
	 */
	public <T> KeyGeneratorBuilder withKeyGenerator(IKeyGenerator<T> keyGenerator) {
		this.configure(CONFIGURED_KEY_GENERATOR_CONFIG_KEY, keyGenerator);
		return this;
	}

	public <T> IKeyGenerator<T> build() {
		IKeyGenerator<T> result = this.get(CONFIGURED_KEY_GENERATOR_CONFIG_KEY, obj -> (IKeyGenerator<T>) obj);
		if (result != null) {
			return result;
		}
		Config config = this.convertAndValidate(Config.class);
		this.storageTest = config.storageTest;
		result = this.factory.make(config.klass, config.minSize, config.maxSize);
		if (result == null) {
			throw new NullPointerException(String.format("KeyGenerator cannot be created with the given parameter class: {}, minSize: {}, maxSize: {}, storageTest: {}",
					config.klass, config.minSize, config.maxSize, this.storageTest));
		}
		return result;
	}

	public boolean isStorageTest() {
		return this.storageTest;
	}


}
