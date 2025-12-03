package bg.sofia.uni.fmi.mjt.foodanalyzer.client.exception;

public class NotInStorageException extends Exception {
    public NotInStorageException(String message) {
        super(message);
    }

    public NotInStorageException(String message, Throwable e) {
        super(message, e);
    }
}
