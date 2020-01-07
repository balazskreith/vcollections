package vcollections.keygenerators;

import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class AbstractGenerator<T> implements IKeyGenerator<T> {

	protected Predicate<T> tester;
	protected Supplier<T> supplier;
	protected int maxRetry = 10;
	private final int minSize;
	private final int maxSize;

	protected AbstractGenerator(int minSize, int maxSize) {
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

	protected int getMinSize() {
		return this.minSize;
	}

	protected int getMaxSize() {
		return this.maxSize;
	}

	public int getMaxRetry() {
		return this.maxRetry;
	}

	public void setMaxRetry(int value) {
		this.maxRetry = value;
	}
}
