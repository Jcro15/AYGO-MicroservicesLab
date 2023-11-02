package escuelaing.aygo.twitter.users.domain;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    void save(User user);

    void delete(User user);

    Optional<User> findById(String userId);

    List<User> getAll();

    Optional<User> findByUsername(String username);
}
