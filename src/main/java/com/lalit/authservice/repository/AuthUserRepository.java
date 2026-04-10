package com.lalit.authservice.repository;

import com.lalit.authservice.entity.AuthUser;
import com.lalit.authservice.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser ,Long> {

    Optional<AuthUser> findByEmail(String email);

    boolean existsByEmail(String email);

    List<AuthUser> findByRole(UserRole role);

    List<AuthUser> findByIsActive(Boolean isActive);

    List<AuthUser> findByRoleAndIsActive(UserRole role, Boolean isActive);

    Page<AuthUser> findByRoleNot(UserRole role, Pageable pageable);

}
