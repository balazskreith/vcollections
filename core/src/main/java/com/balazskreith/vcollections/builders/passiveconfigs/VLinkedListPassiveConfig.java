package com.balazskreith.vcollections.builders.passiveconfigs;

import java.util.Map;
import javax.validation.constraints.NotNull;

public class VLinkedListPassiveConfig {

	public static final String STORAGE_CONFIGURATION = "storage";

	@NotNull
	public Map<String, Object> storage;

	public String keySerDe = null;

	public String head = null;

	public String tail = null;
}
