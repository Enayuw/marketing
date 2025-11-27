package com.br.marketing.service.Impl.wuba;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.entity.WubaCollidingDataFront;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.WubaCollidingDataEliminateMapper;
import com.br.marketing.mapper.WubaCollidingDataFrontMapper;
import com.br.marketing.mapper.WubaCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.WubaCollidingDataRobMapper;
import com.br.marketing.mapper.WubaCollidingDataSecondLoopCycleMapper;
import com.br.marketing.mapper.WubaCollidingDataDelayLoopCycleMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description 58撞库数据同步作业实现类
 * @Author hong.chen
 * @CreateTime 2024/07/10
 */
@Service
@Slf4j
public class WuBaCollidingDataSynchronismServiceImpl implements WuBaCollidingDataSynchronismService {
    public static final String T = "T";
    public static final String S = "S";
    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    private LocalFileMapper localFileMapper;
    @Resource
    WubaCollidingDataFrontMapper wubaCollidingDataFrontMapper;
    @Resource
    WubaCollidingDataLoopCycleMapper wubaCollidingDataLoopCycleMapper;
    @Resource
    WubaCollidingDataSecondLoopCycleMapper wubaCollidingDataSecondLoopCycleMapper;
    @Resource
    WubaCollidingDataEliminateMapper wubaCollidingDataEliminateMapper;
    @Resource
    WubaCollidingDataRobMapper wubaCollidingDataRobMapper;
    @Resource
    WuBaCollidingDataBusinessService wuBaCollidingDataBusinessService;
    @Resource
    private WubaCollidingDataDelayLoopCycleMapper wubaCollidingDataDelayLoopCycleMapper;

