package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.builders.fieldadapters.ObjectMaker;
import com.balazskreith.vcollections.builders.passiveconfigs.AnyStoragePassiveConfig;
import com.balazskreith.vcollections.storages.MemoryStorage;
import java.util.HashMap;

/**
 * Represents a builder responsible for building a {@link MemoryStorage}.
 */
public class AnyStorageConfigAdapter extends AbstractConfigAdapter<AnyStoragePassiveConfig, StorageBuilder> {

	private final ObjectMaker<StorageBuilder> storageBuilderMaker = new ObjectMaker<StorageBuilder>();

	public AnyStorageConfigAdapter() {
	}

	@Override
	protected StorageBuilder doConvert(AnyStoragePassiveConfig source) {
		StorageBuilder result = storageBuilderMaker.convert(source.builder);
		result.withConfiguration(source.configuration);
		return result;
	}

	@Override
	protected AnyStoragePassiveConfig doDeConvert(StorageBuilder source) {
		AnyStoragePassiveConfig result = new AnyStoragePassiveConfig();
		result.builder = this.storageBuilderMaker.deconvert(source);
		result.configuration = new HashMap<>(source.getConfigurations());
		
		return result;
	}
}
