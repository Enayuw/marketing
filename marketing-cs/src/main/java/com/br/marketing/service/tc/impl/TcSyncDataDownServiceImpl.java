package com.br.marketing.service.tc.impl;

import com.alibaba.fastjson2.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.tc.TcServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.file.ZipUtils;
import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrSyncFile;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.mapper.MarketingTcyrSyncFileMapper;
import com.br.marketing.mapper.MarketingTcyrSyncMapper;
import com.br.marketing.mapper.MarketingTcyrSyncRecordMapper;
import com.br.marketing.service.tc.TcSyncDataDownService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 同城易融拉取文件入库-Service实现
 *
 * @author zhiyong.zhang
 * @date 2024/04/21
 */
@Service
@Slf4j
public class TcSyncDataDownServiceImpl implements TcSyncDataDownService {

    private static final String TITLE = "【同程易融-DownToDb任务】";

    private Integer PARTITION_SIZE = 1000;

    @Resource
    private TcServiceClient tcServiceClient;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingTcyrSyncRecordMapper tcyrSyncRecordMapper;

    @Resource
    private MarketingTcyrSyncFileMapper tcyrSyncFileMapper;

    @Resource
    private MarketingTcyrSyncMapper tcyrSyncMapper;

    @Value("${otherConfig.warning.sftpHost:00}")
    private String sftpHost;
    @Value("${otherConfig.warning.sftpPort:00}")
    private Integer sftpPort;
    @Value("${otherConfig.warning.sftpUser:00}")
    private String sftpUsername;
    @Value("${otherConfig.warning.sftpPwd:00}")
    private String sftpPwd;

    @Override
    public List<MarketingTcyrSyncRecord> searchTcyrSyncList(String apiCode,Integer status) {
        return tcyrSyncRecordMapper.searchTcyrSyncList(apiCode,status);
    }


