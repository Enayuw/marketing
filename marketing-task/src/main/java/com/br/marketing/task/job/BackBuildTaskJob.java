package com.br.marketing.task.job;

import com.alibaba.fastjson.JSON;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.customizedassert.AssertResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.BackEndScoreRuleConfigDTO;
import com.br.marketing.service.IRuleConfigService;
import com.br.marketing.service.Impl.datagroup.DataGroupHandlerServiceImpl;
import com.br.marketing.service.MarketingTaskService;
import com.br.marketing.service.ScoreRuleConfigService;
import com.br.marketing.task.service.ITaskService;
import com.br.marketing.vo.CustomerScoreRuleVO;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class BackBuildTaskJob extends AbstractSimpleElasticJob {

    @Autowired
    ITaskService iTaskService;

    @Autowired
    ScoreRuleConfigService scoreRuleConfigService;

    @Autowired
    MarketingTaskService marketingTaskService;

    @Autowired
    IRuleConfigService iRuleConfigService;

    @Resource
    DataGroupHandlerServiceImpl dataGroupHandlerService;

    /**
     * 后台生成手动规则以及任务
     * 数据结构如下
     * {
     * "ruleIds": [123,324],
     * "apiCode": "7491638",
     * "startDate": "2022-05-01",
     * "taskTime": "01:00",
     * "conditionInfo": "[{\"logicalOperation\":\"and\",\"operationFactor\":[{\"fieldName\":\"appletDate\",\"fieldValue\":\"2022-04-28\",\"operation\":\"=\"},{\"fieldName\":\"appletTime\",\"fieldValue\":\"2022-04-28 10:15:03\",\"operation\":\"<\"},{\"fieldName\":\"userType\",\"fieldValue\":\"S02\",\"operation\":\"=\"}]}]"
     * }
     *
     * @param jobExecutionMultipleShardingContext
     */
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        String jobParameter = jobExecutionMultipleShardingContext.getJobParameter();
        BackEndScoreRuleConfigDTO dto = JSON.parseObject(jobParameter, BackEndScoreRuleConfigDTO.class);
        String value = UUID.randomUUID().toString();
        try {
            //加锁-跑分配置获取最新
            dataGroupHandlerService.addLockGroupScoreConfig(dto.getApiCode(), value);
            Result<List<CustomerScoreRuleVO>> scoreConfigNow = iRuleConfigService.getScoreConfigNow(dto.getRuleIds(), dto.getApiCode());
            AssertResult.assertResult(scoreConfigNow);
            for (CustomerScoreRuleVO datum : scoreConfigNow.getData()) {
                datum.setConditionInfo(dto.getConditionInfo());
                datum.setStartDate(dto.getStartDate());
                datum.setStartTime(dto.getTaskTime());
                datum.setAutoBuild(1);
                Result<Long> result = marketingTaskService.buildScoreTaskOfSelect(datum, null);

            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "后台生成手动规则以及任务异常"), e);
        } finally {
            dataGroupHandlerService.unlockGroupScoreConfig(dto.getApiCode(), value);
        }
    }
}
