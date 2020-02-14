package com.wobserver.vcollections;

import java.util.Collection;

/**
 * Test a storage for general capabilities according to the IStorage interface
 * The target storage needs to inherit this one and overwrite the makeStorage protected methods to test the actual storage
 */

public interface CollectionTest<T extends Collection<String>> {

	Collection<String> makeCollection(String... items);

	/**
	 * METHODS:
	 * <p>
	 * size, isEmpty, contains, iterator, toArray, toArray(T1[]), add, remove, containsAll, removeAll,
	 * retainAll, clear, equals, hashCode
	 */
	default void somethinf() {
	}
}
