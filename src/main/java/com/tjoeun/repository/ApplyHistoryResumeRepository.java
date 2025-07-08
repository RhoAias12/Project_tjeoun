package com.tjoeun.repository;

import com.tjoeun.entity.ApplyHistoryResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplyHistoryResumeRepository extends JpaRepository<ApplyHistoryResume, Long> {

}