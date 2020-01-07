package vcollections.builders.validators;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Validator implements Predicate<Object> {
	private Consumer<Map.Entry<String, Object>> errorConsumer = null;
	private List<Predicate<Object>> testers = new LinkedList<>();

	public Validator() {

	}

	protected void add(Predicate<Object> tester) {
		this.testers.add(tester);
	}

	public void reportErrorsTo(Consumer<Map.Entry<String, Object>> errorConsumer) {
		this.errorConsumer = errorConsumer;
	}

	protected void reportError(String key, Object obj) {
		this.errorConsumer.accept(new AbstractMap.SimpleEntry<>(key, obj));
	}

	@Override
	public boolean test(Object o) {
		return this.testers.stream().allMatch(tester -> tester.test(o));
	}
}
