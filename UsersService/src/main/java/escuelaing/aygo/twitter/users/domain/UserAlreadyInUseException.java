package escuelaing.aygo.twitter.users.domain;

public class UserAlreadyInUseException extends RuntimeException {
    public UserAlreadyInUseException() {
        super();
    }

    public UserAlreadyInUseException(String message) {
        super(message);
    }
}
