package collections.exception;

import exception.UncheckedSolverException;

public abstract class UncheckedCollectionsException extends UncheckedSolverException {

    private static final long serialVersionUID = 1L;
    
    public UncheckedCollectionsException(String message) {
        super(message);
    }
}
