package collections.exception;

public class IndexOutOfBoundsException extends UncheckedCollectionsException {
    private static final long serialVersionUID = 1L;

    public IndexOutOfBoundsException(int invalidIndex) {
        this("Index out of bounds exception: attempt to access an invalid index " + invalidIndex);
    }
    
    public IndexOutOfBoundsException(String message) {
        super(message);
    }
}
