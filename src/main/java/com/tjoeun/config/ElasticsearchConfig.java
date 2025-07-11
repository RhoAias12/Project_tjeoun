package com.tjoeun.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.tjoeun.elasticsearch.repository")
public class ElasticsearchConfig {

  @Bean
  public ElasticsearchClient elasticsearchClient() {
    RestClient restClient = RestClient.builder(
      new HttpHost("localhost", 9200)).build();

    ElasticsearchTransport transport = new RestClientTransport(
      restClient, new JacksonJsonpMapper());

    return new ElasticsearchClient(transport);
  }

  @Bean(name = "elasticsearchTemplate")
  public ElasticsearchOperations elasticsearchOperations(ElasticsearchClient client) {
    return new ElasticsearchTemplate(client);
  }
}