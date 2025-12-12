package com.br.marketing.service.Impl.xc;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.xiecheng.XieChengServiceNew;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.xiecheng.XieChengActivateDTO;
import com.br.marketing.entity.CustomizeUploadData;
import com.br.marketing.entity.XieChengCollidingDataHitRequestNoMapping;
import com.br.marketing.entity.XieChengCollidingDataHitRequestNoMappingExample;
import com.br.marketing.entity.XieChengCollidingDataLoopCycle;
import com.br.marketing.entity.XieChengCollidingDataLoopCycleExample;
import com.br.marketing.entity.XieChengCollidingDataPackage;
import com.br.marketing.entity.XieChengCollidingDataPackageExample;
import com.br.marketing.entity.XieChengCollidingDataRob;
import com.br.marketing.entity.XieChengCollidingDataRobExample;
import com.br.marketing.entity.XieChengCollidingDataRobTask;
import com.br.marketing.entity.XiechengCollidingDataPackageRule;
import com.br.marketing.mapper.XieChengCollidingDataHitRequestNoMappingMapper;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataPackageMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobTaskMapper;
import com.br.marketing.mapper.XiechengCollidingDataPackageRuleMapper;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 携程非周期数据撞库相关Service实现
 * @author senyang.zheng
 * @date 2024/03/19
 */
@Service
@Slf4j
public class XieChengRobDataCollidingServiceImpl implements XieChengRobDataCollidingService {

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
    private XiechengCollidingDataPackageRuleMapper packageRuleMapper;
    @Resource
    private XieChengCollidingDataLoopCycleMapper loopCycleMapper;
    @Resource
    private XcLoopCycleDataService xcLoopCycleDataService;
    @Resource
    private XieChengCollidingDataPackageMapper packageMapper;
    private static final Integer RESETPARTITION = 10000;
    @Resource
    private XieChengCollidingDataHitRequestNoMappingMapper mappingMapper;
    @Resource
    private XieChengCollidingDataRobTaskMapper robTaskMapper;
    public static final ThreadPoolExecutor XIECHENG_ACTIVATE_THREAD_POOL = BrExecutors.getThreadPool(10, 10);

