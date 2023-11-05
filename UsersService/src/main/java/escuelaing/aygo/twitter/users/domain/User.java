package escuelaing.aygo.twitter.users.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import escuelaing.aygo.twitter.users.application.repository.LocalDateTimeConverter;

import java.time.LocalDateTime;

@DynamoDBTable(tableName = "user")
public class User {
    @DynamoDBHashKey(attributeName = "id")
    private String id;
    @DynamoDBIndexHashKey(attributeName = "username", globalSecondaryIndexName = "username-index")
    private String username;
    private String email;
    private String profileDescription;
    @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
    private LocalDateTime createdAt;

    public User() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getProfileDescription() {
        return profileDescription;
    }

    public void setProfileDescription(String profileDescription) {
        this.profileDescription = profileDescription;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void updateUserInformation(User user, UserRepository userRepository) throws UserServiceException {
        if(!user.getUsername().equals(username)) {
            if(userRepository.findByUsername(user.getUsername()).isPresent()){
                throw new UserAlreadyInUseException("Username " + user.getUsername() + " is already registered");
            }
            username = user.getUsername();
        }
        profileDescription = user.getProfileDescription();
        email = user.getEmail();
    }
}