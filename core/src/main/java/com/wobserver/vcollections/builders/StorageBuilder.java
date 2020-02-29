package com.wobserver.vcollections.builders;

import com.wobserver.vcollections.storages.IStorage;
import java.util.HashMap;
import java.util.Map;
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
	 * The using keyword for inheriting configurations
	 */
	public static final String USING_PROFILE_CONFIG_KEY = "using";

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
		return super.withConfiguration((Map<String, Object>) obj);
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
		if (config.using != null) {
			Config profileConfig = this.getConfigForProfile(config.using);
			config = this.merge(config, profileConfig);
		}
		IStorageBuilder builder = this.getBuilder(config.builder);
		if (config.configuration == null) {
			return builder.build();
		}
		return builder
				.withConfiguration(config.configuration)
				.build();
	}

	private Config merge(Config source, Config profile) {
		if (source.builder != null) {
			if (profile.builder != null && !source.builder.equals(profile.builder)) {
				throw new InvalidConfigurationException("The Builders must match");
			}
		} else if (profile.builder == null) {
			throw new InvalidConfigurationException("Builder is obligated");
		} else {
			source.builder = profile.builder;
		}

		if (source.configuration == null) {
			source.configuration = new HashMap<>();
		}
		source.configuration = deepMerge(profile.configuration, source.configuration);
		return source;
	}

	private Config getConfigForProfile(String profile) {
		StorageProfiles storageProfiles = this.getStorageProfiles();
		if (storageProfiles == null) {
			throw new InvalidConfigurationException("Cannot use a profile without a " + StorageProfiles.class.getName());
		}
		Map<String, Object> profileConfig = storageProfiles.getConfigurationFor(profile);
		if (profileConfig == null) {
			throw new InvalidConfigurationException("The dedicated profile " + profile + " does not exist in the given " + StorageProfiles.class.getName());
		}
		return this.convertAndValidate(Config.class, profileConfig);
	}

	private IStorageBuilder getBuilder(String builderType) {
		IStorageBuilder result;
		if (builderType.contains(".") == false) {
			result = this.invoke("com.wobserver.vcollections.builders." + builderType);
		} else {
			result = this.invoke(builderType);
		}
		return result
				.withStorageProfiles(this.getStorageProfiles());
	}

	/**
	 * The basic configuration to validate the given configuration.
	 */
	public static class Config {
		/**
		 * The name of the builder class implements {@link IStorageBuilder} interface
		 */
		public String builder;

		/**
		 * The embedded configuration
		 */
		public Map<String, Object> configuration;

		/**
		 * The used profile
		 */
		public String using;
	}
}
