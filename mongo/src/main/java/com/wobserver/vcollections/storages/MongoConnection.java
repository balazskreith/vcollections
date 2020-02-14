package com.wobserver.vcollections.storages;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

class MongoConnection<T> {


	private final MongoClientURI uri;
	private final String databaseName;
	private final String collectionName;
	private final Class<T> valueType;
	private MongoClient client;
	private MongoDatabase database;
	private MongoCollection<T> collection;

	public MongoConnection(MongoClientURI uri, String databaseName, String collectionName, Class<T> valueType) {
		this.uri = uri;
		this.databaseName = databaseName;
		this.collectionName = collectionName;
		this.valueType = valueType;

		CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
				fromProviders(PojoCodecProvider.builder().automatic(true).build()));

		this.client = new MongoClient(this.uri);
		this.database = this.client.getDatabase(this.databaseName)
				.withCodecRegistry(pojoCodecRegistry);
		this.collection = this.database.getCollection(this.collectionName, this.valueType);


	}

	public MongoCollection<T> getCollection() {
		return null;
	}

	public long getCollectionSize() {
		return 0;
	}
}
