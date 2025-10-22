package com.br.marketing.service.Impl.xc;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.xiecheng.XieChengServiceNew;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.Impl.VariableAllocationServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 携程非周期数据撞库相关Service实现
 *
 * @author senyang.zheng
 * @date 2024/03/19
 */
@Service
@Slf4j
public class XieChengRobDataCollidingManyTimesServiceImpl implements XieChengRobDataCollidingManyTimesService {

    public static final ThreadPoolExecutor XIECHENG_ROB_COLLIDING_THREAD = BrExecutors.getThreadPool(20, 20);

    @Resource
    private RedisChgService redisChgService;
    @Resource
    private XieChengCollidingDataLoopCycleMapper xieChengCollidingDataLoopCycleMapper;
    @Resource
    private XieChengCollidingDataRobMapper xieChengCollidingDataRobMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private XieChengServiceNew xieChengServiceNew;
    @Resource
    private XieChengCollidingResultHandleService handleService;
    @Resource
    private VariableAllocationServiceImpl variableAllocationService;
    @Resource
    private XieChengCollidingDataLoopCycleMapper loopCycleMapper;
    @Resource
    private XcLoopCycleDataService xcLoopCycleDataService;

    @Resource
    private XieChengCollidingDataRobTaskMapper xieChengCollidingDataRobTaskMapper;

    @Resource
    private XiechengCollidingDataPackageRuleMapper xiechengCollidingDataPackageRuleMapper;

    @Override
    public void collidingDataManyTimes() {
        // 周期积压量级
        Date startDate = Date.from(LocalDate.now().minusDays(1).atTime(23, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = new Date();
        if (loopCycleMapper.selectCycleCountOfStacktiflash_(startDate, endDate) >= 200000) {
            return;
        }

        Integer perMinuteCounts = getPerMinuteCounts();
        Integer todayTrueTotalCounts = xieChengCollidingDataLoopCycleMapper.selectTodayCycleCounttiflash_();
        Integer totalThreshold = variableAllocationService.getVariableAllocation().getNormalQuantity();
        int limit = Math.min(perMinuteCounts, totalThreshold - todayTrueTotalCounts);
        // 强制开关开启强制撞库，强制开关关闭且条件开关打开开始撞库
        while (limit > 0 && (marketingCommonConfig.getXieChengForceOpenSwitch()
                || Objects.equals("true", redisChgService.get(RedisKeyConstant.XIECHENG_CONDITIONSWITCH)))) {
            ThreadPoolAdjustmentUtil.adjustThreadPoolSize(XIECHENG_ROB_COLLIDING_THREAD, marketingCommonConfig.getXiechengRobCollidingThread());
            int pageSize = Math.min(marketingCommonConfig.getXiechengCollidingPageSize(), limit);
            // 获取当前需要执行的任务 根据任务获取包的信息 以及 当前任务属于第几次撞库
            // 若当前任务为第一次撞库 则需要先查询 push_time is null 的数据
            // 如果没有push_time is null 的数据 说明包已经不是新包了，则按照push_time 小于今天的数据 push_time asc 排序
            // 如果当前任务的count 值> 1  则说明当前包今天已经被全量撞过一次 那么撞库的查询条件为 count < 设置的次数 的数据
            // 根据任务查询不到当前包下的任何数据，则更新当前任务为完成
            List<XieChengCollidingDataRobTask> taskList = xieChengCollidingDataRobTaskMapper.getTask();
            if(taskList.isEmpty()){
                break;
            }
            for (XieChengCollidingDataRobTask task : taskList) {
                // 查询撞得量级
                if(task.getCollidingBackNumber()!=null){
                    int packageTrueCount = task.getCollidingBackNumber();
                    int packageCountTrue = xieChengCollidingDataRobMapper.getPackageCounttiflash_(task.getPackageId());
                    if ( packageCountTrue >= packageTrueCount) {
                        // 更新任务状态为完成
                        xieChengCollidingDataRobTaskMapper.updateTaskStatusComplete(task.getId());
                        continue;
                    }
                }
                List<XieChengCollidingDataRob> collidingDataRobs;
                if (task.getPackageCollidingCount() == 1) {
                    collidingDataRobs = xieChengCollidingDataRobMapper.getFirstRobDataPush(task.getPackageId(), pageSize);
                    collidingDataRobs.stream().peek(rob -> rob.setCollidingCount(task.getPackageCollidingCount() - 1)).collect(Collectors.toList());
                } else {
                    collidingDataRobs = xieChengCollidingDataRobMapper.getMoreRobDataPush(
                            task.getPackageId(), pageSize, task.getPackageCollidingCount());
                }
                if (CollectionUtils.isEmpty(collidingDataRobs)) {
                    // 更新任务状态为完成
                    xieChengCollidingDataRobTaskMapper.updateTaskStatusComplete(task.getId());
                    continue;
                }
                List<CompletableFuture<Void>> futures = getCompletableFutures(collidingDataRobs);
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                limit -= collidingDataRobs.size();
            }

        }
    }

    private List<CompletableFuture<Void>> getCompletableFutures(List<XieChengCollidingDataRob> collidingDataRobs) {
        List<List<XieChengCollidingDataRob>> xieChengCollidingDataListPartition = Lists.partition(collidingDataRobs, 50);
        List<CompletableFuture<Void>> futures = Lists.newArrayList();
        xieChengCollidingDataListPartition.forEach((List<XieChengCollidingDataRob> robData) -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> pushDataAndHandleResult(robData), XIECHENG_ROB_COLLIDING_THREAD);
            futures.add(future);
        });
        return futures;
    }

