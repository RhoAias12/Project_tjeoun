package com.tjoeun.repository;

import com.tjoeun.entity.Recruitment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {

    @Query("SELECT r FROM Recruitment r JOIN r.favorites f GROUP BY r HAVING COUNT(f) > 0 ORDER BY COUNT(f) DESC")
    Page<Recruitment> findOnlyFavorited(Pageable pageable);


}