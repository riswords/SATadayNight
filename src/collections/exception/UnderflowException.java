package collections.exception;

public class UnderflowException extends UncheckedCollectionsException {

    private static final long serialVersionUID = 1L;

    public UnderflowException() {
        this("Underflow exception: unable to remove from empty collection.");
    }
    
    public UnderflowException(String message) {
        super(message);
    }
}
