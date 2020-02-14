package com.wobserver.vcollections.builders;

import com.wobserver.vcollections.storages.IStorage;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Storage Builder invokes a builders for the designated type of storages.
 */
public class StorageBuilder extends AbstractStorageBuilder implements IStorageBuilder {

	private static Logger logger = LoggerFactory.getLogger(StorageBuilder.class);
	/**
	 * The configuration key to provide the class type of a {@link IStorageBuilder}.
	 * Note: if the provided string does not contain dot (.), the
	 * default package path is prefixed, which is currently: com.wobserver.vcollections.builders
	 */
	public static final String BUILDER_CONFIG_KEY = "builder";
	/**
	 * The embedded configurations for the {@link IStorageBuilder}
	 */
	public static final String CONFIGURATION_CONFIG_KEY = "configuration";

	/**
	 * Constructs a {@link StorageBuilder}, with the embedded configuration.
	 */
	public StorageBuilder() {

	}

	/**
	 * Adds the provided configuration to the set of configuration to
	 * generate a {@link IStorageBuilder}.
	 *
	 * @param obj the object must be a type of {@link Map<String, Object>} contains
	 *            the necessary configuration
	 * @return {@link this} to configure the builder further
	 */
	public IStorageBuilder withConfiguration(Object obj) {
		return this.withConfiguration((Map<String, Object>) obj);
	}

	/**
	 * Builds an {@link IStorage} based on the provided configurations.
	 *
	 * @param <K> The type of the key for the {@link IStorage}
	 * @param <V> The type of the value for the {@link IStorage}
	 * @return An {@link IStorage} if the given builder exists.
	 */
	@Override
	public <K, V> IStorage<K, V> build() {
		Config config = this.convertAndValidate(Config.class);
		IStorageBuilder builder;
		if (config.builder.contains(".") == false) {
			builder = this.invoke("com.wobserver.vcollections.builders." + config.builder);
		} else {
			builder = this.invoke(config.builder);
		}

		if (config.configuration == null) {
			return builder.build();
		}

		return builder
				.withConfiguration(config.configuration)
				.build();
	}

	/**
	 * The basic configuration to validate the given configuration.
	 */
	public static class Config {
		/**
		 * The name of the builder class implements {@link IStorageBuilder} interface
		 */
		@NotNull
		public String builder;

		/**
		 * The embedded configuration
		 */
		public Map<String, Object> configuration;
	}
}
