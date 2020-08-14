package com.balazskreith.vcollections.builders;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.balazskreith.vcollections.storages.MongoConnection;
import java.util.Map;
import java.util.function.Supplier;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;

class MongoConnectionBuilder extends AbstractBuilder {

	public static final String DATABASE_CONFIG_KEY = "database";
	public static final String COLLECTION_CONFIG_KEY = "collection";
	public static final String MAX_RETRIES_CONFIG_KEY = "maxRetries";
	public static final String URI_CONFIG_KEY = "URI";
	public static final String VALUE_TYPE_CONFIG_KEY = "valueType";


	/**
	 * Build the mongoURI
	 *
	 * @return
	 */
	public <T> MongoConnection<T> build() {
		Config config = this.convertAndValidate(Config.class);
		Class<T> valueType = this.getClassFor(config.valueType);
		MongoClientURI clientURI = new MongoURIBuilder()
				.withConfiguration(config.URI)
				.build();
		Supplier<MongoClient> clientProvider = () -> {
			return new MongoClient(clientURI);
		};

		ClassModel<T> valueModel = ClassModel.builder(valueType).enableDiscriminator(true).build();

		PojoCodecProvider pojoCodecProvider = PojoCodecProvider.builder().register(
				valueModel
		).build();

		CodecRegistry codecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

		MongoConnection<T> result = new MongoConnection<>(clientProvider,
				config.database,
				config.collection,
				valueType,
				codecRegistry,
				config.maxRetries);
		return result;
	}

	/**
	 * Puts all configuration to the inside holder in the builder.
	 * These configurations are used when the {@link this#build()} is called.
	 *
	 * @param configs The provided configurations
	 * @return {@link this} builder.
	 */
	public MongoConnectionBuilder withConfiguration(Map<String, Object> configs) {
		this.configs.putAll(configs);
		return this;
	}

	/**
	 * Sets the the type of the value for the connection
	 *
	 * @param value the configuration URI for the "{@link MongoStorageBuilder}
	 * @return A {@link MongoStorageBuilder} to set options further
	 */
	public MongoConnectionBuilder withValueType(String value) {
		this.configs.put(VALUE_TYPE_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets the collection for mongodb
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link MongoConnectionBuilder}.
	 */
	public MongoConnectionBuilder withCollection(String value) {
		this.configs.put(COLLECTION_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets the database the storage will connect to
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link MongoConnectionBuilder}.
	 */
	public MongoConnectionBuilder withDatabase(String value) {
		this.configs.put(DATABASE_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets the maximal retry for reconnecting
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link MongoConnectionBuilder}.
	 */
	public MongoConnectionBuilder withMaxRetries(int value) {
		this.configs.put(MAX_RETRIES_CONFIG_KEY, value);
		return this;
	}

	/**
	 * Sets the server for mongodb
	 * Default: null
	 *
	 * @param value the value assigned as a parameter of the given setting
	 * @return This {@link MongoConnectionBuilder}.
	 */
	public MongoConnectionBuilder withURI(Map<String, Object> value) {
		this.configs.put(URI_CONFIG_KEY, value);
		return this;
	}

	public static class Config {

		@NotNull
		public String valueType;

		@NotNull
		public String database;

		@NotNull
		public String collection;

		@Min(1)
		public int maxRetries = 3;

		@NotNull
		public Map<String, Object> URI;
	}
}
