package com.wobserver.vcollections.builders;

import com.wobserver.vcollections.storages.CachedStorage;
import com.wobserver.vcollections.storages.IStorage;
import java.util.Map;
import javax.validation.constraints.NotNull;

/**
 * Builds a {@link CachedStorage} based on the configuration
 *
 * @param <K> The type of the key for {@link IStorage}
 * @param <V> The type of the value for {@link IStorage}
 */
public class CachedStorageBuilder<K, V> extends AbstractStorageBuilder implements IStorageBuilder {
	public static final String KEY_TYPE_CONFIG_KEY = "keyType";
	public static final String SUPERSET_CONFIG_KEY = "superset";
	public static final String SUBSET_CONFIG_KEY = "subset";

	/**
	 * Builds a {@link CachedStorage} based on the previously provided configurations.
	 *
	 * @return The configured {@link CachedStorage}.
	 */
	@Override
	public CachedStorage<K, V> build() {
		Config config = this.convertAndValidate(Config.class);

		Class keyType = this.getClassFor(config.keyType);
		IStorage<K, V> superset = new StorageBuilder().withConfiguration(config.subset).build();
		IStorage<K, V> subset = new StorageBuilder().withConfiguration(config.superset).build();
		CachedStorage<K, V> result = new CachedStorage<>(keyType, superset, subset);
		return result;
	}

	/**
	 * The configuration for the {@link CachedStorage}
	 */
	public static class Config extends AbstractStorageBuilder.Config {
		@NotNull
		public String keyType;

		@NotNull
		public Map<String, Object> superset;

		@NotNull
		public Map<String, Object> subset;
	}
}
