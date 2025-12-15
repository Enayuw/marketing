package com.br.marketing.check.service.Impl.qifu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.check.service.qifu.QiFuDataFlattenService;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.BQifuUploadDataOriginal;
import com.br.marketing.entity.DrsCustomizeUploadData;
import com.br.marketing.mapper.BQifuUploadDataOriginalMapper;
import com.br.marketing.mapper.DrsCustomizeUploadDataMapper;
import com.br.marketing.service.Impl.qifu.enums.QiFuDataTypeEnum;
import com.br.marketing.service.Impl.qifu.enums.QiFuSelectStatusEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 奇富定制前置表数据打平Service实现
 */
@Slf4j
@Service
public class QiFuDataFlattenServiceImpl implements QiFuDataFlattenService {

    /**
     * 批次大小常量
     */
    private static final int BATCH_SIZE = 2000;

    /**
     * 线程数常量
     */
    private static final int THREAD_NUM = 10;

    /**
     * 分页大小常量
     */
    private static final int PAGE_SIZE = 100;

    @Resource
    private DrsCustomizeUploadDataMapper drsCustomizeUploadDataMapper;

    @Resource
    private BQifuUploadDataOriginalMapper bQifuUploadDataOriginalMapper;

    @Resource
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    private TrackingService trackingService;

    @Override
    public void flattenDataProcess() {
        LocalDate currentDate = LocalDate.now();
        JSONObject qifuAiCleanConfig = marketingCommonConfig.getQifuAiCleanConfig();
        String tcId = getValueOfJson(qifuAiCleanConfig, "tCid", "");
        String date = getValueOfJson(qifuAiCleanConfig, "onlineDate", "");
        List<String> apiCodes = Arrays.asList(getValueOfJson(qifuAiCleanConfig, "cleanApiCode", "3700226").split(","));
        LocalDate onlineDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        if (currentDate.isBefore(onlineDate) || currentDate.isEqual(onlineDate)) {
            // 当前日期小于等于上线日，执行历史数据打平
            log.warn("当前日期 {} 小于等于上线日 {}，执行历史数据打平", currentDate, onlineDate);
            flattenHistoryData(tcId, apiCodes, onlineDate.toString());
        } else {
            // 当前日期大于上线日，执行实时打平
            log.warn("当前日期 {} 大于上线日 {}，执行实时数据打平", currentDate, onlineDate);
            flattenRealtimeData(tcId, apiCodes);
        }

    }

    /**
     * 历史数据打平逻辑
     */
    private void flattenHistoryData(String tcId, List<String> apiCodes, String onlineDate) {
        Map<String, Long> idRange = drsCustomizeUploadDataMapper.getDataIdRange(tcId, apiCodes, "history", onlineDate);
        if (idRange == null || idRange.get("minId") == null || idRange.get("maxId") == null) {
            log.warn("历史数据id范围查询为空，无需打平");
            return;
        }

        Long minId = idRange.get("minId");
        Long maxId = idRange.get("maxId");
        log.warn("历史数据id范围：minId={}, maxId={}", minId, maxId);

        // 使用通用方法处理数据打平
        processDataWithMultiThread(tcId, apiCodes, minId, maxId, "历史数据", "qifuFlattenHistory", "history", onlineDate);
    }

