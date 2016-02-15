package exception;

public abstract class CheckedSolverException extends Exception {
    private static final long serialVersionUID = 1L;

    public CheckedSolverException(String message) {
        super(message);
    }
}
