package vcollections.keygenerators;

public class NoKeyGeneratedException extends RuntimeException {
	public NoKeyGeneratedException() {
		super();
	}

	public NoKeyGeneratedException(String message) {
		super(message);
	}
}
