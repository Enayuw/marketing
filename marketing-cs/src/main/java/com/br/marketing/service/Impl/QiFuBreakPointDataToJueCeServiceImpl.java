package com.br.marketing.service.Impl;

import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PeriodRange;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.rule.qifu.util.QiFuTransferDataUtil;
import com.br.marketing.service.QiFuBreakPointDataToJueCeService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.ValidityPeriodDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    @Override
    public void doProcess(String param) {
        log.warn("奇富断点自动化数据推决策，JOB开始");
        Set<String> qiFuApiCodes = marketingCommonConfig.getQiFuApiCodes();
        if (!qiFuApiCodes.isEmpty()) {
            qiFuApiCodes.forEach(apiCode -> {
                String tcId = tableCreateService.getTcId(apiCode);

                // requestDate可配置，param样例：2023-08-08,2023-08-28
//                String parameter = context.getJobParameter();
                String startDate;
                String endDate;
                Pair<String, String> validityRange =
                        validityPeriodDataService.getMarketingTransferDataWithValidityRange(apiCode);
                if (null == validityRange) {
                    log.error("奇富：所有配置在有效期配置表中的上传数据均已失效！");
                    return;
                }

                startDate = validityRange.getKey();
                endDate = validityRange.getValue();

                Long indexId = null;

                // 开启线程池
                Integer threadNum =
                        marketingCommonConfig.getQiFuBreakPointDataToJueCeThreadNum();
                ThreadPoolExecutor pool = BrExecutors.getThreadPool(threadNum, threadNum);
                while (true) {
                    List<MarketingTransferSyncUser> marketingTransferSyncUserList = marketingTransferSyncUserMapper.getQiFuBreakPointTransferByRequestDate(tcId, apiCode, startDate, endDate, indexId);
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
            });
        } else {
            log.error("奇富断点自动化数据推决策JOB未配置apiCode");
        }
        log.warn("奇富断点自动化数据推决策，JOB结束");
    }

    private void modifyCorePoolSize(ThreadPoolExecutor pool) {
        Integer threadNum =
                marketingCommonConfig.getZhongYuanTransferDataToDaasAndCustomerFilterThreadNum();
        pool.setCorePoolSize(threadNum);
        pool.setMaximumPoolSize(threadNum);
    }

    private void filterAndPushData(List<MarketingTransferSyncUser> list, String apiCode, String tcId) {
        try {
            // 遍历
            // 有效期精确过滤
            Set<String> custNumSet = list.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum = transferDataValidityPeriodService.getValidityPeriodsByCustNum(custNumSet, apiCode, new Date());
            Set<String> periodCustNumSet = validityPeriodsByCustNum.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet());
            // todo 可能要解决并发问题
            list.removeIf(t -> !periodCustNumSet.contains(t.getCustNum()));
            for (MarketingTransferSyncUser transferSyncUser : list) {
                String custNum = transferSyncUser.getCustNum();
                SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = validityPeriodsByCustNum.get(custNum);
                // loginTime有值>=有效期生效开始日期
                boolean ruleAssmble = QiFuTransferDataUtil.isRuleAssmble(transferSyncUser.getLoginTime(), custNum,
                        syncUserValidityPeriodsBO);
                if (ruleAssmble) {
                    // 遍历每个有效期范围内全量数据，如transformType非1且applyDt有值，则不推送，否则推送决策
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
                    int applyDtEmply = marketingTransferSyncUserMapper.getCountByQiFuApplyDtEmply(tcId, apiCode, periodRangeList, custNum);
                    if (applyDtEmply == 0) {
                        // 推送
                    }

                }

            }

        } catch (Exception e) {
            log.error("奇富断点自动化数据推决策JOB:" + e.getMessage(), e);
        }
    }
}
