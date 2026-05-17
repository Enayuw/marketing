package com.br.marketing.check.job.dataclean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.MarketingCleanDataFile;
import com.br.marketing.entity.MarketingCleanDataFileExample;
import com.br.marketing.entity.MarketingDataCleanGeneralConfig;
import com.br.marketing.entity.MarketingCleanPersistTask;
import com.br.marketing.entity.MarketingCleanPersistTaskExample;
import com.br.marketing.entity.SftpVirtualHeaderScriptConfig;
import com.br.marketing.entity.SftpVirtualHeaderScriptConfigExample;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncConfigExample;
import com.br.marketing.enums.clean.CleanPersistTaskStatusEnum;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.enums.clean.IsPersEnum;
import com.br.marketing.enums.clean.SftpVirtualHeaderScriptStatusEnum;
import com.br.marketing.enums.sync.SyncConfigIsUnzipEnum;
import com.br.marketing.mapper.MarketingCleanDataFileMapper;
import com.br.marketing.mapper.MarketingCleanPersistTaskMapper;
import com.br.marketing.mapper.SftpVirtualHeaderScriptConfigMapper;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.mapper.rulecleaning.MarketingDataCleanGeneralConfigMapper;
import com.br.marketing.service.IFileActionService;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.clean.common.DataCleanService;
import com.br.marketing.util.DataCleanDelimiterUtils;
import com.br.marketing.utils.CleanDataFileReader;
import com.br.marketing.utils.CleanDataFileReader.HeaderAndLines;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.Lists;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
/**
 * @author zhen.Li1
 * @Classname DataCleanFileSyncJob
 * @Description 数据清洗文件同步
 * @Date 2024/05/22
 */
public class DataCleanFileSyncJob extends AbstractSimpleElasticJob {

    @Resource
    SyncConfigMapper syncConfigMapper;

    @Autowired
    SyncConfigService syncConfigService;

    @Autowired
    IFileActionService iFileActionService;

    @Autowired
    MarketingCleanDataFileMapper marketingCleanDataFileMapper;
    @Resource
    private MarketingCleanPersistTaskMapper marketingCleanPersistTaskMapper;
    @Resource
    private TrackingService trackingService;
    @Resource
    private DataCleanService dataCleanService;

    @Resource
    private MarketingDataCleanGeneralConfigMapper cleanGeneralConfigMapper;
    @Autowired
    private SftpVirtualHeaderScriptConfigMapper sftpVirtualHeaderScriptConfigMapper;

    @Resource(name = "cleanRuleAviatorEvaluatorInstance")
    private AviatorEvaluatorInstance cleanRuleAviatorEvaluatorInstance;

