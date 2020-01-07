package vcollections.keygenerators;

import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class IntegerGenerator extends AbstractGenerator<Integer> {

	private Random random;

	public IntegerGenerator() {
		super(0, 0);
		this.tester = key -> true;
		this.random = new Random();
		this.supplier = this.random::nextInt;
	}

	public IntegerGenerator(int minSize, int maxSize) {
		super(minSize, maxSize);
		this.tester = key -> true;
		this.random = new Random();
		this.supplier = () -> this.random.nextInt(maxSize - minSize) + minSize;
	}

}
