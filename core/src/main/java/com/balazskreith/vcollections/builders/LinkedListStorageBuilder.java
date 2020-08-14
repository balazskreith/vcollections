package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.VLinkedListNode;
import com.balazskreith.vcollections.activeconfigs.LinkedListStorageActiveConfig;
import com.balazskreith.vcollections.adapters.SerDe;
import com.balazskreith.vcollections.builders.fieldadapters.ObjectMaker;
import com.balazskreith.vcollections.builders.passiveconfigs.AbstractStoragePassiveConfig;
import com.balazskreith.vcollections.builders.passiveconfigs.LinkedListStoragePassiveConfig;
import com.balazskreith.vcollections.storages.IStorage;
import com.balazskreith.vcollections.storages.LinkedListStorage;
import com.balazskreith.vcollections.storages.MemoryStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Represents a builder responsible for building a {@link MemoryStorage}.
 */
public class LinkedListStorageBuilder extends AbstractBuilder {

	public LinkedListStorageBuilder(ObjectMapper objectMapper) {
		super(objectMapper);
	}

	public LinkedListStorageBuilder() {
		this(new ObjectMapper());
	}

	/**
	 * Builds an {@link MemoryStorage} based on the provided configurations.
	 *
	 * @param <K> The type of the key for the {@link IStorage}.
	 * @param <V> The type of the value for the {@link IStorage}.
	 * @return Returns an {@link MemoryStorage} set up with the given configurations
	 */
	public <K, V> IStorage<K, VLinkedListNode<K, V>> build() {
		LinkedListStoragesConfigAdapter<K, V> configAdapter = new LinkedListStoragesConfigAdapter<>();
		LinkedListStoragePassiveConfig config = this.getObjectMapper().convertValue(this.getConfigs(), LinkedListStoragePassiveConfig.class);
		LinkedListStorageActiveConfig<K, V> activeConfig = configAdapter.convert(config);
		LinkedListStorage<K, V> result = new LinkedListStorage<>(activeConfig);
		return result;
	}

	/**
	 * Puts all configuration to the inside holder in the builder.
	 * These configurations are used when the {@link this#build()} is called.
	 *
	 * @param configs The provided configurations
	 * @return {@link this} builder.
	 */
	public LinkedListStorageBuilder withConfiguration(Map<String, Object> configs) {
		this.configs.putAll(configs);
		return this;
	}


	public Map<String, Object> getConfigurations() {
		return this.getConfigs();
	}

	public LinkedListStorageBuilder withKeysStorageBuilder(StorageBuilder storageBuilder) {
		return this.withKeysStorageConfiguration(storageBuilder.getConfigurations());
	}

	public LinkedListStorageBuilder withKeysStorageConfiguration(Map<String, Object> storageConfiguration) {
		this.getConfigs().put(LinkedListStoragePassiveConfig.KEYS_STORAGE_CONFIGURATION, storageConfiguration);
		return this;
	}

	public LinkedListStorageBuilder withValuesStorageBuilder(StorageBuilder storageBuilder) {
		return this.withValuesStorageConfiguration(storageBuilder.getConfigurations());
	}

	public LinkedListStorageBuilder withValuesStorageConfiguration(Map<String, Object> storageConfiguration) {
		this.getConfigs().put(LinkedListStoragePassiveConfig.VALUES_STORAGE_CONFIGURATION, storageConfiguration);
		return this;
	}

	public <T> LinkedListStorageBuilder withValueSerDe(SerDe<T> valueSerde) {
		ObjectMaker<SerDe<T>> serDeMaker = new ObjectMaker<>();
		String serDeValue = serDeMaker.deconvert(valueSerde);
		return this.withValueSerDe(serDeValue);
	}

	public LinkedListStorageBuilder withValueSerDe(String klassName) {
		this.getConfigs().set(AbstractStoragePassiveConfig.VALUE_SERDE_FIELD_NAME, klassName);
		return this;
	}

	public <T> LinkedListStorageBuilder withKeySerDe(SerDe<T> valueSerde) {
		ObjectMaker<SerDe<T>> serDeMaker = new ObjectMaker<>();
		String serDeValue = serDeMaker.deconvert(valueSerde);
		return this.withValueSerDe(serDeValue);
	}

	public <T> LinkedListStorageBuilder withKeySupplier(Supplier<T> keySupplier) {
		ObjectMaker<Supplier<T>> keySupplierMaker = new ObjectMaker<>();
		String keySupplierValue = keySupplierMaker.deconvert(keySupplier);
		return this.withKeySupplier(keySupplierValue);
	}

	public LinkedListStorageBuilder withKeySupplier(String klassName) {
		this.getConfigs().set(AbstractStoragePassiveConfig.KEY_SUPPLIER_FIELD_NAME, klassName);
		return this;
	}

	public LinkedListStorageBuilder withCapacity(long capacity) {
		this.getConfigs().set(AbstractStoragePassiveConfig.CAPACITY_FIELD_NAME, capacity);
		return this;
	}

	public <T> T getConfiguration(String key) {
		return this.getConfigs().get(key);
	}

}
