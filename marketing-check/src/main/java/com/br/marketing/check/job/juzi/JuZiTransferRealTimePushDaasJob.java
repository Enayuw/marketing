package com.br.marketing.check.job.juzi;

import com.alibaba.fastjson.JSON;
import com.br.marketing.check.service.JuZiRealTimePushDassService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.StringUtils;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 桔子实时自动化转Daas-3710037（营销→Daas）
 *
 * @author lizhen
 * @dateTime 2022/10/19 14:18
 */
@Component
@Slf4j
public class JuZiTransferRealTimePushDaasJob extends AbstractSimpleElasticJob {

    @Resource
    private JuZiRealTimePushDassService juZiRealTimePushDassService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        String jobParameter = context.getJobParameter();
        Result result;
        if (StringUtils.isNotBlank(jobParameter)) {
            result = juZiRealTimePushDassService.actionRealTimeDataToDx(jobParameter);
        } else {
            result = juZiRealTimePushDassService.actionRealTimeDataToDx(null);
        }
        if (!Objects.isNull(result.getData())) {
            log.warn("桔子实时自动化数据推电销完成 result={}", JSON.toJSONString(result));
        }

    }
}
