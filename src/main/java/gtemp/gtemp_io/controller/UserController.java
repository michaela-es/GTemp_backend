package gtemp.gtemp_io.controller;

import gtemp.gtemp_io.dto.*;
import gtemp.gtemp_io.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import gtemp.gtemp_io.service.UserService;
import gtemp.gtemp_io.security.jwt.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Map;
import java.util.Collections;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    // Updated constructor
    public UserController(UserService userService, JwtService jwtService, PasswordEncoder passwordEncoder){
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    // ==================== PUBLIC ENDPOINTS ====================

    // Register - returns JWT token
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody RegisterRequest request) {
        try {
            // Create user entity from request
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setWallet(0.0);

            // Save user
            User savedUser = userService.saveUser(user);

            // Generate JWT token with userID
            String token = jwtService.generateToken(savedUser.getUserID(), savedUser.getUsername());

            // Return response with token
            AuthResponse response = new AuthResponse(
                    token,
                    savedUser.getUserID(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getWallet()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Login - returns JWT token
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user
            User user = userService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());

            // Generate JWT token with userID
            String token = jwtService.generateToken(user.getUserID(), user.getUsername());

            // Return response with token
            AuthResponse response = new AuthResponse(
                    token,
                    user.getUserID(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getWallet()
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // ==================== PROTECTED ENDPOINTS ====================

    // Get wallet - requires JWT
    @GetMapping("/users/{email}/wallet")
    public ResponseEntity<?> getUserWallet(
            @PathVariable String email,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // Validate JWT
        try {
            validateJwt(authHeader);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }

        return userService.getUserByEmail(email)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(Collections.singletonMap("wallet", user.getWallet())))
                .orElseGet(() -> ResponseEntity.status(404).body("User not found"));
    }

    // Update user - requires JWT
    @PutMapping("/users")
    public ResponseEntity<?> updateUser(
            @RequestBody UpdateUserRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            // Validate JWT and extract userID
            Long tokenUserId = validateJwtAndGetUserId(authHeader);

            // Check if user is updating their own profile
            if (!tokenUserId.equals(request.getUserID())) {
                return ResponseEntity.status(403).body("Cannot update another user's profile");
            }

            User updatedUser = userService.updateUser(
                    request.getUserID(),
                    request.getUsername(),
                    request.getEmail()
            );

            return ResponseEntity.ok(new UserResponse(updatedUser));

        } catch (Exception e) {
            return ResponseEntity.status(400).body("Failed to update user: " + e.getMessage());
        }
    }

    // Change password - requires JWT
    @PostMapping("/users/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            // Validate JWT and extract userID
            Long tokenUserId = validateJwtAndGetUserId(authHeader);

            // Get user by ID
            User user = userService.getUserById(tokenUserId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Verify current password
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                return ResponseEntity.status(400).body(Collections.singletonMap("message", "Current password is incorrect"));
            }

            // Update password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userService.saveUser(user);

            return ResponseEntity.ok(Collections.singletonMap("message", "Password changed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Collections.singletonMap("message", "Failed to change password: " + e.getMessage()));
        }
    }

    // Add to wallet - requires JWT
    @PostMapping("/users/{email}/wallet/add")
    public ResponseEntity<?> addLoad(
            @PathVariable String email,
            @RequestBody Map<String, Double> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            // Validate JWT and extract userID
            Long tokenUserId = validateJwtAndGetUserId(authHeader);

            // Get user to check ownership
            User user = userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if user owns this wallet
            if (!user.getUserID().equals(tokenUserId)) {
                return ResponseEntity.status(403).body(Collections.singletonMap("message", "Cannot add to another user's wallet"));
            }

            // Add money to wallet
            double amount = body.get("amount");
            user.setWallet(user.getWallet() + amount);
            userService.saveUser(user);

            return ResponseEntity.ok(Collections.singletonMap("wallet", user.getWallet()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    // ==================== HELPER METHODS ====================

    private void validateJwt(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid authorization header");
        }

        String token = authHeader.substring(7);
        if (!jwtService.isTokenValid(token)) {
            throw new RuntimeException("Token expired or invalid");
        }
    }

    private Long validateJwtAndGetUserId(String authHeader) {
        validateJwt(authHeader);
        String token = authHeader.substring(7);
        return jwtService.extractUserId(token);
    }
}