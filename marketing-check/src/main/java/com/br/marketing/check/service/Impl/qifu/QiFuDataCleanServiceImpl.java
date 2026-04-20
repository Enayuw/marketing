package com.br.marketing.check.service.Impl.qifu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.MarketingHistoryEsService;
import com.br.marketing.es.util.es.EsIceType;
import com.br.marketing.es.util.es.rpcclient.RpcClientProxy;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.QueryUserRealMessageMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.Impl.DynamicParameterServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.EsNewIndexRuleUtils;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @desc 奇富360数据清洗Service
 */
@Slf4j
@Service
public class QiFuDataCleanServiceImpl implements QiFuDataCleanService {

    public static final String Numeric = "^(\\d)+(\\.){1}(\\d)+$|^(\\d)+$";


    @Resource
    private DynamicParameterServiceImpl dynamicParameterServiceImpl;

    @Autowired
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private QueryUserRealMessageMapper queryUserRealMessageMapper;

    @Resource
    MarketingSyncUserMapper marketingSyncUserMapper;


    @Resource
    StraHisFileMapper straHisFileMapper;

    @Autowired
    private MarketingHistoryEsService marketingHistoryEsService;


    @Override
    public void cleanUploadData(String context) {

        String createDate = LocalDate.now().toString();
        String apiCode = "3710139";
        if (StringUtils.isNotBlank(context)) {
            JSONObject paramJson = JSON.parseObject(context);
            createDate = paramJson.getString("createDate");
            apiCode = paramJson.getString("apiCode");
        }
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(5, 5, 20);
        Integer pageSize = dynamicParameterServiceImpl.getPageSize("qiFuUploadDataClean");
        List<Map<String, String>> appletDateAndTypeList = queryUserRealMessageMapper.selectAppletDataByUpload(apiCode, createDate);
        String finalApiCode = apiCode;
        String finalCreateDate = createDate;
        appletDateAndTypeList.forEach(map -> {
            String appletDate = map.get("appletDate");
            String userType = map.get("userType");
            Long indexId = null;
            while (true) {
                Integer threadNum = Integer.valueOf(marketingCommonConfig.getQiFuCleanDataConfig().get("qiFuUploadDataCleanThreadNum"));
                modifyCorePoolSize(pushPool, threadNum);
                // 从数据库读取满足条件的数据
                List<QueryUserRealMessage> list = queryUserRealMessageMapper.selectUserMessageByStatus(finalApiCode, finalCreateDate, 0,
                        null, appletDate, userType, indexId, pageSize);
                if (CollectionUtils.isEmpty(list)) {
                    break;
                }
                indexId = list.get(list.size() - 1).getId();
                // list数据按照500条切割
                List<List<QueryUserRealMessage>> splitList = Lists.partition(list, 500);
                splitList.forEach((List<QueryUserRealMessage> userRealMessageList) ->
                        pushPool.submit(() -> uploadDataUpdate(userRealMessageList, appletDate, userType))
                );
            }
        });
        pushPool.shutdown();
        try {
            while (!pushPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                // do nothing
            }
        } catch (InterruptedException e) {
            log.error("奇富360-奇富促动支更新上传数据-线程池中断异常-", e);
            pushPool.shutdownNow();
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("奇富360-奇富促动支更新上传数据-线程池停止异常-", e);
            pushPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

    }

    private void uploadDataUpdate(List<QueryUserRealMessage> userRealMessageList, String appletDate, String userType) {

        try {
            Map<String, List<QueryUserRealMessage>> syncuserList = userRealMessageList.stream().collect(Collectors.groupingBy(QueryUserRealMessage::getCell));
            Set<String> cellSet = userRealMessageList.stream().map(QueryUserRealMessage::getCell).collect(Collectors.toSet());

            String apiCode = userRealMessageList.get(0).getApiCode();
            List<MarketingSyncUser> marketingSyncUserList = marketingSyncUserMapper.getSyncUserByCells(apiCode, cellSet, appletDate, userType);
            StringBuilder update = new StringBuilder(String.format("UPDATE b_marketing_sync_%s SET reserve_field1 = CASE id ", apiCode));
            List<Long> ids = new ArrayList<>();
            for (MarketingSyncUser sync : marketingSyncUserList) {
                QueryUserRealMessage queryUserRealMessage = syncuserList.get(sync.getCell()).get(0);
                JSONObject userMessages = JSONObject.parseObject(queryUserRealMessage.getUserMessage());
                String gender = userMessages.getString("sex");
                if (StringUtils.isNotEmpty(gender)) {
                    gender = "M".equals(gender) ? "1" : "0";

                }
                JSONObject tradeMsaages = JSONObject.parseObject(queryUserRealMessage.getTradeMessage());
                Integer curAvailableQuota = tradeMsaages.getInteger("curAvailableQuota");
                String curAvailableQuotays_derived = null;
                if (!Objects.isNull(curAvailableQuota)) {
                    curAvailableQuotays_derived = String.valueOf((curAvailableQuota * 3000 - 3000));
                }

                JSONObject jsonObject = JSON.parseObject(sync.getReserveField1());
                jsonObject.put("cusName", userMessages.getString("name"));
                jsonObject.put("gender", gender);
                jsonObject.put("curAvailableQuotays_derived", curAvailableQuotays_derived);

                String jsonString = jsonObject.toJSONString();
                update.append("WHEN ").append(sync.getId()).append(" THEN '").append(jsonString).append("' ");
                ids.add(sync.getId());
            }
            if (ids.size() <= 0) {
                return;
            }
            update.append("END WHERE id IN (");
            update.append(org.apache.commons.lang3.StringUtils.join(ids, ","));
            update.append(");");
            marketingSyncUserMapper.updateBatchData(update.toString());
            // 根据响应结果更新数据库数据表-status成功
            List<Long> idList = userRealMessageList.stream().map(QueryUserRealMessage::getId).collect(Collectors.toList());
            queryUserRealMessageMapper.updateUploadStatusByIdList(2, idList);
        } catch (Exception e) {
            log.error("奇富促动支更新上传数据异常", e);

        }
    }

    private void modifyCorePoolSize(ThreadPoolExecutor pool, Integer threadNum) {
        if (!Objects.isNull(threadNum)) {
            ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, threadNum);
        }
        log.warn("奇富促动支清洗线程数core={}，max={}", pool.getCorePoolSize(), pool.getMaximumPoolSize());
    }

