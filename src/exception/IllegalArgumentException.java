package exception;

public class IllegalArgumentException extends UncheckedSolverException {
    private static final long serialVersionUID = 1L;

    public IllegalArgumentException(String methodName, Object illegalArg) {
        this("Illegal argument (" + illegalArg.toString() + ") passed to method " + methodName + ".");
    }
    
    public IllegalArgumentException(String message) {
        super(message);
    }
}
