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
            throw new UserServiceException("Username " + user.getUsername() + " is already registered");
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
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()){
            throw new UserServiceException("User " + userId + " not found");
        }
        userRepository.delete(user.get());
    }
}
