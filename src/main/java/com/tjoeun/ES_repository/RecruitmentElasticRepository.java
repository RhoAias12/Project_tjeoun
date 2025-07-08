package com.tjoeun.ES_repository;

import com.tjoeun.document.RecruitmentElastic;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface RecruitmentElasticRepository extends ElasticsearchRepository<RecruitmentElastic, Long> {

    List<RecruitmentElastic> findByTitleContainingOrDescriptionContaining(String title, String description);
}

