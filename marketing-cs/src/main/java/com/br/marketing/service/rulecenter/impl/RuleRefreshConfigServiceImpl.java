package com.br.marketing.service.rulecenter.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.DecisionsTaskLogMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class RuleRefreshConfigServiceImpl implements IRuleRefreshConfigService {


    @Autowired
    MarketingTaskService marketingTaskService;

    @Resource
    MarketingTaskMapper marketingTaskMapper;

    @Resource
    PushDecisionsMapper pushDecisionsMapper;

    @Resource
    DecisionsTaskLogMapper decisionsTaskLogMapper;

    @Resource
    ScoreSearchConditionMapper scoreSearchConditionMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void autoRefreshConfig() {

        // autoRefreshConfig={"3710128":{"ruleNameShort":"R20240905-01","ruleNumber":"JC20241021_7410908_001"}}
        Map<String, JSONObject> map = marketingCommonConfig.getAutoRefreshConfig();

        for (Map.Entry<String, JSONObject> entry : map.entrySet()) {
            String apiCode = entry.getKey();
            JSONObject jsonObject = entry.getValue();
            // 跑分配置规则编号
            String ruleNameShort = jsonObject.getString("ruleNameShort");
            // 推送决策规则编号
            String ruleNumber = jsonObject.getString("ruleNumber");

            // 1- 根据决策规则编号查询决策配置
            PushDecisionsExample pushDecisionsExample = new PushDecisionsExample();
            pushDecisionsExample.createCriteria().andApiCodeEqualTo(apiCode).andRuleNumberEqualTo(ruleNumber).andIsDelEqualTo(Constants.DATA_VALID);
            List<PushDecisions> pushDecisionsList = pushDecisionsMapper.selectByExample(pushDecisionsExample);
            log.warn("决策规则编号查询决策配置:{}" ,JSONObject.toJSONString(pushDecisionsList));
            if(CollectionUtil.isEmpty(pushDecisionsList)){
                return;
            }
            PushDecisions pushDecisions = pushDecisionsList.get(0);

            // 2- aipCode + 推决策配置id + 日期 查询是否生成推决策任务
            DecisionsTaskLogExample decisionsTaskLogExample = new DecisionsTaskLogExample();
            decisionsTaskLogExample.createCriteria()
                    .andApiCodeEqualTo(apiCode)
                    .andPushConfigIdEqualTo(pushDecisions.getId())
                    .andIsDelEqualTo(Constants.DATA_VALID)
                    .andCreateTimeLessThanOrEqualTo(new Date());
            int i = decisionsTaskLogMapper.countByExample(decisionsTaskLogExample);
            if(i > 0){
                // 判断推决策配置是否失效
                if(pushDecisions.getStatus() != 2){
                    PushDecisions pushDecisions1 = new PushDecisions();
                    pushDecisions1.setId(pushDecisions.getId());
                    pushDecisions1.setStatus(2);
                    pushDecisionsMapper.updateByPrimaryKeySelective(pushDecisions1);
                }
                return;
            }
            // 3- apiCode + 日期 + 跑分状态 + 数据范围 + 跑分规则编号 查询当日跑分任务是否完成
            // 获取今天的日期
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDate today = LocalDate.now();
            LocalDateTime startTime = today.atStartOfDay();
            String createTimeStart = startTime.format(formatter);
            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            String createTimeEnd = now.format(formatter);
            Integer taskStatus = 2;
            String conditionType = "1";
            List<MarketingTaskVO> marketingTaskVOS = marketingTaskMapper.queryCompletStatus(apiCode,
                    createTimeStart, createTimeEnd, taskStatus, conditionType, ruleNameShort);
            if(CollectionUtil.isEmpty(marketingTaskVOS)){
                return;
            }
            //List<Long> ids = marketingTaskVOS.stream() // 创建一个 Stream
            //        .map(MarketingTaskVO::getId) // 将每个 MarketingTaskVO 映射到其 id
            //        .collect(Collectors.toList());

            MarketingTaskVO marketingTaskVO = marketingTaskVOS.get(0);

            // 4- 根据规则配置id，更新跑分id
            ScoreSearchCondition scoreSearchCondition = new ScoreSearchCondition();
            scoreSearchCondition.setId(pushDecisions.getDependencyTemplateId());
            scoreSearchCondition.setSourceCondition(String.valueOf(marketingTaskVO.getHisFileId()));
            scoreSearchConditionMapper.updateByPrimaryKeySelective(scoreSearchCondition);
            // 5- 更新生效状态
            PushDecisions pushDecisions1 = new PushDecisions();
            pushDecisions1.setId(pushDecisions.getId());
            pushDecisions1.setStatus(1);
            pushDecisionsMapper.updateByPrimaryKeySelective(pushDecisions1);
        }

    }

}
