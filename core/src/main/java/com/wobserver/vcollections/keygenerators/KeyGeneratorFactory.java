package com.wobserver.vcollections.keygenerators;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible to generate a {@link IKeyGenerator} in the provided range.
 */
public class KeyGeneratorFactory {
	/**
	 * The logger of the class
	 */
	private static Logger logger = LoggerFactory.getLogger(KeyGeneratorFactory.class);

	/**
	 * Constructs the factory to generate a {@link IKeyGenerator}
	 */
	public KeyGeneratorFactory() {

	}

	/**
	 * Makes a {@link IKeyGenerator} based on the provided class given as a parameter.
	 * If, for the provided type of class implements {@link IKeyGenerator} exists, then it is built
	 * for that object. Otherwise the provided class constructor is called if that type of class
	 * implements the {@link IKeyGenerator} interface.
	 * @param klass The type of the manifestation of the {@link IKeyGenerator}
	 * @param <T> The type of the key the {@link IKeyGenerator} will provide
	 * @return An {@link IKeyGenerator} for the provided type of class
	 */
	public <T> IKeyGenerator<T> make(Class klass) {
		return this.make(klass, 0, 0);
	}

	/**
	 * Makes a {@link IKeyGenerator} based on the provided class given as a parameter.
	 * If, for the provided type of class implements {@link IKeyGenerator} exists, then it is built
	 * for that object. Otherwise the provided class constructor is called if that type of class
	 * implements the {@link IKeyGenerator} interface.
	 * @param klass The type of the manifestation of the {@link IKeyGenerator}
	 * @param <T> The type of the key the {@link IKeyGenerator} will provide
	 * @param minSize the minimum size of the key
	 * @param maxSize the maximum size of the key
	 * @return An {@link IKeyGenerator} for the provided type of class
	 */
	public <T> IKeyGenerator<T> make(Class klass, int minSize, int maxSize) {
		IKeyGenerator<T> result = this.make(klass.getName(), minSize, maxSize);
		if (result != null) {
			return result;
		}
		Constructor<T> constructor;
		boolean useMinMaxConstructor = minSize != maxSize;
		try {
			if (useMinMaxConstructor) {
				constructor = klass.getConstructor(Integer.class, Integer.class);	
			} else {
				constructor = klass.getConstructor();
			}
			 
		} catch (NoSuchMethodException e) {
			logger.warn("No constructor exists which accept (int, int) for type " + klass.getName(), e);
			return null;
		}
		Object constructed;
		try {
			if (useMinMaxConstructor) {
				constructed = constructor.newInstance(minSize, maxSize);	
			} else {
				constructed = constructor.newInstance();
			}
			
		} catch (InstantiationException e) {
			logger.warn("Error by invoking constructor for type " + klass.getName(), e);
			return null;
		} catch (IllegalAccessException e) {
			logger.warn("Error by invoking constructor for type " + klass.getName(), e);
			return null;
		} catch (InvocationTargetException e) {
			logger.warn("Error by invoking constructor for type " + klass.getName(), e);
			return null;
		}
		if (constructed.getClass().isAssignableFrom(IKeyGenerator.class) == false) {
			logger.warn("The generated object {} does not implement the {}", constructed.getClass().getName(), IKeyGenerator.class.getName());
			return null;
		}
		result = (IKeyGenerator<T>) constructed;
		return result;
	}

	/**
	 * Check if for the the provided type a built in keygenerator exists.
	 * @param typeName The type of the key
	 * @param minSize The minimal size for the key
	 * @param maxSize The maximal size of the key
	 * @param <T> The type of the keys
	 * @return {@link IKeyGenerator} if a built in keygenerator exists for the provided type,
	 * null otherwise
	 */
	public <T> IKeyGenerator<T> make(String typeName, int minSize, int maxSize) {
		if (typeName.equals(UUID.class.getName())) {
			if (minSize != maxSize || (minSize != 0 && minSize != 128 && minSize == 36)) {
				throw new IllegalArgumentException("For generating UUID, size cannot be different than 0, 36, 128");
			}
			return (IKeyGenerator<T>) new UUIDGenerator();
		}

		if (typeName.equals(String.class.getName())) {
			return (IKeyGenerator<T>) new RandomStringGenerator(minSize, maxSize);
		}

		if (typeName.equals(Integer.class.getName())) {
			return (IKeyGenerator<T>) new RandomIntegerGenerator(minSize, maxSize);
		}

		if (typeName.equals(Long.class.getName())) {
			return (IKeyGenerator<T>) new RandomLongGenerator(minSize, maxSize);
		}
		return null;
	}
}

