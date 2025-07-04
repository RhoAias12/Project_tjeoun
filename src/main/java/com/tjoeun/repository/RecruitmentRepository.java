package com.tjoeun.repository;

import com.tjoeun.entity.Recruitment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {
  // 기본 CRUD 메서드 자동 제공
  int deleteByDeadlineBefore(LocalDate date);

  // 중복 공고 확인용 메서드 추가
  boolean existsByTitleAndCompanyAndDeadline(String title, String company, LocalDate deadline);

  // 페이징 메서드
  Page<Recruitment> findAll(Pageable pageable);


}
