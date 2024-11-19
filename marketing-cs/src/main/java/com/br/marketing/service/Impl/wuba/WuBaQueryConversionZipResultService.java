package com.br.marketing.service.Impl.wuba;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.wuba.WuBaServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.file.ZipUtils;
import com.br.marketing.dto.wuba.WuBaQueryConversionZipResultDto;
import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.entity.WubaSubmitConversionDataTransferClean;
import com.br.marketing.mapper.MarketingCleanDataTaskMapper;
import com.br.marketing.mapper.WubaSubmitConversionDataTransferCleanMapper;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.service.DataCleaningAutoService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description 58新客转化数据zip包清洗
 * @Author lixiang
 * @Date 2024-11-19
 */
@Service
@Slf4j
public class WuBaQueryConversionZipResultService {

    private static final String TITLE = "【58新客转化数据zip包清洗】";

    private Integer PARTITION_SIZE = 2000;

    @Resource
    private WuBaServiceClient wuBaServiceClient;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private DataCleaningAutoService cleaningAutoService;

    @Resource
    private MarketingCleanDataTaskMapper cleanDataTaskMapper;

    @Resource
    private WubaSubmitConversionDataTransferCleanMapper dataTransferCleanMapper;

    public Result action(Page2Condition<WuBaQueryConversionZipResultDto> condition) {
        Result result = new Result<>().failure();
        try {
            // 扫描批次, BatchType 2-上报
            WuBaQueryConversionZipResultDto param = condition.getParam();
            String apiCode = param.getApiCode();
            String bizDate = param.getBizDate();

            // wuBaServiceClient
            String dirPath = marketingCommonConfig.getWuBaQueryConversionZipResultDownloadPath();
            String zipFileName = "bairongkj_" + bizDate + ".csv.zip";
            String zipFilePath = dirPath.concat(zipFileName);
            Result callResult = wuBaServiceClient.queryConversionZipResult(bizDate, zipFilePath);
            if (callResult == null || !callResult.isSuccess()) {
                log.warn(TITLE + "下载zip包失败");
                return result.failure();
            }
            log.warn(TITLE + "下载zip包成功");

            // unzip
            File zipFile = new File(zipFilePath);
            if (!zipFile.exists() || !zipFile.getName().contains(".zip")) {
                log.warn(TITLE + "zip文件不存在");
                return result.failure();
            }

            ZipUtils.unZip(zipFile, dirPath, "");

            File dir = new File(dirPath);
            File[] files = dir.listFiles();
            if (files == null) {
                log.warn(TITLE + "解压csv文件不存在");
                return result.failure();
            }
            for (File csvFile : files) {
                if (!csvFile.getName().contains(".csv")) {
                    log.warn(TITLE + "解压文件不是csv文件");
                    return result.failure();
                }
                // 生成清洗任务
                Long taskId = cleaningAutoService.saveCleanTask(apiCode, 1, "58新客_转化清洗规则勿动");

                // 更新清洗任务表
                MarketingCleanDataTask cleanDataTaskUpdate = new MarketingCleanDataTask();
                cleanDataTaskUpdate.setId(taskId);
                cleanDataTaskUpdate.setCleanStatus(0);
                cleanDataTaskMapper.updateByPrimaryKeySelective(cleanDataTaskUpdate);
                log.warn(TITLE + "更新清洗任务成功");
            }

        } catch (Exception e) {
            log.warn(TITLE + "action error", e);
            return result.failure();
        }


        return result.success();
    }

    public void parseFile(String apiCode, Long taskId, File csvFile, Map<String, String> headerMapping) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            // 读取表头
            String headerLine = reader.readLine();
            String[] headers = headerLine.split(",");

            // 批量处理数据
            List<String> lineBuffer = new ArrayList<>();
            ThreadPoolExecutor actionPool = BrExecutors.getThreadPool(10, 10);
            List<CompletableFuture<Result>> futureList = new ArrayList<>();
            List<Long> resultList = Collections.synchronizedList(new ArrayList<>(20));

            String line;
            Long totalLine = 0L;
            Long successLine = 0L;
            while ((line = reader.readLine()) != null) {
                lineBuffer.add(line);
                if (lineBuffer.size() >= PARTITION_SIZE) {
                    processList(apiCode, taskId, lineBuffer, headers, headerMapping, actionPool, futureList, resultList);
                    totalLine += lineBuffer.size();
                    lineBuffer.clear();
                }
            }

