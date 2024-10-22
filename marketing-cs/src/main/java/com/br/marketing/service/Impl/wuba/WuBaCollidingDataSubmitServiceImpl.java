package com.br.marketing.service.Impl.wuba;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.wuba.WuBaServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.WubaCollidingData;
import com.br.marketing.entity.WubaCollidingDataLog;
import com.br.marketing.entity.WubaCollidingDataLogExample;
import com.br.marketing.mapper.WubaCollidingBatchNoMapper;
import com.br.marketing.mapper.WubaCollidingDataLogMapper;
import com.br.marketing.mapper.WubaCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.WubaCollidingDataRobMapper;
import com.br.marketing.mapper.WubaCollidingDataSecondLoopCycleMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.google.common.collect.Lists;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Description 58提交撞库实现类
 * @Author hong.chen
 * @CreateTime 2024/07/10
 */
@Service
@Slf4j
public class WuBaCollidingDataSubmitServiceImpl implements WuBaCollidingDataSubmitService {
    public static final String T = "T";
    public static final String S = "S";
    public static final String F = "F";
    @Resource
    WuBaServiceClient wuBaServiceClient;
    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    WubaCollidingDataRobMapper wubaCollidingDataRobMapper;
    @Resource
    WubaCollidingDataLoopCycleMapper wubaCollidingDataLoopCycleMapper;
    @Resource
    WubaCollidingDataSecondLoopCycleMapper wubaCollidingDataSecondLoopCycleMapper;
    @Resource
    WubaCollidingBatchNoMapper wubaCollidingBatchNoMapper;
    @Resource
    WubaCollidingDataLogMapper wubaCollidingDataLogMapper;
    @Autowired
    RedisChgService redisChgService;
    private final static int PARTATION_SIZE = 50;
    ThreadPoolExecutor pool = BrExecutors.getThreadPool(20, 20);

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        // 判断redis中超限标记
        if (exceed()) {
            return;
        }

        Integer pagesize = marketingCommonConfig.getWuBaCollidingDataSubmitPageSize();
        marketingCommonConfig.getWubaCollidingApiCodes().forEach((String apiCode) -> {
            // log表当天已撞量级
            WubaCollidingDataLogExample logExample = new WubaCollidingDataLogExample();
            DateTime createTimeStart = DateUtil.parse(LocalDate.now().toString(), DatePattern.NORM_DATE_PATTERN);
            logExample.createCriteria().andApiCodeEqualTo(apiCode).andIsDeletedEqualTo(0)
                    .andCreateTimeGreaterThanOrEqualTo(createTimeStart);
            int logCount = wubaCollidingDataLogMapper.countByExample(logExample);

            // 当天剩余可撞量级
            int remainCount = marketingCommonConfig.getWubaCollidingDataMaxCountLimit() - logCount;
            if (remainCount <= 0) {
                String title = "58提交撞库名单，量级达到设定阈值，撞库暂停";
                String msg = title + "，设定阈值：" + marketingCommonConfig.getWubaCollidingDataMaxCountLimit() + "。需要判断是否调整设定阈值！";
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                        , title));
                return;
            }

            // 查询本次作业可撞量级
            int limit = remainCount - pagesize > 0 ? pagesize : remainCount;

            // 获取待撞数据
            Pair<String, List<WubaCollidingData>> pair = getCollidingDatas(apiCode, limit);

            List<WubaCollidingData> collidingData = pair.getValue();
            if (CollectionUtils.isEmpty(collidingData)) {
                return;
            }
            List<String> cells = collidingData.stream().map(WubaCollidingData::getCell).collect(Collectors.toList());

            long start = System.currentTimeMillis();
            Result result = wuBaServiceClient.submitCredentialStuffingList(cells);
            log.warn("58提交撞库名单，接口耗时：{}ms", System.currentTimeMillis() - start);

