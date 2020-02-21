package com.wobserver.vcollections.keygenerators;

import java.util.Random;
import java.util.UUID;

/**
 * Represents a Generator for Strings
 */
public class RandomStringGenerator extends AbstractGenerator<String> {

	/**
	 * Generates a random 46 character long string by using an UUID generator
	 */
	public RandomStringGenerator() {
		this.supplier = UUID.randomUUID()::toString;
	}

	/**
	 * Constructs a Generator generates {@link String} between the size of min- and maxSize
	 * @param minSize THe minimal size of the generated stings
	 * @param maxSize The maximal size of the generated strings
	 */
	public RandomStringGenerator(int minSize, int maxSize) {
		if (minSize == maxSize && minSize == 0) {
			this.supplier = UUID.randomUUID()::toString;
		} else {
			this.supplier = () -> {
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < minSize; i += 36) {
					builder.append(UUID.randomUUID().toString());
				}
				if (minSize == maxSize) {
					return builder.toString().substring(0, minSize);
				}
				int offset = new Random().nextInt(maxSize - minSize);
				return builder.toString().substring(0, minSize + offset);
			};
		}

	}
}
