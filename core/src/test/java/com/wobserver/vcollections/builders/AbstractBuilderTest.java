package com.wobserver.vcollections.builders;

import java.util.HashMap;
import java.util.Map;

abstract class AbstractBuilderTest {

	public static Map<String, Object> makeMap(Object... items) {
		Map<String, Object> result = new HashMap<>();
		for (int i = 0; i + 1 < items.length; i += 2) {
			String key = items[i].toString();
			Object value = items[i + 1];
			result.put(key, value);
		}
		return result;
	}

	protected Map<String, Object> make(Object... items) {
		return makeMap(items);
	}

}