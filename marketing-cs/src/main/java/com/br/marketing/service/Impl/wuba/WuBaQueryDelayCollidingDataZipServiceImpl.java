package com.br.marketing.service.Impl.wuba;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.br.common.log.AlertLog;
import com.br.marketing.client.wuba.WuBaServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.file.ZipUtils;
import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.entity.WubaCollidingDataDelayLoopCycle;
import com.br.marketing.entity.WubaCollidingDataDelayLoopCycleExample;
import com.br.marketing.mapper.MarketingCleanDataTaskMapper;
import com.br.marketing.mapper.WubaCollidingDataDelayLoopCycleMapper;
import com.br.marketing.mapper.WubaCollidingDataLoopCycleMapper;
import com.br.marketing.service.DataCleaningAutoService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description WuBaQueryDelayCollidingDataZipServiceImpl
 * @Author hong.chen
 * @CreateTime 2025/05/09
 */
@Service
@Slf4j
public class WuBaQueryDelayCollidingDataZipServiceImpl implements WuBaQueryDelayCollidingDataZipService {
    private static final String TITLE = "【58延期数据zip包处理】";

    private static final int BATCH_SIZE = 50;
    @Resource
    private WuBaServiceClient wuBaServiceClient;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private DataCleaningAutoService cleaningAutoService;

    @Resource
    private MarketingCleanDataTaskMapper cleanDataTaskMapper;

    @Resource
    private WubaCollidingDataDelayLoopCycleMapper delayLoopCycleMapper;

    @Resource
    WubaCollidingDataLoopCycleMapper loopCycleMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String collectDate = context.getJobParameter();
        if (StringUtils.isBlank(collectDate)) {
            collectDate = LocalDate.now().toString();
        }
        String finalCollectDate = collectDate;
        marketingCommonConfig.getWubaCollidingApiCodes().forEach((String apiCode) -> {
            File[] files = callWuBaServiceAndGetFiles(finalCollectDate);
            if (files == null) {
                String msg = TITLE + "解压csv文件不存在";
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                        , TITLE));
                return;
            }

            // 创建线程池
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(10, 10);

            // 获取表头配置
            Map<String, Integer> headerConfig = marketingCommonConfig.getWuBaQueryDelayZipHeaderConfig();

            // 生成清洗任务
            Long taskId = cleaningAutoService.saveCleanTask(apiCode, 1, "58新客_延期撞库数据_上传清洗规则勿动");

