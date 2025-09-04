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
import com.br.marketing.mapper.ReportStatisticsScoreMapper;
import com.br.marketing.mapper.ScoreDorisLogMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    public void pushDataCallback(String batchNumber, String whereSql) {
        JSONObject haloAiCallbackConfig = marketingCommonConfig.getHaloAiCallbackConfig();
        String apiCode = haloAiCallbackConfig.getString("apiCode");
        boolean retry = StringUtils.isNotBlank(batchNumber);
        if (!retry) {
            batchNumber = scoreDorisLogMapper.selectNewestBatchNumberLogbI_(apiCode);
        }
        if(StringUtils.isBlank(batchNumber)) {
            String errMsg = "哈啰硅基人业务回调，无批次记录数据";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HALUO_SERVICEERROR.getCode(), errMsg));
            return;
        }
        log.warn("哈啰硅基人数据回传调度开始，batchNumber:{}", batchNumber);
        MarketingHaloCallbackRecordExample example = new MarketingHaloCallbackRecordExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andBatchNumberEqualTo(batchNumber);
        List<MarketingHaloCallbackRecord> records = haloCallbackRecordMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(records) && !retry) {
            log.warn("该批次数据已处理完成: {}", batchNumber);
            return;
        }
        saveHaloCallbackRecord(batchNumber, apiCode);

        List<CompletableFuture<Void>> allFutures = Lists.newArrayList();
        int pageSize = haloAiCallbackConfig.getInteger("pageSize");
        long lastId = 0L;
        int threadBatchSize = haloAiCallbackConfig.getInteger("threadBatchSize");

        TpDynamicExecutor executor = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.HALO_CALLBACK_3710212.getName(), 100, 100);
        try {
            while (!Thread.interrupted()) {
                Boolean interrupt = haloAiCallbackConfig.getBoolean("interrupt");
                if (Boolean.TRUE.equals(interrupt)) {
                    log.warn("任务中断触发，停止硅基人回调读取");
                    break;
                }
                String scoreSql = "select id, cell, section, cus_num custNum from b_marketing_score_" + batchNumber +
                        " where 1 = 1 " + whereSql + " and id > " + lastId + " order by id asc limit " + pageSize;
                List<Map<String, Object>> results = reportStatisticsScoreMapper.queryDataMapNumbI_(scoreSql);

                if (CollectionUtils.isEmpty(results)) {
                    break;
                }

                Map<String, Object> lastRecord = results.get(results.size() - 1);
                lastId = ((Number) lastRecord.get("id")).longValue();

                for (List<Map<String, Object>> batchToProcess : Lists.partition(results, threadBatchSize)) {
                    String finalBatchNumber = batchNumber;
                    List<Integer> ids = batchToProcess.stream().map(record -> ((Number) record.get("id")).intValue()).collect(Collectors.toList());
                    allFutures.add(CompletableFuture.runAsync(() -> {
                        try {
                            doProcess(batchToProcess);
                            reportStatisticsScoreMapper.updateStatusbI_("b_marketing_score_" + finalBatchNumber, ids, 1);
                        } catch (Exception e) {
                            reportStatisticsScoreMapper.updateStatusbI_("b_marketing_score_" + finalBatchNumber, ids, 2);
                            String errMsg = "哈啰硅基人业务异常: " + e.getMessage();
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HALUO_SERVICEERROR.getCode(), errMsg));
                        }
                    }, executor));
                }

                if (results.size() < pageSize) {
                    break;
                }
            }
            CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            String errMsg = "哈啰硅基人任务异常" + e.getMessage();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.BI_SERVICEERROR.getCode(), errMsg));
        } finally {
            executor.shutdownAndAwaitTermination();
            haloCallbackRecordMapper.updateStatusByBatchNumber(1, batchNumber);
        }
    }

    private void doProcess(List<Map<String, Object>> submitList) {
        try {
            ReqHaluoApiDTO reqHaluoApiDTO = new ReqHaluoApiDTO();
            List<Map<String, Object>> dataWithoutId = Lists.newArrayList();

            for (Map<String, Object> originalRecord : submitList) {
                Map<String, Object> copyRecord = new HashMap<>();
                for (Map.Entry<String, Object> entry : originalRecord.entrySet()) {
                    if (!"id".equals(entry.getKey())) {
                        copyRecord.put(entry.getKey(), entry.getValue());
                    }
                }
                dataWithoutId.add(copyRecord);
            }

            reqHaluoApiDTO.setData(JSON.toJSONString(dataWithoutId));
            haluoAiApiServiceClient.postHaluoCallbackApi(reqHaluoApiDTO).isSuccess();
        } catch (Exception e) {
            String errMsg = "哈啰硅基人处理数据发生异常: " + e.getMessage();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HALUO_SERVICEERROR.getCode(), errMsg));
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
