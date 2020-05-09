package com.wobserver.vcollections.builders;

import com.wobserver.vcollections.keygenerators.IAccessKeyGenerator;
import com.wobserver.vcollections.keygenerators.IKeyGenerator;
import com.wobserver.vcollections.storages.IStorage;
import java.util.HashMap;
import java.util.List;
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
	 * @param original the original map the merge place to
	 * @param newMap   the newmap we merge to the original one
	 * @return the original map extended by the newMap
	 * @see <a href="https://stackoverflow.com/questions/25773567/recursive-merge-of-n-level-maps">source</a>
	 */
	static Map deepMerge(Map original, Map newMap) {
		if (newMap == null) {
			return original;
		} else if (original == null) {
			original = new HashMap();
		}
		for (Object key : newMap.keySet()) {
			if (newMap.get(key) instanceof Map && original.get(key) instanceof Map) {
				Map originalChild = (Map) original.get(key);
				Map newChild = (Map) newMap.get(key);
				original.put(key, deepMerge(originalChild, newChild));
			} else if (newMap.get(key) instanceof List && original.get(key) instanceof List) {
				List originalChild = (List) original.get(key);
				List newChild = (List) newMap.get(key);
				for (Object each : newChild) {
					if (!originalChild.contains(each)) {
						originalChild.add(each);
					}
				}
			} else {
				original.put(key, newMap.get(key));
			}
		}
		return original;
	}

	/**
	 * If the storageprovider is set, then the builder can use profileKeys from there
	 */
	private StorageProfiles storageProfiles;

	/**
	 * Gets the storageprofiles
	 *
	 * @return
	 */
	protected StorageProfiles getStorageProfiles() {
		return this.storageProfiles;
	}

	protected IStorageBuilder makeStorageBuilderFor(Map<String, Object> configurations) {
		IStorageBuilder result = new StorageBuilder()
				.withStorageProfiles(this.storageProfiles)
				.withConfiguration(configurations);
		return result;
	}

	/**
	 * Sets up the profiles for building a storage
	 *
	 * @param storageProfiles the instantiated storageprofiles
	 * @return
	 */
	@Override
	public IStorageBuilder withStorageProfiles(StorageProfiles storageProfiles) {
		this.storageProfiles = storageProfiles;
		return this;
	}

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
	 * Gets the actual configuration for the key.
	 *
	 * @param key the key of the configuration we want to retrieve.
	 * @return
	 */
	@Override
	public Object getConfiguration(String key) {
		String[] path = key.split("\\.");
		Map<String, Object> configs = navigate(path);
		key = path[path.length - 1];
		return configs.get(key);
	}

	/**
	 * Adds the provided configuration to the set of configuration to
	 * generate a {@link IStorageBuilder}.
	 *
	 * @param value the value we want to set
	 * @param path  tha path to navigate the map to the configuration we really need to change.
	 * @return {@link this} to configure the builder further
	 */
	public IStorageBuilder withConfiguration(Object value, String... path) {
		Map<String, Object> actual = navigate(path);
		String name = path[path.length - 1];
		actual.put(name, value);
		return this;
	}

	/**
	 * Navigate to the map
	 *
	 * @param path the path. the last index has omitted in navigation
	 * @return
	 */
	protected Map<String, Object> navigate(String... path) {
		return this.navigate(this.configs, path);
	}

	/**
	 * Navigate to a map within a source.
	 *
	 * @param source the source map
	 * @param path   the path array
	 * @return the return map.
	 */
	protected Map<String, Object> navigate(Map<String, Object> source, String... path) {
		Map<String, Object> result = source;
		int lastIndex = path.length - 1;
		for (int i = 0; i < lastIndex; ++i) {
			String node = path[i];
			if (!result.containsKey(node)) {
				result.put(node, new HashMap<>());
			}
			result = (Map<String, Object>) result.get(node);
		}
		return result;
	}


	/**
	 * A convinient version of the {@code StorageProfiles#withConfiguration(Object, String[])} version.
	 * It can be applied if the attribute name does not contain dot(.)
	 *
	 * @param name  the name of the attribute we want to change. if it is in an embedded map, use "." to navigate to it.
	 *              for exanmple: configuration.capacty will navigate to the capacity attribute inside the configuration.
	 * @param value the value we want to set
	 * @return {@link this} to configure the builder further
	 */
	@Override
	public IStorageBuilder withConfiguration(String name, Object value) {
		String[] path = name.split("\\.");
		return this.withConfiguration(value, path);
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

		/**
		 * define the profile we use for building the storage
		 */
		public String using = null;
	}
}
