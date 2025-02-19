package com.br.marketing.service.mark.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.FlagData;
import com.br.marketing.entity.FlagDataExample;
import com.br.marketing.enums.EsSyncStatusEnum;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.MarketingHistoryEsService;
import com.br.marketing.es.util.es.EsHandleUtil;
import com.br.marketing.es.util.es.EsIceType;
import com.br.marketing.es.util.es.rpcclient.RpcClientProxy;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.service.mark.DataUpdateEsMarkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    @Autowired
    RedisChgService redisChgService;
    @Resource
    FlagDataMapper flagDataMapper;
    @Autowired
    private MarketingHistoryEsService marketingHistoryEsService;
    private static final String TITLE = "【pp停车数据更新es】";

    @Override
    public void process() {

        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(5, 5);
        Long minId = null;
        boolean isContiue = Boolean.TRUE;
        while (isContiue) {
            // 分页查询打标数据
            FlagDataExample flagDataExample = new FlagDataExample();
            flagDataExample.setOrderByClause("id limit 2000");

            FlagDataExample.Criteria criteria = flagDataExample.createCriteria()
                    .andApiCodeEqualTo("7410717")
                    .andCreateDateEqualTo(1)
                    .andFlagNewCustComputationEqualTo(1)
                    .andFlagCustomerBaseComputationEqualTo(1)
                    .andFlagHighRiskComputationEqualTo(1)
                    .andFlagBlacklistComputationEqualTo(1)
                    .andFlagWhitelistComputationEqualTo(1)
                    .andEsSyncStatusEqualTo(EsSyncStatusEnum.INITIAL.getValue());
            // 找到最新文件 batchNumber+fieldId
            if (minId != null) {
                criteria.andIdGreaterThan(minId);
            }
            List<FlagData> flagDataList = flagDataMapper.selectByExample(flagDataExample);
            if (CollectionUtil.isEmpty(flagDataList)) {
                isContiue = Boolean.FALSE;
                continue;
            }
            minId = flagDataList.get(flagDataList.size() - 1).getId();
            // 更新ES
            threadPool.submit(() -> updateEsMarkData(flagDataList));
        }
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn(TITLE + "线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ES_RETRY_DATAERROR.getCode(), TITLE + "线程池关闭！异常"), ex);
            Thread.currentThread().interrupt();
        }
    }

    private void updateEsMarkData(List<FlagData> flagDataList) {
        try {
            String index = EsHandleUtil.getDateFromBatchNumber("batchNumber");
            List<String> cellLogList = flagDataList.stream().map(FlagData::getCellLog).collect(Collectors.toList());
            Map<String, FlagData> groupedByCellLog = flagDataList.stream()
                    .collect(Collectors.toMap(FlagData::getCellLog, data -> data, (oldValue, newValue) -> newValue));
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
            queryBaseBean.setApiCode("7410717");
            queryBaseBean.setBatchNumbers("batchNumber");
            queryBaseBean.setFileIds(String.valueOf("fieldId"));
            queryBaseBean.setJsonData(jsonData.toJSONString());
            queryBaseBean.setPageSize(2000);
            List<Map<String, MarketingHistory>> marketingHistoryMapList =
                    marketingHistoryEsService.builderMarketingWithIdList(queryBaseBean, null, false);
            // 更新es数据
            for (Map<String, MarketingHistory> marketingHistoryMap : marketingHistoryMapList) {
                for (Map.Entry<String, MarketingHistory> entry : marketingHistoryMap.entrySet()) {
                    MarketingHistory marketingHistory = entry.getValue();
                    List<MarketingCondition> marketingConditions = marketingHistory.getCondition();
                    FlagData flagData = groupedByCellLog.get(marketingHistory.getCell());
                    buildParams(marketingConditions,flagData);
                    JSONObject params = JSON.parseObject(JSON.toJSONString(marketingHistory));
                    params.put("_id", entry.getKey());
                    RpcClientProxy.modify(index, params, EsIceType.EE.getCode(), EsIceType.R_FALSE.getCode(),
                            EsIceType.MARKETING.getCode());
                }
            }
        }catch (Exception e){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    TITLE+"出现异常，" + "errorMessage=" + e.getMessage()), e);
        }
    }

    private void buildParams(List<MarketingCondition> conditions, FlagData flagData) {
        List<String> fieldKeys = Arrays.asList(
                "flagNewCust", "flagRiskgroup", "flagInterest", "flagAge",
                "flagProvince", "flagSpecialSmall", "flagSpecialrisklevelRule", "flagIndexcs",
                "flagApplyloan", "flagIntellaudioBlacklist", "flagWithoutWillingness",
                "flagScoreWhitelist", "flagWhitelist"
        );
        for (String fieldKey : fieldKeys) {
            MarketingCondition condition = new MarketingCondition();
            condition.setFieldKey(fieldKey);
            String value;
            switch (fieldKey) {
                case "flagNewCust":
                    value = String.valueOf(flagData.getFlagNewCust());
                    break;
                case "flagRiskgroup":
                    value = String.valueOf(flagData.getFlagRiskgroup());
                    break;
                case "flagInterest":
                    value = String.valueOf(flagData.getFlagInterest());
                    break;
                case "flagAge":
                    value = String.valueOf(flagData.getFlagAge());
                    break;
                case "flagProvince":
                    value = String.valueOf(flagData.getFlagProvince());
                    break;
                case "flagSpecialSmall":
                    value = String.valueOf(flagData.getFlagSpecialSmall());
                    break;
                case "flagSpecialrisklevelRule":
                    value = String.valueOf(flagData.getFlagSpecialrisklevelRule());
                    break;
                case "flagIndexcs":
                    value = String.valueOf(flagData.getFlagIndexcs());
                    break;
                case "flagApplyloan":
                    value = String.valueOf(flagData.getFlagApplyloan());
                    break;
                case "flagIntellaudioBlacklist":
                    value = String.valueOf(flagData.getFlagIntellaudioBlacklist());
                    break;
                case "flagWithoutWillingness":
                    value = String.valueOf(flagData.getFlagWithoutWillingness());
                    break;
                case "flagScoreWhitelist":
                    value = String.valueOf(flagData.getFlagScoreWhitelist());
                    break;
                case "flagWhitelist":
                    value = String.valueOf(flagData.getFlagWhitelist());
                    break;
                default:
                    value = fieldKey;
            }
            condition.setStrValue(value);
            conditions.add(condition);
        }
    }

}
