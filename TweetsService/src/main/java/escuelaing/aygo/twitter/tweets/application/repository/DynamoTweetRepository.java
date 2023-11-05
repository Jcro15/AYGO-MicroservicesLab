package escuelaing.aygo.twitter.tweets.application.repository;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import escuelaing.aygo.twitter.tweets.domain.Tweet;
import escuelaing.aygo.twitter.tweets.domain.TweetRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DynamoTweetRepository implements TweetRepository {

    private final String userIdIndex = "userId-index";
    private final DynamoDBMapper dynamoDBMapper;

    public DynamoTweetRepository() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        dynamoDBMapper = new DynamoDBMapper(client);
    }

    @Override
    public void save(Tweet tweet) {
        dynamoDBMapper.save(tweet);
    }

    @Override
    public void delete(Tweet tweet) {
        dynamoDBMapper.delete(tweet);
    }

    @Override
    public Optional<Tweet> findById(String tweetId) {
        return Optional.ofNullable(dynamoDBMapper.load(Tweet.class, tweetId));
    }

    @Override
    public List<Tweet> getAll() {
        return dynamoDBMapper.scan(Tweet.class, new DynamoDBScanExpression());
    }

    @Override
    public List<Tweet> findAllByUserId(String userId) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":userId", new AttributeValue().withS(userId));

        DynamoDBQueryExpression<Tweet> queryExpression = new DynamoDBQueryExpression<Tweet>()
                .withIndexName(userIdIndex)
                .withKeyConditionExpression("userId= :userId").
                withExpressionAttributeValues(eav).withConsistentRead(false);

        return dynamoDBMapper.queryPage(Tweet.class, queryExpression).getResults();
    }
}
