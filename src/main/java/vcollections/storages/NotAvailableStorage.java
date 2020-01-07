package vcollections.storages;

public class NotAvailableStorage extends RuntimeException {
	public NotAvailableStorage() {
		super();
	}

	public NotAvailableStorage(String message) {
		super(message);
	}
}
