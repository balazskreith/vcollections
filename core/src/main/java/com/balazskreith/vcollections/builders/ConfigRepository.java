package com.balazskreith.vcollections.builders;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This responsible to give a fully fledged map, no missing environment variable,
 * no using keyword anywhere in the resulted map
 */
public class ConfigRepository implements BiConsumer<InputStream, List<String>>, Function<String, Configurations> {
	private static Logger logger = LoggerFactory.getLogger(ConfigRepository.class);

	private static final String REPOSITORY_KEY_USING = "using";

	private static final String SYSTEM_ENV_PATTERN_REGEX = "\\$\\{([A-Za-z0-9_]+)(?::([^\\}]*))?\\}";

	private static MapMerger deepMerge = new MapMerger();

	private final Pattern systemEnvPattern = Pattern.compile(SYSTEM_ENV_PATTERN_REGEX);

	private Map<String, Configurations> configProfiles;

	public ConfigRepository() {
		this.configProfiles = new HashMap<>();
	}


	@Override
	public void accept(InputStream inputStream, List<String> sourceKeys) {
		// TODO: also check if there is any profileKey duplication
	}

	@Override
	public Configurations apply(String profileKey) {
		return this.load(profileKey);
	}

	public Configurations load(String profileKey) {
		Configurations profileConfig = this.configProfiles.get(profileKey);
		if (Objects.isNull(profileConfig)) {
			throw new RuntimeException("config for profile key " + profileKey + " is not found");
		}
		return profileConfig;
	}

	/**
	 * Checks all values for a configuration may possible have a pattern points to system environments.
	 * If it does found a pattern: ${ENV} or ${ENV:DEFAULT_VALUE} than it tries to get a
	 * system variable name ENV (case sensitive try!), if it does not find it,
	 * it checks if a DEFAULT_VALUE has been set, and assign that.
	 * <p>
	 * NOTE: ${HOST:localhost:1} sets the DEFAULT_VALUE to "localhost:1", but for
	 * IDE parsing reason, or feeling better whatever, you can write `localhost:1`, the
	 * result will be the same.
	 */

	private String convertValue(String value) {
		Matcher matcher = this.systemEnvPattern.matcher(value);

		while (matcher.find()) {
			String ENV = matcher.group(1);
			String envValue = System.getenv(ENV);
			if (envValue == null) {
				envValue = matcher.group(2);
				if (envValue != null) {
					char quote = '`';
					if (envValue.charAt(0) == quote && envValue.charAt(envValue.length() - 1) == quote) {
						envValue = envValue.substring(1, envValue.length() - 1);
					}
				} else {
					// It is necessary to assign an empty striing when nothing has been found
					// because otherwise the subexpr would crash with null.
					envValue = "";
				}
			}
			Pattern subexpr = Pattern.compile(Pattern.quote(matcher.group(0)));
			value = subexpr.matcher(value).replaceAll(envValue);
		}
		return value;
	}

	protected Object check(Object obj) {
		if (obj instanceof String) {
			return this.convertValue((String) obj);
		}
		if (obj instanceof List) {
			List subject = ((List) obj);
			for (int i = 0; i < subject.size(); ++i) {
				Object before = subject.get(i);
				Object after = this.check(before);
				if (!before.equals(after)) {
					subject.set(i, after);
				}
			}
			return subject;
		}

		if (obj instanceof Set) {
			Set subject = ((Set) obj);
			Iterator<Map.Entry<String, Object>> it = subject.iterator();
			for (; it.hasNext(); ) {
				Map.Entry<String, Object> entry = it.next();
				Object before = entry.getValue();
				Object after = this.check(before);
				entry.setValue(after);
			}
			return subject;
		}
		if (obj instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) obj;
			if (map.containsKey(REPOSITORY_KEY_USING)) {
				String usingProfileKey = map.remove(REPOSITORY_KEY_USING).toString();
				Map<String, Object> original = this.load(usingProfileKey);
				map = deepMerge.apply(original, map);
			}
			Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
			for (; it.hasNext(); ) {
				Map.Entry<String, Object> entry = it.next();
				Object beforeValue = entry.getValue();
				Object afterValue = this.check(beforeValue);
				entry.setValue(afterValue);
			}
			return map;
		}
		return obj;
	}

}
