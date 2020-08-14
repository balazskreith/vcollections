package com.balazskreith.vcollections.builders.passiveconfigs;

import com.balazskreith.vcollections.storages.IStorage;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class FileStoragePassiveConfig extends AbstractStoragePassiveConfig {

	public static final String KEY_ADAPTER_FIELD_NAME = "keyAdapter";
	public static final String PATH_FIELD_NAME = "path";

	public String keyAdapter;

	public String path;

}
