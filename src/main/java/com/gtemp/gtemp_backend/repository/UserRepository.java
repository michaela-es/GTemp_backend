package com.gtemp.gtemp_backend.repository;

import com.gtemp.gtemp_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Optional: you can add custom queries here if needed
    User findByUsername(String username);
}
