package vcollections.builders;

import java.util.HashMap;
import java.util.Map;
import vcollections.storages.IStorage;

public class StorageBuilder extends AbstractStorageBuilder implements IStorageBuilder {
	public static final String BUILDER_CONFIG_KEY = "builder";
	public static final String CONFIGURATION_CONFIG_KEY = "configuration";
	public static final String CACHED_STORAGE_CONFIG_KEY = "cachedStorage";
	public static final String MEMORY_STORAGE_CONFIG_KEY = "memoryStorage";

	protected Map<String, IStorageBuilder> builders = new HashMap<>();

	public StorageBuilder() {
		this.builders.put(CACHED_STORAGE_CONFIG_KEY, new CachedStorageBuilder());
		this.builders.put(MEMORY_STORAGE_CONFIG_KEY, new MemoryStorageBuilder());
	}

	public IStorageBuilder withConfiguration(Object obj) {
		return this.withConfiguration((Map<String, Object>) obj);
	}

	@Override
	public <K, V> IStorage<K, V> build() {

		Map<String, Object> configuration = this.get(CONFIGURATION_CONFIG_KEY, obj -> (Map<String, Object>) obj);
		IStorageBuilder builder = this.get(BUILDER_CONFIG_KEY, this.builders::get);

		if (configuration == null) {
			return builder.build();
		}

		return builder
				.withConfiguration(configuration)
				.build();
	}
}
