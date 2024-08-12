package com.br.marketing.bridge.job.clean;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.file.ZipUtils;
import com.br.marketing.entity.MarketingCleanDataFile;
import com.br.marketing.entity.MarketingCleanDataFileExample;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.clean.MarketingCleanCreateTaskRule;
import com.br.marketing.entity.clean.MarketingCleanCreateTaskRuleExample;
import com.br.marketing.entity.clean.rongshu.RongshuPaofenFileUpdateSyncCleanLog;
import com.br.marketing.enums.DingDingAlarmFunctionEnum;
import com.br.marketing.mapper.MarketingCleanDataFileMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.clean.MarketingCleanCreateTaskRuleMapper;
import com.br.marketing.mapper.clean.rongshu.RongshuPaofenFileUpdateSyncCleanLogMapper;
import com.br.marketing.service.IMarketingDataValidService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


/**
 * D20240729榕树上传数据更新-4004643
 * https://c.100credit.cn/pages/viewpage.action?pageId=166656716
 *
 * @author Guo Zeqiang
 * @date 2024-08-08 13:37
 */
@Component
@Slf4j
public class RongShuFileCleanUploadDateJob extends AbstractSimpleElasticJob {


    @Resource
    private MarketingCleanCreateTaskRuleMapper marketingCleanCreateTaskRuleMapper;

    @Resource
    private MarketingCleanDataFileMapper marketingCleanDataFileMapper;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    private IMarketingDataValidService iMarketingDataValidService;

    @Resource
    private RongshuPaofenFileUpdateSyncCleanLogMapper rongshuPaofenFileUpdateSyncCleanLogMapper;

    @Resource
    private DingDingRobotHookService dingDingRobotHookService;

