package com.br.marketing.service.rulecenter.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.*;
import com.br.marketing.enums.ConditionTypeEnum;
import com.br.marketing.enums.ScoreStatusEnum;
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
import java.time.ZoneId;
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
    private static final String TITLE = "【自动刷新推决策配置】";

    @Override
    public void autoRefreshConfig() {

        Map<String, JSONObject> map = marketingCommonConfig.getAutoRefreshConfig();
        for (Map.Entry<String, JSONObject> entry : map.entrySet()) {
            buildRefreshConfig(entry);
        }
    }

    private void buildRefreshConfig(Map.Entry<String, JSONObject> entry) {

        try {
            String apiCode = entry.getKey();
            JSONObject jsonObject = entry.getValue();
            // 跑分配置规则编号
            String ruleNameShort = jsonObject.getString("ruleNameShort");
            // 推送决策规则编号
            String ruleNumber = jsonObject.getString("ruleNumber");

            // 1- 根据决策规则编号查询决策配置
            PushDecisions pushDecisions = queryPushDecisions(apiCode, ruleNumber);
            if(pushDecisions == null){
                return;
            }

            // 2- aipCode + 推决策配置id + 日期 查询是否生成推决策任务
            Boolean b = queryDecisionsTaskLog(apiCode, pushDecisions);
            if(b){
                return;
            }

            // 3- apiCode + 日期 + 跑分状态 + 数据范围 + 跑分规则编号 查询当日跑分任务是否完成
            MarketingTaskVO marketingTaskVO = queryMarketingTask(apiCode, ruleNameShort);
            if(marketingTaskVO == null){
                return;
            }

            // 4- 根据规则配置id，更新跑分id
            updateScoreSearch(pushDecisions,marketingTaskVO);

        }catch (Exception e){
            String title = "自动化刷新推送决策任务 刷新失败【未知异常】";
            String text = String.format("异常【%s】", e.getMessage());
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), text, title), e);
        }
    }

    private void updateScoreSearch(PushDecisions pushDecisions, MarketingTaskVO marketingTaskVO) {
        // 查询是否已经更新过跑分id
        ScoreSearchConditionExample scoreSearchConditionExample = new ScoreSearchConditionExample();
        scoreSearchConditionExample.createCriteria()
                .andIdEqualTo(pushDecisions.getDependencyTemplateId())
                .andSourceConditionEqualTo(String.valueOf(marketingTaskVO.getHisFileId()));
        int i1 = scoreSearchConditionMapper.countByExample(scoreSearchConditionExample);
        log.warn(TITLE + "查询是否已经更新过跑分:{}", i1);
        if(i1> 0){
            return;
        }
        // 更新跑分id
        ScoreSearchCondition scoreSearchCondition = new ScoreSearchCondition();
        scoreSearchCondition.setId(pushDecisions.getDependencyTemplateId());
        scoreSearchCondition.setSourceCondition(String.valueOf(marketingTaskVO.getHisFileId()));
        scoreSearchConditionMapper.updateByPrimaryKeySelective(scoreSearchCondition);
        // 更新生效状态
        PushDecisions pushDecisions1 = new PushDecisions();
        pushDecisions1.setId(pushDecisions.getId());
        pushDecisions1.setStatus(1);
        pushDecisionsMapper.updateByPrimaryKeySelective(pushDecisions1);
    }

    private MarketingTaskVO queryMarketingTask(String apiCode, String ruleNameShort) {

        // 获取今天的日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDate today = LocalDate.now();
        LocalDateTime startTime = today.atStartOfDay();
        String createTimeStart = startTime.format(formatter);
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        String createTimeEnd = now.format(formatter);
        Integer taskStatus = ScoreStatusEnum.FINISH.getValue();
        Integer conditionType = ConditionTypeEnum.RUNNING.getValue();
        List<MarketingTaskVO> marketingTaskVOS = marketingTaskMapper.queryCompletStatus(apiCode,
                createTimeStart, createTimeEnd, taskStatus, conditionType, ruleNameShort);
        if(CollectionUtil.isEmpty(marketingTaskVOS)){
            log.warn(TITLE + "当日跑分任务未完成，apiCode:{}", apiCode);
            return null;
        }
        return marketingTaskVOS.get(0);
    }

    private Boolean queryDecisionsTaskLog(String apiCode, PushDecisions pushDecisions) {
        LocalDateTime localDateTime = LocalDate.now().atTime(0, 0, 0);
        Date createTimeStart = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

        Boolean aTrue = Boolean.FALSE;
        DecisionsTaskLogExample decisionsTaskLogExample = new DecisionsTaskLogExample();
        decisionsTaskLogExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andPushConfigIdEqualTo(pushDecisions.getId())
                .andIsDelEqualTo(Constants.DATA_VALID)
                .andCreateTimeGreaterThanOrEqualTo(createTimeStart);
        int i = decisionsTaskLogMapper.countByExample(decisionsTaskLogExample);
        log.warn(TITLE + "查询是否生成推决策任务:{}", i);
        if(i > 0){
            // 判断推决策配置是否失效
            if(pushDecisions.getStatus() != 2){
                PushDecisions pushDecisions1 = new PushDecisions();
                pushDecisions1.setId(pushDecisions.getId());
                pushDecisions1.setStatus(2);
                pushDecisionsMapper.updateByPrimaryKeySelective(pushDecisions1);
            }
            aTrue = Boolean.TRUE;
            return aTrue;
        }
        return aTrue;
    }

    private PushDecisions queryPushDecisions(String apiCode, String ruleNumber) {
        PushDecisions pushDecisions = new PushDecisions();
        PushDecisionsExample pushDecisionsExample = new PushDecisionsExample();
        pushDecisionsExample.createCriteria().andApiCodeEqualTo(apiCode).andRuleNumberEqualTo(ruleNumber).andIsDelEqualTo(Constants.DATA_VALID);
        List<PushDecisions> pushDecisionsList = pushDecisionsMapper.selectByExample(pushDecisionsExample);
        log.warn(TITLE + "决策规则编号查询决策配置:{}", JSONObject.toJSONString(pushDecisionsList));
        if(CollectionUtil.isEmpty(pushDecisionsList)){
            return null;
        }
        pushDecisions = pushDecisionsList.get(0);
        return pushDecisions;
    }

}
