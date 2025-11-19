package com.br.marketing.check.service.Impl.qifu;

import com.alibaba.fastjson.JSON;
import com.br.marketing.check.service.qifu.QiFuQueryCallService;
import com.br.marketing.client.RedisChgService;
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
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
     * 时间阈值（12:00）
     */
    private static final LocalTime TIME_THRESHOLD = LocalTime.of(12, 00);

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

    @Override
    public void queryCallMessage() {
        // 获取今天的日期
        String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
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
                    processUserTypeData(finalUserType, finalTodayDate);
                } catch (Exception e) {
                    log.error("处理userType={}的数据失败，error: {}", finalUserType, e.getMessage(), e);
                }
            });
        }

        // 关闭线程池
        shutdownThreadPool(threadPool);
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
            // 检查当前时间是否>12:00
            LocalTime currentTime = LocalTime.now();
            if (currentTime.isAfter(TIME_THRESHOLD) || currentTime.equals(TIME_THRESHOLD)) {
                log.warn("userType={} 当前时间 {} >= {}，Redis开关打开", userType, currentTime, TIME_THRESHOLD);
                return true;
            }

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
            log.error("检查userType={}的Redis开关失败，error: {}", userType, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 计算有卷比例（基于今天的数据）
     * 查询该user_type下select_status为0、3、4的数据，统计有卷的比例
     * 有卷：extend字段不为空或者select_status=2（查询成功）
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
            log.error("计算userType={}今天 {} 的有卷比例失败，error: {}", userType, todayDate, e.getMessage(), e);
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
            log.error("更新userType={}今天 {} 的有卷比例到Redis失败，error: {}", userType, todayDate, e.getMessage(), e);
        }
    }

    /**
     * 处理某个userType的数据（按场景维度处理，基于今天的数据）
     */
    private void processUserTypeData(String userType, String todayDate) {
        // 检查当前场景的Redis开关
        boolean switchOpen = checkRedisSwitch(userType, todayDate);

        // 根据开关状态确定查询的select_status列表
        List<Integer> selectStatusList;
        if (switchOpen) {
            // 开关打开：查询 select_status in (0, 3)
            selectStatusList = Arrays.asList(0, 3);
        } else {
            // 开关关闭：查询 select_status in (0, 3, 4)
            selectStatusList = Arrays.asList(0, 3, 4);
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
            processUserTypeDataList(userType, dataList, todayDate);

            if (dataList.size() < PAGE_SIZE) {
                hasMore = false;
            }
        }
    }

    /**
     * 处理某个userType的数据列表（单场景调用接口）
     */
    private void processUserTypeDataList(String userType, List<BQifuUploadDataOriginal> dataList, String todayDate) {
        // 按serialNo分组，每50个一批调用接口
        List<String> serialNoList = dataList.stream()
                .map(BQifuUploadDataOriginal::getSerialNo)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        if (serialNoList.isEmpty()) {
            return;
        }

        List<List<String>> partitions = ListUtils.partition(serialNoList, 50);
        List<Result<ResponseData<QryCallRealTimeResp>>> resultList = new ArrayList<>();

        // 调用360查询接口
        for (List<String> partition : partitions) {
            QryCallRealTimeReq qryCallRealTimeReq = new QryCallRealTimeReq();
            qryCallRealTimeReq.setCallType("AI");
            qryCallRealTimeReq.setRequestNo(UUID.randomUUID().toString());
            qryCallRealTimeReq.setSerialNoList(partition);

            Result<ResponseData<QryCallRealTimeResp>> result = methodRetryHandlerService.qryCallRealTime(qryCallRealTimeReq, 0);
            resultList.add(result);
        }

        // 检查是否有失败的结果
        List<Result<ResponseData<QryCallRealTimeResp>>> failResults = resultList.stream()
                .filter(result -> !ResultCode.SUCCESS.getValue().equals(result.getCode()))
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(failResults)) {
            log.warn("userType={} 查询外呼信息有失败，失败数量: {}", userType, failResults.size());
            // 有异常，更新select_status为3（重试-接口异常）
            updateSelectStatus(dataList, 3);
            return;
        }

        // 收集所有返回的详情数据
        List<CallRealTimeDTO> allDetailList = new ArrayList<>();
        for (Result<ResponseData<QryCallRealTimeResp>> result : resultList) {
            if (result.getData() != null && result.getData().getData() != null
                    && result.getData().getData().getT() != null
                    && result.getData().getData().getT().getDataDetails() != null) {
                allDetailList.addAll(result.getData().getData().getT().getDataDetails());
            }
        }

        // 构建serialNo -> CallRealTimeDTO的映射
        // 更新数据：将返回信息存在extend里，更新select_status为2（查询成功）
        List<BQifuUploadDataOriginal> updateRecords = new ArrayList<>();
        for (BQifuUploadDataOriginal record : dataList) {
            // 查找对应的返回数据
            List<CallRealTimeDTO> matchedDetails = allDetailList.stream()
                    .filter(detail -> record.getSerialNo().equals(detail.getSerialNo()))
                    .collect(Collectors.toList());

            if (!matchedDetails.isEmpty()) {
                // 将返回信息存在extend里
                record.setExtend(JSON.toJSONString(matchedDetails));
                record.setSelectStatus(2);
            } else {
                // 没有匹配到数据，可能是无卷信息，更新select_status为4（重试-无卷信息）
                record.setSelectStatus(4);
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
     * 更新select_status
     */
    @Override
    public void updateSelectStatus(List<BQifuUploadDataOriginal> dataList, Integer selectStatus) {
        List<BQifuUploadDataOriginal> updateRecords = dataList.stream()
                .map(record -> {
                    BQifuUploadDataOriginal updateRecord = new BQifuUploadDataOriginal();
                    updateRecord.setId(record.getId());
                    updateRecord.setSelectStatus(selectStatus);
                    return updateRecord;
                })
                .collect(Collectors.toList());

        batchUpdateRecords(updateRecords);
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
            log.error("查询外呼信息线程池关闭异常", e);
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

