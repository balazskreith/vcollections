package com.balazskreith.vcollections.builders;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
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
 * @since 0.1
 */
public abstract class AbstractBuilder {

	private static Logger logger = LoggerFactory.getLogger(AbstractBuilder.class);

	private final ObjectMapper mapper;
	protected Configurations configs;


	/**
	 * Constructs an abstract builder
	 */
	public AbstractBuilder(ObjectMapper objectMapper) {
		this.mapper = objectMapper;
		this.configs = new Configurations();
	}

	protected <T> T convertAndValidate(Class<T> klass) {
		// a comment, to not to let the IDE make it in one line
		return this.convertAndValidate(klass, this.configs);
	}

	protected ObjectMapper getObjectMapper() {
		return this.mapper;
	}

	protected Configurations getConfigs() {
		return this.configs;
	}

	/**
	 * Converts the provided configuration to the type of object provided as a parameter, and
	 * validates the conversion.
	 *
	 * @param klass The type of the object we want to convert the configuration to
	 * @param <T>   The type of the result we return after the conversion
	 * @return An object of the desired type setup with values from the configuration.
	 * @throws ConstraintViolationException if the validation fails during the conversion.
	 */
	protected <T> T convertAndValidate(Class<T> klass, Map<String, Object> configs) {
		T result = this.mapper.convertValue(configs, klass);
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
}
