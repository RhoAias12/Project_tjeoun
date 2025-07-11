package com.tjoeun.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.tjoeun.elasticsearch.document.RecruitmentDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruitmentSearchService {

  private final ElasticsearchClient esClient;
  private static final String INDEX_NAME = "recruitments";

  public Page<RecruitmentDocument> searchJobs(
    String title,
    String content,
    String region,
    String company,
    String startDate,
    String endDate,
    String sortOrder,
    int page,
    int pageSize
  ) throws IOException {

    SearchRequest.Builder builder = new SearchRequest.Builder()
      .index(INDEX_NAME)
      .from((page - 1) * pageSize)
      .size(pageSize);

    builder.query(q -> q.bool(b -> {
      // title, content, region, company 필터
      if (title != null && !title.isBlank()) {
        b.must(m -> m.match(mm -> mm.field("title").query(title)));
      }
      if (content != null && !content.isBlank()) {
        b.must(m -> m.match(mm -> mm.field("combinedContent").query(content)));
      }
      if (region != null && !region.isBlank()) {
        b.must(m -> m.match(mm -> mm.field("location").query(region)));
      }
      if (company != null && !company.isBlank()) {
        b.must(m -> m.match(mm -> mm.field("company").query(company)));
      }

      // 날짜 필터 조건 (deadline or 9999-12-31T00:00:00)
      if ((startDate != null && !startDate.isBlank()) || (endDate != null && !endDate.isBlank())) {
        b.filter(f -> f.bool(boolQuery -> {
          boolQuery.should(s1 -> s1.range(r -> {
            r.field("deadline");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            if (startDate != null && !startDate.isBlank()) {
              LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
              r.gte(JsonData.of(start.format(formatter)));
            }
            if (endDate != null && !endDate.isBlank()) {
              LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
              r.lte(JsonData.of(end.format(formatter)));
            }
            return r;
          }));

          boolQuery.should(s2 -> s2.term(t ->
            t.field("deadline").value("9999-12-31T00:00:00")
          ));

          boolQuery.minimumShouldMatch("1");
          return boolQuery;
        }));
      }

      return b;
    }));

    // 정렬
    if ("deadline_asc".equals(sortOrder)) {
      builder.sort(s -> s.field(f -> f.field("deadline").order(SortOrder.Asc)));
    } else if ("deadline_desc".equals(sortOrder)) {
      builder.sort(s -> s.field(f -> f.field("deadline").order(SortOrder.Desc)));
    } else if ("scrap_desc".equals(sortOrder)) {
      builder.sort(s -> s.field(f -> f.field("scrapCount").order(SortOrder.Desc)));
    } else if ("scrap_asc".equals(sortOrder)) {
      builder.sort(s -> s.field(f -> f.field("scrapCount").order(SortOrder.Asc)));
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

