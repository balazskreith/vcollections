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
public class StorageProfiles {
	/**
	 * The base key we search to reach all configurations belongs to
	 * build storages.
	 */
	public static final String STORAGE_PROFILES_CONFIG_KEY = "storageProfiles";
	/**
	 * The profile key for a specific type of storage
	 */
	public static final String PROFILE_CONFIG_KEY = "profile";
	
	private Map<String, Map<String, Object>> profiles = new HashMap<>();

	/**
	 * Gets storage based on profile key added as configuration.
	 * @param profile The key for the profile the storage configuration belongs to.
	 * @return An {@link IStorage} storage for the given profile key
	 */
	public Map<String, Object> getConfigurationFor(String profile) {
		return this.profiles.get(profile);
	}

	/**
	 * Gets a {@link StorageBuilder} for the profile given as a parameter
	 * @param profile the name of the profile a {@link IStorageBuilder} will given for
	 * @return An {@link IStorageBuilder} for the given profile
	 */
	public IStorageBuilder getStorageBuilderFor(String profile) {
		Map<String, Object> configurations = this.getConfigurationFor(profile);
		if (configurations == null) {
			return null;
		}
		return this.getStorageBuilderFor(configurations);
		
	}

	/**
	 * Gets a {@link StorageBuilder} for the configuration given as a parameter
	 * @param configurations the configurations passed to the {@link StorageBuilder}.
	 * @return An {@link IStorageBuilder} for the given configuration
	 */
	public IStorageBuilder getStorageBuilderFor(Map<String, Object> configurations) {
		if (configurations == null) {
			return null;
		}
		IStorageBuilder builder = new StorageBuilder().withConfiguration(configurations);
		return builder;
	}

	/**
	 * Builds a storage for the given profile
	 * @param profile the name of the profile we request the storage for
	 * @param <K> the key type of the storage
	 * @param <V> the value type of the storage
	 * @return
	 */
	public<K, V> IStorage<K, V> buildStorageFor(String profile) {
		IStorageBuilder builder = this.getStorageBuilderFor(profile);
		if (builder == null) {
			return null;
		}
		return builder.build();
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
		Map<String, Object> storageProfiles = new HashMap<>();
		try (InputStream input = new FileInputStream(yamlFile)) {
			Yaml yaml = new Yaml(new SafeConstructor());
			Iterable<Object> iterable = yaml.loadAll(input);
			for (Iterator<Object> it = iterable.iterator(); it.hasNext(); ) {
				Object obj = it.next();
				if (obj instanceof Map == false) {
					continue;
				}
				Map<String, Object> map = (Map<String, Object>) obj;
				if (map.get(STORAGE_PROFILES_CONFIG_KEY) == null) {
					continue;
				}
				storageProfiles = (Map<String, Object>) map.get(STORAGE_PROFILES_CONFIG_KEY);
				break;
			}
		} catch (Throwable e) {
			// TODO: logging
		}
		this.evaluate(storageProfiles);
	}
	
	private void evaluate(Map<String, Object> storageProfiles) {
		for (Map.Entry<String, Object> entry : storageProfiles.entrySet()) {
			String profile = entry.getKey();
			Map<String, Object> configuration = (Map<String, Object>) entry.getValue();
			this.profiles.put(profile, configuration);
		}
	}
}
