package com.tjoeun.repository;

import com.tjoeun.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Integer> {
  Users findByUserEmail(String email);
  Users findByUserNickname(String nickname);

}
