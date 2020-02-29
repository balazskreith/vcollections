package com.wobserver.vcollections.builders;

import com.wobserver.vcollections.keygenerators.IAccessKeyGenerator;
import com.wobserver.vcollections.keygenerators.IKeyGenerator;
import com.wobserver.vcollections.storages.CachedStorage;
import com.wobserver.vcollections.storages.ClusteredVStorage;
import com.wobserver.vcollections.storages.IStorage;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;

/**
 * Builds a {@link CachedStorage} based on the configuration
 *
 */
public abstract class AbstractVStorageBuilder extends AbstractStorageBuilder implements IStorageBuilder {
	public static final String STORAGES_CONFIG_KEY = "storages";

	/**
	 * Builds a {@link CachedStorage} based on the previously provided configurations.
	 *
	 * @return The configured {@link CachedStorage}.
	 */
	@Override
	public<K, V> IStorage<K, V> build() {
		Config config = this.convertAndValidate(Config.class);
		IStorage<K, V>[] storages = new IStorage[config.storages.size()];
		int i = 0;
		for (Map<String, Object> storageConfig : config.storages) {
			IStorage<K, V> storage = this.makeStorageBuilderFor(storageConfig).build();
			storages[i++] = storage;
		}
		var result = this.build(config, storages);
		this.decorateWithKeyGenerator(result, config);
		return result;
	}
	
	public static interface IVStorage<K, V> extends IStorage<K, V>, IAccessKeyGenerator<K> {
		
	}
	
	protected abstract<K, V, T extends IStorage<K, V> & IAccessKeyGenerator<K>> T build(Config config, IStorage<K, V>... storages);
	

	/**
	 * The configuration for the {@link CachedStorage}
	 */
	public static class Config extends AbstractStorageBuilder.Config {
		@NotNull
		public List<Map<String, Object>> storages;

	}
}
