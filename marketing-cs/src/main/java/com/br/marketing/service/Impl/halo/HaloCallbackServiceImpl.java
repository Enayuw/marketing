package com.br.marketing.service.Impl.halo;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.br.common.log.AlertLog;
import com.br.marketing.client.halo.HaluoAiApiServiceClient;
import com.br.marketing.client.halo.input.ReqHaluoApiDTO;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.entity.MarketingHaloCallbackRecord;
import com.br.marketing.entity.MarketingHaloCallbackRecordExample;
import com.br.marketing.mapper.MarketingHaloCallbackRecordMapper;
import com.br.marketing.mapper.ScoreDorisLogMapper;
import com.br.marketing.mapper.ReportStatisticsScoreMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author xiong luo
 * @date 2025-09-01 17:57
 */
@Service
@Slf4j
public class HaloCallbackServiceImpl implements IHaloCallbackService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private HaluoAiApiServiceClient haluoAiApiServiceClient;

    @Resource
    private ScoreDorisLogMapper scoreDorisLogMapper;

    @Resource
    private ReportStatisticsScoreMapper reportStatisticsScoreMapper;

    @Resource
    private MarketingHaloCallbackRecordMapper haloCallbackRecordMapper;

    @Override
    public void pushDataCallback(String batchNumber, LocalDate localDate, String whereSql) {
        JSONObject haloAiCallbackConfig = marketingCommonConfig.getHaloAiCallbackConfig();
        String apiCode = haloAiCallbackConfig.getString("apiCode");
        boolean forceCallback = StringUtils.isNotBlank(batchNumber);
        if (!forceCallback) {
            String recordSql = "select batch_number from b_marketing_score_doris_log where api_code = " + apiCode + " and status = 2 order by update_time desc limit 1";
            batchNumber = scoreDorisLogMapper.selectNewestBatchNumberLog(recordSql);
        }
        MarketingHaloCallbackRecordExample example = new MarketingHaloCallbackRecordExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andBatchNumberEqualTo(batchNumber);
        List<MarketingHaloCallbackRecord> records = haloCallbackRecordMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(records) && !forceCallback) {
            log.warn("该批次数据已处理完成: {}", batchNumber);
            return;
        }
        saveHaloCallbackRecord(batchNumber, apiCode);

        TpDynamicExecutor executor = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.HALO_CALLBACK_3710212.getName(), 100, 100);
        List<CompletableFuture<Boolean>> allFutures = Lists.newArrayList();

        int pageSize = haloAiCallbackConfig.getInteger("pageSize");
        long lastId = 0L;
        int threadBatchSize = haloAiCallbackConfig.getInteger("threadBatchSize");

        while (!Thread.interrupted()) {
            String scoreSql = "select id, cell, section from b_marketing_score_" + batchNumber +
                    " where 1 = 1 " + whereSql + " and id > " + lastId + " order by id asc limit " + pageSize;

            List<Map<String, Object>> results = reportStatisticsScoreMapper.queryDataMapNum(scoreSql);

            if (CollectionUtils.isEmpty(results)) {
                break;
            }

            Map<String, Object> lastRecord = results.get(results.size() - 1);
            lastId = ((Number) lastRecord.get("id")).longValue();

            int index = 0;
            while (index < results.size()) {
                int endIndex = Math.min(index + threadBatchSize, results.size());
                List<Map<String, Object>> batchToProcess = results.subList(index, endIndex);
                CompletableFuture<Boolean> batchFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        return doProcess(batchToProcess);
                    } catch (Exception e) {
                        String errMsg = "哈啰硅基人业务异常: " + e.getMessage();
                        log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HALUO_SERVICEERROR.getCode(), errMsg));
                        return false;
                    }
                }, executor).handle((result, e) -> {
                    if (Objects.nonNull(e)) {
                        String errMsg = "哈啰硅基人业务异常: " + e.getMessage();
                        log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HALUO_SERVICEERROR.getCode(), errMsg));
                        return false;
                    }
                    return result;
                });
                allFutures.add(batchFuture);
                index = endIndex;
            }

            if (results.size() < pageSize) {
                break;
            }
        }

        CompletableFuture<Void> allTasks = CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0]));
        CompletableFuture<Boolean> overallSuccessFuture = allTasks.thenApply(v -> {
            for (CompletableFuture<Boolean> future : allFutures) {
                Boolean batchSuccess = future.getNow(false);
                if (!batchSuccess) {
                    return false;
                }
            }
            return true;
        });

        boolean overallSuccess = false;
        try {
            overallSuccess = overallSuccessFuture.get(8, TimeUnit.HOURS);
        } catch (Exception e) {
            String errMsg = "哈啰硅基人业务异常: " + e.getMessage();
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HALUO_SERVICEERROR.getCode(), errMsg));
        }

        if (overallSuccess) {
            haloCallbackRecordMapper.updateStatusByBatchNumber(1, batchNumber);
        } else {
            haloCallbackRecordMapper.updateStatusByBatchNumber(2, batchNumber);
        }
    }

    private boolean doProcess(List<Map<String, Object>> submitList) {
        try {
            ReqHaluoApiDTO reqHaluoApiDTO = new ReqHaluoApiDTO();
            reqHaluoApiDTO.setData(JSON.toJSONString(submitList));
            reqHaluoApiDTO.setMethod("hello.finance.loan.marketing.callback.end");
            return haluoAiApiServiceClient.postHaluoCallbackApi(reqHaluoApiDTO).isSuccess();
        } catch (Exception e) {
            String errMsg = "哈啰硅基人处理数据发生异常: " + e.getMessage();
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HALUO_SERVICEERROR.getCode(), errMsg));
            return false;
        }
    }

    private void saveHaloCallbackRecord(String batchNumber, String apiCode) {
        MarketingHaloCallbackRecord haloCallbackRecord = new MarketingHaloCallbackRecord();
        haloCallbackRecord.setApiCode(apiCode);
        haloCallbackRecord.setBatchNumber(batchNumber);
        haloCallbackRecord.setStatus(0);
        haloCallbackRecordMapper.insertSelective(haloCallbackRecord);
    }
}
