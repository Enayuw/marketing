package com.br.marketing.service.Impl;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.NfsFileTOBiRecord;
import com.br.marketing.entity.NfsFileTOBiRecordExample;
import com.br.marketing.mapper.BFileBiConfigMapper;
import com.br.marketing.mapper.NfsFileTOBiRecordMapper;
import com.br.marketing.mapper.TransferFileExtractToDorisBIMapper;
import com.br.marketing.service.TransFileToMarketingBiShardService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.TransFileToBiConfigRecordVO;
import com.google.api.client.util.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zhiyong.zhang
 * @description: 内部服务器的转化文件落库到marketingBI(分片)
 * @date 2025/06/30
 */
@Slf4j
@Service
public class TransFileToMarketingBiShardServiceImpl implements TransFileToMarketingBiShardService {

    private static final String TITLE = "【内部服务器的转化文件落库到marketingBI】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TransferFileExtractToDorisBIMapper transferFileExtractToDorisBIMapper;

    @Resource
    private BFileBiConfigMapper bFileBiConfigMapper;

    @Resource
    private NfsFileTOBiRecordMapper nfsFileTOBiRecordMapper;

    @Resource
    private RedisChgService redisChgService;

    private static final int BATCH_SIZE = 1000;

    @Override
    public void process(String jobParameter, List<Integer> shardingItems) {
        // 1. 识别优先级分片组
        Integer priorityStatus = marketingCommonConfig.getTransFilePriorityList().containsAll(shardingItems) ? 1 : 0;
        String dateStr = StringUtils.isNotEmpty(jobParameter)
                ? jobParameter
                : LocalDate.now().toString().replace("-", "");
        for (String dataDate : dateStr.split(",")) {
            // 2. 按日期+优先级动态锁
            String lockKey = String.format("trans_file_to_marketing_bi_shard_lock:%s:%s", dataDate, priorityStatus);
            String lockValue = UUID.randomUUID().toString();
            try {
                // 获取锁
                redisChgService.lock(lockKey, lockValue);

                TransFileToBiConfigRecordVO configRecord = bFileBiConfigMapper.selectConfigAndTaskForBiShard(dataDate, priorityStatus);
                if (Objects.isNull(configRecord)) {
                    continue;
                }
                nfsFileTOBiRecordMapper.insertSelective(buildNfsRecord(configRecord, dataDate));
                redisChgService.unlock(lockKey, lockValue);

                // ndf文件落库处理
                dealOldBIData(configRecord, dataDate);
                processTransferFile(configRecord, dataDate);
            } catch (Exception e) {
                String errMsg = "分片处理转化文件落库到marketingBI异常: " + e.getMessage();
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.BI_SERVICEERROR.getCode(), errMsg));
            } finally {
                redisChgService.unlock(lockKey, lockValue);
            }
        }
    }

    /**
     * 处理转化文件落库到BI
     */
    private void processTransferFile(TransFileToBiConfigRecordVO configRecordVO, String dateDate) {
        long startTime = System.currentTimeMillis();
        log.warn("apiCode: {} 日期: {} 文件落库BI开始", configRecordVO.getApiCode(), dateDate);
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(
                marketingCommonConfig.getTransFileExtractionBIThread(),
                marketingCommonConfig.getTransFileExtractionBIThread());
        String filePath = configRecordVO.getFilePath().concat(configRecordVO.getFileName());
        File file = new File(filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<String> columns = Arrays.asList(configRecordVO.getDbFields().split(","));
            List<String> batchData = Lists.newArrayList();
            String dataLine;
            reader.readLine(); // 跳过表头
            List<CompletableFuture<Void>> futures = Lists.newArrayList();

            String formattedDate = getFormattedDate(dateDate);

            while ((dataLine = reader.readLine()) != null) {
                modifyThreadPool(threadPool);
                batchData.add(dataLine);
                if (batchData.size() != BATCH_SIZE) {
                    continue;
                }
                ArrayList<String> copyListObj = Lists.newArrayList(batchData);
                futures.add(CompletableFuture.runAsync(() ->
                        writeFileDataToTidb(configRecordVO.getDbName(), columns, copyListObj, formattedDate), threadPool));
                batchData.clear();
            }
            // 处理剩余数据
            if (!batchData.isEmpty()) {
                ArrayList<String> copyListObj = Lists.newArrayList(batchData);
                futures.add(CompletableFuture.runAsync(() ->
                        writeFileDataToTidb(configRecordVO.getDbName(), columns, copyListObj, formattedDate), threadPool));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            log.warn("apiCode: {} 日期: {} 文件落库BI完成,耗时:{}", configRecordVO.getApiCode(), dateDate, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            String errMsg = "nfs转化提取文件读取入库异常path: " + filePath + " Exception: " + e.getMessage();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.BI_SERVICEERROR.getCode(), errMsg));
        } finally {
            threadPoolShutDown(threadPool);
        }
    }

    private static String getFormattedDate(String dateDate) {
        String formattedDate = null;
        try {
            formattedDate = new SimpleDateFormat("yyyy-MM-dd")
                    .format(new SimpleDateFormat("yyyyMMdd").parse(dateDate));
        } catch (ParseException ignore) {
        }
        return formattedDate;
    }

    /**
     * 删除dataDate历史已落BI数据
     *
     * @param configRecord
     */
    private void dealOldBIData(TransFileToBiConfigRecordVO configRecord, String dataDate) {
        NfsFileTOBiRecordExample nfsOtherExample = new NfsFileTOBiRecordExample();
        nfsOtherExample.createCriteria()
                .andApiCodeEqualTo(configRecord.getApiCode())
                .andFileTypeEqualTo(configRecord.getFileType())
                .andExecuteDateEqualTo(dataDate)
                .andTaskIdLessThan(configRecord.getTaskId());
        List<NfsFileTOBiRecord> oldRecords = nfsFileTOBiRecordMapper.selectByExample(nfsOtherExample);
        if (CollectionUtils.isEmpty(oldRecords)) {
            return;
        }
        // 删除旧b_nfsfile_bi_record
        nfsFileTOBiRecordMapper.deleteByExample(nfsOtherExample);
        // 批量删除目标表数据，每批2000条
        int batchSize = 2000;
        String countSql = "select count(*) from " + configRecord.getDbName() + " where data_date = '" + dataDate + "'";
        int totalCount = transferFileExtractToDorisBIMapper.countDataFromMarketingBiTable(countSql);

        if (totalCount > 0) {
            int totalBatches = (totalCount + batchSize - 1) / batchSize;
            for (int i = 0; i < totalBatches; i++) {
                String deleteSql = "delete from " + configRecord.getDbName() +
                        " where data_date = '" + dataDate + "' limit " + batchSize;
                transferFileExtractToDorisBIMapper.deleteDataFromMarketingBiTablebI_(deleteSql);
                log.info("apiCode: {} 日期: {} 批次: {}/{} 删除数据完成",
                        configRecord.getApiCode(), dataDate, (i + 1), totalBatches);
            }
        }
        log.info("apiCode: {} 日期: {} 存在二次提取，已删除旧数据，总记录数: {}", configRecord.getApiCode(), dataDate, totalCount);
    }


    /**
     * 写入文件数据到Tidb
     */
    private void writeFileDataToTidb(String tableName, List<String> columns, List<String> batchData, String formattedDate) {
        StringBuilder insertSql = new StringBuilder("INSERT INTO ")
                .append(tableName).append(" (")
                .append(columns.stream().map(String::trim).collect(Collectors.joining(", ")))
                .append(") VALUES ");

        for (String dataLine : batchData) {
            String[] rawValues = dataLine.split(",", -1);
            insertSql.append("\n(");
            // 处理非日期字段
            for (int i = 0; i < columns.size() - 1; i++) {
                if (i > 0) {
                    insertSql.append(", ");
                }
                String rawValue = (i < rawValues.length) ? rawValues[i].trim() : "";
                if (rawValue.isEmpty()) {
                    insertSql.append("NULL");
                } else {
                    insertSql.append("'").append(rawValue.replace("'", "''")).append("'");
                }
            }
            // 统一添加日期字段
            insertSql.append(", ")
                    .append(formattedDate != null ? "'" + formattedDate + "'" : "NULL")
                    .append("),");
        }
        if (insertSql.charAt(insertSql.length() - 1) == ',') {
            insertSql.setLength(insertSql.length() - 1);
        }
        transferFileExtractToDorisBIMapper.insertDataToMarketingBiTablebI_(insertSql.toString());
    }

    private NfsFileTOBiRecord buildNfsRecord(TransFileToBiConfigRecordVO configTask, String dataDate) {
        NfsFileTOBiRecord record = new NfsFileTOBiRecord();
        record.setApiCode(configTask.getApiCode());
        record.setFileType(configTask.getFileType());
        record.setFilePath(configTask.getFilePath());
        record.setFileName(configTask.getFileName());
        record.setTaskId(configTask.getTaskId());
        record.setExecuteDate(dataDate);
        return record;
    }

    /**
     * 修改线程池配置
     */
    private void modifyThreadPool(ThreadPoolExecutor pool) {
        Integer threadNum = marketingCommonConfig.getTransFileExtractionBIThread();
        pool.setCorePoolSize(threadNum);
        pool.setMaximumPoolSize(threadNum);
    }


    /**
     * 关闭线程池
     */
    private void threadPoolShutDown(ThreadPoolExecutor executor) {
        log.warn("shutdownThreadPool开始");
        executor.shutdown();
        try {
            while (!executor.awaitTermination(60L, TimeUnit.SECONDS)) {
                log.info("{},线程池关闭", TITLE);
            }
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            log.error("{},日志保存线程池结束异常！", TITLE, ex);
            Thread.currentThread().interrupt();
        }
        log.warn("shutdownThreadPool结束");
    }
}

