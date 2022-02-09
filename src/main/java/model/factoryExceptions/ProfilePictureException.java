package model.factoryExceptions;

public class ProfilePictureException extends RuntimeException {
    public ProfilePictureException() {
    }

    public ProfilePictureException(String message) {
        super(message);
    }

    public ProfilePictureException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProfilePictureException(Throwable cause) {
        super(cause);
    }
}
