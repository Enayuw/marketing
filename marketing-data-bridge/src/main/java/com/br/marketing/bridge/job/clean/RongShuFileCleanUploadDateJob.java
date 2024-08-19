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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

    private static final List<String> FILE_HEADER = new ArrayList<>(
            Arrays.asList("uid", "register_no_first_login", "first_login_no_borrow", "borrow_no_credit", "credit_no_loan"));


    /**
     * 2024-08-08 15:51
     * JobParameter 参数为apiCode
     */
    @SneakyThrows
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
        String regex = "\t";
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
                        stopCheckOrUpdate(apiCode);
                        String md5Value = dataFile.getMd5Value();
                        String fileName = dataFile.getFileName();
                        try {
                            dingDingRobotHookService.sendDingDingTextMessage(
                                    "榕树上传数据更新-" + apiCode + "开始[" + LocalDateTime.now().format(
                                            DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "]\n文件：" + fileName, map);
                        } catch (Exception e) {
                            log.warn(e.getMessage(), e);
                        }
                        long sum = 0;
                        boolean bool = true;
                        try {
                            if (1 == taskRule.getIsMd5Check() && StringUtils.isNotBlank(md5Value) && md5Check(dataFile, map)) {
                                continue;
                            }
                            String localPath = dataFile.getLocalPath();
                            File srcFile = new File(localPath.concat(File.separator).concat(fileName));
                            if (srcFile.exists()) {
                                if (fileName.contains(".zip")) {
                                    String localUnzipPath = localPath.concat(File.separator).concat("unzip".concat(File.separator)
                                            + fileName + System.currentTimeMillis()).concat(File.separator);
                                    ZipUtils.unZip(srcFile, localUnzipPath, taskRule.getZipPassword());
                                    File dir = new File(localUnzipPath);
                                    File[] files = dir.listFiles();
                                    if (files != null) {
                                        for (File file : files) {
                                            stopCheckOrUpdate(apiCode);
                                            long l = readFile(dataFile, file, regex, localDate, true);
                                            if (l < 0) {
                                                bool = false;
                                            } else {
                                                sum += l;
                                            }
                                        }
                                        updateDataFile(dataFile, false);
                                    }
                                } else {
                                    long l = readFile(dataFile, srcFile, regex, localDate, false);
                                    if (l < 0) {
                                        bool = false;
                                    } else {
                                        sum = l;
                                    }
                                }
                            } else {
                                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_USUAL_NOTICE.getCode()
                                        , "榕树(" + apiCode + ")待清洗文件" + fileName + "不存在\n目录：" + localPath
                                        , "榕树清洗上传数据异常-" + apiCode));
                                updateDataFile(dataFile, false);
                            }
                        } catch (Exception e) {
                            bool = false;
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_USUAL_NOTICE.getCode(),
                                    e.getMessage(), "榕树清洗上传数据异常-" + apiCode), e);
                            updateDataFile(dataFile, false);
                        }
                        try {
                            dingDingRobotHookService.sendDingDingTextMessage("榕树上传数据更新-" + apiCode + "结束["
                                    + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                    + "]\n文件：" + fileName + "\n清洗" + (bool ? "成功^_^\n清洗量级：" + sum : "失败!!!")
                                    + (sum < 1 ? "\n文件无内容" : ""), map);
                        } catch (Exception e) {
                            log.warn(e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }

    /**
     * 2024-08-16 20:01
     * 与最近一次同类型的文件md5值
     *
     * @param dataFile 清洗文件
     * @param map      告警消息
     * @return true 一致
     */
    private boolean md5Check(MarketingCleanDataFile dataFile, JSONObject map) {
        String fileName = dataFile.getFileName();
        String apiCode = dataFile.getApiCode();
        String md5Value = dataFile.getMd5Value();
        String[] split = fileName.split("\\.");
        MarketingCleanDataFileExample fileExampleCount = new MarketingCleanDataFileExample();
        fileExampleCount.createCriteria().andApiCodeEqualTo(apiCode).andSyncConfigIdEqualTo(dataFile.getSyncConfigId())
                .andIdNotEqualTo(dataFile.getId()).andCreateTimeLessThanOrEqualTo(dataFile.getCreateTime())
                .andMd5ValueNotEqualTo("").andMd5ValueIsNotNull().andFileNameLike("%" + split[1]);
        fileExampleCount.setOrderByClause("create_time desc limit 1");
        List<MarketingCleanDataFile> marketingCleanDataFiles = marketingCleanDataFileMapper.selectByExample(fileExampleCount);
        if (marketingCleanDataFiles.size() > 0 && dataFile.getMd5Value().equals(marketingCleanDataFiles.get(0).getMd5Value())) {
            MarketingCleanDataFile dataFileOld = marketingCleanDataFiles.get(0);
            dingDingRobotHookService.sendDingDingTextMessage(
                    "榕树上传数据更新-" + apiCode + "文件：" + fileName + "与最近("
                            + dataFileOld.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                            .toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ")的文件"
                            + dataFileOld.getFileName() + "内容重复，本次文件不进行清洗\n文件MD5值：" + md5Value, map);
            updateDataFile(dataFile, false);
            return true;
        }
        return false;
    }

    /**
     * 2024-08-16 20:03
     * 停止运行任务及更新线程池线程大小
     */
    private void stopCheckOrUpdate(String apiCode) throws Exception {
        List<Integer> rongShuCleanUploadTreadPoolSize = marketingCommonConfig.getRongShuCleanUploadTreadPoolSize();
        if (rongShuCleanUploadTreadPoolSize.size() == 0) {
            throw new Exception(apiCode + "榕树上传停止清洗");
        }
        int size = rongShuCleanUploadTreadPoolSize.size();
        if (size == 2) {
            Integer corePoolSizeNew = rongShuCleanUploadTreadPoolSize.get(0);
            int corePoolSize = THREAD_POOL.getCorePoolSize();
            if (corePoolSizeNew > 0 && corePoolSizeNew != corePoolSize) {
                THREAD_POOL.setCorePoolSize(corePoolSizeNew);
            }
            Integer maximumPoolSizeNew = rongShuCleanUploadTreadPoolSize.get(1);
            int maximumPoolSize = THREAD_POOL.getMaximumPoolSize();
            if (maximumPoolSizeNew >= corePoolSize && maximumPoolSizeNew != maximumPoolSize) {
                THREAD_POOL.setMaximumPoolSize(maximumPoolSizeNew);
            }
        } else if (size == 1) {
            Integer corePoolSizeNew = rongShuCleanUploadTreadPoolSize.get(0);
            int corePoolSize = THREAD_POOL.getCorePoolSize();
            if (corePoolSizeNew > 0 && corePoolSizeNew != corePoolSize) {
                THREAD_POOL.setCorePoolSize(corePoolSizeNew);
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
    private long readFile(MarketingCleanDataFile dataFile, File file, String regex, LocalDate localDate, boolean isCreate) {
        String name = file.getName();
        String apiCode = dataFile.getApiCode();
        Set<String> appletDateSet = iMarketingDataValidService.getAppletDateSet(apiCode, localDate.toString());
        MarketingCleanDataFile dataFileNew = null;
        Map<String, JSONObject> map = new HashMap<>(2048);
        long rowNum = 0L;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            String rowData;
            while ((rowData = reader.readLine()) != null) {
                md5.update(rowData.getBytes(StandardCharsets.UTF_8));
                stopCheckOrUpdate(apiCode);
                if (rowNum == 0) {
                    rowNum++;
                    if (isCreate) {
                        dataFileNew = saveDataFileInfo(dataFile, name, file.getParent(), rowData);
                    } else {
                        dataFileNew = dataFile;
                        dataFileNew.setFileData(rowData);
                        dataFileNew.setFileHeader(String.join(",", FILE_HEADER));
                    }
                    continue;
                }
                String[] split = rowData.split(regex);
                String uid = split[0];
                JSONObject object = new JSONObject();
                int size = FILE_HEADER.size();
                for (int i = 1; i < size; i++) {
                    object.put(FILE_HEADER.get(i), split[i]);
                }
                map.put(uid, object);
                if (map.size() == 2000 && dataFileNew.getId() != null) {
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
            if (dataFileNew != null && dataFileNew.getId() != null && map.size() != 0) {
                update(apiCode, map, appletDateSet, dataFileNew);
                dataFileNew.setMd5Value(DatatypeConverter.printHexBinary(md5.digest()));
            }
            updateDataFile(dataFileNew, isCreate);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_USUAL_NOTICE.getCode(), e.getMessage()
                    + "\n已清洗:" + (rowNum), "榕树清洗上传数据异常-" + apiCode), e);
            updateDataFile(dataFileNew, isCreate);
            return -1L;
        }
        return rowNum;
    }

    /**
     * 2024-08-12 23:24
     * 更新文件信息，设置已失效
     */
    private void updateDataFile(MarketingCleanDataFile dataFileNew, boolean isCreate) {
        if (dataFileNew != null && dataFileNew.getId() != null) {
            MarketingCleanDataFile dataFileUpdate = new MarketingCleanDataFile();
            dataFileUpdate.setId(dataFileNew.getId());
            dataFileUpdate.setMd5Value(dataFileNew.getMd5Value());
            dataFileUpdate.setIsDel(9);
            if (isCreate) {
                marketingCleanDataFileMapper.updateByPrimaryKeySelective(dataFileUpdate);
                return;
            }
            dataFileUpdate.setFileData(dataFileNew.getFileData());
            dataFileUpdate.setFileHeader(dataFileNew.getFileHeader());
            marketingCleanDataFileMapper.updateByPrimaryKeySelective(dataFileUpdate);
        }
    }


    /**
     * 2024-08-08 22:35
     * 保存文件信息
     *
     * @param fileName   文件名
     * @param targetPath 目标目录
     */
    private MarketingCleanDataFile saveDataFileInfo(MarketingCleanDataFile dataFile, String fileName
            , String targetPath
            , String fileData) {
        String apiCode = dataFile.getApiCode();
        Long syncConfigId = dataFile.getSyncConfigId();
        String localPath = dataFile.getLocalPath();
        MarketingCleanDataFile dataFileNew = new MarketingCleanDataFile();
        dataFileNew.setFileHeader(String.join(",", FILE_HEADER));
        dataFileNew.setFileName(fileName);
        dataFileNew.setApiCode(apiCode);
        dataFileNew.setFileData(fileData);
        dataFileNew.setCreateTime(new Date());
        dataFileNew.setLocalPath(targetPath);
        dataFileNew.setUpdateTime(new Date());
        dataFileNew.setTargetSftpPath(localPath);
        dataFileNew.setMd5Value("");
        dataFileNew.setSyncConfigId(syncConfigId);
        int i = marketingCleanDataFileMapper.insertSelective(dataFileNew);
        if (i < 1) {
            log.warn("添加清洗文件失败！fileName:{},targetPath:{},srcPath:{},syncConfigId:{}"
                    , fileName, targetPath, localPath, syncConfigId);
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
            JSONObject newData = map.get(syncUser.getCustNum());
            cleanLog.setNewDataJson(newData.toJSONString());
            if (JSONObject.isValidObject(reserveField1)) {
                JSONObject oldData = JSONObject.parseObject(reserveField1);
                newData.forEach((String key, Object value) -> oldData.put(key, value.toString()));
                syncUser.setReserveField1(oldData.toJSONString());
                marketingSyncUserMapper.updateReserveFieldByPrimaryKey(syncUser);
                cleanLog.setIsSuccess(0);
            } else if (StringUtils.isBlank(reserveField1)) {
                syncUser.setReserveField1(newData.toJSONString());
                marketingSyncUserMapper.updateReserveFieldByPrimaryKey(syncUser);
            }
            rongshuPaofenFileUpdateSyncCleanLogMapper.insertSelective(cleanLog);
        }
    }

}
