package escuelaing.aygo.twitter.users.domain;

import escuelaing.aygo.twitter.users.application.repository.DynamoUserRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserService {
    private final UserRepository userRepository = new DynamoUserRepository();
    private final Clock clock = Clock.systemUTC();

    public User saveUser(User user) throws UserServiceException {
        user.setId(UUID.randomUUID().toString());
        user.setCreatedAt(LocalDateTime.now(clock));
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new UserAlreadyInUseException("Username " + user.getUsername() + " is already registered");
        }
        userRepository.save(user);
        return user;
    }

    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    public List<User> getAll() {
        return userRepository.getAll();
    }

    public void deleteUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->  new UserServiceException("User " + userId + " not found") );
        userRepository.delete(user);
    }

    public User updateUser(String userId, User user) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() ->  new UserServiceException("User " + userId + " not found") );
        currentUser.updateUserInformation(user,userRepository);
        userRepository.save(currentUser);
        return currentUser;
    }
}
