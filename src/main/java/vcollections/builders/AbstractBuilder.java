package vcollections.builders;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.validation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBuilder {

	private static Logger logger = LoggerFactory.getLogger(AbstractBuilder.class);

	private ObjectMapper mapper = new ObjectMapper();

	protected Map<String, Object> configs = new HashMap<>();

	//	protected <T> T convert(Class<T> klass) {
//		T result = this.mapper.convertValue(this.configs, klass);
//		return result;
//	}
//
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

	protected <T> T get(String key, Function<Object, T> converter) {
		return this.getOrDefault(key, converter, null);
	}

	protected <T> T getOrDefault(String key, Function<Object, T> converter, T defaultValue) {
		Object value = this.configs.get(key);
		if (value == null) {
			return defaultValue;
		}
		T result = converter.apply(value);
		return result;
	}

	protected void configure(String key, Object value) {
		this.configs.put(key, value);
	}
}
