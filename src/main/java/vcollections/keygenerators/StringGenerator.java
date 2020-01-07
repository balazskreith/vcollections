package vcollections.keygenerators;

import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class StringGenerator extends AbstractGenerator<String> {

	private Predicate<String> tester;
	private Supplier<String> supplier;

	public StringGenerator() {
		this.supplier = UUID.randomUUID()::toString;
		this.tester = key -> true;
	}

	public StringGenerator(int minSize, int maxSize) {
		this.tester = key -> true;
		if (minSize == maxSize && minSize == 0) {
			this.supplier = UUID.randomUUID()::toString;
		} else {
			this.supplier = () -> {
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < minSize; i+=36) {
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

	@Override
	public String get() {
		String result;
		while(true) {
			result = this.supplier.get();
			if (this.tester.test(result)) {
				return result;
			}
		}
	}

}
