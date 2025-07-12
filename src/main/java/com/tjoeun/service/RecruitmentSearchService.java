package com.tjoeun.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.tjoeun.elasticsearch.document.RecruitmentDocument;
import com.tjoeun.elasticsearch.repository.SearchLogRepository;
import com.tjoeun.entity.SearchLog;
import com.tjoeun.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tjoeun.constant.UserRole;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RecruitmentSearchService {

  private final ElasticsearchClient esClient;
  private final SearchLogRepository searchLogRepository;
  private static final String INDEX_NAME = "recruitments";
  private final UserRepository userRepository;

  @Transactional
  public List<RecruitmentDocument> search(String keyword, Long userIdx) throws IOException {
    if (keyword != null && !keyword.isBlank()) {
      // ✅ userIdx가 null이 아닌 경우에만 사용자 조회
      boolean isAdmin = false;
      if (userIdx != null) {
        isAdmin = userRepository.findById(userIdx.intValue())
                .map(user -> user.getUserRole() == UserRole.ADMIN)
                .orElse(false);
      }

      // ✅ admin이 아닐 경우에만 로그 저장
      if (!isAdmin) {
        searchLogRepository.save(SearchLog.builder()
                .keyword(keyword)
                .searchedAt(LocalDateTime.now())
                .userId(userIdx) // null 허용
                .build());
      }
    }

    SearchResponse<RecruitmentDocument> response = esClient.search(s -> s
            .index(INDEX_NAME)
            .query(q -> q
                    .multiMatch(t -> t
                            .fields("title", "content")
                            .query(keyword)
                    )
            ), RecruitmentDocument.class);

    return response.hits().hits().stream()
            .map(Hit::source)
            .collect(Collectors.toList());
  }






  public List<RecruitmentDocument> findAll() throws IOException {
    SearchResponse<RecruitmentDocument> response = esClient.search(s -> s
                    .index(INDEX_NAME)
                    .query(q -> q.matchAll(m -> m)), // 모든 문서 조회
            RecruitmentDocument.class
    );

    return response.hits().hits().stream()
            .map(Hit::source)
            .collect(Collectors.toList());
  }

}

