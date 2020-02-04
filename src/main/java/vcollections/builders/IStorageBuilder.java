package vcollections.builders;

import java.util.Map;
import vcollections.storages.IStorage;

public interface IStorageBuilder {

	String CAPACITY_CONFIG_KEY = "capacity";

	IStorageBuilder withConfiguration(Map<String, Object> configs);

	<K, V> IStorage<K, V> build();
}
