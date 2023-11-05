package escuelaing.aygo.twitter.tweets.domain;

public class NoAccessException extends RuntimeException {
    public NoAccessException() {
        super();
    }

    public NoAccessException(String message) {
        super(message);
    }
}
