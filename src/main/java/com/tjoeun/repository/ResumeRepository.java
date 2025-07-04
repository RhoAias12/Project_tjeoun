package com.tjoeun.repository;

import com.tjoeun.entity.Resume;
import com.tjoeun.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
  List<Resume> findByUser(Users user);
  // id가 Long인 경우
}
