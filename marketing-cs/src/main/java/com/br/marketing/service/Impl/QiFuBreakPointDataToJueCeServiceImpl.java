package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.intelligentcustomerservice.input.PolicyRetryByRuleSoleDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.DataJoinLogDTO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PeriodRange;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.rule.qifu.util.QiFuTransferDataUtil;
import com.br.marketing.service.QiFuBreakPointDataToJueCeService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.ValidityPeriodDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import cn.hutool.core.lang.Pair;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description QiFuBreakPointDataToJueCeServiceImpl
 * @Author hong.chen
 * @CreateTime 2023/10/09
 */
@Service
@Slf4j
public class QiFuBreakPointDataToJueCeServiceImpl implements QiFuBreakPointDataToJueCeService {
    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private ValidityPeriodDataService validityPeriodDataService;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;
    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Override
    public void doProcess(String param) {
        log.warn("奇富断点自动化数据推决策，JOB开始");
        HashMap<String, String> qiFuToJueCeApiCodes = marketingCommonConfig.getQiFuToJueCeApiCodes();
        if (!qiFuToJueCeApiCodes.isEmpty()) {
            for (String apiCode : qiFuToJueCeApiCodes.keySet()) {
                String tcId = tableCreateService.getTcId(apiCode);

                Pair<String, String> validityRange =
                        validityPeriodDataService.getMarketingTransferDataWithValidityRange(apiCode);
                if (null == validityRange) {
                    log.error("奇富apiCode:" + apiCode + ":所有配置在有效期配置表中的上传数据均已失效！");
                    continue;
                }

                String startDate = validityRange.getKey();
                String endDate = validityRange.getValue();

                Long indexId = null;

                // 开启线程池
                Integer threadNum =
                        marketingCommonConfig.getQiFuBreakPointDataToJueCeThreadNum();
                ThreadPoolExecutor pool = BrExecutors.getThreadPool(threadNum, threadNum);
                while (true) {
                    // 每个线程2000条
                    // 初步框定transformType非1的转化数据
                    List<MarketingTransferSyncUser> marketingTransferSyncUserList =
                            marketingTransferSyncUserMapper.getQiFuBreakPointTransferByRequestDatetikv_(tcId, apiCode, startDate, endDate, indexId);
                    if (marketingTransferSyncUserList.isEmpty()) {
                        break;
                    }

                    indexId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();

                    modifyCorePoolSize(pool);
                    pool.execute(() -> filterAndPushData(marketingTransferSyncUserList, apiCode, tcId));
                }

                // 关闭线程池
                pool.shutdown();
                try {
                    while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
                        log.info("等待线程池结束");
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
        } else {
            log.error("奇富断点自动化数据推决策JOB未配置apiCode");
        }
        log.warn("奇富断点自动化数据推决策，JOB结束");
    }

    private void modifyCorePoolSize(ThreadPoolExecutor pool) {
        Integer threadNum =
                marketingCommonConfig.getQiFuBreakPointDataToJueCeThreadNum();
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, threadNum);
    }

    private void filterAndPushData(List<MarketingTransferSyncUser> list, String apiCode, String tcId) {
        try {
            long start = System.currentTimeMillis();
            // 根据有效期过滤转化数据并返回有效期内的上传数据
            Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum = getStringSyncUserValidityPeriodsBOMap(list, apiCode);
            long cost1 = System.currentTimeMillis();
            log.warn("奇富推送决策，调用有效期方法耗时：{}ms", cost1 - start);

            // 剔除掉不符合推送规则的转化数据，在有效期内且满足规则1且满足规则2，则推送。
            filterData(list, apiCode, tcId, validityPeriodsByCustNum);
            long cost2 = System.currentTimeMillis();
            log.warn("奇富推送决策，剔除方法耗时：{}ms", cost2 - cost1);

            // 组装参数并推送
            pushData(list, apiCode, validityPeriodsByCustNum);
            long cost3 = System.currentTimeMillis();
            log.warn("奇富推送决策，推送方法耗时：{}ms", cost3 - cost2);
        } catch (Exception e) {
            log.error("奇富断点自动化数据推决策JOB:" + e.getMessage(), e);
        }
    }

    private void pushData(List<MarketingTransferSyncUser> list, String apiCode, Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum) {
        ArrayList<DataJoinLogDTO> logList = new ArrayList<>();
        ArrayList<PushMarketingUserDetailDTO> pushs = new ArrayList<>();
        // 推送的apicode
        String toJueCeApiCode = marketingCommonConfig.getQiFuToJueCeApiCodes().get(apiCode);
        // 情况类型
        String actionType = "1";

        // 组装推送参数
        buildPushParam(list, validityPeriodsByCustNum, logList, pushs, toJueCeApiCode, actionType);
        // 组装重试参数
        PolicyRetryByRuleSoleDTO retryByRuleDTO = getPolicyRetryByRuleSoleDTO(actionType, toJueCeApiCode, logList, pushs);

        log.warn("奇富推送决策,去重前数据量级:{}", pushs.size());
        // 推送决策方法
        methodRetryHandlerService.callPolicySoleData(retryByRuleDTO, 0);
    }

    private void buildPushParam(List<MarketingTransferSyncUser> list, Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum,
                                ArrayList<DataJoinLogDTO> logList, ArrayList<PushMarketingUserDetailDTO> pushs, String toJueCeApiCode,
                                String actionType) {
        for (MarketingTransferSyncUser marketingTransferSyncUser : list) {
            String custNum = marketingTransferSyncUser.getCustNum();
            String requestTime = marketingTransferSyncUser.getRequestData();

            SyncUserValidityPeriodsBO bo = validityPeriodsByCustNum.get(custNum);

            List<MarketingSyncUser> syncUsers = bo.getSyncUsers();
            if (syncUsers == null) {
                log.error("奇富：根据有效期方法没有获取到上传数据。custNum：{}" + custNum);
                continue;
            }
            MarketingSyncUser syncUser = syncUsers.get(0);
            if (syncUser == null) {
                log.error("奇富：根据有效期方法没有获取到上传数据。custNum：{}" + custNum);
                continue;
            }

            String cell = syncUser.getCell();
            String taskId = syncUser.getCusBatch();

            // 封装推送参数
            PushMarketingUserDetailDTO marketingUserDetailDTO = getPushMarketingUserDetailDTO(custNum, requestTime, cell, taskId);

            pushs.add(marketingUserDetailDTO);
            // 把封装的日志插入到数组中
            logList.add(methodRetryHandlerService.dataJoinLogFix(marketingUserDetailDTO, DistributeTypeEnum.POLICYDATA
                    , toJueCeApiCode, custNum, cell
                    , null, DistributeSourceTypeEnum.TRANSFER, actionType, null));
        }
    }

    private void filterData(List<MarketingTransferSyncUser> list, String apiCode, String tcId,
                            Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum) {
        // 有效期过滤
        Set<String> filterPeriodSet = list.stream().filter(t -> {
            String custNum = t.getCustNum();
            SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = validityPeriodsByCustNum.get(custNum);
            return syncUserValidityPeriodsBO == null;
        }).map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
        list.removeIf(t -> filterPeriodSet.contains(t.getCustNum()));
        log.warn("奇富推送决策,有效期过滤掉的数据量级:{}", filterPeriodSet.size());

        // 规则1过滤：loginTime有值>=有效期生效开始日期
        Set<Long> filterRuleFirst = list.stream().filter(t -> {
            String custNum = t.getCustNum();
            SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = validityPeriodsByCustNum.get(custNum);
            return !QiFuTransferDataUtil.isRuleAssmble(t.getLoginTime(), custNum,
                    syncUserValidityPeriodsBO);
        }).map(MarketingTransferSyncUser::getId).collect(Collectors.toSet());
        list.removeIf(t -> filterRuleFirst.contains(t.getId()));
        log.warn("奇富推送决策,规则1过滤掉的数据量级:{}", filterRuleFirst.size());

        // 规则2过滤：applyDt均为空（包含null、有key无value、未传该记录）
        Set<String> filterRuleSecond = list.stream().filter(t -> {
            String custNum = t.getCustNum();
            SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = validityPeriodsByCustNum.get(custNum);
            List<PeriodRange> periodRangeList = getPeriodRanges(syncUserValidityPeriodsBO);
            int applyDtEmply = marketingTransferSyncUserMapper.getCountByQiFuApplyDtEmply(tcId, apiCode, periodRangeList, custNum);
            return applyDtEmply > 0;
        }).map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
        list.removeIf(t -> filterRuleSecond.contains(t.getCustNum()));
        log.warn("奇富推送决策,规则2过滤掉的数据量级:{}", filterRuleSecond.size());
    }

    private List<PeriodRange> getPeriodRanges(SyncUserValidityPeriodsBO syncUserValidityPeriodsBO) {
        List<PeriodOfValidityBO.Builder> builders = syncUserValidityPeriodsBO.getBuilders();
        List<PeriodRange> periodRangeList = new ArrayList<>();
        for (PeriodOfValidityBO.Builder builder : builders) {
            PeriodOfValidityBO periodOfValidityBO = builder.addDateString().addOfDayTimeStrString().builder();
            String beginDateStr = periodOfValidityBO.getBeginDateStr();
            String enDateStr = periodOfValidityBO.getEnDateStr();

            PeriodRange periodRange = new PeriodRange();
            periodRange.setBeginDateStr(beginDateStr);
            periodRange.setEndDateStr(enDateStr);

            periodRangeList.add(periodRange);
        }
        return periodRangeList;
    }

    private Map<String, SyncUserValidityPeriodsBO> getStringSyncUserValidityPeriodsBOMap(List<MarketingTransferSyncUser> list, String apiCode) {
        // 组装查询有效期方法的custNumSet
        Set<String> custNumSet = list.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
        // 查询在有效期内的数据
        Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum =
                transferDataValidityPeriodService.getValidityPeriodsByCustNumAndTaskId(custNumSet, apiCode, new Date());
        return validityPeriodsByCustNum;
    }

    private PushMarketingUserDetailDTO getPushMarketingUserDetailDTO(String custNum, String requestTime, String cell, String taskId) {
        PushMarketingUserDetailDTO marketingUserDetailDTO = new PushMarketingUserDetailDTO();
        // log解密  md5加密
        String phone = DigestUtils.md5DigestAsHex(
                BrCipherMaker.getInstance().decode(cell).getBytes(StandardCharsets.UTF_8));
        marketingUserDetailDTO.setCaseNumber(custNum);
        marketingUserDetailDTO.setPhone(phone);
        marketingUserDetailDTO.setVariables(variablesInit(requestTime, "1", taskId));
        return marketingUserDetailDTO;
    }

    private PolicyRetryByRuleSoleDTO getPolicyRetryByRuleSoleDTO(String actionType,
                                                                 String apiCodeJc,
                                                                 ArrayList<DataJoinLogDTO> logList,
                                                                 ArrayList<PushMarketingUserDetailDTO> pushs) {
        PolicyRetryByRuleSoleDTO retryByRuleDTO = new PolicyRetryByRuleSoleDTO();
        retryByRuleDTO.setApiCode(apiCodeJc);
        retryByRuleDTO.setBatchNumber(DateFormatUtils.format(new Date(), "yyyyMMdd") + "_" + actionType.toLowerCase() + "_" + apiCodeJc);
        retryByRuleDTO.setData(pushs);
        retryByRuleDTO.setDetailLogList(logList);

        // 手机号维度去重
        retryByRuleDTO.setIsSole(Boolean.TRUE);
        retryByRuleDTO.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        // 单一cell当天仅推送一次
        retryByRuleDTO.setSoleDay(1);

        return retryByRuleDTO;
    }

    private JSONObject variablesInit(String requestTime, String userType, String taskId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("requestTime", requestTime);
        jsonObject.put("userType", userType);
        jsonObject.put("taskId", taskId);
        return jsonObject;
    }
}