    /**
     * 实时数据打平逻辑
     */
    private void flattenRealtimeData(String tcId, List<String> apiCodes) {
        String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        Map<String, Long> idRange = drsCustomizeUploadDataMapper.getDataIdRange(tcId, apiCodes,"today", todayDate);
        if (idRange == null || idRange.get("minId") == null || idRange.get("maxId") == null) {
            log.warn("今日数据id范围查询为空，无需打平");
            return;
        }

        Long minId = idRange.get("minId");
        Long maxId = idRange.get("maxId");
        log.warn("今日数据id范围：minId={}, maxId={}", minId, maxId);

        try {
            String remark = String.format("奇富定制前置表数据打平, minId：%d, maxId：%d, 注意：%s"
                    , minId, maxId, "量级不准确!");
            trackingService.trackPointLog(DataFlowDirection.OUT
                    , apiCodes.get(0)
                    , "奇富定制前置表数据打平"
                    , 1L
                    , remark
                    , TrackingContext.generateBatchId());
        } catch (Exception ex) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                            , ex.getMessage()
                            , "埋点异常")
                    , ex);
        }

        // 使用通用方法处理数据打平
        processDataWithMultiThread(tcId, apiCodes, minId, maxId, "今日数据", "qifuFlattenRealtime", "today", todayDate);
    }

    /**
     * 通用方法：多线程分批处理数据打平
     *
     * @param minId 最小id
     * @param maxId 最大id
     * @param dataType 数据类型描述（用于日志）
     * @param threadPoolName 线程池名称
     * @param dateType 日期类型：history表示<=，today表示=
     * @param dateValue 日期值
     */
    private void processDataWithMultiThread(String tcId, List<String> apiCodes, long minId, long maxId, String dataType, String threadPoolName,
                                            String dateType, String dateValue) {
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(THREAD_NUM, THREAD_NUM, threadPoolName, 200);
        long currentMinId = minId;

        // 按id范围分批处理
        while (currentMinId <= maxId) {
            long currentMaxId = Math.min(currentMinId + BATCH_SIZE - 1, maxId);
            final long finalMinId = currentMinId;
            final long finalMaxId = currentMaxId;

            threadPool.submit(() -> {
                try {
                    processDataBatch(tcId, apiCodes, finalMinId, finalMaxId, dateType, dateValue);
                } catch (Exception e) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode()
                            , "奇富360ai数据打平异常[批次: " + dataType + ", minId: "
                                    + finalMinId + ", maxId: " + finalMaxId + "]" + e.getMessage()), e);
                }
            });

            currentMinId = currentMaxId + 1;
        }

        // 关闭线程池并等待完成
        shutdownThreadPool(threadPool, dataType);
    }

    /**
     * 通用方法：处理单个数据批次
     *
     * @param minId 批次最小id
     * @param maxId 批次最大id
     * @param dateType 日期类型：history表示<=，today表示=
     * @param dateValue 日期值
     */
    private void processDataBatch(String tcId, List<String> apiCodes, long minId, long maxId, String dateType, String dateValue) {
        Long indexId = minId - 1L;

        while (indexId < maxId) {
            List<DrsCustomizeUploadData> dataList =
                    drsCustomizeUploadDataMapper.getDataByIdRange(tcId, apiCodes, PAGE_SIZE, indexId, maxId, dateType, dateValue);
            if (dataList == null || dataList.isEmpty()) {
                break;
            }

            // 处理数据列表
            processDataList(dataList, tcId);
            
            // 更新indexId为最后一条记录的id
            indexId = dataList.get(dataList.size() - 1).getId();

            if (dataList.size() < PAGE_SIZE) {
                break;
            }
        }
    }

    /**
     * 通用方法：处理数据列表（打平并插入）
     *
     * @param dataList 源数据列表
     */
    private void processDataList(List<DrsCustomizeUploadData> dataList, String tcId) {
        List<BQifuUploadDataOriginal> flattenDataList = new ArrayList<>();
        List<Long> successIds = new ArrayList<>();

        for (DrsCustomizeUploadData sourceData : dataList) {
            try {
                List<BQifuUploadDataOriginal> flattened = flattenSingleRecord(sourceData);
                flattenDataList.addAll(flattened);
                successIds.add(sourceData.getId());
            } catch (Exception e) {
                log.warn("打平数据失败，id: {}, error: {}", sourceData.getId(), e.getMessage(), e);
            }
        }

        // 批量插入打平后的数据
        if (!flattenDataList.isEmpty()) {
            batchInsertFlattenData(flattenDataList);
        }

        // 更新打平状态为1（已打平）
        if (!successIds.isEmpty() && !dataList.isEmpty()) {
            drsCustomizeUploadDataMapper.updateFlattenStatusByIds(tcId, successIds, 1);
        }
    }

    /**
     * 通用方法：关闭线程池并等待完成
     *
     * @param threadPool 线程池
     * @param dataType 数据类型描述（用于日志）
     */
    private void shutdownThreadPool(ThreadPoolExecutor threadPool, String dataType) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                // 等待所有任务完成
            }
            log.warn("{}打平完成", dataType);
        } catch (InterruptedException e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode()
                    , "奇富360ai数据打平线程池中断异常[" + dataType + "]"), e);
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 打平单条记录
     */
    private List<BQifuUploadDataOriginal> flattenSingleRecord(DrsCustomizeUploadData sourceData) {
        List<BQifuUploadDataOriginal> result = new ArrayList<>();

        try {
            // 解析 request_json_data
            JSONObject requestJson = JSON.parseObject(sourceData.getRequestJsonData());
            JSONArray dataList = requestJson.getJSONArray("dataList");

            // 解析 extend，构建 serialNo -> extend 的映射
            Map<String, String> extendMap = buildExtendMap(sourceData.getExtend(), sourceData.getId());

            // 遍历 dataList，打平数据
            if (dataList != null && !dataList.isEmpty()) {
                for (int i = 0; i < dataList.size(); i++) {
                    JSONObject dataItem = dataList.getJSONObject(i);
                    BQifuUploadDataOriginal target = buildTargetRecord(sourceData, requestJson, dataItem, extendMap);
                    result.add(target);
                }
            }
        } catch (Exception e) {
            log.warn("打平数据失败，id: {}, error: {}", sourceData.getId(), e.getMessage(), e);
            throw e;
        }

        return result;
    }

    /**
     * 构建extend映射
     */
    private Map<String, String> buildExtendMap(String extend, Long sourceId) {
        Map<String, String> extendMap = new HashMap<>();
        if (StringUtils.isNotBlank(extend)) {
            try {
                JSONArray extendArray = JSON.parseArray(extend);
                for (int i = 0; i < extendArray.size(); i++) {
                    JSONObject extendObj = extendArray.getJSONObject(i);
                    String serialNo = extendObj.getString("serialNo");
                    if (StringUtils.isNotBlank(serialNo)) {
                        extendMap.put(serialNo, extendObj.toJSONString());
                    }
                }
            } catch (Exception e) {
                log.warn("解析extend失败，id: {}, error: {}", sourceId, e.getMessage());
            }
        }
        return extendMap;
    }

    /**
     * 构建目标记录
     */
    private BQifuUploadDataOriginal buildTargetRecord(DrsCustomizeUploadData sourceData, JSONObject requestJson,
                                                       JSONObject dataItem, Map<String, String> extendMap) {
        BQifuUploadDataOriginal target = new BQifuUploadDataOriginal();

        // 基础字段
        target.setDrsId(sourceData.getId());
        target.setBatchNo(requestJson.getString("batchNo"));
        target.setApiCode(sourceData.getApiCode());
        target.setCallTimeRange(requestJson.getString("callTimeRange"));
        target.setCallType(requestJson.getString("callType"));
        target.setFlowNo(requestJson.getString("flowNo"));
        target.setOperateScene(requestJson.getString("operateScene"));
        String templateNo = requestJson.getString("templateNo");
        target.setTemplateNo(templateNo);
        target.setSendMsg(requestJson.getString("sendMsg"));
        target.setRetryCall(requestJson.getString("retryCall"));
        target.setRetryCallStrategy(requestJson.getString("retryCallStrategy"));
        target.setRetryRange(requestJson.getString("retryRange"));
        target.setRetryNums(requestJson.getString("retryNums"));
        target.setRetryInterval(requestJson.getString("retryInterval"));
        target.setEventType(requestJson.getString("eventType"));
        target.setIsReal(QiFuDataTypeEnum.NON_REALTIME.getCode());
        target.setReceiveDate(sourceData.getReceiveDate());

        // 从 dataList 中获取
        target.setGender(dataItem.getString("gender"));
        target.setPhoneNoMd5(dataItem.getString("phoneNoMd5"));
        target.setSerialNo(dataItem.getString("serialNo"));
        target.setSurname(dataItem.getString("surname"));

        // 默认值
        target.setSelectStatus(QiFuSelectStatusEnum.WAIT_QUERY.getCode());
        target.setStatus(null);

        String userType = "";
        if (templateNo.length() > 12) {
            userType = templateNo.substring(0, templateNo.length() - 12);
        } else {
            userType = templateNo;
        }
        target.setUserType(userType);
        // 从 extend 中匹配对应的扩展信息
        String serialNo = target.getSerialNo();
        if (StringUtils.isNotBlank(serialNo) && extendMap.containsKey(serialNo)) {
            target.setExtend(extendMap.get(serialNo));
        }

        return target;
    }

    /**
     * 批量插入打平后的数据
     */
    private void batchInsertFlattenData(List<BQifuUploadDataOriginal> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        // 分批插入，每批1000条
        int batchSize = 1000;
        List<List<BQifuUploadDataOriginal>> batches = Lists.partition(dataList, batchSize);
        for (List<BQifuUploadDataOriginal> batch : batches) {
            for (BQifuUploadDataOriginal item : batch) {
                bQifuUploadDataOriginalMapper.insertSelective(item);
            }
        }
    }

    private String getValueOfJson(JSONObject jo, String key, String defaultValue) {
        if (jo == null || ObjectUtils.isEmpty(jo.getString(key))) {
            return defaultValue;
        }
        return jo.getString(key);
    }
}
