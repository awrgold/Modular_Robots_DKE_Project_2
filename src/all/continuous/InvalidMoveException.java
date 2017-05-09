package all.continuous;

public class InvalidMoveException extends RuntimeException {
	private static final long serialVersionUID = -2821821836183911255L;

	public InvalidMoveException(String message) {
        super(message);
    }
}
