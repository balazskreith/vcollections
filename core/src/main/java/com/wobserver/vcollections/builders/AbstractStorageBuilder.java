package com.wobserver.vcollections.builders;

import com.wobserver.vcollections.keygenerators.IAccessKeyGenerator;
import com.wobserver.vcollections.keygenerators.IKeyGenerator;
import com.wobserver.vcollections.storages.IStorage;
import java.util.Map;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a skeletal implementation for {@link IStorageBuilder}
 * interface to minimize the effort required to implement such builder.
 *
 * <p>To implement any kind of {@link IStorageBuilder}, it is recommended to extend this class
 * and use the {@link AbstractBuilder#convertAndValidate(Class)} method, which validates and
 * converts the provided configuration (which is a {@link Map<String, Object>} type)
 * to the desired class, and throws {@link ConstraintViolationException} if
 * a validation fails.
 *
 * <p>The programmer should generally provide configuration keys in the
 * extended class.
 *
 * @author Balazs Kreith
 * @since 0.7
 */
public abstract class AbstractStorageBuilder extends AbstractBuilder implements IStorageBuilder {

	public static final String CAPACITY_CONFIG_KEY = "capacity";

	private static Logger logger = LoggerFactory.getLogger(AbstractStorageBuilder.class);


	/**
	 * Puts all configuration to the inside holder in the builder.
	 * These configurations are used when the {@link this#build()} is called.
	 *
	 * @param configs The provided configurations
	 * @return {@link this} builder.
	 */
	@Override
	public IStorageBuilder withConfiguration(Map<String, Object> configs) {
		this.configs.putAll(configs);
		return this;
	}

	/**
	 * Invoke this method to decorate the built {@link IStorage} with a keygenerator if that is provided
	 * in the configuration.
	 *
	 * @param target The target {@link IStorage} the keygenerator is configured into
	 * @param config The configuration examined to setup a keygenerator if that is provided
	 * @param <K>    The type of the key for the {@link IStorage}
	 * @param <V>    The type of the value for the {@link IStorage}
	 * @param <T>    The type of the target set up with a {@link IKeyGenerator}.
	 *               This type must implement the {@link IStorage}, and the {@link IAccessKeyGenerator}
	 * @return The target decorated with a {@link IKeyGenerator} or, if the keygenerator configuration is not
	 * provided, than the target itself provided as a parameter.
	 */
	protected <K, V, T extends IStorage<K, V> & IAccessKeyGenerator<K>> T decorateWithKeyGenerator(T target, Config config) {
		if (config.keyGenerator == null) {
			return target;
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

	/**
	 * The base configuration for any kind of Configuration foor a {@link AbstractStorageBuilder}
	 */
	public static class Config {
		/**
		 * The capacity of the storage (default is {@link IStorage#NO_MAX_SIZE}.
		 */
		@Min(value = IStorage.NO_MAX_SIZE)
		public long capacity = IStorage.NO_MAX_SIZE;

		/**
		 * Configurations for a Keygenerator (default is null)
		 */
		public Map<String, Object> keyGenerator;
	}
}
