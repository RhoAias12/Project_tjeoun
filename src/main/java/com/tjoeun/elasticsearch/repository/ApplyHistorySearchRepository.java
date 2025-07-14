package com.tjoeun.elasticsearch.repository;

import com.tjoeun.elasticsearch.document.ApplyHistoryDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplyHistorySearchRepository extends ElasticsearchRepository<ApplyHistoryDocument, Integer> {

}
