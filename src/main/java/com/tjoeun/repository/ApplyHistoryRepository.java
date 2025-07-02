package com.tjoeun.repository;

import com.tjoeun.entity.ApplyHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplyHistoryRepository extends JpaRepository<ApplyHistory, Integer> {
  List<ApplyHistory> findByUser_UserIdx(Integer userIdx);
  Page<ApplyHistory> findByUser_UserIdx(Integer userIdx, Pageable pageable);
}