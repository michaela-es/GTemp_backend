package gtemp.gtemp_io.controller;

import gtemp.gtemp_io.dto.*;
import gtemp.gtemp_io.entity.User;
import gtemp.gtemp_io.security.jwt.JwtService;
import gtemp.gtemp_io.service.UserService;
import gtemp.gtemp_io.utils.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtil securityUtil;

    public UserController(UserService userService, JwtService jwtService,
                          PasswordEncoder passwordEncoder, SecurityUtil securityUtil) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/users/register")
    public ResponseEntity<?> createUser(@RequestBody RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setWallet(0.0);

        User savedUser = userService.saveUser(user);

        String token = jwtService.generateToken(savedUser.getUserID(), savedUser.getUsername());

        AuthResponse response = new AuthResponse(
                token,
                savedUser.getUserID(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getWallet()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        User user = userService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());

        String token = jwtService.generateToken(user.getUserID(), user.getUsername());

        AuthResponse response = new AuthResponse(
                token,
                user.getUserID(),
                user.getUsername(),
                user.getEmail(),
                user.getWallet()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/me")
    public ResponseEntity<?> getCurrentUser() {
        Long userId = securityUtil.getCurrentUserId();
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(new UserResponse(user));
    }

    @PutMapping("/users")
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest request) {
        Long currentUserId = securityUtil.getCurrentUserId();

        User updatedUser = userService.updateUser(
                currentUserId,
                request.getUsername(),
                request.getEmail()
        );

        return ResponseEntity.ok(new UserResponse(updatedUser));
    }

    @PostMapping("/users/wallet/add")
    public ResponseEntity<?> addToWallet(@RequestBody AddToWalletRequest request) {
        Long userId = securityUtil.getCurrentUserId();

        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setWallet(user.getWallet() + request.getAmount());
        userService.saveUser(user);

        return ResponseEntity.ok(Collections.singletonMap("wallet", user.getWallet()));
    }
}