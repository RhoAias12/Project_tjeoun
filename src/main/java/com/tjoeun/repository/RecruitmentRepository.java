package com.tjoeun.repository;

import com.tjoeun.dto.RecruitmentDTO;
import com.tjoeun.entity.Recruitment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> , JpaSpecificationExecutor<Recruitment> {
  // 기본 CRUD 메서드 자동 제공
  int deleteByDeadlineBefore(LocalDate date);

  // 중복 공고 확인용 메서드 추가
  boolean existsByTitleAndCompanyAndDeadline(String title, String company, LocalDateTime deadline);

  // 페이징 메서드
  Page<Recruitment> findAll(Pageable pageable);

  // 스크랩된 공고만 가져오는 네이티브 쿼리
//  @Query(value = """
//            SELECT r.* FROM recruitment r
//            JOIN favorite f ON r.recruitment_idx = f.recruitment_idx
//            GROUP BY r.recruitment_idx
//            HAVING COUNT(f.favorite_idx) > 0
//            ORDER BY COUNT(f.favorite_idx) DESC
//            """,
//    countQuery = """
//            SELECT COUNT(DISTINCT r.recruitment_idx) FROM recruitment r
//            JOIN favorite f ON r.recruitment_idx = f.recruitment_idx
//            """,
//    nativeQuery = true)
//  Page<Recruitment> findOnlyFavorited(Pageable pageable);

  @Query("SELECT new com.tjoeun.dto.RecruitmentDTO(r.id, r.title, r.company, r.deadline, COUNT(f)) " +
          "FROM Recruitment r LEFT JOIN Favorite f ON r.id = f.recruitment.id " +
          "GROUP BY r.id, r.title, r.company, r.deadline " +
          "ORDER BY COUNT(f) DESC")
  Page<RecruitmentDTO> findAllOrderByScrapCountDesc(Pageable pageable);

  @Query("SELECT new com.tjoeun.dto.RecruitmentDTO(r.id, r.title, r.company, r.deadline, COUNT(f)) " +
          "FROM Recruitment r LEFT JOIN Favorite f ON r.id = f.recruitment.id " +
          "GROUP BY r.id, r.title, r.company, r.deadline " +
          "ORDER BY COUNT(f) ASC")
  Page<RecruitmentDTO> findAllOrderByScrapCountAsc(Pageable pageable);


  // 마감일 오름차순
  Page<Recruitment> findAllByOrderByDeadlineAsc(Pageable pageable);
}