            // 文件解析入库
            for (File csvFile : files) {
                if (!csvFile.getName().contains(".csv")) {
                    String msg = TITLE + "解压文件不是csv文件";
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                            , TITLE));
                    continue;
                }

                // 读取文件
                try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
                    String header = reader.readLine();
                    if (header == null) {
                        String errMsg = TITLE + "文件表头为空，path：" + csvFile.getAbsolutePath();
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), errMsg));
                    }

                    // 每50行数据1个线程
                    List<String> batchData = new ArrayList<>();
                    String dataLine;
                    List<CompletableFuture<Void>> futures = new ArrayList<>();
                    while ((dataLine = reader.readLine()) != null) {
                        batchData.add(dataLine);
                        if (batchData.size() == BATCH_SIZE) {
                            ThreadPoolAdjustmentUtil.adjustThreadPoolSize(threadPool, marketingCommonConfig.getWuBaQueryDelayZipThreadNum());

                            ArrayList<String> subList = new ArrayList<>(batchData);
                            futures.add(CompletableFuture.runAsync(() -> deleteLoopCycleAndSaveDelay(subList, apiCode, headerConfig, taskId)
                                    , threadPool));
                            batchData.clear();
                        }
                    }
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                    // 主线程处理尾量数据
                    if (!batchData.isEmpty()) {
                        deleteLoopCycleAndSaveDelay(new ArrayList<>(batchData), apiCode, headerConfig, taskId);
                    }
                } catch (Exception e) {
                    String msg = TITLE + "csv文件处理异常";
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage()
                            , msg));
                }
            }

            // 更新清洗任务表
            MarketingCleanDataTask cleanDataTaskUpdate = new MarketingCleanDataTask();
            cleanDataTaskUpdate.setId(taskId);
            cleanDataTaskUpdate.setCleanStatus(0);
            cleanDataTaskMapper.updateByPrimaryKeySelective(cleanDataTaskUpdate);
            log.warn(TITLE + "更新清洗任务成功");

            threadPoolShutDown(threadPool);
        });
    }

    private File[] callWuBaServiceAndGetFiles(String finalCollectDate) {
        File[] files;
        String dirPath = marketingCommonConfig.getWuBaQueryDelayZipResultFilePath();
        String zipFileName = "extend_bairongkj_" + finalCollectDate + ".csv.zip";
        String zipFilePath = dirPath.concat(zipFileName);
        Result callResult = wuBaServiceClient.queryDelayZipResult(finalCollectDate, zipFilePath);
        if (callResult == null || !callResult.isSuccess()) {
            String msg = TITLE + "下载zip包失败";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                    , TITLE));
            return null;
        }
        log.warn(TITLE + "下载zip包成功");

        // unzip
        File zipFile = new File(zipFilePath);
        if (!zipFile.exists() || !zipFile.getName().contains(".zip")) {
            String msg = TITLE + "zip文件不存在";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg
                    , TITLE));
            return null;
        }

        String csvDirPath = dirPath + "csv/" + finalCollectDate + "/";
        ZipUtils.unZip(zipFile, csvDirPath, "");
        log.warn(TITLE + "解压zip包成功");

        File csvDir = new File(csvDirPath);
        files = csvDir.listFiles();

        return files;
    }

    private void deleteLoopCycleAndSaveDelay(List<String> batchData, String apiCode, Map<String, Integer> headerConfig, Long taskId) {
        try {
            List<String> dataList;
            List<WubaCollidingDataDelayLoopCycle> delayLoopCycleList = new ArrayList<>();
            // 遍历每行
            for (String dataLine : batchData) {
                dataList = Arrays.asList(dataLine.split(",", -1));

                WubaCollidingDataDelayLoopCycle delayLoopCycle = new WubaCollidingDataDelayLoopCycle();
                delayLoopCycle.setCell(dataList.get(headerConfig.get("cell")).trim());
                delayLoopCycle.setBatchNo(dataList.get(headerConfig.get("batchNo")).trim());
                delayLoopCycle.setReleaseTime(DateUtil.parse(dataList.get(headerConfig.get("releaseTime")).trim(),
                        DatePattern.NORM_DATETIME_PATTERN));
                delayLoopCycle.setLastPushTime(DateUtil.parse(dataList.get(headerConfig.get("lastPushTime")).trim(),
                        DatePattern.NORM_DATETIME_PATTERN));
                delayLoopCycle.setExtend(JSON.toJSONString(dataLine));
                delayLoopCycle.setCleanStatus(0);
                delayLoopCycle.setApiCode(apiCode);
                delayLoopCycle.setTaskId(taskId);

                delayLoopCycleList.add(delayLoopCycle);
            }

            List<String> cells = delayLoopCycleList.stream().map(WubaCollidingDataDelayLoopCycle::getCell).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(cells)) {
                return;
            }

            // 从非金融周期表删除
            loopCycleMapper.batchDeleteByCell(cells, apiCode);

            WubaCollidingDataDelayLoopCycleExample example = new WubaCollidingDataDelayLoopCycleExample();
            example.createCriteria().andCellIn(cells).andApiCodeEqualTo(apiCode).andIsDeletedEqualTo(0);
            List<WubaCollidingDataDelayLoopCycle> existDelayLoopCycles = delayLoopCycleMapper.selectByExample(example);
            List<String> existCells = existDelayLoopCycles.stream().map(WubaCollidingDataDelayLoopCycle::getCell).collect(Collectors.toList());
            List<WubaCollidingDataDelayLoopCycle> updateDelayLoopCycles =
                    delayLoopCycleList.stream().filter(t -> existCells.contains(t.getCell())).collect(Collectors.toList());

            // 延期表已存在，则更新释放时间
            if (!CollectionUtils.isEmpty(updateDelayLoopCycles)) {
                delayLoopCycleMapper.batchUpdateReleaseTimeByCell(updateDelayLoopCycles);
            }

            // 延期表不存在，则插入
            List<WubaCollidingDataDelayLoopCycle> newDelayLoopCycles =
                    delayLoopCycleList.stream().filter(t -> !existCells.contains(t.getCell())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(newDelayLoopCycles)) {
                delayLoopCycleMapper.batchSaveData(newDelayLoopCycles);
            }
        } catch (Exception e) {
            String msg = TITLE + "子线程处理异常";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage()
                    , msg));
        }
    }

    private void threadPoolShutDown(ThreadPoolExecutor threadPool) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("58延期数据zip包处理，线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),
                    "58延期数据zip包处理，日志保存线程池结束异常！errorMessage=" + ex.getMessage()), ex);
            Thread.currentThread().interrupt();
        }
    }
}
