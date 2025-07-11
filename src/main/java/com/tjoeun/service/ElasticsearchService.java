package com.tjoeun.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.ExtendedBounds;
import co.elastic.clients.elasticsearch._types.analysis.TokenFilterDefinition;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import co.elastic.clients.util.NamedValue;
import com.tjoeun.document.RecruitmentSearchDocument;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.elasticsearch._types.analysis.StopTokenFilter;
import co.elastic.clients.elasticsearch._types.analysis.TokenFilterDefinition;
import co.elastic.clients.elasticsearch._types.analysis.StopTokenFilter;


import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
public class ElasticsearchService {

    private final ElasticsearchClient elasticsearchClient;

    public void createIndexIfNotExists() {
        String indexName = "recruitments";

        try {
            // 🔥 기존 인덱스 삭제
            boolean exists = elasticsearchClient.indices()
                    .exists(e -> e.index(indexName)).value();

            if (exists) {
                elasticsearchClient.indices().delete(d -> d.index(indexName));
                System.out.println("[Elasticsearch] 기존 인덱스 삭제 완료");
            }

            // ✅ 새 인덱스 생성 (커스텀 분석기 포함)
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
                    "stopwords": ["이", "자", "직", "주요", "사"]
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

            System.out.println("[Elasticsearch] recruitments 인덱스 새로 생성 완료");

        } catch (IOException e) {
            System.err.println("[Elasticsearch] recruitments 인덱스 생성 실패");
            e.printStackTrace();
        }
    }

    public void resetRecruitmentIndex(RecruitmentSyncService recruitmentSyncService) {
        try {
            // 1. 기존 인덱스 삭제
            elasticsearchClient.indices().delete(d -> d.index("recruitments"));
            System.out.println("🧹 기존 인덱스 삭제 완료");

            // 2. 인덱스 재생성
            createIndexIfNotExists();

            // 3. 전체 데이터 재동기화 (DB → Elasticsearch)
            recruitmentSyncService.syncAllToElasticsearch();

            System.out.println("✅ 인덱스 재설정 및 전체 동기화 완료");

        } catch (IOException e) {
            System.err.println("❌ 인덱스 재설정 실패");
            e.printStackTrace();
        }
    }


    // 전체 채용 공고 수
    public long countRecruitments() {
        try {
            CountRequest request = CountRequest.of(c -> c.index("recruitments"));
            CountResponse response = elasticsearchClient.count(request);
            return response.count();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }


    // 인기 직무 TOP N
    public Map<String, Long> getTopRoles(int size) {
        try {
            SearchResponse<Void> response = elasticsearchClient.search(s -> s
                    .index("recruitments")
                    .size(0)
                    .aggregations("top_roles", a -> a
                            .terms(t -> t
                                    .field("jobKeywords")
                                    .size(size)
                            )

                    ), Void.class);

            return response.aggregations().get("top_roles").sterms().buckets().array().stream()
                    .collect(Collectors.toMap(
                            b -> b.key().stringValue(),
                            b -> b.docCount()
                    ));
        } catch (IOException e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    // 마감 임박 공고 수
    public long countClosingSoonRecruitments() {
        try {
            String today = LocalDate.now().toString();
            String soon = LocalDate.now().plusDays(7).toString();  // 3일 이내 마감

            System.out.println(" 오늘 날짜: " + today);
            System.out.println(" 마감 기준 날짜: " + soon);

            CountRequest request = CountRequest.of(c ->
                    c.index("recruitments")
                            .query(q -> q.range(r -> r
                                    .field("deadline")
                                    .gte(JsonData.of(today))
                                    .lte(JsonData.of(soon))
                            ))
            );

            long count = elasticsearchClient.count(request).count();
            System.out.println(" 마감 임박 공고 수: " + count);

            return count;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // top keyword
    public Map<String, Long> getTopKeywords(int size) {
        try {
            SearchResponse<Void> response = elasticsearchClient.search(s -> s
                    .index("recruitments")
                    .size(0)
                    .aggregations("top_keywords", a -> a
                            .terms(t -> t.field("jobKeywords").size(size))
                    ), Void.class);

            return response.aggregations().get("top_keywords").sterms().buckets().array().stream()
                    .collect(Collectors.toMap(
                            b -> b.key().stringValue(),
                            b -> b.docCount()
                    ));
        } catch (IOException e) {
            e.printStackTrace();
            return Map.of();
        }
    }


    // 지역 분포
    public Map<String, Long> getRegionDistribution() {
        try {
            SearchResponse<Void> response = elasticsearchClient.search(s -> s
                    .index("recruitments")
                    .size(0)
                    .aggregations("region_dist", a -> a
                            .terms(t -> t.field("location.keyword").size(10))
                    ), Void.class);

            return response.aggregations().get("region_dist").sterms().buckets().array().stream()
                    .collect(Collectors.toMap(
                            b -> b.key().stringValue(),
                            b -> b.docCount()
                    ));
        } catch (IOException e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    // 일별 등록 추이(최근 7일 이내)
    public Map<String, Long> getDailyTrends() {
        try {
            String minDate = LocalDate.now().minusDays(6).toString(); // 최근 7일 전
            String maxDate = LocalDate.now().toString();              // 오늘

            SearchResponse<Void> response = elasticsearchClient.search(s -> s
                    .index("recruitments")
                    .size(0)
                    .aggregations("daily", a -> a.dateHistogram(dh -> dh
                            .field("createdAt")
                            .calendarInterval(CalendarInterval.Day)
                            .format("yyyy-MM-dd")
                            .minDocCount(0)
                            .extendedBounds(new ExtendedBounds.Builder()
                                    .min(minDate)
                                    .max(maxDate)
                                    .build())
                            .order(List.of(NamedValue.of("_key", SortOrder.Asc)))
                    )), Void.class);

            return response.aggregations()
                    .get("daily").dateHistogram().buckets().array().stream()
                    .collect(Collectors.toMap(
                            b -> b.keyAsString(),
                            b -> b.docCount(),
                            (v1, v2) -> v1,
                            LinkedHashMap::new
                    ));
        } catch (IOException e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    public void saveToES(RecruitmentSearchDocument doc) {
        try {
            elasticsearchClient.index(i -> i
                    .index("recruitments")
                    .document(doc)
            );
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch 저장 실패", e);
        }
    }

    public long countClosedRecruitments() {
        try {
            String today = LocalDate.now().toString();
            CountRequest request = CountRequest.of(c ->
                    c.index("recruitments")
                            .query(q -> q.range(r -> r
                                    .field("deadline")
                                    .lt(JsonData.of(today))
                            ))
            );
            CountResponse response = elasticsearchClient.count(request);
            return response.count();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }



    public Map<String, Long> getTopTitles(int size) {
        try {
            SearchResponse<Void> response = elasticsearchClient.search(s -> s
                    .index("recruitments")
                    .size(0)
                    .aggregations("top_titles", a -> a
                            .terms(t -> t.field("title.keyword").size(size))
                    ), Void.class);

            return response.aggregations().get("top_titles").sterms().buckets().array().stream()
                    .collect(Collectors.toMap(
                            b -> b.key().stringValue(),
                            b -> b.docCount(),
                            (a, b) -> a, // merge function
                            LinkedHashMap::new
                    ));
        } catch (IOException e) {
            e.printStackTrace();
            return Map.of();
        }
    }




}

