package com.br.marketing.bridge.job.clean;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Md5Utils;
import com.br.common.encryption.Sha256Util;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.file.ZipUtils;
import com.br.marketing.common.validators.user.UserValidator;
import com.br.marketing.entity.MarketingCleanDataFile;
import com.br.marketing.entity.MarketingCleanDataFileExample;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.clean.MarketingCleanCreateTaskRule;
import com.br.marketing.entity.clean.MarketingCleanCreateTaskRuleExample;
import com.br.marketing.entity.clean.rongshu.RongshuPaofenFileUpdateSyncCleanLog;
import com.br.marketing.entity.clean.rongshu.RongshuPaofenFileUpdateSyncCleanLogExample;
import com.br.marketing.enums.DingDingAlarmFunctionEnum;
import com.br.marketing.mapper.MarketingCleanDataFileMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.clean.MarketingCleanCreateTaskRuleMapper;
import com.br.marketing.mapper.clean.rongshu.RongshuPaofenFileUpdateSyncCleanLogMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rpcclient.rpcclientImpl.DecodeGrpcClient;
import com.br.marketing.service.IMarketingDataValidService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
        String regex = marketingCommonConfig.getRongShuCleanUploadConfig().getOrDefault("regex", "\t");

        // b_marketing_clean_create_task_rule 数据清洗自动生成任务规则
        MarketingCleanCreateTaskRuleExample example = new MarketingCleanCreateTaskRuleExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo(0).andDataTypeEqualTo(1);
        List<MarketingCleanCreateTaskRule> taskRules = marketingCleanCreateTaskRuleMapper.selectByExample(example);

        for (MarketingCleanCreateTaskRule taskRule : taskRules) {
            if (1 == taskRule.getTaskCreateRule() && 1 == taskRule.getDataType()) {
                String startTime = taskRule.getStartTime();
                // TODO time.isBefore(LocalTime.parse(startTime))
                if (StringUtils.isEmpty(startTime) || time.isBefore(LocalTime.parse(startTime))) {
                    Long syncConfigId = taskRule.getSyncConfigId();

                    // b_marketing_clean_data_file
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
                            // md5Check
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
                                            long l = readFile(dataFile, file, regex, localDate, true, map);
                                            if (l < 0) {
                                                bool = false;
                                            } else {
                                                sum += l;
                                            }
                                        }
                                        updateDataFile(dataFile, false);
                                    }
                                } else {
                                    long l = readFile(dataFile, srcFile, regex, localDate, false, map);
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
                        sendMessage(dataFile, localDateTime, bool, fileName, sum, map);
                    }
                }
            }
        }
    }

    /**
     * 2024-11-12 16:34
     * 发送消息
     */
    private void sendMessage(MarketingCleanDataFile dataFile, LocalDateTime localDateTime, boolean bool
            , String fileName, long sum, JSONObject map) {
        try {
            int i = countCleanSuccess(dataFile, localDateTime);
            dingDingRobotHookService.sendDingDingTextMessage("榕树上传数据更新-" + dataFile.getApiCode() + "结束["
                    + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    + "]\n文件：" + fileName + "\n清洗" + (bool ? ("成功^_^\n文件量级："
                    + sum + "\n清洗成功量级：" + i) : "失败!!!")
                    + (sum < 1 ? "\n文件无内容" : ""), map);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    /**
     * 2024-11-12 15:55
     * 查询清洗成功量级
     */
    private int countCleanSuccess(MarketingCleanDataFile dataFile, LocalDateTime localDateTime) {
        int i = 0;
        while (THREAD_POOL.getTaskCount() != THREAD_POOL.getCompletedTaskCount() && i < 5) {
            log.warn("等待清洗任务结束，任务总数：{}，执行任务数：{}", THREAD_POOL.getTaskCount(), THREAD_POOL.getCompletedTaskCount());
            try {
                TimeUnit.SECONDS.sleep(60);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            i++;
        }
        try {
            MarketingCleanDataFileExample sumExample = new MarketingCleanDataFileExample();
            sumExample.createCriteria().andApiCodeEqualTo(dataFile.getApiCode()).andSyncConfigIdEqualTo(dataFile.getSyncConfigId())
                    .andTargetSftpPathEqualTo(dataFile.getLocalPath()).andLocalPathLike("%" + dataFile.getFileName() + "%");
            List<MarketingCleanDataFile> sumCleanDataFiles = marketingCleanDataFileMapper.selectByExample(sumExample);
            List<Long> ids = sumCleanDataFiles.stream().map(MarketingCleanDataFile::getId).collect(Collectors.toList());
            ids.add(dataFile.getId());
            RongshuPaofenFileUpdateSyncCleanLogExample logExample = new RongshuPaofenFileUpdateSyncCleanLogExample();
            logExample.createCriteria().andApiCodeEqualTo(dataFile.getApiCode()).andMarketingCleanDataFileIdIn(ids).andIsDelEqualTo(0)
                    .andIsSuccessEqualTo(0).andCreateTimeGreaterThanOrEqualTo(
                    Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
            return rongshuPaofenFileUpdateSyncCleanLogMapper.countByExample(logExample);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return 0;
    }

    /**
     * 2024-08-16 20:01
     * 与最近一次同类型的文件md5值
     *
     * @param dataFile 清洗文件
     * @param map      告警消息
     * @return true 一致
     */
    boolean md5Check(MarketingCleanDataFile dataFile, JSONObject map) {
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
     * 2024-08-19 20:12
     * 检查压缩包的明细文件MD5值
     *
     * @return true 一致
     */
    private boolean md5CheckDetailsFile(File file, MarketingCleanDataFile dataFile, JSONObject map) throws IOException
            , NoSuchAlgorithmException {
        // 设置分段大小1M
        int segmentSize = 1024 << 10;
        FileChannel channel = FileChannel.open(file.toPath());
        long size = channel.size();
        long segments = size / segmentSize + 1;
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        for (int i = 0; i < segments; i++) {
            int offset = i * segmentSize;
            int remaining = (int) Math.min(segmentSize, size - offset);
            MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, offset, remaining);
            md5.update(byteBuffer);
        }
        String fileMd5 = DatatypeConverter.printHexBinary(md5.digest());
        String fileName = file.getName();
        String[] split = fileName.split("\\.");
        MarketingCleanDataFileExample fileExampleCount = new MarketingCleanDataFileExample();
        fileExampleCount.createCriteria().andApiCodeEqualTo(dataFile.getApiCode()).andIdNotEqualTo(dataFile.getId())
                .andSyncConfigIdEqualTo(dataFile.getSyncConfigId()).andFileNameLike("%" + split[1]);
        fileExampleCount.setOrderByClause("create_time desc limit 1");
        List<MarketingCleanDataFile> marketingCleanDataFiles = marketingCleanDataFileMapper.selectByExample(fileExampleCount);
        dataFile.setMd5Value(fileMd5);
        if (marketingCleanDataFiles.size() > 0 && fileMd5.equals(marketingCleanDataFiles.get(0).getMd5Value())) {
            MarketingCleanDataFile dataFileOld = marketingCleanDataFiles.get(0);
            dingDingRobotHookService.sendDingDingTextMessage(
                    "榕树上传数据更新-" + dataFile.getApiCode() + "文件：" + fileName + "与最近("
                            + dataFileOld.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                            .toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ")的文件"
                            + dataFileOld.getFileName() + "内容重复，本次文件不进行清洗\n文件MD5值：" + fileMd5, map);
            return true;
        }
        return false;
    }

    /**
     * 2024-08-16 20:03
     * 停止运行任务及更新线程池线程大小
     */
    private void stopCheckOrUpdate(String apiCode) throws Exception {
        String[] poolSizes = marketingCommonConfig.getRongShuCleanUploadConfig()
                .getOrDefault("poolSize", "2,5").split(",");
        int size = poolSizes.length;
        if (size == 0 || "0".equals(poolSizes[0])) {
            throw new Exception(apiCode + "榕树上传停止清洗");
        }
        if (size == 2) {
            int corePoolSizeNew = Integer.parseInt(poolSizes[0]);
            int corePoolSize = THREAD_POOL.getCorePoolSize();
            if (corePoolSizeNew > 0 && corePoolSizeNew != corePoolSize) {
                ThreadPoolAdjustmentUtil.adjustThreadPoolSize(THREAD_POOL, corePoolSizeNew);
            }
            int maximumPoolSizeNew = Integer.parseInt(poolSizes[1]);
            int maximumPoolSize = THREAD_POOL.getMaximumPoolSize();
            if (maximumPoolSizeNew >= corePoolSize && maximumPoolSizeNew != maximumPoolSize) {
                ThreadPoolAdjustmentUtil.adjustThreadPoolSize(THREAD_POOL, maximumPoolSizeNew);
            }
        } else if (size == 1) {
            int corePoolSizeNew = Integer.parseInt(poolSizes[0]);
            int corePoolSize = THREAD_POOL.getCorePoolSize();
            if (corePoolSizeNew > 0 && corePoolSizeNew != corePoolSize) {
                ThreadPoolAdjustmentUtil.adjustThreadPoolSize(THREAD_POOL, corePoolSizeNew);
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
    private long readFile(MarketingCleanDataFile dataFile, File file, String regex, LocalDate localDate
            , boolean isCreate, JSONObject dingdingMap) {
        String[] fileHeaders = marketingCommonConfig.getRongShuCleanUploadConfig()
                .getOrDefault("fileHeader", "").split(",");
        List<String> fileHeader = fileHeaders.length == 0 ? FILE_HEADER : Arrays.asList(fileHeaders);
        String name = file.getName();
        String apiCode = dataFile.getApiCode();
        // appletDateSet
        Set<String> appletDateSet = iMarketingDataValidService.getAppletDateSet(apiCode, localDate.toString());
        MarketingCleanDataFile dataFileNew = null;
        Map<String, JSONObject> fileDataMap = new HashMap<>(2048);

        long rowNum = 0L;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String rowData;
            while ((rowData = reader.readLine()) != null) {
                if(StringUtils.isBlank(rowData)){
                    continue;
                }
                stopCheckOrUpdate(apiCode);
                if (rowNum == 0) {
                    boolean b = md5CheckDetailsFile(file, dataFile, dingdingMap);
                    if (isCreate) {
                        dataFileNew = saveDataFileInfo(dataFile, name, file.getParent(), rowData, fileHeader);
                    } else {
                        dataFileNew = dataFile;
                        dataFileNew.setFileData(rowData);
                        dataFileNew.setFileHeader(String.join(",", fileHeader));
                    }
                    if (b) {
                        break;
                    }
                }
                String[] split = rowData.split(regex);
                JSONObject fileDataJo = new JSONObject();
                int size = fileHeader.size();
                for (int i = 0; i < size; i++) {
                    fileDataJo.put(fileHeader.get(i), split[i]);
                }
                String uid = fileDataJo.getString("uid");
                fileDataMap.put(uid, fileDataJo);
                if (fileDataMap.size() == 2000 && dataFileNew.getId() != null) {
                    Map<String, JSONObject> finalFileDataMap = fileDataMap;
                    MarketingCleanDataFile finalDataFileNew = dataFileNew;
                    THREAD_POOL.submit(() -> {
                        update(apiCode, finalFileDataMap, appletDateSet, finalDataFileNew);
                        finalFileDataMap.clear();
                    });
                    fileDataMap = new HashMap<>(2048);
                }
                rowNum++;
            }
            // TODO
            if (dataFileNew != null && dataFileNew.getId() != null && fileDataMap.size() != 0) {
                update(apiCode, fileDataMap, appletDateSet, dataFileNew);
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
            , String fileData
            , List<String> fileHeader) {
        String apiCode = dataFile.getApiCode();
        Long syncConfigId = dataFile.getSyncConfigId();
        String localPath = dataFile.getLocalPath();
        MarketingCleanDataFile dataFileNew = new MarketingCleanDataFile();
        dataFileNew.setFileHeader(String.join(",", fileHeader));
        dataFileNew.setFileName(fileName);
        dataFileNew.setApiCode(apiCode);
        dataFileNew.setFileData(fileData);
        dataFileNew.setCreateTime(new Date());
        dataFileNew.setLocalPath(targetPath);
        dataFileNew.setUpdateTime(new Date());
        dataFileNew.setTargetSftpPath(localPath);
        dataFileNew.setMd5Value(dataFile.getMd5Value());
        dataFileNew.setSyncConfigId(syncConfigId);
        dataFileNew.setIsDel(dataFile.getIsDel());
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
    private void update(String apiCode, Map<String, JSONObject> fileDataMap, Set<String> appletDateSet
            , MarketingCleanDataFile dataFileNew) {
        Map<String, String> commonFieldMap = marketingCommonConfig.getRongShuCleanUploadCommonFieldMap();
        Map<String, String> extendFieldMap = marketingCommonConfig.getRongShuCleanUploadExtendFieldMap();
        Map<String, Map<String, String>> cipherMap = marketingCommonConfig.getRongShuCleanUploadCipherMap();

        List<MarketingSyncUser> list = marketingSyncUserMapper
                .getReserveFieldByCustNumAndAppletDateList(apiCode, fileDataMap.keySet(), appletDateSet);
        // idSet, syncIds
        Set<Long> idSet = list.stream().map(MarketingSyncUser::getId).collect(Collectors.toSet());
        Set<Long> syncIds = new HashSet<>();
        if (idSet.size() > 0) {
            syncIds.addAll(rongshuPaofenFileUpdateSyncCleanLogMapper.getSyncApicodeId(apiCode, dataFileNew.getId(), idSet));
        }
        //
        for (MarketingSyncUser syncUser : list) {
            syncUser.setApiCode(apiCode);
            Long id = syncUser.getId();
            if (syncIds.contains(id)) {
                continue;
            }
            Map<String, Object> syncUserMap = BeanUtil.beanToMap(syncUser);
            JSONObject oldDataJson = new JSONObject();
            JSONObject newDataJson = new JSONObject();
            String oldReserveField1 = syncUser.getReserveField1();
            JSONObject fileDataJo = fileDataMap.get(syncUser.getCustNum());
            List<Map<String, String>> fieldItemList = new ArrayList<>();

            // commonField
            if (!CollectionUtils.isEmpty(commonFieldMap)) {
                commonFieldMap.forEach((String key, String value) -> {
                    Map<String, String> fieldItemMap = new HashMap<>();
                    if(cipherMap.containsKey(key)){
                        fieldItemMap = cipherField(key, fileDataJo.getString(key), cipherMap.get(key));
                    } else {
                        fieldItemMap.put(value, fileDataJo.getString(key));
                    }
                    if(!CollectionUtils.isEmpty(fieldItemMap)){
                        fieldItemMap.forEach((String fieldItemKey, String fieldItemValue) -> {
                            Map<String, String> fieldMap = new HashMap<>();
                            fieldMap.put(fieldItemKey, fieldItemValue);
                            fieldItemList.add(fieldMap);
                        });
                        oldDataJson.put(value, syncUserMap.get(value));
                        newDataJson.putAll(fieldItemMap);
                    }
                });
            }

            // extendField
            if (!CollectionUtils.isEmpty(extendFieldMap)) {
                JSONObject extendDataJo = new JSONObject();
                extendFieldMap.forEach((String key, String value) -> extendDataJo.put(key, fileDataJo.getString(key)));

                if (StringUtils.isBlank(oldReserveField1)) {
                    Map<String, String> fieldItemMap = new HashMap<>();
                    fieldItemMap.put("reserve_field1", extendDataJo.toJSONString());
                    fieldItemList.add(fieldItemMap);
                    //
                    oldDataJson.put("reserve_field1", "");
                    newDataJson.put("reserve_field1", extendDataJo.toJSONString());
                }
                if (JSONObject.isValidObject(oldReserveField1)) {
                    JSONObject oldReserveField1Jo = JSONObject.parseObject(oldReserveField1);
                    extendFieldMap.forEach((String key, String value) -> oldReserveField1Jo.put(value, extendDataJo.getString(key)));
                    Map<String, String> fieldItemMap = new HashMap<>();
                    fieldItemMap.put("reserve_field1", oldReserveField1Jo.toJSONString());
                    fieldItemList.add(fieldItemMap);
                    //
                    oldDataJson.put("reserve_field1", oldReserveField1);
                    newDataJson.put("reserve_field1", oldReserveField1Jo.toJSONString());
                }
            }

            if (!CollectionUtils.isEmpty(fieldItemList)) {
                marketingSyncUserMapper.cleanUpdateById(apiCode, syncUser.getId(), fieldItemList);

                RongshuPaofenFileUpdateSyncCleanLog cleanLog = new RongshuPaofenFileUpdateSyncCleanLog();
                cleanLog.setApiCode(apiCode);
                cleanLog.setHistoryDataJson(oldDataJson.toJSONString());
                cleanLog.setSyncApicodeId(id);
                cleanLog.setMarketingCleanDataFileId(dataFileNew.getId());
                cleanLog.setCreateTime(new Date());
                cleanLog.setUid(syncUser.getCustNum());
                cleanLog.setUpdateTime(cleanLog.getUpdateTime());
                cleanLog.setNewDataJson(newDataJson.toJSONString());
                cleanLog.setIsSuccess(0);
                rongshuPaofenFileUpdateSyncCleanLogMapper.insertSelective(cleanLog);
            }
        }
    }

    private Map<String, String> cipherField(String fileDataKey, String fileDataValue, Map<String, String> cipherConfig) {
        Map<String, String> resMap= new HashMap<>();

        String content = StringUtils.isBlank(fileDataValue) ? "" : fileDataValue;
        String decryptType = cipherConfig.get("decryptType");

        switch (decryptType){
            case "md5":
                if(!DecodeGrpcClient.isMd5(content)){
                    return resMap;
                }
                content = RpcClientProxy.decode(content, fileDataKey, "md5", "");
                break;
            case "sha256":
                if(content.length() != 64){
                    return resMap;
                }
                content = RpcClientProxy.decode(content, fileDataKey, "sha", "");
                break;
        }

        if (StringUtils.isBlank(content)) {
            return resMap;
        }

        UserValidator userValidator = new UserValidator(0);

        switch (fileDataKey){
            case "cell":
                if (!userValidator.validatePhone(content)) {
                    return resMap;
                }
                resMap.put("cell_md5", Md5Utils.cell32(content));
                resMap.put("cell_sha256", Sha256Util.getSHA256Encrypt(content));
                resMap.put("cell", BrCipherMaker.getInstance().encode(content));
                return resMap;
            case "id":
                if (!userValidator.validateId(content)) {
                    return resMap;
                }
                resMap.put("id_card", BrCipherMaker.getInstance().encode(content));
                return resMap;
            case "name":
                if (!userValidator.validateName(content)) {
                    return resMap;
                }
                resMap.put("name", BrCipherMaker.getInstance().encode(content));
                return resMap;
        }
        return resMap;
    }

}
