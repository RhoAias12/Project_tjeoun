package com.tjoeun.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ESIndexService {

  private final ElasticsearchClient esClient;
  private static final String INDEX_NAME = "recruitments";

  /**
   * 서버 시작 시 인덱스를 존재 여부 확인 후, 없으면 생성
   */
  @PostConstruct
  public void init() throws IOException {
    if (!indexExists(INDEX_NAME)) {
      createRecruitmentsIndex();
    }
  }

  /**
   * 인덱스 존재 여부 확인
   */
  public boolean indexExists(String indexName) throws IOException {
    return esClient.indices().exists(e -> e.index(indexName)).value();
  }

  public void createRecruitmentsIndex() throws IOException {
    // analyzer 설정
    Map<String, Object> analysis = new HashMap<>();

    Map<String, Object> tokenizerMap = new HashMap<>();
    Map<String, Object> ngramTokenizer = new HashMap<>();
    ngramTokenizer.put("type", "ngram");
    ngramTokenizer.put("min_gram", 2);
    ngramTokenizer.put("max_gram", 32);
    ngramTokenizer.put("token_chars", List.of("letter", "digit"));
    tokenizerMap.put("ngram_tokenizer", ngramTokenizer);

    Map<String, Object> analyzerMap = new HashMap<>();
    Map<String, Object> ngramAnalyzer = new HashMap<>();
    ngramAnalyzer.put("type", "custom");
    ngramAnalyzer.put("tokenizer", "ngram_tokenizer");
    ngramAnalyzer.put("filter", List.of("lowercase"));
    analyzerMap.put("ngram_analyzer", ngramAnalyzer);

    analysis.put("analyzer", analyzerMap);
    analysis.put("tokenizer", tokenizerMap);

    Map<String, Object> indexSettings = new HashMap<>();
    indexSettings.put("max_ngram_diff", 32);

    Map<String, Object> settings = new HashMap<>();
    settings.put("index", indexSettings);
    settings.put("analysis", analysis);

    // 매핑 설정 (기존과 동일)
    Map<String, Object> mappings = new HashMap<>();
    Map<String, Object> properties = new HashMap<>();

    properties.put("title", Map.of(
      "type", "text",
      "analyzer", "ngram_analyzer",
      "search_analyzer", "standard",
      "fields", Map.of(
        "keyword", Map.of("type", "keyword")
      )
    ));
    // company, combinedContent, location도 동일하게 추가...

    properties.put("company", Map.of(
      "type", "text",
      "analyzer", "ngram_analyzer",
      "search_analyzer", "standard",
      "fields", Map.of(
        "keyword", Map.of("type", "keyword")
      )
    ));
    properties.put("combinedContent", Map.of(
      "type", "text",
      "analyzer", "ngram_analyzer",
      "search_analyzer", "standard",
      "fields", Map.of(
        "keyword", Map.of("type", "keyword")
      )
    ));
    properties.put("location", Map.of(
      "type", "text",
      "analyzer", "ngram_analyzer",
      "search_analyzer", "standard",
      "fields", Map.of(
        "keyword", Map.of("type", "keyword")
      )
    ));

    properties.put("deadline", Map.of("type", "date", "format", "strict_date_time||yyyy-MM-dd'T'HH:mm:ss"));
    properties.put("createdAt", Map.of("type", "date", "format", "strict_date_time||yyyy-MM-dd'T'HH:mm:ss"));
    properties.put("scrapCount", Map.of("type", "integer"));

    mappings.put("properties", properties);

    // 최종 JSON 구조에 settings 와 mappings를 넣음
    Map<String, Object> root = new HashMap<>();
    root.put("settings", settings);
    root.put("mappings", mappings);

    ObjectMapper mapper = new ObjectMapper();
    String indexDefinition = mapper.writeValueAsString(root);

    System.out.println("indexDefinition = " + indexDefinition);

    CreateIndexResponse response = esClient.indices().create(c -> c
      .index(INDEX_NAME)
      .withJson(new ByteArrayInputStream(indexDefinition.getBytes(StandardCharsets.UTF_8)))
    );

    if (response.acknowledged()) {
      System.out.println("✅ 인덱스 생성 완료: " + INDEX_NAME);
    } else {
      throw new IOException("❌ 인덱스 생성 실패");
    }
  }




  /**
   * 인덱스 삭제 (개발 중 테스트용)
   */
  public void deleteIndex(String indexName) throws IOException {
    esClient.indices().delete(DeleteIndexRequest.of(d -> d.index(indexName)));
    System.out.println("🗑 인덱스 삭제 완료: " + indexName);
  }
}
