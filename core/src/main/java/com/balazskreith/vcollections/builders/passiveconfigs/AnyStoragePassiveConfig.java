package com.balazskreith.vcollections.builders.passiveconfigs;

import java.util.Map;

public class AnyStoragePassiveConfig extends AbstractStoragePassiveConfig {

	public static final String BUILDER_FIELD_NAME = "builder";
	public static final String CONFIGURATION_FIELD_NAME = "configuration";

	public String builder;

	public Map<String, Object> configuration;

}
