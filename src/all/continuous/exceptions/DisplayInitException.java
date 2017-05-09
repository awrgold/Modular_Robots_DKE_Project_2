package all.continuous.exceptions;

public class DisplayInitException extends Exception {
	public DisplayInitException() {
		super("Display could not be initialized");
	}

	private static final long serialVersionUID = 970317489204903391L;
}
