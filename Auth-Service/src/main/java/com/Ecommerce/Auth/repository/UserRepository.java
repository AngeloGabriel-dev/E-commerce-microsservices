package com.Ecommerce.Auth.repository;

import com.Ecommerce.Auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("Select u.role from User u where u.email like :email")
    User.Role findRoleByEmail(String email);
}