package com.br.marketing.service.didi.impl;

import com.br.marketing.service.didi.DiDiCollidingDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DiDiCollidingDataServiceImpl implements DiDiCollidingDataService {

    @Override
    public void allow(JobExecutionMultipleShardingContext context) {

    }
}
