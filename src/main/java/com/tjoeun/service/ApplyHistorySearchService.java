package com.tjoeun.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonData;
import com.tjoeun.elasticsearch.document.ApplyHistoryDocument;
import com.tjoeun.elasticsearch.repository.ApplyHistorySearchRepository;
import com.tjoeun.entity.ApplyHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplyHistorySearchService {

  private final ApplyHistorySearchRepository applyHistorySearchRepository;
  private final ElasticsearchClient elasticsearchClient;

  public void updateStatusInES(Integer applyHistoryId, ApplyHistory.ApplyStatus newStatus) {
    Optional<ApplyHistoryDocument> optionalDoc = applyHistorySearchRepository.findById(applyHistoryId);
    if (optionalDoc.isPresent()) {
      ApplyHistoryDocument doc = optionalDoc.get();
      doc.setStatus(newStatus.name());
      doc.setStatusDisplay(getStatusDisplayName(newStatus.name()));
      applyHistorySearchRepository.save(doc);
    } else {
      System.out.println("해당 지원기록이 ES에 존재하지 않습니다" + applyHistoryId);
    }


  }

  public String getStatusDisplayName(String status) {
    return switch (status) {
      case "SUBMITTED" -> "진행중";
      case "FINALIZED" -> "합격";
      case "REJECTED"  -> "불합격";
      default -> "알 수 없음";
    };
  }

  private String escapeWildcard(String input) {
    if (input == null) return null;
    String escaped = input;
    String[] specialChars = { "+", "-", "&&", "||", "!", "(", ")", "{", "}", "[", "]", "^", "\"", "~", "*", "?", ":", "\\", "/" };

    for (String ch : specialChars) {
      escaped = escaped.replace(ch, "\\" + ch);
    }

    return escaped;
  }


  public Page<ApplyHistoryDocument> searchApplyHistories(
    String recruitmentTitle,
    String recruitmentCompany,
    String userEmail,
    String userNickname,
    String status,
    String startDate,
    String endDate,
    int page,
    int size,
    String sortOption
  ) throws IOException {

    BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
    List<Query> mustQueries = new java.util.ArrayList<>();

    // 제목 필터링
    if (recruitmentTitle != null && !recruitmentTitle.isBlank()) {
      mustQueries.add(Query.of(q -> q
        .wildcard(w -> w
          .field("recruitmentTitle")
          .value("*" + recruitmentTitle.toLowerCase() + "*")
        )
      ));
    }

    // 회사명 필터링
    if (recruitmentCompany != null && !recruitmentCompany.isBlank()) {
      mustQueries.add(Query.of(q -> q
        .wildcard(w -> w
          .field("recruitmentCompany")
          .value("*" + recruitmentCompany.toLowerCase() + "*")
        )
      ));
    }

    // 이메일 필터링
    if (userEmail != null && !userEmail.isBlank()) {
      mustQueries.add(Query.of(q -> q
        .wildcard(w -> w
          .field("userEmail")
          .value("*" + userEmail.toLowerCase() + "*")
        )
      ));
    }

    // 사용자 닉네임 필터링
    if (userNickname != null && !userNickname.isBlank()) {
      // URL 디코딩
      String decodedNickname = URLDecoder.decode(userNickname, StandardCharsets.UTF_8);

      // 특수문자 이스케이프 처리
      String escapedNickname = escapeWildcard(decodedNickname);

      mustQueries.add(Query.of(q -> q
        .wildcard(w -> w
          .field("userNickname")
          .value("*" + escapedNickname + "*")
        )
      ));
    }

    // 상태 필터링
    if (status != null && !status.isBlank()) {
      mustQueries.add(Query.of(q -> q
        .term(t -> t
          .field("status.keyword")
          .value(status)
        )
      ));
    }
    boolQueryBuilder.must(mustQueries);

    // 날짜 필터 추가: startDate, endDate 기준 recruitmentDeadline 필터링
    final Long startMillis = (startDate != null && !startDate.isBlank()) ?
      LocalDate.parse(startDate)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli() : null;

    final Long endMillis = (endDate != null && !endDate.isBlank()) ?
      LocalDate.parse(endDate)
        .atTime(23, 59, 59)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli() : null;

    final Long eternalDeadline = LocalDate.of(9999, 12, 31)
      .atStartOfDay(ZoneId.systemDefault())
      .toInstant()
      .toEpochMilli();

    boolQueryBuilder.filter(fb -> fb.bool(b -> {
      b.should(s -> s.range(r -> {
        r.field("recruitmentDeadline");
        if (startMillis != null) r.gte(JsonData.of(startMillis));
        if (endMillis != null) r.lte(JsonData.of(endMillis));
        return r;
      }));
      b.should(s -> s.term(t -> t.field("recruitmentDeadline").value(eternalDeadline)));
      b.minimumShouldMatch("1");
      return b;
    }));

    Query finalQuery = Query.of(q -> q.bool(boolQueryBuilder.build()));

    // 정렬 설정
    List<co.elastic.clients.elasticsearch._types.SortOptions> sortOptions = new java.util.ArrayList<>();

    switch (sortOption) {
      case "apply_latest":
        sortOptions.add(co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
          .field(f -> f.field("appliedAt").order(co.elastic.clients.elasticsearch._types.SortOrder.Desc))));
        break;
      case "apply_oldest":
        sortOptions.add(co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
          .field(f -> f.field("appliedAt").order(co.elastic.clients.elasticsearch._types.SortOrder.Asc))));
        break;
      case "deadline_latest":
        sortOptions.add(co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
          .field(f -> f.field("recruitmentDeadline").order(co.elastic.clients.elasticsearch._types.SortOrder.Asc))));
        break;
      case "deadline_oldest":
        sortOptions.add(co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
          .field(f -> f.field("recruitmentDeadline").order(co.elastic.clients.elasticsearch._types.SortOrder.Desc))));
        break;
      default:
        sortOptions.add(co.elastic.clients.elasticsearch._types.SortOptions.of(s -> s
          .field(f -> f.field("appliedAt").order(co.elastic.clients.elasticsearch._types.SortOrder.Desc))));
        break;
    }

    // 검색 요청 빌드
    SearchRequest searchRequest = SearchRequest.of(sr -> sr
      .index("apply_histories")
      .query(finalQuery)
      .from(page * size)
      .size(size)
      .sort(sortOptions)
    );

    SearchResponse<ApplyHistoryDocument> searchResponse =
      elasticsearchClient.search(searchRequest, ApplyHistoryDocument.class);

    List<ApplyHistoryDocument> resultList = searchResponse.hits().hits().stream()
      .map(Hit::source)
      .collect(Collectors.toList());

    Pageable pageable = PageRequest.of(page, size);
    return new PageImpl<>(resultList, pageable, searchResponse.hits().total().value());
  }
}
