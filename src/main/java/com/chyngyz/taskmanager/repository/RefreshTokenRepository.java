package com.chyngyz.taskmanager.repository;

import com.chyngyz.taskmanager.entity.RefreshToken;
import com.chyngyz.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteAllByUser(User user);
}
