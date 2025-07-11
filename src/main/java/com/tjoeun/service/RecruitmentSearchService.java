package com.tjoeun.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.tjoeun.elasticsearch.document.RecruitmentDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruitmentSearchService {

  private final ElasticsearchClient esClient;
  private static final String INDEX_NAME = "recruitments";

  public List<RecruitmentDocument> search(String keyword, String sortOrder) throws IOException {
    SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
      .index(INDEX_NAME)
      .query(q -> q
        .multiMatch(t -> t
          .fields("title", "company", "combinedContent")
          .query(keyword)
        )
      );

    // 정렬 조건 추가
    if ("deadline_asc".equals(sortOrder)) {
      searchBuilder.sort(s -> s.field(f -> f.field("deadline").order(SortOrder.Asc)));
    } else if ("deadline_desc".equals(sortOrder)) {
      searchBuilder.sort(s -> s.field(f -> f.field("deadline").order(SortOrder.Desc)));
    }

    SearchResponse<RecruitmentDocument> response = esClient.search(searchBuilder.build(), RecruitmentDocument.class);

    return response.hits().hits().stream()
      .map(Hit::source)
      .collect(Collectors.toList());
  }

  public Page<RecruitmentDocument> searchJobs(
    String keyword,
    String sortOrder,
    int page,
    int pageSize) throws IOException {

    SearchRequest.Builder builder = new SearchRequest.Builder()
      .index(INDEX_NAME)
      .query(keyword == null || keyword.isEmpty()
          ? q -> q.matchAll(m -> m)
          : q -> q.multiMatch(mm -> mm
          .fields("title", "company", "combinedContent")
          .query(keyword)
        )
      )
      .from((page - 1) * pageSize)
      .size(pageSize);

    // 정렬 적용
    if ("deadline_asc".equals(sortOrder)) {
      builder.sort(s -> s.field(f -> f.field("deadline").order(SortOrder.Asc)));
    } else if ("deadline_desc".equals(sortOrder)) {
      builder.sort(s -> s.field(f -> f.field("deadline").order(SortOrder.Desc)));
    } else {
      builder.sort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)));
    }

    SearchResponse<RecruitmentDocument> response = esClient.search(builder.build(), RecruitmentDocument.class);

    List<RecruitmentDocument> docs = response.hits().hits().stream()
      .map(Hit::source)
      .collect(Collectors.toList());

    long total = response.hits().total().value();

    return new PageImpl<>(docs, PageRequest.of(page - 1, pageSize), total);
  }

}

