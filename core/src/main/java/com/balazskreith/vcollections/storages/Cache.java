package com.balazskreith.vcollections.storages;

public interface Cache {
	void flush();

	long hits();

	long misses();
}
