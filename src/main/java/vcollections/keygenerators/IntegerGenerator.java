package vcollections.keygenerators;

import java.util.Random;

public class IntegerGenerator extends AbstractGenerator<Integer> {

	private Random random;

	public IntegerGenerator() {
		super(0, 0);
		this.random = new Random();
		this.supplier = this.random::nextInt;
	}

	public IntegerGenerator(int minSize, int maxSize) {
		super(minSize, maxSize);
		this.random = new Random();
		this.supplier = () -> this.random.nextInt(maxSize - minSize) + minSize;
	}

}
