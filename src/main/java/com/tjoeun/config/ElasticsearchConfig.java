package com.tjoeun.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

  @Bean
  public ElasticsearchClient elasticsearchClient() {
    RestClient restClient = RestClient.builder(
      new HttpHost("localhost", 9200)).build();

    // ObjectMapper에 JavaTimeModule 등록
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());

    // JacksonJsonpMapper에 위 ObjectMapper 전달
    ElasticsearchTransport transport = new RestClientTransport(
      restClient, new JacksonJsonpMapper(objectMapper));

    return new ElasticsearchClient(transport);
  }

  public static ElasticsearchClient createClient() {
    // 1. Jackson ObjectMapper 생성 및 JavaTimeModule 등록
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());

    // 2. JacksonJsonpMapper 생성 (위 objectMapper 사용)
    JacksonJsonpMapper mapper = new JacksonJsonpMapper(objectMapper);

    // 3. RestClient 생성 (Elasticsearch 서버 주소 지정)
    RestClient restClient = RestClient.builder(
      new HttpHost("localhost", 9200)
    ).build();

    // 4. RestClientTransport 생성
    RestClientTransport transport = new RestClientTransport(restClient, mapper);

    // 5. ElasticsearchClient 생성 및 반환
    return new ElasticsearchClient(transport);
  }
}