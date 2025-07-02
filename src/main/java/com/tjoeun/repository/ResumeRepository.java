package com.tjoeun.repository;

import com.tjoeun.entity.Resume;
import com.tjoeun.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
  // 특정 사용자에 해당하는 이력서 리스트를 반환하는 메서드
  List<Resume> findByUser(Users user);
}
