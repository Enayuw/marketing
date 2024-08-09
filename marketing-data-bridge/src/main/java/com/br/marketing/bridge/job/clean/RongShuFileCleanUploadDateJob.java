package com.br.marketing.bridge.job.clean;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.file.ZipUtils;
import com.br.marketing.entity.MarketingCleanDataFile;
import com.br.marketing.entity.MarketingCleanDataFileExample;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.clean.MarketingCleanCreateTaskRule;
import com.br.marketing.entity.clean.MarketingCleanCreateTaskRuleExample;
import com.br.marketing.entity.clean.RongshuPaofenFileUpdateSyncCleanLog;
import com.br.marketing.mapper.MarketingCleanDataFileMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.clean.MarketingCleanCreateTaskRuleMapper;
import com.br.marketing.mapper.clean.rongshu.RongshuPaofenFileUpdateSyncCleanLogMapper;
import com.br.marketing.service.IMarketingDataValidService;
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
import java.util.*;


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
        LocalDateTime localDateTime = LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toLocalDateTime();
        // 前一天
        Instant instant = localDateTime.plusDays(1).atZone(ZoneId.systemDefault()).toInstant();
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
                    if (1 == taskRule.getIsMd5Check()) {
                        for (MarketingCleanDataFile dataFile : cleanDataFiles) {
                            String md5Value = dataFile.getMd5Value();
                            MarketingCleanDataFileExample fileExampleCount = new MarketingCleanDataFileExample();
                            fileExampleCount.createCriteria().andApiCodeEqualTo(apiCode).andSyncConfigIdEqualTo(syncConfigId)
                                    .andMd5ValueEqualTo(md5Value).andIdNotEqualTo(dataFile.getId());
                            long l = marketingCleanDataFileMapper.countByExample(fileExampleCount);
                            if (l > 0) {
                                continue;
                            }
                            String fileName = dataFile.getFileName();
                            String localPath = dataFile.getLocalPath();
                            if (fileName.contains(".zip")) {
                                String localUnzipPath = localPath.concat(File.separator).concat("unzip").concat(File.separator);
                                ZipUtils.unZip(new File(dataFile.getLocalPath()), localUnzipPath, taskRule.getZipPassword());
                                File dir = new File(localUnzipPath);
                                File[] files = dir.listFiles();
                                if (files != null) {
                                    for (File file : files) {
                                        Set<String> appletDateSet = iMarketingDataValidService.getAppletDateSet(apiCode, localDate.toString());
                                        String name = file.getName();
                                        try {
                                            RandomAccessFile accessFile = new RandomAccessFile(file.getAbsoluteFile(), "r");
                                            int rowNum = 1;
                                            String fileHeader = "";
                                            MarketingCleanDataFile dataFileNew = null;
                                            String[] fileHeaders = null;
                                            Map<String, JSONObject> map = new HashMap<>();
                                            while (accessFile.readBoolean()) {
                                                String rowData = accessFile.readLine();
                                                if (rowNum < 3) {
                                                    if (rowNum == 1) {
                                                        fileHeader = rowData;
                                                        fileHeaders = fileHeader.split(regex);
                                                        rowNum++;
                                                        continue;
                                                    } else {
                                                        dataFileNew = saveDataFileInfo(name, syncConfigId, localUnzipPath
                                                                , localPath, "", apiCode, fileHeader, rowData);
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
                                                if (map.size() == 2000 && dataFileNew != null) {
                                                    List<MarketingSyncUser> list = marketingSyncUserMapper
                                                            .getReserveFieldByCustNumAndAppletDateList(apiCode, map.keySet(), appletDateSet);
                                                    for (MarketingSyncUser syncUser : list) {
                                                        String reserveField1 = syncUser.getReserveField1();
                                                        RongshuPaofenFileUpdateSyncCleanLog cleanLog = new RongshuPaofenFileUpdateSyncCleanLog();
                                                        cleanLog.setApiCode(apiCode);
                                                        cleanLog.setHistoryDataJson(reserveField1);
                                                        cleanLog.setSyncApicodeId(syncUser.getId());
                                                        cleanLog.setMarketingCleanDataFileId(dataFileNew.getId());
                                                        cleanLog.setIsSuccess(1);
                                                        if (JSONObject.isValidObject(reserveField1)) {
                                                            JSONObject newData = map.get(syncUser.getCustNum());
                                                            JSONObject oldData = JSONObject.parseObject(reserveField1);
                                                            newData.forEach((String key, Object value) -> {
                                                                oldData.put(key, value.toString());
                                                            });
                                                            syncUser.setReserveField1(oldData.toJSONString());
                                                            marketingSyncUserMapper.updateReserveFieldByPrimaryKey(syncUser);
                                                            cleanLog.setIsSuccess(0);
                                                        }
                                                        rongshuPaofenFileUpdateSyncCleanLogMapper.insertSelective(cleanLog);
                                                    }
                                                }
                                                rowNum++;
                                            }
                                        } catch (IOException e) {
                                            log.warn(e.getMessage(), e);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        for (MarketingCleanDataFile dataFile : cleanDataFiles) {
// TODO: 2024-08-09  实现 
                        }
                    }

                }

            }
        }

    }

    /**
     * 2024-08-08 22:35
     * 保存文件信息
     *
     * @param fileName     文件名
     * @param syncConfigId sftp配置信息id
     * @param targetPath   目标目录
     * @param srcPath      源目录
     * @param md5Value     md5
     */
    private MarketingCleanDataFile saveDataFileInfo(String fileName, Long syncConfigId
            , String targetPath, String srcPath, String md5Value, String apiCode
            , String fileHeader, String fileData) {
        MarketingCleanDataFile dataFile = new MarketingCleanDataFile();
        dataFile.setFileName(fileName);
        dataFile.setApiCode(apiCode);
        dataFile.setFileHeader(fileHeader);
        dataFile.setFileData(fileData);
        dataFile.setCreateTime(new Date());
        dataFile.setLocalPath(targetPath);
        dataFile.setUpdateTime(new Date());
        dataFile.setTargetSftpPath(srcPath);
        dataFile.setMd5Value(md5Value);
        dataFile.setSyncConfigId(syncConfigId);
        int i = marketingCleanDataFileMapper.insertSelective(dataFile);
        if (i < 1) {
            log.warn("添加清洗文件失败！fileName:{},targetPath:{},srcPath:{},md5Value:{},syncConfigId:{}"
                    , fileName, targetPath, srcPath, md5Value, syncConfigId);
            return null;
        }
        return dataFile;
    }

}
