package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.activeconfigs.FileStorageActiveConfig;
import com.balazskreith.vcollections.adapters.Adapter;
import com.balazskreith.vcollections.adapters.SerDe;
import com.balazskreith.vcollections.builders.fieldadapters.ObjectMaker;
import com.balazskreith.vcollections.builders.passiveconfigs.FileStoragePassiveConfig;
import com.balazskreith.vcollections.storages.FileStorage;
import com.balazskreith.vcollections.storages.IStorage;
import com.balazskreith.vcollections.storages.MemoryStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Represents a builder responsible for building a {@link MemoryStorage}.
 */
public class FileStorageBuilder extends AbstractStorageBuilder {

	public FileStorageBuilder(ObjectMapper objectMapper) {
		super(objectMapper);
	}

	public FileStorageBuilder() {
		this(new ObjectMapper());
	}

	/**
	 * Builds an {@link MemoryStorage} based on the provided configurations.
	 *
	 * @param <K> The type of the key for the {@link IStorage}.
	 * @param <V> The type of the value for the {@link IStorage}.
	 * @return Returns an {@link MemoryStorage} set up with the given configurations
	 */
	@Override
	public <K, V> IStorage<K, V> build() {
		FileStorageConfigAdapter<K, V> configAdapter = new FileStorageConfigAdapter<>();
		FileStoragePassiveConfig passiveConfig = this.getObjectMapper().convertValue(this.getConfigs(), FileStoragePassiveConfig.class);
		FileStorageActiveConfig<K, V> activeConfig = configAdapter.convert(passiveConfig);
		IStorage<K, V> result = new FileStorage<K, V>(activeConfig);
		return result;
	}

	public <T> FileStorageBuilder withKeyAdapter(Adapter<T, String> adapter) {
		ObjectMaker<Adapter<T, String>> adapterMaker = new ObjectMaker<>();
		String adapterValue = adapterMaker.deconvert(adapter);
		this.getConfigs().set(FileStoragePassiveConfig.KEY_ADAPTER_FIELD_NAME, adapterValue);
		return this;
	}

	public FileStorageBuilder withPath(Path path) {
		this.getConfigs().set(FileStoragePassiveConfig.PATH_FIELD_NAME, path);
		return this;
	}

}
