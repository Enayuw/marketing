package com.br.marketing.monkey.job.wuba;

import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.WubaCollidingDataFrontMapper;
import com.br.marketing.service.Impl.wuba.WuBaCollidingDataSynchronismService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 58重置高价值文件同步状态作业
 * @Author chenh
 * @Date 2024-10-09
 */
@Component
@Slf4j
public class WuBaCollidingDataHighValueResetJob extends AbstractSimpleElasticJob {
    @Resource
    LocalFileMapper localFileMapper;
    @Resource
    WubaCollidingDataFrontMapper wubaCollidingDataFrontMapper;
    @Resource
    WuBaCollidingDataSynchronismService wuBaCollidingDataSynchronismService;
    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long start = System.currentTimeMillis();
        marketingCommonConfig.getWubaCollidingApiCodes().forEach((String apiCode) -> {
            List<Long> highValueIdList = wuBaCollidingDataSynchronismService.getHighValueFileIds(apiCode);
            if (Objects.isNull(highValueIdList)) {
                return;
            }
            String highValueFileIds = "(" + Joiner.on(",").join(highValueIdList) + ")";
            int updateCount;
            while (true) {
                updateCount = wubaCollidingDataFrontMapper.updatePushStatusByHighValueFileIds(highValueFileIds);
                if (updateCount == 0) {
                    break;
                }
                log.warn("58重置高价值文件同步状态作业,更新量级：{}", updateCount);
            }
            localFileMapper.updatePushStatusByLocalId(highValueFileIds);
        });

        log.warn("58重置高价值文件同步状态作业，运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}
