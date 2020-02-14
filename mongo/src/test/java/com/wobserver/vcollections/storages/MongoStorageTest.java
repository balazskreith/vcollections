package com.wobserver.vcollections.storages;

import com.github.fakemongo.Fongo;

class MongoStorageTest implements StorageTest<MongoStorage<String, String>> {

	class MyObject {

	}

	private Fongo fongo = new Fongo("mongo-test");

	@Override
	public IStorage<String, String> makeStorage(long maxSize, String... items) {

		return null;
	}
}