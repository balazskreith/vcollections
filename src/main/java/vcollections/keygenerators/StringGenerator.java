package vcollections.keygenerators;

import java.util.Random;
import java.util.UUID;

public class StringGenerator extends AbstractGenerator<String> {

	public StringGenerator() {
		this.supplier = UUID.randomUUID()::toString;
	}

	public StringGenerator(int minSize, int maxSize) {
		if (minSize == maxSize && minSize == 0) {
			this.supplier = UUID.randomUUID()::toString;
		} else {
			this.supplier = () -> {
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < minSize; i += 36) {
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
}
