package com.br.marketing.monkey.service.qifu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.client.qifu.ResponseData;
import com.br.marketing.client.qifu.callrealtime.CallRealTimeDTO;
import com.br.marketing.client.qifu.callrealtime.QryCallRealTimeReq;
import com.br.marketing.client.qifu.callrealtime.QryCallRealTimeResp;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.BQifuUploadDataOriginal;
import com.br.marketing.mapper.BQifuUploadDataOriginalMapper;
import com.br.marketing.service.Impl.qifu.enums.QiFuProcessStatusEnum;
import com.br.marketing.service.Impl.qifu.enums.QiFuSelectStatusEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 奇富查询外呼信息Service实现
 */
@Slf4j
@Service
public class QiFuQueryCallServiceImpl implements QiFuQueryCallService {

    /**
     * Redis开关key前缀
     */
    private static final String REDIS_SWITCH_KEY_PREFIX = "qifu:query:call:switch:";

    /**
     * 分页大小
     */
    private static final int PAGE_SIZE = 2000;

    /**
     * 线程数
     */
    private static final int THREAD_NUM = 10;

    /**
     * 有卷比例阈值
     */
    private static final double COUPON_RATIO_THRESHOLD = 0.75;

    /**
     * Redis过期时间（秒），24小时
     */
    private static final int REDIS_EXPIRE_SECONDS = 24 * 60 * 60;

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private BQifuUploadDataOriginalMapper bQifuUploadDataOriginalMapper;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TrackingService trackingService;

