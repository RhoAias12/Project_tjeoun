package com.tjoeun.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.tjoeun.elasticsearch.document.RecruitmentDocument;
import com.tjoeun.elasticsearch.repository.SearchLogRepository;
import com.tjoeun.entity.SearchLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  @Transactional
  public List<RecruitmentDocument> search(String keyword) throws IOException {

    // 1. 검색 로그 저장
    System.out.println("🔍 검색 로그 저장 시도 전");

    try {
      searchLogRepository.save(SearchLog.builder()
              .keyword("테스트키워드")
              .searchedAt(LocalDateTime.now())
              .ip("127.0.0.1")
              .userId(1L)
              .build());
      System.out.println("✅ 검색 로그 저장 성공");
    } catch (Exception e) {
      System.out.println("❌ 검색 로그 저장 실패: " + e.getMessage());
      e.printStackTrace();
    }


    // 2. Elasticsearch 검색
    SearchResponse<RecruitmentDocument> response = esClient.search(s -> s
      .index(INDEX_NAME)
      .query(q -> q
        .multiMatch(t -> t
          .fields("title", "company", "combinedContent")
          .query(keyword)
        )
      ), RecruitmentDocument.class);

    return response.hits().hits().stream()
      .map(Hit::source)
      .collect(Collectors.toList());
  }
}

