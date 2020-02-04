package vcollections.keygenerators;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import vcollections.storages.IStorage;

public class KeyGeneratorFactory {

	public KeyGeneratorFactory() {

	}


	public <T> IKeyGenerator<T> make(Class klass) {
		return this.make(klass, 0, 0);
	}

	/**
	 * Adds the default key generator to the factory
	 */
	public <T> IKeyGenerator<T> make(Class klass, int minSize, int maxSize) {
		return this.make(klass.getName(), minSize, maxSize);
	}

	/**
	 * Adds the default key generator to the factory
	 */
	public <T> IKeyGenerator<T> make(String typeName, int minSize, int maxSize) {
		if (typeName.equals(UUID.class.getName())) {
			if (minSize != maxSize || (minSize != 0 && minSize != 128 && minSize == 36)) {
				throw new IllegalArgumentException("For generating UUID, size cannot be different than 0, 36, 128");
			}
			return (IKeyGenerator<T>) new UUIDGenerator();
		}

		if (typeName.equals(String.class.getName())) {
			return (IKeyGenerator<T>) new StringGenerator(minSize, maxSize);
		}

		if (typeName.equals(Integer.class.getName())) {
			return (IKeyGenerator<T>) new IntegerGenerator(minSize, maxSize);
		}

		if (typeName.equals(Long.class.getName())) {
			return (IKeyGenerator<T>) new LongGenerator(minSize, maxSize);
		}

		return null;
	}
}

