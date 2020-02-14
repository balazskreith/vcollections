package com.wobserver.vcollections.builders;

import com.wobserver.vcollections.storages.IStorage;
import com.wobserver.vcollections.storages.MemoryStorage;

/**
 * Represents a builder responsible for building a {@link MemoryStorage}.
 */
public class MemoryStorageBuilder extends AbstractStorageBuilder implements IStorageBuilder {

	/**
	 * Constructs a {@link IStorageBuilder}.
	 */
	public MemoryStorageBuilder() {

	}

	/**
	 * Builds an {@link MemoryStorage} based on the provided configurations.
	 * @param <K> The type of the key for the {@link IStorage}.
	 * @param <V> The type of the value for the {@link IStorage}.
	 * @return Returns an {@link MemoryStorage} set up with the given configurations
	 */
	@Override
	public <K, V> IStorage<K, V> build() {
		Config config = this.convertAndValidate(Config.class);

		MemoryStorage<K, V> result = new MemoryStorage<>(null, null, config.capacity);
		this.decorateWithKeyGenerator(result, config);
		return result;
	}

	/**
	 * Sets up the capacity for the built {@link IStorage}
	 * @param value The capacity of the provided {@link IStorage}
	 * @return {@link this} to provide further configurations.
	 */
	public MemoryStorageBuilder withCapacity(Long value) {
		this.configure(CAPACITY_CONFIG_KEY, value);
		return this;
	}

	/**
	 * The configuration possibilities inherited from the {@link AbstractStorageBuilder.Config}.
	 */
	public static class Config extends AbstractStorageBuilder.Config {

	}


}
