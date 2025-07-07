package com.tjoeun.repository;

import com.tjoeun.entity.Resume;
import com.tjoeun.entity.ResumeContent;
import com.tjoeun.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeContentRepository extends JpaRepository<ResumeContent, Long> {

  // 연관 Resume 기준으로 ResumeContent 전부 삭제하는 메소드
  void deleteByResume(Resume resume);
}
