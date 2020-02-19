package com.wobserver.vcollections.builders;

import com.wobserver.vcollections.storages.IStorage;
import com.wobserver.vcollections.storages.MongoConnection;
import com.wobserver.vcollections.storages.MongoStorage;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.validation.constraints.NotNull;
import org.bson.codecs.pojo.annotations.BsonId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoStorageBuilder extends AbstractStorageBuilder {

	private static final Logger logger = LoggerFactory.getLogger(MongoStorageBuilder.class);

	public static final String KEY_FIELD_IN_VALUE_CONFIG_KEY = "keyFieldInValue";
	public static final String KEY_FIELD_IN_DOCUMENT_CONFIG_KEY = "keyFieldInDocument";
	public static final String VALUE_TYPE_CONFIG_KEY = "valueType";
	public static final String CONNECTION_CONFIG_KEY = "connection";
	public static final String SWAPPER_TYPE_CONFIG_KEY = "swapperType";


	/**
	 * Build a {@link com.wobserver.vcollections.storages.MongoStorage}
	 *
	 * @param <K> the type of the key
	 * @param <V> the type of the value
	 * @return A {@link com.wobserver.vcollections.storages.MongoStorage} that built.
	 */
	public <K, V> IStorage<K, V> build() {
		Config config = this.convertAndValidate(Config.class);
		Class<V> valueType = this.getClassFor(config.valueType);
		FieldAccessBuilder keyFieldInValueAccessor = new FieldAccessBuilder()
				.withClass(config.valueType)
				.withField(config.keyFieldInValue);
		BiConsumer<V, V> swapper = null;

		String keyFieldInDocument = config.keyFieldInDocument;
		if (keyFieldInDocument == null) {
			keyFieldInDocument = this.tryDetermineKeyFieldInDocument(config.keyFieldInValue, keyFieldInValueAccessor);
		}

		MongoConnection<V> connection = new MongoConnectionBuilder()
				.withConfiguration(config.connection)
				.withValueType(config.valueType)
				.build();

		if (config.swapperType != null) {
			swapper = (BiConsumer<V, V>) this.invoke(config.swapperType);
		}

		Function<V, K> keyExtractor = keyFieldInValueAccessor.getKeyExtractor();
		MongoStorage<K, V> result = new MongoStorage<>(
				connection,
				keyExtractor,
				keyFieldInDocument,
				config.capacity,
				valueType,
				swapper
		);


		return result;
	}

	/**
	 * This method is called if the keyfieldInDocument is null, in which case
	 * the builder try to infer the name of the field for key property in the document.
	 * Note: if the keyfield uses in document is not indexed in mongo,
	 * update and delete oeration will not work.
	 *
	 * @param keyFieldInValue
	 * @param keyFieldInValueAccessor
	 * @return
	 */
	private String tryDetermineKeyFieldInDocument(String keyFieldInValue, FieldAccessBuilder keyFieldInValueAccessor) {
		String result;
		// the key field is annotated as an id, which will be interpreted in the codec as the id
		if (keyFieldInValueAccessor.getAnnotatedElement().isAnnotationPresent(BsonId.class)) {
			result = "id";
			return result;
		}


		if (keyFieldInValue != "id") {
			logger.info("The keyfield for the document is inferred as {}, " +
					"which if it is not indexed will cause a problem. " +
					"To avoid this message, define it explicitly " +
					"in your configuration", keyFieldInValue);
		}
		result = keyFieldInValue;
		return result;
	}

	/**
	 * Sets the configuration for the connection used to create a client for Redis
	 *
	 * @param value the configuration URI for the "{@link MongoStorageBuilder}
	 * @return A {@link MongoStorageBuilder} to set options further
	 */
	public MongoStorageBuilder withConnection(Map<String, Object> value) {
		this.configs.put(CONNECTION_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets the the name of the key field for the document retrieved from mongodb
	 *
	 * @param value the configuration URI for the "{@link MongoStorageBuilder}
	 * @return A {@link MongoStorageBuilder} to set options further
	 */
	public MongoStorageBuilder withKeyFieldInDocument(String value) {
		this.configs.put(KEY_FIELD_IN_DOCUMENT_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets the the type of the value for the mongodb
	 *
	 * @param value the configuration URI for the "{@link MongoStorageBuilder}
	 * @return A {@link MongoStorageBuilder} to set options further
	 */
	public MongoStorageBuilder withValueType(String value) {
		this.configs.put(VALUE_TYPE_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets the the name of the key field for the object used in the program
	 *
	 * @param value the configuration URI for the "{@link MongoStorageBuilder}
	 * @return A {@link MongoStorageBuilder} to set options further
	 */
	public MongoStorageBuilder withKeyFieldInValue(String value) {
		this.configs.put(KEY_FIELD_IN_VALUE_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets the the name of the key field for the object used in the program
	 *
	 * @param value the configuration URI for the "{@link MongoStorageBuilder}
	 * @return A {@link MongoStorageBuilder} to set options further
	 */
	public MongoStorageBuilder withSwapperType(String value) {
		this.configs.put(SWAPPER_TYPE_CONFIG_KEY, value);
		return this;
	}


	public static class Config extends AbstractStorageBuilder.Config {

		@NotNull
		public String valueType;

		@NotNull
		public String swapperType;

		public String keyFieldInDocument;

		@NotNull
		public String keyFieldInValue;

		@NotNull
		Map<String, Object> connection;
	}
}
