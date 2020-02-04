package vcollections.keygenerators;

import java.util.UUID;

public class UUIDGenerator extends AbstractGenerator<UUID> {

	public UUIDGenerator() {
		super(0, 0);
		this.supplier = UUID::randomUUID;
	}
}
