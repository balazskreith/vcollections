package com.wobserver.vcollections.builders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wobserver.vcollections.storages.FileStorage;
import com.wobserver.vcollections.storages.IMapper;
import com.wobserver.vcollections.storages.IStorage;
import com.wobserver.vcollections.storages.MemoryStorage;
import com.wobserver.vcollections.storages.PrimitiveTypesMapperFactory;
import java.io.IOException;
import javax.validation.constraints.NotNull;

/**
 * Represents a builder responsible for building a {@link MemoryStorage}.
 */
public class FileStorageBuilder extends AbstractStorageBuilder implements IStorageBuilder {

	public static final String VALUE_TYPE_CONFIG_KEY = "valueType";
	public static final String KEY_TYPE_CONFIG_KEY = "keyType";
	public static final String KEY_MAPPER_CONFIG_KEY = "keyMapper";
	public static final String PATH_CONFIG_KEY = "path";
	public static final String VALUE_MAPPER_TYPE_CONFIG_KEY = "valueMapperType";

	@FunctionalInterface
	public interface ValueTypeCollector {
		PathCollector withValueType(String value);
	}

	@FunctionalInterface
	public interface PathCollector {
		FileStorageBuilder withPath(String value);
	}

	public static ValueTypeCollector make() {
		return valueType -> path -> new FileStorageBuilder().withValueType(valueType).withPath(path);
	}

	/**
	 * Constructs a {@link IStorageBuilder}.
	 */
	public FileStorageBuilder() {
		
	}

	/**
	 * Builds an {@link MemoryStorage} based on the provided configurations.
	 *
	 * @param <K> The type of the key for the {@link IStorage}.
	 * @param <V> The type of the value for the {@link IStorage}.
	 * @return Returns an {@link MemoryStorage} set up with the given configurations
	 */
	@Override
	public <K, V> IStorage<K, V> build() {
		Config config = this.convertAndValidate(Config.class);
		IMapper<K, String> keyMapper;
		if (!(config.keyMapper != null ^ config.keyType != null)) {
			throw new InvalidConfigurationException("Providing either " + KEY_TYPE_CONFIG_KEY + " or " + KEY_MAPPER_CONFIG_KEY + " is mandatory for " + this.getClass().getName() + ", but only one of it!");
		}
		if (config.keyType != null) {

			if (PrimitiveTypesMapperFactory.isPrimitiveType(config.keyType)) {
				Class<K> klass = this.getClassFor(config.keyType);
				keyMapper = PrimitiveTypesMapperFactory.make(klass, String.class);
			} else {
				throw new InvalidConfigurationException(KEY_TYPE_CONFIG_KEY + " for " + this.getClass().getName() + " can only be a primitive type.");
			}
		} else {
			keyMapper = this.invoke(config.keyMapper);
		}

		Class<V> valueClass = this.getClassFor(config.valueType);
		ObjectMapper valueMapper = this.invoke(config.valueMapperType);
		String path = config.path;

		FileStorage<K, V> result = null;
		try {
			result = new FileStorage<K, V>(keyMapper, valueClass, valueMapper, path, null, config.capacity);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.decorateWithKeyGenerator(result, config);
		return result;
	}

	/**
	 * Sets the key type class used for
	 * {@link com.wobserver.vcollections.storages.FileStorage}
	 *
	 * @param value the name of the class of the key
	 * @return A {@link FileStorageBuilder} to set options further
	 */
	public FileStorageBuilder withKeyType(String value) {
		this.configure(KEY_TYPE_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets the value type class used for
	 * {@link com.wobserver.vcollections.storages.FileStorage}
	 *
	 * @param value the name of the class of the value
	 * @return A {@link FileStorageBuilder} to set options further
	 */
	public FileStorageBuilder withValueType(String value) {
		this.configure(VALUE_TYPE_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets the value mapper type class used for
	 * {@link com.wobserver.vcollections.storages.FileStorage}
	 *
	 * @param value the name of the class of the mapper used for mapping the value
	 * @return A {@link FileStorageBuilder} to set options further
	 */
	public FileStorageBuilder withValueMapperType(String value) {
		this.configure(VALUE_MAPPER_TYPE_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets the key mapper type class used for
	 * {@link com.wobserver.vcollections.storages.FileStorage}
	 *
	 * @param value the name of the class of the mapper used for mapping the key
	 * @return A {@link FileStorageBuilder} to set options further
	 */
	public FileStorageBuilder withKeyMapper(String value) {
		this.configure(KEY_MAPPER_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets the path used to store entries for
	 * {@link com.wobserver.vcollections.storages.FileStorage}
	 *
	 * @param value the name of the class of the mapper used for mapping the value
	 * @return A {@link FileStorageBuilder} to set options further
	 */
	public FileStorageBuilder withPath(String value) {
		this.configure(PATH_CONFIG_KEY, value);
		return this;
	}


	/**
	 * The configuration possibilities inherited from the {@link AbstractStorageBuilder.Config}.
	 */
	public static class Config extends AbstractStorageBuilder.Config {

		@NotNull
		public String valueType;

		public String keyType;

		public String keyMapper;

		@NotNull
		public String path;

		public String valueMapperType = "com.fasterxml.jackson.databind.ObjectMapper";


	}


}
