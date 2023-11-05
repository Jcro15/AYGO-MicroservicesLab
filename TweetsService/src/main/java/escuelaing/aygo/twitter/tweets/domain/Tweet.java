package escuelaing.aygo.twitter.tweets.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import escuelaing.aygo.twitter.tweets.application.repository.LocalDateTimeConverter;

import java.time.LocalDateTime;
@DynamoDBTable(tableName = "tweet")
public class Tweet {
    @DynamoDBHashKey(attributeName = "id")
    private String id;
    @DynamoDBIndexHashKey(attributeName = "userId", globalSecondaryIndexName = "userId-index")
    private String userId;
    private String content;
    @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
    private LocalDateTime createdAt;

    public Tweet() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void updateTweetContent(Tweet tweet) {
        if (!tweet.userId.equals(this.userId)){
            throw new NoAccessException("Only the original author of the tweet can modify it");
        }
        content = tweet.content;
    }
}
