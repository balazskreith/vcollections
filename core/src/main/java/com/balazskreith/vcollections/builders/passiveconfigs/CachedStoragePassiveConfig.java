package com.balazskreith.vcollections.builders.passiveconfigs;

import com.balazskreith.vcollections.storages.IStorage;
import java.util.Map;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class CachedStoragePassiveConfig  extends AbstractStoragePassiveConfig {
	public static final String SUPERSET_FIELD_NAME = "superset";
	public static final String SUBSET_FIELD_NAME = "subset";
	public static final String CACHE_ON_READ_FIELD_NAME = "cacheOnRead";
	public static final String CACHE_ON_CREATE_FIELD_NAME = "cacheOnCreate";
	public static final String CACHE_ON_UPDATE_FIELD_NAME = "cacheOnUpdate";

	@Min(IStorage.NO_MAX_SIZE)
	public long capacity = IStorage.NO_MAX_SIZE;

	@NotNull
	public Map<String, Object> superset;

	@NotNull
	public Map<String, Object> subset;

	public boolean cacheOnCreate = false;

	public boolean cacheOnRead = true;

	public boolean cacheOnUpdate = false;

}