    /**
     * 推送非周期撞库数据
     * @param robData rob数据
     * @author senyang.zheng
     * @date 2024/03/21
     */
    @Override
    public void pushDataAndHandleResult(List<XieChengCollidingDataRob> robData) {
        Map<String, XieChengCollidingDataRob> cellMap = robData.stream()
                .collect(Collectors.toMap(XieChengCollidingDataRob::getCellSha256CodeList, rob -> rob, (existing, replacement) -> replacement));
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

    public void initializeTodayReleaseTime(String key) {
        List<Map<String, Object>> perMinuteCounts = xieChengCollidingDataLoopCycleMapper.selectPerMinuteCountstiflash_();
        // 初始化剔除当天和昨天的key
        redisChgService.del(key);
        String yesKey = RedisKeyConstant.XIECHENG_RELEASE_TIME + DateUtil.formatDate(DateUtil.yesterday());
        redisChgService.del(yesKey);
        perMinuteCounts.forEach((Map<String, Object> perMinuteCount) -> redisChgService.hset(key, String.valueOf(perMinuteCount.get("releaseTime")),
                String.valueOf(perMinuteCount.get("counts"))));
    }

    @Override
    public void resetCollidingCountAndBuildRobTask() {
        XieChengCollidingDataPackageExample packageExample = new XieChengCollidingDataPackageExample();
        packageExample.createCriteria().andIsDeleteEqualTo(0);
        List<XieChengCollidingDataPackage> packages = packageMapper.selectByExample(packageExample);

        List<XieChengCollidingDataPackage> validPackages = packages.stream().filter(this::checkPackageValid).collect(Collectors.toList());
        // 按轮次分组
        Map<Integer, List<XieChengCollidingDataPackage>> roundPackageMap =
                validPackages.stream().collect(Collectors.groupingBy(XieChengCollidingDataPackage::getRound));

        // 不开启轮次的撞库包
        List<XieChengCollidingDataPackage> nonRoundPackages = roundPackageMap.get(0);

        // 开启轮次的撞库包
        List<XieChengCollidingDataPackage> roundPackages = roundPackageMap.get(1);

        // 重置撞库次数任务
        resetCollidingCount(nonRoundPackages, roundPackages);

        // 生成非周期撞库任务
        buildRobTask(nonRoundPackages, roundPackages);
    }

    private void buildRobTask(List<XieChengCollidingDataPackage> nonRoundPackages, List<XieChengCollidingDataPackage> roundPackages) {
        try {
            if (CollectionUtils.isNotEmpty(nonRoundPackages)) {
                nonRoundPackages.forEach(this::buildRobTask);
            }

            if (CollectionUtils.isEmpty(roundPackages)) {
                return;
            }

            roundPackages.forEach(p -> {
                // 有撞库次数=0且重试次数=0的数据，才生成任务
                Long zeroCount = xieChengCollidingDataRobMapper.selectCountByRoundPackagestiflash_(Lists.newArrayList(p));
                if (zeroCount > 0) {
                    buildRobTask(p);
                }
            });
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程生成非周期撞库任务作业，主线程处理异常"), e);
        }
    }

    private void buildRobTask(XieChengCollidingDataPackage p) {
        List<XiechengCollidingDataPackageRule> packageRules = packageRuleMapper.getPackageRuleByPackageId(p.getId());
        packageRules.forEach(rule -> {
            // 规则开关关闭不生成撞库任务
            if (Objects.equals(1, rule.getCollidingSwitch())) {
                return;
            }

            String startTimes = rule.getStartTimes();
            if (StringUtils.isEmpty(startTimes)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                        "规则id：" + rule.getId().toString(), "携程生成非周期撞库任务作业，规则配置的撞库开始时间为空，不生成撞库任务，需要关注"));
                return;
            }

            int collidingCount = 0;
            for (String startTime : startTimes.split(",")) {
                XieChengCollidingDataRobTask task = new XieChengCollidingDataRobTask();
                task.setPackageId(p.getId());
                task.setRuleId(rule.getId());
                task.setCollidingBackNumber(rule.getCollidingBackNumber());
                task.setPackageCollidingCount(++collidingCount);
                task.setTaskStatus(0);
                startTime = LocalDate.now() + " " + startTime;
                task.setStartTime(DateUtil.parse(startTime, DatePattern.NORM_DATETIME_MINUTE_PATTERN));
                robTaskMapper.insertSelective(task);
            }
        });
    }

    private void resetCollidingCount(List<XieChengCollidingDataPackage> nonRoundPackages, List<XieChengCollidingDataPackage> roundPackages) {
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(20, 20);
        try {
            resetCollidingCountByPackages(nonRoundPackages, threadPool);

            if (CollectionUtils.isEmpty(roundPackages)) {
                return;
            }

            // 有撞库次数=0且重试次数=0的数据，不重置撞库次数
            Long zeroCount = xieChengCollidingDataRobMapper.selectCountByRoundPackagestiflash_(roundPackages);
            if (zeroCount > 0) {
                return;
            }

            resetCollidingCountByPackages(roundPackages, threadPool);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程重置撞库次数作业，重置轮次内数据异常"), e);
        } finally {
            threadPoolShutDown(threadPool);
        }
    }

    /**
     * 根据撞库包查询非周期数据id，根据id将撞库次数置为0
     * @param packages
     * @param threadPool
     */
    private void resetCollidingCountByPackages(List<XieChengCollidingDataPackage> packages, ThreadPoolExecutor threadPool) {
        if (CollectionUtils.isEmpty(packages)) {
            return;
        }

        Long minId = null;
        while (true) {
            List<Long> list;
            try {
                list = xieChengCollidingDataRobMapper.selectRobsByNonRoundPackages(minId, packages);
            } catch (Exception e) {
                log.warn("携程重置撞库次数作业，主线程查询数据异常。", e);
                continue;
            }

            if (CollectionUtils.isEmpty(list)) {
                break;
            }

            minId = list.get(list.size() - 1);

            List<List<Long>> partition = Lists.partition(list, RESETPARTITION);
            for (List<Long> robList : partition) {
                threadPool.submit(() -> {
                    try {
                        xieChengCollidingDataRobMapper.batchResetCollidingCountByIds(robList);
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                                , "携程重置撞库次数作业，子线程处理异常"), e);
                        try {
                            Thread.sleep(3000L);
                            xieChengCollidingDataRobMapper.batchResetCollidingCountByIds(robList);
                        } catch (Exception ex) {
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                                    , "携程重置撞库次数作业，子线程处理再次异常"), ex);
                            Thread.currentThread().interrupt();
                        }
                    }
                });
            }
        }
    }

    private void threadPoolShutDown(ThreadPoolExecutor threadPool) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程重置撞库次数作业线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), ex.getMessage()
                    , "携程重置撞库次数作业，日志保存线程池结束异常"), ex);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 根据撞库规则的撞库开始和结束时间获取有效撞库包
     * @param dataPackage
     * @return
     */
    private boolean checkPackageValid(XieChengCollidingDataPackage dataPackage) {
        List<XiechengCollidingDataPackageRule> packageRules = packageRuleMapper.getPackageRuleByPackageId(dataPackage.getId());
        if (CollectionUtils.isEmpty(packageRules)) {
            return false;
        }

        return true;
    }

    /**
     * 促活数据接入后续处理(消费端在marketing-mq-consumer，保证兆维亦庄流量都可以被消费)
     * @param xieChengActivateDTO
     * @return
     */
    @Override
    public Result<Boolean> activateDataHandle(XieChengActivateDTO xieChengActivateDTO) {
        CustomizeUploadData data = loopCycleMapper.selectActivateData(xieChengActivateDTO);
        if (Objects.isNull(data)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode()
                    , "携程促活，根据id查询前置表数据为空"));
            return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        }
        try {
            JSONArray jsonArray = JSON.parseArray(data.getRequestJsonData());
            List<JSONObject> jsonDataList = jsonArray.stream().map((Object t) -> (JSONObject) t).collect(Collectors.toList());

            ThreadPoolAdjustmentUtil.adjustThreadPoolSize(XIECHENG_ACTIVATE_THREAD_POOL, marketingCommonConfig.getXiechengCollidingActivateThread());
            jsonDataList.forEach((JSONObject jsonData) -> {
                XIECHENG_ACTIVATE_THREAD_POOL.submit(() -> singleHandle(jsonData, data));
            });
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode()
                    , "携程促活，主线程处理异常，前置表id：" + data.getId()), e);
        }

        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }

    private void singleHandle(JSONObject jsonData, CustomizeUploadData data) {
        try {
            String releaseTime = jsonData.getString("releaseTime");
            if (Objects.isNull(releaseTime)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode()
                        , "携程促活，客户未传releaseTime，前置表id:" + data.getId()));
                return;
            }

            String hitRequestNo = jsonData.getString("hitRequestNo");
            if (Objects.isNull(hitRequestNo)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode()
                        , "携程促活，客户未传hitRequestNo，前置表id:" + data.getId()));
                return;
            }

            // 根据hitRequestNo查询cell
            XieChengCollidingDataHitRequestNoMappingExample mappingExample = new XieChengCollidingDataHitRequestNoMappingExample();
            mappingExample.createCriteria().andIsDeleteEqualTo(0).andHitRequestNoEqualTo(hitRequestNo);
            List<XieChengCollidingDataHitRequestNoMapping> hitRequestNoMappings = mappingMapper.selectByExample(mappingExample);
            if (org.springframework.util.CollectionUtils.isEmpty(hitRequestNoMappings)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode()
                        , "携程促活，根据hitRequestNo查询映射表数据为空，前置表id:" + data.getId()));
                return;
            }
            String cellSha256CodeList = hitRequestNoMappings.get(0).getCellSha256CodeList();

            DateTime releaseDateTime = DateUtil.parse(releaseTime, DatePattern.NORM_DATETIME_PATTERN);
            // cell在周期表：更新release_time并打标cpa促活
            XieChengCollidingDataLoopCycle loopCycle = new XieChengCollidingDataLoopCycle();
            loopCycle.setCustomerGroup(2);
            loopCycle.setReleaseTime(releaseDateTime);
            loopCycle.setUpdateTime(new Date());
            XieChengCollidingDataLoopCycleExample loopCycleExample = new XieChengCollidingDataLoopCycleExample();
            loopCycleExample.createCriteria().andIsDeleteEqualTo(0).andCellSha256CodeListEqualTo(cellSha256CodeList);
            int i = loopCycleMapper.updateByExampleSelective(loopCycle, loopCycleExample);
            if (i > 0) {
                return;
            }
            // cell在非周期表：从非周期表删除，写入周期表，更新release_time并打标cpa促活
            XieChengCollidingDataRobExample robExample = new XieChengCollidingDataRobExample();
            robExample.createCriteria().andIsDeleteEqualTo(0).andCellSha256CodeListEqualTo(cellSha256CodeList);
            List<XieChengCollidingDataRob> robs = xieChengCollidingDataRobMapper.selectByExample(robExample);
            if (!org.springframework.util.CollectionUtils.isEmpty(robs)) {
                XieChengCollidingDataRob rob = robs.get(0);
                rob.setReleaseTime(releaseDateTime);
                handleService.activateDataByFalseToTrue(rob);
                return;
            }
            // cell不在撞库表：撞库包=cpa促活数据包，data_source_type='A'，写入周期表，更新release_time并打标cpa促活
            XieChengCollidingDataLoopCycle xieChengCollidingDataLoopCycle = new XieChengCollidingDataLoopCycle();
            xieChengCollidingDataLoopCycle.setReleaseTime(releaseDateTime);
            xieChengCollidingDataLoopCycle.setCustomerGroup(2);
            xieChengCollidingDataLoopCycle.setPackageId(getActivatePackageId());
            xieChengCollidingDataLoopCycle.setDataSourceType("A");
            xieChengCollidingDataLoopCycle.setCellSha256CodeList(cellSha256CodeList);
            xieChengCollidingDataLoopCycle.setRetryCount(0);
            xieChengCollidingDataLoopCycle.setCreateTime(new Date());
            xieChengCollidingDataLoopCycle.setUpdateTime(new Date());
            loopCycleMapper.insertSelective(xieChengCollidingDataLoopCycle);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode()
                    , "携程促活，子线程处理异常"), e);
        }
    }

    private Long getActivatePackageId() {
        XieChengCollidingDataPackageExample packageExample = new XieChengCollidingDataPackageExample();
        packageExample.createCriteria().andPackageNameEqualTo("cpa促活数据包");
        List<XieChengCollidingDataPackage> packages = packageMapper.selectByExample(packageExample);
        return org.springframework.util.CollectionUtils.isEmpty(packages) ? null : packages.get(0).getId();
    }
}
