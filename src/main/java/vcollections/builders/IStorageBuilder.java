package vcollections.builders;

import java.util.Map;
import vcollections.storages.IStorage;

public interface IStorageBuilder {

	IStorageBuilder withConfiguration(Map<String, Object> configs);

	<K, V> IStorage<K, V> build();
}
