package com.br.marketing.service.Impl.xc;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.MarketingTaskExample;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.StraHisFileExample;
import com.br.marketing.entity.XieChengRuleScoreRecord;
import com.br.marketing.entity.XieChengRuleScoreRecordExample;
import com.br.marketing.enums.DingDingAlarmFunctionEnum;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.mapper.XieChengRuleScoreRecordMapper;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description 携程跑分数据同步作业实现类
 * @Author hong.chen
 * @CreateTime 2024/04/22
 */
@Service
@Slf4j
public class XieChengRuleScoreToDbServiceImpl implements XieChengRuleScoreToDbService {
    public static String REGEX = "^[a-z]+[A-Z][a-zA-Z0-9]*$";
    @Resource
    private XieChengRuleScoreToDbServiceImpl ruleScoreToDbService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    StraHisFileMapper straHisFileMapper;
    @Resource
    XieChengRuleScoreRecordMapper scoreRecordMapper;
    @Resource
    MarketingTaskMapper marketingTaskMapper;

    @Autowired
    SyncConfigService syncConfigService;

    @Value("${datasource.database.marketing-doris.replicationAllocation:1}")
    String replicationAllocation;

    @Autowired
    private DingDingRobotHookService dingDingRobotHookService;

    private static final int BATCH_SIZE = 50;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        // 获取分片信息
        List<Integer> shardingItems = context.getShardingItems();
        int shardingTotalCount = context.getShardingTotalCount();
        List<Long> Longitems = shardingItems.stream().map(Integer::longValue).collect(Collectors.toList());

