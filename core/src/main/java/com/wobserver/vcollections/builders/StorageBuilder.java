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
	 * Sets the builder of the Storage
	 *
	 * @param value
	 * @return
	 */
	public IStorageBuilder withBuilder(String value) {
		this.configs.put(BUILDER_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Assign a profile key to use it as a base value
	 *
	 * @param value
	 * @return
	 */
	public IStorageBuilder usingProfile(String value) {
		this.configs.put(USING_PROFILE_CONFIG_KEY, value);
		return this;
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
		Map<String, Object> configuration = this.buildConfigurations();
		Config config = this.convertAndValidate(Config.class, configuration);
		IStorageBuilder builder = this.getBuilder(config.builder);
		if (config.configuration == null) {
			return builder.build();
		}
		return builder
				.withConfiguration(config.configuration)
				.build();
	}

	@Override
	protected Map<String, Object> navigate(String... path) {
		// This was omitted, becasue of the decision, to give more possibilities in retrieving configurations.
//		Map<String, Object> configuration = (Map<String, Object>) this.buildConfigurations().getOrDefault(CONFIGURATION_CONFIG_KEY, new HashMap<>());
		Map<String, Object> configuration = (Map<String, Object>) this.buildConfigurations();
		return this.navigate(configuration, path);
	}

	/**
	 * Build the configuration map for this builder
	 *
	 * @return
	 */
	private Map<String, Object> buildConfigurations() {
		return this.buildConfigurations(this.configs);
	}

	/**
	 * Gets the configuration, in which it merges the profile backwards
	 *
	 * @param source the sourcemap, which will be exemined for profile key
	 * @return the result map, which is merged using profile keys
	 */
	private Map<String, Object> buildConfigurations(Map<String, Object> source) {
		if (source.get(USING_PROFILE_CONFIG_KEY) == null) {
			return source;
		}
		Map<String, Object> actual = new HashMap<>();
		actual.putAll(this.configs);
		String profile = (String) actual.remove(USING_PROFILE_CONFIG_KEY);
		Map<String, Object> profileConfiguration = this.getStorageProfiles().getConfigurationFor(profile);
		Map<String, Object> result = AbstractStorageBuilder.deepMerge(profileConfiguration, actual);
		return this.buildConfigurations(result);
	}

	/**
	 * Gets a Builder class implements the {@link IStorageBuilder} for the class name
	 *
	 * @param builderType the name of the class.
	 * @return
	 */
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
