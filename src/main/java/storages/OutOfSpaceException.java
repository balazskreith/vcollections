package storages;

public class OutOfSpaceException extends RuntimeException {
	public OutOfSpaceException() {
		super();
	}

	public OutOfSpaceException(String message) {
		super(message);
	}
}
