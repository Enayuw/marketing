package com.br.marketing.check.job;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.IPPDTransferService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author lizhen
 * @Date 2022/4/24 15:06
 * @Description: 拍拍贷转化数据推电销
 **/
@Component
@Slf4j
public class PPDTransferToDxJob extends AbstractSimpleElasticJob {

    @Autowired
    IPPDTransferService ippdTransferService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String jobParameter = context.getJobParameter();
        Result result;
        if (StringUtils.isNotBlank(jobParameter)) {
            result = ippdTransferService.actionPPDToDx(jobParameter);
        } else {
            result = ippdTransferService.actionPPDToDx(null);
        }
        log.warn("拍拍贷转化数据推电销 result={}", JSON.toJSONString(result));

    }
}
