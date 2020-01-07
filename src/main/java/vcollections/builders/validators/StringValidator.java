package vcollections.builders.validators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringValidator extends Validator {

	public StringValidator() {
		this.add(obj -> obj instanceof String);

	}

	public StringValidator withRegex() {
		return this;
	}

	private List<String> possibleItems = new ArrayList<>();

	public StringValidator withPossibleValues(String... items) {
		this.possibleItems.addAll(Arrays.asList(items));
		this.add(this.possibleItems::contains);
		return this;
	}

}
