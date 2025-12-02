package com.br.marketing.service.tccpa.impl;

import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.entity.TcyrCpaPushData;
import com.br.marketing.mapper.TcyrCpaPushDataMapper;
import com.br.marketing.mapper.TcyrCpaPushFileTaskVtMapper;
import com.br.marketing.service.tccpa.TcCpaPushFileGenVTService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class TcCpaPushFileGenVTServiceImpl implements TcCpaPushFileGenVTService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcyrCpaPushDataMapper tcyrCpaPushDataMapper;

    @Resource
    private TcyrCpaPushFileTaskVtMapper tcyrCpaPushFileTaskVtMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 执行TcCpaPushFileGenVTJob的主要逻辑
     */
    @Override
    public void process() {
        TpDynamicExecutor actionPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.TCYR_CPA_COLLIDING_DEAL.getName(), 50, 50);
        try {
            Integer taskId = tcyrCpaPushFileTaskVtMapper.selectLatestTaskIdByStatus(2);
            if (taskId == null) {
                log.warn("未找到status=2的任务记录，任务结束");
                return;
            }

            log.info("获取到最新taskId: {}", taskId);

            // 2. 获取总数据量用于分页
            int totalCount = tcyrCpaPushDataMapper.countByTaskId(taskId);
            if (totalCount == 0) {
                log.warn("taskId: {} 对应的数据量为0，任务结束", taskId);
                return;
            }

            log.info("taskId: {} 总数据量: {}", taskId, totalCount);
            // 多线程处理数据
            String filePath = marketingCommonConfig.getTcCpaFilePushConfig().getString("filePath");
            processDataWithMultiThread(taskId, totalCount, actionPool, filePath);
            // 生成.ok文件
            generateOkFile(filePath);
            tcyrCpaPushFileTaskVtMapper.updateStatusByTaskId(taskId, 4);
        } catch (Exception e) {
            log.error("TcCpaPushFileGenVTJob执行异常", e);
            throw new RuntimeException("文件生成任务执行失败", e);
        } finally {
            // 关闭线程池
            actionPool.shutdownAndAwaitTermination();
        }
    }

    /**
     * 多线程处理数据
     */
    private void processDataWithMultiThread(Integer taskId, int totalCount, TpDynamicExecutor actionPool, String targetDirectory) {
        int batchSize = 1000; // 每批处理数量
        int totalPages = (totalCount + batchSize - 1) / batchSize;

        log.info("开始多线程处理，总页数: {}, 每批大小: {}", totalPages, batchSize);

        // 使用CompletableFuture进行多线程处理
        CompletableFuture<?>[] futures = new CompletableFuture[totalPages];
        AtomicInteger successCount = new AtomicInteger(0);

        for (int page = 0; page < totalPages; page++) {
            final int currentPage = page;
            final int offset = currentPage * batchSize;

            futures[currentPage] = CompletableFuture.runAsync(() -> {
                try {
                    processBatchData(taskId, currentPage, batchSize, offset, targetDirectory);
                    successCount.incrementAndGet();
                    log.debug("第{}批数据处理完成", currentPage + 1);
                } catch (Exception e) {
                    log.error("第{}批数据处理异常", currentPage + 1, e);
                    throw new RuntimeException(e);
                }
            }, actionPool);
        }
        CompletableFuture.allOf(futures).join();
    }

    /**
     * 处理批次数据
     */
    public void processBatchData(Integer taskId, int pageNum, int batchSize, int offset, String targetDirectory) {
        // 1. 查询批次数据
        List<TcyrCpaPushData> batchData = tcyrCpaPushDataMapper.selectByTaskIdWithPagination(
                taskId, offset, batchSize);

        if (CollectionUtils.isEmpty(batchData)) {
            log.debug("第{}页无数据，taskId: {}", pageNum + 1, taskId);
            return;
        }

        // 2. 生成CSV文件
        String fileName = generateFileName(pageNum);
        generateCsvFile(batchData, fileName, targetDirectory);
    }

    /**
     * 生成文件名
     */
    private String generateFileName(int pageNum) {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        return String.format("%s_%d.csv", dateStr, pageNum + 1);
    }

    /**
     * 生成CSV文件
     */
    private void generateCsvFile(List<TcyrCpaPushData> dataList, String fileName, String targetDirectory) {
        String filePath = targetDirectory + File.separator + fileName;
        File file = new File(filePath);

        // 确保目录存在
        File directory = file.getParentFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // 写入CSV头部
            writer.write("userKey");
            writer.newLine();

            // 写入数据行
            for (TcyrCpaPushData data : dataList) {
                String line = data.getUserKey();
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            log.error("生成CSV文件失败: {}", filePath, e);
            throw new RuntimeException("文件生成失败", e);
        }
    }

    /**
     * 生成.ok文件
     */
    private void generateOkFile(String filePath) {
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);
        String okFileName = dateStr + ".ok";
        String okFilePath = filePath + File.separator + okFileName;

        File okFile = new File(okFilePath);
        try {
            if (okFile.createNewFile()) {
                log.info("OK文件生成成功: {}", okFilePath);
            } else {
                log.warn("OK文件已存在: {}", okFilePath);
            }
        } catch (IOException e) {
            log.error("生成OK文件失败: {}", okFilePath, e);
            throw new RuntimeException("OK文件生成失败", e);
        }
    }
}