    /**
     * 推送非周期撞库数据
     *
     * @param robData rob数据
     * @author senyang.zheng
     * @date 2024/03/21
     */
    public void pushDataAndHandleResult(List<XieChengCollidingDataRob> robData) {
        Map<String, XieChengCollidingDataRob> cellMap = robData.stream()
                .collect(Collectors.toMap(XieChengCollidingDataRob::getCellSha256CodeList, rob -> rob,
                        (existing, replacement) -> replacement));
        List<String> sha256Codes = robData.stream().map(XieChengCollidingDataRob::getCellSha256CodeList).collect(Collectors.toList());

        List<String> cells = xcLoopCycleDataService.excludeData(sha256Codes, "F");
        if (CollectionUtils.isEmpty(cells)) {
            return;
        }

        try {
            Result collidingResult = xieChengServiceNew.pushXieChengSmsCollidingDataNew(cells);
            handleService.robDataHandle(collidingResult, cellMap);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程非周期撞库异常"), e);
        }

    }

    public Integer getPerMinuteCounts() {
        Integer threshold = marketingCommonConfig.getXiechengPerMinuteThreshold();
        String today = DateUtil.today();
        String key = RedisKeyConstant.XIECHENG_RELEASE_TIME + today;
        Long size = redisChgService.hlen(key);
        if (size.equals(0L)) {
            initializeTodayReleaseTime(key);
        }
        String minute = DateUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_MINUTE_PATTERN);
        String perMinuteCounts = redisChgService.hget(key, minute) == null ? "0" : redisChgService.hget(key, minute);
        return threshold - Integer.parseInt(perMinuteCounts);
    }

    public void initializeTodayReleaseTime(String key) {
        List<Map<String, Object>> perMinuteCounts = xieChengCollidingDataLoopCycleMapper.selectPerMinuteCountstiflash_();
        // 初始化剔除当天和昨天的key
        redisChgService.del(key);
        String yesKey = RedisKeyConstant.XIECHENG_RELEASE_TIME + DateUtil.formatDate(DateUtil.yesterday());
        redisChgService.del(yesKey);
        perMinuteCounts.forEach((Map<String, Object> perMinuteCount) -> redisChgService.hset(key, String.valueOf(perMinuteCount.get("releaseTime")),
                String.valueOf(perMinuteCount.get("counts"))));
    }
}
