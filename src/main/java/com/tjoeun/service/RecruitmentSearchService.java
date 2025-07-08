package com.tjoeun.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.tjoeun.elasticsearch.document.RecruitmentDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruitmentSearchService {

  private final ElasticsearchClient esClient;
  private static final String INDEX_NAME = "recruitment_index";

  public List<RecruitmentDocument> search(String keyword) throws IOException {
    SearchResponse<RecruitmentDocument> response = esClient.search(s -> s
      .index(INDEX_NAME)
      .query(q -> q
        .multiMatch(t -> t
          .fields("title", "company", "responsibilities", "preferred")
          .query(keyword)
        )
      ), RecruitmentDocument.class);

    return response.hits().hits().stream()
      .map(Hit::source)
      .collect(Collectors.toList());
  }
}

