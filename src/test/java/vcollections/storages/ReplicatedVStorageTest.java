package vcollections.storages;

import java.util.HashMap;
import java.util.Map;
import vcollections.keygenerators.KeyGeneratorFactory;

class ReplicatedVStorageTest implements StorageTest<ReplicatedVStorage<String, String>> {

	@Override
	public IStorage<String, String> makeStorage(long maxSize, String... items) {
		Map<String, String> pairs = new HashMap<>();
		if (items != null) {
			for (int i = 0; i + 1 < items.length; i += 2) {
				String key = items[i];
				String value = items[i + 1];
				pairs.put(key, value);
			}
		}

		IStorage<String, String> result =
				new ReplicatedVStorage<>(pairs, maxSize,
						new KeyGeneratorFactory().make(String.class),
						new MemoryStorage<String, String>(null, null, IStorage.NO_MAX_SIZE),
						new MemoryStorage<String, String>(null, null, IStorage.NO_MAX_SIZE)
				);

		return result;
	}
}