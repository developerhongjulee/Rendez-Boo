package com.ssafy.a107.db.repository;

import com.ssafy.a107.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
