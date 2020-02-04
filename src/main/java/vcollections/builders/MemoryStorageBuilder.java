package vcollections.builders;

import vcollections.storages.IStorage;
import vcollections.storages.MemoryStorage;

public class MemoryStorageBuilder extends AbstractStorageBuilder implements IStorageBuilder {

	public MemoryStorageBuilder() {

	}

	@Override
	public <K, V> IStorage<K, V> build() {
		Config config = this.convertAndValidate(Config.class);

		MemoryStorage<K, V> result = new MemoryStorage<>(null, null, config.capacity);
		this.decorateWithKeyGenerator(result, config);
		return result;
	}

	public MemoryStorageBuilder withCapacity(Long value) {
		this.configure(CAPACITY_CONFIG_KEY, value);
		return this;
	}

	public static class Config extends AbstractStorageBuilder.Config {

	}


}
