package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.NfsFileTOBiRecord;
import com.br.marketing.entity.NfsFileTOBiRecordExample;
import com.br.marketing.mapper.BFileBiConfigMapper;
import com.br.marketing.mapper.NfsFileTOBiRecordMapper;
import com.br.marketing.mapper.TransferFileExtractToDorisBIMapper;
import com.br.marketing.service.TransFileToMarketingBiShardService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.TransFileToBiConfigRecordVO;
import com.google.common.collect.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author xiong.luo
 * @description: 内部服务器的转化文件落库到marketingBI(分片)
 * @date 2025/06/30
 */
@Slf4j
@Service
public class TransFileToMarketingBiShardServiceImpl implements TransFileToMarketingBiShardService {

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

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public void process(String jobParameter, List<Integer> shardingItems) {
        log.warn("当前作业服务器的分片序列号为:{}", shardingItems);
        // 1. 识别优先级分片组
        Integer priorityStatus = marketingCommonConfig.getTransFilePriorityList().containsAll(shardingItems) ? 1 : 0;
        JSONObject param = JSON.parseObject(jobParameter);
        LocalDate startDate = Optional.ofNullable(param.getString("startDate"))
                .map(dateStr -> LocalDate.parse(dateStr, FORMATTER))
                .orElseGet(LocalDate::now);
        LocalDate endDate = Optional.ofNullable(param.getString("endDate"))
                .map(dateStr -> LocalDate.parse(dateStr, FORMATTER))
                .orElseGet(LocalDate::now);
        List<String> dates = Stream.iterate(startDate, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(startDate, endDate) + 1L)
                .map(date -> date.format(FORMATTER))
                .collect(Collectors.toList());
        for (String dataDate : dates) {
            Boolean interrupt = marketingCommonConfig.getFileToMarketingBiInterrupt();
            if(Boolean.TRUE.equals(interrupt)) {
                log.warn("分片处理转化文件落库到marketingBI任务中断，当前执行日期:{}", dataDate);
                break;
            }

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
        TpDynamicExecutor threadPool = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.FILE_TO_MARKETING_BI.getName(), 5, 10);
        String filePath = configRecordVO.getFilePath().concat(configRecordVO.getFileName());
        File file = new File(filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            Map<String, String> colFieldMap = JSON.parseObject(configRecordVO.getDbColFieldsMap(),
                    new TypeReference<Map<String, String>>() {});
            List<String> batchData = Lists.newArrayList();
            String dataLine;

            String headerLine = reader.readLine();
            List<String> fileHeaders = Lists.newArrayList(headerLine.split(","))
                    .stream().map(String::trim).map(x -> x.replace("\uFEFF", "")).collect(Collectors.toList());
            Map<String, Integer> indexFieldMap = colFieldMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getValue, entry -> fileHeaders.indexOf(entry.getKey())));

            List<CompletableFuture<Void>> futures = Lists.newArrayList();

            String formattedDate = getFormattedDate(dateDate);

            while ((dataLine = reader.readLine()) != null) {
                Boolean interrupt = marketingCommonConfig.getFileToMarketingBiInterrupt();
                if(Boolean.TRUE.equals(interrupt)) {
                    log.warn("任务中断触发，停止文件读取");
                    break;
                }
                batchData.add(dataLine);
                if (batchData.size() != marketingCommonConfig.getFileToMarketingBiBatchSize()) {
                    continue;
                }
                ArrayList<String> copyListObj = Lists.newArrayList(batchData);
                futures.add(CompletableFuture.runAsync(() ->
                        writeFileDataToTidb(configRecordVO.getDbName(), indexFieldMap, copyListObj, formattedDate), threadPool));
                batchData.clear();
            }
            // 处理剩余数据
            if (!batchData.isEmpty()) {
                ArrayList<String> copyListObj = Lists.newArrayList(batchData);
                futures.add(CompletableFuture.runAsync(() ->
                        writeFileDataToTidb(configRecordVO.getDbName(), indexFieldMap, copyListObj, formattedDate), threadPool));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            log.warn("apiCode: {} 日期: {} 文件落库BI完成,耗时:{}", configRecordVO.getApiCode(), dateDate, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            String errMsg = "nfs转化提取文件读取入库异常path: " + filePath + " Exception: " + e.getMessage();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.BI_SERVICEERROR.getCode(), errMsg));
        } finally {
            threadPool.shutdownAndAwaitTermination();
        }
    }

    private static String getFormattedDate(String dateDate) {
        String formattedDate = null;
        try {
            formattedDate = new SimpleDateFormat("yyyy-MM-dd")
                    .format(new SimpleDateFormat("yyyyMMdd").parse(dateDate));
        } catch (ParseException ignore) {
            log.warn("日期转换异常: {}", dateDate);
        }
        return formattedDate;
    }

    /**
     * 删除dataDate历史已落BI数据
     *
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
        int totalCount = transferFileExtractToDorisBIMapper.countDataFromMarketingBiTablebI_(countSql);

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
    private void writeFileDataToTidb(String tableName, Map<String, Integer> colFieldMap,
                                     List<String> batchData, String formattedDate){
        StringJoiner valuesJoiner = new StringJoiner(", \n", "", "");
        String colNames = colFieldMap.keySet().stream()
                .map(String::trim)
                .collect(Collectors.joining(", "));

        for (String dataLine : batchData) {
            String[] rawValues = dataLine.split(",", -1);
            StringJoiner rowJoiner = new StringJoiner(", ", "(", ")");

            colFieldMap.forEach((colName, index) -> {
                String rawValue = resolveRawValue(rawValues, index, colName, formattedDate);
                rowJoiner.add(formatSqlValue(rawValue));
            });
            valuesJoiner.add(rowJoiner.toString());
        }

        String insertSql = "INSERT INTO " + tableName + " (" + colNames + ") VALUES \n" + valuesJoiner;
        transferFileExtractToDorisBIMapper.insertDataToMarketingBiTablebI_(insertSql);
    }

    private String resolveRawValue(String[] rawValues, int index, String colName, String formattedDate) {
        if (index >= 0 && index < rawValues.length) {
            return rawValues[index].trim();
        } else if ("data_date".equals(colName)) {
            return formattedDate;
        }
        return "";
    }

    private String formatSqlValue(String rawValue) {
        if (StringUtils.isEmpty(rawValue)) {
            return "NULL";
        }
        return "'" + rawValue.replace("'", "''") + "'";
    }

    private NfsFileTOBiRecord buildNfsRecord(TransFileToBiConfigRecordVO configTask, String dataDate) {
        NfsFileTOBiRecord nfsFileTOBiRecord = new NfsFileTOBiRecord();
        nfsFileTOBiRecord.setApiCode(configTask.getApiCode());
        nfsFileTOBiRecord.setFileType(configTask.getFileType());
        nfsFileTOBiRecord.setFilePath(configTask.getFilePath());
        nfsFileTOBiRecord.setFileName(configTask.getFileName());
        nfsFileTOBiRecord.setTaskId(configTask.getTaskId());
        nfsFileTOBiRecord.setExecuteDate(dataDate);
        nfsFileTOBiRecord.setBusType(configTask.getBusType());
        return nfsFileTOBiRecord;
    }
}

