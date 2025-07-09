package com.tjoeun.repository;

import com.tjoeun.dto.ApplyHistoryDTO;
import com.tjoeun.entity.ApplyHistory;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ApplyHistoryRepository extends JpaRepository<ApplyHistory, Integer> {
  List<ApplyHistory> findByUser_UserIdx(Integer userIdx);
  Page<ApplyHistory> findByUser(Users user, Pageable pageable);

  Page<ApplyHistory> findAll(Pageable pageable);

  boolean existsByUser_UserIdxAndRecruitment_RecruitmentIdx(Integer userIdx, Long recruitmentIdx);

  boolean existsByUserAndRecruitment(Users user, Recruitment recruitment);
}