package gtemp.gtemp_io.service;

import gtemp.gtemp_io.entity.User;
import gtemp.gtemp_io.repository.UserRepository;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    @Getter
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a new user with encrypted password
     */
    public User createUser(User user) {
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        return userRepository.save(user);
    }

    /**
     * Authenticate user by username or email and password
     */public User authenticateUser(String identifier, String password) {
            // Find user by username OR email
            User user = userRepository.findByUsernameOrEmail(identifier)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check password
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException("Invalid password");
            }

            return user;
     }

    /**
     * Save or update user
     */

    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(Long currentUserId, String username, String email) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (username != null && !username.trim().isEmpty()) {
            user.setUsername(username);
        }

        if (email != null && !email.trim().isEmpty()) {
            user.setEmail(email);
        }

        return userRepository.save(user);
    }

}
