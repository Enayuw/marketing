package com.br.marketing.check.job;


import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.IYiXinTransferService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Author lizhen
 * @Date 2022/3/31 15:46
 * @Description: 宜信非实时转化数据推客服任务
 **/
@Component
@Slf4j
public class YiXinTransferToRobotAIJob extends AbstractSimpleElasticJob {

    @Autowired
    IYiXinTransferService iYiXinTransferService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String jobParameter = context.getJobParameter();
        if (StringUtils.isNotBlank(jobParameter)) {
            String apiCode, data = null;
            List<String> params = Splitter.on(",").splitToList(jobParameter);
            apiCode = params.get(0);
            if (params.size() > 1) {
                data = params.get(1);
            }
            iYiXinTransferService.actionYiXinToRobotAI(apiCode, data);
        } else {
            iYiXinTransferService.actionYiXinToRobotAI(null, null);
        }
    }
}
