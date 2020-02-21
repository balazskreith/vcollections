package com.wobserver.vcollections.keygenerators;

import java.util.Random;

/**
 * Represent a {@link IKeyGenerator} for integers
 */
public class RandomIntegerGenerator extends AbstractGenerator<Integer> {

	private Random random;

	/**
	 * Constructs a keygenerator for signed integers
	 */
	public RandomIntegerGenerator() {
		super(0, 0);
		this.random = new Random();
		this.supplier = this.random::nextInt;
	}

	/**
	 * Constructs a keygenerator between min and max
	 * @param minSize The minimum for the the random numbers
	 * @param maxSize the maximum value of the random numbers
	 *
	 * <p>Note</p> If minSize and maxSize are 0, then the generated numbers are 32 bit signed integers
	 */
	public RandomIntegerGenerator(int minSize, int maxSize) {
		super(minSize, maxSize);
		this.random = new Random();
		this.supplier = () -> this.random.nextInt(maxSize - minSize) + minSize;
	}

}
