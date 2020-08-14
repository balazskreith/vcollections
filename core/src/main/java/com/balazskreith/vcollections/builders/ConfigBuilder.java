package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.activeconfigs.MemoryStorageActiveConfig;
import com.balazskreith.vcollections.adapters.Adapter;
import com.balazskreith.vcollections.storages.IStorage;
import java.util.Map;

/**
 * An interface for any kind of Builder class intend to
 * build a {@link IStorage}
 */
public interface ConfigBuilder<T extends MemoryStorageActiveConfig> {

	<K, V> Adapter<Map<String, Object>, MemoryStorageActiveConfig<K, V>> build();

}
