package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.adapters.Adapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This responsible to give a fully fledged map, no missing environment variable,
 * no using keyword anywhere in the resulted map
 */
public abstract class AbstractConfigAdapter<TPassive, TActive> implements Adapter<TPassive, TActive> {
	private static Logger logger = LoggerFactory.getLogger(AbstractConfigAdapter.class);



	public AbstractConfigAdapter() {
	}

	/**
	 * Converts the provided configuration to the type of object provided as a parameter, and
	 * validates the conversion.
	 *
	 * @return An object of the desired type setup with values from the configuration.
	 * @throws ConstraintViolationException if the validation fails during the conversion.
	 */
	protected void validate(TPassive data) {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<TPassive>> violations = validator.validate(data);

		if (violations != null && !violations.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (ConstraintViolation<TPassive> constraintViolation : violations) {
				sb.append(constraintViolation.getMessage())
						.append(" ");
			}

			String errorMessage = sb.toString();
			logger.error(errorMessage);

			throw new ConstraintViolationException(violations);
		}
	}

	@Override
	public TActive convert(TPassive source) {
		Objects.requireNonNull(source);
		this.validate(source);
		TActive result = this.doConvert(source);
		Objects.requireNonNull(result);
		return result;
	}

	protected abstract TActive doConvert(TPassive source);

	protected abstract TPassive doDeConvert(TActive source);

	@Override
	public TPassive deconvert(TActive source) {
		Objects.requireNonNull(source);
		TPassive result = this.doDeConvert(source);
		Objects.requireNonNull(result);

		this.validate(result);
		return result;
	}
}
