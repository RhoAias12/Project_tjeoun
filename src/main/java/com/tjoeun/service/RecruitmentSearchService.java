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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RecruitmentSearchService {

  private final ElasticsearchClient esClient;
  private final SearchLogRepository searchLogRepository;
  private static final String INDEX_NAME = "recruitments";

  @Transactional
  public List<RecruitmentDocument> search(String keyword, Long userIdx) throws IOException {
    if (keyword != null && !keyword.isBlank()) {
      searchLogRepository.save(SearchLog.builder()
              .keyword(keyword)
              .searchedAt(LocalDateTime.now())
              .userId(userIdx) // null도 허용 가능
              .build());
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

  // ✅ 오버로딩: 비로그인 사용자용
  public List<RecruitmentDocument> search(String keyword) throws IOException {
    return search(keyword, null);  // userId 없이 저장됨
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

