package escuelaing.aygo.twitter.tweets.domain;

public class TweetServiceException extends RuntimeException {
    public TweetServiceException() {
        super();
    }

    public TweetServiceException(String message) {
        super(message);
    }
}
