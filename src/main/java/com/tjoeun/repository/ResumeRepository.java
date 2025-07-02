package com.tjoeun.repository;

import com.tjoeun.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
  // id가 Long인 경우
}