    @Override
    public void cleanESData(String context) {

        String createDate = LocalDate.now().toString();
        String apiCode = "3710139";
        if (StringUtils.isNotBlank(context)) {
            JSONObject paramJson = JSON.parseObject(context);
            createDate = paramJson.getString("createDate");
            apiCode = paramJson.getString("apiCode");
        }
        ThreadPoolExecutor pushPool = BrExecutors.getThreadPool(40, 40, 50);

        Integer pageSize = dynamicParameterServiceImpl.getPageSize("qiFuEsDataClean");

        List<Map<String, String>> appletDateAndTypeList = queryUserRealMessageMapper.selectAppletDataAndType(apiCode, createDate);
        String batchNumber = marketingCommonConfig.getQiFuCleanDataConfig().get("qiFuEsDataCleanBatchNumber");

        String finalApiCode = apiCode;
        String finalCreateDate = createDate;
        appletDateAndTypeList.forEach(map -> {
            String appletDate = map.get("appletDate");
            String userType = map.get("userType");
            String condition = "{\"fieldName\":\"appletDate\",\"fieldValue\":\"".concat(appletDate).concat("\",\"operation\":\"=\"},{\"fieldName\":\"userType\",\"fieldValue\":\"")
                    .concat(userType).concat("\",\"operation\":\"=\"}");
            StraHisFile straHisFile = null;
            //batchNumber配置了，取配置
            if (StringUtils.isNotEmpty(batchNumber)) {
                straHisFile = straHisFileMapper.getStFileByBatchNumber(batchNumber);
            } else {
                straHisFile = straHisFileMapper.getTaskbyDataContion(finalApiCode, condition);
            }
            if (Objects.isNull(straHisFile)) {
                log.warn("奇富促动支跑分记录未找到，appletDate={}，userType={}", appletDate, userType);
                return;
            }
            log.warn("奇富促动支更新Es数据batchNumber={}", straHisFile.getBatchNumber());
            Long indexId = null;
            while (true) {
                Integer threadNum = Integer.valueOf(marketingCommonConfig.getQiFuCleanDataConfig().get("qiFuEsDataCleanThreadNum"));
                modifyCorePoolSize(pushPool, threadNum);

                // 从数据库读取满足条件的数据
                List<QueryUserRealMessage> list = queryUserRealMessageMapper.selectUserMessageByStatus(finalApiCode, finalCreateDate, null,
                        0, appletDate, userType, indexId, pageSize);
                if (CollectionUtils.isEmpty(list)) {
                    break;
                }
                indexId = list.get(list.size() - 1).getId();
                // list数据按照500条切割
                List<List<QueryUserRealMessage>> splitList = Lists.partition(list, 500);
                StraHisFile finalStraHisFile = straHisFile;
                splitList.forEach((List<QueryUserRealMessage> userRealMessageList) ->
                        pushPool.submit(() -> esDataUpdate(userRealMessageList, finalStraHisFile)
                        ));
            }
        });
        pushPool.shutdown();
        try {
            while (!pushPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                // do nothing
            }
        } catch (InterruptedException e) {
            log.error("奇富360-奇富促动支更新Es数据-线程池中断异常-", e);
            pushPool.shutdownNow();
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("奇富360-奇富促动支更新Es数据-线程池停止异常-", e);
            pushPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

    }

    private void esDataUpdate(List<QueryUserRealMessage> userRealMessageList, StraHisFile straHisFile) {

        try {
            //清洗es 存入log加密的cell
            String apiCode = userRealMessageList.get(0).getApiCode();
            String batchNumber = straHisFile.getBatchNumber();
            String index = EsNewIndexRuleUtils.indexForModify(batchNumber, straHisFile, marketingCommonConfig);
            List<String> cells = userRealMessageList.stream().map(QueryUserRealMessage::getCell).collect(Collectors.toList());
            Map<String, List<QueryUserRealMessage>> userRealMessageMap = userRealMessageList.stream().collect(Collectors.groupingBy(QueryUserRealMessage::getCell));
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
            queryBaseBean.setFileIds(String.valueOf(straHisFile.getId()));
            queryBaseBean.setJsonData(jsonData.toJSONString());
            queryBaseBean.setPageSize(2000);
            queryBaseBean.setUseNewIndexRule(EsNewIndexRuleUtils.resolveAsMap(Collections.singletonList(straHisFile), marketingCommonConfig));
            List<Map<String, MarketingHistory>> marketingHistoryMapList =
                    marketingHistoryEsService.builderMarketingWithIdList(queryBaseBean, null, false);
            log.warn("奇富促动支ES中查询的数据量级为num={}", marketingHistoryMapList.size());
            for (Map<String, MarketingHistory> marketingHistoryMap : marketingHistoryMapList) {
                for (Map.Entry<String, MarketingHistory> entry : marketingHistoryMap.entrySet()) {
                    MarketingHistory marketingHistory = entry.getValue();
                    List<MarketingCondition> marketingConditions = marketingHistory.getCondition();
                    updateCondition(marketingConditions, userRealMessageMap, marketingHistory);
                    JSONObject params = JSON.parseObject(JSON.toJSONString(marketingHistory));
                    params.put("_id", entry.getKey());
                    RpcClientProxy.modify(index, params, EsIceType.EE.getCode(), EsIceType.R_FALSE.getCode(),
                            EsIceType.MARKETING.getCode());
                }
            }
            // 根据响应结果更新数据库数据表-status成功
            List<Long> idList = userRealMessageList.stream().map(QueryUserRealMessage::getId).collect(Collectors.toList());
            queryUserRealMessageMapper.updateEsStatusByIdList(2, idList);
        } catch (Exception e) {
            log.error("奇富促动支更新Es数据异常", e);

        }


    }

    private void updateCondition(List<MarketingCondition> conditions, Map<String, List<QueryUserRealMessage>> userRealMessageMap, MarketingHistory marketingHistory) {

        List<QueryUserRealMessage> marketingSyncList = userRealMessageMap.get(marketingHistory.getCell());
        if (CollectionUtils.isEmpty(marketingSyncList)) {
            log.warn("ES和上传数据不匹配，cell={}", marketingHistory.getCell());
            return;
        }
        QueryUserRealMessage queryUserRealMessage = marketingSyncList.get(0);
        JSONObject tradeMsaages = JSONObject.parseObject(queryUserRealMessage.getTradeMessage());
        Integer curAvailableQuota = tradeMsaages.getInteger("curAvailableQuota");
        String curAvailableQuotays_derived = null;
        if (!Objects.isNull(curAvailableQuota)) {
            curAvailableQuotays_derived = String.valueOf((curAvailableQuota * 3000 - 3000));
        }
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(curAvailableQuotays_derived)) {
            addOrUpdateCustypeCondition(conditions, "curAvailableQuotays_derived", curAvailableQuotays_derived);
        }
        JSONObject userMessages = JSONObject.parseObject(queryUserRealMessage.getUserMessage());
        String gender = userMessages.getString("sex");
        if (StringUtils.isNotEmpty(gender)) {
            gender = "M".equals(gender) ? "1" : "0";
        }
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(gender)) {
            addOrUpdateCustypeCondition(conditions, "gender", gender);
        }


        String name = userMessages.getString("name");
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(name)) {
            addOrUpdateCustypeCondition(conditions, "cusName", name);
        }

    }


    private void addOrUpdateCustypeCondition(List<MarketingCondition> conditions, String key, String s) {
        boolean isExist = false;
        for (MarketingCondition condition : conditions) {
            if (key.equals(condition.getFieldKey())) {
                condition.setStrValue(s);
                if (Pattern.compile(Numeric).matcher(s).matches()) {
                    condition.setDValue(Double.valueOf(s));

                }
                isExist = true;
                break;
            }
        }
        // 如果 "key" 条件不存在，则添加新的条件
        if (!isExist) {
            MarketingCondition newCondition = new MarketingCondition();
            newCondition.setFieldKey(key);
            newCondition.setStrValue(s);
            if (Pattern.compile(Numeric).matcher(s).matches()) {
                newCondition.setDValue(Double.valueOf(s));

            }
            conditions.add(newCondition);
        }
    }


}
