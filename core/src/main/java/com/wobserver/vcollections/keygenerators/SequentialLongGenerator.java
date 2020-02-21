package com.wobserver.vcollections.keygenerators;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SequentialLongGenerator extends AbstractGenerator<Long> {

	private AtomicLong idHolder;

	/**
	 * Generate random number by using the provided Random number generator
	 * @param 
	 */
	public SequentialLongGenerator(int offset) {
		super(0, 0);
		this.idHolder = new AtomicLong(offset);
		this.supplier = this.idHolder::getAndIncrement;
	}

	/**
	 * Constructs random 64 bits signed integers
	 */
	public SequentialLongGenerator() {
		super(0, 0);
		this.idHolder = new AtomicLong(0);
		this.supplier = this.idHolder::getAndIncrement;
	}
	/**
	 * Constructs a keygenerator between min and max
	 * @param minSize The minimum for the the random numbers
	 * @param maxSize the maximum value of the random numbers
	 *
	 * <p>Note</p> If minSize and maxSize are 0, then the generated numbers are 64 bit signed integers
	 */
	public SequentialLongGenerator(long minSize, long maxSize) {
		super(minSize, maxSize);
		this.idHolder = new AtomicLong(minSize);
		this.supplier = () -> {
			long result = this.idHolder.getAndIncrement();
			if (maxSize < result) {
				throw new RuntimeException("Out of range");
			}
			return result;
		};
	}

}