    @Value("${otherConfig.warning.sftpHost:00}")
    private String sftpHost;
    @Value("${otherConfig.warning.sftpPort:00}")
    private Integer sftpPort;
    @Value("${otherConfig.warning.sftpUser:00}")
    private String sftpUsername;
    @Value("${otherConfig.warning.sftpPwd:00}")
    private String sftpPwd;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        String jobParameter = context.getJobParameter();
        String apiCode = StringUtils.isBlank(jobParameter) ? "" : jobParameter;
        SyncConfigExample syncConfigExample = new SyncConfigExample();
        SyncConfigExample.Criteria criteria = syncConfigExample.createCriteria();
        if (StringUtils.isNotBlank(apiCode)) {
            criteria.andApiCodeEqualTo(apiCode);
        }
        criteria.andStatusEqualTo(1).andDataTypeEqualTo(DataTypeEnum.MARKETINGUPLOADDATA.getValue())
                .andTypeEqualTo(1);
        List<SyncConfig> syncConfigs = syncConfigMapper.selectByExample(syncConfigExample);
        for (SyncConfig syncConfig : syncConfigs) {
            // 组装本地下载路径
            String targetPath = syncConfigService.getPath().concat("initPath/upload/").concat(syncConfig.getApiCode()).concat("/")
                    .concat(LocalDate.now().toString()).concat("/");
            SftpClient sftpClient = new SftpClient(sftpHost, sftpPort, sftpUsername, sftpPwd);
            Result<List<String>> res = iFileActionService.downSyncFileBySftp(sftpClient, syncConfig, targetPath);
            if (ResultCode.SUCCESS.getValue().equals(res.getCode())) {
                List<String> fileNames = res.getData();
                for (String fileName : fileNames) {
                    fileSyncTable(syncConfig, targetPath, fileName);

                }
            }
        }
        Date date = Date.from(LocalDate.now().minusDays(1L).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        SyncConfigExample syncConfigCycle = new SyncConfigExample();
        SyncConfigExample.Criteria criteriaCycle = syncConfigCycle.createCriteria();
        criteriaCycle.andStatusEqualTo(1).andDataTypeEqualTo(DataTypeEnum.MARKETING_UP_CYCLE_DATA.getValue())
                .andTypeEqualTo(1);
        List<SyncConfig> syncCycleConfigs = syncConfigMapper.selectByExample(syncConfigCycle);
        for (SyncConfig syncCycleConfig : syncCycleConfigs) {
            if (!SyncConfigIsUnzipEnum.needUnzip(syncCycleConfig.getIsUnzip())
                    && StringUtils.isNotBlank(syncCycleConfig.getSuffix())
                    && StringUtils.lowerCase(syncCycleConfig.getSuffix()).contains(".zip")) {
                continue;
            }
            //填充b_marketing_clean_data_file表的表头及字段（targetPath 中 yyyyMMdd/yyyy-MM-dd 替换为当天日期后查询）
            String localPathForQuery = replaceDateInTargetPath(syncCycleConfig.getTargetPath());
            MarketingCleanDataFileExample fileExample = new MarketingCleanDataFileExample();
            fileExample.createCriteria().andCreateTimeGreaterThanOrEqualTo(date).andApiCodeEqualTo(syncCycleConfig.getApiCode())
                    .andLocalPathEqualTo(localPathForQuery).andFileHeaderIsNull();
            fileExample.setOrderByClause("create_time desc");
            List<MarketingCleanDataFile> cleanDataFiles = marketingCleanDataFileMapper.selectByExample(fileExample);
            cleanDataFiles.forEach(cleanDataFile -> {
                log.warn("开始填充文件表头及样例,fileName={}", cleanDataFile.getFileName());
                fillHeaderAndData(cleanDataFile);
            });
        }

    }

