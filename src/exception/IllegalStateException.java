package exception;

public class IllegalStateException extends UncheckedSolverException {
    private static final long serialVersionUID = 1L;

    public IllegalStateException(String message) {
        super(message);
    }
}
