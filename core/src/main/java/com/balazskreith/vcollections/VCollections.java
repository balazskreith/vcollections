package com.balazskreith.vcollections;

import com.balazskreith.vcollections.builders.ConfigurationLoader;
import com.balazskreith.vcollections.builders.IConfigurationLoader;
import com.balazskreith.vcollections.builders.IConfigurationProfiles;
import com.balazskreith.vcollections.builders.StorageBuilder;
import com.balazskreith.vcollections.storages.IStorage;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * The main class for the library
 */
public class VCollections {

	private static final String DEFAULT_SOURCE_KEY = "vcollections";

	public static VCollections loadFor(File file) {
		return VCollections
				.builder()
				.withYaml(file)
				.withSourceKey(DEFAULT_SOURCE_KEY)
				.build();
	}

	public static VCollections loadFor(File file, String sourceKey) {
		return VCollections
				.builder()
				.withYaml(file)
				.withSourceKey(sourceKey)
				.build();
	}

	public static Builder builder() {
		return new VCollections.Builder();
	}

	private final IConfigurationProfiles profiles;

	private VCollections(IConfigurationProfiles profiles) {
		this.profiles = profiles;
	}

	public <K, V> Map<K, V> getMapFor(String profileKey) {
		return null;
	}

	public MapBuilder getMapBuilderFor(String profileKey) {
		return null;
	}

	public StorageBuilder getStorageBuilderFor(String profileKey) {
		return null;
	}

	public <K, V> IStorage<K, V> getStorageFor(String profileKey) {

	}

	public CoreStorageBuilders getCoreStorageBuilders() {
		return null;
	}

	public <T> List<T> getListFor(String profileKey) {
		return null;
	}

	public <T> T getStorageBuilders(Class<T> storageBuildersKlass) {
		return null;
	}

	public StorageBuilder buildStorage(String profileKey) {
		return null;
	}

	public static class Builder {
		IConfigurationLoader loader;
		String sourceKey = DEFAULT_SOURCE_KEY;

		private Builder() {
			this.loader = new ConfigurationLoader();
		}

		public Builder withYaml(InputStream input) {
			this.loader.withYaml(input);
			return this;
		}

		public Builder withYaml(File input) {
			this.loader.withYaml(input);
			return this;
		}

		public Builder withYaml(String input) {
			this.loader.withYaml(input);
			return this;
		}

		public Builder withSourceKey(String sourceKey) {
			this.sourceKey = sourceKey;
			return this;
		}

		public VCollections build() {
			IConfigurationProfiles configurationProfiles = this.loader.getConfigurationSourceFor(this.sourceKey);
			VCollections result = new VCollections(configurationProfiles);
			return result;
		}

	}

}
