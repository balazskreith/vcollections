package storages;

public class KeyUniquenessViolationException extends RuntimeException {
	public KeyUniquenessViolationException() {
		super();
	}

	public KeyUniquenessViolationException(String message) {
		super(message);
	}
}
