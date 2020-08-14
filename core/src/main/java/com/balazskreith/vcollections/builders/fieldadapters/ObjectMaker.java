package com.balazskreith.vcollections.builders.fieldadapters;

import com.balazskreith.vcollections.adapters.Adapter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * Responsible to create an object based on class name in string
 *
 * @param <T> the type of the object
 */
public class ObjectMaker<T> implements Adapter<String, T> {

	@Override
	public T convert(String className) {
		Class<T> klass;
		try {
			klass = (Class<T>) Class.forName(className);

		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		T result = this.invokeConstructor(klass);
		return result;
	}

	@Override
	public String deconvert(T data) {
		Objects.requireNonNull(data);
		return data.getClass().getName();
	}


	/**
	 * Invokes a constructor for the given class
	 *
	 * @return An instantiated object with a type of {@link T}.
	 * @throws RuntimeException if there was a problem in invocation
	 */
	protected T invokeConstructor(Class<T> klass) {
		Constructor<T> constructor;
		try {
			constructor = klass.getConstructor();

		} catch (NoSuchMethodException e) {
			throw new RuntimeException("No constructor exists which accept () for type " + klass.getName(), e);
		}
		Object constructed;

		try {
			constructed = constructor.newInstance(null);
		} catch (InstantiationException e) {
			throw new RuntimeException("Error by invoking constructor for type " + klass.getName(), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Error by invoking constructor for type " + klass.getName(), e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Error by invoking constructor for type " + klass.getName(), e);
		}
		return (T) constructed;
	}
}
