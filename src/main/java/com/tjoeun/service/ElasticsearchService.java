package com.tjoeun.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.ExtendedBounds;
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

import java.io.IOException;
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
            boolean exists = elasticsearchClient.indices()
                    .exists(b -> b.index(indexName))
                    .value();

            if (exists) {
                System.out.println("[Elasticsearch] recruitments 인덱스 이미 존재");
                return;
            }

            // 인덱스 생성
            elasticsearchClient.indices().create(c -> c
                    .index(indexName)
                    .settings(s -> s
                            .analysis(a -> a
                                    .analyzer("my_nori", an -> an
                                            .custom(ca -> ca.tokenizer("nori_tokenizer"))
                                    )
                            )
                    )
                    .mappings(mb -> mb
                            .properties("combinedContent", p -> p.text(t -> t))
                            .properties("jobKeywords", p -> p.text(t -> t
                                    .analyzer("my_nori")
                                    .fielddata(true)
                            ))
                            .properties("_class", p -> p.keyword(k -> k.index(false).docValues(false)))
                            .properties("deadline", p -> p.date(d -> d.format("yyyy-MM-dd")))
                            .properties("createdAt", p -> p.date(d -> d.format("yyyy-MM-dd")))
                            .properties("location", p -> p.keyword(k -> k))
                            .properties("title", p -> p.keyword(k -> k))
                    )
            );

            System.out.println("[Elasticsearch] recruitments 인덱스 생성 완료");

        } catch (IOException e) {
            System.err.println("[Elasticsearch] recruitments 인덱스 생성 중 오류 발생");
            e.printStackTrace();
        }
    }

    // 총 공고 수
    public long countRecruitments() {
        try {
            CountRequest request = CountRequest.of(c ->
                    c.index("recruitments")
                            .query(q -> q.matchAll(m -> m))
            );
            CountResponse response = elasticsearchClient.count(request);
            return response.count();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }


    // 마감 임박 공고 수 (7일 내)
    public long countClosingSoonRecruitments() {
        try {
            String today = LocalDate.now().toString();
            String soon = LocalDate.now().plusDays(7).toString();
            CountRequest request = CountRequest.of(c ->
                    c.index("recruitments")
                            .query(q -> q.range(r -> r
                                    .field("deadline")
                                    .gte(JsonData.of(today))
                                    .lte(JsonData.of(soon))
                            ))
            );
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

    public Map<String, Long> getTopKeywords(int size) {
        try {
            SearchResponse<Void> response = elasticsearchClient.search(s -> s
                    .index("recruitments")
                    .size(0)
                    .aggregations("top_keywords", a -> a
                            .terms(t -> t.field("title.keyword").size(size))
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

