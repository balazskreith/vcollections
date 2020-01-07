package vcollections.builders;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vcollections.keygenerators.IKeyGenerator;
import vcollections.storages.IStorage;
import vcollections.storages.MemoryStorage;

public abstract class AbstractStorageBuilder extends AbstractBuilder implements IStorageBuilder {

	private KeyGeneratorBuilder keyGeneratorBuilder = new KeyGeneratorBuilder();
	private static Logger logger = LoggerFactory.getLogger(AbstractStorageBuilder.class);

	@Override
	public IStorageBuilder withConfiguration(Map<String, Object> configs) {
		this.configs.putAll(configs);
		return this;
	}

	protected<T> IKeyGenerator<T> buildKeyGenerator(AtomicBoolean selfTest) {
		Map<String, Object> keyGeneratorConfig = this.get(KeyGeneratorBuilder.KEY_GENERATOR_CONFIG_KEY,  obj -> (Map<String, Object>) obj);
		if (keyGeneratorConfig == null) {
			return null;
		}
		this.keyGeneratorBuilder.withConfiguration(keyGeneratorConfig);
		IKeyGenerator<T> result = this.keyGeneratorBuilder.build();
		selfTest.set(this.keyGeneratorBuilder.isStorageTest());
		return result;
	}
}
