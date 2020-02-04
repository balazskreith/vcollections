package vcollections.builders;

import java.util.Map;
import javax.validation.constraints.NotNull;
import vcollections.storages.CachedStorage;
import vcollections.storages.IStorage;

public class CachedStorageBuilder<K, V> extends AbstractStorageBuilder implements IStorageBuilder {
	public static final String KEY_TYPE_CONFIG_KEY = "keyType";
	public static final String SUPERSET_CONFIG_KEY = "superset";
	public static final String SUBSET_CONFIG_KEY = "subset";

	@Override
	public CachedStorage<K, V> build() {
		Config config = this.convertAndValidate(Config.class);

		Class keyType;
		try {
			keyType = Class.forName(config.keyType);
		} catch (ClassNotFoundException e) {
			throw new InvalidConfigurationException("Class type for " + config.keyType + " does not exist");
		}

		IStorage<K, V> superset = new StorageBuilder().withConfiguration(config.subset).build();
		IStorage<K, V> subset = new StorageBuilder().withConfiguration(config.superset).build();
		CachedStorage<K, V> result = new CachedStorage<>(keyType, superset, subset);
		return result;
	}

	public static class Config extends AbstractStorageBuilder.Config {
		@NotNull
		public String keyType;

		@NotNull
		public Map<String, Object> superset;

		@NotNull
		public Map<String, Object> subset;
	}
}
