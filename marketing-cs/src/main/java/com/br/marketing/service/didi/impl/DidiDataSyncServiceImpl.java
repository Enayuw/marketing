package com.br.marketing.service.didi.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.didi.DiDiDataSyncService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DidiDataSyncServiceImpl implements DiDiDataSyncService {

    private final static String TITLE = "【滴滴V5-筛选数据】";

    private final static int PARTATION_SIZE = 500;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    private DiDiV5CollidingDataMapper diDiV5CollidingDataMapper;

    @Resource
    private DidiCallBackDataMapper didiCallBackDataMapper;

    @Resource
    private DiDiV5CollidingDataLogMapper diDiV5CollidingDataLogMapper;

    @Resource
    private DiDiV5CollidingDataRobMapper diDiV5CollidingDataRobMapper;

    @Resource
    private DiDiV5DataLoopCycleMapper diDiV5DataLoopCycleMapper;

    /**
     * 滴滴V5筛选job执行方法
     */
    @Override
    public void sync() {
        JSONObject pushConfig = marketingCommonConfig.getDiDiV5Config();
        String apiCode = pushConfig.getString("apiCode");
        Integer collidingLimit = pushConfig.getInteger("collidingLimit");
        Integer pageSize = pushConfig.getInteger("limit");

        LocalFileExample example = new LocalFileExample();
        // 查询待推送文件 查询条件b_local_file：status=2 且 push_status=空
        example.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.DD.getValue())
                .andStatusEqualTo("2").andPushStatusIsNull().andApiCodeEqualTo(apiCode);
        List<LocalFile> localFiles = localFileMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(localFiles)) {
            return;
        }
        localFiles = localFiles.stream().sorted(Comparator.comparing(LocalFile::getFileName)).collect(Collectors.toList());

        // 记录已处理的数据总量
        int cycleCount = diDiV5DataLoopCycleMapper.queryCollidingDataAmount(DateUtil.beginOfDay(DateUtil.tomorrow()),
                DateUtil.endOfDay(DateUtil.tomorrow()));
        Date afterTomorrow = getAfterTomorrow();
        int preCount = diDiV5CollidingDataRobMapper.queryCollidingDataAmount(DateUtil.beginOfDay(afterTomorrow));
        int totalProcessedCount = cycleCount + preCount;

        for (LocalFile localFile : localFiles) {
            DiDiV5CollidingDataExample dataExample = new DiDiV5CollidingDataExample();
            dataExample.createCriteria().andApiCodeEqualTo(apiCode).andLocalIdEqualTo(localFile.getId()).andPushStatusEqualTo(0)
                    .andCollidingTimeBetween(DateUtil.beginOfDay(DateUtil.tomorrow()), DateUtil.endOfDay(DateUtil.tomorrow()));
            if (diDiV5CollidingDataMapper.countByExample(dataExample) == 0) {
                continue;
            }
            try {
                totalProcessedCount += process(localFile);
                updatePushStatus(localFile);
            } catch (Exception e) {
                String subject = TITLE + localFile.getId();
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(), e.getMessage()
                        , subject), e);
            }
        }
        // 检查是否需要从后天数据中补数
        if (totalProcessedCount < collidingLimit) {
            int needCount = collidingLimit - totalProcessedCount;
            log.info("已处理数据量{}不足collidingLimit{}, 需要从后天数据中补充{}条",
                    totalProcessedCount, collidingLimit, needCount);

            // 从后天数据中补数
            supplementDataFromAfterTomorrow(apiCode, pageSize, needCount);
        }
    }

    private static Date getAfterTomorrow() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 2);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 从后天数据中补充数据
     */
    private void supplementDataFromAfterTomorrow(String apiCode, Integer pageSize, int needCount) {
        // 获取后天0点时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 2);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date afterTomorrow = calendar.getTime();

        // 获取明天0点时间
        calendar.add(Calendar.DATE, -1);
        Date tomorrow = calendar.getTime();

        TpDynamicExecutor pushPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.DIDI_V5_FILTER.getName(), 50, 50);

        Long minId = 0L;
        AtomicInteger currentSupplementedCount = new AtomicInteger();

        while (currentSupplementedCount.get() < needCount) {
            JSONObject pushConfig = marketingCommonConfig.getDiDiV5Config();
            if (pushConfig.getBooleanValue("syncSwitch")) {
                log.warn("检测到中断信号，停止补充数据");
                break;
            }

            // 查询后天数据
            List<DiDiV5CollidingData> afterTomorrowDataList = diDiV5CollidingDataMapper.selectNoDupDataByDateAndIdtikv_(
                    apiCode, afterTomorrow, minId, pageSize);
            if (CollectionUtils.isEmpty(afterTomorrowDataList)) {
                break;
            }
            minId = afterTomorrowDataList.get(afterTomorrowDataList.size() - 1).getId();
            List<List<DiDiV5CollidingData>> partitions = Lists.partition(afterTomorrowDataList, PARTATION_SIZE);
            for (List<DiDiV5CollidingData> partition : partitions) {
                if (currentSupplementedCount.get() >= needCount) {
                    break;
                }
                List<DiDiV5CollidingData> list = new ArrayList<>(partition);
                pushPool.submit(() -> supplementAndInsertToRob(list, apiCode, tomorrow,
                        needCount, currentSupplementedCount));
            }
        }
        pushPool.shutdownAndAwaitTermination();
    }

    private void updatePushStatus(LocalFile localFile) {
        localFile.setPushStatus("2");
        localFileMapper.updateByPrimaryKeySelective(localFile);
    }

    private void supplementAndInsertToRob(List<DiDiV5CollidingData> list, String apiCode,
                                          Date tomorrow, int needCount, AtomicInteger currentCount) {
        try {
            // 过滤数据
            List<DiDiCollidingDataRob> insertToRobData = filter(list, apiCode, tomorrow);
            if (CollectionUtils.isEmpty(insertToRobData)) {
                return;
            }
            int actualInsertCount;
            while (true) {
                int expected = currentCount.get();
                if (expected >= needCount) {
                    return;
                }
                int remaining = needCount - expected;
                if (remaining <= 0) {
                    return;
                }
                int canInsert = Math.min(insertToRobData.size(), remaining);
                if (currentCount.compareAndSet(expected, expected + canInsert)) {
                    actualInsertCount = canInsert;
                    break;
                }
            }

            if (actualInsertCount < insertToRobData.size()) {
                insertToRobData = insertToRobData.subList(0, actualInsertCount);
            }
            if (CollectionUtils.isNotEmpty(insertToRobData)) {
                diDiV5CollidingDataRobMapper.insertToRobAndUpdateFront(insertToRobData);
                List<Long> ids = insertToRobData.stream()
                        .map(DiDiCollidingDataRob::getDataId)
                        .collect(Collectors.toList());
                diDiV5CollidingDataMapper.updateCollidingTimeByIds(3, tomorrow, ids);
            }

        } catch (Exception e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(), e.getMessage()
                    , TITLE + "补充数据"), e);
        }
    }

    /**
     * 处理文件数据
     */
    private int process(LocalFile localFile) {
        String apiCode = localFile.getApiCode();
        TpDynamicExecutor pushPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.DIDI_V5_FILTER.getName(), 50, 50);
        Long minId = 0L;
        AtomicInteger processedCount = new AtomicInteger(0);
        while (true) {
            JSONObject pushConfig = marketingCommonConfig.getDiDiV5Config();
            if (pushConfig.getBooleanValue("syncSwitch")) {
                log.warn("检测到中断信号，停止同步");
                break;
            }
            Integer pageSize = pushConfig.getInteger("limit");
            List<DiDiV5CollidingData> collidingDataList = diDiV5CollidingDataMapper.selectNoDupDataByLocalIdtikv_(localFile.getId(),
                    apiCode, minId, pageSize, DateUtil.tomorrow());
            if (CollectionUtils.isEmpty(collidingDataList)) {
                break;
            }
            List<Long> ids = collidingDataList.stream().map(DiDiV5CollidingData::getId).toList();
            diDiV5CollidingDataMapper.updatePushStatusByIds(1, ids);
            minId = collidingDataList.get(collidingDataList.size() - 1).getId();
            List<List<DiDiV5CollidingData>> partitions = Lists.partition(collidingDataList, PARTATION_SIZE);
            for (List<DiDiV5CollidingData> partition : partitions) {
                List<DiDiV5CollidingData> list = new ArrayList<>(partition);
                pushPool.submit(() -> removeDuplicateAndInsertToRob(list, localFile, apiCode, processedCount));
            }
            diDiV5CollidingDataMapper.updatePushStatusByIds(3, ids);
        }
        pushPool.shutdownAndAwaitTermination();
        return processedCount.get();
    }

    private void removeDuplicateAndInsertToRob(List<DiDiV5CollidingData> list, LocalFile localFile,
                                               String apiCode, AtomicInteger processedCount) {
        try {
            List<DiDiCollidingDataRob> insertToRobData = filter(list, apiCode, null);
            if (CollectionUtils.isEmpty(insertToRobData)) {
                return;
            }
            diDiV5CollidingDataRobMapper.insertToRobAndUpdateFront(insertToRobData);
            processedCount.addAndGet(insertToRobData.size());
        } catch (Exception e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(), e.getMessage()
                    , TITLE + localFile.getId()), e);
        }
    }

    /**
     * 过滤数据
     *
     * @param newCollidingTime 新的碰撞时间，如果为null则使用原始时间
     */
    private List<DiDiCollidingDataRob> filter(List<DiDiV5CollidingData> list, String apiCode, Date newCollidingTime) {
        try {
            JSONObject pushConfig = marketingCommonConfig.getDiDiV5Config();
            Set<String> cells = list.stream().map(DiDiV5CollidingData::getCell).collect(Collectors.toSet());

            // 1.去重规则1
            List<String> cycleCells = diDiV5DataLoopCycleMapper.selectUnpushedCells(cells, apiCode);
            if (!CollectionUtils.isEmpty(cycleCells)) {
                cycleCells.forEach(cells::remove);
            }

            // 2.去重规则2
            List<String> robCells = diDiV5CollidingDataRobMapper.selectUnpushedCells(cells, apiCode);
            if (!CollectionUtils.isEmpty(robCells)) {
                robCells.forEach(cells::remove);
            }

            // 1.筛选规则1
            boolean preScreen1 = pushConfig.getBoolean("preScreen1");
            if(preScreen1) {
                List<String> delayLoopCycleData = didiCallBackDataMapper.selectPushedCells(cells, apiCode);
                delayLoopCycleData.forEach(cells::remove);
                if (CollectionUtils.isEmpty(cells)) {
                    return Lists.newArrayList();
                }
            }

            // 2.筛选规则2
            boolean preScreen2 = pushConfig.getBoolean("preScreen2");
            if(preScreen2) {
                List<Integer> failMsgs = pushConfig.getObject("failMsgs", List.class);
                if (!CollectionUtils.isEmpty(failMsgs)) {
                    List<String> loopCycleData = diDiV5CollidingDataLogMapper.checkCellBatchFailMsgs(cells, failMsgs);
                    loopCycleData.forEach(cells::remove);
                }
                if (CollectionUtils.isEmpty(cells)) {
                    return Lists.newArrayList();
                }
            }
            List<DiDiCollidingDataRob> robList = list.stream().filter(t -> cells.contains(t.getCell()))
                    .map(t -> {
                        DiDiCollidingDataRob diDiCollidingDataRob = new DiDiCollidingDataRob();
                        BeanUtils.copyProperties(t, diDiCollidingDataRob);
                        diDiCollidingDataRob.setPackageId(t.getLocalId());
                        diDiCollidingDataRob.setSourceType("F");
                        diDiCollidingDataRob.setIsDelete(0);
                        diDiCollidingDataRob.setDataId(t.getId());
                        if (newCollidingTime != null) {
                            diDiCollidingDataRob.setCollidingTime(newCollidingTime);
                        }
                        return diDiCollidingDataRob;
                    }).collect(Collectors.toList());
            return robList;
        } catch (Exception e) {
            String subject = TITLE + "数据剔除，子线程处理异常！";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(), e.getMessage()
                    , subject), e);
            return new ArrayList<>();
        }
    }
}
