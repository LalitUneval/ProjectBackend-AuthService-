package com.lalit.authservice.repository;


import com.lalit.authservice.entity.AuthUser;
import com.lalit.authservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {

    Optional<RefreshToken> findByToken(String token);

    // Find all tokens for a specific user
    List<RefreshToken> findByAuthUser(AuthUser authUser);

    // Find all tokens for a specific user by user ID
    List<RefreshToken> findByAuthUserId(Long authUserId);

    // Delete token by token string
    // when the user logout

    void deleteByToken(String token);

    // Delete all tokens for a specific user
    @Modifying
    @Query("DELETE FROM refresh_tokens rt WHERE rt.authUser.id = :userId")
    void deleteByAuthUserId(Long userId);

    // Delete all expired tokens
    @Modifying
    @Query("DELETE FROM refresh_tokens rt WHERE rt.expiryDate < :now")
    void deleteAllExpiredTokens(LocalDateTime now);

    // Find expired tokens
    List<RefreshToken> findByExpiryDateBefore(LocalDateTime dateTime);


    boolean existsByAuthUserIdAndDeviceFingerprint(Long userId, String deviceFingerprint);


}
