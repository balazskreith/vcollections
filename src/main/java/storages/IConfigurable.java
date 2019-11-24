package storages;

import java.util.Map;
import java.util.function.Supplier;

public interface IConfigurable<K, V> {

	void setKeyGenerator(Supplier<K> value);

	void setCapacity(Long capacity);

	void setEntries(Map<K, V> items);

}
