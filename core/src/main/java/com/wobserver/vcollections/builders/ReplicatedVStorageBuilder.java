package com.wobserver.vcollections.builders;

import com.wobserver.vcollections.keygenerators.IAccessKeyGenerator;
import com.wobserver.vcollections.storages.CachedStorage;
import com.wobserver.vcollections.storages.ClusteredVStorage;
import com.wobserver.vcollections.storages.IStorage;
import com.wobserver.vcollections.storages.ReplicatedVStorage;

/**
 * Builds a {@link CachedStorage} based on the configuration
 *
 * @param <K> The type of the key for {@link IStorage}
 * @param <V> The type of the value for {@link IStorage}
 */
public class ReplicatedVStorageBuilder<K, V> extends AbstractVStorageBuilder {
	
	@Override
	protected <K, V, T extends IStorage<K, V> & IAccessKeyGenerator<K>> T build(AbstractVStorageBuilder.Config config, IStorage<K, V>... storages) {
		ReplicatedVStorage<K, V> result = new ReplicatedVStorage<K, V>(null, config.capacity, null, storages);
		return (T) result;
	}

	/**
	 * The configuration for the {@link CachedStorage}
	 */
	public static class Config extends AbstractVStorageBuilder.Config {

	}
}
