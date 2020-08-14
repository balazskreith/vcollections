package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.activeconfigs.AbstractStorageActiveConfig;
import com.balazskreith.vcollections.activeconfigs.FileStorageActiveConfig;
import com.balazskreith.vcollections.adapters.Adapter;
import com.balazskreith.vcollections.adapters.SerDe;
import com.balazskreith.vcollections.builders.fieldadapters.ObjectMaker;
import com.balazskreith.vcollections.builders.fieldadapters.PathAdapter;
import com.balazskreith.vcollections.builders.passiveconfigs.FileStoragePassiveConfig;
import java.nio.file.Path;
import java.util.function.Supplier;

public class FileStorageConfigAdapter<K, V> extends AbstractStorageConfigAdapter<K, V, FileStoragePassiveConfig, FileStorageActiveConfig<K, V>> {

	private final ObjectMaker<Adapter<K, String>> keyAdapterMaker;
	private final PathAdapter pathAdapter;

	public FileStorageConfigAdapter() {
		this.keyAdapterMaker = new ObjectMaker<>();
		this.pathAdapter = new PathAdapter();
	}

	@Override
	protected FileStorageActiveConfig<K, V> doConvert(FileStoragePassiveConfig source) {
		AbstractStorageActiveConfig<K, V> abstractStorageActiveConfig = this.getAbstractStorageActiveConfig(source);
		Adapter<K, String> keyAdapter = this.keyAdapterMaker.convert(source.keyAdapter);
		Path path = this.pathAdapter.convert(source.path);
		FileStorageActiveConfig<K, V> result = new FileStorageActiveConfig(abstractStorageActiveConfig,
				keyAdapter,
				path
		);
		return result;
	}

	@Override
	protected FileStoragePassiveConfig doDeConvert(FileStorageActiveConfig<K, V> source) {
		FileStoragePassiveConfig result = new FileStoragePassiveConfig();
		this.setupAbstractStoragePassiveConfig(result, source);
		result.keyAdapter = this.keyAdapterMaker.deconvert(source.keyAdapter);
		result.path = this.pathAdapter.deconvert(source.path);
		return result;
	}
}
