package com.tjoeun.elasticsearch.repository;

import com.tjoeun.entity.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

    @Query(value = "SELECT keyword, COUNT(*) FROM search_log GROUP BY keyword ORDER BY COUNT(*) DESC LIMIT 5", nativeQuery = true)
    List<Object[]> findTopKeywords();
}
