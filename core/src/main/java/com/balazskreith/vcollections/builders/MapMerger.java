package com.balazskreith.vcollections.builders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This responsible to give a fully fledged map, no missing environment variable,
 * no using keyword anywhere in the resulted map
 */
public class MapMerger implements BiFunction<Map, Map, Map> {
	private static Logger logger = LoggerFactory.getLogger(MapMerger.class);

	/**
	 * @param original the original map the merge place to
	 * @param newMap   the newmap we merge to the original one
	 * @return the original map extended by the newMap
	 * @see <a href="https://stackoverflow.com/questions/25773567/recursive-merge-of-n-level-maps">source</a>
	 */
	@Override
	public Map apply(Map original, Map newMap) {
		if (newMap == null) {
			return original;
		} else if (original == null) {
			original = new HashMap();
		}
		for (Object key : newMap.keySet()) {
			if (newMap.get(key) instanceof Map && original.get(key) instanceof Map) {
				Map originalChild = (Map) original.get(key);
				Map newChild = (Map) newMap.get(key);
				original.put(key, this.apply(originalChild, newChild));
			} else if (newMap.get(key) instanceof List && original.get(key) instanceof List) {
				List originalChild = (List) original.get(key);
				List newChild = (List) newMap.get(key);
				for (Object each : newChild) {
					if (!originalChild.contains(each)) {
						originalChild.add(each);
					}
				}
			} else {
				original.put(key, newMap.get(key));
			}
		}
		return original;
	}
}
