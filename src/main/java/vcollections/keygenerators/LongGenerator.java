package vcollections.keygenerators;

import java.util.Random;

public class LongGenerator extends AbstractGenerator<Long> {

	private Random random;

	public LongGenerator(Random random) {
		super(0, 0);
		this.random = random;
		this.supplier = this.random::nextLong;
	}

	public LongGenerator() {
		super(0, 0);
		this.random = new Random();
		this.supplier = this.random::nextLong;
	}

	public LongGenerator(long minSize, long maxSize) {
		super(minSize, maxSize);
		this.random = new Random();
		this.supplier = () -> {
			long offset = this.random.nextLong() % (maxSize - minSize);
			return minSize + offset;
		};
	}

}
