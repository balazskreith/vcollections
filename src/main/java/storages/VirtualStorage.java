package storages;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public abstract class VirtualStorage<K, V> implements IStorage<K, V> {

	private transient List<IStorage<K, V>> storages;
	private Long capacity = 0L;
	private Long entries = 0L;
	private Supplier<K> keyGenerator;

	public VirtualStorage(Supplier<K> keyGenerator, IStorage<K, V>... storages) {
		this.keyGenerator = keyGenerator;
		this.storages = Arrays.asList(storages);
	}

	protected void getAndUpdateEntries(UnaryOperator<Long> operator) {
		this.entries = operator.apply(this.entries);
	}

	protected void getAndUpdateCapacity(UnaryOperator<Long> operator) {
		this.capacity = operator.apply(this.capacity);
	}

	protected List<IStorage<K, V>> getStorages() {
		return this.storages;
	}

	@Override
	public Long entries() {
		return this.entries;
	}

	@Override
	public Long capacity() {
		return this.capacity;
	}

	@Override
	public boolean isEmpty() {
		for (IStorage<K, V> storage : this.storages) {
			if (!storage.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isFull() {
		if (this.capacity() == NO_MAX_SIZE) {
			return false;
		}
		for (IStorage<K, V> storage : this.storages) {
			if (!storage.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	protected K generate() {
		if (this.keyGenerator == null) {
			throw new NullPointerException("No KeyGenerator is found");
		}
		return this.keyGenerator.get();
	}

	@Override
	public boolean has(Object key) {
		for (IStorage<K, V> storage : this.storages) {
			if (storage.has(key)) {
				return true;
			}
		}
		return false;
	}


	@Override
	public V read(Object key) {
		for (IStorage<K, V> storage : this.storages) {
			if (storage.has(key)) {
				return storage.read(key);
			}
		}
		return null;
	}

	/**
	 * @param key1
	 * @param key2
	 * @throws UnsupportedOperationException if any of the key has not been found
	 */
	@Override
	public abstract void swap(K key1, K key2);

	@Override
	public void clear() {
		for (IStorage<K, V> storage : this.storages) {
			storage.clear();
		}
	}
}
