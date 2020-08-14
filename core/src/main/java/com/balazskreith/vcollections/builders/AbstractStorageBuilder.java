package com.balazskreith.vcollections.builders;

import com.balazskreith.vcollections.adapters.SerDe;
import com.balazskreith.vcollections.builders.fieldadapters.ObjectMaker;
import com.balazskreith.vcollections.builders.passiveconfigs.AbstractStoragePassiveConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.function.Supplier;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a skeletal implementation for {@link IStorageBuilder}
 * interface to minimize the effort required to implement such builder.
 *
 * <p>To implement any kind of {@link IStorageBuilder}, it is recommended to extend this class
 * and use the {@link AbstractBuilder#convertAndValidate(Class)} method, which validates and
 * converts the provided configuration (which is a {@link Map<String, Object>} type)
 * to the desired class, and throws {@link ConstraintViolationException} if
 * a validation fails.
 *
 * <p>The programmer should generally provide configuration keys in the
 * extended class.
 *
 * @author Balazs Kreith
 * @since 0.7
 */
public abstract class AbstractStorageBuilder extends AbstractBuilder implements StorageBuilder {

	private static Logger logger = LoggerFactory.getLogger(AbstractStorageBuilder.class);

	public AbstractStorageBuilder(ObjectMapper objectMapper) {
		super(objectMapper);
	}

	/**
	 * Puts all configuration to the inside holder in the builder.
	 * These configurations are used when the {@link this#build()} is called.
	 *
	 * @param configs The provided configurations
	 * @return {@link this} builder.
	 */
	@Override
	public StorageBuilder withConfiguration(Map<String, Object> configs) {
		this.configs.putAll(configs);
		return this;
	}


	@Override
	public Map<String, Object> getConfigurations() {
		return this.getConfigs();
	}

	@Override
	public <T> StorageBuilder withValueSerDe(SerDe<T> valueSerde) {
		ObjectMaker<SerDe<T>> serDeMaker = new ObjectMaker<>();
		String serDeValue = serDeMaker.deconvert(valueSerde);
		return this.withValueSerDe(serDeValue);
	}

	@Override
	public StorageBuilder withValueSerDe(String klassName) {
		this.getConfigs().set(AbstractStoragePassiveConfig.VALUE_SERDE_FIELD_NAME, klassName);
		return this;
	}

	@Override
	public <T> StorageBuilder withKeySupplier(Supplier<T> keySupplier) {
		ObjectMaker<Supplier<T>> keySupplierMaker = new ObjectMaker<>();
		String keySupplierValue = keySupplierMaker.deconvert(keySupplier);
		return this.withKeySupplier(keySupplierValue);
	}

	@Override
	public StorageBuilder withKeySupplier(String klassName) {
		this.getConfigs().set(AbstractStoragePassiveConfig.KEY_SUPPLIER_FIELD_NAME, klassName);
		return this;
	}

	@Override
	public StorageBuilder withCapacity(long capacity) {
		this.getConfigs().set(AbstractStoragePassiveConfig.CAPACITY_FIELD_NAME, capacity);
		return this;
	}

	@Override
	public <T> T getConfiguration(String key) {
		return this.getConfigs().get(key);
	}

	@Override
	public <T> SerDe<T> getValueSerDe() {
		ObjectMaker<SerDe<T>> valueSerDeMaker = new ObjectMaker<>();
		String valueSerDeKlassName = this.getConfigs().get(AbstractStoragePassiveConfig.VALUE_SERDE_FIELD_NAME);
		SerDe<T> result = valueSerDeMaker.convert(valueSerDeKlassName);
		return result;
	}
}
