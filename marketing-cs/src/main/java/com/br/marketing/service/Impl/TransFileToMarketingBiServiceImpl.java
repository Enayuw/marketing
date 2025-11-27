package com.br.marketing.service.Impl;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.TransFileToMarketingBiService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author peng.kang
 * @description: 内部服务器的转化文件落库到marketing BI
 * @date 2025/4/22 16:04
 */
@Slf4j
@Service
public class TransFileToMarketingBiServiceImpl implements TransFileToMarketingBiService {
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Autowired
    private TransferFileTaskMapper transferFileTaskMapper;

    @Autowired
    private TransferFileExtractToDorisBIMapper transferFileExtractToDorisBIMapper;

    @Resource
    private BFileBiConfigMapper bFileBiConfigMapper;

    @Autowired
    private NfsFileTOBiRecordMapper nfsFileTOBiRecordMapper;

    private static final int BATCH_SIZE = 50;

    @Override
    public void transFileToMarketingBiProcess(String jobParam) {
        marketingCommonConfig.getTransFileExtractionApiCodesConfig().forEach(obj -> {
            String apiCode = obj.getString("apiCode");
            Integer fileType = obj.getInteger("fileType");

            String date = StringUtils.isNotEmpty(jobParam) ? jobParam : LocalDate.now().toString().replace("-", "");
            List<String> dateList = Arrays.asList(date.split(","));
            for (int i = 0; i < dateList.size(); i++) {
                String dateItem = dateList.get(i);
                //按照日期执行
                TransferFileTaskExample taskExample = new TransferFileTaskExample();
                taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(dateItem)
                        .andFileTypeEqualTo(fileType).andStatusGreaterThanOrEqualTo(2);
                List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
                if (CollectionUtils.isNotEmpty(transferFileTasks)) {
                    TransferFileTask transferFileTask = transferFileTasks.get(0);

                    NfsFileTOBiRecordExample nfsExample = new NfsFileTOBiRecordExample();
                    nfsExample.createCriteria().andApiCodeEqualTo(apiCode).andFileTypeEqualTo(fileType)
                            .andExecuteDateEqualTo(dateItem).andBusTypeEqualTo("1");
                    List<NfsFileTOBiRecord> nfsFileTOBiRecordList = nfsFileTOBiRecordMapper.selectByExample(nfsExample);
                    if (CollectionUtils.isNotEmpty(nfsFileTOBiRecordList)) {
                        if (transferFileTask.getId().equals(nfsFileTOBiRecordList.get(0).getTaskId())) {
                            continue;
                        } else {
                            nfsFileTOBiRecordMapper.deleteByExample(nfsExample);
                            List<BFileBiConfig> bFileBiConfigs = getBFileBiConfig(apiCode, "1");
                            if (CollectionUtils.isEmpty(bFileBiConfigs)) {
                                continue;
                            }
                            String deleteSql = "delete from " + bFileBiConfigs.get(0).getDbName() +
                                    " where data_date = '" + LocalDate.now() + "'";
                            transferFileExtractToDorisBIMapper.deleteDataFromMarketingBiTablebI_(deleteSql);
                        }
                    }
                    List<BFileBiConfig> bFileBiConfigs = getBFileBiConfig(apiCode, "1");
                    if (CollectionUtils.isEmpty(bFileBiConfigs)) {
                        continue;
                    }
                    processTransferFile(transferFileTask, bFileBiConfigs.get(0), dateItem, apiCode, fileType);
                }
            }
        });
    }

