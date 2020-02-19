package com.wobserver.vcollections.storages;

import org.bson.codecs.pojo.annotations.BsonId;
import org.junit.jupiter.api.Test;

public class Sandbox {


	public static class MyObject {

		public MyObject() {

		}

		@BsonId
		public String key;
		public String value;
		public Integer v2 = 2;
	}

	@Test
	public void test() {
//		MongoClientURI clientURI = new MongoClientURI("mongodb://localhost:27017");
//		MongoStorage<String, MyObject> storage = new MongoStorage(clientURI, "testdb", "test", "key", IStorage.NO_MAX_SIZE, MyObject.class);
//
//		MyObject myObject = new MyObject();
//		myObject.key = "key";
//		myObject.value = "value6";
////		System.out.println(storage.create(myObject));
//		storage.update("key", myObject);
//		storage.delete("key");
////		System.out.println(storage.read("key").key);
////		myObject = storage.read("key2");
////		System.out.println(String.join(":", myObject.key, myObject.value));
	}

}