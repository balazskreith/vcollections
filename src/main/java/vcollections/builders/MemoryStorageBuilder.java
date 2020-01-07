package vcollections.builders;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import vcollections.keygenerators.IKeyGenerator;
import vcollections.keygenerators.KeyGeneratorFactory;
import vcollections.storages.IStorage;
import vcollections.storages.MemoryStorage;

public class MemoryStorageBuilder extends AbstractStorageBuilder implements IStorageBuilder{

	public static final String KEY_GENERATOR_CONFIG_KEY = "keyGenerator";
	public static final String CAPACITY_CONFIG_KEY = "capacity";


	@Override
	public <K, V> IStorage<K, V> build() {
		// TODO: validate
		AtomicBoolean selfTest = new AtomicBoolean(false);
		IKeyGenerator<K> keyGenerator = this.buildKeyGenerator(selfTest);
		Long capacity = this.getOrDefault(CAPACITY_CONFIG_KEY, obj -> Long.parseLong(obj.toString()), IStorage.NO_MAX_SIZE);
		MemoryStorage<K, V> result = new MemoryStorage<>(keyGenerator, null, capacity);

		// if we need to test the keygenerator
		if (selfTest.get()) {
			result.getKeyGenerator().setup(result::has);
		}
		return result;
	}

	public MemoryStorageBuilder withCapacity(Long value) {
		this.configure(CAPACITY_CONFIG_KEY, value);
		return this;
	}

	public MemoryStorageBuilder withKeyGenerator(String type) {
		this.configure(KEY_GENERATOR_CONFIG_KEY, type);
		return this;
	}
}
