package vcollections.builders;

import java.util.Map;
import javax.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vcollections.keygenerators.IAccessKeyGenerator;
import vcollections.keygenerators.IKeyGenerator;
import vcollections.storages.IStorage;

public abstract class AbstractStorageBuilder extends AbstractBuilder implements IStorageBuilder {

	private KeyGeneratorBuilder keyGeneratorBuilder = new KeyGeneratorBuilder();
	private static Logger logger = LoggerFactory.getLogger(AbstractStorageBuilder.class);

	@Override
	public IStorageBuilder withConfiguration(Map<String, Object> configs) {
		this.configs.putAll(configs);
		return this;
	}

	protected <K, V, T extends IStorage<K, V> & IAccessKeyGenerator<K>> T decorateWithKeyGenerator(T target, Config config) {
		if (config.keyGenerator == null) {
			return null;
		}
		KeyGeneratorBuilder keyGeneratorBuilder = new KeyGeneratorBuilder();
		keyGeneratorBuilder.withConfiguration(config.keyGenerator);
		IKeyGenerator<K> keyGenerator = keyGeneratorBuilder.build();
		if (keyGeneratorBuilder.isStorageTest()) {
			keyGenerator.setup(target::has);
		}
		target.setKeyGenerator(keyGenerator);
		return target;
	}

	// Jackson configuration can be used to generate this class of config
	// as it is implemented in the AbstractBuilder
	// and javax validation can be used to validate the objects.
	// and then we can do a proper
	public static class Config {

		@Min(value = IStorage.NO_MAX_SIZE)
		public long capacity = IStorage.NO_MAX_SIZE;

		public Map<String, Object> keyGenerator;
	}
}
