package com.balazskreith.vcollections.storages;

import java.util.function.Consumer;

class CapacityChecker<T> {

	private final Runnable checkForCreate;
	private final Consumer<T> checkForUpdate;

	public CapacityChecker(IStorage<T, ?> storage, long capacity) {

		if (capacity == IStorage.NO_MAX_SIZE) { // In this case there is no point to check
			this.checkForCreate = () -> {

			};
			this.checkForUpdate = obj -> {

			};
		} else {
			this.checkForCreate = () -> {
				if (storage.isFull()) {
					throw new OutOfSpaceException();
				}
			};
			this.checkForUpdate = key -> {
				if (!storage.has(key)) {
					if (storage.isFull()) {
						throw new OutOfSpaceException();
					}
				}
			};
		}
	}

	public void checkForUpdate(T key) {
		this.checkForUpdate.accept(key);
	}

	public void checkForCreate() {
		this.checkForCreate.run();
	}
}
