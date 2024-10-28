package com.br.marketing.check.job;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.customizedassert.AssertResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.MarketingTaskExtend;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.service.MarketingTaskExtendService;
import com.br.marketing.service.ShuHeCuFuJieMatchDataService;
import com.br.marketing.service.SoleStrategyService;
import com.br.marketing.vo.MarketingTaskVO;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.base.Splitter;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 数禾促复借每日自动化匹配数据作业
 *
 * @author senyang.zheng
 * @date 2024/10/21
 */
@Component
@Slf4j
public class ShuHeCuFuJieMatchDataJob extends AbstractSimpleElasticJob {

    @Resource
    private ShuHeCuFuJieMatchDataService shuHeCuFuJieMatchDataService;
    @Resource
    private MarketingTaskMapper marketingTaskMapper;
    @Resource
    private MarketingTaskExtendService marketingTaskExtendService;
    @Resource
    private SoleStrategyService soleStrategyService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    /**
     * 过程
     *
     * @param context 语境
     * @author senyang.zheng
     * @date 2024/10/21
     */
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        JSONObject shuHeCuFuJieMatchDataConfig = JSONObject.parseObject(marketingCommonConfig.getShuHeCuFuJieMatchDataConfig());
        String apiCode = shuHeCuFuJieMatchDataConfig.getString("apiCode");
        String date = DateUtil.today();
        String batchNumber = null;
        Boolean forceFlag = false;
        String jobParameter = context.getJobParameter();
        if (StringUtils.isNotEmpty(jobParameter)) {
            List<String> params = Splitter.on("#").splitToList(jobParameter);
            date = params.get(0);
            apiCode = params.get(1);
            batchNumber = params.get(2);
            forceFlag = Boolean.valueOf(params.get(3));
        }
        MarketingTaskVO marketingTaskVO = marketingTaskMapper.queryLastNonValidationTask(apiCode, batchNumber);
        if (ObjectUtil.isNull(marketingTaskVO)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SHUHE_SERVICEERROR.getCode(), "数禾促复借每日自动化匹配 未查询到最近一次非验证跑分任务"));
            return;
        }
        batchNumber = marketingTaskVO.getBatchNumber();
        MarketingTaskExtend marketingTaskExtend = marketingTaskExtendService.getMarketingTaskExtend(marketingTaskVO.getId());
        Result<List<String>> dataCondition = soleStrategyService.analysisConditions(marketingTaskExtend.getDataCondition());
        AssertResult.assertResult(dataCondition);
        List<String> conditionList = dataCondition.getData();
        for (String condition : conditionList) {
            shuHeCuFuJieMatchDataService.matchData(condition, apiCode, date, batchNumber, forceFlag, marketingTaskVO.getHisFileId());
        }
    }

}
