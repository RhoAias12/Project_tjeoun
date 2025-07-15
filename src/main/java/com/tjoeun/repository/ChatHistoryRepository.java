package com.tjoeun.repository;

import com.tjoeun.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

  List<ChatHistory> findTop10ByUserIdOrderByCreatedAtAsc(String userId);

}