            // code返回9999，撞库超限
            if (Objects.equals(result.getCode(), ResultCode.INTERNAL_SERVER_ERROR.getValue())) {
                JSONObject resMap = JSONObject.parseObject(result.getData().toString());
                String title = "58提交撞库名单，客户返回超限，撞库暂停";
                String msg = title + "，响应内容：" + JSON.toJSONString(resMap);
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                        , title));
                wuBaServiceClient.sendDingDingAlert(title, msg);
                // redis中设置超限标记，当天有效
                setRedisExceedMark();
                return;
            }

            if (Objects.equals(result.getCode(), ResultCode.FAIL.getValue())) {
                JSONObject resMap = JSONObject.parseObject(result.getData().toString());
                String title = "58提交撞库名单，调用客户接口异常";
                String msg = title + "，响应内容：" + JSON.toJSONString(resMap);
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                        , title));
                wuBaServiceClient.sendDingDingAlert(title, msg);
                return;
            }

            // 保存批次号表
            String batchNo = result.getData().toString();
            String sourceType = pair.getKey();
            wubaCollidingBatchNoMapper.saveDataByBatchNo(batchNo, 1, apiCode, sourceType);

            switch (sourceType) {
                case T:
                    // 更新周期场景1表pushTime
                    wubaCollidingDataLoopCycleMapper.batchUpdatePushTimeById(collidingData);
                    break;
                case S:
                    // 更新周期场景2表pushTime
                    wubaCollidingDataSecondLoopCycleMapper.batchUpdatePushTimeById(collidingData);
                    break;
                case F:
                    // 更新非周期表pushTime
                    wubaCollidingDataRobMapper.batchUpdatePushTimeById(collidingData);
                    break;
                default:
                    break;
            }

            pool.setCorePoolSize(marketingCommonConfig.getWubaCollidingDataSyncThreadNum());
            pool.setMaximumPoolSize(marketingCommonConfig.getWubaCollidingDataSyncThreadNum());

            // 保存log表
            List<List<WubaCollidingData>> partitions = Lists.partition(collidingData, PARTATION_SIZE);
            for (List<WubaCollidingData> partition : partitions) {
                pool.submit(() -> batchSaveLog(apiCode, partition, batchNo, sourceType));
            }
        });
    }

    private void setRedisExceedMark() {
        // 当前日期
        LocalDateTime now = LocalDateTime.now();
        // 当前时间至23:59:59
        LocalDateTime endOfDay = now.with(LocalTime.MAX);
        // 计算当前时间至23:59:59的秒数
        int secondsUntilEndOfDay = (int) ChronoUnit.SECONDS.between(now, endOfDay);
        try {
            redisChgService.setex(RedisKeyConstant.WUBA_COLLIDING_EXCEED_LIMIT, "1", secondsUntilEndOfDay);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage(), "58提交撞库名单，设置redis超限标记失败"));
        }
    }

    private boolean exceed() {
        String exceedLimit;
        try {
            exceedLimit = redisChgService.get(RedisKeyConstant.WUBA_COLLIDING_EXCEED_LIMIT);
            if (Objects.equals(exceedLimit, "1")) {
                return true;
            }
        } catch (Exception e) {
            // 异常，认为不超限
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage()
                    , "58撞库，获取redis撞库超限标记失败"), e);
        }

        return false;
    }

    private void batchSaveLog(String apiCode, List<WubaCollidingData> datas, String batchNo, String dataSourceType) {
        try {
            List<WubaCollidingDataLog> logs = datas.stream().map((WubaCollidingData data) -> {
                WubaCollidingDataLog log = new WubaCollidingDataLog();
                log.setDataId(data.getId());
                log.setCell(data.getCell());
                log.setBatchNo(batchNo);
                log.setApiCode(apiCode);
                log.setDataSourceType(dataSourceType);

                // 周期数据不保存packageId
                if (Objects.equals(dataSourceType, F)) {
                    log.setPackageId(data.getPackageId());
                }

                return log;
            }).collect(Collectors.toList());

            wubaCollidingDataLogMapper.batchSaveByBatchNo(logs);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage()
                    , "58提交撞库，子线程保存撞库日志处理异常"), e);
        }
    }

    /**
     * 查询待撞数据，顺序为：周期场景1 → 周期场景2 → 高价值 → 手动上传
     * @param apiCode
     * @param limit
     * @return
     */
    private Pair<String, List<WubaCollidingData>> getCollidingDatas(String apiCode, Integer limit) {
        DateTime nowDate = DateUtil.parse(LocalDate.now().toString(), DatePattern.NORM_DATE_PATTERN);
        // 周期场景1的数据
        if (marketingCommonConfig.getWuBaCollidingDataSwitch().get(T)) {
            Integer cycleConfig = marketingCommonConfig.getWuBaCollidingCycleDayConfig().get(T);
            DateTime pushTimeStart = DateUtil.parse(LocalDate.now().minusDays(cycleConfig).toString(), DatePattern.NORM_DATE_PATTERN);
            DateTime pushTimeEnd = DateUtil.parse(LocalDate.now().minusDays(cycleConfig - 1).toString(), DatePattern.NORM_DATE_PATTERN);
            List<WubaCollidingData> loopCycles = wubaCollidingDataLoopCycleMapper.selectCollidingData(pushTimeStart, pushTimeEnd, apiCode,
                    limit);
            if (!CollectionUtils.isEmpty(loopCycles)) {
                return new Pair<>(T, loopCycles);
            }
        }

        // 周期场景2的数据
        if (marketingCommonConfig.getWuBaCollidingDataSwitch().get(S)) {
            Integer cycleConfig = marketingCommonConfig.getWuBaCollidingCycleDayConfig().get(S);
            DateTime pushTimeStart = DateUtil.parse(LocalDate.now().minusDays(cycleConfig).toString(), DatePattern.NORM_DATE_PATTERN);
            DateTime pushTimeEnd = DateUtil.parse(LocalDate.now().minusDays(cycleConfig - 1).toString(), DatePattern.NORM_DATE_PATTERN);
            List<WubaCollidingData> loopCycles = wubaCollidingDataSecondLoopCycleMapper.selectCollidingData(pushTimeStart, pushTimeEnd, apiCode,
                    limit);
            if (!CollectionUtils.isEmpty(loopCycles)) {
                return new Pair<>(S, loopCycles);
            }
        }

        // 高价值数据
        List<String> highValueFiles = marketingCommonConfig.getWubaCollidingHighValueFiles();
        if (!CollectionUtils.isEmpty(highValueFiles)) {
            List<WubaCollidingData> robs = wubaCollidingDataRobMapper.selectHighValueCollidingData(limit, apiCode, nowDate, highValueFiles);
            if (!CollectionUtils.isEmpty(robs)) {
                return new Pair<>(F, robs);
            }
        }

        // 手动上传数据
        List<WubaCollidingData> robs = wubaCollidingDataRobMapper.selectCollidingData(limit, apiCode);
        if (!CollectionUtils.isEmpty(robs)) {
            return new Pair<>(F, robs);
        }

        // 非高价值周期非金融TRUE的-2
        Long nonFinancialReavedFileId = getReavedFileIdByType(T);
        if (Objects.nonNull(nonFinancialReavedFileId)) {
            Integer cycleConfig = marketingCommonConfig.getWuBaCollidingCycleDayConfig().get(T);
            DateTime pushTimeEnd = DateUtil.parse(LocalDate.now().minusDays(cycleConfig - 1).toString(), DatePattern.NORM_DATE_PATTERN);
            List<WubaCollidingData> nonFinancialReaveds = wubaCollidingDataRobMapper.selectReavedData(limit, apiCode, pushTimeEnd,
                    nonFinancialReavedFileId);
            if (!CollectionUtils.isEmpty(nonFinancialReaveds)) {
                return new Pair<>(F, nonFinancialReaveds);
            }
        }

        // 非高价值周期金融TRUE的-2
        Long financialReavedFileId = getReavedFileIdByType(S);
        if (Objects.nonNull(financialReavedFileId)) {
            Integer cycleConfig = marketingCommonConfig.getWuBaCollidingCycleDayConfig().get(S);
            DateTime pushTimeEnd = DateUtil.parse(LocalDate.now().minusDays(cycleConfig - 1).toString(), DatePattern.NORM_DATE_PATTERN);
            List<WubaCollidingData> financialReaveds = wubaCollidingDataRobMapper.selectReavedData(limit, apiCode, pushTimeEnd,
                    financialReavedFileId);
            if (!CollectionUtils.isEmpty(financialReaveds)) {
                return new Pair<>(F, financialReaveds);
            }
        }

        // 补包的-2
        Long supplyReavedFileId = getReavedFileIdByType(F);
        if (Objects.nonNull(supplyReavedFileId)) {
            List<WubaCollidingData> supplyReaveds = wubaCollidingDataRobMapper.selectReavedData(limit, apiCode, nowDate,
                    supplyReavedFileId);
            if (!CollectionUtils.isEmpty(supplyReaveds)) {
                return new Pair<>(F, supplyReaveds);
            }
        }

        return new Pair<>(null, null);
    }

    private Long getReavedFileIdByType(String type) {
        HashMap<String, JSONObject> map = marketingCommonConfig.getWubaCollidingReavedFileIds();
        JSONObject hashMap = map.get(type);
        if (CollectionUtils.isEmpty(hashMap)) {
            return null;
        }

        for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
            if ((Boolean) entry.getValue()) {
                return Long.valueOf(entry.getKey());
            }
        }

        return null;
    }
}
