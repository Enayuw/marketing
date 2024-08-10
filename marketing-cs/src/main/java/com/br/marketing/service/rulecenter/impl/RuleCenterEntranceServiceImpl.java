package com.br.marketing.service.rulecenter.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
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
import org.springframework.stereotype.Service;

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
        List<PushDecisions> pushDecisionsConfig = getPushDecisionsConfig();
        for (PushDecisions pushDecisions : pushDecisionsConfig) {
            try {
                ScoreSearchCondition scoreSearchCondition = scoreSearchConditionMapper.selectByPrimaryKey(pushDecisions.getDependencyTemplateId());
                IRuleTaskService ruleTaskService = ruleCenterBySourceTypeFactory.getRuleTaskService(scoreSearchCondition.getSourceType());
                IRuleCenterFilterTemplateService fileterTemplate = ruleCenterBySourceTypeFactory.getFileterTemplate(scoreSearchCondition.getSourceType());
                Result<CustomerInfoPushMain> canBuild = isCanBuild(pushDecisions, scoreSearchCondition);
                if (canBuild.isSuccess()) {

                    CustomerInfoPushMain data = canBuild.getData();
                    PushCustomerDTO pushCustomerDTO = new PushCustomerDTO();
                    pushCustomerDTO.setApiCode(pushDecisions.getApiCode());
                    pushCustomerDTO.setBatchNumberList(Lists.newArrayList());
                    List<Long> collect = Arrays.stream(scoreSearchCondition.getSourceCondition().split(",")).map(t -> Long.valueOf(t)).collect(Collectors.toList());
                    pushCustomerDTO.setFileIdList(collect);
                    pushCustomerDTO.setmRuleCondition(scoreSearchCondition.getSourceCondition());
                    pushCustomerDTO.setmRuleConditionShow(scoreSearchCondition.getContentShow());
                    Result<PushViewVO> pushViewVOResult = ruleTaskService.pushPreview(pushCustomerDTO);
                    if (pushViewVOResult.isSuccess()
                            && pushViewVOResult.getData() != null
                            && pushViewVOResult.getData().getTotal() >= 0) {
                            data.setmRealyNum(pushViewVOResult.getData().getTotal());
                            data.setmStatus(PushRuleStatusEnum.TO_BE_RUNNING.getValue());
                            customerInfoPushMainMapper.updateByPrimaryKeySelective(data);
                    } else {
                        String title = "自动化推送决策任务生成失败";
                        String text = String.format("自动化规则【%s】,apiCde【%s】,失败原因【%s】"
                                , pushDecisions.getRuleNumber()
                                , pushDecisions.getApiCode()
                                , pushViewVOResult.getMessage());
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_URGENT.getCode(), text, title));
                    }
                }
            } catch (Exception e) {
                String title = "自动化推送决策任务生成失败【未知异常】";
                String text = String.format("异常【%s】",e.getMessage());
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), text, title));
            }
        }
    }

    private List<PushDecisions> getPushDecisionsConfig() {
        String nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        PushDecisionsExample decisionsExample = new PushDecisionsExample();
        decisionsExample.createCriteria()
                .andStatusEqualTo(Constants.STATUS_START)
                .andAutoTimeGreaterThanOrEqualTo(nowTime)
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
        if (decisionsTaskLogs.size() <= 0) {
            unLockDecis(pushDecisions.getId(), uuid.toString());
            return res.failure();
        }

        Date date = new Date();
        CustomerInfoPushMain pushMain = new CustomerInfoPushMain();
        pushMain.setId(0L);
        pushMain.setmApiCode(pushDecisions.getApiCode());
        pushMain.setmRealyNum(0);
        pushMain.setmStatus(PushRuleStatusEnum.TO_BE_BUILDING.getValue());
        pushMain.setCreateTime(date);
        pushMain.setUpdateTime(date);
        pushMain.setmRuleCondition(scoreSearchCondition.getSourceCondition());
        pushMain.setmRuleConditionShow(scoreSearchCondition.getContentShow());
        pushMain.setBatchName(pushDecisions.getPushDatasets());
        pushMain.setBuildType(BuildTypeEnum.AUTOBUILD.getCode());
        customerInfoPushMainMapper.insertSelective(pushMain);


        IRuleCenterFilterTemplateService fileterTemplate = ruleCenterBySourceTypeFactory.getFileterTemplate(scoreSearchCondition.getSourceType());
        fileterTemplate.autoBuildSource(pushMain, scoreSearchCondition);

        DecisionsTaskLog decisionsTaskLog = new DecisionsTaskLog();
        decisionsTaskLog.setApiCode(pushDecisions.getApiCode());
        decisionsTaskLog.setPushConfigId(pushDecisions.getId());
        decisionsTaskLog.setPushMainId(pushMain.getId());
        decisionsTaskLog.setIsDel(0);
        decisionsTaskLog.setCreateTime(new Date());
        decisionsTaskLog.setUpdateTime(new Date());

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
