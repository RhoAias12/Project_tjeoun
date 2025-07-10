package com.tjoeun.repository;

import com.tjoeun.entity.ApplyHistoryResumeContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplyHistoryResumeContentRepository extends JpaRepository<ApplyHistoryResumeContent, Long> {

}
