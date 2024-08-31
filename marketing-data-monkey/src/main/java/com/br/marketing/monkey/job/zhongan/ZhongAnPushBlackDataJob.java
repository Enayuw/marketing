package com.br.marketing.monkey.job.zhongan;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.monkeydata.service.ZhongAnPushBlackDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author lizhen
 * @Description 众安信贷自动化推送黑名单(营销 → 外呼)
 * @Date 2022/11/15 10:02
 */
@Component
@Slf4j
public class ZhongAnPushBlackDataJob extends AbstractSimpleElasticJob {

    @Autowired
    private ZhongAnPushBlackDataService zhongAnPushBlackDataService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        String jobParameter = context.getJobParameter();
        Result result;
        if (StringUtils.isNotBlank(jobParameter)) {
            result = zhongAnPushBlackDataService.actionPushBlackData(jobParameter);
        } else {
            result = zhongAnPushBlackDataService.actionPushBlackData(null);
        }
        if (!Objects.isNull(result.getData())) {
            log.warn("众安推送黑名单至客服 result={}", JSON.toJSONString(result));
        }

    }
}
