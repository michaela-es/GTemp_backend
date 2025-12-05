package gtemp.gtemp_io.controller;

import gtemp.gtemp_io.dto.LoginRequest;
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
            UserResponse response = new UserResponse(user.getUsername(), user.getEmail(), user.getWallet());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}

