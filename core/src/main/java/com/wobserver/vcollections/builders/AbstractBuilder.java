package com.wobserver.vcollections.builders;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.validation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a skeletal implementation for builder classes
 * to minimize the effort required to implement a builder
 *
 * <p>To implement any kind of builder, it is recommended to extend this class
 * and use the {@link this#convertAndValidate(Class)} method, which validates and
 * converts the provided configuration (which is a {@link Map<String, Object>} type)
 * to the desired class, and throws {@link ConstraintViolationException} if
 * a validation fails.
 *
 * <p>The programmer should generally provide the configuration keys in the
 * extended class.
 *
 * @author Balazs Kreith
 * @since 0.7
 */
public abstract class AbstractBuilder {

	private static Logger logger = LoggerFactory.getLogger(AbstractBuilder.class);

	private ObjectMapper mapper = new ObjectMapper();

	protected Map<String, Object> configs = new HashMap<>();

	/**
	 * Converts the provided configuration to the type of object provided as a parameter, and
	 * validates the conversion.
	 *
	 * @param klass The type of the object we want to convert the configuration to
	 * @param <T>   The type of the result we return after the conversion
	 * @return An object of the desired type setup with values from the configuration.
	 * @throws ConstraintViolationException if the validation fails during the conversion.
	 */
	protected <T> T convertAndValidate(Class<T> klass) {
		T result = this.mapper.convertValue(this.configs, klass);
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<T>> violations = validator.validate(result);

		if (violations != null && !violations.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (ConstraintViolation<T> constraintViolation : violations) {
				sb.append(constraintViolation.getMessage())
						.append(" ");
			}

			String errorMessage = sb.toString();

			if (logger.isDebugEnabled()) {
				logger.debug(errorMessage);
			}

			throw new ConstraintViolationException(violations);
		}
		return result;
	}

	/**
	 * Gets the config belongs to the key, and if it exists, it
	 * converts it using a converter function provided in the params.
	 * If the key does not exist it returns null.
	 *
	 * @param key       The key we are looking for in the so far provided configurations
	 * @param converter The converter converts to the desired type of object if the key exists
	 * @param <T>       The type of the result of the conversion
	 * @return The result of the convert operation if the key exists, null otherwise
	 */
	protected <T> T get(String key, Function<Object, T> converter) {
		return this.getOrDefault(key, converter, null);
	}

	/**
	 * Gets the config belongs to the key, and if it exists, it
	 * converts it using a converter function provided in the params.
	 * If the key does not exist it returns the defaultValue.
	 *
	 * @param key          The key we are looking for in the so far provided configurations
	 * @param converter    The converter converts to the desired type of object if the key exists
	 * @param defaultValue The default value returned if the key does not exist
	 * @param <T>          The type of the result of the conversion
	 * @return The result of the convert operation if the key exists, defaultValue otherwise
	 */
	protected <T> T getOrDefault(String key, Function<Object, T> converter, T defaultValue) {
		Object value = this.configs.get(key);
		if (value == null) {
			return defaultValue;
		}
		T result = converter.apply(value);
		return result;
	}

	/**
	 * Adds a key - value pair to the configuration map
	 *
	 * @param key   The key we bound the value to
	 * @param value The value we store for the corresponding key
	 */
	protected void configure(String key, Object value) {
		this.configs.put(key, value);
	}

	/**
	 * Gets a klass corresponding to the name of the class
	 *
	 * @param className the name of the class
	 * @param <T>       the type of the class
	 * @return the class type
	 * @throws InvalidConfigurationException if the type of the klass does not exists
	 */
	protected <T> Class<T> getClassFor(String className) {
		Class<T> result;
		try {
			result = (Class<T>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new InvalidConfigurationException("Class type for " + className + " does not exist");
		}
		return result;
	}

	/**
	 * Invokes a constructor for the given class
	 *
	 * @param className the name of the class
	 * @param params    the parameters given to the constructor when we invokes it
	 * @param <T>       the type of the class
	 * @return An instantiated object with a type of {@link T}.
	 * @throws InvalidConfigurationException if there was a problem in invocation
	 */
	protected <T> T invoke(String className, Object... params) {

		Class<T> klass = this.getClassFor(className);

		Constructor<T> constructor;
		try {
			constructor = klass.getConstructor();
		} catch (NoSuchMethodException e) {
			throw new InvalidConfigurationException(e, "No constructor exists which accept () for type " + klass.getName());
		}
		Object constructed;
		try {
			constructed = constructor.newInstance(params);
		} catch (InstantiationException e) {
			throw new InvalidConfigurationException(e, "Error by invoking constructor for type " + klass.getName());
		} catch (IllegalAccessException e) {
			throw new InvalidConfigurationException(e, "Error by invoking constructor for type " + klass.getName());
		} catch (InvocationTargetException e) {
			throw new InvalidConfigurationException(e, "Error by invoking constructor for type " + klass.getName());
		}
		return (T) constructed;
	}
}
