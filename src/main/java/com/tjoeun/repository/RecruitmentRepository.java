package com.tjoeun.repository;

import com.tjoeun.entity.Recruitment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {

    // 스크랩된 공고만 가져오는 네이티브 쿼리
    @Query(value = """
            SELECT r.* FROM recruitment r
            JOIN favorite f ON r.recruitment_idx = f.recruitment_idx
            GROUP BY r.recruitment_idx
            HAVING COUNT(f.favorite_idx) > 0
            ORDER BY COUNT(f.favorite_idx) DESC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT r.recruitment_idx) FROM recruitment r
            JOIN favorite f ON r.recruitment_idx = f.recruitment_idx
            """,
            nativeQuery = true)
    Page<Recruitment> findOnlyFavorited(Pageable pageable);

    // 마감일 오름차순
    Page<Recruitment> findAllByOrderByDeadlineAsc(Pageable pageable);


}