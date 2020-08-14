package com.balazskreith.vcollections.storages;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import java.net.InetSocketAddress;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.codecs.pojo.annotations.BsonId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

class MongoStorageTest implements StorageTest<String, MongoStorageTest.MyObject, MongoStorage<String, MongoStorageTest.MyObject>> {

	private static final String MONGO_DATABASE_NAME = "test-db";
	private static final String MONGO_COLLECTION_NAME = "test-collection";

	public static class MyObject {
		@BsonId
		public String key;
		public String value;

		@Override
		public boolean equals(Object peer) {
			if (peer instanceof MyObject == false) {
				return false;
			}
			MyObject myPeer = (MyObject) peer;
			if (myPeer == null) {
				return false;
			}
//			return myPeer.key.equals(this.key) && myPeer.value.equals(this.value);
			return myPeer.value.equals(this.value);
		}

		@Override
		public String toString() {
			return String.format("%s: %s", this.key, this.value);
		}
	}

	private MongoCollection<MyObject> collection;
	private MongoServer server;
	private MongoClient client;

	@BeforeEach
	public void setUp() {
		server = new MongoServer(new MemoryBackend());

		// bind on a random local port
		InetSocketAddress serverAddress = server.bind();

		this.client = new MongoClient(new ServerAddress(serverAddress));

	}

	@AfterEach
	public void tearDown() {
		client.close();
		server.shutdown();

	}


	@BeforeEach
	public void setup() {

	}


	@Override
	public IStorage<String, MyObject> makeStorage(long maxSize, Map.Entry<String, MyObject>... entries) {

		Supplier<MongoClient> clientProvider = () -> this.client;
		ClassModel<MyObject> valueModel = ClassModel.builder(MyObject.class).enableDiscriminator(true).build();
		PojoCodecProvider pojoCodecProvider = PojoCodecProvider.builder().register(valueModel).build();
		CodecRegistry codecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

		MongoDatabase db = clientProvider.get().getDatabase(MONGO_DATABASE_NAME);
		MongoCollection<MyObject> collection =
				db.getCollection(MONGO_COLLECTION_NAME).withCodecRegistry(codecRegistry)
						.withDocumentClass(MyObject.class);


		List<MyObject> documents = Arrays.asList(entries).stream().map(entry -> entry.getValue()).collect(Collectors.toList());
		if (0 < documents.size()) {
			collection.insertMany(documents);
			System.out.println("The size of the collection is " + collection.countDocuments());
			MongoCursor<MyObject> it = collection.find().cursor();
			for (; it.hasNext(); ) {
				MyObject myObject = it.next();
				System.out.println(String.format("%s:%s", myObject.key, myObject.value));
			}
		}

		MongoConnection<MyObject> connection = new MongoConnection<>(
				clientProvider,
				MONGO_DATABASE_NAME,
				MONGO_COLLECTION_NAME,
				MyObject.class,
				codecRegistry,
				3
		);

		BiConsumer<MyObject, MyObject> swapper = new BiConsumer<MyObject, MyObject>() {
			@Override
			public void accept(MyObject value1, MyObject value2) {
				String tempValue = value2.value;
				value2.value = value1.value;
				value1.value = tempValue;
			}
		};
		Function<MyObject, String> keyExtractor = myObject -> myObject.key;
		MongoStorage<String, MyObject> result = new MongoStorage<>(
				connection,
				keyExtractor,
				"_id",
				maxSize,
				MyObject.class,
				swapper);
		return result;
	}

	@Override
	public String toKey(String key) {
		if (key == null) {
			return "null";
		}
		return key;
	}

	@Override
	public MyObject toValue(String value) {
		MyObject result = new MyObject();
		if (value == null) {
			result.value = "null";
			result.key = "null";
			return result;
		}
		result.value = value;
		result.key = "default";
		return result;
	}

	@Override
	public Map.Entry<String, MyObject> toEntry(String keyString, String valueString) {
		MyObject myObject = new MyObject();
		myObject.value = valueString == null ? "null" : valueString;
		myObject.key = keyString == null ? "null" : keyString;

		return new AbstractMap.SimpleEntry(myObject.key, myObject);
	}


}