    /**
     *  具体的下载文件->数据入库->文件备份操作
     *  //TODO 文件上传SFTP,SFTP相关的服务器/账号/路径 都通过speed配置
     * @param syncRecord
     * @return
     */
    @Override
    public Result dealTcyrFileSync(MarketingTcyrSyncRecord syncRecord) {
        Result result = new Result<>().failure();
        Long totalSuccess =0L;
        try{
            String dataInfo = syncRecord.getData();
            if (StringUtils.isEmpty(dataInfo)) {
                log.warn("apiCode:{},batchNo:{} 下载数据为空",syncRecord.getApiCode(),syncRecord.getBatchNo());
                return result.failure();
            }
            JSONObject dataJson = JSONObject.parseObject(dataInfo);
            String fileUrl = dataJson.getString("fileUrl");
            if (StringUtils.isEmpty(fileUrl)) {
                log.warn("apiCode:{},batchNo:{},fileUrl:{} 下载链接为空",syncRecord.getApiCode(),syncRecord.getBatchNo(),fileUrl);
                return result.failure();
            }
            //文件下载
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            String dirPath = getPath() +"tongcheng_customize_upload_data/"+yyyyMMdd+"/";
            String gzFileName= "tcyr_"+syncRecord.getBatchNo()+".csv.gz";
            String gzFilePath = dirPath.concat(gzFileName);
            Result callFileResult = tcServiceClient.pullTcyrGzFileResult(fileUrl,gzFilePath);
            if (callFileResult == null || !callFileResult.isSuccess()) {
                log.warn("{},batchNo:{} 下载gz包失败",TITLE,syncRecord.getBatchNo());
                return result.failure();
            }
            log.warn("{},batchNo:{} 下载gz包成功",TITLE,syncRecord.getBatchNo());


            // 解压
            File gzFile = new File(gzFilePath);
            if (!gzFile.exists() || !gzFile.getName().contains(".gz")) {
                log.warn("{}_batchNo:{} 对应gz文件不存在",TITLE,syncRecord.getBatchNo());
                return result.failure();
            }
            String csvFilePath = dirPath+"/csv/"+syncRecord.getBatchNo()+"/";
            ZipUtils.unZip(gzFile, csvFilePath, "");
            log.warn(TITLE + "解压zip包成功");
            File csvDir = new File(csvFilePath);
            File[] files = csvDir.listFiles();
            if (files == null) {
                log.warn(TITLE + "解压csv文件不存在");
                return result.failure();
            }
            //文件解析入库
            for (File csvFile : files) {
                log.warn("{} csv文件入db,csvName:{},csvPath:{} 开始执行",TITLE,csvFile.getName(),csvFile.getAbsolutePath());
                Result parseResult = parseCsvFileToDb(syncRecord.getApiCode(),syncRecord.getBatchNo(),csvFile,dataInfo);
                Long successLine = Long.parseLong(parseResult.getData().toString());
                log.warn("{} csv文件入db,batchNo:{},csvName{} 执行完成,successCount:{}",TITLE,syncRecord.getBatchNo(),csvFile.getName(),successLine);
                totalSuccess += successLine;
            }
            result = result.success().setDate(totalSuccess);
        }catch (Exception e){
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
        return result;
    }

    /**
     * csvFile文件入库
     * @param apiCode
     * @param batchNo
     * @param csvFile
     * @return
     */
    private Result parseCsvFileToDb(String apiCode, String batchNo, File csvFile,String dataInfo) {
        Result result = new Result().failure();
        Long successLine =0L;
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            //批处理数据
            List<String> lineBuffer = new ArrayList<>();

            ThreadPoolExecutor actionPool = BrExecutors.getThreadPool(10, 10);
            List<CompletableFuture<Result>> futureList = new ArrayList<>();
            List<Long> resultList = Collections.synchronizedList(new ArrayList<>(20));
            PARTITION_SIZE = marketingCommonConfig.getTcGzResultPartitionSize();
            String line;
            Long totalLine = 0L;
            while ((line = reader.readLine()) != null) {
                lineBuffer.add(line);
                if (lineBuffer.size() >= PARTITION_SIZE) {
                    List<String> lineList = new ArrayList<>();
                    lineList.addAll(lineBuffer);
                    totalLine += lineBuffer.size();
                    processList(apiCode, batchNo, lineList, actionPool, futureList, resultList,dataInfo);
                    lineBuffer.clear();
                }
            }
            // 处理剩余数据
            if (!lineBuffer.isEmpty()) {
                List<String> lineList = new ArrayList<>();
                lineList.addAll(lineBuffer);
                totalLine += lineBuffer.size();
                processList(apiCode, batchNo, lineList, actionPool, futureList, resultList,dataInfo);

            }

            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
            log.warn(TITLE + "all process complete");

            for (Long successCount : resultList) {
                successLine += successCount;
            }
            log.warn(TITLE + "csvFile:{}, totalLine: {}, successLine: {}", csvFile.getName(),totalLine, successLine);
            result = result.success().setDate(successLine);
            shutdownThreadPool(actionPool);
        }catch (IOException e) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
            return result.failure();
        }
        return result;

    }

    private Result processList(String apiCode, String batchNo, List<String> lineList, ThreadPoolExecutor actionPool,
                               List<CompletableFuture<Result>> futureList, List<Long> resultList,String dataInfo) {
        Result result = new Result().failure();
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(actionPool, marketingCommonConfig.getTcGzBatDBThreadPool());
        futureList.add(CompletableFuture.supplyAsync(() -> processData(apiCode, batchNo, lineList,dataInfo), actionPool)
                .whenComplete((processDataResult, throwable) -> {
                    if (processDataResult == null || !processDataResult.isSuccess()) {
                        resultList.add(0L);
                        return;
                    }
                    resultList.add((Long) processDataResult.getData());
                    if (throwable != null) {
                        log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),throwable.getMessage(), TITLE), throwable);
                        resultList.add(0L);
                    }
                })
        );
        return result.success();
    }

    private Result processData(String apiCode, String batchNo, List<String> lineList,String dataInfo) {
        Result result = new Result().failure();
        try {
            Result processResult = processLineBuffer(apiCode, batchNo, lineList,dataInfo);
            if (processResult == null || !processResult.isSuccess() || processResult.getData() == null) {
                return result.failure();
            }
            List<MarketingTcyrSync> dataList = (List<MarketingTcyrSync>) processResult.getData();
            if (CollectionUtils.isEmpty(dataList)) {
                return result.failure();
            }
            tcyrSyncRecordMapper.batchAdd(dataList);
            return result.success().setDate( Long.valueOf(dataList.size()));
        } catch (Exception e) {
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
            return result.failure();
        }
    }

    /**
     * 转化成 List<MarketingTcyrSync>
     * @param apiCode
     * @param batchNo
     * @param lineList
     * @return
     */
    private Result processLineBuffer(String apiCode, String batchNo, List<String> lineList,String dataInfo) {
        Result result = new Result().failure();
        if (CollectionUtils.isEmpty(lineList)) {
            return result.success();
        }
        List<MarketingTcyrSync> dataList = new ArrayList<>();
        for (String line : lineList) {
            MarketingTcyrSync syncItem = new MarketingTcyrSync();
            String[] data = line.split(",");
            int dataStatus = 0;
            // length=1: 空字符串/没有逗号 赋值给第一个字段
            // length=2 userKey:column1、terminal:column
            // length>2  多余的数据放入extend:扩展字段(jsonObject)
            if (data.length ==1) {
                syncItem.setUserKey(line);
            }else if (data.length >=2) {
                syncItem.setUserKey(data[0].trim());
                syncItem.setTerminal(data[1].trim());
                dataStatus =1;
            }
            JSONObject extentJson = new JSONObject();
            for (int i = 0; i < data.length; i++) {
                extentJson.put("column_"+(i+1), data[i]);
            }

            JSONObject customJson = JSONObject.parseObject(dataInfo);
            List<String> tcyrSyncExcludeFieldList = marketingCommonConfig.getTcyrSyncSaveExcludeFieldList();
            for (String key : customJson.keySet()) {
                if (!tcyrSyncExcludeFieldList.contains(key)) {
                    extentJson.put(key, customJson.get(key));
                }
            }
            syncItem.setExtend(extentJson.toJSONString());
            syncItem.setApiCode(apiCode);
            syncItem.setBatchNo(batchNo);
            syncItem.setCreateTime(new Date());
            syncItem.setStatus(dataStatus);
            dataList.add(syncItem);
        }
        return result.success().setDate(dataList);
    }


    public  void shutdownThreadPool(ThreadPoolExecutor executor) {
        log.warn(TITLE + "shutdownThreadPool开始");
        long taskCount = -1;
        executor.shutdown();
        try {
            while (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                long completedTaskCount = executor.getCompletedTaskCount();
                if (taskCount == completedTaskCount) {
                    log.warn(TITLE + "业务线程等待超时");
                    break;
                }
                taskCount = completedTaskCount;
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
        } catch (Throwable e) {
            log.warn(TITLE + "ThreadPoolManager shutdown executor has error : ", e);
        }
        log.warn(TITLE + "shutdownThreadPool结束");
    }

    /**
     *  同程易融: gz下载文件->TXT信息入库
     * @param syncRecord
     * @return
     * todo 半小时job一次
     */
    @Override
    public void dealTcyrTxtFileSync(MarketingTcyrSyncRecord syncRecord) {
        try{
            JSONObject dataJson = JSONObject.parseObject(syncRecord.getData());
            String fileUrl = dataJson.getString("fileUrl");
            //1、gz文件下载
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            String dirPath = getPath() +"tongcheng_customize_upload_data/"+yyyyMMdd+"/";
            String gzFileName= "tcyr_"+syncRecord.getBatchNo()+".csv.gz";
            String gzFilePath = dirPath.concat(gzFileName);
            Result callFileResult = tcServiceClient.pullTcyrGzFileResult(fileUrl,gzFilePath);
            if (callFileResult == null || !callFileResult.isSuccess()) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                        syncRecord.getBatchNo()+"文件下载失败", TITLE));
                return;
            }
            //2、gz解压
            File gzFile = new File(gzFilePath);
            if (!gzFile.exists() || !gzFile.getName().contains(".gz")) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                        syncRecord.getBatchNo()+"对应gz文件不存在", TITLE));
                return;
            }
            String csvFilePath = dirPath+"csv/"+syncRecord.getBatchNo()+"/";
            ZipUtils.unZip(gzFile, csvFilePath, "");
            File csvDir = new File(csvFilePath);
            File[] files = csvDir.listFiles();
            if (files == null) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                        syncRecord.getBatchNo()+"解压csv文件不存在", TITLE));
               return;
            }
            //3、txt文件信息析入库
            Date nowDate = new Date();
            for (File csvFile : files) {
                log.warn("{} csv文件入db,csvName:{},csvPath:{} 开始执行",TITLE,csvFile.getName(),csvFile.getAbsolutePath());
                MarketingTcyrSyncFile tcyrSyncFile = new MarketingTcyrSyncFile();
                tcyrSyncFile.setApiCode(syncRecord.getApiCode());
                tcyrSyncFile.setBatchNo(syncRecord.getBatchNo());
                tcyrSyncFile.setFileName(csvFile.getName());
                tcyrSyncFile.setFilePath(csvFilePath+csvFile.getName());
                tcyrSyncFile.setSyncRecordId(syncRecord.getId());
                tcyrSyncFile.setCreateTime(nowDate);
                tcyrSyncFileMapper.insertSelective(tcyrSyncFile);
            }
            //4、更新 syncRecord 状态
            tcyrSyncRecordMapper.updateTcyrRecordDownStatus(syncRecord.getBatchNo(), 2);
        }catch (Exception e){
            tcyrSyncRecordMapper.updateTcyrRecordDownStatus(syncRecord.getBatchNo(), 3);
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
    }



    public String getPath() {
        String nfsPath = marketingCommonConfig.getNfsPath();
        return StringUtils.isBlank(nfsPath) ? "/opt/data/inloan/download/marketing/" : nfsPath;
    }

    @Override
    public Integer updateTcyrRecordDownStatus(String batchNo, Integer status) {
        return tcyrSyncRecordMapper.updateTcyrRecordDownStatus(batchNo,status);
    }

}
