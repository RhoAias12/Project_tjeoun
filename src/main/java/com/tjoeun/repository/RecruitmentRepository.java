package com.tjoeun.repository;

import com.tjoeun.entity.Recruitment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {

    @Query("SELECT r FROM Recruitment r LEFT JOIN r.favorites f GROUP BY r ORDER BY COUNT(f) DESC")
    Page<Recruitment> findAllSortedByScrapCount(Pageable pageable);

}