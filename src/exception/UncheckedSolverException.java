package exception;

public abstract class UncheckedSolverException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UncheckedSolverException(String message) {
        super(message);
    }
}