        marketingCommonConfig.getXieChengCollidingDataProcessApiCodes().forEach((String apiCode) -> {
            // 获取T-n~T日跑分记录表
            List<StraHisFile> straHisFiles = getStraHisFiles(apiCode);

            // 获取分片后的跑分记录
            List<StraHisFile> shardStraHisFiles = straHisFiles.stream()
                    .filter((StraHisFile t) -> Longitems.contains((long)Math.floorMod(t.getId(), shardingTotalCount)))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(shardStraHisFiles)) {
                return;
            }

            // 过滤出类型为一次性查询的记录
            List<StraHisFile> straHisFileList = getStraHisFiles(apiCode, shardStraHisFiles);

            // 创建线程池
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(marketingCommonConfig.getXieChengCollidingRuleScoreToDBThread(),
                    marketingCommonConfig.getXieChengCollidingRuleScoreToDBThread());

            straHisFileList.forEach((StraHisFile straHisFile) -> {
                // 根据跑分编号获取携程数据同步记录表
                List<XieChengRuleScoreRecord> xieChengRuleScoreRecords = getXieChengRuleScoreRecords(straHisFile);

                if (CollectionUtils.isEmpty(xieChengRuleScoreRecords)) {
                    // 新增数据同步记录
                    XieChengRuleScoreRecord scoreRecord = createRuleScoreRecord(apiCode, straHisFile);

                    // 执行数据同步
                    Result<Integer> result = doProcess(straHisFile, threadPool);

                    // 更新数据同步记录
                    updateRuleScoreRecord(straHisFile, result, scoreRecord);
                }
            });

            // 关闭线程池
            threadPoolShutDown(threadPool);
        });
    }

    /**
     * 过滤出类型为一次性查询的记录
     * @param apiCode
     * @param shardStraHisFiles
     * @return
     */
    private List<StraHisFile> getStraHisFiles(String apiCode, List<StraHisFile> shardStraHisFiles) {
        List<StraHisFile> straHisFileList = shardStraHisFiles.stream().filter((StraHisFile t) -> {
            MarketingTaskExample taskExample = new MarketingTaskExample();
            taskExample.createCriteria()
                    .andApiCodeEqualTo(apiCode)
                    .andBatchNumberEqualTo(t.getBatchNumber())
                    .andStatusEqualTo(1)
                    .andMonitorTypeEqualTo(1);
            List<MarketingTask> marketingTasks = marketingTaskMapper.selectByExample(taskExample);

            return !CollectionUtils.isEmpty(marketingTasks);
        }).collect(Collectors.toList());

        return straHisFileList;
    }

    private void threadPoolShutDown(ThreadPoolExecutor threadPool) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程跑分数据同步作业线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程跑分数据同步作业，日志保存线程池结束异常！errorMessage=" + ex.getMessage()), ex);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 更新数据同步记录
     * @param straHisFile
     * @param result
     * @param scoreRecord
     */
    private void updateRuleScoreRecord(StraHisFile straHisFile, Result<Integer> result, XieChengRuleScoreRecord scoreRecord) {
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

    /**
     * 新增数据同步记录
     * @param apiCode
     * @param straHisFile
     * @return
     */
    private XieChengRuleScoreRecord createRuleScoreRecord(String apiCode, StraHisFile straHisFile) {
        XieChengRuleScoreRecord scoreRecord = new XieChengRuleScoreRecord();
        scoreRecord.setApiCode(apiCode);
        scoreRecord.setRecordStatus(1);
        scoreRecord.setBatchNumber(straHisFile.getBatchNumber());
        scoreRecord.setFileName(straHisFile.getFileName());
        scoreRecord.setCreateTime(new Date());
        scoreRecord.setUpdateTime(new Date());

        scoreRecordMapper.insertSelective(scoreRecord);
        return scoreRecord;
    }

    /**
     * 获取T-n~T日跑分记录表
     * stra_his_file.status=2 && stra_his_file.create_time>=T-n && type = 2(一次性跑分)
     * @param apiCode
     * @return
     */
    private List<StraHisFile> getStraHisFiles(String apiCode) {
        LocalDate createTimeStartLocalDate = LocalDate.now().minusDays(marketingCommonConfig.getXieChengRuleScoreToDbLastDays());
        Date createTimeStartDate = Date.from(createTimeStartLocalDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria()
                .andApiCodeEqualTo(apiCode).andStatusEqualTo(2)
                .andCreateTimeGreaterThanOrEqualTo(createTimeStartDate)
                .andTypeEqualTo(2);
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
        return straHisFiles;
    }

    /**
     * 根据跑分编号获取携程数据同步记录表
     * b_xiecheng_colliding_rule_score_record.is_delete=0 && batch_number='跑分任务编号'
     * @param straHisFile
     * @return
     */
    private List<XieChengRuleScoreRecord> getXieChengRuleScoreRecords(StraHisFile straHisFile) {
        XieChengRuleScoreRecordExample scoreRecordExample = new XieChengRuleScoreRecordExample();
        scoreRecordExample.createCriteria().andIsDeleteEqualTo(0).andBatchNumberEqualTo(straHisFile.getBatchNumber());
        List<XieChengRuleScoreRecord> xieChengRuleScoreRecords = scoreRecordMapper.selectByExample(scoreRecordExample);
        return xieChengRuleScoreRecords;
    }

    private Result doProcess(StraHisFile straHisFile, ThreadPoolExecutor threadPool) {
        Result result = new Result();
        int deleteCount = 0;

        String batchNumber = straHisFile.getBatchNumber();
        if (StringUtils.isEmpty(batchNumber)) {
            String errMsg = "跑分编号为空，跑分id：" + straHisFile.getId();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), errMsg));
            return result.setCode(ResultCode.FAIL.getValue()).setMessage(errMsg);
        }
        String tableName = "b_xiecheng_colliding_" + batchNumber;
        Map<String, String> fieldMap = marketingCommonConfig.getXieChengCollidingRuleScoreFieldMap();

        for (String fileName : straHisFile.getFileName().split(",")) {
            File file = new File(straHisFile.getFilePath(), fileName);

            if (!file.exists()) {
                String errMsg = "跑分文件不存在，path：" + file.getAbsolutePath();
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), errMsg));
                return result.setCode(ResultCode.FAIL.getValue()).setMessage(errMsg);
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                // 获取文件表头
                String header = reader.readLine();
                if (header == null) {
                    String errMsg = "文件内容为空，path：" + file.getAbsolutePath();
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), errMsg));
                    return result.setCode(ResultCode.FAIL.getValue()).setMessage(errMsg);
                }

                // 将文件字段id替换为t_id
                String headerColumn = header.replace(",id,", ",t_id,");

                List<String> columns = Arrays.asList(headerColumn.split(",", -1));
                if (!columns.contains("cell")) {
                    String errMsg = "文件表头缺少cell字段，path：" + file.getAbsolutePath();
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), errMsg));
                    return result.setCode(ResultCode.FAIL.getValue()).setMessage(errMsg);
                }

                boolean anyMatchBlank = columns.stream().anyMatch(StringUtils::isBlank);
                if (anyMatchBlank) {
                    String errMsg = "文件表头缺失字段，path：" + file.getAbsolutePath();
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), errMsg));
                    return result.setCode(ResultCode.FAIL.getValue()).setMessage(errMsg);
                }

                // 根据配置重新映射表头
                List<String> transferColumn =
                        columns.stream().map((String column) -> fieldMap.getOrDefault(column, column)).collect(Collectors.toList());

                // 校验表头是否有驼峰字段
                checkIsCamelCase(transferColumn, file);

                // 获取表头下第一行数据
                String firstLine = reader.readLine();
                List<String> firstLineList = Arrays.asList(firstLine.split(",", -1));

                // 创建tidb和doris表结构
                createTidbAndDorisTable(transferColumn, firstLineList, tableName);

                // 每50行数据1个线程写入tidb
                List<String> batchData = new ArrayList<>();
                batchData.add(firstLine);
                String dataLine;
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                while ((dataLine = reader.readLine()) != null) {
                    if (marketingCommonConfig.getXieChengCollidingRuleScoreStopBatchNums().contains(batchNumber)) {
                        return result.setCode(ResultCode.FAIL.getValue()).setMessage("手动停止该同步任务");
                    }

                    modifyThreadPool(threadPool);

                    batchData.add(dataLine);
                    if (batchData.size() == BATCH_SIZE) {
                        ArrayList<String> subList = new ArrayList<>(batchData);
                        futures.add(CompletableFuture.runAsync(() -> writeFileDataToTidb(tableName, transferColumn, subList)
                                , threadPool));
                        batchData.clear();
                    }
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                // 主线程处理尾量数据
                if (!batchData.isEmpty()) {
                    writeFileDataToTidb(tableName, transferColumn, new ArrayList<>(batchData));
                }
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                        "携程跑分文件同步DB异常, " + e.getMessage()), e);
                return result.setCode(ResultCode.FAIL.getValue()).setMessage("未知异常");
            }
        }

        return result.setCode(ResultCode.SUCCESS.getValue());
    }

    private void checkIsCamelCase(List<String> transferColumn, File file) {
        List<String> xieChengScoreToDbKeepCamelColumns = marketingCommonConfig.getXieChengScoreToDbKeepCamelColumns();
        for (String column : transferColumn) {
            if (xieChengScoreToDbKeepCamelColumns.contains(column)) {
                continue;
            }
            // 驼峰格式的正则表达式
            if (column.matches(REGEX)) {
                String errMsg = "文件表头字段为驼峰格式，文件path：" + file.getAbsolutePath() + "，字段：" + column;
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), errMsg));
            }
        }
    }

    private void createTidbAndDorisTable(List<String> columns, List<String> firstLine, String tableName) {
        // tidb建表ddl
        StringBuilder createTidbDDL = new StringBuilder();
        // doris建表ddl
        StringBuilder createDorisDDL = new StringBuilder();

        createTidbDDL.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (")
                .append(" id bigint auto_increment primary key, ");

        createDorisDDL.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (")
                .append(" id bigint, ");

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

        createTidbDDL.append(" extend longtext,")
                .append(" create_time datetime,")
                .append(" update_time timestamp null on update CURRENT_TIMESTAMP,")
                .append(" is_delete int default 0,")
                .append(" unique index idx_cell (cell) ")
                .append("); ");

        scoreRecordMapper.createXieChengScoreTidbTableByBatchNum(createTidbDDL.toString());

        createDorisDDL.append(" extend string,")
                .append(" create_time datetime,")
                .append(" update_time datetime,")
                .append(" is_delete int default '0'")
                .append(") ENGINE=OLAP\n" +
                        "Unique KEY(id)\n" +
                        "DISTRIBUTED BY HASH(id) BUCKETS 16\n" +
                        "PROPERTIES (\n" +
                        "'replication_allocation' = '").append(replicationAllocation)
                .append("',\n" +
                        "'in_memory' = 'false',\n" +
                        "'storage_format' = 'V2',\n" +
                        "'disable_auto_compaction' = 'false'\n" +
                        ");");

        scoreRecordMapper.createXieChengScoreDorisTableByBatchNumdoris_(createDorisDDL.toString());
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
            StringBuilder insertSql = new StringBuilder("INSERT IGNORE INTO ");
            insertSql.append(tableName).append(" (");
            List<Integer> numColumns = new ArrayList<>(columns.size());
            for (int i = 0; i < columns.size(); i++) {
                insertSql.append(columns.get(i).trim()).append(", ");

                if (columns.get(i).startsWith("score") || columns.get(i).endsWith("age")) {
                    numColumns.add(i);
                }
            }

            insertSql.append("extend,").append("create_time,")
                    .append("update_time,").append("is_delete").append(") VALUES ");

            List<String> dataList;
            // 遍历每行
            for (String dataLine : batchData) {
                dataList = Arrays.asList(dataLine.split(",", -1));
                insertSql.append("(");
                // 遍历每列
                for (int i = 0; i < dataList.size(); i++) {
                    String value = dataList.get(i).trim();
                    if (numColumns.contains(i) && StringUtils.isEmpty(value)) {
                        insertSql.append("null, ");
                    } else {
                        insertSql.append("'").append(value).append("', ");
                    }
                }

                insertSql.append("null, now(), now(), 0").append("),");
            }

            insertSql.setLength(insertSql.length() - 1);
            ruleScoreToDbService.insertXieChengScoreTidbTable(insertSql.toString(), batchData);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程跑分数据同步作业，子线程异常"), e);
        }
    }

    @RetryMethod(retryNowNum = 2)
    public Result insertXieChengScoreTidbTable(String insertSql, List<String> batchData) {
        try {
            scoreRecordMapper.insertXieChengScoreTidbTable(insertSql);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程跑分数据同步作业,写入数据库异常:" + String.join(";", batchData)), e);
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }

        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void sendDingDing(String msg) {
        Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
        Map<String, Object> map = webHookInfo.get(DingDingAlarmFunctionEnum.XIECHENG_TRUE_DELETE_NOTICE.toString());

        dingDingRobotHookService.sendDingDingTextMessage(msg, map);
    }

    /**
     * 修改线程池大小
     * @param pool
     */
    private void modifyThreadPool(ThreadPoolExecutor pool) {
        Integer threadNum = marketingCommonConfig.getXieChengCollidingRuleScoreToDBThread();
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, threadNum);
    }
}