    private void fillHeaderAndData(MarketingCleanDataFile cleanDataFile) {
        File file = new File(cleanDataFile.getLocalPath().concat(cleanDataFile.getFileName()));
        HeaderAndLines headerAndLines = CleanDataFileReader.read(file, cleanDataFile.getFileName(), 10);
        String headerLine = headerAndLines.getHeaderLine();
        List<String> batchLines = headerAndLines.getDataLines();
        if (StringUtils.isBlank(headerLine)) {
            log.warn("文件表头为空，跳过填充 fileName={}", cleanDataFile.getFileName());
            return;
        }
        String syncSrcPath = null;
        if (cleanDataFile.getSyncConfigId() != null) {
            SyncConfig sc = syncConfigMapper.selectByPrimaryKey(cleanDataFile.getSyncConfigId());
            if (sc != null) {
                syncSrcPath = sc.getSrcPath();
            }
        }
        String fieldDelimiter = resolveFieldDelimiter(cleanDataFile.getApiCode(), syncSrcPath);
        String[] headerCols = DataCleanDelimiterUtils.splitLine(headerLine, fieldDelimiter);
        MarketingCleanDataFile dataFile = new MarketingCleanDataFile();
        dataFile.setFileHeader(headerLine);
        if (!batchLines.isEmpty()) {
            dataFile.setFileData(batchLines.get(0));
        }
        List<JSONObject> jsonList = dataCleanService.fileDataAssemble(
                batchLines, headerCols, cleanDataFile.getFileName(), 0, null, fieldDelimiter);
        dataFile.setId(cleanDataFile.getId());
        dataFile.setReceiveDate(LocalDate.now().toString());
        dataFile.setTestRunData(JSON.toJSONString(jsonList));
        String virtualHeadersJson = evaluateVirtualHeadersScript(
                cleanDataFile.getSyncConfigId(), cleanDataFile.getFileName());
        if (virtualHeadersJson != null) {
            dataFile.setVirtualHeaders(virtualHeadersJson);
        }
        marketingCleanDataFileMapper.updateByPrimaryKeySelective(dataFile);

        insertPersistTaskIfNeeded(cleanDataFile, headerLine, fieldDelimiter);

        try {
            String remark = String.format("手动清洗-文件样例同步,文件名称：%s"
                    , cleanDataFile.getFileName());
            trackingService.trackPointLog(DataFlowDirection.OUT
                    , cleanDataFile.getApiCode()
                    , "手动清洗-文件样例同步,"
                    , (long) batchLines.size()
                    , remark
                    , TrackingContext.generateBatchId());
        } catch (Exception ex) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                            , ex.getMessage()
                            , "埋点异常")
                    , ex);
        }

    }

    /**
     * is_pers=1 时创建文件清洗持久化任务（在更新完 dataFile 表头后插入，保证 fileHeader 有值）
     */
    /**
     * @param fieldDelimiter 与表头/样例解析一致的分隔符（{@link #resolveFieldDelimiter}），落库供本地文件按列切分
     */
    private void insertPersistTaskIfNeeded(MarketingCleanDataFile cleanDataFile, String fileHeader, String fieldDelimiter) {
        if (cleanDataFile.getSyncConfigId() == null) {
            return;
        }
        SyncConfig syncConfig = syncConfigMapper.selectByPrimaryKey(cleanDataFile.getSyncConfigId());
        if (syncConfig == null || !IsPersEnum.isPers(syncConfig.getIsPers())) {
            return;
        }
        MarketingCleanPersistTaskExample ex = new MarketingCleanPersistTaskExample();
        ex.createCriteria().andCleanDataFileRecordIdEqualTo(cleanDataFile.getId());
        if (marketingCleanPersistTaskMapper.countByExample(ex) > 0) {
            return;
        }
        try {
            MarketingCleanPersistTask task = new MarketingCleanPersistTask();
            task.setCleanDataFileRecordId(cleanDataFile.getId());
            task.setSyncConfigId(syncConfig.getId());
            task.setFileHeader(fileHeader);
            task.setFileName(cleanDataFile.getFileName());
            task.setSftpFileSeparator(fieldDelimiter);
            task.setLocalPath(cleanDataFile.getLocalPath());
            task.setStatus(CleanPersistTaskStatusEnum.PENDING.getCode());
            Date now = new Date();
            task.setCreateTime(now);
            task.setUpdateTime(now);
            marketingCleanPersistTaskMapper.insertSelective(task);
        } catch (Exception e) {
            log.warn("创建清洗持久化任务失败，cleanDataFileId:{}, syncConfigId:{}, error:{}",
                    cleanDataFile.getId(), syncConfig.getId(), e.getMessage(), e);
        }
    }

    private void fileSyncTable(SyncConfig syncConfig, String path, String fileName) {
        MarketingCleanDataFile dataFile = new MarketingCleanDataFile();
        String fileStr = path.concat(fileName);
        List<String> batchLines = new ArrayList<>();
        File file = new File(fileStr);
        Integer line = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String row;
            while (line < 11 && (row = br.readLine()) != null) {
                String rowData = row.trim();
                // 跳过空行（包含空白字符行）
                if (rowData.isEmpty()) {
                    continue;
                }
                if (line == 0) {
                    // 第一行作为表头
                    dataFile.setFileHeader(rowData);
                } else {
                    // 除表头外的所有数据行都添加到batchLines
                    batchLines.add(rowData);
                    if (line == 1) {
                        // 第一行数据设置为FileData
                        dataFile.setFileData(rowData);
                    }
                }
                line++;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        String fieldDelimiter = resolveFieldDelimiter(syncConfig.getApiCode(), syncConfig.getSrcPath());
        String[] headerCols = DataCleanDelimiterUtils.splitLine(dataFile.getFileHeader(), fieldDelimiter);
        List<JSONObject> jsonList = dataCleanService.fileDataAssemble(
                batchLines, headerCols, fileName, 0, null, fieldDelimiter);
        dataFile.setApiCode(syncConfig.getApiCode());
        dataFile.setFileName(fileName);
        dataFile.setLocalPath(path);
        dataFile.setTargetSftpPath(syncConfig.getTargetPath());
        dataFile.setCleanType(0);
        dataFile.setCreateTime(new Date());
        dataFile.setUpdateTime(new Date());
        dataFile.setReceiveDate(LocalDate.now().toString());
        dataFile.setTestRunData(JSON.toJSONString(jsonList));
        marketingCleanDataFileMapper.insertSelective(dataFile);

        try {
            String remark = String.format("清洗系统-文件样例同步,文件名称：%s"
                    , fileStr);
            trackingService.trackPointLog(DataFlowDirection.OUT
                    , syncConfig.getApiCode()
                    , "清洗系统-文件样例同步"
                    , Long.valueOf(batchLines.size())
                    , remark
                    , TrackingContext.generateBatchId());
        } catch (Exception ex) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                            , ex.getMessage()
                            , "埋点异常")
                    , ex);
        }

    }

    /**
     * 将路径中的 yyyyMMdd 或 yyyy-MM-dd 替换为当天日期，用于按本地路径查询 dataFile（与 FileUploadDownloadServiceImpl.replaceDateInPath 逻辑一致）。
     */
    private String replaceDateInTargetPath(String targetPath) {
        if (StringUtils.isBlank(targetPath)) {
            return targetPath;
        }
        String todayYyyyMMdd = DateHelper.getDateAddYyMmDd(0);
        if (targetPath.contains("yyyy-MM-dd")) {
            String todayYyyyMmDd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return targetPath.replace("yyyy-MM-dd", todayYyyyMmDd);
        }
        if (targetPath.contains("yyyyMMdd")) {
            return targetPath.replace("yyyyMMdd", todayYyyyMMdd);
        }
        return targetPath;
    }

    /**
     * 根据 sync_config_id 从 b_sftp_virtual_header_script_config 表取 Aviator 脚本，入参 file_name 执行，
     * 将返回的虚拟 header 键值对序列化为 JSON 写入 virtual_headers。
     *
     * @param syncConfigId b_sync_config.id
     * @param fileName     文件名，作为脚本入参 file_name
     * @return JSON 字符串，异常或未配置时返回 null
     */
    private String evaluateVirtualHeadersScript(Long syncConfigId, String fileName) {
        if (syncConfigId == null) {
            return null;
        }
        SftpVirtualHeaderScriptConfigExample example = new SftpVirtualHeaderScriptConfigExample();
        example.createCriteria()
                .andSyncConfigIdEqualTo(syncConfigId)
                .andStatusEqualTo(SftpVirtualHeaderScriptStatusEnum.ENABLED.getValue());
        example.setOrderByClause("id asc");
        List<SftpVirtualHeaderScriptConfig> list = sftpVirtualHeaderScriptConfigMapper.selectByExample(example);
        if (list == null || list.isEmpty()) {
            return null;
        }
        String script = list.get(0).getScriptContent();
        if (StringUtils.isBlank(script)) {
            return null;
        }
        Map<String, Object> env = new HashMap<>();
        env.put("file_name", fileName != null ? fileName : "");
        try {
            Object result = cleanRuleAviatorEvaluatorInstance.execute(script, env);
            if (result == null) {
                return null;
            }
            return JSON.toJSONString(result);
        } catch (Exception e) {
            String msg = String.format(
                    "虚拟header Aviator脚本执行失败 syncConfigId=%s fileName=%s error=%s",
                    syncConfigId, fileName, e.getMessage());
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                    msg,
                    "虚拟header Aviator脚本执行异常"), e);
            return null;
        }
    }

    /**
     * 与 FTP 上传清洗配置一致：按 apiCode、SFTP 源路径匹配规则，取 sftp_file_separator；无配置时默认逗号。
     */
    private String resolveFieldDelimiter(String apiCode, String syncSrcPath) {
        if (StringUtils.isBlank(apiCode)) {
            return DataCleanDelimiterUtils.resolveDelimiter(null);
        }
        MarketingDataCleanGeneralConfig queryParam = new MarketingDataCleanGeneralConfig();
        queryParam.setAcceptType(DataProcessEnum.AcceptTypeEnum.FTP.getCode());
        queryParam.setDataType(DataProcessEnum.DataTypeEnum.UPLOAD.getCode());
        queryParam.setApiCode(apiCode);
        if (StringUtils.isNotBlank(syncSrcPath)) {
            queryParam.setSftpPath(syncSrcPath);
        }
        List<MarketingDataCleanGeneralConfig> list = cleanGeneralConfigMapper.selectRuleList(queryParam);
        if (CollectionUtils.isEmpty(list)) {
            return DataCleanDelimiterUtils.resolveDelimiter(null);
        }
        return DataCleanDelimiterUtils.resolveDelimiter(list.get(0).getSftpFileSeparator());
    }
}
