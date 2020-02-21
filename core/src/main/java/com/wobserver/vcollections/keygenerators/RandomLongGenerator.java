package com.wobserver.vcollections.keygenerators;

import java.util.Random;

public class RandomLongGenerator extends AbstractGenerator<Long> {

	private Random random;

	/**
	 * Generate random number by using the provided Random number generator
	 * @param random
	 */
	public RandomLongGenerator(Random random) {
		super(0, 0);
		this.random = random;
		this.supplier = this.random::nextLong;
	}

	/**
	 * Constructs random 64 bits signed integers
	 */
	public RandomLongGenerator() {
		super(0, 0);
		this.random = new Random();
		this.supplier = this.random::nextLong;
	}
	/**
	 * Constructs a keygenerator between min and max
	 * @param minSize The minimum for the the random numbers
	 * @param maxSize the maximum value of the random numbers
	 *
	 * <p>Note</p> If minSize and maxSize are 0, then the generated numbers are 64 bit signed integers
	 */
	public RandomLongGenerator(long minSize, long maxSize) {
		super(minSize, maxSize);
		this.random = new Random();
		this.supplier = () -> {
			long offset = this.random.nextLong() % (maxSize - minSize);
			return minSize + offset;
		};
	}

}
