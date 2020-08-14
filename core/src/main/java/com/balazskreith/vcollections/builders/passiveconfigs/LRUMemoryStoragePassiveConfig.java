package com.balazskreith.vcollections.builders.passiveconfigs;

import com.balazskreith.vcollections.utils.SystemClockProvider;

public class LRUMemoryStoragePassiveConfig extends AbstractStoragePassiveConfig {

	public static final int NO_RETENTION_TIME = -1;

	public static final String RETENTION_TIME_FIELD_NAME = "retentionInMs";
	public static final String TIME_IN_MS_PROVIDER = "timeInMsProvider";

	public int retentionInMs = NO_RETENTION_TIME;
	
	public String timeInMsProvider = SystemClockProvider.class.getName();

}
