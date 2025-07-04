package com.tjoeun.repository;

import com.tjoeun.entity.Resume;
import com.tjoeun.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
  Optional<Resume> findTopByUserOrderByCreatedAtDesc(Users user);
  List<Resume> findByUser(Users user);
}