    public List<BFileBiConfig> getBFileBiConfig(String apiCode, String busType) {
        BFileBiConfigExample example = new BFileBiConfigExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andBusTypeEqualTo(busType);
        List<BFileBiConfig> bFileBiConfigs = bFileBiConfigMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(bFileBiConfigs)) {
            String errMsg = "apiCode: " + apiCode + " nfs转化提取文件落库到marketingBI没有找到对应的配置信息";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.BI_SERVICEERROR.getCode(), errMsg));
            return new ArrayList<>();
        }
        return bFileBiConfigs;
    }


    void processTransferFile(TransferFileTask transferFileTask, BFileBiConfig bFileBiConfig, String dateItem, String apiCode, Integer fileType) {
        //修改自己的配置
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(marketingCommonConfig.getTransFileExtractionBIThread()
                , marketingCommonConfig.getTransFileExtractionBIThread());
        String filePath = transferFileTask.getFilePath().concat(transferFileTask.getFileName());
        File file = new File(filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // 获取文件表头
            List<String> columns = Arrays.asList(bFileBiConfig.getDbFields().split(","));
            // 每50行数据1个线程写入tidb
            List<String> batchData = new ArrayList<>();
            String dataLine;
            reader.readLine();
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            while ((dataLine = reader.readLine()) != null) {
                modifyThreadPool(threadPool);
                batchData.add(dataLine);
                if (batchData.size() == BATCH_SIZE) {
                    ArrayList<String> copyListObj = new ArrayList<>(batchData);
                    futures.add(CompletableFuture.runAsync(() -> writeFileDataToTidb(bFileBiConfig.getDbName(), columns, copyListObj, dateItem), threadPool));
                    batchData.clear();
                }
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            if (!batchData.isEmpty()) {
                writeFileDataToTidb(bFileBiConfig.getDbName(), columns, new ArrayList<>(batchData), dateItem);
            }
            NfsFileTOBiRecord record = new NfsFileTOBiRecord();
            record.setApiCode(apiCode);
            record.setFileType(fileType);
            record.setFilePath(transferFileTask.getFilePath());
            record.setFileName(transferFileTask.getFileName());
            record.setTaskId(transferFileTask.getId());
            record.setExecuteDate(LocalDate.now().toString().replace("-", ""));
            nfsFileTOBiRecordMapper.insertSelective(record);

            threadPoolShutDown(threadPool);
        } catch (Exception e) {
            String errMsg = "nfs转化提取文件读取入库异常path: " + filePath + " Exception: " + e.getMessage();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.BI_SERVICEERROR.getCode(), errMsg));
            threadPoolShutDown(threadPool);
        }
    }

    private void modifyThreadPool(ThreadPoolExecutor pool) {
        Integer threadNum = marketingCommonConfig.getTransFileExtractionBIThread();
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, threadNum);
    }

    private void writeFileDataToTidb(String tableName, List<String> columns, List<String> batchData, String dateItem) {
        StringBuilder insertSql = new StringBuilder("INSERT INTO ");
        insertSql.append(tableName).append(" (");
        for (int i = 0; i < columns.size(); i++) {
            if (i != columns.size() - 1) {
                insertSql.append(columns.get(i).trim()).append(", ");
            } else {
                insertSql.append(columns.get(i).trim()).append(") VALUES ");
            }
        }
        List<String> dataList;
        // 遍历每行
        for (String dataLine : batchData) {
            dataList = Arrays.asList(dataLine.split(",", -1));
            insertSql.append("(");
            // 遍历每列
            for (int i = 0; i < dataList.size(); i++) {
                String value = StringUtils.isEmpty(dataList.get(i).trim()) ? null : dataList.get(i).trim();
                if (i != dataList.size() - 1) {
                    insertSql.append("'").append(value).append("', ");
                } else {
                    try {
                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");
                        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                        insertSql.append("'").append(value).append("', ");
                        insertSql.append("'").append(outputFormat.format(inputFormat.parse(dateItem))).append("'),");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
        insertSql.setLength(insertSql.length() - 1);
        transferFileExtractToDorisBIMapper.insertDataToMarketingBiTablebI_(insertSql.toString());
    }

    private void threadPoolShutDown(ThreadPoolExecutor threadPool) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("nfs转文件提取至BI线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "nfs转文件提取至BI，日志保存线程池结束异常！errorMessage=" + ex.getMessage()), ex);
            Thread.currentThread().interrupt();
        }
    }
}

