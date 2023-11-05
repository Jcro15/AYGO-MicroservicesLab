package escuelaing.aygo.twitter.tweets.domain;

import java.util.List;
import java.util.Optional;

public interface TweetRepository {
    void save(Tweet tweet);

    void delete(Tweet tweet);

    Optional<Tweet> findById(String tweetId);

    List<Tweet> getAll();

    List<Tweet> findAllByUserId(String userId);
}
