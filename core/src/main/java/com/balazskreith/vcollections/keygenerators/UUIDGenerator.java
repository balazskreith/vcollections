package com.balazskreith.vcollections.keygenerators;

import java.util.UUID;

public class UUIDGenerator extends AbstractGenerator<UUID> {
	/**
	 * Constructs a {@link IKeyGenerator} generate UUID-s
	 */
	public UUIDGenerator() {
		super(0, 0);
		this.supplier = UUID::randomUUID;
	}

	/**
	 * Generates a UUD generator. This constructor is here to be compatible with the standard of having a constructor
	 * accepts two parameters (int, int)
	 * @param minSize
	 * @param maxSize
	 */
	public UUIDGenerator(int minSize, int maxSize) {
		this();
	}
}
