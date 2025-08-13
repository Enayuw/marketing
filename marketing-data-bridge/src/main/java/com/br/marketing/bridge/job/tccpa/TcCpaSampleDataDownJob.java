package com.br.marketing.bridge.job.tccpa;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.tccpa.TcCpaSampleDataDownService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description 同城易融正负样本数据下载
 * @Author hong.chen
 * @CreateTime 2025/05/23
 */
@Slf4j
@Component
public class TcCpaSampleDataDownJob extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcCpaSampleDataDownService tcCpaSampleDataDownService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        long start = System.currentTimeMillis();
        try {
            String testApiCode = shardingContext.getJobParameter();
            String apiCode = StringUtils.isNotBlank(testApiCode) ? testApiCode : marketingCommonConfig.getTcyrCpaApiCode();
            tcCpaSampleDataDownService.process(apiCode);
        } catch (Exception e) {
            String title = "同程易融CPA，正负样本数据下载异常";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), e.getMessage()
                    , title));
        }
        log.warn("同程易融CPA，正负样本数据下载，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}
