package com.wobserver.vcollections.builders;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractBuilderTester {

	private static Map deepMerge(Map original, Map newMap) {
		return AbstractStorageBuilder.deepMerge(original, newMap);
	}

	protected static Map<String, Object> makeMap(Object... items) {
		Map<String, Object> result = new HashMap<>();
		for (int i = 0; i + 1 < items.length; i += 2) {
			String key = items[i].toString();
			Object value = items[i + 1];
			result.put(key, value);
		}
		return result;
	}

	private Map<String, Object> configurations = new HashMap<>();

	private StorageProfiles storageProfiles = null;

	/**
	 * Reset the configuration built so far
	 */
	@BeforeEach
	public void setup() {
		this.configurations.clear();

		File file = this.getSourceFile();
		this.storageProfiles = new StorageProfiles();
		try {
			this.storageProfiles.addYamlFile(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	protected Map<String, Object> navigate(String... options) {
		if (options.length < 2) {
			return this.configurations;
		}
		Map<String, Object> result = this.configurations;
		int c = options.length - 1;
		for (int i = 0; i < c; ++i) {
			String key = options[i];
			if (!result.containsKey(key)) {
				Map<String, Object> map = new HashMap<>();
				result.put(key, map);
				result = map;
			} else {
				result = (Map<String, Object>) result.get(key);
			}
		}
		return result;
	}

	protected StorageProfiles getStorageProfiles() {
		return this.storageProfiles;
	}

	protected AbstractBuilderTester withStorageProfile(String profileName) {
		Map<String, Object> profileConfigurations = this.storageProfiles.getConfigurationFor(profileName);
		deepMerge(this.configurations, profileConfigurations);
		return this;
	}

	protected AbstractBuilderTester with(Object value, String... keys) {
		Map<String, Object> configurations = navigate(keys);
		String key = keys[keys.length - 1];
		configurations.put(key, value);
		return this;
	}

	protected <T> T get(String... keys) {
		Map<String, Object> configurations = navigate(keys);
		String key = keys[keys.length - 1];
		return (T) configurations.get(key);
	}

	protected IStorageBuilder makeBuilder() {
		IStorageBuilder builder = new StorageBuilder();
		return builder
				.withConfiguration(this.configurations)
				.withStorageProfiles(this.storageProfiles);
	}

	protected abstract File getSourceFile();

}