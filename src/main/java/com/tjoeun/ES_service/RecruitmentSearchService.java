package com.tjoeun.ES_service;


import com.tjoeun.ES_repository.RecruitmentElasticRepository;
import com.tjoeun.document.RecruitmentElastic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruitmentSearchService {

    private final RecruitmentElasticRepository elasticRepo;

    public List<RecruitmentElastic> search(String keyword) {
        return elasticRepo.findByTitleContainingOrDescriptionContaining(keyword, keyword);
    }
}
