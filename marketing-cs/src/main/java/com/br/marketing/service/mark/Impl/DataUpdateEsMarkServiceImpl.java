package com.br.marketing.service.mark.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.mark.FlagDataEsMark;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.enums.EsSyncStatusEnum;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.MarketingHistoryEsService;
import com.br.marketing.es.util.es.EsIceType;
import com.br.marketing.es.util.es.rpcclient.RpcClientProxy;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.service.mark.DataMarkCommonService;
import com.br.marketing.service.mark.DataUpdateEsMarkService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.EsNewIndexRuleUtils;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
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
    private final static int PARTITION_SIZE = 1500;

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
    private static final String fieldKeys = "dt_whitelist,flag_new_cust,flag_riskgroup,flag_interest,flag_age,flag_province," +
            "flag_special_small,flag_specialrisklevel_rule,flag_applyloan,flag_scoreysbase,flag_scorefxsbbaseb," +
            "flag_scorescashonregisternologin,flag_scorescashonyxxy,flag_scorencashonzawswyyym," +
            "flag_intellaudio_blacklist,flag_without_willingness,flag_whitelist";


    @Override
    public void process(String scoreDate) {
        marketingCommonConfig.getDataMarkApiCodes().forEach((String apiCode) -> {
            StraHisFile straHisFile = dataMarkCommonService.getStraHisFile(apiCode, scoreDate);
            if (null == straHisFile) {
                return;
            }
            Integer threadPoolSize = marketingCommonConfig.getDataMarkESThreadNum();
            Integer threadUpdatePoolSize = marketingCommonConfig.getDataUpdateMarkESThreadNum();
            Integer dataMarkEsQueueNum = marketingCommonConfig.getDataMarkEsQueueNum();
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize, dataMarkEsQueueNum);
            ThreadPoolExecutor threadUpdatePool = BrExecutors.getThreadPool(threadUpdatePoolSize, threadUpdatePoolSize, dataMarkEsQueueNum);
            List<Long> ids = new ArrayList<>();
            while (true) {
                try {
                    Integer newThreadPoolSize = marketingCommonConfig.getDataMarkESThreadNum();
                    if (!newThreadPoolSize.equals(threadPoolSize)) {
                        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(threadPool, newThreadPoolSize);
                        log.warn(TITLE + "查询线程池大小已动态调整为: {}", newThreadPoolSize);
                    }
                    Integer newThreadUpdatePoolSize = marketingCommonConfig.getDataUpdateMarkESThreadNum();
                    if (!newThreadUpdatePoolSize.equals(threadUpdatePoolSize)) {
                        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(threadUpdatePool, newThreadUpdatePoolSize);
                        log.warn(TITLE + "写入线程池大小已动态调整为: {}", newThreadUpdatePoolSize);
                    }

                    int dataMarkPageSize = marketingCommonConfig.getDataMarkPageSize() == null?2000:marketingCommonConfig.getDataMarkPageSize();
                    List<FlagDataEsMark> flagDataEsMarkList = flagDataMapper.queryEsMarkByDate(apiCode, LocalDate.now().toString(), dataMarkPageSize);
                    if (CollectionUtil.isEmpty(flagDataEsMarkList)) {
                        threadPoolShutDown(threadPool);
                        threadPoolShutDown(threadUpdatePool);
                        break;
                    }
                    //更新打标表状态
                    ids = flagDataEsMarkList.stream().map(FlagDataEsMark::getId).collect(Collectors.toList());
                    flagDataMapper.batchUpdateEsStatusById(ids, EsSyncStatusEnum.SYNCING.getValue());

                    List<List<FlagDataEsMark>> partitions = Lists.partition(flagDataEsMarkList, PARTITION_SIZE);
                    for (List<FlagDataEsMark> list : partitions) {
                        threadPool.submit(() -> updateEsMarkData(list,straHisFile,threadUpdatePool));
                    }
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                            TITLE + "更新打标表状态出现异常，" + "errorMessage=" + e.getMessage()), e);
                    if(!CollectionUtil.isEmpty(ids)){
                        flagDataMapper.batchUpdateEsStatusById(ids, EsSyncStatusEnum.INITIAL.getValue());
                    }
                    threadPoolShutDown(threadPool);
                    threadPoolShutDown(threadUpdatePool);
                    break;
                }
            }
            threadPoolShutDown(threadPool);
            threadPoolShutDown(threadUpdatePool);
        });
    }

    private void updateEsMarkData(List<FlagDataEsMark> flagDataList, StraHisFile straHisFile, ThreadPoolExecutor threadUpdatePool) {
        List<Long> ids = flagDataList.stream().map(FlagDataEsMark::getId).collect(Collectors.toList());
        try {
            String index = EsNewIndexRuleUtils.indexForModify(straHisFile.getBatchNumber(), straHisFile, marketingCommonConfig);

            List<String> cellLogList = flagDataList.stream().map(FlagDataEsMark::getCellLog).collect(Collectors.toList());
            Map<String, FlagDataEsMark> groupedByCellLog = flagDataList.stream()
                    .collect(Collectors.toMap(FlagDataEsMark::getCellLog, data -> data, (oldValue, newValue) -> newValue));

            // 构造查询条件
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
            queryBaseBean.setUseNewIndexRule(EsNewIndexRuleUtils.resolveAsMap(Collections.singletonList(straHisFile), marketingCommonConfig));

            // 查询 Elasticsearch 数据
            List<Map<String, MarketingHistory>> marketingHistoryMapList =
                    marketingHistoryEsService.builderMarketingWithIdList(queryBaseBean, null, false);
            if (CollectionUtil.isEmpty(marketingHistoryMapList)) {
                log.warn(TITLE + "查询ES数据为空,batchNumber："+straHisFile.getBatchNumber());
                return;
            }

            // 提交任务到线程池
            threadUpdatePool.submit(() -> {
                try {
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
                    // 更新状态为 COMPLETE
                    updateEsStatus(ids, EsSyncStatusEnum.COMPLETE);
                } catch (Exception e) {
                    log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                            TITLE + "更新ES数据异常，errorMessage=" + e.getMessage()), e);
                    updateEsStatus(ids, EsSyncStatusEnum.INITIAL);
                }
            });
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                    TITLE + "处理异常，errorMessage=" + e.getMessage()), e);
            updateEsStatus(ids, EsSyncStatusEnum.INITIAL);
        }
    }

    // 提取状态更新方法
    private void updateEsStatus(List<Long> ids, EsSyncStatusEnum status) {
        if (!CollectionUtil.isEmpty(ids)) {
            flagDataMapper.batchUpdateEsStatusById(ids, status.getValue());
        }
    }

    private void buildParams(List<MarketingCondition> conditions, FlagDataEsMark flagData) {
        String[] split = fieldKeys.split(",");
        Map<String, MarketingCondition> map = conditions.stream()
                .collect(Collectors.toMap(MarketingCondition::getFieldKey, data -> data, (oldValue, newValue) -> newValue));
        for (String fieldKey : split) {
            String value = "";
            switch (fieldKey.trim()) {
                case "dt_whitelist":
                    value = flagData.getDtWhitelist()== null?"": new SimpleDateFormat("yyyy-MM-dd").format(flagData.getDtWhitelist());
                    break;
                case "flag_new_cust":
                    value = flagData.getFlagNewCust()== null?"":String.valueOf(flagData.getFlagNewCust());
                    break;
                case "flag_riskgroup":
                    value = flagData.getFlagRiskgroup()== null?"": flagData.getFlagRiskgroup();
                    break;
                case "flag_interest":
                    value = flagData.getFlagInterest()== null?"":String.valueOf(flagData.getFlagInterest());
                    break;
                case "flag_age":
                    value = flagData.getFlagAge()== null?"":String.valueOf(flagData.getFlagAge());
                    break;
                case "flag_province":
                    value = flagData.getFlagProvince()== null?"":String.valueOf(flagData.getFlagProvince());
                    break;
                case "flag_special_small":
                    value = flagData.getFlagSpecialSmall()== null?"":String.valueOf(flagData.getFlagSpecialSmall());
                    break;
                case "flag_specialrisklevel_rule":
                    value = flagData.getFlagSpecialrisklevelRule()== null?"":String.valueOf(flagData.getFlagSpecialrisklevelRule());
                    break;
                case "flag_applyloan":
                    value = flagData.getFlagApplyloan()== null?"":String.valueOf(flagData.getFlagApplyloan());
                    break;
                case "flag_scoreysbase":
                    value = flagData.getFlagScoreysbase()== null?"":String.valueOf(flagData.getFlagScoreysbase());
                    break;
                case "flag_scorefxsbbaseb":
                    value = flagData.getFlagScorefxsbbaseb()== null?"":String.valueOf(flagData.getFlagScorefxsbbaseb());
                    break;
                case "flag_scorescashonregisternologin":
                    value = flagData.getFlagScorescashonregisternologin()== null?"":String.valueOf(flagData.getFlagScorescashonregisternologin());
                    break;
                case "flag_scorescashonyxxy":
                    value = flagData.getFlagScorescashonyxxy()== null?"":String.valueOf(flagData.getFlagScorescashonyxxy());
                    break;
                case "flag_scorencashonzawswyyym":
                    value = flagData.getFlagScorencashonzawswyyym()== null?"":String.valueOf(flagData.getFlagScorencashonzawswyyym());
                    break;
                case "flag_intellaudio_blacklist":
                    value = flagData.getFlagIntellaudioBlacklist()== null?"":String.valueOf(flagData.getFlagIntellaudioBlacklist());
                    break;
                case "flag_without_willingness":
                    value = flagData.getFlagWithoutWillingness()== null?"":String.valueOf(flagData.getFlagWithoutWillingness());
                    break;
                case "flag_whitelist":
                    value = flagData.getFlagWhitelist()== null?"":String.valueOf(flagData.getFlagWhitelist());
                    break;
            }
            MarketingCondition marketingCondition = map.get(fieldKey);
            if(null != marketingCondition){
                marketingCondition.setStrValue(value);
            }else {
                MarketingCondition condition = new MarketingCondition();
                condition.setFieldKey(fieldKey);
                condition.setStrValue(value);
                conditions.add(condition);
            }
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
