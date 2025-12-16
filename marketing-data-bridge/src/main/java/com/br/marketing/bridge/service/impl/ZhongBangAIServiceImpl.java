package com.br.marketing.bridge.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.MD5Utils;
import com.br.marketing.bridge.service.ZhongBangAIService;
import com.br.marketing.client.zbank.ZbankClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.PullCustomerFileData;
import com.br.marketing.entity.PullCustomerFileDataExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.PullCustomerFileDataMapper;
import com.br.marketing.mapper.PushCustomerFileInfoMapper;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import com.zbank.file.bean.FileDownLoadInfo;
import com.zbank.file.bean.FileInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ZhongBangAIServiceImpl implements ZhongBangAIService {


    @Resource
    private ZbankClient zBankClient;

    @Resource
    private PullCustomerFileDataMapper pullCustomerFileDataMapper;

    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    private PushCustomerFileInfoMapper pushCustomerFileInfoMapper;

    @Resource
    private TrackingService trackingService;


    @Override
    public boolean fileQueryAndDownload(String apiCode, String cid, String fileName, String tableHead, String filePath, String beginDate,
                                        String endDate, ThreadPoolExecutor threadPool) {
        String okFileExtension = ".txt.ok";
        String txtFileExtension = ".txt";
        String regex = "\\|@\\|";
        int maxSaveSize = 30;
        // 查询文件是否已创建完成
        List<FileInfo> okFiles = zBankClient.queryFileList(fileName, beginDate, endDate, 1);
        if (okFiles.size() > 0) {
            List<FileInfo> sortedOkFiles = sortedFileCreateTime(okFiles);
            for (FileInfo okFile : sortedOkFiles) {
                // 查询已经生成完成的文件
                String txtFileName = okFile.getFileName().replace(okFileExtension, txtFileExtension);
                List<FileInfo> infos = zBankClient.queryFileList(txtFileName, beginDate, endDate, 1);
                if (infos.size() > 0) {
                    List<FileInfo> sortedInfos = sortedFileCreateTime(infos);
                    String[] tableHeads = tableHead.split(regex);
                    int heads = tableHeads.length;
                    for (FileInfo fileInfo : sortedInfos) {
                        log.warn("众邦AI({})文件{}开始下载FileId:{}...", apiCode, fileInfo.getFileName(), fileInfo.getFileId());
                        String txtFilePath = filePath.concat(txtFileExtension);
                        if (mkdirPath(txtFilePath, apiCode, fileName)) {
                            return false;
                        }
                        long startTime = System.currentTimeMillis();
                        // 下载生成的文件
                        FileDownLoadInfo fileDownLoadInfo = zBankClient.downLoadSplitFileMergeInLocal(fileInfo, txtFilePath);
                        long endTime = System.currentTimeMillis();
                        if (fileDownLoadInfo == null || fileDownLoadInfo.getDestFile() == null) {
                            log.error("众邦AI({})文件{}下载失败！耗时：{}ms", apiCode, txtFileName, endTime - startTime);
                            return false;
                        }
                        log.warn("众邦AI({})文件{}下载完成，FileMd5:{},FileId:{},文件大小:{},耗时：{}ms"
                                , apiCode, fileDownLoadInfo.getFileName(), fileDownLoadInfo.getFileMd5()
                                , fileDownLoadInfo.getFileId(), fileDownLoadInfo.getFileSize(), endTime - startTime);
                        LocalFile localFile = saveLocalFile(cid, apiCode, txtFilePath, fileInfo);
                        fileInfo.setFileMd5(fileDownLoadInfo.getFileMd5());
                        LocalFile localFileUpdate = new LocalFile();
                        localFileUpdate.setErrorActualNumber(0);
                        localFileUpdate.setPushNumber(0);
                        localFileUpdate.setActualNumber(0);
                        localFileUpdate.setId(localFile.getId());
                        localFileUpdate.setLocalPath(fileDownLoadInfo.getDestFile().getParent());
                        try (LineNumberReader lineNumberReader = new LineNumberReader(new BufferedReader(
                                new InputStreamReader(new BufferedInputStream(new FileInputStream(
                                        // 缓存1M
                                        fileDownLoadInfo.getDestFile())), StandardCharsets.UTF_8), 1024 << 10))) {
                            AtomicInteger errorSum = new AtomicInteger(0);
                            String lineTxt;
                            List<PullCustomerFileData> fileDataList = new ArrayList<>();
                            int rowNum = 1;
                            while ((lineTxt = lineNumberReader.readLine()) != null) {
                                fileDataList.add(newFileData(lineTxt, apiCode, tableHeads, heads, regex, localFileUpdate
                                        , rowNum));
                                rowNum++;
                                if (saveFileData(fileDataList, maxSaveSize, localFile, threadPool, errorSum)) {
                                    fileDataList = new ArrayList<>();
                                }
                            }
                            int lineNumber = lineNumberReader.getLineNumber();
                            localFileUpdate.setActualNumber(lineNumber);
                            saveFileData(fileDataList, 1, localFile, threadPool, errorSum);
                            localFileUpdate.setSrcPath(fileInfo.getFileMd5());
                            isCompletedByTaskCount(threadPool, fileInfo.getFileName());
                            int sum = errorSum.get();
                            if (sum > 0) {
                                log.error("众邦AI({})文件{}入库大量失败或入库异常！失败量：{}", apiCode, txtFileName, sum);
                                return false;
                            }
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                            return false;
                        }
                        localFileUpdate.setStatus("2");
                        localFileUpdate.setPushEndTime(new Date());
                        if (localFileUpdate.getComplete() == null) {
                            localFileUpdate.setComplete("1");
                        }
                        setNumber(apiCode, localFileUpdate);
                        localFileMapper.updateByPrimaryKeySelective(localFileUpdate);
                        String okFilePath = filePath.concat(okFileExtension);
                        if (!mkdirPath(okFilePath, apiCode, okFile.getFileName())) {
                            zBankClient.downLoadSplitFileMergeInLocal(okFile, okFilePath);
                        }

                        try {
                            String remark = String.format("众邦AI文件下载,文件名称：%s"
                                    , fileDownLoadInfo.getFileName());
                            trackingService.trackPointLog(DataFlowDirection.OUT
                                    , apiCode
                                    , "众邦AI文件下载"
                                    , Long.valueOf(localFileUpdate.getActualNumber())
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
                } else {
                    log.warn("众邦AI({})在{}~{}时间段内没有查询到txt文件{}", apiCode, beginDate, endDate, txtFileName);
                }
            }
        } else {
            log.warn("众邦AI({})在{}~{}时间段内没有查询到ok文件{}", apiCode, beginDate, endDate, fileName);
        }
        return false;
    }

    /**
     * 2023-11-17 13:39
     * 文件信息排序，按创建时间降序，创建时间相同时按fileId降序
     */
    private static List<FileInfo> sortedFileCreateTime(List<FileInfo> files) {
        return files.stream().sorted(Comparator.comparing(FileInfo::getCreateTime)
                .thenComparing(FileInfo::getFileId).reversed()).collect(Collectors.toList());
    }

    /**
     * 2023-12-01 12:22
     * true 创建失败
     */
    private boolean mkdirPath(String path, String apiCode, String fileName) {
        File file = new File(path);
        if (!file.exists() && !file.mkdirs()) {
            log.error("众邦AI({})拉取文件目录创建失败，path:{},name:{}", apiCode, path, fileName);
            return true;

        }
        return false;
    }

    /**
     * 2025-11-14 15:49
     * 保存本地文件记录
     */
    private LocalFile saveLocalFile(String cid, String apiCode, String localPath, FileInfo fileInfo) {
        LocalFile localFile = new LocalFile();
        localFile.setApiCode(apiCode);
        localFile.setCid(cid);
        localFile.setPushStartTime(new Date());
        localFile.setFileName(fileInfo.getFileName());
        localFile.setLocalPath(localPath);
        localFile.setStatus("1");
        localFile.setComplete("3");
        localFile.setPushStatus("0");
        localFile.setActualNumber(0);
        localFile.setPushNumber(0);
        localFile.setErrorActualNumber(0);
        // 众邦AI
        localFile.setFileType(SftpFileTypeEnum.ZHONGBANG_AI.getValue());
        localFile.setCreateTime(new Date());
        localFile.setUpdateTime(localFile.getCreateTime());
        localFile.setPushStartTime(localFile.getCreateTime());
        localFileMapper.insertSelective(localFile);
        return localFile;
    }

    /**
     * 2023-11-20 9:48
     * 创建文件数据日志
     */
    private PullCustomerFileData newFileData(String lineTxt
            , String apiCode, String[] tableHead, int heads, String regex, LocalFile localFile, int rowNum) {
        PullCustomerFileData fileData = new PullCustomerFileData();
        String[] rows;
        if (StringUtils.isBlank(lineTxt)) {
            log.warn("文件数据为空");
            fileData.setDataStatus(2);
            localFile.setComplete(null);
            localFile.setErrorActualNumber(localFile.getErrorActualNumber() + 1);
        } else {
            rows = lineTxt.split(regex, -1);
            if (rows.length != heads) {
                log.warn("文件数据分割长度rowlength={},headlength={}", rows.length, heads);
                fileData.setDataStatus(2);
                localFile.setComplete("3");
                localFile.setErrorActualNumber(localFile.getErrorActualNumber() + 1);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject();
                    for (int i = 0; i < heads; i++) {
                        jsonObject.put(tableHead[i], rows[i]);
                    }
                    fileData.setJsonData(jsonObject.toJSONString());}
                catch (Exception e) {
                    log.warn("众邦AI({})解析文件数据异常，行号:{}, 错误:{}", apiCode, rowNum, e.getMessage());
                }
                fileData.setDataStatus(1);
            }
        }
        fileData.setFileData(lineTxt);
        fileData.setApiCode(apiCode);
        fileData.setDataFingerprint(MD5Utils.cell32(lineTxt).concat("_") + rowNum);
        fileData.setLocalFileId(localFile.getId());
        fileData.setCreateDate(LocalDate.now().toString());
        fileData.setCreateTime(new Date());
        fileData.setUpdateTime(fileData.getCreateTime());
        return fileData;
    }


    /**
     * 2023-11-20 9:47
     * 批量保存
     */
    private boolean saveFileData(List<PullCustomerFileData> fileDataList, int saveSize, LocalFile localFile
            , ThreadPoolExecutor threadPool, AtomicInteger errorSum) {
        if (fileDataList.size() >= saveSize) {
            threadPool.execute(() -> {
                if (localFile != null) {
                    Set<String> dataFingerprintSet = pullCustomerFileDataMapper.getDataFingerprintSet(localFile.getId()
                            , fileDataList);
                    if (dataFingerprintSet.size() == fileDataList.size()) {
                        return;
                    }
                    fileDataList.removeIf(f -> dataFingerprintSet.contains(f.getDataFingerprint()));
                }
                if (fileDataList.size() > 0) {
                    int i = pullCustomerFileDataMapper.insertBatchSelective(fileDataList);
                    if (i > 0) {
                        fileDataList.clear();
                        return;
                    }
                    log.error("众邦AI数据入库失败！localFile:{},数据指纹集合{}"
                            , fileDataList.get(0).getLocalFileId()
                            , fileDataList.stream().map(PullCustomerFileData::getDataFingerprint).toArray());
                    errorSum.addAndGet(fileDataList.size());
                }
            });
            return true;
        }
        return false;
    }

    private void isCompletedByTaskCount(ThreadPoolExecutor threadPool, String fileName) {
        int count = 0;
        while (threadPool.getTaskCount() != threadPool.getCompletedTaskCount() && count < 12) {
            log.warn("众邦AI文件{}批量入库未完成，计划执行的任务总数{},完成执行任务的总数{}，当前工作线程数{}，最大线程数{}" +
                            "，等待入库线程执行完。。。",
                    fileName, threadPool.getTaskCount()
                    , threadPool.getCompletedTaskCount()
                    , threadPool.getActiveCount()
                    , threadPool.getMaximumPoolSize());
            count++;
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * 2023-11-22 10:56
     * 实际入库量
     */
    private void setNumber(String apiCode, LocalFile localFileUpdate) {
        PullCustomerFileDataExample example = new PullCustomerFileDataExample();
        example.createCriteria().andApiCodeEqualTo(apiCode)
                .andLocalFileIdEqualTo(localFileUpdate.getId())
                .andDataStatusEqualTo(1);
        int i = pullCustomerFileDataMapper.countByExample(example);
        int num = localFileUpdate.getActualNumber() - localFileUpdate.getErrorActualNumber();
        localFileUpdate.setPushNumber(Math.max(num, i));
    }


}