            // 处理剩余数据
            if (!lineBuffer.isEmpty()) {
                processList(apiCode, taskId, lineBuffer, headers, headerMapping, actionPool, futureList, resultList);
            }

            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
            log.warn(TITLE + "all process complete");

            for (Long total : resultList) {
                successLine += total;
            }

            log.warn(TITLE + "totalLine: {}, successLine: {}", totalLine, successLine);
            shutdownThreadPool(actionPool);
        } catch (IOException e) {
            log.error(TITLE + "parseFile error", e);
            throw new RuntimeException(e);
        }
    }

    public Result processList(String apiCode, Long taskId, List<String> lineBuffer, String[] headers, Map<String, String> headerMapping
            , ThreadPoolExecutor actionPool, List<CompletableFuture<Result>> futureList, List<Long> resultList) {
        Result result = new Result().failure();
        actionPool.setCorePoolSize(marketingCommonConfig.getWuBaQueryConversionBatDBThreadPool());
        actionPool.setMaximumPoolSize(marketingCommonConfig.getWuBaQueryConversionBatDBThreadPool());

        futureList.add(CompletableFuture.supplyAsync(() -> processData(apiCode, taskId, lineBuffer, headers, headerMapping), actionPool)
                .whenComplete((processDataResult, throwable) -> {
                    if (processDataResult == null || !processDataResult.isSuccess()) {
                        resultList.add(0L);
                        return;
                    }
                    resultList.add((Long) processDataResult.getData());
                    if (throwable != null) {
                        log.error(TITLE + "completableFuture error:{}", throwable);
                        resultList.add(0L);
                    }
                })
        );
        return result.success();
    }

    public Result processData(String apiCode, Long taskId, List<String> lineBuffer, String[] headers, Map<String, String> headerMapping) {
        Result result = new Result().failure();
        try {
            Result processResult = processLineBuffer(apiCode, taskId, lineBuffer, headers, headerMapping);
            if (processResult == null || !processResult.isSuccess() || processResult.getData() == null) {
                return result.failure();
            }
            List<WubaSubmitConversionDataTransferClean> dataList = (List<WubaSubmitConversionDataTransferClean>) processResult.getData();
            if (CollectionUtils.isEmpty(dataList)) {
                return result.failure();
            }
            batAddDataTransferClean(dataList);
            Long successLine = Long.valueOf(dataList.size());
            return result.success().setDate(successLine);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),
                    TITLE + "批量保存转化结果异常"));
            return result.failure();
        }
    }

    public Result processLineBuffer(String apiCode, Long taskId, List<String> lineBuffer, String[] headers
            , Map<String, String> headerMapping) {
        Result result = new Result().failure();
        if (CollectionUtils.isEmpty(lineBuffer)) {
            return result.success();
        }

        List<WubaSubmitConversionDataTransferClean> dataList = new ArrayList<>();

        for (String line : lineBuffer) {
            Map<String, Object> dataMap = new HashMap<>();
            JSONObject extendJo = new JSONObject();
            String[] lineValues = line.split(",");
            for (String header : headers) {
                String value = lineValues[Arrays.asList(headers).indexOf(header)];
                if (headerMapping.containsKey(header)) {
                    dataMap.put(headerMapping.get(header), value);
                    continue;
                }
                extendJo.put(header, value);
            }
            WubaSubmitConversionDataTransferClean data = BeanUtil.toBean(dataMap, WubaSubmitConversionDataTransferClean.class);
            data.setApiCode(apiCode);
            data.setBatchNo("");
            Date pushTime = DateUtil.date(LocalDateTime.now().minusDays(1));
            data.setPushTime(pushTime);
            data.setCleanStatus(0);
            data.setTaskId(taskId);
            dataList.add(data);
        }
        return result.success().setDate(dataList);
    }

    private void batAddDataTransferClean(List<WubaSubmitConversionDataTransferClean> batList) {
        dataTransferCleanMapper.batchAdd(batList);
        log.warn(TITLE + "保存转化结果成功");
    }

    public static void shutdownThreadPool(ThreadPoolExecutor executor) {
        log.warn(TITLE + "shutdownThreadPool开始");
        long taskCount = -1;
        executor.shutdown();
        try {
            while (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                long completedTaskCount = executor.getCompletedTaskCount();
                if (taskCount == completedTaskCount) {
                    log.warn(TITLE + "业务线程等待超时");
                    break;
                }
                taskCount = completedTaskCount;
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
        } catch (Throwable e) {
            log.warn(TITLE + "ThreadPoolManager shutdown executor has error : ", e);
        }
        log.warn(TITLE + "shutdownThreadPool结束");
    }
}
