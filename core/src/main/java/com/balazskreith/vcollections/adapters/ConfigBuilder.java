package com.balazskreith.vcollections.adapters;

import java.util.Map;

public class ConfigBuilder<T extends PassiveStorageConfig> implements Adapter<Map<String, Object>, T> {
	@Override
	public T convert(Map<String, Object> data) {
		return null;
	}

	@Override
	public Map<String, Object> deconvert(T data) {
		return null;
	}
}
