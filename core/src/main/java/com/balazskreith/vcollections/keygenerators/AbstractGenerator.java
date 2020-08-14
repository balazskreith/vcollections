package com.balazskreith.vcollections.keygenerators;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This class provides a skeletal implementation for {@link IKeyGenerator} classes
 * to minimize the effort required to implement a builder
 *
 * <p>To implement any kind of generator, it is recommended to extend this class
 * and use the constructors in the inherited classes
 *
 * @author Balazs Kreith
 * @since 0.7
 */
public abstract class AbstractGenerator<T> implements IKeyGenerator<T> {

	protected transient Supplier<T> supplier;
	/**
	 * Check if the storgage has this value or not
	 */
	protected transient Predicate<T> tester = storageValue -> false;
	private int maxRetry = 10;
	private final long minSize;
	private final long maxSize;

	protected AbstractGenerator(long minSize, long maxSize) {
		this.minSize = minSize;
		this.maxSize = maxSize;
	}

	protected AbstractGenerator() {
		this(0, 0);
	}

	/**
	 * Gets the geenerated key for type {@link T}.
	 *
	 * @return A generated key
	 */
	@Override
	public T get() {
		T result;
		for (int i = 0; i < maxRetry; ++i) {
			result = this.supplier.get();
			if (this.tester.test(result) == false) {
				return result;
			}
		}
		throw new NoKeyGeneratedException("The maximum retry is exceeded for generating a key");
	}

	/**
	 * Sets up a predicate to test the generated key.
	 * the tester should return true if the generated key cause collusion, and false otherwise
	 *
	 * @param tester
	 */
	@Override
	public void setup(Predicate<T> tester) {
		if (tester == null) {
			throw new NullPointerException("Tester cannot be null");
		}
		this.tester = tester;
	}

	protected long getMinSize() {
		return this.minSize;
	}

	protected long getMaxSize() {
		return this.maxSize;
	}

	/**
	 * Gets the maximum number of retry for the key generation
	 *
	 * @return An integer determines how many times this {@link IKeyGenerator}
	 * tries to generate a key.
	 */
	public int getMaxRetry() {
		return this.maxRetry;
	}

	/**
	 * Sets the maximum number of retry to generate a key.
	 *
	 * @param value the value for the maximum number of retry.
	 */
	public void setMaxRetry(int value) {
		this.maxRetry = value;
	}
}
