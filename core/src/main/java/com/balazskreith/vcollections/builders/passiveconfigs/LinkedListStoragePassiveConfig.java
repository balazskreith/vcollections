package com.balazskreith.vcollections.builders.passiveconfigs;

import java.util.Map;
import javax.validation.constraints.NotNull;

public class LinkedListStoragePassiveConfig extends AbstractStoragePassiveConfig {

	public static final String KEYS_STORAGE_CONFIGURATION = "keysStorage";
	public static final String VALUES_STORAGE_CONFIGURATION = "valuesStorage";

	@NotNull
	public Map<String, Object> keysStorage;

	@NotNull
	public Map<String, Object> valuesStorage;

}
