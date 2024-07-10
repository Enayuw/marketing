package com.br.marketing.service.Impl.wuba;

import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description WuBaCollidingDataSynchronismServiceImpl
 * @Author hong.chen
 * @CreateTime 2024/07/10
 */
@Service
@Slf4j
public class WuBaCollidingDataSynchronismServiceImpl implements WuBaCollidingDataSynchronismService{
    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Override
    public void process(JobExecutionMultipleShardingContext context) {

    }
}
