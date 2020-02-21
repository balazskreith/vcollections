package com.wobserver.vcollections.builders;

import com.wobserver.vcollections.storages.IStorage;
import java.util.Map;
import jdk.jshell.spi.ExecutionControl;

/**
 * An interface for any kind of Builder class intend to
 * build a {@link IStorage}
 */
public interface IStorageBuilder {
	/**
	 * Sets up the Builder imlementation with the provided configuration
	 * @param configs The provided configuration used to build the result storage
	 * @return {@link IStorageBuilder} to configure the Builder further.
	 */
	IStorageBuilder withConfiguration(Map<String, Object> configs);

	/**
	 * Builds an {@link IStorage} with the provided parameters given as a configuration.
	 * @param <K> The type of the key for the {@link IStorage}
	 * @param <V> The type of the value for the {@link IStorage}
	 * @return The {@link IStorage} with the desired configurations.
	 */
	<K, V> IStorage<K, V> build();
}
