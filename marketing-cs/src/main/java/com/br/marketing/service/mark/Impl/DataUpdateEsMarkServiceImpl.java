package com.br.marketing.service.mark.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.mark.FlagDataEsMark;
import com.br.marketing.entity.FlagDataExample;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.enums.EsSyncStatusEnum;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.MarketingHistoryEsService;
import com.br.marketing.es.util.es.EsHandleUtil;
import com.br.marketing.es.util.es.EsIceType;
import com.br.marketing.es.util.es.rpcclient.RpcClientProxy;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.service.mark.DataMarkCommonService;
import com.br.marketing.service.mark.DataUpdateEsMarkService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName DataUpdateEsMarkServiceImpl
 * @Description pp停车文件数据更新es数据信息实现
 * @Author kongbx
 * @Date 2025/2/19 15:16
 */
@Service
@Slf4j
public class DataUpdateEsMarkServiceImpl implements DataUpdateEsMarkService {
    private final static int PARTITION_SIZE = 2000;

    @Autowired
    RedisChgService redisChgService;
    @Autowired
    private MarketingHistoryEsService marketingHistoryEsService;
    @Resource
    FlagDataMapper flagDataMapper;
    @Resource
    DataMarkCommonService dataMarkCommonService;
    @Resource
    MarketingCommonConfig marketingCommonConfig;
    private static final String TITLE = "【pp停车数据更新es】";

