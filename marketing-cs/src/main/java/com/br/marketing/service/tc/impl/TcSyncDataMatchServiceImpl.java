package com.br.marketing.service.tc.impl;

import com.alibaba.fastjson2.JSONObject;
import com.br.marketing.client.tc.TcServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.file.ZipUtils;
import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.mapper.MarketingTcyrSyncRecordMapper;
import com.br.marketing.service.tc.TcSyncDataMatchService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
public class TcSyncDataMatchServiceImpl implements TcSyncDataMatchService {

    private static final String TITLE = "【同城易融上传数据匹配-gz包拉取入库】";

    private Integer PARTITION_SIZE = 1000;

    @Resource
    private TcServiceClient tcServiceClient;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingTcyrSyncRecordMapper tcPullGzFileMapper;

    @Override
    public List<MarketingTcyrSyncRecord> searchTcyrSyncList(String apiCode,Integer status) {
        return tcPullGzFileMapper.searchTcyrSyncList(apiCode,status);
    }

    /**
     *  具体的下载文件->匹配数据->入库->文件备份操作
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
            String dirPath = marketingCommonConfig.getTcGzFilePath();
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
                log.warn("csv文件入db,csvName:{},csvPath:{} 开始执行",csvFile.getName(),csvFile.getAbsolutePath());
                if (!csvFile.getName().contains(".csv")) {
                    log.warn(TITLE + "解压文件不是csv文件");
                    return result.failure();
                }
                Result parseResult = parseCsvFileToDb(syncRecord.getApiCode(),syncRecord.getBatchNo(),csvFile);
                Long successLine = Long.parseLong(parseResult.getData().toString());
                log.warn("csv文件入db,batchNo:{},csvName{} 执行完成,successCount:{}",syncRecord.getBatchNo(),csvFile.getName(),successLine);
                totalSuccess += successLine;
            }
            result = result.success().setDate(totalSuccess);
        }catch (Exception e){
            log.warn("{} sync error,batchNo:{},error: ", TITLE,syncRecord.getBatchNo(),e);
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
    private Result parseCsvFileToDb(String apiCode, String batchNo, File csvFile) {
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
                    processList(apiCode, batchNo, lineList, actionPool, futureList, resultList);
                    lineBuffer.clear();
                }
            }
            // 处理剩余数据
            if (!lineBuffer.isEmpty()) {
                List<String> lineList = new ArrayList<>();
                lineList.addAll(lineBuffer);
                totalLine += lineBuffer.size();
                processList(apiCode, batchNo, lineList, actionPool, futureList, resultList);

            }

            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
            log.warn(TITLE + "all process complete");

            for (Long successCount : resultList) {
                successLine += successCount;
            }
            log.warn(TITLE + "totalLine: {}, successLine: {}", totalLine, successLine);
            result = result.success().setDate(successLine);
            shutdownThreadPool(actionPool);
        }catch (IOException e) {
            log.error(TITLE + "parseFile error", e);
            return result.failure();
        }
        return result;

    }

    private Result processList(String apiCode, String batchNo, List<String> lineList, ThreadPoolExecutor actionPool, List<CompletableFuture<Result>> futureList, List<Long> resultList) {
        Result result = new Result().failure();
        actionPool.setCorePoolSize(marketingCommonConfig.getTcGzBatDBThreadPool());
        actionPool.setMaximumPoolSize(marketingCommonConfig.getTcGzBatDBThreadPool());
        futureList.add(CompletableFuture.supplyAsync(() -> processData(apiCode, batchNo, lineList), actionPool)
                .whenComplete((processDataResult, throwable) -> {
                    if (processDataResult == null || !processDataResult.isSuccess()) {
                        resultList.add(0L);
                        return;
                    }
                    resultList.add((Long) processDataResult.getData());
                    if (throwable != null) {
                        log.error(TITLE + "completableFuture error:{}", throwable);
                        resultList.add(0L);
                    }
                })
        );
        return result.success();
    }

    private Result processData(String apiCode, String batchNo, List<String> lineList) {
        Result result = new Result().failure();
        try {
            Result processResult = processLineBuffer(apiCode, batchNo, lineList);
            if (processResult == null || !processResult.isSuccess() || processResult.getData() == null) {
                return result.failure();
            }
            List<MarketingTcyrSync> dataList = (List<MarketingTcyrSync>) processResult.getData();
            if (CollectionUtils.isEmpty(dataList)) {
                return result.failure();
            }
            tcPullGzFileMapper.batchAdd(dataList);
            log.warn("{},batchNo:{} 保存转化结果成功,successLine:{}",TITLE,batchNo,dataList.size());
            return result.success().setDate( Long.valueOf(dataList.size()));
        } catch (Exception e) {
            log.error(TITLE + "processData error", e);
            return result.failure();
        }
    }

    /**
     * 转化成 List<MarketingTcyrSync> ,并去匹配填充is_match,cell字段
     * @param apiCode
     * @param batchNo
     * @param lineList
     * @return
     */
    private Result processLineBuffer(String apiCode, String batchNo, List<String> lineList) {
        Result result = new Result().failure();
        if (CollectionUtils.isEmpty(lineList)) {
            return result.success();
        }
        List<MarketingTcyrSync> dataList = new ArrayList<>();
        List<String> userKeyList = new ArrayList<>();
        for (String line : lineList) {
            String[] data = line.split(",");
            String userKey;
            Integer terminal = 0;
            if (data.length > 1) {
                String firstColumn = data[0].trim();
                String secondColumn = data[1].trim();
                if (firstColumn.isEmpty() || secondColumn.isEmpty()) {
                    continue;
                }else {
                    userKey = firstColumn;
                    terminal =Integer.parseInt(secondColumn);
                }
            }else {
                continue;
            }
            MarketingTcyrSync syncItem = new MarketingTcyrSync();
            syncItem.setApiCode(apiCode);
            syncItem.setBatchNo(batchNo);
            syncItem.setUserKey(userKey);
            syncItem.setTerminal(terminal);
            syncItem.setIsMatch(0);
            syncItem.setIsClean(0);
            Date nowDate = new Date();
            syncItem.setCreateTime(nowDate);
            syncItem.setUpdateTime(nowDate);
            userKeyList.add(userKey);
            dataList.add(syncItem);
        }
        List<Long> idList = tcPullGzFileMapper.selectLastUserRecordIdList(apiCode,userKeyList);
        if (!CollectionUtils.isEmpty(idList)) {
            List<Map<String,String>>  userCellMap = tcPullGzFileMapper.selectLastUserRecordList(apiCode,idList);
            //List<Map<String,String>>  userCellMap = tcPullGzFileMapper.selectLastUserCellList(userKeyList);
            Map<String, String> resultMap = new HashMap<>();
            for (Map<String,String> map : userCellMap) {
                String custNum = map.get("custNum");
                String cell = map.get("cell");
                if(StringUtils.isNotBlank(custNum) && StringUtils.isNotBlank(cell)) {
                    resultMap.put(custNum, cell);
                }
            }
            for(MarketingTcyrSync syncItem : dataList) {
                if(resultMap.containsKey(syncItem.getUserKey())) {
                    syncItem.setCell(resultMap.get(syncItem.getUserKey()));
                    syncItem.setIsMatch(1);
                }
            }
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

    @Override
    public Integer updageTcyrRecordSyncStatus(String batchNo, Integer status) {
        return tcPullGzFileMapper.updageTcyrRecordSyncStatus(batchNo,status);
    }
}
