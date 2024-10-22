package com.br.marketing.service.rulecenter.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.DecisionsTaskLogMapper;
import com.br.marketing.mapper.PushDecisionsMapper;
import com.br.marketing.mapper.ScoreSearchConditionMapper;
import com.br.marketing.service.MarketingTaskService;
import com.br.marketing.service.rulecenter.*;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.MarketingTaskVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Service
@Slf4j
public class RuleRefreshConfigServiceImpl implements IRuleRefreshConfigService {


    @Autowired
    MarketingTaskService marketingTaskService;

    @Resource
    PushDecisionsMapper pushDecisionsMapper;

    @Resource
    DecisionsTaskLogMapper decisionsTaskLogMapper;

    @Resource
    ScoreSearchConditionMapper scoreSearchConditionMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void buildRefreshConfig() {

        // autoRefreshConfig={"3710128":{"ruleNameShort":"R20240905-01","ruleNumber":"JC20241021_7410908_001"}}
        Map<String, JSONObject> map = marketingCommonConfig.getAutoRefreshConfig();


        for (Map.Entry<String, JSONObject> entry : map.entrySet()) {
            String apiCode = entry.getKey();
            JSONObject jsonObject = entry.getValue();
            // 跑分配置规则编号
            String ruleNameShort = jsonObject.getString("ruleNameShort");
            // 推送决策规则编号
            String ruleNumber = jsonObject.getString("ruleNumber");


            // 根据决策规则编号查询决策配置
            PushDecisionsExample pushDecisionsExample = new PushDecisionsExample();
            pushDecisionsExample.createCriteria().andApiCodeEqualTo(apiCode).andRuleNumberEqualTo(ruleNumber).andIsDelEqualTo(Constants.DATA_VALID);
            List<PushDecisions> pushDecisionsList = pushDecisionsMapper.selectByExample(pushDecisionsExample);

            if(CollectionUtil.isEmpty(pushDecisionsList)){
                return;
            }
            PushDecisions pushDecisions = pushDecisionsList.get(0);

            // aipCode + 推决策配置id + 日期 查询是否生成推决策任务

            DecisionsTaskLogExample decisionsTaskLogExample = new DecisionsTaskLogExample();
            decisionsTaskLogExample.createCriteria()
                    .andPushConfigIdEqualTo(pushDecisions.getId())
                    .andIsDelEqualTo(Constants.DATA_VALID)
                    .andCreateTimeGreaterThanOrEqualTo(new Date());
            List<DecisionsTaskLog> decisionsTaskLogs = decisionsTaskLogMapper.selectByExample(decisionsTaskLogExample);


            // 判断推决策配置是否失效
            // apiCode + 日期 + 跑分状态 + 数据范围 + 跑分规则编号 查询当日跑分任务是否完成

            //根据规则配置id，更新跑分id和生效状态



            //
            //
            //
            //PageResultReturn list = marketingTaskService.list(1, 10, key,
            //        null, null, null, null, null, null, null);
            //
            //List<MarketingTaskVO> fastTaskRuleListVOS = list.getRecords();
            //if(CollectionUtil.isEmpty(fastTaskRuleListVOS)){
            //    return;
            //}
            //MarketingTaskVO marketingTaskVO = fastTaskRuleListVOS.get(0);
            //
            //Long hisFileId = marketingTaskVO.getHisFileId();
            //
            ////根据value查询模板
            //ScoreSearchConditionExample scoreSearchConditionExample = new ScoreSearchConditionExample();
            //scoreSearchConditionExample.createCriteria().andNameEqualTo(value).andIsDelEqualTo(0).andStatusEqualTo(1);
            //List<ScoreSearchCondition> scoreSearchConditions = scoreSearchConditionMapper.selectByExample(scoreSearchConditionExample);
            //if(CollectionUtil.isEmpty(scoreSearchConditions)){
            //    return;
            //}
            ////若不为空，则更新改模板的source_condition字段为hisFileId
            //ScoreSearchCondition scoreSearchCondition1 = new ScoreSearchCondition();
            //scoreSearchCondition1.setSourceCondition(String.valueOf(hisFileId));
            //ScoreSearchConditionExample scoreSearchConditionExample1 = new ScoreSearchConditionExample();
            //scoreSearchConditionExample1.createCriteria().andNameEqualTo(value).andIsDelEqualTo(0).andStatusEqualTo(1);
            //int updateCount = scoreSearchConditionMapper.updateByExampleSelective(scoreSearchCondition1, scoreSearchConditionExample1);
            //if(updateCount == 0){
            //    log.warn("自动刷新模板失败:{}" ,key);
            //    return;
            //}

        }


    }
}
