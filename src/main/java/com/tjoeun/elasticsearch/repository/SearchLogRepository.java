package com.tjoeun.elasticsearch.repository;

import com.tjoeun.entity.SearchLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

    @Query("SELECT s.keyword, COUNT(s) FROM SearchLog s GROUP BY s.keyword ORDER BY COUNT(s) DESC")
    List<Object[]> findTopKeywords(Pageable pageable);




}
