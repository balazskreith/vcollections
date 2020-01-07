package vcollections.builders;

import vcollections.storages.CachedStorage;
import vcollections.storages.IStorage;

public class CachedStorageBuilder<K, V> extends AbstractStorageBuilder implements IStorageBuilder{
	public static final String KEY_TYPE_CONFIG_KEY = "keyType";
	public static final String SUPERSET_CONFIG_KEY = "superset";
	public static final String SUBSET_CONFIG_KEY = "subset";

	@Override
	public CachedStorage<K, V> build() {
		// TODO: validate

		String keyTypeConfig = this.get(KEY_TYPE_CONFIG_KEY, Object::toString);
		if (keyTypeConfig == null) {
			throw new InvalidConfigurationException();
		}
		Class keyType;
		try { // TODO: this should go to a validation?
			keyType = Class.forName(keyTypeConfig);
		} catch (ClassNotFoundException e) {
			throw new InvalidConfigurationException("Class type for " + keyTypeConfig + " does not exist");
		}

		IStorage<K, V> superset = this.get(SUPERSET_CONFIG_KEY, obj ->
				new StorageBuilder().withConfiguration(obj).build());
		IStorage<K, V> subset = this.get(SUBSET_CONFIG_KEY, obj ->
				new StorageBuilder().withConfiguration(obj).build());
		CachedStorage<K, V> result = new CachedStorage<>(keyType, superset, subset);
		return result;
	}
}
