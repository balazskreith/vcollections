package com.wobserver.vcollections.builders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.constructor.SafeConstructor;
import com.wobserver.vcollections.storages.IStorage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * The main class to provide storages based on a given configuration.
 * It process the configurations stores a key-value pair, and for the
 * provided profile keys it callls the {@link StorageBuilder} with the
 * corresponding configurations.
 */
public class StorageProvider {
	/**
	 * The base key we search to reach all configurations belongs to
	 * build storages.
	 */
	public static final String STORAGES_CONFIG_KEY = "storages";
	/**
	 * The profile key for a specific type of storage
	 */
	public static final String KEY_CONFIG_KEY = "key";
	/**
	 * The stored builders after processing the configurations.
	 */
	private Map<String, StorageBuilder> builders = new HashMap<>();

	/**
	 * Gets storage based on profile key added as configuration.
	 * @param key The key for the profile the storage configuration belongs to.
	 * @param <K> The type of the key for the {@link IStorage}
	 * @param <V> The type of the value for the {@link IStorage}
	 * @return An {@link IStorage} storage for the given profile key
	 */
	public <K, V> IStorage<K, V> get(String key) {
		StorageBuilder storageBuilder = this.builders.get(key);
		return storageBuilder.build();
	}
	
	/**
	 * Adds a Map based configuration to process for
	 * @param configurations the configurations
	 */
	public void addYamlString(Map<String, Object> configurations) {
		Map<String, Object> unmodifiableConfigurations = Collections.unmodifiableMap(configurations);
		this.evaluate(unmodifiableConfigurations);
	}

	/**
	 * Add a json file contains a configuration to build the storage
	 * @param jsonFile the file pointing to the json file holding the configuration
	 * @throws IOException when the an error occured during the reading or converting
	 */
	public void addJsonFile(File jsonFile) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> storagesMap = mapper.readValue(
				jsonFile, new TypeReference<Map<String, Object>>() {
				});
		this.evaluate(storagesMap);
	}

	/**
	 * Add a json string contains a configuration to build the storage
	 * @param jsonString the string holding the json file holding the configuration
	 * @throws IOException when the an error occured during the reading or converting
	 */
	public void addJsonString(String jsonString) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> storagesMap = mapper.readValue(
				jsonString, new TypeReference<Map<String, Object>>() {
				});
		this.evaluate(storagesMap);
	}

	/**
	 * Adds a yaml structured string to process configuration
	 * @param yaml a string structured as yaml
	 * @throws IOException if the parsing of the yaml string is unsuccessful
	 */
	public void addYamlString(String yaml) throws IOException {
		YAMLFactory yamlFactory = new YAMLFactory();
		YAMLMapper mapper = new YAMLMapper(yamlFactory);
		Map<String, Object> input = mapper.readValue(yaml, Map.class);
		this.evaluate(input);
	}
	
	/**
	 * Adds a yaml file to process as a configuration
	 * @param yamlFile the file name to process
	 * @throws IOException if the parsing of the yaml string is unsuccessful
	 */
	public void addYamlFile(File yamlFile) throws IOException {
		Map<String, Object> storagesMap = null;
		try (InputStream input = new FileInputStream(yamlFile)) {
			Yaml yaml = new Yaml(new SafeConstructor());
			Iterable<Object> iterable = yaml.loadAll(input);
			for (Iterator<Object> it = iterable.iterator(); it.hasNext(); ) {
				Object obj = it.next();
				if (obj instanceof Map == false) {
					continue;
				}
				Map<String, Object> map = (Map<String, Object>) obj;
				if (map.get(STORAGES_CONFIG_KEY) == null) {
					continue;
				}
				storagesMap = map;
				break;
			}
		} catch (Throwable e) {
			// TODO: logging
		}
		this.evaluate(storagesMap);
	}
	
	private void evaluate(Map<String, Object> input) {
		List<Map<String, Object>> storages = (List<Map<String, Object>>) input.get(STORAGES_CONFIG_KEY);
		for (Map<String, Object> storage : storages) {
			String key = (String) storage.get(KEY_CONFIG_KEY);
			StorageBuilder storageBuilder = new StorageBuilder();
			storageBuilder.withConfiguration(storage);
			this.builders.put(key, storageBuilder);
		}
	}
}
