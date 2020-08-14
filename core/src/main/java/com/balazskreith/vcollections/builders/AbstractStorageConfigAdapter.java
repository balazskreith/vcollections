package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.activeconfigs.AbstractStorageActiveConfig;
import com.balazskreith.vcollections.adapters.SerDe;
import com.balazskreith.vcollections.builders.fieldadapters.ObjectMaker;
import com.balazskreith.vcollections.builders.passiveconfigs.AbstractStoragePassiveConfig;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
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
public abstract class AbstractStorageConfigAdapter<K, V, TPassive extends AbstractStoragePassiveConfig,
		TActive extends AbstractStorageActiveConfig<K, V>> extends AbstractConfigAdapter<TPassive,
		TActive> {

	private static Logger logger = LoggerFactory.getLogger(AbstractStorageConfigAdapter.class);

	private final ObjectMaker<Supplier<K>> keySupplierAdapter;
	private final ObjectMaker<SerDe<V>> valueSerDeMaker;

	public AbstractStorageConfigAdapter() {
		this.keySupplierAdapter = new ObjectMaker<>();
		this.valueSerDeMaker = new ObjectMaker<>();
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

	protected AbstractStorageActiveConfig getAbstractStorageActiveConfig(TPassive passiveConfig) {
		long capacity = passiveConfig.capacity;
		Supplier<K> keySupplier = this.keySupplierAdapter.convert(passiveConfig.keySupplier);
		SerDe<V> valueSerDe = this.valueSerDeMaker.convert(passiveConfig.valueSerDe);
		return new AbstractStorageActiveConfig<K, V>(capacity, keySupplier, valueSerDe);
	}

	protected void setupAbstractStoragePassiveConfig(AbstractStoragePassiveConfig result, TActive activeConfig) {
		result.capacity = activeConfig.capacity;
		result.keySupplier = this.keySupplierAdapter.deconvert(activeConfig.keySupplier);
		result.valueSerDe = this.valueSerDeMaker.deconvert(activeConfig.valueSerDe);
	}

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
