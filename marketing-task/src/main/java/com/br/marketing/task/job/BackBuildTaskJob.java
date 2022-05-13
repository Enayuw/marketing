package com.br.marketing.task.job;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.customizedassert.AssertResult;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.ScoreRuleConfigDTO;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.service.ScoreRuleConfigService;
import com.br.marketing.task.service.ITaskService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BackBuildTaskJob extends AbstractSimpleElasticJob {

    @Autowired
    ITaskService iTaskService;

    @Autowired
    ScoreRuleConfigService scoreRuleConfigService;

    /**
     * 后台生成手动规则以及任务
     * 数据结构如下
     * {
     *     "ruleName": "wjm后台手动规则",
     *     "apiCode": "7410437",
     *     "taskTime": "2022-05-12",
     *     "conditionInfo": "[{\"logicalOperation\":\"and\",\"operationFactor\":[{\"fieldName\":\"appletDate\",\"fieldValue\":\"2022-04-28\",\"operation\":\"=\"},{\"fieldName\":\"appletTime\",\"fieldValue\":\"2022-04-28 10:15:03\",\"operation\":\"<\"},{\"fieldName\":\"userType\",\"fieldValue\":\"S02\",\"operation\":\"=\"}]}]"
     * }
     * @param jobExecutionMultipleShardingContext
     */
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        String jobParameter = jobExecutionMultipleShardingContext.getJobParameter();
        ScoreRuleConfigDTO scoreRuleConfigDTO = JSON.parseObject(jobParameter, ScoreRuleConfigDTO.class);
        Result<List<Long>> ruleRes = scoreRuleConfigService.saveFromCallBack(scoreRuleConfigDTO);
        AssertResult.assertResult(ruleRes);
        List<Long> data = ruleRes.getData();
        iTaskService.buildScoreTask(data);
    }
}
