package com.tjoeun.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.tjoeun.elasticsearch.document.RecruitmentDocument;
import com.tjoeun.elasticsearch.repository.SearchLogRepository;
import com.tjoeun.entity.SearchLog;
import com.tjoeun.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tjoeun.constant.UserRole;


import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

  private static final String[] SPECIAL_CHARS_FOR_SQS = {
    "+", "-", "&&", "||", "!", "(", ")", "{", "}", "[", "]", "^", "\"", "~", "*", "?", ":", "\\", "/"
  };

  private String escapeSpecialCharsForSimpleQueryString(String input) {
    if (input == null) return null;
    String escaped = input;
    for (String ch : SPECIAL_CHARS_FOR_SQS) {
      escaped = escaped.replace(ch, "\\" + ch);
    }
    return escaped;
  }

  private String escapeWildcard(String input) {
    return input.replaceAll("([\\*\\?\\[\\]])", "\\\\$1");
  }

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

      if (title != null && !title.isBlank()) {
        // 제목에 포함된 특수문자들을 이스케이프 처리
        String escapedTitle = escapeSpecialCharsForSimpleQueryString(title);

        // 쿼리에서 이스케이프된 제목을 사용
        b.must(m -> m.queryString(qs -> qs
                .fields("title")
                .query(escapedTitle)  // 이스케이프된 제목으로 검색
        ));
      }

      if (content != null && !content.isBlank()) {
        String escapedContent = escapeSpecialCharsForSimpleQueryString(content);
        b.must(m -> m.simpleQueryString(sqs -> sqs
          .fields("combinedContent")
          .query(escapedContent)
        ));
      }

      if (region != null && !region.isBlank()) {
        String escapedRegion = escapeSpecialCharsForSimpleQueryString(region);
        b.must(m -> m.simpleQueryString(sqs -> sqs
          .fields("location")
          .query(escapedRegion)
        ));
      }

      if (company != null && !company.isBlank()) {
        String escapedCompany = escapeSpecialCharsForSimpleQueryString(company);
        b.must(m -> m.simpleQueryString(sqs -> sqs
          .fields("company")
          .query(escapedCompany)
        ));
      }

      // 날짜 범위 필터 (deadline)
      if ((startDate != null && !startDate.isBlank()) || (endDate != null && !endDate.isBlank())) {
        b.filter(f -> f.range(r -> {
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
      }

      return b;
    }));

    // 정렬
    switch (sortOrder) {
      case "all":
        builder.sort(s -> s.field(f -> f.field("recruitmentIdx").order(SortOrder.Asc)));
        break;
      case "deadline_asc":
        builder.sort(s -> s.field(f -> f.field("deadline").order(SortOrder.Asc)));
        break;
      case "deadline_desc":
        builder.sort(s -> s.field(f -> f.field("deadline").order(SortOrder.Desc)));
        break;
      case "scrap_desc":
        builder.sort(s -> s.field(f -> f.field("scrapCount").order(SortOrder.Desc)));
        break;
      case "scrap_asc":
        builder.sort(s -> s.field(f -> f.field("scrapCount").order(SortOrder.Asc)));
        break;
      default:
        builder.sort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)));
        break;
    }

    SearchRequest request = builder.build();

    SearchResponse<RecruitmentDocument> response = esClient.search(request, RecruitmentDocument.class);

    List<RecruitmentDocument> docs = response.hits().hits().stream()
      .map(Hit::source)
      .collect(Collectors.toList());

    long total = response.hits().total().value();

    return new PageImpl<>(docs, PageRequest.of(page - 1, pageSize), total);
  }


  public void updateScrapCountInES(Long recruitmentId, int newScrapCount) throws IOException {
    // 문서 조회
    RecruitmentDocument doc = getDocumentById(recruitmentId);
    if (doc == null) {
      throw new IllegalStateException("해당 공고 문서를 Elasticsearch에서 찾을 수 없습니다.");
    }

    // 스크랩 수 갱신
    doc.setScrapCount(newScrapCount);

    // 문서 업데이트 (덮어쓰기)
    esClient.index(i -> i
      .index(INDEX_NAME)
      .id(String.valueOf(recruitmentId))
      .document(doc)
    );
  }

  public RecruitmentDocument getDocumentById(Long recruitmentId) throws IOException {
    try {
      return esClient.get(g -> g
          .index(INDEX_NAME)
          .id(String.valueOf(recruitmentId)),
        RecruitmentDocument.class
      ).source();
    } catch (Exception e) {
      return null;
    }
  }

  public void saveOrUpdate(RecruitmentDocument doc) throws IOException {
    esClient.index(i -> i
      .index(INDEX_NAME)
      .id(String.valueOf(doc.getRecruitmentIdx()))
      .document(doc)
    );
  }

  public void deleteById(Long recruitmentIdx) throws IOException {
    esClient.delete(d -> d
      .index(INDEX_NAME)
      .id(String.valueOf(recruitmentIdx))
    );
  }


}

