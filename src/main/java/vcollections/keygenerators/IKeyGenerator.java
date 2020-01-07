package vcollections.keygenerators;

import java.util.function.Predicate;
import java.util.function.Supplier;

@FunctionalInterface
public interface IKeyGenerator<T>  extends Supplier<T> {

	default void setup(Predicate<T> tester) {

	}
}
