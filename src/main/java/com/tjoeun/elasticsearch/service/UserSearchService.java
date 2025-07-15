package com.tjoeun.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.tjoeun.dto.UserListDto;
import com.tjoeun.elasticsearch.document.UserDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSearchService {

  private final ElasticsearchClient esClient;
  private final String indexName = "users";

  // escape 메서드 추가
  private String escapeWildcard(String input) {
    return input.replaceAll("([\\*\\?])", "\\\\$1"); // *와 ?를 \* 와 \? 로 escape
  }

  public Page<UserListDto> searchUsers(
    String email, String nickname, String sortBy, int page, int size
  ) throws IOException {

    int from = (page - 1) * size;

    // 필터 조건 설정
    List<Query> mustQueries = new ArrayList<>();

    if (email != null && !email.isBlank()) {
      mustQueries.add(Query.of(q -> q
        .wildcard(w -> w
          .field("userEmail")
          .wildcard("*" + email + "*")
        )
      ));
    }
    if (nickname != null && !nickname.isBlank()) {
      String escapedNickname = escapeWildcard(nickname);
      mustQueries.add(Query.of(q -> q
              .wildcard(w -> w
                      .field("userNickname")
                      .wildcard("*" + escapedNickname + "*")
              )
      ));
    }

    // 정렬 조건
    SortOptions sort = switch (sortBy) {
      case "email" -> SortOptions.of(s -> s.field(f -> f.field("userEmail").order(SortOrder.Asc)));
      case "emailDesc" -> SortOptions.of(s -> s.field(f -> f.field("userEmail").order(SortOrder.Desc)));
      case "nickname" -> SortOptions.of(s -> s.field(f -> f.field("userNickname").order(SortOrder.Asc)));
      case "nicknameDesc" -> SortOptions.of(s -> s.field(f -> f.field("userNickname").order(SortOrder.Desc)));
      case "oldest" -> SortOptions.of(s -> s.field(f -> f.field("userIdx").order(SortOrder.Asc)));
      default -> SortOptions.of(s -> s.field(f -> f.field("userIdx").order(SortOrder.Desc)));
    };

    // 검색 요청
    SearchResponse<UserDocument> response = esClient.search(s -> s
        .index(indexName)
        .query(q -> q
          .bool(b -> b.must(mustQueries))
        )
        .sort(sort)
        .from(from)
        .size(size),
      UserDocument.class
    );

    List<UserListDto> content = response.hits().hits().stream()
      .map(Hit::source)
      .map(doc -> UserListDto.builder()
        .userIdx(doc.getUserIdx())
        .userName(doc.getUserName())
        .userEmail(doc.getUserEmail())
        .userNickname(doc.getUserNickname())
        .userBirth(Instant.ofEpochMilli(doc.getUserBirth())
          .atZone(ZoneId.systemDefault())
          .toLocalDate())
        .build())
      .toList();

    return new PageImpl<>(content, PageRequest.of(page - 1, size), response.hits().total().value());
  }
}