    private final static int PARTATION_SIZE = 500;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        marketingCommonConfig.getWubaCollidingApiCodes().forEach((String apiCode) -> {
            LocalFileExample example = new LocalFileExample();
            // 查询待推送文件 查询条件b_local_file：status=2 且 push_status=空
            example.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.WUBA_COLLIDING.getValue())
                    .andStatusEqualTo("2").andPushStatusIsNull().andApiCodeEqualTo(apiCode);
            List<LocalFile> localFiles = localFileMapper.selectByExample(example);
            if (CollectionUtils.isEmpty(localFiles)) {
                return;
            }

            for (LocalFile localFile : localFiles) {
                try {
                    process(localFile);
                    // 只有process执行成功，才更新push_status，否则下次调度时会重新执行
                    updatePushStatus(localFile, "2");
                } catch (Exception e) {
                    String subject = "58撞库数据同步作业异常,localFIleId:" + localFile.getId();
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage()
                            , subject), e);
                }
            }
        });
    }

    private void process(LocalFile localFile) {
        String apiCode = localFile.getApiCode();
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(marketingCommonConfig.getWubaCollidingDataSyncThreadNum(),
                marketingCommonConfig.getWubaCollidingDataSyncThreadNum());

        Long minId = null;
        while (true) {
            Integer pageSize = marketingCommonConfig.getWuBaCollidingDataSyncPageSize();
            // local_id and status =1 and push_status =1，与该文件本身数据去重
            List<WubaCollidingDataFront> wubaCollidingDataFronts = wubaCollidingDataFrontMapper.selectNoDupDataByLocalIdtikv_(localFile.getId(),
                    apiCode, minId, pageSize);
            if (CollectionUtils.isEmpty(wubaCollidingDataFronts)) {
                break;
            }
            minId = wubaCollidingDataFronts.get(wubaCollidingDataFronts.size() - 1).getId();

            modifyThreadPool(pool);

            List<List<WubaCollidingDataFront>> partitions = Lists.partition(wubaCollidingDataFronts, PARTATION_SIZE);
            for (List<WubaCollidingDataFront> partition : partitions) {
                List<WubaCollidingDataFront> list = new ArrayList<>(partition);
                pool.submit(() -> removeDuplicateAndInsertToRob(list, localFile, apiCode));
            }
        }

        threadPoolShutDown(pool);
    }

    @Override
    public List<Long> getHighValueFileIds(String apiCode) {
        List<String> highValueFiles = marketingCommonConfig.getWubaCollidingHighValueFiles();
        if (CollectionUtils.isEmpty(highValueFiles)) {
            return null;
        }

        LocalFileExample localFileExample = new LocalFileExample();
        localFileExample.createCriteria().andApiCodeEqualTo(apiCode).andFileNameIn(highValueFiles);
        List<LocalFile> localFiles = localFileMapper.selectByExample(localFileExample);
        List<Long> highValueIds = localFiles.stream().map(LocalFile::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(highValueIds)) {
            return null;
        }

        return highValueIds;
    }

    private void modifyThreadPool(ThreadPoolExecutor pool) {
        Integer threadNum = marketingCommonConfig.getWubaCollidingDataSyncThreadNum();
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, threadNum);
    }

    private void threadPoolShutDown(ThreadPoolExecutor threadPool) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("58撞库数据同步作业，线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), ex.getMessage()
                    , "58撞库数据同步作业，日志保存线程池结束异常"), ex);
            Thread.currentThread().interrupt();
        }
    }

    private void updatePushStatus(LocalFile localFile, String pushStatus) {
        localFile.setPushStatus(pushStatus);
        localFileMapper.updateByPrimaryKeySelective(localFile);
    }

    private void removeDuplicateAndInsertToRob(List<WubaCollidingDataFront> list, LocalFile localFile, String apiCode) {
        List<WubaCollidingDataFront> insertToRobData = removeDuplicateData(list, apiCode);
        if (CollectionUtils.isEmpty(insertToRobData)) {
            return;
        }
        wuBaCollidingDataBusinessService.insertToRobAndUpdateFront(insertToRobData, localFile);
    }

    /**
     * 1.与延期有效数据去重
     * 2.与周期非金融数据去重
     * 3.与周期金融数据去重
     * 4.与status=-1历史数据去重
     * 5.与周期非金融status=-2包去重、与周期金融status=-2包去重、与补包status=-2包去重
     * 6.与高价值数据去重
     * 7.与当天已入库数据去重
     * @param list
     * @param apiCode
     */
    private List<WubaCollidingDataFront> removeDuplicateData(List<WubaCollidingDataFront> list, String apiCode) {
        try {
            List<String> cells = list.stream().map(WubaCollidingDataFront::getCell).collect(Collectors.toList());

            // 1.与延期有效数据去重
            List<String> delayLoopCycleData = wubaCollidingDataDelayLoopCycleMapper.selectDuplicateData(cells, apiCode);
            cells.removeAll(delayLoopCycleData);
            if (CollectionUtils.isEmpty(cells)) {
                return new ArrayList<>();
            }

            // 2.与周期非金融数据去重
            if (marketingCommonConfig.getWuBaCollidingDataSwitch().get(T)) {
                List<String> loopCycleData = wubaCollidingDataLoopCycleMapper.selectDuplicateData(cells, apiCode);
                cells.removeAll(loopCycleData);
            }
            if (CollectionUtils.isEmpty(cells)) {
                return new ArrayList<>();
            }

            // 3.与周期金融数据去重
            if (marketingCommonConfig.getWuBaCollidingDataSwitch().get(S)) {
                List<String> secondCycleData = wubaCollidingDataSecondLoopCycleMapper.selectDuplicateData(cells, apiCode);
                cells.removeAll(secondCycleData);
            }
            if (CollectionUtils.isEmpty(cells)) {
                return new ArrayList<>();
            }

            // 4.与status=-1历史数据去重
            List<String> secondCycleData = wubaCollidingDataEliminateMapper.selectDuplicateData(cells);
            cells.removeAll(secondCycleData);
            if (CollectionUtils.isEmpty(cells)) {
                return new ArrayList<>();
            }

            // 5.与周期非金融status=-2包去重、与周期金融status=-2包去重、与补包status=-2包去重、与高价值数据去重
            List<Long> fileIds = new ArrayList<>();
            HashMap<String, JSONObject> map = marketingCommonConfig.getWubaCollidingReavedFileIds();
            for (Map.Entry<String, JSONObject> mapEntry : map.entrySet()) {
                for (Map.Entry<String, Object> booleanEntry : mapEntry.getValue().entrySet()) {
                    if ((Boolean) booleanEntry.getValue()) {
                        fileIds.add(Long.valueOf(booleanEntry.getKey()));
                    }
                }
            }
            List<Long> highValueIdList = getHighValueFileIds(apiCode);
            if (Objects.nonNull(highValueIdList)) {
                fileIds.addAll(highValueIdList);
            }
            if (!CollectionUtils.isEmpty(fileIds)) {
                String highValueAndReavedFileIds = "(" + Joiner.on(",").join(fileIds) + ")";
                List<String> highValueAndReavedDuplicateData = wubaCollidingDataRobMapper.selectDuplicateDataByFileId(cells, apiCode,
                        highValueAndReavedFileIds);
                cells.removeAll(highValueAndReavedDuplicateData);
            }
            if (CollectionUtils.isEmpty(cells)) {
                return new ArrayList<>();
            }

            // 6.与当天已入库数据去重
            Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date tomorrow = Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            List<String> duplicateDataByCreateTime = wubaCollidingDataRobMapper.selectDuplicateDataByCreateTime(cells, apiCode, today, tomorrow);
            cells.removeAll(duplicateDataByCreateTime);
            if (CollectionUtils.isEmpty(cells)) {
                return new ArrayList<>();
            }

            return list.stream().filter(t -> cells.contains(t.getCell())).collect(Collectors.toList());
        } catch (Exception e) {
            String subject = "58同步撞库数据作业，数据去重，子线程处理异常！";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage()
                    , subject), e);
            return new ArrayList<>();
        }
    }
}
