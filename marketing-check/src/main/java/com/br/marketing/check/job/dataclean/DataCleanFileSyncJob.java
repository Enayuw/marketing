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
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncConfigExample;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.mapper.MarketingCleanDataFileMapper;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.service.IFileActionService;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.clean.common.impl.DataCleanServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.Lists;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
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
    private TrackingService trackingService;
    @Resource
    private DataCleanServiceImpl dataCleanService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

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
            //填充b_marketing_clean_data_file表的表头及字段
            MarketingCleanDataFileExample fileExample = new MarketingCleanDataFileExample();
            fileExample.createCriteria().andCreateTimeGreaterThanOrEqualTo(date).andApiCodeEqualTo(syncCycleConfig.getApiCode())
                    .andLocalPathEqualTo(syncCycleConfig.getTargetPath()).andFileHeaderIsNull();
            fileExample.setOrderByClause("create_time desc");
            List<MarketingCleanDataFile> cleanDataFiles = marketingCleanDataFileMapper.selectByExample(fileExample);
            cleanDataFiles.forEach(cleanDataFile -> {
                log.warn("开始填充文件表头及样例,fileName={}", cleanDataFile.getFileName());
                fillHeaderAndData(cleanDataFile);
            });
        }

    }

    private void fillHeaderAndData(MarketingCleanDataFile cleanDataFile) {
        MarketingCleanDataFile dataFile = new MarketingCleanDataFile();
        File file = new File(cleanDataFile.getLocalPath().concat(cleanDataFile.getFileName()));
        List<String> batchLines = new ArrayList<>();
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
        List<JSONObject> jsonList = dataCleanService.fileDataAssemble(
                batchLines, dataFile.getFileHeader().split(","), cleanDataFile.getFileName(), 0);
        dataFile.setId(cleanDataFile.getId());
        dataFile.setReceiveDate(LocalDate.now().toString());
        dataFile.setTestRunData(JSON.toJSONString(jsonList));
        String virtualHeadersJson = evaluateVirtualHeadersScript(
                cleanDataFile.getSyncConfigId(), cleanDataFile.getFileName());
        if (virtualHeadersJson != null) {
            dataFile.setVirtualHeaders(virtualHeadersJson);
        }
        marketingCleanDataFileMapper.updateByPrimaryKeySelective(dataFile);

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
        List<JSONObject> jsonList = dataCleanService.fileDataAssemble(
                batchLines, dataFile.getFileHeader().split(","), fileName, 0);
        dataFile.setApiCode(syncConfig.getApiCode());
        dataFile.setFileName(fileName);
        dataFile.setLocalPath(path);
        dataFile.setTargetSftpPath(syncConfig.getTargetPath());
        dataFile.setCleanType(0);
        dataFile.setCreateTime(new Date());
        dataFile.setUpdateTime(new Date());
        dataFile.setReceiveDate(LocalDate.now().toString());
        dataFile.setTestRunData(JSON.toJSONString(jsonList));
        String virtualHeadersJson = evaluateVirtualHeadersScript(
                syncConfig.getId(), fileName);
        if (virtualHeadersJson != null) {
            dataFile.setVirtualHeaders(virtualHeadersJson);
        }
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
     * 根据 sync_config_id 从 speed 配置取 Aviator 脚本，入参 file_name 执行，
     * 将返回的虚拟 header 键值对序列化为 JSON 写入 virtual_headers。
     *
     * @param syncConfigId b_sync_config.id
     * @param fileName     文件名，作为脚本入参 file_name
     * @return JSON 字符串，异常或未配置时返回 null
     */
    private String evaluateVirtualHeadersScript(Long syncConfigId, String fileName) {
        if (syncConfigId == null
                || marketingCommonConfig.getVirtualHeaderAviatorScriptConfig() == null) {
            return null;
        }
        String script = marketingCommonConfig.getVirtualHeaderAviatorScriptConfig()
                .get(String.valueOf(syncConfigId));
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
}
