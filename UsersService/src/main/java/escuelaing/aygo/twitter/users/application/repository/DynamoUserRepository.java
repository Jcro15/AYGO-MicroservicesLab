package escuelaing.aygo.twitter.users.application.repository;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import escuelaing.aygo.twitter.users.domain.User;
import escuelaing.aygo.twitter.users.domain.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DynamoUserRepository implements UserRepository {
    private final String usernameIndex = "username-index";
    private final DynamoDBMapper dynamoDBMapper;

    public DynamoUserRepository() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        dynamoDBMapper = new DynamoDBMapper(client);
    }

    @Override
    public void save(User user) throws ConditionalCheckFailedException {
        dynamoDBMapper.save(user);
    }

    @Override
    public void delete(User user) {
        dynamoDBMapper.delete(user);
    }

    @Override
    public Optional<User> findById(String userId) {
        return Optional.ofNullable(dynamoDBMapper.load(User.class, userId));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":username", new AttributeValue().withS(username));

        DynamoDBQueryExpression<User> queryExpression = new DynamoDBQueryExpression<User>()
                .withIndexName(usernameIndex)
                .withKeyConditionExpression("username= :username").
                withExpressionAttributeValues(eav).withConsistentRead(false).withLimit(1);

        return dynamoDBMapper.queryPage(User.class, queryExpression).getResults().stream().findFirst();
    }

    @Override
    public List<User> getAll()
    {

        return dynamoDBMapper.scan(User.class, new DynamoDBScanExpression());
    }
}
