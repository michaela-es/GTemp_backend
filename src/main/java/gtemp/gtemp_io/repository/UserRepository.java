package gtemp.gtemp_io.repository;

import gtemp.gtemp_io.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    default Optional<User> findByUsernameOrEmail(String identifier) {
        return findByUsername(identifier)
                .or(() -> findByEmail(identifier));
    }

}