package com.wobserver.vcollections.builders;

import com.wobserver.vcollections.keygenerators.IAccessKeyGenerator;
import com.wobserver.vcollections.storages.CachedStorage;
import com.wobserver.vcollections.storages.ChainedVStorage;
import com.wobserver.vcollections.storages.ClusteredVStorage;
import com.wobserver.vcollections.storages.IStorage;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;

/**
 * Builds a {@link CachedStorage} based on the configuration
 *
 * @param <K> The type of the key for {@link IStorage}
 * @param <V> The type of the value for {@link IStorage}
 */
public class ChainedVStorageBuilder<K, V> extends AbstractVStorageBuilder {


	@Override
	protected <K, V, T extends IStorage<K, V> & IAccessKeyGenerator<K>> T build(AbstractVStorageBuilder.Config config, IStorage<K, V>... storages) {
		ChainedVStorage<K, V> result = new ChainedVStorage<K, V>(null, config.capacity, null, storages);
		return (T) result;
	}

	/**
	 * The configuration for the {@link CachedStorage}
	 */
	public static class Config extends AbstractVStorageBuilder.Config {

	}
}
