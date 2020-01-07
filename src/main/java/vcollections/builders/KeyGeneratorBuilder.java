package vcollections.builders;

import static vcollections.builders.MemoryStorageBuilder.CAPACITY_CONFIG_KEY;
import java.util.Map;
import java.util.function.Supplier;
import vcollections.keygenerators.IKeyGenerator;
import vcollections.keygenerators.KeyGeneratorFactory;
import vcollections.storages.IStorage;
import vcollections.storages.MemoryStorage;

public class KeyGeneratorBuilder extends AbstractBuilder {

	public static final String KEY_GENERATOR_CONFIG_KEY = "keyGenerator";
	public static final String CLASS_CONFIG_KEY = "class";
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
	 * @param keyGenerator
	 * @param <T>
	 * @return
	 */
	public<T> KeyGeneratorBuilder withKeyGenerator(IKeyGenerator<T> keyGenerator) {
		this.configure(CONFIGURED_KEY_GENERATOR_CONFIG_KEY, keyGenerator);
		return this;
	}

	public <T> IKeyGenerator<T> build() {
		IKeyGenerator<T> result = this.get(CONFIGURED_KEY_GENERATOR_CONFIG_KEY, obj -> (IKeyGenerator<T>) obj);
		if (result != null) {
			return null;
		}
		String klassName = this.get(CLASS_CONFIG_KEY, Object::toString);
		if (klassName == null) {
			return null;
		}
		Boolean storageTest = this.getOrDefault(STORAGE_TEST_CONFIG_KEY, obj-> (Boolean)obj, false);
		Class klass = null;
		try {
			klass = Class.forName(klassName);
		} catch (ClassNotFoundException e) {
			return null;
		}
		int minSize = this.getOrDefault(MIN_KEY_SIZE_CONFIG_KEY, obj -> (Integer) obj, 0);
		int maxSize = this.getOrDefault(MAX_KEY_SIZE_CONFIG_KEY, obj -> (Integer) obj, 0);
		result = this.factory.make(klass, minSize, maxSize);
		return result;
	}

	public boolean isStorageTest() {
		return this.storageTest;
	}
}
