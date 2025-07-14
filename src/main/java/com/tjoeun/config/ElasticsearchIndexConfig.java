package com.tjoeun.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.context.annotation.Configuration;

import java.io.StringReader;

@Configuration
public class ElasticsearchIndexConfig {

    private final ElasticsearchClient elasticsearchClient;

    public ElasticsearchIndexConfig(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
        createRecruitmentIndexIfMissing(); // Bean 등록 전에 실행됨
    }

    private void createRecruitmentIndexIfMissing() {
        try {
            boolean exists = elasticsearchClient.indices().exists(e -> e.index("recruitments")).value();
            if (!exists) {
                elasticsearchClient.indices().create(c -> c
                        .index("recruitments")
                        .settings(s -> s.withJson(new StringReader("""
                            {
                              "analysis": {
                                "analyzer": {
                                  "korean_custom": {
                                    "type": "custom",
                                    "tokenizer": "nori_tokenizer",
                                    "filter": ["lowercase", "nori_readingform", "my_stop"]
                                  }
                                },
                                "filter": {
                                  "my_stop": {
                                    "type": "stop",
                                    "stopwords": ["이", "자", "직", "주요", "사", "관리", "동"]
                                  }
                                }
                              }
                            }
                        """)))
                        .mappings(mb -> mb
                                .properties("jobKeywords", p -> p.text(t -> t.analyzer("korean_custom").fielddata(true)))
                                .properties("title", p -> p.text(t -> t.analyzer("korean_custom")))
                                .properties("combinedContent", p -> p.text(t -> t.analyzer("korean_custom")))
                                .properties("company", p -> p.keyword(k -> k))
                                .properties("deadline", p -> p.date(d -> d.format("yyyy-MM-dd")))
                                .properties("createdAt", p -> p.date(d -> d.format("yyyy-MM-dd")))
                                .properties("location", p -> p.keyword(k -> k))
                                .properties("_class", p -> p.keyword(k -> k.index(false).docValues(false)))
                        )
                );
                System.out.println("✅ recruitments 인덱스 자동 생성 완료");
            }
        } catch (Exception e) {
            System.err.println("❌ recruitments 인덱스 자동 생성 실패");
            e.printStackTrace();
        }
    }
}

