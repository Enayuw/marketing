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
import com.br.marketing.entity.WubaCollidingConfig;
import com.br.marketing.entity.WubaCollidingData;
import com.br.marketing.entity.WubaCollidingDataLog;
import com.br.marketing.entity.WubaCollidingDataLogExample;
import com.br.marketing.mapper.WubaCollidingBatchNoMapper;
import com.br.marketing.mapper.WubaCollidingConfigMapper;
import com.br.marketing.mapper.WubaCollidingDataDelayLoopCycleMapper;
import com.br.marketing.mapper.WubaCollidingDataLogMapper;
import com.br.marketing.mapper.WubaCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.WubaCollidingDataRobMapper;
import com.br.marketing.mapper.WubaCollidingDataSecondLoopCycleMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import cn.hutool.core.lang.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
    public static final String H = "H";
    public static final String F = "F";
    public static final String J = "J";
    public static final String Q = "Q";
    public static final String K = "K";
    public static final String D = "D";
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
    WubaCollidingDataDelayLoopCycleMapper wubaCollidingDataDelayLoopCycleMapper;
    @Resource
    WubaCollidingBatchNoMapper wubaCollidingBatchNoMapper;
    @Resource
    WubaCollidingDataLogMapper wubaCollidingDataLogMapper;
    @Resource
    WubaCollidingConfigMapper wubaCollidingConfigMapper;
    @Resource
    WuBaCollidingDataSynchronismService wuBaCollidingDataSynchronismService;
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
            Pair<WubaCollidingConfig, List<WubaCollidingData>> pair;
            try {
                pair = getCollidingDatas(apiCode, limit);
            } catch (Exception e) {
                String title = "58提交撞库名单，查询数据库异常，如下次运行时恢复则可忽略";
                String msg = e.getMessage();
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                        , title));
                wuBaServiceClient.sendDingDingAlert(title, msg);
                return;
            }

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
            String scoreSourceType = pair.getKey().getScoreType();
            String sourceType = pair.getKey().getDataSourceType();
            wubaCollidingBatchNoMapper.saveDataByBatchNo(batchNo, 1, apiCode, sourceType);

            switch (sourceType) {
                case D:
                    // 更新延期表pushTime
                    wubaCollidingDataDelayLoopCycleMapper.batchUpdatePushTimeById(collidingData);
                    break;
                case T:
                    // 更新周期场景1表pushTime
                    wubaCollidingDataLoopCycleMapper.batchUpdatePushTimeById(collidingData);
                    break;
                case S:
                    // 更新周期场景2表pushTime
                    wubaCollidingDataSecondLoopCycleMapper.batchUpdatePushTimeById(collidingData);
                    break;
                case H:
                case F:
                case J:
                case Q:
                case K:
                    // 更新非周期表pushTime、sourceType
                    wubaCollidingDataRobMapper.batchUpdatePushTimeById(collidingData, sourceType);
                    break;
                default:
                    break;
            }

            ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, marketingCommonConfig.getWubaCollidingDataSyncThreadNum());

            // 保存log表
            List<List<WubaCollidingData>> partitions = Lists.partition(collidingData, PARTATION_SIZE);
            for (List<WubaCollidingData> partition : partitions) {
                pool.submit(() -> batchSaveLog(apiCode, partition, batchNo, sourceType, scoreSourceType));
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

    private void batchSaveLog(String apiCode, List<WubaCollidingData> datas, String batchNo, String dataSourceType, String scoreSourceType) {
        try {
            List<WubaCollidingDataLog> logs = datas.stream().map((WubaCollidingData data) -> {
                WubaCollidingDataLog log = new WubaCollidingDataLog();
                log.setDataId(data.getId());
                log.setCell(data.getCell());
                log.setBatchNo(batchNo);
                log.setApiCode(apiCode);
                log.setDataSourceType(dataSourceType);
                log.setScoreType(scoreSourceType);

                // 周期数据没有packageId
                log.setPackageId(data.getPackageId());
                return log;
            }).collect(Collectors.toList());

            wubaCollidingDataLogMapper.batchSaveByBatchNo(logs);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage()
                    , "58提交撞库，子线程保存撞库日志处理异常"), e);
        }
    }

    /**
     * 查询待撞数据：按照优先级字段遍历撞库配置表
     * @param apiCode
     * @param limit
     * @return
     */
    private Pair<WubaCollidingConfig, List<WubaCollidingData>> getCollidingDatas(String apiCode, Integer limit) {
        List<Long> highValueIdList = wuBaCollidingDataSynchronismService.getHighValueFileIds(apiCode);
        String highValueIds =
                CollectionUtils.isEmpty(highValueIdList) ? "\"\"" :
                        Joiner.on(",").join(highValueIdList.stream().map(id -> "\"" + id + "\"").collect(Collectors.toList()));
        log.warn("58提交撞库，高价值文件id：{}", highValueIds);
        List<WubaCollidingConfig> configs = wubaCollidingConfigMapper.queryWuBaCollidingConfigByPriority();
        if (CollectionUtils.isEmpty(configs)) {
            return new Pair<>(null, null);
        }

        for (WubaCollidingConfig config : configs) {
            String configSql = config.getQuerySql();
            String replaceSql = configSql.replace("#{apiCode}", "\"" + apiCode + "\"").replace("#{highValueIds}", highValueIds);
            String completeSql = replaceSql.concat(" limit " + limit);
            List<WubaCollidingData> collidingData = wubaCollidingConfigMapper.queryCollidingDataByConfigSql(completeSql);
            if (CollectionUtils.isEmpty(collidingData)) {
                continue;
            }

            return new Pair<>(config, collidingData);
        }

        return new Pair<>(null, null);
    }
}
