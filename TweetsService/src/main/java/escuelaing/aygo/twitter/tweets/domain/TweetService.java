package escuelaing.aygo.twitter.tweets.domain;

import escuelaing.aygo.twitter.tweets.application.repository.DynamoTweetRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TweetService {
    private final TweetRepository tweetRepository = new DynamoTweetRepository();
    private final Clock clock = Clock.systemUTC();

    public  Tweet saveTweet(Tweet tweet) {
        tweet.setId(UUID.randomUUID().toString());
        tweet.setCreatedAt(LocalDateTime.now(clock));
        tweetRepository.save(tweet);
        return tweet;
    }

    public Optional<Tweet> getTweetById(String tweetId) {
        return tweetRepository.findById(tweetId);
    }

    public List<Tweet> getAll() {
        return tweetRepository.getAll();
    }
    public void deleteTweetById(String tweetId) {
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new TweetServiceException("Tweet " + tweetId + " not found"));

        tweetRepository.delete(tweet);
    }

    public List<Tweet> getAllByUserId(String userId) {

        return tweetRepository.findAllByUserId(userId);

    }

    public Tweet updateTweet(String tweetId,Tweet tweet) {
        Tweet currentTweet = tweetRepository
                .findById(tweetId)
                .orElseThrow(() -> new TweetServiceException("Tweet " + tweetId + " not found"));
        currentTweet.updateTweetContent(tweet);
        tweetRepository.save(currentTweet);
        return currentTweet;
    }
}
