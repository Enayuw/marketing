package com.br.marketing.service.rulecenter.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.ConditionTypeEnum;
import com.br.marketing.enums.PushRuleStatusEnum;
import com.br.marketing.enums.ScoreStatusEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.rulecenter.IRuleCenterEntranceService;
import com.br.marketing.service.rulecenter.IRuleCenterFilterTemplateService;
import com.br.marketing.service.rulecenter.IRuleTaskService;
import com.br.marketing.service.rulecenter.RuleCenterBySourceTypeFactory;
import com.br.marketing.service.rulecenter.enums.BuildTypeEnum;
import com.br.marketing.vo.MarketingTaskVO;
import com.br.marketing.vo.xiecheng.PushViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    StraHisFileMapper straHisFileMapper;

    @Resource
    MarketingTaskMapper marketingTaskMapper;
    @Resource
    PushRuleService pushRuleService;

    private static final String TITLE = "【自动生成决策任务】";

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
                ScoreSearchCondition scoreSearchCondition1 = scoreSearchConditionMapper.selectByPrimaryKey(pushDecisions.getDependencyTemplateId());
                PushCustomerDTO pushCustomerDTO = ruleTaskService.buildPreviewDTO(data, scoreSearchCondition1);
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

    private ScoreSearchCondition updateScoreSearch(PushDecisions pushDecisions, MarketingTaskVO marketingTaskVO) {
        // 查询是否已经更新过跑分id
        ScoreSearchConditionExample scoreSearchConditionExample = new ScoreSearchConditionExample();
        scoreSearchConditionExample.createCriteria()
                .andIdEqualTo(pushDecisions.getDependencyTemplateId())
                .andSourceConditionEqualTo(String.valueOf(marketingTaskVO.getHisFileId()));
        int i1 = scoreSearchConditionMapper.countByExample(scoreSearchConditionExample);
        if(i1> 0){
            return null;
        }
        // 更新跑分id
        ScoreSearchCondition scoreSearchCondition = new ScoreSearchCondition();
        scoreSearchCondition.setId(pushDecisions.getDependencyTemplateId());
        scoreSearchCondition.setSourceCondition(String.valueOf(marketingTaskVO.getHisFileId()));
        scoreSearchConditionMapper.updateByPrimaryKeySelective(scoreSearchCondition);

        // 查询最新的规则配置信息
        return scoreSearchConditionMapper.selectByPrimaryKey(pushDecisions.getDependencyTemplateId());
    }

    private MarketingTaskVO queryMarketingTask(String apiCode, ScoreSearchCondition scoreSearchCondition) {
        List<String> ids = new ArrayList<>();
        String sourceCondition = scoreSearchCondition.getSourceCondition();
        if(sourceCondition.contains(",")){
            ids = Arrays.stream(sourceCondition.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }else {
            ids.add(sourceCondition);
        }
        if(CollectionUtil.isEmpty(ids)){
            log.warn(TITLE + "规则模板未关联跑分文件，模板名称:{}", scoreSearchCondition.getName());
            return null;
        }
        // 根据跑分文件id查询跑分配置
        List<String> ruleNameShorts = straHisFileMapper.getFileById(ids);
        if(CollectionUtil.isEmpty(ruleNameShorts)){
            log.warn(TITLE + "查询跑分配置为空，跑分文件id:{}", ids);
            return null;
        }
        String ruleNameShort = ruleNameShorts.get(0);
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
            log.warn(TITLE + "当日跑分文件未执行完成:{}", JSONObject.toJSONString(marketingTaskVOS));
            return null;
        }
        return marketingTaskVOS.get(0);
    }

    private List<PushDecisions> getPushDecisionsConfig() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String nowTime = LocalTime.now().format(formatter);
        PushDecisionsExample decisionsExample = new PushDecisionsExample();
        decisionsExample.createCriteria()
                .andStatusEqualTo(Constants.STATUS_START)
                .andAutoTimeLessThanOrEqualTo(nowTime)
                .andIsDelEqualTo(Constants.DATA_VALID);
        decisionsExample.setOrderByClause(" auto_time");
        List<PushDecisions> pushDecisions = pushDecisionsMapper.selectByExample(decisionsExample);

        // 过滤掉（今天更新 && 执行时间小于更新时间HH:mm）
        return filterConfig(pushDecisions,formatter);
    }

    private Result<CustomerInfoPushMain> isCanBuild(PushDecisions pushDecisions, ScoreSearchCondition scoreSearchCondition) {

        Result<CustomerInfoPushMain> res = new Result<>();

        UUID uuid = UUID.randomUUID();
        if (!LockDecis(pushDecisions.getId(), uuid.toString())) {
            return res.failure();
        }
        // 1- 判断是否已经生成推决策任务
        Date day = Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        DecisionsTaskLogExample decisionsTaskLogExample = new DecisionsTaskLogExample();
        decisionsTaskLogExample.createCriteria()
                .andPushConfigIdEqualTo(pushDecisions.getId())
                .andIsDelEqualTo(Constants.DATA_VALID)
                .andCreateTimeGreaterThanOrEqualTo(day);
        List<DecisionsTaskLog> decisionsTaskLogs = decisionsTaskLogMapper.selectByExample(decisionsTaskLogExample);
        if (decisionsTaskLogs.size() > 0) {
            log.warn("已生成推决策任务，推决策配置id:{}", pushDecisions.getId());
            unLockDecis(pushDecisions.getId(), uuid.toString());
            return res.failure();
        }
        // 2- 判断是否需要自动刷新配置
        if(pushDecisions.getAutoRefresh() == 1){
            // 3- 查询当日跑分任务是否完成
            MarketingTaskVO marketingTaskVO = queryMarketingTask(pushDecisions.getApiCode(), scoreSearchCondition);
            if(marketingTaskVO == null){
                unLockDecis(pushDecisions.getId(), uuid.toString());
                return res.failure();
            }
            // 4- 根据规则配置id，更新跑分id
            scoreSearchCondition = updateScoreSearch(pushDecisions, marketingTaskVO);
            if(scoreSearchCondition == null){
                log.warn(TITLE + "该规则配置已经更新过跑分:{}", pushDecisions.getDependencyTemplateId());
                unLockDecis(pushDecisions.getId(), uuid.toString());
                return res.failure();
            }
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
        pushMain.setmScoreCondition(scoreSearchCondition.getScoreContent());
        pushMain.setStrategyCode(pushDecisions.getReachStrategy());
        pushMain.setTagContent(scoreSearchCondition.getTagContent());
        pushMain.setPushTarget(pushDecisions.getPushTarget());
        String batchName = "";
        String datasets = pushDecisions.getPushDatasets();
        if (StringUtils.isNotBlank(datasets)) {
            boolean containsDatePlaceholder = datasets.contains("yyyymmdd") || datasets.contains("YYYYMMDD");
            if (containsDatePlaceholder) {
                String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                batchName = datasets.replaceFirst("(?i)yyyymmdd|YYYYMMDD", formattedDate);
            }else {
                batchName = datasets;
            }
        }else {
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

    private List<PushDecisions> filterConfig(List<PushDecisions> pushDecisions,DateTimeFormatter formatter) {
        LocalDate today = LocalDate.now();
        pushDecisions = pushDecisions.stream()
                .filter(p -> {
                    Date date = p.getUpdateTime();
                    // 更新日期
                    LocalDate updateDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    // 更新时间 HH:mm
                    LocalTime updateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalTime();
                    // 执行时间 HH:mm
                    LocalTime autoTimeParsed = LocalTime.parse(p.getAutoTime(), formatter);

                    boolean b = !updateDate.equals(today);
                    boolean b1 = !autoTimeParsed.isBefore(updateTime);
                    return b || b1;
                })
                .collect(Collectors.toList());
        return pushDecisions;
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
