package builders;

import java.util.ArrayList;

public class Builder {
	public Builder useProfile() {
		return this;
	}

	public <T> ArrayList<T> buildArrayList() {
		return new ArrayList<>();
	}
}
