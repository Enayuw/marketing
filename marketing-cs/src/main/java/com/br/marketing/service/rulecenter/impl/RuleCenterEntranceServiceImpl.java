package com.br.marketing.service.rulecenter.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.PushRuleStatusEnum;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.mapper.DecisionsTaskLogMapper;
import com.br.marketing.mapper.PushDecisionsMapper;
import com.br.marketing.mapper.ScoreSearchConditionMapper;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.rulecenter.IRuleCenterEntranceService;
import com.br.marketing.service.rulecenter.IRuleCenterFilterTemplateService;
import com.br.marketing.service.rulecenter.IRuleTaskService;
import com.br.marketing.service.rulecenter.RuleCenterBySourceTypeFactory;
import com.br.marketing.service.rulecenter.enums.BuildTypeEnum;
import com.br.marketing.vo.xiecheng.PushViewVO;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RuleCenterEntranceServiceImpl implements IRuleCenterEntranceService {

    @Resource
    PushDecisionsMapper pushDecisionsMapper;

    @Resource
    ScoreSearchConditionMapper scoreSearchConditionMapper;

    @Resource
    RedisChgService redisChgService;

    @Resource
    DecisionsTaskLogMapper decisionsTaskLogMapper;

    @Resource
    CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Resource
    RuleCenterBySourceTypeFactory ruleCenterBySourceTypeFactory;

    @Resource
    PushRuleService pushRuleService;

    @Override
    public void buildPolicyTask() {
//        RuleCenterEntranceServiceImpl ruleCenterEntranceService = (RuleCenterEntranceServiceImpl) AopContext.currentProxy();
        List<PushDecisions> pushDecisionsConfig = getPushDecisionsConfig();
        for (PushDecisions pushDecisions : pushDecisionsConfig) {
            buildSiglePolicyTask(pushDecisions);
        }
    }

    public void buildSiglePolicyTask(PushDecisions pushDecisions) {
        try {
            ScoreSearchCondition scoreSearchCondition = scoreSearchConditionMapper.selectByPrimaryKey(pushDecisions.getDependencyTemplateId());
            IRuleTaskService ruleTaskService = ruleCenterBySourceTypeFactory.getRuleTaskService(scoreSearchCondition.getSourceType());
            IRuleCenterFilterTemplateService fileterTemplate = ruleCenterBySourceTypeFactory.getFileterTemplate(scoreSearchCondition.getSourceType());
            Result<CustomerInfoPushMain> canBuild = isCanBuild(pushDecisions, scoreSearchCondition);
            if (canBuild.isSuccess()) {

                CustomerInfoPushMain data = canBuild.getData();
                PushCustomerDTO pushCustomerDTO = ruleTaskService.buildPreviewDTO(data, scoreSearchCondition);
                Result<PushViewVO> pushViewVOResult = ruleTaskService.pushPreview(pushCustomerDTO);
                if (pushViewVOResult.isSuccess()
                        && pushViewVOResult.getData() != null
                        && pushViewVOResult.getData().getTotal() >= 0) {
                    data.setmRealyNum(pushViewVOResult.getData().getTotal());
                    data.setmStatus(PushRuleStatusEnum.TO_BE_RUNNING.getValue());
                    customerInfoPushMainMapper.updateByPrimaryKeySelective(data);
                } else {
                    String title = "自动化推送决策任务生成失败";
                    String text = String.format("自动化规则【%s】,apiCode【%s】,失败原因【%s】"
                            , pushDecisions.getRuleNumber()
                            , pushDecisions.getApiCode()
                            , pushViewVOResult.getMessage());
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_URGENT.getCode(), text, title));
                }
            }
        } catch (Exception e) {
            String title = "自动化推送决策任务生成失败【未知异常】";
            String text = String.format("异常【%s】", e.getMessage());
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), text, title), e);
        }
    }

    private List<PushDecisions> getPushDecisionsConfig() {
        String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        PushDecisionsExample decisionsExample = new PushDecisionsExample();
        decisionsExample.createCriteria()
                .andStatusEqualTo(Constants.STATUS_START)
                .andAutoTimeLessThanOrEqualTo(nowTime)
                .andIsDelEqualTo(Constants.DATA_VALID);
        decisionsExample.setOrderByClause(" auto_time");
        List<PushDecisions> pushDecisions = pushDecisionsMapper.selectByExample(decisionsExample);
        return pushDecisions;
    }

    private Result<CustomerInfoPushMain> isCanBuild(PushDecisions pushDecisions, ScoreSearchCondition scoreSearchCondition) {

        Result<CustomerInfoPushMain> res = new Result<>();

        UUID uuid = UUID.randomUUID();
        if (!LockDecis(pushDecisions.getId(), uuid.toString())) {
            return res.failure();
        }
        Date day = Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        DecisionsTaskLogExample decisionsTaskLogExample = new DecisionsTaskLogExample();
        decisionsTaskLogExample.createCriteria()
                .andPushConfigIdEqualTo(pushDecisions.getId())
                .andIsDelEqualTo(Constants.DATA_VALID)
                .andCreateTimeGreaterThanOrEqualTo(day);
        List<DecisionsTaskLog> decisionsTaskLogs = decisionsTaskLogMapper.selectByExample(decisionsTaskLogExample);
        if (decisionsTaskLogs.size() > 0) {
            unLockDecis(pushDecisions.getId(), uuid.toString());
            return res.failure();
        }

        Date date = new Date();
        CustomerInfoPushMain pushMain = new CustomerInfoPushMain();
        pushMain.setmApiCode(pushDecisions.getApiCode());
        pushMain.setmRealyNum(0);
        pushMain.setmStatus(PushRuleStatusEnum.TO_BE_BUILDING.getValue());
        pushMain.setCreateTime(date);
        pushMain.setUpdateTime(date);
        pushMain.setmRuleCondition(scoreSearchCondition.getContent());
        pushMain.setmRuleConditionShow(scoreSearchCondition.getContentShow());
        pushMain.setStrategyCode(pushDecisions.getReachStrategy());
        String batchName = "";
        if (StringUtils.isBlank(pushDecisions.getPushDatasets())) {
            batchName = pushDecisions.getPushDatasets();
        } else {
            batchName = LocalDate.now().toString()
                    .concat("-")
                    .concat(scoreSearchCondition.getName())
                    .concat("-")
                    .concat(LocalTime.now().withNano(0)
                            .toString());
        }
        pushMain.setBatchName(batchName);
        pushMain.setBuildType(BuildTypeEnum.AUTOBUILD.getCode());
        customerInfoPushMainMapper.insertSelective(pushMain);


        IRuleCenterFilterTemplateService fileterTemplate = ruleCenterBySourceTypeFactory.getFileterTemplate(scoreSearchCondition.getSourceType());
        Result sourceResul = fileterTemplate.autoBuildSource(pushMain, scoreSearchCondition);

        DecisionsTaskLog decisionsTaskLog = new DecisionsTaskLog();
        decisionsTaskLog.setApiCode(pushDecisions.getApiCode());
        decisionsTaskLog.setPushConfigId(pushDecisions.getId());
        decisionsTaskLog.setPushMainId(pushMain.getId());
        decisionsTaskLog.setIsDel(Constants.DATA_VALID);
        decisionsTaskLog.setCreateTime(new Date());
        decisionsTaskLog.setUpdateTime(new Date());
        decisionsTaskLogMapper.insertSelective(decisionsTaskLog);
        unLockDecis(pushDecisions.getId(), uuid.toString());
        return res.setDate(pushMain).success();
    }

    private Boolean LockDecis(Long id, String value) {
        String key = RedisKeyConstant.POLICY_BUILD_LOCK.concat(id.toString());
        return redisChgService.lock(key, value, 5000L);
    }

    private void unLockDecis(Long id, String value) {
        String key = RedisKeyConstant.POLICY_BUILD_LOCK.concat(id.toString());
        if (redisChgService.exists(key)) {
            String s = redisChgService.get(key);
            if (value.equals(s)) {
                redisChgService.del(s);
            }
        }
    }
}
