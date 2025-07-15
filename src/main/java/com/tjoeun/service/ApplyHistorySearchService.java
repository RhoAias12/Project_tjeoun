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

//  private String escapeWildcard(String input) {
//    // 이스케이프할 특수문자 목록을 정규식으로 정의
//    return input.replaceAll("([~`!@#$%^&*()_\\-+=\\[\\]{};:\"',.<>?/])", "\\\\$1");
//  }

  private String escapeWildcard(String input) {
    // 이스케이프할 특수문자 목록에 \와 | 추가
    return input.replaceAll("([~`!@#$%^&*()_\\-+=\\[\\]{};:\"',.<>?/\\\\|])", "\\\\$1");
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
      String escapedTitle = escapeWildcard(recruitmentTitle.toLowerCase());
      mustQueries.add(Query.of(q -> q
        .wildcard(w -> w
          .field("recruitmentTitle.keyword")
          .value("*" + escapedTitle + "*")
        )
      ));
    }

    // 회사명 필터링
    if (recruitmentCompany != null && !recruitmentCompany.isBlank()) {
      String escapedCompany = escapeWildcard(recruitmentCompany.toLowerCase());
      mustQueries.add(Query.of(q -> q
        .wildcard(w -> w
          .field("recruitmentCompany.keyword")
          .value("*" + escapedCompany + "*")
        )
      ));
    }

    // 이메일 필터링
    if (userEmail != null && !userEmail.isBlank()) {
      String escapedEmail = escapeWildcard(userEmail.toLowerCase());
      mustQueries.add(Query.of(q -> q
        .wildcard(w -> w
          .field("userEmail.keyword")
          .value("*" + escapedEmail + "*")
        )
      ));
    }

    // 사용자 닉네임 필터링
    if (userNickname != null && !userNickname.isBlank()) {
      String escapedNickname = escapeWildcard(userNickname.toLowerCase());
      mustQueries.add(Query.of(q -> q
        .wildcard(w -> w
          .field("userNickname.keyword")
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

  public void save(ApplyHistory applyHistory) throws IOException {
    // 기존 ApplyHistoryDocument 가져오기 (ES에서) - 선택사항, 없으면 null 처리
    Optional<ApplyHistoryDocument> existingDocOpt = applyHistorySearchRepository.findById(applyHistory.getOptionalIdx());

    // 기존 문서에서 사용자, 이력서 등 기타 필드 가져오기
    ApplyHistoryDocument existingDoc = existingDocOpt.orElse(null);

    ApplyHistoryDocument updatedDoc = ApplyHistoryDocument.builder()
      .applyHistoryId(applyHistory.getOptionalIdx())

      // 공고 관련 필드: Recruitment 정보 최신값으로 바꿔서 넣기
      .recruitmentTitle(applyHistory.getRecruitment().getTitle())
      .recruitmentCompany(applyHistory.getRecruitment().getCompany())
      .recruitmentDeadline(applyHistory.getRecruitment().getDeadline() != null
        ? applyHistory.getRecruitment().getDeadline().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        : null)
      .recruitmentLocation(applyHistory.getRecruitment().getLocation())
      .recruitmentLogoUrl(applyHistory.getRecruitment().getLogoUrl())

      // 나머지 필드: 기존 문서가 있으면 기존 값을 그대로 쓰거나,
      // 없으면 엔티티에서 가져오기 (적절히 선택)
      .status(existingDoc != null ? existingDoc.getStatus() : applyHistory.getStatus().name())
      .statusDisplay(existingDoc != null ? existingDoc.getStatusDisplay() : getStatusDisplayName(applyHistory.getStatus().name()))

      .userId(existingDoc != null ? existingDoc.getUserId() : applyHistory.getUser().getUserIdx())
      .userEmail(existingDoc != null ? existingDoc.getUserEmail() : applyHistory.getUser().getUserEmail())
      .userNickname(existingDoc != null ? existingDoc.getUserNickname() : applyHistory.getUser().getUserNickname())
      .userName(existingDoc != null ? existingDoc.getUserName() : applyHistory.getUser().getUserName())
      .userBirth(existingDoc != null ? existingDoc.getUserBirth()
        : applyHistory.getUser().getUserBirth() != null
        ? applyHistory.getUser().getUserBirth().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        : null)

      .applyHistoryResumeId(existingDoc != null ? existingDoc.getApplyHistoryResumeId()
        : applyHistory.getApplyHistoryResume() != null ? applyHistory.getApplyHistoryResume().getApplyHistoryResumeId() : null)
      .resumeTitle(existingDoc != null ? existingDoc.getResumeTitle()
        : applyHistory.getApplyHistoryResume() != null ? applyHistory.getApplyHistoryResume().getTitle() : null)
      .resumeImg(existingDoc != null ? existingDoc.getResumeImg()
        : applyHistory.getApplyHistoryResume() != null ? applyHistory.getApplyHistoryResume().getImg() : null)
      .resumeAddress(existingDoc != null ? existingDoc.getResumeAddress()
        : applyHistory.getApplyHistoryResume() != null ? applyHistory.getApplyHistoryResume().getAddress() : null)
      .resumePhoneNum(existingDoc != null ? existingDoc.getResumePhoneNum()
        : applyHistory.getApplyHistoryResume() != null ? applyHistory.getApplyHistoryResume().getPhoneNum() : null)
      .resumeEducation(existingDoc != null ? existingDoc.getResumeEducation()
        : applyHistory.getApplyHistoryResume() != null ? applyHistory.getApplyHistoryResume().getEducation() : null)
      .resumeAbility(existingDoc != null ? existingDoc.getResumeAbility()
        : applyHistory.getApplyHistoryResume() != null ? applyHistory.getApplyHistoryResume().getAbility() : null)
      .resumeAntecedents(existingDoc != null ? existingDoc.getResumeAntecedents()
        : applyHistory.getApplyHistoryResume() != null ? applyHistory.getApplyHistoryResume().getAntecedents() : null)
      .resumeAwards(existingDoc != null ? existingDoc.getResumeAwards()
        : applyHistory.getApplyHistoryResume() != null ? applyHistory.getApplyHistoryResume().getAwards() : null)
      .resumeContext(existingDoc != null ? existingDoc.getResumeContext()
        : applyHistory.getApplyHistoryResume() != null ? applyHistory.getApplyHistoryResume().getContext() : null)

      .appliedAt(existingDoc != null ? existingDoc.getAppliedAt()
        : applyHistory.getApplyHistoryResume() != null ? applyHistory.getApplyHistoryResume().getApplyHistoryResumeId() : null
      )

      .build();

    applyHistorySearchRepository.save(updatedDoc);
  }

}