    @Override
    public void queryCallMessage() {
        // 获取今天的日期
        String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        JSONObject qifuAiCleanConfig = marketingCommonConfig.getQifuAiCleanConfig();
        LocalTime timeThreshold = LocalTime.parse(qifuAiCleanConfig.getString("timeThreshold"));
        String cleaningSwitch = qifuAiCleanConfig.getString("cleaningSwitch");

        
        // 查询今天所有不同的user_type
        List<String> userTypeList = bQifuUploadDataOriginalMapper.selectDistinctUserTypeByDate(todayDate);
        if (CollectionUtils.isEmpty(userTypeList)) {
            log.warn("今天 {} 没有查询到user_type数据，无需处理", todayDate);
            return;
        }

        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(THREAD_NUM, THREAD_NUM, "qifuQueryCall", 200);

        // 按user_type维度处理，每个user_type单独处理
        for (String userType : userTypeList) {
            final String finalUserType = userType;
            final String finalTodayDate = todayDate;
            threadPool.submit(() -> {
                try {
                    processUserTypeData(finalUserType, finalTodayDate, timeThreshold, cleaningSwitch);
                } catch (Exception e) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode()
                            , "奇富360ai查询外呼信息异常[userType: " + finalUserType + "]" + e.getMessage()), e);
                }
            });
        }

        // 关闭线程池
        shutdownThreadPool(threadPool);

        try {
            if(!userTypeList.isEmpty()){
                List<String> apiCodes = Arrays.asList(getValueOfJson(qifuAiCleanConfig, "cleanApiCode", "3700226").split(","));
                String remark = String.format("奇富360ai查询外呼信息,userTypeList：%s,注意：%s"
                        , userTypeList, "量级不准确!");
                trackingService.trackPointLog(DataFlowDirection.IN
                        , apiCodes.get(0)
                        , "奇富360定制查询外呼信息"
                        , 1L
                        , remark
                        , TrackingContext.generateBatchId());
            }
        } catch (Exception ex) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                            , ex.getMessage()
                            , "埋点异常")
                    , ex);
        }

    }

    private String getValueOfJson(JSONObject jo, String key, String defaultValue) {
        if (jo == null || ObjectUtils.isEmpty(jo.getString(key))) {
            return defaultValue;
        }
        return jo.getString(key);
    }


    /**
     * 检查Redis开关（按user_type维度）
     * 条件：user_type有卷比例>=75% 或者 当前时间>12:00
     * 如果Redis中不存在，则查询数据库统计有卷比例并新增到Redis
     * 
     * @param userType 场景标识
     * @param todayDate 今天的日期 yyyy-MM-dd
     * @return true表示开关打开，false表示开关关闭
     */
    private boolean checkRedisSwitch(String userType, String todayDate) {
        try {
            // 检查Redis中是否存在该user_type的开关
            String redisKey = REDIS_SWITCH_KEY_PREFIX + userType;
            Boolean exists = redisChgService.exists(redisKey);
            
            if (exists == null || !exists) {
                // Redis中不存在，查询数据库统计有卷比例并新增到Redis
                double ratio = calculateCouponRatio(userType, todayDate);
                // 新增到Redis
                redisChgService.setex(redisKey, String.valueOf(ratio), REDIS_EXPIRE_SECONDS);
                log.warn("userType={} Redis开关不存在，查询数据库统计今天有卷比例={}，已新增到Redis", userType, ratio);
                
                if (ratio >= COUPON_RATIO_THRESHOLD) {
                    log.warn("userType={} 有卷比例 {} >= {}，Redis开关打开", userType, ratio, COUPON_RATIO_THRESHOLD);
                    return true;
                }
            } else {
                // Redis中存在，获取有卷比例
                String ratioStr = redisChgService.get(redisKey);
                if (StringUtils.isNotBlank(ratioStr)) {
                    try {
                        double ratio = Double.parseDouble(ratioStr);
                        if (ratio >= COUPON_RATIO_THRESHOLD) {
                            log.warn("userType={} 有卷比例 {} >= {}，Redis开关打开", userType, ratio, COUPON_RATIO_THRESHOLD);
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        log.warn("解析userType={}的有卷比例失败，ratioStr={}，重新计算", userType, ratioStr);
                        // 解析失败，重新计算并更新Redis
                        double ratio = calculateCouponRatio(userType, todayDate);
                        redisChgService.setex(redisKey, String.valueOf(ratio), REDIS_EXPIRE_SECONDS);
                        if (ratio >= COUPON_RATIO_THRESHOLD) {
                            return true;
                        }
                    }
                }
            }

            return false;
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode()
                    , "奇富360ai查询外呼信息异常[检查Redis开关失败, userType: " + userType + "]" + e.getMessage()), e);
            return false;
        }
    }

    /**
     * 计算有卷比例（基于今天的数据）
     * 查询该user_type下的数据，统计有卷的比例
     * 有卷：extend字段不为空或者select_status=查询成功
     * 
     * @param userType 场景标识
     * @param todayDate 今天的日期 yyyy-MM-dd
     * @return 有卷比例（0-1之间）
     */
    private double calculateCouponRatio(String userType, String todayDate) {
        try {
            // 查询该user_type下今天的数据总数
            Long totalCount =
                    bQifuUploadDataOriginalMapper.countByUserTypeAndSelectStatusAndDate(userType, todayDate);
            
            if (totalCount == null || totalCount == 0) {
                log.warn("userType={} 今天 {} 没有查询到的数据", userType, todayDate);
                return 0.0;
            }

            // 查询今天有卷的数据数量（select_status=2）
            Long couponCount = bQifuUploadDataOriginalMapper.countCouponDataByUserTypeAndDate(userType, todayDate);
            
            if (couponCount == null || couponCount == 0) {
                return 0.0;
            }

            double ratio = (double) couponCount / totalCount;
            log.warn("userType={} 今天 {} 有卷比例计算：总数={}，有卷数={}，比例={}", userType, todayDate, totalCount, couponCount, ratio);
            return ratio;
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode()
                    , "奇富360ai查询外呼信息异常[计算有卷比例失败, userType: " + userType + ", todayDate: " + todayDate + "]" + e.getMessage()), e);
            return 0.0;
        }
    }

    /**
     * 更新Redis中的有卷比例（基于今天的数据）
     * 在处理完数据后调用，更新当前场景的有卷比例
     * 
     * @param userType 场景标识
     * @param todayDate 今天的日期 yyyy-MM-dd
     */
    private void updateCouponRatio(String userType, String todayDate) {
        try {
            String redisKey = REDIS_SWITCH_KEY_PREFIX + userType;
            double ratio = calculateCouponRatio(userType, todayDate);
            // 更新Redis
            redisChgService.setex(redisKey, String.valueOf(ratio), REDIS_EXPIRE_SECONDS);
            log.warn("userType={} 今天 {} 更新有卷比例={}到Redis", userType, todayDate, ratio);
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode()
                    , "奇富360ai查询外呼信息异常[更新有卷比例到Redis失败, userType: " + userType + ", todayDate: " + todayDate + "]" + e.getMessage()), e);
        }
    }

    /**
     * 处理某个userType的数据（按场景维度处理，基于今天的数据）
     */
    private void processUserTypeData(String userType, String todayDate, LocalTime timeThreshold, String cleaningSwitch) {
        // 检查当前场景的Redis开关
        boolean switchOpen = checkRedisSwitch(userType, todayDate);

        // 根据开关状态确定查询的select_status列表
        List<Integer> selectStatusList;
        if (switchOpen && LocalTime.now().isBefore(timeThreshold)) {
            // 开关打开：查询 select_status in (待查询, 重试-接口异常)
            selectStatusList = Arrays.asList(
                QiFuSelectStatusEnum.WAIT_QUERY.getCode(), 
                QiFuSelectStatusEnum.RETRY_INTERFACE_ERROR.getCode()
            );
        } else {
            // 开关关闭：查询 select_status in (待查询, 重试-接口异常, 重试-无卷信息)
            selectStatusList = Arrays.asList(
                QiFuSelectStatusEnum.WAIT_QUERY.getCode(), 
                QiFuSelectStatusEnum.RETRY_INTERFACE_ERROR.getCode(),
                QiFuSelectStatusEnum.RETRY_NO_COUPON.getCode()
            );
        }

        Long indexId = null;
        boolean hasMore = true;
        while (hasMore) {
            // 查询当前场景今天的数据
            List<BQifuUploadDataOriginal> dataList = bQifuUploadDataOriginalMapper.selectDataForQueryCallByUserTypeAndDate(
                    userType, selectStatusList, todayDate, PAGE_SIZE, indexId);
            if (dataList == null || dataList.isEmpty()) {
                hasMore = false;
                break;
            }

            indexId = dataList.get(dataList.size() - 1).getId();

            // 处理当前场景的数据（单场景调用接口）
            processUserTypeDataList(userType, dataList, todayDate, timeThreshold, cleaningSwitch);

            if (dataList.size() < PAGE_SIZE) {
                hasMore = false;
            }
        }

        // 数据处理完成后，如果当前user_type有卷比例>=75%，需要将所有非实时数据今天的这个场景下的查询状态非0的全部清洗状态置为0
        resetCleanStatusIfCouponRatioHigh(userType, todayDate);
    }

    /**
     * 如果当前user_type有卷比例>=75%，将所有非实时数据今天的这个场景下的查询状态非0的全部清洗状态置为0
     * 
     * @param userType 场景标识
     * @param todayDate 今天的日期 yyyy-MM-dd
     */
    private void resetCleanStatusIfCouponRatioHigh(String userType, String todayDate) {
        try {
            // 检查当前user_type的有卷比例
            double ratio = calculateCouponRatio(userType, todayDate);
            
            if (ratio >= COUPON_RATIO_THRESHOLD) {
                log.warn("userType={} 今天 {} 有卷比例 {} >= {}，开始将所有非实时数据查询状态非0的清洗状态置为0", 
                        userType, todayDate, ratio, COUPON_RATIO_THRESHOLD);
                
                // 批量更新符合条件的记录的清洗状态为0
                int updateCount = bQifuUploadDataOriginalMapper.updateStatusToUnprocessedForNonRealtime(userType, todayDate);
                
                log.warn("userType={} 今天 {} 已将 {} 条非实时数据查询状态非0的清洗状态置为0", 
                        userType, todayDate, updateCount);
            } else {
                log.info("userType={} 今天 {} 有卷比例 {} < {}，无需重置清洗状态", 
                        userType, todayDate, ratio, COUPON_RATIO_THRESHOLD);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode()
                    , "奇富360ai查询外呼信息异常[重置清洗状态失败, userType: " + userType + ", todayDate: " + todayDate + "]" + e.getMessage()), e);
        }
    }

    /**
     * 处理某个userType的数据列表（单场景调用接口）
     */
    private void processUserTypeDataList(String userType, List<BQifuUploadDataOriginal> dataList,
                                         String todayDate, LocalTime timeThreshold, String cleaningSwitch) {
        // 按serialNo分组，每50个一批调用接口
        List<String> serialNoList = dataList.stream()
                .map(BQifuUploadDataOriginal::getSerialNo)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        if (serialNoList.isEmpty()) {
            return;
        }

        List<List<String>> partitions = ListUtils.partition(serialNoList, 50);
        
        // 收集所有成功返回的详情数据和失败批次的serialNo
        List<CallRealTimeDTO> allDetailList = new ArrayList<>();
        Set<String> failedSerialNoSet = new HashSet<>();
        int successCount = 0;
        int failCount = 0;

        // 调用360查询接口，按批次分别处理
        for (int i = 0; i < partitions.size(); i++) {
            List<String> partition = partitions.get(i);
            QryCallRealTimeReq qryCallRealTimeReq = new QryCallRealTimeReq();
            qryCallRealTimeReq.setCallType("AI");
            qryCallRealTimeReq.setRequestNo(UUID.randomUUID().toString());
            qryCallRealTimeReq.setSerialNoList(partition);

            Result<ResponseData<QryCallRealTimeResp>> result = methodRetryHandlerService.qryCallRealTime(qryCallRealTimeReq, 0);
            
            // 判断当前批次是否成功
            if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                // 当前批次成功，收集返回数据
                successCount++;
                if (result.getData() != null && result.getData().getData() != null
                        && result.getData().getData().getT() != null
                        && result.getData().getData().getT().getDataDetails() != null) {
                    allDetailList.addAll(result.getData().getData().getT().getDataDetails());
                }
            } else {
                // 当前批次失败，记录失败的serialNo
                failCount++;
                failedSerialNoSet.addAll(partition);
                log.warn("userType={} 第{}批次查询外呼信息失败，批次大小: {}, 错误码: {}, 错误信息: {}", 
                        userType, i + 1, partition.size(), result.getCode(), result.getMessage());
            }
        }

        log.info("userType={} 查询外呼信息完成，成功批次: {}, 失败批次: {}", userType, successCount, failCount);

        // 更新数据：分别处理成功和失败的数据
        List<BQifuUploadDataOriginal> updateRecords = new ArrayList<>();
        for (BQifuUploadDataOriginal record : dataList) {
            String serialNo = record.getSerialNo();
            
            // 判断当前记录所在的批次是否失败
            if (failedSerialNoSet.contains(serialNo)) {
                // 该记录所在批次失败，标记为接口异常
                record.setSelectStatus(QiFuSelectStatusEnum.RETRY_INTERFACE_ERROR.getCode());
            } else {
                // 该记录所在批次成功，查找对应的返回数据
                CallRealTimeDTO callRealTimeDTO = allDetailList.stream()
                        .filter(detail -> serialNo.equals(detail.getSerialNo()))
                        .findFirst()
                        .orElse(null);
                // 将返回信息存在extend里
                record.setExtend(JSON.toJSONString(callRealTimeDTO));
                if (callRealTimeDTO != null && hasValidCouponName(callRealTimeDTO)) {
                    record.setStatus(QiFuProcessStatusEnum.UNPROCESSED.getCode());
                    record.setSelectStatus(QiFuSelectStatusEnum.QUERY_SUCCESS.getCode());
                } else {
                    // 没有匹配到数据，可能是无卷信息，更新select_status为重试-无卷信息
                    record.setSelectStatus(QiFuSelectStatusEnum.RETRY_NO_COUPON.getCode());
                }
            }
            if (LocalTime.now().isAfter(timeThreshold) || LocalTime.now().equals(timeThreshold) || "true".equals(cleaningSwitch)) {
                record.setStatus(QiFuProcessStatusEnum.UNPROCESSED.getCode());
            }
            updateRecords.add(record);
        }

        // 批量更新
        if (!updateRecords.isEmpty()) {
            batchUpdateRecords(updateRecords);
            // 更新Redis中的有卷比例
            updateCouponRatio(userType, todayDate);
        }
    }

    /**
     * 批量更新记录
     */
    private void batchUpdateRecords(List<BQifuUploadDataOriginal> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        // 分批更新，每批100条
        int batchSize = 100;
        List<List<BQifuUploadDataOriginal>> batches = ListUtils.partition(records, batchSize);
        for (List<BQifuUploadDataOriginal> batch : batches) {
            bQifuUploadDataOriginalMapper.batchUpdateExtendAndSelectStatus(batch);
        }
    }

    /**
     * 检查callRealTimeDTO的rCouponInfo字段中是否有有效的couponName
     *
     * @param callRealTimeDTO 实时外呼信息DTO
     * @return true表示存在有效的couponName，false表示不存在
     */
    private boolean hasValidCouponName(CallRealTimeDTO callRealTimeDTO) {
        if (callRealTimeDTO == null) {
            return false;
        }

        String rCouponInfo = callRealTimeDTO.getRCouponInfo();
        if (StringUtils.isBlank(rCouponInfo)) {
            return false;
        }

        try {
            JSONArray coupons = JSON.parseArray(rCouponInfo);
            if (coupons == null || coupons.isEmpty()) {
                return false;
            }

            // 遍历数组，检查是否有任何一个对象的couponName字段有值
            for (int i = 0; i < coupons.size(); i++) {
                JSONObject coupon = coupons.getJSONObject(i);
                if (coupon != null) {
                    String couponName = coupon.getString("couponName");
                    if (StringUtils.isNotBlank(couponName)) {
                        return true;
                    }
                }
            }

            return false;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode(),
                    "解析rCouponInfo时发生错误，错误信息：" + e.getMessage()), e);
            return false;
        }
    }

    /**
     * 关闭线程池
     */
    private void shutdownThreadPool(ThreadPoolExecutor threadPool) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn("等待查询外呼信息线程池结束");
            }
            log.warn("查询外呼信息完成");
        } catch (InterruptedException e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUAI_SERVICEERROR.getCode()
                    , "奇富360ai查询外呼信息线程池关闭异常"), e);
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