    public static void main(String[] args) {
        long timestamp = System.currentTimeMillis();
        System.out.println("当前时间的时间戳（毫秒）：" + timestamp);
    }
    @Override
    public void process() {
        marketingCommonConfig.getDataMarkApiCodes().forEach((String apiCode) -> {
            StraHisFile straHisFile = dataMarkCommonService.getStraHisFile(apiCode);
            if (null == straHisFile) {
                return;
            }
            Integer threadPoolSize = marketingCommonConfig.getDataMarkThreadNum();
            int dataMarkPageSize = marketingCommonConfig.getDataMarkPageSize() == null?2000:marketingCommonConfig.getDataMarkPageSize();
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);
            String key = RedisKeyConstant.DATA_UPDATE_ES_MARK.concat(":").concat(apiCode);
            while (true) {
                String lockValue = UUID.randomUUID().toString();
                try {
                    redisChgService.lock(key, lockValue);
                    List<FlagDataEsMark> flagDataEsMarkList = flagDataMapper.queryEsMarkByDate(apiCode, LocalDate.now().toString(), dataMarkPageSize);
                    if (CollectionUtil.isEmpty(flagDataEsMarkList)) {
                        redisChgService.unlock(key, lockValue);
                        break;
                    }
                    //更新打标表状态
                    List<Long> ids = flagDataEsMarkList.stream().map(FlagDataEsMark::getId).collect(Collectors.toList());
                    flagDataMapper.batchUpdateEsStatusById(ids, EsSyncStatusEnum.SYNCING.getValue());
                    //释放锁
                    redisChgService.unlock(key, lockValue);

                    List<List<FlagDataEsMark>> partitions = Lists.partition(flagDataEsMarkList, PARTITION_SIZE);
                    for (List<FlagDataEsMark> list : partitions) {
                        threadPool.submit(() -> updateEsMarkData(list,straHisFile));
                    }
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                            TITLE + "抢锁出现异常，" + "errorMessage=" + e.getMessage()), e);
                    redisChgService.unlock(key, lockValue);
                    threadPoolShutDown(threadPool);
                    break;
                }
            }
            threadPoolShutDown(threadPool);
        });
    }

    @RetryMethod(retryNowNum = 3, isOrNoDbRetry = true)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    private void updateEsMarkData(List<FlagDataEsMark> flagDataList, StraHisFile straHisFile) {
        try {
            String index = EsHandleUtil.getDateFromBatchNumber(straHisFile.getBatchNumber());

            List<String> cellLogList = flagDataList.stream().map(FlagDataEsMark::getCellLog).collect(Collectors.toList());
            Map<String, FlagDataEsMark> groupedByCellLog = flagDataList.stream()
                    .collect(Collectors.toMap(FlagDataEsMark::getCellLog, data -> data, (oldValue, newValue) -> newValue));

            // 查询es数据
            JSONObject jsonData = new JSONObject();
            jsonData.put("type", "logic");
            jsonData.put("logic", "and");
            JSONArray data = new JSONArray();
            JSONObject cellCondition = new JSONObject();
            cellCondition.put("type", "operation");
            cellCondition.put("key", "cell");
            cellCondition.put("operation", "in");
            cellCondition.put("value", cellLogList);
            data.add(cellCondition);
            jsonData.put("data", data);
            QueryBaseBean queryBaseBean = new QueryBaseBean();
            queryBaseBean.setApiCode(straHisFile.getApiCode());
            queryBaseBean.setBatchNumbers(straHisFile.getBatchNumber());
            queryBaseBean.setFileIds(String.valueOf(straHisFile.getId()));
            queryBaseBean.setJsonData(jsonData.toJSONString());
            queryBaseBean.setPageSize(2000);
            List<Map<String, MarketingHistory>> marketingHistoryMapList =
                    marketingHistoryEsService.builderMarketingWithIdList(queryBaseBean, null, false);

            if(CollectionUtil.isEmpty(marketingHistoryMapList)){
                log.warn(TITLE+"查询ES数据为空");
                return;
            }
            // 更新es数据
            for (Map<String, MarketingHistory> marketingHistoryMap : marketingHistoryMapList) {
                for (Map.Entry<String, MarketingHistory> entry : marketingHistoryMap.entrySet()) {
                    MarketingHistory marketingHistory = entry.getValue();
                    List<MarketingCondition> marketingConditions = marketingHistory.getCondition();
                    FlagDataEsMark flagData = groupedByCellLog.get(marketingHistory.getCell());
                    buildParams(marketingConditions, flagData);
                    JSONObject params = JSON.parseObject(JSON.toJSONString(marketingHistory));
                    params.put("_id", entry.getKey());
                    RpcClientProxy.modify(index, params, EsIceType.EE.getCode(), EsIceType.R_FALSE.getCode(),
                            EsIceType.MARKETING.getCode());
                }
            }
            //更新打标表状态
            List<Long> ids = flagDataList.stream().map(FlagDataEsMark::getId).collect(Collectors.toList());
            if (!CollectionUtil.isEmpty(ids)) {
                flagDataMapper.batchUpdateEsStatusById(ids, EsSyncStatusEnum.COMPLETE.getValue());
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                    TITLE + "出现异常，" + "errorMessage=" + e.getMessage()), e);
        }
    }

    private void buildParams(List<MarketingCondition> conditions, FlagDataEsMark flagData) {
        List<String> fieldKeys = marketingCommonConfig.getDataMarkField();
        for (String fieldKey : fieldKeys) {
            MarketingCondition condition = new MarketingCondition();
            condition.setFieldKey(fieldKey);
            String value;
            switch (fieldKey) {
                case "flag_new_cust":
                    value = String.valueOf(flagData.getFlagNewCust());
                    break;
                case "flag_riskgroup":
                    value = String.valueOf(flagData.getFlagRiskgroup());
                    break;
                case "flag_interest":
                    value = String.valueOf(flagData.getFlagInterest());
                    break;
                case "flag_age":
                    value = String.valueOf(flagData.getFlagAge());
                    break;
                case "flag_province":
                    value = String.valueOf(flagData.getFlagProvince());
                    break;
                case "flag_special_small":
                    value = String.valueOf(flagData.getFlagSpecialSmall());
                    break;
                case "flag_specialrisklevel_rule":
                    value = String.valueOf(flagData.getFlagSpecialrisklevel());
                    break;
                case "flag_applyloan":
                    value = String.valueOf(flagData.getFlagApplyloan());
                    break;
                case "flag_scoreysbase":
                    value = String.valueOf(flagData.getFlagScorefxsbbaseb());
                    break;
                case "flag_scorefxsbbaseb":
                    value = String.valueOf(flagData.getFlagScorefxsbbaseb());
                    break;
                case "flag_scorescashonregisternologin":
                    value = String.valueOf(flagData.getFlagScorescashonregisternologin());
                    break;
                case "flag_scorescashonyxxy":
                    value = String.valueOf(flagData.getFlagScorescashonyxxy());
                    break;
                case "flag_scorencashonzawswyyym":
                    value = String.valueOf(flagData.getFlagScorencashonzawswyyym());
                    break;
                case "flag_intellaudio_blacklist":
                    value = String.valueOf(flagData.getFlagIntellaudioBlacklist());
                    break;
                case "flag_without_willingness":
                    value = String.valueOf(flagData.getFlagWithoutWillingness());
                    break;
                case "flag_whitelist":
                    value = String.valueOf(flagData.getFlagWhitelist());
                    break;
                default:
                    value = fieldKey;
            }
            condition.setStrValue(value);
            conditions.add(condition);
        }
    }

    private void threadPoolShutDown(ThreadPoolExecutor threadPool) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info(TITLE + "线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                    TITLE + "线程作业，日志保存线程池结束异常！errorMessage=" + ex.getMessage()), ex);
            Thread.currentThread().interrupt();
        }
    }

}
