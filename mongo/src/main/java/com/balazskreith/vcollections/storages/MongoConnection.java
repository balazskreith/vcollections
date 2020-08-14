package com.balazskreith.vcollections.storages;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.function.Supplier;
import org.bson.codecs.configuration.CodecRegistry;

public class MongoConnection<T> {


	private final String databaseName;
	private final String collectionName;
	private final Class<T> valueType;
	private final CodecRegistry codecRegistry;
	private final int maxRetries;
	private final Supplier<MongoClient> clientProvider;
	private MongoClient client;
	private MongoDatabase database;
	private MongoCollection<T> collection;
	private Long connected;
	private long checkedInMs;
	private int timeoutInMs;

	public MongoConnection(Supplier<MongoClient> clientProvider, String databaseName, String collectionName, Class<T> valueType, CodecRegistry codecRegistry, int maxRetries) {
		this.clientProvider = clientProvider;
		this.databaseName = databaseName;
		this.collectionName = collectionName;
		this.valueType = valueType;
		this.codecRegistry = codecRegistry;
		this.maxRetries = maxRetries;

		this.connect();
	}

	private void connect() {
		this.client = this.clientProvider.get();
		this.database = this.client.getDatabase(this.databaseName);
		this.collection = this.database.getCollection(this.collectionName)
				.withCodecRegistry(codecRegistry)
				.withDocumentClass(valueType);
		this.connected = System.currentTimeMillis();
		this.timeoutInMs = this.client.getMongoClientOptions().getConnectTimeout();
	}

	private void checkConnectivity() {
		long now = System.currentTimeMillis();
		if (now < this.checkedInMs + this.timeoutInMs) {
			this.checkedInMs = now;
			return;
		}
		try {
			this.database = this.client.getDatabase(this.databaseName);
			this.collection = this.database.getCollection(this.collectionName)
					.withCodecRegistry(codecRegistry)
					.withDocumentClass(valueType);
			this.checkedInMs = now;
			return;
		} catch (MongoException ex) {
			this.client.close();
		}
		int retried = 0;
		while (true) {
			try {
				this.connect();
				break;
			} catch (MongoException ex) {
				this.client.close();
				if (++retried < this.maxRetries) {
					continue;
				}
				throw ex;
			}
		}
		this.checkedInMs = now;
	}

	public MongoDatabase getDatabase() {
		this.checkConnectivity();
		return this.database;
	}

	public MongoCollection<T> getCollection() {
		this.checkConnectivity();

		return this.collection;
	}

	public long getCollectionSize() {
		return this.collection.countDocuments();
	}
}
