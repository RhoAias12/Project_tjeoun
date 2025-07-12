package com.tjoeun.elasticsearch;

import com.tjoeun.service.ElasticsearchService;
import com.tjoeun.service.RecruitmentSyncService;
import com.tjoeun.service.UnifiedJobCrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ElasticsearchInitializer implements ApplicationRunner {

    private final ElasticsearchService elasticsearchService;
    private final UnifiedJobCrawlerService unifiedJobCrawlerService;
    private final RecruitmentSyncService recruitmentSyncService;

    @Override
    public void run(ApplicationArguments args) {


//        elasticsearchService.createIndexIfNotExists();

     //   unifiedJobCrawlerService.runCrawler();

        elasticsearchService.resetRecruitmentIndex(recruitmentSyncService);

    }
}
