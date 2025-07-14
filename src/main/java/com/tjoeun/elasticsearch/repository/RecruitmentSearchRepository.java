package com.tjoeun.elasticsearch.repository;

import com.tjoeun.elasticsearch.document.RecruitmentDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruitmentSearchRepository  extends ElasticsearchRepository<RecruitmentDocument, Long> {

}
