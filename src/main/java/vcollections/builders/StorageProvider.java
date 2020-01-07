package vcollections.builders;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.constructor.SafeConstructor;
import vcollections.storages.IStorage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class StorageProvider {
	public static final String KEY_CONFIG_KEY = "key";
	public static final String STORAGES_CONFIG_KEY = "storages";

	private Map<String, StorageBuilder> builders = new HashMap<>();

	public void add(String yaml) throws IOException {
		YAMLFactory yamlFactory = new YAMLFactory();
		YAMLMapper mapper = new YAMLMapper(yamlFactory);
		Map<String, Object> input = mapper.readValue(yaml, Map.class);
		this.evaluate(input);
	}

	public void add(Map<String, Object> configurations) {
		Map<String, Object> unmodifiableConfigurations = Collections.unmodifiableMap(configurations);
		this.evaluate(unmodifiableConfigurations);
	}

	public void add(File yamlFile) throws IOException {
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

	public <K, V> IStorage<K, V> get(String key) {
		StorageBuilder storageBuilder = this.builders.get(key);
		return storageBuilder.build();
	}
}
