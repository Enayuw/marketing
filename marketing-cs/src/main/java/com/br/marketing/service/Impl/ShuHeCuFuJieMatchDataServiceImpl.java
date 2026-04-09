package com.br.marketing.service.Impl;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.es.bean.MarketingCondition;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import java.util.stream.Collectors;
import javax.annotation.Resource;

import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.ShuHeCuFuJieData;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.MarketingHistoryEsService;
import com.br.marketing.es.util.es.EsIceType;
import com.br.marketing.es.util.es.rpcclient.RpcClientProxy;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.ShuHeCuFuJieDataMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.ShuHeCuFuJieMatchDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.EsNewIndexRuleUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 数禾促复借每日自动化匹配数据相关实现
 *
 * @author senyang.zheng
 * @date 2024/10/21
 */
@Service
@Slf4j
public class ShuHeCuFuJieMatchDataServiceImpl implements ShuHeCuFuJieMatchDataService {

    @Resource
    private ShuHeCuFuJieDataMapper shuHeCuFuJieDataMapper;
    @Resource
    private RedisChgService redisChgService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;
    @Autowired
    private MarketingHistoryEsService marketingHistoryEsService;
    @Resource
    private StraHisFileMapper straHisFileMapper;

    /**
     * @param condition 跑分规则筛选条件
     * @param apiCode apiCode
     * @param date 促复借文件拉取日期
     * @param batchNumber 跑分编号
     * @param forceFlag 强制全量清洗标识
     * @param fieldId 跑分任务主键id
     */
    @Override
    public void matchData(String condition, String apiCode, String date, String batchNumber, Boolean forceFlag, Long fieldId) {
        JSONObject shuHeCuFuJieMatchDataConfig = JSONObject.parseObject(marketingCommonConfig.getShuHeCuFuJieMatchDataConfig());
        Integer threadSize = shuHeCuFuJieMatchDataConfig.getInteger("threadSize");
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadSize, threadSize);
        String redisKey = RedisKeyConstant.SHU_HE_CUFUJIE_MATCH_DATA_FLAG + ":" + date + ":" + batchNumber;
        final StraHisFile straHisFileForEs = fieldId != null ? straHisFileMapper.selectByPrimaryKey(fieldId) : null;
        String index = EsNewIndexRuleUtils.indexForModify(batchNumber, straHisFileForEs, marketingCommonConfig);
        boolean mark = Boolean.TRUE;
        Long minId = StringUtils.isNotEmpty(redisChgService.get(redisKey)) ? Long.valueOf(redisChgService.get(redisKey))
            : shuHeCuFuJieDataMapper.shuHeCuFuJieMatchDataOfMinId(date);
        if (forceFlag) {
            minId = shuHeCuFuJieDataMapper.shuHeCuFuJieMatchDataOfMinId(date);
        }
        if (minId == null) {
            return;
        }
        log.warn("数禾促复借{}自动化匹配数据清洗开始", date);
        minId = minId - 1;
        while (mark) {
            Integer limit = shuHeCuFuJieMatchDataConfig.getInteger("limit");
            List<ShuHeCuFuJieData> shuHeCuFuJieDataList = shuHeCuFuJieDataMapper.shuHeCuFuJieMatchDataByMinId(date, minId, limit);
            if (shuHeCuFuJieDataList.size() <= 0) {
                mark = Boolean.FALSE;
                continue;
            }
            JSONObject updateConfig = JSONObject.parseObject(marketingCommonConfig.getShuHeCuFuJieMatchDataConfig());
            if (updateConfig != null && updateConfig.getBoolean("isPause")) {
                mark = Boolean.FALSE;
                continue;
            }
            minId = shuHeCuFuJieDataList.get(shuHeCuFuJieDataList.size() - 1).getId();
            Integer seconds = DateHelper.getRemainSecondsOneDay(new Date());
            redisChgService.setex(redisKey, String.valueOf(minId), seconds);
            if (updateConfig != null && !threadSize.equals(updateConfig.getInteger("threadSize"))) {
                threadSize = updateConfig.getInteger("threadSize");
                ThreadPoolAdjustmentUtil.adjustThreadPoolSize(threadPool, threadSize);
            }
            threadPool.submit(() -> {
                try {
                    //清洗上传数据
                    List<String> sha256Cells = shuHeCuFuJieDataList.stream().map(ShuHeCuFuJieData::getMobileSha256).collect(Collectors.toList());
                    Map<String, ShuHeCuFuJieData> sha256MobileMap = shuHeCuFuJieDataList.stream()
                                    .collect(Collectors.toMap(ShuHeCuFuJieData::getMobileSha256, data -> data,
                                            (oldValue, newValue) -> newValue));
                    List<MarketingSyncUser> marketingSyncUsers = marketingSyncUserMapper.selectByDynamicCondition(apiCode, sha256Cells, condition);
                    for (MarketingSyncUser marketingSyncUser : marketingSyncUsers) {
                        String custype = "已结清";
                        String sha256Cell = marketingSyncUser.getCellSha256();
                        ShuHeCuFuJieData shuHeCuFuJieData = sha256MobileMap.get(sha256Cell);
                        String reserveField1 = marketingSyncUser.getReserveField1();
                        if (StringUtils.isNotEmpty(reserveField1)) {
                            JSONObject reserveField = JSONObject.parseObject(reserveField1);
                            if (!"0".equals(shuHeCuFuJieData.getAvlLmt()) && (
                                    StringUtils.isNotEmpty(shuHeCuFuJieData.getAdtLmt())
                                            && StringUtils.isNotEmpty(shuHeCuFuJieData.getAvlLmt())
                                    && Long.parseLong(shuHeCuFuJieData.getAdtLmt()) > Long.parseLong(shuHeCuFuJieData.getAvlLmt())
                            )) {
                                custype = "额度未清空";
                            }
                            reserveField.put("custype", custype);
                            marketingSyncUser.setReserveField1(reserveField.toJSONString());
                        }
                        marketingSyncUserMapper.updateReserveFieldByPrimaryKey(marketingSyncUser);
                    }
                    //清洗es
                    List<String> cells = marketingSyncUsers.stream().map(MarketingSyncUser::getCell).collect(Collectors.toList());
                    Map<String, MarketingSyncUser> cellMap =
                            marketingSyncUsers.stream().collect(Collectors.toMap(MarketingSyncUser::getCell, data -> data, (oldValue,
                                                                                                                            newValue) -> newValue));
                    JSONObject jsonData = new JSONObject();
                    jsonData.put("type", "logic");
                    jsonData.put("logic", "and");
                    JSONArray data = new JSONArray();
                    JSONObject cellCondition = new JSONObject();
                    cellCondition.put("type", "operation");
                    cellCondition.put("key", "cell");
                    cellCondition.put("operation", "in");
                    cellCondition.put("value", cells);
                    data.add(cellCondition);
                    jsonData.put("data", data);
                    QueryBaseBean queryBaseBean = new QueryBaseBean();
                    queryBaseBean.setApiCode(apiCode);
                    queryBaseBean.setBatchNumbers(batchNumber);
                    queryBaseBean.setFileIds(String.valueOf(fieldId));
                    queryBaseBean.setJsonData(jsonData.toJSONString());
                    queryBaseBean.setPageSize(2000);
                    queryBaseBean.setUseNewIndexRule(EsNewIndexRuleUtils.resolveAsMap(
                            straHisFileForEs != null ? Collections.singletonList(straHisFileForEs) : null,
                            marketingCommonConfig));
                    List<Map<String, MarketingHistory>> marketingHistoryMapList =
                            marketingHistoryEsService.builderMarketingWithIdList(queryBaseBean, null, false);
                    for (Map<String, MarketingHistory> marketingHistoryMap : marketingHistoryMapList) {
                        for (Map.Entry<String, MarketingHistory> entry : marketingHistoryMap.entrySet()) {
                            MarketingHistory marketingHistory = entry.getValue();
                            List<MarketingCondition> marketingConditions = marketingHistory.getCondition();
                            MarketingSyncUser marketingSyncUser = cellMap.get(marketingHistory.getCell());
                            String custype = JSONObject.parseObject(marketingSyncUser.getReserveField1()).getString("custype");
                            addOrUpdateCustypeCondition(marketingConditions, custype);
                            JSONObject params = JSON.parseObject(JSON.toJSONString(marketingHistory));
                            params.put("_id", entry.getKey());
                            RpcClientProxy.modify(index, params, EsIceType.EE.getCode(), EsIceType.R_FALSE.getCode(),
                                    EsIceType.MARKETING.getCode());
                        }
                    }
                } catch (Exception ex) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SHUHE_SERVICEERROR.getCode(),
                            "数禾促复借每日自动化匹配清洗异常，minId:" + shuHeCuFuJieDataList.get(shuHeCuFuJieDataList.size() - 1).getId()), ex);
                }
            });
        }
    }

    private void addOrUpdateCustypeCondition(List<MarketingCondition> conditions, String custype) {
        boolean isExist = false;
        for (MarketingCondition condition : conditions) {
            if ("custype".equals(condition.getFieldKey())) {
                condition.setStrValue(custype);
                isExist = true;
                break;
            }
        }
        // 如果 "custype" 条件不存在，则添加新的条件
        if (!isExist) {
            MarketingCondition newCondition = new MarketingCondition();
            newCondition.setFieldKey("custype");
            newCondition.setStrValue(custype);
            conditions.add(newCondition);
        }
    }
}
