package com.Ecommerce.User.repository;

import com.Ecommerce.User.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}