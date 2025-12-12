package com.br.marketing.monkey.service.zhongbang.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.SftpClient;
import com.br.marketing.client.zbank.ZbankClient;
import com.br.marketing.client.zbank.ZbankResponse;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.dto.zbank.ZbankAIVoiceFileDetailResultDTO;
import com.br.marketing.entity.*;
import com.br.marketing.entity.zhongbang.ZhongbangAiVoiceFileDetail;
import com.br.marketing.entity.zhongbang.ZhongbangAiVoiceFileDetailExample;
import com.br.marketing.mapper.*;
import com.br.marketing.mapper.zhongbang.ZhongbangAiVoiceFileDetailMapper;
import com.br.marketing.monkey.enums.zhongbangai.CallRecordStatusEnum;
import com.br.marketing.monkey.enums.zhongbangai.PushFileStatusEnum;
import com.br.marketing.monkey.service.zhongbang.ZhongBangAIVoiceService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.jcraft.jsch.SftpException;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import com.zbank.file.bean.UploadInfo;
import com.zbank.file.common.utils.Md5EncodeUtil;
import com.zbank.file.exception.SDKException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 众邦AI录音文件Service
 *
 * @author zhen.Li1
 * @dateTime 2025/11/20 20:13
 */
@Component
@Slf4j
public class ZhongBangAIVoiceServiceImpl implements ZhongBangAIVoiceService {


    @Autowired
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private FileDbConfigMapper fileDbConfigMapper;

    @Resource
    private SyncConfigMapper syncConfigMapper;

    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    private ZbankClient zBankClient;


    @Resource
    private PushCustomerFileInfoMapper pushCustomerFileInfoMapper;


    @Resource
    private ZhongbangAiVoiceFileDetailMapper zhongbangAiVoiceFileDetailMapper;

    @Autowired
    private CallRecordingMapper callRecordingMapper;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;


    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;
    @Resource
    private TrackingService trackingService;


