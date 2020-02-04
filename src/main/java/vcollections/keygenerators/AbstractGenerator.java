package vcollections.keygenerators;

import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class AbstractGenerator<T> implements IKeyGenerator<T> {

	protected Supplier<T> supplier;
	/**
	 * Check if the storgage has this value or not
	 */
	protected Predicate<T> tester = storageValue -> false;
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

	public int getMaxRetry() {
		return this.maxRetry;
	}

	public void setMaxRetry(int value) {
		this.maxRetry = value;
	}
}
