package com.tjoeun.repository;

import com.tjoeun.entity.Resume;
import com.tjoeun.entity.Users;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;



import java.util.List;

import java.util.Optional;

import java.util.List;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
  Optional<Resume> findTopByUserOrderByCreatedAtDesc(Users user);
  List<Resume> findByUser(Users user);

  List<Resume> findByUser_UserIdx(Long userIdx);

  Page<Resume> findByUser_UserIdx(Long userIdx, Pageable pageable);

  @EntityGraph(attributePaths = "resumeContents")
  Optional<Resume> findWithContentsByResumeIdx(Long resumeIdx);

}