    @Override
    public boolean voiceAIFileUpload(String apiCode, String cId, LocalDate localDate) {

        boolean resultBool = true;
        int pageSize = 2000;
        int availableNumber = Runtime.getRuntime().availableProcessors();
        boolean bool = availableNumber > 25;
        int corePoolSize = (availableNumber / 2);
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(bool ? 15 : corePoolSize, bool ? 25
                : (availableNumber + corePoolSize), new SynchronousQueue<>(), "br-zbankAI-voiceFile-upload");
        ThreadPoolExecutor threadPoolGet = BrExecutors.getThreadPool(corePoolSize, availableNumber
                , new SynchronousQueue<>(), "br-zbankAI-voiceFile-sftp-get");
        Map<String, JSONObject> zhongBangVoiceFileConfig = getVoiceAIFileConfig();
        ZonedDateTime zonedDateTime = localDate.atStartOfDay().atZone(ZoneId.systemDefault());
        Date startDate = Date.from(zonedDateTime.toInstant());
        Date endDate = Date.from(zonedDateTime.plusDays(1).toInstant());
        String dateStr = localDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        Set<Map.Entry<String, JSONObject>> entrySet = zhongBangVoiceFileConfig.entrySet();
        // 遍历文件配置信息
        for (Map.Entry<String, JSONObject> entry : entrySet) {
            JSONObject entryValue = entry.getValue();
            String fileType = entryValue.getString("fileType");
            String tableName = entry.getKey();
            List<FileDbConfig> fileDbConfigs = getFileDbConfig(apiCode, fileType);
            // 文件服务信息遍历
            for (FileDbConfig fileDbConfig : fileDbConfigs) {
                Long sftpConfigId = fileDbConfig.getSftpConfigId();
                SyncConfig syncConfig = syncConfigMapper.selectByPrimaryKey(sftpConfigId);
                List<LocalFile> localFiles = getLocalFile(apiCode, fileType, startDate, endDate);
                resultBool = localFiles.size() > 0 && resultBool;
                // 明细文件信息
                for (LocalFile localFile : localFiles) {
                    String fileName = localFile.getFileName();
                    Long localFileId = localFile.getId();
                    int fileDetailsCount;
                    int fileInfoCount;
                    PushCustomerFileInfoExample exampleInfoCount = new PushCustomerFileInfoExample();
                    exampleInfoCount.createCriteria().andApiCodeEqualTo(apiCode).andCidEqualTo(cId)
                            .andStatusEqualTo(1).andPushStatusIn(Arrays.asList(PushFileStatusEnum.READY.getCode(),
                                    PushFileStatusEnum.PUSH_ERROR.getCode())).andLocalFileIdEqualTo(localFileId);
                    fileInfoCount = pushCustomerFileInfoMapper.countByExample(exampleInfoCount);
                    ZhongbangAiVoiceFileDetailExample exampleDetailCount = new ZhongbangAiVoiceFileDetailExample();
                    exampleDetailCount.createCriteria().andLocalIdEqualTo(localFileId).andStatusEqualTo(1)
                            .andApiCodeEqualTo(apiCode).andPushStatusEqualTo(PushFileStatusEnum.READY.getCode()).andIsDeletedEqualTo(0);
                    fileDetailsCount = zhongbangAiVoiceFileDetailMapper.countByExample(exampleDetailCount);
                    if (fileInfoCount != fileDetailsCount) {
                        // 下载远程文件
                        resultBool = isFromSftpLocalDisk(localFile, syncConfig, dateStr, apiCode, cId
                                , pageSize, threadPoolGet, tableName) && resultBool;
                    }
                    fileInfoCount = pushCustomerFileInfoMapper.countByExample(exampleInfoCount);
                    if (fileInfoCount == fileDetailsCount) {
                        if (fileDetailsCount > 0) {
                            // 上传开始时间记录
                            updateLocalFilePushTime(localFileId);
                            // 文件上传
                            List<CompletableFuture<Boolean>> futures = uploadFile(
                                    cId, apiCode, pageSize, tableName, threadPool, localFile);
                            // 结果转换
                            resultBool = allOf(futures) && resultBool;
                        } else {
                            ZhongbangAiVoiceFileDetailExample countExample = new ZhongbangAiVoiceFileDetailExample();
                            exampleDetailCount.createCriteria().andLocalIdEqualTo(localFileId).andApiCodeEqualTo(apiCode)
                                    .andStatusEqualTo(1).andIsDeletedEqualTo(0);
                            fileDetailsCount = zhongbangAiVoiceFileDetailMapper.countByExample(countExample);
                            resultBool = fileDetailsCount > 0;
                        }
                    } else {
                        String msg = "众邦AI录音文件量级与明细量级不匹配，录音文件量级:" + fileInfoCount
                                + ",明细量级:" + fileDetailsCount + ",明细文件：" + fileName;
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_USUAL_NOTICE.getCode(), msg
                                , "众邦AI录音文件量级与明细量级不匹配"));
                        resultBool = false;
                    }
                    //更新local_file
                    if (resultBool) {
                        localFile.setPushStatus(String.valueOf(PushFileStatusEnum.RUNNING.getCode()));
                        localFileMapper.updateByPrimaryKeySelective(localFile);
                    }

                    try {
                        if(resultBool){
                            String remark = "众邦AI录音文件量级与明细量级不匹配，录音文件量级:" + fileInfoCount
                                    + ",明细量级:" + fileDetailsCount + ",明细文件：" + fileName;
                            trackingService.trackPointLog(DataFlowDirection.OUT
                                    , apiCode
                                    , "众邦AI上传录音文件"
                                    , Long.valueOf(fileDetailsCount)
                                    , remark
                                    , TrackingContext.generateBatchId());
                        }
                    } catch (Exception ex) {
                        log.warn(
                                AlertLog.buildWarnMessage(
                                        AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                        , ex.getMessage()
                                        , "埋点异常")
                                , ex);
                    }

                }
            }
        }
        return resultBool;
    }


    private Map<String, JSONObject> getVoiceAIFileConfig() {
        Map<String, JSONObject> zhongBangVoiceFileConfig = marketingCommonConfig.getZhongBangAIVoiceFileConfig();
        if (zhongBangVoiceFileConfig.isEmpty()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fileType", "zhongbang_ai_voice");
            jsonObject.put("uploadPoolSize", 5);
            jsonObject.put("getFilePoolSize", 5);
            zhongBangVoiceFileConfig.put("aiVoicePush", jsonObject);
        } else {
            JSONObject jsonObject = zhongBangVoiceFileConfig.get("aiVoicePush");
            if (jsonObject == null) {
                JSONObject jo = new JSONObject();
                jo.put("fileType", "zhongbang_ai_voice");
                jo.put("uploadPoolSize", 1);
                jo.put("getFilePoolSize", 5);
                zhongBangVoiceFileConfig.put("aiVoicePush", jo);
            } else {
                if (!jsonObject.containsKey("fileType")) {
                    jsonObject.put("fileType", "zhongbang_ai_voice");
                }
                if (!jsonObject.containsKey("uploadPoolSize")) {
                    jsonObject.put("uploadPoolSize", Runtime.getRuntime().availableProcessors());
                }
                if (!jsonObject.containsKey("getFilePoolSize")) {
                    jsonObject.put("getFilePoolSize", Runtime.getRuntime().availableProcessors());
                }
            }
        }
        return zhongBangVoiceFileConfig;
    }

    private List<FileDbConfig> getFileDbConfig(String apiCode, String fileType) {
        FileDbConfigExample fileDbConfigExample = new FileDbConfigExample();
        fileDbConfigExample.createCriteria().andApiCodeEqualTo(apiCode)
                .andDelEqualTo(Constants.DATA_VALID).andFileTypeEqualTo(fileType);
        return fileDbConfigMapper.selectByExample(fileDbConfigExample);
    }

    private List<LocalFile> getLocalFile(String apiCode, String fileType, Date startDate, Date endDate) {
        LocalFileExample localFileExample = new LocalFileExample();
        localFileExample.createCriteria().andStatusEqualTo("2").andCompleteEqualTo("1")
                .andApiCodeEqualTo(apiCode).andFileTypeEqualTo(fileType)
                .andCreateTimeGreaterThanOrEqualTo(startDate).andCreateTimeLessThan(endDate)
                .andActualNumberGreaterThan(0);
        return localFileMapper.selectByExample(localFileExample);
    }

    /**
     * 2025-11-20 19:59
     * 下载远程文件到本地磁盘及文件信息保存
     */
    private boolean isFromSftpLocalDisk(LocalFile localFile, SyncConfig syncConfig, String dateStr
            , String apiCode, String cid, Integer pageSize, ThreadPoolExecutor threadPoolGet
            , final String tableName) {
        long localFileId = localFile.getId();
        String fileName = localFile.getFileName();
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        String localPath = localFile.getLocalPath();
        String localDir = localPath.replaceAll(DateUtils.yyyyMMdd, dateStr).concat(fileName.replace(".txt", ""))
                .concat(File.separator).concat("voice_" + localFileId).concat(File.separator);
        String srcPath = syncConfig.getSrcPath().replaceAll(DateUtils.yyyyMMdd, dateStr);
        long maxId = 0;
        while (!Thread.currentThread().isInterrupted()) {
            ZhongbangAiVoiceFileDetailExample voiceFileDetailExample = new ZhongbangAiVoiceFileDetailExample();
            voiceFileDetailExample.createCriteria().andLocalIdEqualTo(localFileId).andStatusEqualTo(1)
                    .andApiCodeEqualTo(apiCode).andPushStatusEqualTo(0).andIsDeletedEqualTo(0)
                    .andIdGreaterThan(maxId);
            voiceFileDetailExample.setOrderByClause("id limit " + pageSize);
            List<ZhongbangAiVoiceFileDetail> fileDetails = zhongbangAiVoiceFileDetailMapper
                    .selectByExample(voiceFileDetailExample);
            if (fileDetails.isEmpty()) {
                break;
            }
            int size = fileDetails.size();
            maxId = fileDetails.get(size - 1).getId();
            setThreadPool(tableName, "getFilePoolSize", threadPoolGet);
            futures.add(CompletableFuture.supplyAsync(() -> {
                boolean bool = true;
                // 获得sftp连接
                SftpClient ftpClient = new SftpClient(syncConfig, true);
                try {
                    if (ftpClient.connect() && ftpClient.isConnected()) {
                        List<String> fileNameList = fileDetails.stream().map(ZhongbangAiVoiceFileDetail::getFileName)
                                .collect(Collectors.toList());
                        PushCustomerFileInfoExample example = new PushCustomerFileInfoExample();
                        example.createCriteria().andApiCodeEqualTo(apiCode)
                                .andCidEqualTo(cid).andFileNameIn(fileNameList).andLocalFileIdEqualTo(localFileId);
                        List<PushCustomerFileInfo> infoList = pushCustomerFileInfoMapper.selectByExample(example);
                        Map<String, PushCustomerFileInfo> fileInfoMap = infoList.stream().collect(Collectors.toMap(
                                PushCustomerFileInfo::getFileName, Function.identity()));
                        for (ZhongbangAiVoiceFileDetail detail : fileDetails) {
                            if (StringUtils.isBlank(detail.getFileName())) {
                                continue;
                            }
                            try {
                                File dir = new File(localDir);
                                if (!dir.exists() && !dir.mkdirs()) {
                                    log.error("众邦AI下载外呼录音文件，本地目录创建失败：{}", localDir);
                                    continue;
                                }
                                File file = ftpClient.downloadLocalFile(srcPath, detail.getFileName()
                                        , localDir.concat(detail.getFileName()));
                                String fileMd5 = Md5EncodeUtil.encode(file);
                                String parent = file.getParent();
                                long length = file.length();
                                PushCustomerFileInfo fileInfoOld = fileInfoMap.get(detail.getFileName());
                                if (fileInfoOld == null) {
                                    // 文件信息入库
                                    bool = saveInfo(cid, file, apiCode, parent, localDir, length, fileMd5, localFileId) && bool;
                                } else {
                                    updateInfo(fileInfoOld, fileMd5, file, parent, localDir, length);
                                }
                            } catch (SftpException | IOException | SDKException e) {
                                log.error(e.getMessage() + "录音文件：" + detail.getFileName(), e);
                                Thread.currentThread().interrupt();
                                bool = false;
                                if (e instanceof SftpException) {
                                    disconnect(ftpClient);
                                    try {
                                        ftpClient = new SftpClient(syncConfig, true);
                                        ftpClient.connect();
                                    } catch (Exception exception) {
                                        log.error(exception.getMessage());
                                    }
                                }
                            }
                        }
                    } else {
                        bool = false;
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    bool = false;
                } finally {
                    disconnect(ftpClient);
                }
                return bool;
            }, threadPoolGet).exceptionally((Throwable throwable) -> {
                if (throwable != null) {
                    log.error(throwable.getMessage(), throwable);
                }
                return false;
            }));
            if (size < pageSize) {
                break;
            }
        }
        return allOf(futures);
    }

    private void setThreadPool(final String tableName, String poolKey, ThreadPoolExecutor poolExecutor) {
        Map<String, JSONObject> voiceFileConfig = getVoiceAIFileConfig();
        int poolSize = voiceFileConfig.get(tableName).getIntValue(poolKey);
        if (poolSize > 0 && poolSize != poolExecutor.getCorePoolSize()) {
            ThreadPoolAdjustmentUtil.adjustThreadPoolSize(poolExecutor, poolSize);
        }
    }


    private boolean saveInfo(String cid, File file, String apiCode, String parent, String localDir
            , long length, String fileMd5, long localFileId) {
        // 文件信息入库
        PushCustomerFileInfo fileInfo = new PushCustomerFileInfo();
        Date lastModifiedDate = new Date(file.lastModified());
        fileInfo.setCid(cid);
        fileInfo.setApiCode(apiCode);
        fileInfo.setFileName(file.getName());
        fileInfo.setLastModifiedTime(lastModifiedDate);
        fileInfo.setLastModifiedDate(lastModifiedDate);
        fileInfo.setFileDirectory(parent == null ? localDir : parent);
        fileInfo.setFileSize(length);
        fileInfo.setCreateTime(new Date());
        fileInfo.setUpdateTime(fileInfo.getCreateTime());
        fileInfo.setFileMd5(fileMd5);
        fileInfo.setLocalFileId(localFileId);
        // 待推送
        fileInfo.setPushStatus(PushFileStatusEnum.READY.getCode());
        try {
            pushCustomerFileInfoMapper.insertSelective(fileInfo);
            return true;
        } catch (Exception e) {
            log.error("众邦AI录音文件信息保存失败！" + e.getMessage(), e);
            return false;
        }
    }

    private void updateInfo(PushCustomerFileInfo fileInfoOld, String fileMd5, File file, String parent
            , String localDir, long length) {
        if (!fileInfoOld.getFileMd5().equals(fileMd5)) {
            PushCustomerFileInfo fileInfoUpdate = new PushCustomerFileInfo();
            Date lastModifiedDate = new Date(file.lastModified());
            fileInfoUpdate.setId(fileInfoOld.getId());
            fileInfoUpdate.setLastModifiedTime(lastModifiedDate);
            fileInfoUpdate.setLastModifiedDate(lastModifiedDate);
            fileInfoUpdate.setFileDirectory(parent == null ? localDir : parent);
            fileInfoUpdate.setFileSize(length);
            fileInfoUpdate.setUpdateTime(new Date());
            fileInfoUpdate.setFileMd5(fileMd5);
            pushCustomerFileInfoMapper.updateByPrimaryKeySelective(fileInfoUpdate);
        }
    }

    private void disconnect(SftpClient ftpClient) {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.disconnect();
            } catch (Exception e) {
                log.error("众邦AI录音文件下载sftp关闭异常！" + e.getMessage(), e);
            }
        }
    }

    private boolean allOf(List<CompletableFuture<Boolean>> futures) {
        try {
            return CompletableFuture.allOf(futures.toArray(
                    new CompletableFuture[0])).thenApply((Void v) -> {
                boolean b = true;
                for (CompletableFuture<Boolean> future : futures) {
                    try {
                        if (!future.get(1, TimeUnit.MINUTES) && b) {
                            b = false;
                        }
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        log.error(e.getMessage());
                        b = false;
                        Thread.currentThread().interrupt();
                    }
                }
                return b;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage(), e);
        }
        return false;
    }

    private void updateLocalFilePushTime(long localFileId) {
        LocalFile byPrimaryKey = localFileMapper.getByPrimaryKey(localFileId);
        if (byPrimaryKey.getPushStartTime() == null) {
            LocalFile localFileNew = new LocalFile();
            localFileNew.setId(localFileId);
            localFileNew.setPushStartTime(new Date());
            localFileMapper.updateByPrimaryKeySelective(localFileNew);
        }
    }

    private List<CompletableFuture<Boolean>> uploadFile(String cid, String apiCode, int pageSize
            , final String tableName, ThreadPoolExecutor threadPool, LocalFile localFile) {
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        long localFileId = localFile.getId();
        long maxId = 0L;
        for (; true; ) {
            PushCustomerFileInfoExample exampleSelect = new PushCustomerFileInfoExample();
            exampleSelect.createCriteria().andApiCodeEqualTo(apiCode)
                    .andCidEqualTo(cid).andLocalFileIdEqualTo(localFileId)
                    .andStatusEqualTo(1).andPushStatusIn(Arrays.asList(PushFileStatusEnum.READY.getCode(),
                            PushFileStatusEnum.PUSH_ERROR.getCode())).andIdGreaterThan(maxId);
            exampleSelect.setOrderByClause("id limit " + pageSize);
            List<PushCustomerFileInfo> infoList = pushCustomerFileInfoMapper.selectByExample(exampleSelect);
            if (infoList.isEmpty()) {
                break;
            }
            int size = infoList.size();
            maxId = infoList.get(size - 1).getId();
            for (PushCustomerFileInfo fileInfo : infoList) {
                setThreadPool(tableName, "uploadPoolSize", threadPool);
                // 文件推送
                futures.add(CompletableFuture.supplyAsync(() -> {
                    File file = new File(fileInfo.getFileDirectory().concat(File.separator) + fileInfo.getFileName());
                    ZhongbangAiVoiceFileDetail voiceFileDetail = new ZhongbangAiVoiceFileDetail();
                    voiceFileDetail.setFileName(file.getName());
                    voiceFileDetail.setLocalId(localFileId);
                    fileInfo.setPushDate(new Date());
                    //校验sessionId是否在api明细数据中，不在不推送
                    ZhongbangAiVoiceFileDetailExample fileDetailExample = new ZhongbangAiVoiceFileDetailExample();
                    fileDetailExample.createCriteria().andApiCodeEqualTo(apiCode)
                            .andLocalIdEqualTo(localFileId).andFileNameEqualTo(file.getName());
                    List<ZhongbangAiVoiceFileDetail> fileDetailList = zhongbangAiVoiceFileDetailMapper.selectByExample(fileDetailExample);
                    String sessionId = fileDetailList.get(0).getSessionId();
                    CallRecordingExample callRecordingExample = new CallRecordingExample();
                    callRecordingExample.createCriteria().andApiCodeEqualTo(apiCode).andSessionIdEqualTo(sessionId);
                    int callRecord = callRecordingMapper.countByExample(callRecordingExample);
                    if (callRecord == 0) {
                        log.error("众邦AI推送录音文件，sessionId不存在callRecord中,sessionId={}", sessionId);
                        fileInfo.setStatus(3);
                        fileInfo.setRemark("sessionId不存在callRecord中");
                        pushCustomerFileInfoMapper.updateByPrimaryKeySelective(fileInfo);
                        return true;
                    }
                    if (file.exists() && file.isFile()) {
                        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                            UploadInfo uploadInfo = zBankClient.uploadInputStream(inputStream
                                    , file.getName(), file.length(), fileInfo.getFileMd5());
                            voiceFileDetail.setCustomerFileId(uploadInfo.getFileId());
                            voiceFileDetail.setPushStatus(PushFileStatusEnum.SUCCESS.getCode());
                            // 推送成功
                            fileInfo.setPushStatus(PushFileStatusEnum.SUCCESS.getCode());
                            fileInfo.setRemark("");
                        } catch (SDKException | IOException e) {
                            log.error(e.getMessage(), e);
                            fileInfo.setRemark(e.getMessage());
                            if (e instanceof IOException) {
                                // 文件异常
                                fileInfo.setStatus(2);
                            }
                            // 推送失败
                            fileInfo.setPushStatus(PushFileStatusEnum.PUSH_ERROR.getCode());
                            pushCustomerFileInfoMapper.updateByPrimaryKeySelective(fileInfo);
                            return false;
                        }
                    } else {
                        fileInfo.setStatus(2);
                        pushCustomerFileInfoMapper.updateByPrimaryKeySelective(fileInfo);
                        return false;
                    }
                    int i = pushCustomerFileInfoMapper.updateAIFileInfoAndFileDetailtikv_(fileInfo, voiceFileDetail);
                    if (i != 2) {
                        String msg = "众邦AI录音文件上传更新失败:文件：" + fileInfo.getFileName()
                                + ",明细文件：" + localFile.getFileName();
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_USUAL_NOTICE.getCode()
                                , msg, "众邦AI录音文件上传更新失败"));
                        return false;
                    }
                    return true;
                }, threadPool).exceptionally((Throwable throwable) -> {
                    if (throwable != null) {
                        log.error(throwable.getMessage(), throwable);
                    }
                    return false;
                }));
            }
            if (size < pageSize) {
                break;
            }
        }
        return futures;
    }

    @Override
    public void voiceAIFileUploadDetail(LocalFile localFile, LocalDate localDate) {
        String apiCode = localFile.getApiCode();
        String date = localDate.minusDays(1).toString();
        Integer pageSize = 2000;
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(marketingCommonConfig.getZhongBangAIPushFileDetailNum(),
                marketingCommonConfig.getZhongBangAIPushFileDetailNum(), 30);
        //查询api录音回调
        Long indexId = null;
        AtomicLong total = new AtomicLong(0L);
        while (true) {
            List<CallRecording> callRecordingList = callRecordingMapper.getCallRecord(apiCode,
                    date, indexId, pageSize);
            if (callRecordingList.isEmpty()) {
                break;
            }
            indexId = callRecordingList.get(callRecordingList.size() - 1).getId();

            try {
                total.addAndGet(callRecordingList.size());
            } catch (Exception ex) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                , ex.getMessage()
                                , "埋点异常")
                        , ex);
            }

            threadPool.submit(() -> {
                try {
                    pushVoiceDeatil(callRecordingList, localFile);
                } catch (Exception ex) {
                    log.error("众邦AI推送录音明细线程执行异常", ex);
                }
            });
        }
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn("众邦AI推送录音明细线程关闭");
            }
        } catch (InterruptedException ex) {
            log.warn(ex.getMessage(), ex);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        CallRecordingExample callRecordingExample = new CallRecordingExample();
        callRecordingExample.createCriteria().andApiCodeEqualTo(apiCode)
                .andStatusEqualTo(CallRecordStatusEnum.PUSH_ERROR.getCode()).andReceiveDateEqualTo(date);
        int errorNum = callRecordingMapper.countByExample(callRecordingExample);
        if (errorNum > 0) {
            localFile.setPushStatus(String.valueOf(PushFileStatusEnum.PUSH_ERROR.getCode()));
        } else {
            localFile.setPushStatus(String.valueOf(PushFileStatusEnum.SUCCESS.getCode()));
        }
        localFileMapper.updateByPrimaryKeySelective(localFile);

        try {
            String remark = String.format("众邦AI-录音文件明细推送，localFileId：%s，推送是否成功：%s"
                    , localFile.getId()
                    , errorNum > 0 ? "推送异常" : "推送成功");
            trackingService.trackPointLog(DataFlowDirection.OUT
                    , apiCode
                    , "众邦AI-录音文件明细推送"
                    , total.get()
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

    private void pushVoiceDeatil(List<CallRecording> callRecordingList, LocalFile localFile) {
        String apiCode = localFile.getApiCode();
        List<Long> successIdList = new ArrayList<>();
        List<Long> errorIdList = new ArrayList<>();
        //判断有效期
        Set<String> custNumSets = callRecordingList.stream().map(CallRecording::getCustNum).collect(Collectors.toSet());
        Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum =
                transferDataValidityPeriodService.getValidityPeriodsByCustNum(custNumSets, apiCode, localFile.getCreateTime());
        //查询录音上传数据 sessionId+localId
        List<String> seeeionIds = callRecordingList.stream().map(CallRecording::getSessionId).collect(Collectors.toList());
        ZhongbangAiVoiceFileDetailExample fileDetailExample = new ZhongbangAiVoiceFileDetailExample();
        fileDetailExample.createCriteria().andApiCodeEqualTo(apiCode)
                .andLocalIdEqualTo(localFile.getId()).andSessionIdIn(seeeionIds);
        List<ZhongbangAiVoiceFileDetail> fileDetailList = zhongbangAiVoiceFileDetailMapper.selectByExample(fileDetailExample);
        Map<String, List<ZhongbangAiVoiceFileDetail>> fileDetailBySessionId =
                fileDetailList.stream().collect(Collectors.groupingBy(ZhongbangAiVoiceFileDetail::getSessionId));
        callRecordingList.forEach(callRecording -> {
            String custNum = callRecording.getCustNum();
            String sessionId = callRecording.getSessionId();
            SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = validityPeriodsByCustNum.get(custNum);
            if (Objects.isNull(syncUserValidityPeriodsBO)) {
                log.warn("众邦AI推录音明细-不在有效期内的callrecord数据id是:{}", callRecording.getId());
                return;
            }
            JSONObject jsonObject = JSONObject.parseObject(callRecording.getReserveField1());
            MarketingSyncUser marketingSyncUser = syncUserValidityPeriodsBO.getSyncUsers().get(0);
            List<ZhongbangAiVoiceFileDetail> voiceFileDetails = fileDetailBySessionId.get(sessionId);
            JSONObject paramJson = new JSONObject();
            paramJson.put("TskId", marketingSyncUser.getCusBatch());
            paramJson.put("UserTp", marketingSyncUser.getUserType());
            paramJson.put("MblPhnId", marketingSyncUser.getCellOriginal());
            paramJson.put("CllStrtTm", DateHelper.timestampToDateTime(callRecording.getCallStartTime()));
            paramJson.put("CsNo", callRecording.getCustNum());
            paramJson.put("TskNm", callRecording.getTaskName());
            paramJson.put("IsCnnct", callRecording.getIsConnect());
            if (Objects.nonNull(jsonObject)) {
                paramJson.put("CalDrtn", jsonObject.get("callTimeS"));
                paramJson.put("RspRst", jsonObject.get("returnResult"));
            }
            paramJson.put("EndTm", DateHelper.timestampToDateTime(callRecording.getCallEndTime()));
            paramJson.put("AskTms", callRecording.getDialogTurn());
            if(StringUtils.isNotEmpty(callRecording.getCallDialog())){
                String cleanedDialog = callRecording.getCallDialog().replaceAll("<[^>]*>", "");
                paramJson.put("Text", cleanedDialog);
            }
            paramJson.put("CalSts", callRecording.getCallStatus());
            paramJson.put("IntntGrd", callRecording.getIntentionGrade());
            paramJson.put("TgLst", callRecording.getTagList());
            if (!CollectionUtils.isEmpty(voiceFileDetails)) {
                ZhongbangAiVoiceFileDetail fileDetail = voiceFileDetails.get(0);
                paramJson.put("FileNm", fileDetail.getFileName());
                paramJson.put("FlId", fileDetail.getCustomerFileId());
            }
            paramJson.put("SessId", callRecording.getSessionId());
            Result<ZbankResponse<ZbankAIVoiceFileDetailResultDTO>> result = methodRetryHandlerService.pushZbankRecodAIFileRe(paramJson,
                    null);
            if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                successIdList.add(callRecording.getId());
            } else {
                errorIdList.add(callRecording.getId());
            }

        });
        //更新状态
        if (!CollectionUtils.isEmpty(successIdList)) {
            callRecordingMapper.updateBatchByIds(successIdList, CallRecordStatusEnum.SUCCESS.getCode());
        }
        //失败状态
        if (!CollectionUtils.isEmpty(errorIdList)) {
            callRecordingMapper.updateBatchByIds(errorIdList, CallRecordStatusEnum.PUSH_ERROR.getCode());
        }
    }

}
