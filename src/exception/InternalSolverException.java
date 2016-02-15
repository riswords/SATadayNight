/**
 * 
 */
package exception;

/**
 * Used for exceptions resulting from illegal states that occur during solver execution.
 * May be indicators of bugs in the system or from invalid/unexpected user inputs that were propagated further than 
 * they should be.
 *
 */
public class InternalSolverException extends UncheckedSolverException {
    private static final long serialVersionUID = 1L;

    public InternalSolverException(String message) {
        super(message);
    }
}
