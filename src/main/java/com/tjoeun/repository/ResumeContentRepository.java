package com.tjoeun.repository;

import com.tjoeun.entity.Resume;
import com.tjoeun.entity.ResumeContent;
import com.tjoeun.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeContentRepository extends JpaRepository<ResumeContent, Long> {
}
