package builders;

import java.util.HashMap;
import java.util.Map;

public class MemoryStorageBuilder {

	private Map<String, String> configs = new HashMap<>();

	public MemoryStorageBuilder() {

	}

	public MemoryStorageBuilder withCapacity() {
		return this;
	}

	public MemoryStorageBuilder withEntries() {
		return this;
	}

	public MemoryStorageBuilder withKeyGenerator() {
		return this;
	}
}
