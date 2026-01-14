package com.br.marketing.monkey.job.syj;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.monkey.service.syj.SuiYiJiService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName SuiyijiAdmissionJob
 * @Author hang.zhou
 * @Date 2025/12/1
 */
@Component
@Slf4j
public class SuiYiJiOriginalJob extends AbstractSimpleElasticJob {

    @Resource
    private SuiYiJiService suiYiJiService;


    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        log.warn("随忆记用户信息撞库job开始执行");
        Long start = System.currentTimeMillis();
        String apiCode;
        String jobParameter = shardingContext.getJobParameter();
        if (StringUtils.isNotBlank(jobParameter)) {
            apiCode = jobParameter;
        } else {
            apiCode = "3710222";
        }

        suiYiJiService.originalToUpload(apiCode);
        Long end = System.currentTimeMillis();
        log.warn("随忆记用户信息撞库job执行结束，耗时:{}", end - start);
    }
}
