package com.br.marketing.service.Impl.xc;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.StraHisFileExample;
import com.br.marketing.entity.XieChengRuleScoreRecord;
import com.br.marketing.entity.XieChengRuleScoreRecordExample;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.mapper.XieChengRuleScoreRecordMapper;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description XieChengRuleScoreToDbServiceImpl
 * @Author hong.chen
 * @CreateTime 2024/04/22
 */
@Service
@Slf4j
public class XieChengRuleScoreToDbServiceImpl implements XieChengRuleScoreToDbService {
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    StraHisFileMapper straHisFileMapper;
    @Resource
    XieChengRuleScoreRecordMapper scoreRecordMapper;

    @Resource
    XieChengRuleScoreRecordMapper ruleScoreRecordMapper;

    @Autowired
    SyncConfigService syncConfigService;
    @Resource
    private AlarmApiClient alarmClient;

    @Value("${datasource.database.marketingDoris.replicationAllocation}")
    String replicationAllocation;

    private static final int BATCH_SIZE = 50;

    @Override
    public void process() {
        marketingCommonConfig.getXieChengCollidingDataProcessApiCodes().forEach(apiCode -> {
            LocalDate createTimeStartLocalDate = LocalDate.now().minusDays(marketingCommonConfig.getXieChengRuleScoreToDbLastDays());
            Date createTimeStartDate = Date.from(createTimeStartLocalDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

            StraHisFileExample straHisFileExample = new StraHisFileExample();
            straHisFileExample.createCriteria().andApiCodeEqualTo(apiCode).andStatusEqualTo(2).andCreateTimeGreaterThanOrEqualTo(createTimeStartDate).andTypeEqualTo(2);
            List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
            straHisFiles.forEach(straHisFile -> {
                XieChengRuleScoreRecordExample scoreRecordExample = new XieChengRuleScoreRecordExample();
                scoreRecordExample.createCriteria().andIsDeleteEqualTo(0).andBatchNumberEqualTo(straHisFile.getBatchNumber());
                List<XieChengRuleScoreRecord> xieChengRuleScoreRecords = scoreRecordMapper.selectByExample(scoreRecordExample);

                if (CollectionUtils.isEmpty(xieChengRuleScoreRecords)) {
                    XieChengRuleScoreRecord scoreRecord = new XieChengRuleScoreRecord();
                    scoreRecord.setApiCode(apiCode);
                    scoreRecord.setRecordStatus(1);
                    scoreRecord.setBatchNumber(straHisFile.getBatchNumber());
                    scoreRecord.setFileName(straHisFile.getFileName());
                    scoreRecord.setCreateTime(new Date());
                    scoreRecord.setUpdateTime(new Date());

                    scoreRecordMapper.insertSelective(scoreRecord);

                    createTableAndInsert(straHisFile);

                    String tableName = "b_xiecheng_colliding_" + straHisFile.getBatchNumber();
                    Long actualNumber = scoreRecordMapper.getXieChengScoreTidbTableCount(tableName);
                    XieChengRuleScoreRecord updateRecord = new XieChengRuleScoreRecord();
                    updateRecord.setId(scoreRecord.getId());
                    updateRecord.setRecordStatus(2);
                    updateRecord.setActualNumber(actualNumber.intValue());
                    scoreRecordMapper.updateByPrimaryKey(updateRecord);
                }
            });
        });
    }

    private void createTableAndInsert(StraHisFile straHisFile) {
        for (String fileName : straHisFile.getFileName().split(",")) {
            File file = new File(straHisFile.getFilePath(), fileName);
            if (!file.exists()) {
                // todo 是否重新配置告警码
                alarmClient.sendAlarm("File not found: " + file.getAbsolutePath(), "携程跑分数据同步作业", AlarmSendCodeEnum.EXCEPTION_USUAL_NOTICE.getCode());
                return;
            }

            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(50, 50);
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                // Read the first header to get table headers
                String header = reader.readLine();
                if (header == null) {
                    alarmClient.sendAlarm("File is empty: " + file.getAbsolutePath(), "携程跑分数据同步作业",
                            AlarmSendCodeEnum.EXCEPTION_USUAL_NOTICE.getCode());
                    return;
                }

                List<String> columns = Arrays.asList(header.split(",", -1));
                if (!columns.contains("cell")) {
                    alarmClient.sendAlarm("文件表头缺少cell字段: " + file.getAbsolutePath(), "携程跑分数据同步作业",
                            AlarmSendCodeEnum.EXCEPTION_USUAL_NOTICE.getCode());
                    return;
                }

                boolean anyMatchBlank = columns.stream().anyMatch(StringUtils::isBlank);
                if (anyMatchBlank) {
                    alarmClient.sendAlarm("文件表头缺失字段: " + file.getAbsolutePath(), "携程跑分数据同步作业",
                            AlarmSendCodeEnum.EXCEPTION_USUAL_NOTICE.getCode());
                    return;
                }
                List<String> firstLine = Arrays.asList(reader.readLine().split(",", -1));

                // todo 校验第一行数据
                String tableName = "b_xiecheng_colliding_" + straHisFile.getBatchNumber();
                createTidbAndDorisTable(columns, straHisFile, firstLine, tableName);

                List<String> batchData = new ArrayList<>();
                String dataLine;
                while ((dataLine = reader.readLine()) != null) {
                    batchData.add(dataLine);
                    if (batchData.size() == BATCH_SIZE) {
                        threadPool.submit(() -> writeFileDataToTidb(tableName, columns, new ArrayList<>(batchData)));
                        batchData.clear();
                    }
                }

                if (!batchData.isEmpty()) {
                    writeFileDataToTidb(tableName, columns, new ArrayList<>(batchData));
                }

            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }

            threadPool.shutdown();
            try {
                while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                    log.info("携程跑分数据同步作业线程池关闭");
                }
            } catch (InterruptedException ex) {
                threadPool.shutdownNow();
                log.error("携程跑分数据同步作业，日志保存线程池结束异常！", ex);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void createTidbAndDorisTable(List<String> columns, StraHisFile straHisFile, List<String> firstLine, String tableName) {
        StringBuilder createTidbDDL = new StringBuilder();
        createTidbDDL.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        createTidbDDL.append(" id bigint auto_increment primary key, ");

        StringBuilder createDorisDDL = new StringBuilder();
        createDorisDDL.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        createDorisDDL.append(" id bigint auto_increment primary key, ");

        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            String value = firstLine.get(i);
            createTidbDDL.append(column.trim());
            createDorisDDL.append(column.trim());
            if (column.startsWith("score") && canConvertToBigdecimal(value)) {
                createTidbDDL.append(" decimal(12,6), ");
                createDorisDDL.append(" decimal(12,6), ");
            } else if (column.endsWith("age") && canConvertToInt(value)) {
                createTidbDDL.append(" int(2), ");
                createDorisDDL.append(" int(2), ");
            } else {
                createTidbDDL.append(" varchar(255), ");
                createDorisDDL.append(" varchar(765), ");
            }
        }

        createTidbDDL.setLength(createTidbDDL.length() - 2); // Remove the last comma
        createDorisDDL.append(" extend longtext,");
        createDorisDDL.append(" create_time datetime,");
        createDorisDDL.append(" update_time timestamp null on update CURRENT_TIMESTAMP,");
        createDorisDDL.append(" is_delete int default 0");

        createTidbDDL.append("); ");
        createTidbDDL.append("ALTER TABLE ").append(tableName).append(" ADD UNIQUE INDEX idx_cell (cell);");

        ruleScoreRecordMapper.createXieChengScoreTidbTableByBatchNum(createTidbDDL.toString());

        createDorisDDL.setLength(createTidbDDL.length() - 2); // Remove the last comma
        createDorisDDL.append(" extend string,");
        createDorisDDL.append(" create_time datetime,");
        createDorisDDL.append(" update_time timestamp null on update CURRENT_TIMESTAMP,");
        createDorisDDL.append(" is_delete int default 0");

        createDorisDDL.append(") ENGINE=OLAP\n" +
                "Unique KEY(id)\n" +
                "DISTRIBUTED BY HASH(id) BUCKETS 16\n" +
                "PROPERTIES (\n" +
                "'replication_allocation' = ");
        createDorisDDL.append(replicationAllocation);
        createDorisDDL.append(",\n" +
                "'in_memory' = 'false',\n" +
                "'storage_format' = 'V2',\n" +
                "'disable_auto_compaction' = 'false'\n" +
                ");");
        ruleScoreRecordMapper.createXieChengScoreDorisTableByBatchNumdoris_(createDorisDDL.toString());
    }

    private boolean canConvertToBigdecimal(String value) {
        try {
            new BigDecimal(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean canConvertToInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void writeFileDataToTidb(String tableName, List<String> columns, List<String> batchData) {
        try {
            StringBuilder insertSql = new StringBuilder("INSERT INTO ");
            insertSql.append(tableName);
            for (String header : columns) {
                insertSql.append(header.trim()).append(", ");
            }
            insertSql.append("extend,");
            insertSql.append("create_time,");
            insertSql.append("update_time,");
            insertSql.append("is_delete");

            insertSql.append(") VALUES ");

            List<String> dataList;
            for (String dataLine : batchData) {
                // clear
                dataList = Arrays.asList(dataLine.split(",", -1));
                insertSql.append("(");
                for (String value : dataList) {
                    insertSql.append("'").append(value.trim()).append("', ");
                }

                insertSql.append("null, now(), now(), 0");
                insertSql.append(")");
                dataList.clear();
            }

            insertXieChengScoreTidbTable(insertSql.toString(), batchData);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @RetryMethod(retryNowNum = 2)
    public Result insertXieChengScoreTidbTable(String insertSql, List<String> batchData) {
        try {
            ruleScoreRecordMapper.insertXieChengScoreTidbTable(insertSql);
        } catch (Exception e) {
            alarmClient.sendAlarm("写入数据库异常: " + String.join(";", batchData), "携程跑分数据同步作业",
                    AlarmSendCodeEnum.EXCEPTION_USUAL_NOTICE.getCode());
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }

        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }
}