    private static final ThreadPoolExecutor THREAD_POOL = BrExecutors.getThreadPool(
            Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors() + 2
            , "rongShu-file-clean-upload-data-%d");

    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    /**
     * 2024-08-08 15:51
     * JobParameter 参数为apiCode
     */
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String apiCode = context.getJobParameter();
        if (StringUtils.isEmpty(apiCode)) {
            apiCode = "4004643";
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", "7f32618dafd2d2126f5564aaf57a35867c8775baf78777140990c16d56edc457");
        jsonObject.put("secret", "SEC4d2d8a91842ad25136e92213a852ebe5cf1c22ddaf49dcfd352d5a9323eb1ca8");
        JSONObject map = marketingCommonConfig.getDingDingWebHookInfo().getOrDefault(
                DingDingAlarmFunctionEnum.RONGSHU_FILE_CLEAN_UPLOAD_READFILE.toString(), jsonObject);
        LocalDateTime localDateTime = LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toLocalDateTime();
        // 前一天
        Instant instant = localDateTime.minusDays(1).atZone(ZoneId.systemDefault()).toInstant();
        LocalDate localDate = localDateTime.toLocalDate();
        LocalTime time = localDateTime.toLocalTime();
        String regex = ",";
        MarketingCleanCreateTaskRuleExample example = new MarketingCleanCreateTaskRuleExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo(0).andDataTypeEqualTo(1);
        List<MarketingCleanCreateTaskRule> taskRules = marketingCleanCreateTaskRuleMapper.selectByExample(example);
        for (MarketingCleanCreateTaskRule taskRule : taskRules) {
            if (1 == taskRule.getTaskCreateRule() && 1 == taskRule.getDataType()) {
                String startTime = taskRule.getStartTime();
                if (StringUtils.isEmpty(startTime) || time.isBefore(LocalTime.parse(startTime))) {
                    Long syncConfigId = taskRule.getSyncConfigId();
                    MarketingCleanDataFileExample fileExample = new MarketingCleanDataFileExample();
                    fileExample.createCriteria().andApiCodeEqualTo(apiCode).andSyncConfigIdEqualTo(syncConfigId)
                            .andIsDelEqualTo(1).andCreateTimeGreaterThanOrEqualTo(Date.from(instant));
                    fileExample.setOrderByClause("create_time");
                    List<MarketingCleanDataFile> cleanDataFiles = marketingCleanDataFileMapper.selectByExample(fileExample);
                    for (MarketingCleanDataFile dataFile : cleanDataFiles) {
                        String md5Value = dataFile.getMd5Value();
                        String fileName = dataFile.getFileName();
                        try {
                            try {
                                dingDingRobotHookService.sendDingDingTextMessage(
                                        "榕树上传数据更新-" + apiCode + "开始[" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                                + "]，文件：" + fileName, map);
                            } catch (Exception e) {
                                log.warn(e.getMessage(), e);
                            }
                            if (1 == taskRule.getIsMd5Check() && StringUtils.isNotBlank(md5Value)) {
                                MarketingCleanDataFileExample fileExampleCount = new MarketingCleanDataFileExample();
                                fileExampleCount.createCriteria().andApiCodeEqualTo(apiCode).andSyncConfigIdEqualTo(syncConfigId)
                                        .andIdNotEqualTo(dataFile.getId()).andCreateTimeLessThanOrEqualTo(dataFile.getCreateTime());
                                fileExampleCount.setOrderByClause("create_time desc limit 1");
                                List<MarketingCleanDataFile> marketingCleanDataFiles = marketingCleanDataFileMapper.selectByExample(fileExampleCount);
                                if (marketingCleanDataFiles.size() > 0 && md5Value.equals(marketingCleanDataFiles.get(0).getMd5Value())) {
                                    MarketingCleanDataFile dataFileOld = marketingCleanDataFiles.get(0);
                                    dingDingRobotHookService.sendDingDingTextMessage(
                                            "榕树上传数据更新-" + apiCode + "文件：" + fileName + "与最近("
                                                    + dataFileOld.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                                                    .toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ")的文件"
                                                    + dataFileOld.getFileName() + "内容重复，本次文件不进行清洗，文件MD5值：" + md5Value, map);
                                    MarketingCleanDataFile dataFileUpdate = new MarketingCleanDataFile();
                                    dataFileUpdate.setId(dataFile.getId());
                                    dataFileUpdate.setIsDel(9);
                                    marketingCleanDataFileMapper.updateByPrimaryKeySelective(dataFileUpdate);
                                    continue;
                                }
                            }
                            boolean bool = false;
                            String localPath = dataFile.getLocalPath();
                            File srcFile = new File(localPath.concat(File.separator).concat(fileName));
                            if (!srcFile.exists()) {
                                log.warn("荣树({})待清洗文件{}不存在,目录：{}", apiCode, fileName, localPath);
                            }
                            if (fileName.contains(".zip")) {
                                String localUnzipPath = localPath.concat(File.separator).concat("unzip".concat(File.separator)
                                        + fileName + System.currentTimeMillis()).concat(File.separator);
                                ZipUtils.unZip(srcFile, localUnzipPath, taskRule.getZipPassword());
                                File dir = new File(localUnzipPath);
                                File[] files = dir.listFiles();
                                if (files != null) {
                                    bool = true;
                                    for (File file : files) {
                                        bool = bool && readFile(dataFile, file, regex, localDate, true);
                                    }
                                    MarketingCleanDataFile dataFileUpdate = new MarketingCleanDataFile();
                                    dataFileUpdate.setId(dataFile.getId());
                                    dataFileUpdate.setIsDel(9);
                                    marketingCleanDataFileMapper.updateByPrimaryKeySelective(dataFileUpdate);
                                }
                            } else {
                                bool = readFile(dataFile, srcFile, regex, localDate, false);
                            }
                            try {
                                dingDingRobotHookService.sendDingDingTextMessage("榕树上传数据更新-" + apiCode + "结束["
                                        + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                        + "]，文件：" + fileName + ",清洗" + (bool ? "成功" : "失败"), map);
                            } catch (Exception e) {
                                log.warn(e.getMessage(), e);
                            }
                        } catch (Exception e) {
                            log.warn(e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }


    /**
     * 2024-08-12 11:28
     *
     * @param file      文件
     * @param dataFile  文件信息
     * @param localDate 日期
     * @param regex     分隔符
     * @return true 成功
     */
    private boolean readFile(MarketingCleanDataFile dataFile, File file, String regex, LocalDate localDate, boolean isCreate) {
        String name = file.getName();
        String apiCode = dataFile.getApiCode();
        Set<String> appletDateSet = iMarketingDataValidService.getAppletDateSet(apiCode, localDate.toString());
        try {
            RandomAccessFile accessFile = new RandomAccessFile(file, "r");
            int rowNum = 1;
            String fileHeader = "";
            MarketingCleanDataFile dataFileNew = null;
            String[] fileHeaders = null;
            Map<String, JSONObject> map = new HashMap<>(2048);
            String rowData;
            while ((rowData = accessFile.readLine()) != null) {
                if (rowNum < 3) {
                    if (rowNum == 1) {
                        fileHeader = rowData;
                        fileHeaders = fileHeader.split(regex);
                        rowNum++;
                        continue;
                    } else {
                        dataFileNew = isCreate ? saveDataFileInfo(dataFile, name, file.getParent(), "", fileHeader, rowData)
                                : dataFile;
                        dataFileNew.setFileData(rowData);
                    }
                }
                String[] split = rowData.split(regex);
                int length = fileHeaders.length;
                String uid = split[0];
                JSONObject object = new JSONObject();
                for (int i = 1; i < length; i++) {
                    object.put(fileHeaders[i], split[i]);
                }
                map.put(uid, object);
                if (map.size() == 2000 && dataFileNew != null && dataFileNew.getId() != null) {
                    Map<String, JSONObject> finalMap = map;
                    MarketingCleanDataFile finalDataFileNew = dataFileNew;
                    THREAD_POOL.submit(() -> {
                        update(apiCode, finalMap, appletDateSet, finalDataFileNew);
                        finalMap.clear();
                    });
                    map = new HashMap<>(2048);
                }
                rowNum++;
            }
            if (dataFileNew != null && dataFileNew.getId() != null) {
                if (map.size() != 0) {
                    update(apiCode, map, appletDateSet, dataFileNew);
                }
                MarketingCleanDataFile dataFileUpdate = new MarketingCleanDataFile();
                dataFileUpdate.setId(dataFileNew.getId());
                dataFileUpdate.setIsDel(9);
                if (!isCreate) {
                    dataFileUpdate.setFileData(dataFileNew.getFileData());
                    dataFileUpdate.setFileHeader(fileHeader);
                }
                marketingCleanDataFileMapper.updateByPrimaryKeySelective(dataFileUpdate);
            }
        } catch (IOException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ERROR_UNKNOWN.getCode(), e.getMessage()
                    , "榕树清洗上传数据异常-" + apiCode), e);
            return false;
        }
        return true;
    }


    /**
     * 2024-08-08 22:35
     * 保存文件信息
     *
     * @param fileName   文件名
     * @param targetPath 目标目录
     * @param md5Value   md5
     */
    private MarketingCleanDataFile saveDataFileInfo(MarketingCleanDataFile dataFile, String fileName
            , String targetPath, String md5Value
            , String fileHeader, String fileData) {
        String apiCode = dataFile.getApiCode();
        Long syncConfigId = dataFile.getSyncConfigId();
        String localPath = dataFile.getLocalPath();
        MarketingCleanDataFile dataFileNew = new MarketingCleanDataFile();
        dataFileNew.setFileHeader(fileHeader);
        dataFileNew.setFileName(fileName);
        dataFileNew.setApiCode(apiCode);
        dataFileNew.setFileData(fileData);
        dataFileNew.setCreateTime(new Date());
        dataFileNew.setLocalPath(targetPath);
        dataFileNew.setUpdateTime(new Date());
        dataFileNew.setTargetSftpPath(localPath);
        dataFileNew.setMd5Value(md5Value);
        dataFileNew.setSyncConfigId(syncConfigId);
        int i = marketingCleanDataFileMapper.insertSelective(dataFileNew);
        if (i < 1) {
            log.warn("添加清洗文件失败！fileName:{},targetPath:{},srcPath:{},md5Value:{},syncConfigId:{}"
                    , fileName, targetPath, localPath, md5Value, syncConfigId);
        }
        return dataFileNew;
    }

    /**
     * 2024-08-09 18:13
     * 更新
     */
    private void update(String apiCode, Map<String, JSONObject> map, Set<String> appletDateSet
            , MarketingCleanDataFile dataFileNew) {
        List<MarketingSyncUser> list = marketingSyncUserMapper
                .getReserveFieldByCustNumAndAppletDateList(apiCode, map.keySet(), appletDateSet);
        Set<Long> idSet = list.stream().map(MarketingSyncUser::getId).collect(Collectors.toSet());
        Set<Long> syncIds = new HashSet<>();
        if (idSet.size() > 0) {
            syncIds.addAll(rongshuPaofenFileUpdateSyncCleanLogMapper.getSyncApicodeId(apiCode, dataFileNew.getId(), idSet));
        }
        for (MarketingSyncUser syncUser : list) {
            syncUser.setApiCode(apiCode);
            Long id = syncUser.getId();
            if (syncIds.contains(id)) {
                continue;
            }
            String reserveField1 = syncUser.getReserveField1();
            RongshuPaofenFileUpdateSyncCleanLog cleanLog = new RongshuPaofenFileUpdateSyncCleanLog();
            cleanLog.setApiCode(apiCode);
            cleanLog.setHistoryDataJson(reserveField1);
            cleanLog.setSyncApicodeId(id);
            cleanLog.setMarketingCleanDataFileId(dataFileNew.getId());
            cleanLog.setIsSuccess(1);
            cleanLog.setCreateTime(new Date());
            cleanLog.setUid(syncUser.getCustNum());
            cleanLog.setUpdateTime(cleanLog.getUpdateTime());
            if (JSONObject.isValidObject(reserveField1)) {
                JSONObject newData = map.get(syncUser.getCustNum());
                cleanLog.setNewDataJson(newData.toJSONString());
                JSONObject oldData = JSONObject.parseObject(reserveField1);
                newData.forEach((String key, Object value) -> oldData.put(key, value.toString()));
                syncUser.setReserveField1(oldData.toJSONString());
                marketingSyncUserMapper.updateReserveFieldByPrimaryKey(syncUser);
                cleanLog.setIsSuccess(0);
            }
            rongshuPaofenFileUpdateSyncCleanLogMapper.insertSelective(cleanLog);
        }
    }

}
