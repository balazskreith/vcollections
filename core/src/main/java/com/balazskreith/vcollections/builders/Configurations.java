package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.aaaaa.MapMerger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This responsible to give a fully fledged map, no missing environment variable,
 * no using keyword anywhere in the resulted map
 */
class Configurations extends HashMap<String, Object> {
	private static Logger logger = LoggerFactory.getLogger(Configurations.class);
	private static com.balazskreith.vcollections.aaaaa.MapMerger mapMerger = new MapMerger();

	public Configurations(Map<String, Object> configs) {
		this.putAll(configs);
	}

	public Configurations() {

	}


	public <T> T get(String key) {
		return this.get(key, obj -> (T) obj);
	}

	/**
	 * Gets the config belongs to the key, and if it exists, it
	 * converts it using a converter function provided in the params.
	 * If the key does not exist it returns null.
	 *
	 * @param key       The key we are looking for in the so far provided configurations
	 * @param converter The converter converts to the desired type of object if the key exists
	 * @param <T>       The type of the result of the conversion
	 * @return The result of the convert operation if the key exists, null otherwise
	 */
	public <T> T get(String key, Function<Object, T> converter) {
		return this.getOrDefault(key, converter, null);
	}

	/**
	 * Gets the config belongs to the key, and if it exists, it
	 * converts it using a converter function provided in the params.
	 * If the key does not exist it returns the defaultValue.
	 *
	 * @param key          The key we are looking for in the so far provided configurations
	 * @param converter    The converter converts to the desired type of object if the key exists
	 * @param defaultValue The default value returned if the key does not exist
	 * @param <T>          The type of the result of the conversion
	 * @return The result of the convert operation if the key exists, defaultValue otherwise
	 */
	public <T> T getOrDefault(String key, Function<Object, T> converter, T defaultValue) {
		Object value = this.get(key);
		if (value == null) {
			return defaultValue;
		}
		T result = converter.apply(value);
		return result;
	}

	/**
	 * Adds a key - value pair to the configuration map
	 *
	 * @param key   The key we bound the value to
	 * @param value The value we store for the corresponding key
	 */
	public void set(String key, Object value) {
		this.put(key, value);
	}

	public Configurations withConfigs(Map<String, Object> map) {
		this.putAll(map);
		return this;
	}

	public Configurations mergeWith(Map<String, Object> otherMap) {
		mapMerger.apply(this, otherMap);
		return this;
	}
}
