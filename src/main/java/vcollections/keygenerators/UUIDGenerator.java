package vcollections.keygenerators;

import java.util.UUID;
import java.util.function.Function;

public class UUIDGenerator implements IKeyGenerator<UUID> {

	public UUIDGenerator() {

	}

	@Override
	public UUID get() {
		return UUID.randomUUID();
	}
}
