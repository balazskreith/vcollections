package com.balazskreith.vcollections.builders.passiveconfigs;

import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;

public class ReplicatedStoragesPassiveConfig extends AbstractStoragePassiveConfig {
	public static final String STORAGES_FIELD_NAME = "storages";

	@NotNull
	public List<Map<String, Object>> storages;

}
