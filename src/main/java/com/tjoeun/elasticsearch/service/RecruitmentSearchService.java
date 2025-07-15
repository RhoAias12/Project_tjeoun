package com.tjoeun.elasticsearch.service;

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
      boolean isAdmin = false;
      if (userIdx != null) {
        isAdmin = userRepository.findById(userIdx.intValue())
          .map(user -> user.getUserRole() == UserRole.ADMIN)
          .orElse(false);
      }
      if (!isAdmin) {
        searchLogRepository.save(SearchLog.builder()
          .keyword(keyword)
          .searchedAt(LocalDateTime.now())
          .userId(userIdx)
          .build());
      }
    }

    SearchResponse<RecruitmentDocument> response = esClient.search(s -> s
      .index(INDEX_NAME)
      .query(q -> containsSpecialRegexChars(keyword)
        ? q.regexp(r -> r.field("title.keyword").value(".*" + escapeForRegexp(keyword) + ".*"))
        : q.multiMatch(m -> m.fields("title.ngram^2", "title^1").query(keyword))
      ), RecruitmentDocument.class);

    return response.hits().hits().stream()
      .map(Hit::source)
      .collect(Collectors.toList());
  }

  public List<RecruitmentDocument> findAll() throws IOException {
    SearchResponse<RecruitmentDocument> response = esClient.search(s -> s
        .index(INDEX_NAME)
        .query(q -> q.matchAll(m -> m)),
      RecruitmentDocument.class);

    return response.hits().hits().stream()
      .map(Hit::source)
      .collect(Collectors.toList());
  }

  public Page<RecruitmentDocument> searchJobs(String title, String content, String region, String company,
                                              String startDate, String endDate, String sortOrder,
                                              int page, int pageSize) throws IOException {

    SearchRequest.Builder builder = new SearchRequest.Builder()
      .index(INDEX_NAME)
      .from((page - 1) * pageSize)
      .size(pageSize);

    builder.query(q -> q.bool(b -> {
      // title
      if (title != null && !title.isBlank()) {
        String escapedTitle = escapeWildcard(title.toLowerCase());
        b.must(m -> m
          .wildcard(w -> w
            .field("title.keyword") // .keyword 필드에서 정확히 일치 여부 확인
            .value("*" + escapedTitle + "*") // 포함 검색
          )
        );
      }
      // content
      if (content != null && !content.isBlank()) {
        String escapedContent = escapeWildcard(content.toLowerCase());
        b.must(m -> m.wildcard(w -> w
          .field("combinedContent.keyword")
          .value("*" + escapedContent + "*")
        ));
      }

      // region
      if (region != null && !region.isBlank()) {
        String escapedRegion = escapeWildcard(region.toLowerCase());
        b.must(m -> m.wildcard(w -> w
          .field("location.keyword")
          .value("*" + escapedRegion + "*")
        ));
      }

      // company
      if (company != null && !company.isBlank()) {
        String escapedCompany = escapeWildcard(company.toLowerCase());
        b.must(m -> m.wildcard(w -> w
          .field("company.keyword")
          .value("*" + escapedCompany + "*")
        ));
      }


      // 날짜 필터 + 상시채용
      if ((startDate != null && !startDate.isBlank()) || (endDate != null && !endDate.isBlank())) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String formattedStart = (startDate != null && !startDate.isBlank()) ?
          LocalDate.parse(startDate).atStartOfDay().format(formatter) : null;
        String formattedEnd = (endDate != null && !endDate.isBlank()) ?
          LocalDate.parse(endDate).atTime(23, 59, 59).format(formatter) : null;

        b.filter(f -> f.bool(bool -> bool
          .should(s -> s.range(r -> {
            r.field("deadline");
            if (formattedStart != null) r.gte(JsonData.of(formattedStart));
            if (formattedEnd != null) r.lte(JsonData.of(formattedEnd));
            return r;
          }))
          .should(s -> s.term(t -> t.field("deadline").value("9999-12-31T00:00:00")))
        ));
      }
      return b;
    }));

    switch (sortOrder) {
      case "all" -> builder.sort(s -> s.field(f -> f.field("recruitmentIdx").order(SortOrder.Asc)));
      case "deadline_asc" -> builder.sort(s -> s.field(f -> f.field("deadline").order(SortOrder.Asc)));
      case "deadline_desc" -> builder.sort(s -> s.field(f -> f.field("deadline").order(SortOrder.Desc)));
      case "scrap_desc" -> builder.sort(s -> s.field(f -> f.field("scrapCount").order(SortOrder.Desc)));
      case "scrap_asc" -> builder.sort(s -> s.field(f -> f.field("scrapCount").order(SortOrder.Asc)));
      default -> builder.sort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)));
    }

    SearchResponse<RecruitmentDocument> response = esClient.search(builder.build(), RecruitmentDocument.class);
    List<RecruitmentDocument> docs = response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
    long total = response.hits().total().value();

    return new PageImpl<>(docs, PageRequest.of(page - 1, pageSize), total);
  }

  public void updateScrapCountInES(Long recruitmentId, int newScrapCount) throws IOException {
    RecruitmentDocument doc = getDocumentById(recruitmentId);
    if (doc == null) throw new IllegalStateException("문서 없음");
    doc.setScrapCount(newScrapCount);
    esClient.index(i -> i.index(INDEX_NAME).id(String.valueOf(recruitmentId)).document(doc));
  }

  public RecruitmentDocument getDocumentById(Long recruitmentId) throws IOException {
    try {
      return esClient.get(g -> g.index(INDEX_NAME).id(String.valueOf(recruitmentId)), RecruitmentDocument.class).source();
    } catch (Exception e) {
      return null;
    }
  }

  public void saveOrUpdate(RecruitmentDocument doc) throws IOException {
    esClient.index(i -> i.index(INDEX_NAME).id(String.valueOf(doc.getRecruitmentIdx())).document(doc));
  }

  public void deleteById(Long recruitmentIdx) throws IOException {
    esClient.delete(d -> d.index(INDEX_NAME).id(String.valueOf(recruitmentIdx)));
  }

  private String escapeForRegexp(String input) {
    return input == null ? null :
      input.replaceAll("([\\\\~`!#\\$%\\^&\\*()_\\-\\+=\\[\\]\\{\\};:'\",\\.<>/\\?@])", "\\\\$1");
  }
  private boolean containsSpecialRegexChars(String input) {
    return input != null &&
      input.matches(".*[\\\\~`!#\\$%\\^&\\*()_\\-\\+=\\[\\]\\{\\};:'\",\\.<>/\\?@].*");
  }

  private String escapeWildcard(String input) {
    // 이스케이프할 특수문자 목록에 \와 | 추가
    return input.replaceAll("([~`!@#$%^&*()_\\-+=\\[\\]{};:\"',.<>?/\\\\|])", "\\\\$1");
  }
}
