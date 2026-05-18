package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.check.service.RongShuNewScenePushPolicyService;
import com.br.marketing.client.intelligentcustomerservice.input.PolicyRetryByRuleSoleDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.dto.DataJoinLogDTO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 榕树新场景推决策：先情况 1（转化）全部分批推送，再情况 2（上传 201 窗口）；去重由 sole 切面处理。
 */
@Slf4j
@Service
public class RongShuNewScenePushPolicyServiceImpl implements RongShuNewScenePushPolicyService {

    private static final int BATCH_SIZE = 2000;
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final List<String> SCENARIO1_USER_TYPES = Arrays.asList("1", "201", "202", "3");
    private static final String SCENARIO2_USER_TYPE = "201";
    private static final String SCENARIO_TYPE_TRANSFER = "1";
    private static final String SCENARIO_TYPE_UPLOAD = "2";
    public static final String CASTR_0323375 = "CASTR0323375";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;
    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Override
    public void executePushPolicy() {
        String strategyCode = marketingCommonConfig.getRongShuNewScenePushPolicyStrategyCode();
        if (StringUtils.isBlank(strategyCode)) {
            strategyCode = CASTR_0323375;
        }
        List<String> apiCodes = marketingCommonConfig.getRongShuNewSceneApiCodes();
        if (CollectionUtils.isEmpty(apiCodes)) {
            log.warn("榕树新场景推决策：rongShuNewScenePushBlackListApiCodes 为空，跳过执行");
            return;
        }
        for (String apiCode : apiCodes) {
            try {
                pushPolicyForOneApiCode(apiCode, strategyCode);
            } catch (Exception ex) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(),
                                "榕树新场景推决策单 apiCode 执行异常 apiCode=" + apiCode + " " + ex.getMessage()),
                        ex);
            }
        }
    }

    private void pushPolicyForOneApiCode(String apiCode, String strategyCode) {
        String tcId = tableCreateService.getTcId(apiCode);
        if (StringUtils.isBlank(tcId)) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(),
                            "榕树新场景推决策未解析到 tcId，跳过 apiCode=" + apiCode));
            return;
        }
        LocalDate registerDateTMinus1 = LocalDate.now().minusDays(1);
        String registerStartTime = registerDateTMinus1 + " 00:00:00";
        String registerEndTime = registerDateTMinus1.plusDays(1) + " 00:00:00";
        TpDynamicExecutor threadPool = TpDynamicExecutorFactory
                .getThreadPool(ThreadPoolNameEnum.RONGSHU_NEW_SCENE_POLICY.getName(), 5, 20);
        try {
            pushScenario1Transfer(tcId, apiCode, registerStartTime, registerEndTime, strategyCode, threadPool);
            pushScenario2Upload(apiCode, strategyCode, threadPool);
        } finally {
            threadPool.shutdownAndAwaitTermination();
        }
    }

    private void pushScenario1Transfer(
            String tcId,
            String apiCode,
            String registerStartTime,
            String registerEndTime,
            String strategyCode,
            TpDynamicExecutor threadPool) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Long minId = null;
        for (; ; ) {
            List<MarketingTransferSyncUser> batch =
                    marketingTransferSyncUserMapper.getRsToPolicyDataByRegisterTime(
                            tcId,
                            SCENARIO1_USER_TYPES,
                            "0",
                            minId,
                            BATCH_SIZE,
                            apiCode,
                            registerStartTime,
                            registerEndTime);
            if (CollectionUtils.isEmpty(batch)) {
                break;
            }
            futures.add(CompletableFuture.runAsync(
                    () -> pushOneBatchFromTransfer(batch, apiCode, strategyCode, SCENARIO_TYPE_TRANSFER), threadPool));
            if (batch.size() < BATCH_SIZE) {
                break;
            }
            minId = batch.get(batch.size() - 1).getId();
        }
        if (CollectionUtils.isEmpty(futures)) {
            return;
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void pushScenario2Upload(String apiCode, String strategyCode, TpDynamicExecutor threadPool) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        String today = LocalDate.now().format(DAY_FMT);
        Long minId = null;
        for (; ; ) {
            List<MarketingSyncUser> batch =
                    marketingSyncInfoMapper.getMarketingSyncByCondition(
                            apiCode, null, today, SCENARIO2_USER_TYPE, null, null, minId);
            if (CollectionUtils.isEmpty(batch)) {
                break;
            }
            futures.add(CompletableFuture.runAsync(
                    () -> pushOneBatchFromSync(batch, apiCode, strategyCode, SCENARIO_TYPE_UPLOAD), threadPool));
            if (batch.size() < BATCH_SIZE) {
                break;
            }
            minId = batch.get(batch.size() - 1).getId();
        }
    }

    private void pushOneBatchFromTransfer(
            List<MarketingTransferSyncUser> rows,
            String apiCode,
            String strategyCode,
            String scenarioType) {
        ArrayList<DataJoinLogDTO> logList = new ArrayList<>();
        ArrayList<PushMarketingUserDetailDTO> pushs = new ArrayList<>();
        for (MarketingTransferSyncUser row : rows) {
            String custNum = row.getCustNum();
            if (StringUtils.isBlank(custNum)) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(),
                                "榕树新场景推决策 cust_num 为空 apiCode=" + apiCode + " transferId=" + row.getId()));
                continue;
            }
            PushMarketingUserDetailDTO dto = new PushMarketingUserDetailDTO();
            dto.setCaseNumber(custNum);
            dto.setPhone(custNum);
            dto.setVariables(variablesFromTransfer(row));
            pushs.add(dto);
            logList.add(
                    methodRetryHandlerService.dataJoinLogFix(
                            dto,
                            DistributeTypeEnum.POLICYDATA,
                            apiCode,
                            custNum,
                            custNum,
                            row.getId(),
                            DistributeSourceTypeEnum.TRANSFER,
                            scenarioType,
                            null));
        }
        if (pushs.isEmpty()) {
            return;
        }
        PolicyRetryByRuleSoleDTO sole = buildSoleDto(apiCode, strategyCode, pushs, logList, scenarioType);
        Result<?> result = methodRetryHandlerService.callPolicySoleData(sole, 0);
        if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(),
                            String.format(
                                    "榕树新场景推决策调用失败 apiCode=%s scenarioType=%s code=%s",
                                    apiCode, scenarioType, result.getCode())));
        }
    }

    private void pushOneBatchFromSync(
            List<MarketingSyncUser> rows,
            String apiCode,
            String strategyCode,
            String scenarioType) {
        ArrayList<DataJoinLogDTO> logList = new ArrayList<>();
        ArrayList<PushMarketingUserDetailDTO> pushs = new ArrayList<>();
        for (MarketingSyncUser row : rows) {
            String cellMd5 = row.getCellMd5();
            if (StringUtils.isBlank(cellMd5)) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(),
                                "榕树新场景推决策 cust_num 为空 apiCode=" + apiCode + " syncId=" + row.getId()));
                continue;
            }
            PushMarketingUserDetailDTO dto = new PushMarketingUserDetailDTO();
            dto.setCaseNumber(cellMd5);
            dto.setPhone(cellMd5);
            dto.setVariables(variablesFromSync(row));
            pushs.add(dto);
            logList.add(
                    methodRetryHandlerService.dataJoinLogFix(
                            dto,
                            DistributeTypeEnum.POLICYDATA,
                            apiCode,
                            cellMd5,
                            cellMd5,
                            row.getId(),
                            DistributeSourceTypeEnum.TRANSFER,
                            scenarioType,
                            null));
        }
        if (pushs.isEmpty()) {
            return;
        }
        PolicyRetryByRuleSoleDTO sole = buildSoleDto(apiCode, strategyCode, pushs, logList, scenarioType);
        Result<?> result = methodRetryHandlerService.callPolicySoleData(sole, 0);
        if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(),
                            String.format(
                                    "榕树新场景推决策调用失败 apiCode=%s scenarioType=%s code=%s",
                                    apiCode, scenarioType, result.getCode())));
        }
    }

    private PolicyRetryByRuleSoleDTO buildSoleDto(
            String apiCode,
            String strategyCode,
            ArrayList<PushMarketingUserDetailDTO> pushs,
            ArrayList<DataJoinLogDTO> logList,
            String scenarioType) {
        PolicyRetryByRuleSoleDTO retryByRuleDTO = new PolicyRetryByRuleSoleDTO();
        retryByRuleDTO.setApiCode(apiCode);
        retryByRuleDTO.setBatchNumber(DateFormatUtils.format(new Date(), "yyyyMMdd") + "_" + scenarioType + "_" + apiCode);
        retryByRuleDTO.setStrategyCode(strategyCode);
        retryByRuleDTO.setData(pushs);
        retryByRuleDTO.setDetailLogList(logList);
        retryByRuleDTO.setIsSole(Boolean.TRUE);
        retryByRuleDTO.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        return retryByRuleDTO;
    }

    private JSONObject variablesFromTransfer(MarketingTransferSyncUser row) {
        JSONObject v = new JSONObject();
        v.put("userType", row.getUserType());
        return v;
    }

    private JSONObject variablesFromSync(MarketingSyncUser row) {
        JSONObject v = new JSONObject();
        v.put("userType", row.getUserType());
        mergeReserveJson(v, row.getReserveField1());
        return v;
    }

    private void mergeReserveJson(JSONObject target, String reserveField1) {
        if (StringUtils.isBlank(reserveField1)) {
            return;
        }
        try {
            JSONObject parsed = JSONObject.parseObject(reserveField1);
            if (parsed != null) {
                for (String key : parsed.keySet()) {
                    if (!target.containsKey(key)) {
                        target.put(key, parsed.get(key));
                    }
                }
            }
        } catch (Exception ignored) {
            // 非 JSON 的 reserve 不合并
        }
    }
}
