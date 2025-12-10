package gtemp.gtemp_io.controller;

import gtemp.gtemp_io.dto.LoginRequest;
import gtemp.gtemp_io.dto.UpdateUserRequest;
import gtemp.gtemp_io.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import gtemp.gtemp_io.service.UserService;
import gtemp.gtemp_io.dto.UserResponse;
import java.util.Map;
import java.util.Collections;
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
//@CrossOrigin(origins = "http://localhost:5173")

public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/users")
    public User createUser(@RequestBody User user){
        return userService.createUser(user);
    }
    
    @GetMapping("/users/{email}/wallet")
    public ResponseEntity<?> getUserWallet(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(Collections.singletonMap("wallet", user.getWallet())))
                .orElseGet(() -> ResponseEntity.status(404).body("User not found"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            User user = userService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());
            UserResponse response = new UserResponse(user.getUsername(), user.getEmail(), user.getWallet(), user.getUserID());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PutMapping("/users")
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest request) {
        try {
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

    @PostMapping("/users/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        try {
            Long userId = Long.parseLong(body.get("userID"));
            String currentPassword = body.get("currentPassword");
            String newPassword = body.get("newPassword");

            User user = userService.getUserById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

            if (!userService.getPasswordEncoder().matches(currentPassword, user.getPassword())) {
                return ResponseEntity.status(400).body(Collections.singletonMap("message", "Current password is incorrect"));
            }

            user.setPassword(userService.getPasswordEncoder().encode(newPassword));
            userService.saveUser(user);

            return ResponseEntity.ok(Collections.singletonMap("message", "Password changed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Collections.singletonMap("message", "Failed to change password: " + e.getMessage()));
        }
    }

    @PostMapping("/users/{email}/wallet/add")
    public ResponseEntity<?> addLoad(@PathVariable String email, @RequestBody Map<String, Double> body) {
        try {
            double amount = body.get("amount");
            User user = userService.getUserByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));
            user.setWallet(user.getWallet() + amount);
            userService.saveUser(user);
            return ResponseEntity.ok(Collections.singletonMap("wallet", user.getWallet()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Collections.singletonMap("message", e.getMessage()));
        }
    }

}

