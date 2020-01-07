package vcollections.builders;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBuilder {

	private static Logger logger = LoggerFactory.getLogger(AbstractBuilder.class);

	protected Map<String, Object> configs = new HashMap<>();

	protected <T> T get(String key, Function<Object, T> converter) {
		return this.getOrDefault(key, converter, null);
	}

	protected <T> T getOrDefault(String key, Function<Object, T> converter, T defaultValue) {
		Object value = this.configs.get(key);
		if (value == null) {
			return defaultValue;
		}
		T result = converter.apply(value);
		return result;
	}

	protected void configure(String key, Object value) {
		this.configs.put(key, value);
	}
}
