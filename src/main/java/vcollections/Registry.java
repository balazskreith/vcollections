package vcollections;

import vcollections.storages.IStorage;

public class Registry<K> {

	private final IStorage<K, Object> storage;

	public Registry(IStorage<K, Object> storage) {
		this.storage = storage;
	}

	public<T> T get(K key) {
		return (T) this.storage.read(key);
	}
}
