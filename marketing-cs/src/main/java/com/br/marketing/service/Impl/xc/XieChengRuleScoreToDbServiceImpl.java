package com.br.marketing.service.Impl.xc;

import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.StraHisFileExample;
import com.br.marketing.entity.XieChengRuleScoreRecord;
import com.br.marketing.entity.XieChengRuleScoreRecordExample;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.mapper.XieChengRuleScoreRecordMapper;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description XieChengRuleScoreToDbServiceImpl
 * @Author hong.chen
 * @CreateTime 2024/04/22
 */
@Service
@Slf4j
public class XieChengRuleScoreToDbServiceImpl implements XieChengRuleScoreToDbService {
    @Resource
    private XieChengRuleScoreToDbServiceImpl ruleScoreToDbService;
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

    @Value("${datasource.database.marketingDoris.replicationAllocation:1}")
    String replicationAllocation;

    private static final int BATCH_SIZE = 50;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        // 分片项目
        List<Integer> shardingItems = context.getShardingItems();
        // 总分片数
        int shardingTotalCount = context.getShardingTotalCount();

        marketingCommonConfig.getXieChengCollidingDataProcessApiCodes().forEach((String apiCode) -> {
            LocalDate createTimeStartLocalDate = LocalDate.now().minusDays(marketingCommonConfig.getXieChengRuleScoreToDbLastDays());
            Date createTimeStartDate = Date.from(createTimeStartLocalDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

            StraHisFileExample straHisFileExample = new StraHisFileExample();
            straHisFileExample.createCriteria()
                    .andApiCodeEqualTo(apiCode).andStatusEqualTo(2)
                    .andCreateTimeGreaterThanOrEqualTo(createTimeStartDate)
                    .andTypeEqualTo(2);
            List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);

            List<Long> Longitems = shardingItems.stream().map(Integer::longValue).collect(Collectors.toList());
            List<StraHisFile> shardStraHisFiles = straHisFiles.stream().filter((StraHisFile t) -> Longitems.contains(Math.floorMod(t.getId(),
                    shardingTotalCount))).collect(Collectors.toList());

            shardStraHisFiles.forEach((StraHisFile straHisFile) -> {
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

                    Result<Integer> result = createTableAndInsert(straHisFile);

                    if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                        String tableName = "b_xiecheng_colliding_" + straHisFile.getBatchNumber();
                        Long actualNumber = scoreRecordMapper.getXieChengScoreTidbTableCount(tableName);
                        XieChengRuleScoreRecord updateRecord = new XieChengRuleScoreRecord();
                        updateRecord.setId(scoreRecord.getId());
                        updateRecord.setRecordStatus(2);
                        updateRecord.setUpdateTime(new Date());
                        updateRecord.setActualNumber(actualNumber.intValue());
                        scoreRecordMapper.updateByPrimaryKeySelective(updateRecord);
                    } else if (ResultCode.FAIL.getValue().equals(result.getCode())) {
                        XieChengRuleScoreRecord updateRecord = new XieChengRuleScoreRecord();
                        updateRecord.setId(scoreRecord.getId());
                        updateRecord.setRecordStatus(3);
                        updateRecord.setErrorMessage(result.getMessage());
                        updateRecord.setUpdateTime(new Date());
                        scoreRecordMapper.updateByPrimaryKeySelective(updateRecord);
                    }
                }
            });
        });
    }

    private Result<Integer> createTableAndInsert(StraHisFile straHisFile) {
        Result result = new Result();
        for (String fileName : straHisFile.getFileName().split(",")) {
            File file = new File(straHisFile.getFilePath(), fileName);
//            String path =
//                    "D:\\opt\\data1\\inloan\\download\\marketing\\once\\7410950\\7410950_20240425000000_8332\\2024-04-25" + File.separator +
//                    fileName;
//            File file = new File(path);
            if (!file.exists()) {
                String errMsg = "跑分文件不存在，path：" + file.getAbsolutePath();
                log.error(errMsg);
                return result.setCode(ResultCode.FAIL.getValue()).setMessage(errMsg);
            }

            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(marketingCommonConfig.getXieChengCollidingRuleScoreToDBThread(),
                    marketingCommonConfig.getXieChengCollidingRuleScoreToDBThread());

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String header = reader.readLine();
                if (header == null) {
                    String errMsg = "文件内容为空，path：" + file.getAbsolutePath();
                    log.error(errMsg);
                    return result.setCode(ResultCode.FAIL.getValue()).setMessage(errMsg);
                }

                String headerColumn = header.replace(",id,", ",t_id,");
                List<String> columns = Arrays.asList(headerColumn.split(",", -1));
                if (!columns.contains("cell")) {
                    String errMsg = "文件表头缺少cell字段，path：" + file.getAbsolutePath();
                    log.error(errMsg);
                    return result.setCode(ResultCode.FAIL.getValue()).setMessage(errMsg);
                }

                boolean anyMatchBlank = columns.stream().anyMatch(StringUtils::isBlank);
                if (anyMatchBlank) {
                    String errMsg = "文件表头缺失字段，path：" + file.getAbsolutePath();
                    log.error(errMsg);
                    return result.setCode(ResultCode.FAIL.getValue()).setMessage(errMsg);
                }

                String firstLine = reader.readLine();
                List<String> firstLineList = Arrays.asList(firstLine.split(",", -1));

                String batchNumber = straHisFile.getBatchNumber();
                String tableName = "b_xiecheng_colliding_" + batchNumber;

                Map<String, String> fieldMap = marketingCommonConfig.getXieChengCollidingRuleScoreFieldMap();
                // 创建tidb和doris表结构
                createTidbAndDorisTable(columns, firstLineList, tableName, fieldMap);

                List<String> batchData = new ArrayList<>();
                batchData.add(firstLine);
                String dataLine;

                while ((dataLine = reader.readLine()) != null) {
                    if (marketingCommonConfig.getXieChengCollidingRuleScoreStopBatchNums().contains(batchNumber)) {
                        return result.setCode(ResultCode.FAIL.getValue()).setMessage("手动停止该同步任务");
                    }

                    batchData.add(dataLine);
                    if (batchData.size() == BATCH_SIZE) {
                        ArrayList<String> subList = new ArrayList<>(batchData);
                        threadPool.submit(() -> writeFileDataToTidb(tableName, columns, subList, fieldMap));
                        batchData.clear();
                    }
                }

                if (!batchData.isEmpty()) {
                    writeFileDataToTidb(tableName, columns, new ArrayList<>(batchData), fieldMap);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return result.setCode(ResultCode.FAIL.getValue()).setMessage("未知异常");
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

        return result.setCode(ResultCode.SUCCESS.getValue());
    }

    private void createTidbAndDorisTable(List<String> columns, List<String> firstLine, String tableName, Map<String, String> fieldMap) {
        StringBuilder createTidbDDL = new StringBuilder();
        createTidbDDL.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        createTidbDDL.append(" id bigint auto_increment primary key, ");

        StringBuilder createDorisDDL = new StringBuilder();
        createDorisDDL.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        createDorisDDL.append(" id bigint, ");

        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);
            String value = firstLine.get(i);

            if (fieldMap.containsKey(column)) {
                createTidbDDL.append(fieldMap.get(column));
                createDorisDDL.append(fieldMap.get(column));
            } else {
                createTidbDDL.append(column.trim());
                createDorisDDL.append(column.trim());
            }

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

        createTidbDDL.append(" extend longtext,")
                .append(" create_time datetime,")
                .append(" update_time timestamp null on update CURRENT_TIMESTAMP,")
                .append(" is_delete int default 0,")
                .append(" index idx_cell (cell) ")
                .append("); ");

        ruleScoreRecordMapper.createXieChengScoreTidbTableByBatchNum(createTidbDDL.toString());

        createDorisDDL.append(" extend string,")
                .append(" create_time datetime,")
                .append(" update_time datetime,")
                .append(" is_delete int default '0'");

        createDorisDDL.append(") ENGINE=OLAP\n" +
                "Unique KEY(id)\n" +
                "DISTRIBUTED BY HASH(id) BUCKETS 16\n" +
                "PROPERTIES (\n" +
                "'replication_allocation' = '");
        createDorisDDL.append(replicationAllocation);
        createDorisDDL.append("',\n" +
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

    private void writeFileDataToTidb(String tableName, List<String> columns, List<String> batchData, Map<String, String> fieldMap) {
        try {
            StringBuilder insertSql = new StringBuilder("INSERT INTO ");
            insertSql.append(tableName).append(" (");
            List<Integer> numColumns = new ArrayList<>(columns.size());
            for (int i = 0; i < columns.size(); i++) {
                if (fieldMap.containsKey(columns.get(i))) {
                    insertSql.append(fieldMap.get(columns.get(i))).append(", ");
                } else {
                    insertSql.append(columns.get(i).trim()).append(", ");
                }

                if (columns.get(i).startsWith("score") || columns.get(i).endsWith("age")) {
                    numColumns.add(i);
                }
            }

            insertSql.append("extend,");
            insertSql.append("create_time,");
            insertSql.append("update_time,");
            insertSql.append("is_delete");

            insertSql.append(") VALUES ");

            List<String> dataList;
            for (String dataLine : batchData) {
                dataList = Arrays.asList(dataLine.split(",", -1));
                insertSql.append("(");
                for (int i = 0; i < dataList.size(); i++) {
                    String value = dataList.get(i).trim();
                    if (numColumns.contains(i) && StringUtils.isEmpty(value)) {
                        insertSql.append("null, ");
                    } else {
                        insertSql.append("'").append(value).append("', ");
                    }
                }

                insertSql.append("null, now(), now(), 0");
                insertSql.append("),");
            }

            insertSql.setLength(insertSql.length() - 1);
            ruleScoreToDbService.insertXieChengScoreTidbTable(insertSql.toString(), batchData);

        } catch (Exception e) {
            log.error("携程跑分数据同步作业，子线程异常", e.getMessage(), e);
        }
    }

    @RetryMethod(retryNowNum = 2)
    public Result insertXieChengScoreTidbTable(String insertSql, List<String> batchData) {
        try {
            ruleScoreRecordMapper.insertXieChengScoreTidbTable(insertSql);
        } catch (Exception e) {
            log.error("携程跑分数据同步作业,写入数据库异常:" + String.join(";", batchData), e.getMessage(), e);
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }

        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }
}
