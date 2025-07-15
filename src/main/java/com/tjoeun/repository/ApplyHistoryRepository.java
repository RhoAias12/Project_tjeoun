package com.tjoeun.repository;

import com.tjoeun.dto.ApplyHistoryDTO;
import com.tjoeun.elasticsearch.document.ApplyHistoryDocument;
import com.tjoeun.entity.ApplyHistory;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ApplyHistoryRepository extends JpaRepository<ApplyHistory, Integer> {
  Page<ApplyHistory> findByUser(Users user, Pageable pageable);

  Page<ApplyHistory> findAll(Pageable pageable);

  boolean existsByUser_UserIdxAndRecruitment_RecruitmentIdx(Integer userIdx, Long recruitmentIdx);

  boolean existsByUserAndRecruitment(Users user, Recruitment recruitment);

  List<ApplyHistory> findByRecruitment(Recruitment recruitment);

  @Query("SELECT COUNT(a) FROM ApplyHistory a WHERE a.user.userIdx = :userIdx")
  int countByUserIdx(@Param("userIdx") Long userIdx);

//  @Query(value = "SELECT AVG(cnt) FROM (SELECT COUNT(*) AS cnt FROM apply_history GROUP BY user_idx) AS A", nativeQuery = true)
//  Double findAvgApplyCountPerUser();

  @Query("SELECT COUNT(h) FROM ApplyHistory h GROUP BY h.user.userIdx")
  List<Long> findApplyCountsPerUser();

  @Query(value = """
    SELECT DATE(apply) as applyDate, COUNT(*) 
    FROM apply_history 
    WHERE user_idx = :userIdx 
      AND DATE(apply) BETWEEN :start AND :end
    GROUP BY DATE(apply)
    ORDER BY applyDate
""", nativeQuery = true)
  List<Object[]> countDailyApplies(
          @Param("userIdx") Long userIdx,
          @Param("start") LocalDate start,
          @Param("end") LocalDate end
  );

  long count(); // 전체 지원 수





}