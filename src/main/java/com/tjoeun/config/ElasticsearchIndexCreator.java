package com.tjoeun.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.json.JsonData;

import java.io.IOException;
import java.util.Map;

public class ElasticsearchIndexCreator {

    private final ElasticsearchClient client;

    public ElasticsearchIndexCreator(ElasticsearchClient client) {
        this.client = client;
    }

    public void createIndexWithKoreanAnalyzer(String indexName) throws IOException {
        CreateIndexResponse response = client.indices().create(c -> c
                .index(indexName)
                .settings(s -> s
                        .analysis(a -> a
                                .analyzer("korean_custom", analyzer -> analyzer
                                        .custom(custom -> custom
                                                .tokenizer("nori_tokenizer")
                                                .filter("lowercase", "nori_readingform", "nori_part_of_speech")
                                        )
                                )
                        )
                )
                .mappings(TypeMapping.of(m -> m
                        .properties("title", p -> p
                                .text(t -> t
                                        .analyzer("korean_custom")
                                )
                        )
                ))
        );

        System.out.println("Index created: " + response.acknowledged());
    }
}

