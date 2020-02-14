package com.wobserver.vcollections.keygenerators;

import com.wobserver.vcollections.storages.IStorage;

/**
 * Interface to provide a common method to setup a KeyGenerator in {@link IStorage}s
 * @param <T> The type of the key
 */
public interface IAccessKeyGenerator<T> {
	/**
	 * Provides an access point to be able to setup a keygenerator
	 * @param value The keygenerator to assign to the object implementing this interface
	 */
	void setKeyGenerator(IKeyGenerator<T> value);

	/**
	 * Gets the actual {@link IKeyGenerator} of the object
	 * @return a {@link IKeyGenerator} {@link this} object has.
	 */
	IKeyGenerator<T> getKeyGenerator();
}
