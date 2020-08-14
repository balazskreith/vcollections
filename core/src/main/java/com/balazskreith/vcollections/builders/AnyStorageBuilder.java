package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.adapters.SerDe;
import com.balazskreith.vcollections.builders.fieldadapters.ObjectMaker;
import com.balazskreith.vcollections.builders.passiveconfigs.AbstractStoragePassiveConfig;
import com.balazskreith.vcollections.builders.passiveconfigs.AnyStoragePassiveConfig;
import com.balazskreith.vcollections.storages.IStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Objects;

public class AnyStorageBuilder extends AbstractStorageBuilder {

	private final AnyStorageConfigAdapter configAdapter = new AnyStorageConfigAdapter();

	public AnyStorageBuilder(ObjectMapper objectMapper) {
		super(objectMapper);
	}

	public AnyStorageBuilder() {
		this(new ObjectMapper());
	}

	@Override
	public <K, V> IStorage<K, V> build() {
		String keySupplierKlassName = (String) this.getConfigs().remove(AbstractStoragePassiveConfig.KEY_SUPPLIER_FIELD_NAME);
		String valueSerDeKlassName = (String) this.getConfigs().remove(AbstractStoragePassiveConfig.VALUE_SERDE_FIELD_NAME);
		Long capacity = (Long) this.getConfigs().remove(AbstractStoragePassiveConfig.CAPACITY_FIELD_NAME);
		StorageBuilder storageBuilder = this.makeStorageBuilder();
		if (!Objects.isNull(capacity)) {
			storageBuilder.withCapacity(capacity);
		}

		if (!Objects.isNull(keySupplierKlassName)) {
			storageBuilder.withKeySupplier(keySupplierKlassName);
		}

		if (!Objects.isNull(valueSerDeKlassName)) {
			storageBuilder.withValueSerDe(valueSerDeKlassName);
		}

		IStorage<K, V> result = storageBuilder.build();
		return result;
	}

	public StorageBuilder makeStorageBuilder() {
		AnyStoragePassiveConfig passiveConfig = this.getObjectMapper().convertValue(this.getConfigs(), AnyStoragePassiveConfig.class);
		StorageBuilder result = this.configAdapter.convert(passiveConfig);
		return result;
	}

	public AnyStorageBuilder withBuilder(StorageBuilder builder) {
		ObjectMaker<StorageBuilder> storageBuilderObjectMaker = new ObjectMaker<>();
		String className = storageBuilderObjectMaker.deconvert(builder);
		return this.withBuilder(className);
	}

	public AnyStorageBuilder withBuilder(String className) {
		this.getConfigs().set(AnyStoragePassiveConfig.BUILDER_FIELD_NAME, className);
		return this;
	}

	public AnyStorageBuilder withBuilderConfiguration(Map<String, Object> configs) {
		this.getConfigs().set(AnyStoragePassiveConfig.CONFIGURATION_FIELD_NAME, configs);
		return this;
	}

	public AnyStorageBuilder withConfiguration(AnyStoragePassiveConfig config) {
		return this
				.withBuilder(config.builder)
				.withBuilderConfiguration(config.configuration);
	}
}
