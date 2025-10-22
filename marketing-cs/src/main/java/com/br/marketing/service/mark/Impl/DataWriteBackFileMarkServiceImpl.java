package com.br.marketing.service.mark.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.excel.util.CollectionUtils;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.file.ZipUtil;
import com.br.marketing.entity.FlagData;
import com.br.marketing.entity.FlagDataExample;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.enums.EsSyncStatusEnum;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.mark.DataMarkCommonService;
import com.br.marketing.service.mark.DataWriteBackFileMarkService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName DataWriteBackFileMarkServiceImpl
 * @Description pp停车文件数据回写跑分文件与Doris实现
 * @Author kongbx
 * @Date 2025/2/19 19:12
 */
@Service
@Slf4j
public class DataWriteBackFileMarkServiceImpl implements DataWriteBackFileMarkService {

    @Autowired
    SyncConfigService syncConfigService;
    @Value("${otherConfig.warning.sftpHost:00}")
    private String sftpHost;
    @Value("${otherConfig.warning.sftpPort:00}")
    private Integer sftpPort;
    @Value("${otherConfig.warning.sftpUser:00}")
    private String sftpUsername;
    @Value("${otherConfig.warning.sftpPwd:00}")
    private String sftpPwd;
    @Autowired
    RedisChgService redisChgService;
    @Resource
    DataMarkCommonService dataMarkCommonService;
    @Resource
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    FlagDataMapper flagDataMapper;
    private static final String TITLE = "【pp停车文件数据回写】";

    @Override
    public void process(String scoreDate) {
        marketingCommonConfig.getDataMarkApiCodes().forEach((String apiCode) -> {
            if (checkEsStatus(apiCode)) {
                StraHisFile straHisFile = dataMarkCommonService.getStraHisFile(apiCode, scoreDate);;
                if (null == straHisFile) {
                    log.warn(TITLE + "查询跑分文件未空，apiCode:"+apiCode,"日期："+scoreDate);
                    return;
                }
                String batchNumber = straHisFile.getBatchNumber();
                String syncDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
                String descPath = syncConfigService.getPath().concat("ppMarkToFile/").concat(apiCode).concat("/").concat(syncDate).concat("/");
                String fileName = "pp_" + apiCode + "_" + syncDate + ".txt";
                List<String> columnNameList = flagDataMapper.queryColumnNamebI_("b_score_".concat(batchNumber));

                // 确保目标目录存在
                File dir = new File(descPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String fileAllPath = Paths.get(descPath, fileName).toString();
                File outputFile = new File(fileAllPath);
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile, true), StandardCharsets.UTF_8))) {
                    if (!outputFile.exists() || outputFile.length() == 0) {
                        // 写入表头
                        String header = String.join(",", columnNameList) + System.lineSeparator();
                        writer.append(header);
                    }
                    // 同步数据写入doris
                    syncData(apiCode, descPath, batchNumber, columnNameList, writer);

                } catch (IOException e) {
                    log.warn(TITLE + "文件写入异常", e);
                }
                // 推送文件至SFTP
                pushFileSftp(apiCode, descPath, fileName);
            }
        });

    }

    /**
     * 判断es数据是否补充完毕
     *
     * @return
     */
    private boolean checkEsStatus(String apiCode) {
        Boolean aFalse = Boolean.TRUE;
        FlagDataExample flagDataExample = new FlagDataExample();
        flagDataExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andAppletDateEqualTo(LocalDate.now().toString())
                .andEsSyncStatusNotEqualTo(EsSyncStatusEnum.COMPLETE.getValue())
                .andIsDeleteEqualTo(0);
        int i = flagDataMapper.countByExample(flagDataExample);
        if (i > 0) {
            log.warn(TITLE + "es数据未补充完毕");
            aFalse = Boolean.FALSE;
        }
        return aFalse;
    }

    /**
     * 同步数据写入doris
     */
    private void syncData(String apiCode, String descPath, String batchNumber,
                          List<String> columnNameList, Writer writer) {
        Integer threadPoolSize = marketingCommonConfig.getDataWriterMarkThreadNum();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);
        try {
            Long minId = null;
            boolean isContiue = Boolean.TRUE;
            while (isContiue) {
                // 分页查询打标数据
                int dataMarkPageSize = marketingCommonConfig.getDataDorisMarkPageSize() == null ? 2000 : marketingCommonConfig.getDataDorisMarkPageSize();
                Integer newThreadPoolSize = marketingCommonConfig.getDataWriterMarkThreadNum();
                if (!newThreadPoolSize.equals(threadPoolSize)) {
                    ThreadPoolAdjustmentUtil.adjustThreadPoolSize(threadPool, newThreadPoolSize);
                    threadPoolSize = newThreadPoolSize;
                    log.warn(TITLE + "线程池大小已动态调整为: {}", threadPoolSize);
                }

                FlagDataExample flagDataExample = new FlagDataExample();
                flagDataExample.setOrderByClause("id limit " + dataMarkPageSize);

                FlagDataExample.Criteria criteria = flagDataExample.createCriteria()
                        .andApiCodeEqualTo(apiCode)
                        .andAppletDateEqualTo(LocalDate.now().toString())
                        .andEsSyncStatusEqualTo(EsSyncStatusEnum.COMPLETE.getValue());
                if (minId != null) {
                    criteria.andIdGreaterThan(minId);
                }
                List<FlagData> flagDataList = flagDataMapper.selectByExample(flagDataExample);
                if (CollectionUtil.isEmpty(flagDataList)) {
                    isContiue = Boolean.FALSE;
                    continue;
                }
                minId = flagDataList.get(flagDataList.size() - 1).getId();
                threadPool.submit(() -> writeBackFileMark(flagDataList, batchNumber, descPath, columnNameList, writer));
            }
            threadPool.shutdown();
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn(TITLE + "线程池关闭");
            }
        } catch (Exception ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ES_RETRY_DATAERROR.getCode(), TITLE + "线程池关闭！异常"), ex);
            Thread.currentThread().interrupt();
        }
    }

    private void writeBackFileMark(List<FlagData> flagDataList, String batchNumber,
                                   String descPath, List<String> columnNameList, Writer writer) {
        // 写入doris
        List<Map<String, Object>> dataList = insertMarkData(flagDataList, batchNumber,columnNameList);
        // 写入文件
        writeDataToFile(dataList, descPath,columnNameList, writer);
    }

    private List<Map<String, Object>> insertMarkData(List<FlagData> flagDataList, String batchNumber,List<String> columnNameList) {
        List<Map<String, Object>> flagDataResult = new ArrayList<>();
        try {
            List<String> cellMd5List = flagDataList.stream().map(FlagData::getCellMd5).collect(Collectors.toList());

            Map<String, FlagData> groupByCellMd5 = flagDataList.stream()
                    .collect(Collectors.toMap(FlagData::getCellMd5, data -> data, (oldValue, newValue) -> newValue));

            // 根据cells查询doris数据
            String cell = cellMd5List.stream()
                    .map(md5 -> "'" + md5 + "'")
                    .collect(Collectors.joining(","));
            String scoreSql = "select * from b_score_".concat(batchNumber).concat(" where cell in(").concat(cell).concat(")");
            flagDataResult = flagDataMapper.queryDataByCellbI_(scoreSql);

            if(CollectionUtil.isEmpty(flagDataResult)){
                return flagDataResult;
            }

            flagDataResult.forEach((Map<String, Object> resultMap) -> {
                FlagData flagData = groupByCellMd5.get(resultMap.get("cell"));
                resultMap.put("dt_whitelist", flagData.getDtWhitelist() != null?new SimpleDateFormat("yyyy-MM-dd").format(flagData.getDtWhitelist()):null);
                resultMap.put("flag_new_cust", flagData.getFlagNewCust());
                resultMap.put("flag_riskgroup", flagData.getFlagRiskgroup());
                resultMap.put("flag_interest", flagData.getFlagInterest());
                resultMap.put("flag_age", flagData.getFlagAge());
                resultMap.put("flag_province", flagData.getFlagProvince());
                resultMap.put("flag_special_small", flagData.getFlagSpecialSmall());
                resultMap.put("flag_specialrisklevel_rule", flagData.getFlagSpecialrisklevelRule());
                resultMap.put("flag_applyloan", flagData.getFlagApplyloan());
                resultMap.put("flag_scoreysbase", flagData.getFlagScoreysbase());
                resultMap.put("flag_scorefxsbbaseb", flagData.getFlagScorefxsbbaseb());
                resultMap.put("flag_scorescashonregisternologin", flagData.getFlagScorescashonregisternologin());
                resultMap.put("flag_scorescashonyxxy", flagData.getFlagScorescashonyxxy());
                resultMap.put("flag_scorencashonzawswyyym", flagData.getFlagScorencashonzawswyyym());
                resultMap.put("flag_intellaudio_blacklist", flagData.getFlagIntellaudioBlacklist());
                resultMap.put("flag_without_willingness", flagData.getFlagWithoutWillingness());
                resultMap.put("flag_whitelist", flagData.getFlagWhitelist());
            });
            // 构建批量插入语句
            List<String> valueClauses = flagDataResult.stream()
                    .map(resultMap -> "(" + columnNameList.stream()
                            .map(columnName -> resultMap.get(columnName) != null ? "'" + resultMap.get(columnName).toString().replace("'", "''") + "'" : "NULL")
                            .collect(Collectors.joining(", ")) + ")")
                    .collect(Collectors.toList());

            String tableName = marketingCommonConfig.getDataMarkTableName();
            String batchInsertSql = "INSERT INTO " + tableName.replace("\"", "") + " (" + String.join(", ", columnNameList) + ") VALUES " + String.join(", ", valueClauses);
            // 写入doris
            flagDataMapper.insertbI_(batchInsertSql);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    TITLE + "出现异常，" + "errorMessage=" + e.getMessage()), e);
        }
        return flagDataResult;
    }

    private void writeDataToFile(List<Map<String, Object>> dataList, String descPath,
                                 List<String> columnNameList,  Writer writer) {

        if(CollectionUtils.isEmpty(dataList)){
            return;
        }
        // 确保目标目录存在
        File dir = new File(descPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            for (Map<String, Object> resultMap : dataList) {
                List<String> values = new ArrayList<>();
                for (String column : columnNameList){
                    Object o = resultMap.get(column);
                    if(o == null){
                        values.add("");
                        continue;
                    }
                    if("dt_whitelist".equals(column)){
                        if (o instanceof Date) {
                            Date date = (Date) o;
                            String format = new SimpleDateFormat("yyyy-MM-dd").format(date);
                            values.add(format);
                        }else {
                            values.add((String) o);
                        }
                    }else {
                        values.add(o.toString().replace(".000000",""));
                    }
                }
                String row = String.join(",", values) + System.lineSeparator();
                writer.append(row);
            }
        } catch (IOException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                    TITLE + "写入文件时发生异常"), e);
        }
    }

    /**
     * 推送文件至SFTP
     */
    private void pushFileSftp(String apiCode, String descPath, String fileName) {
        SftpClient sftpClient = new SftpClient(sftpHost, sftpPort, sftpUsername, sftpPwd);
        String syncDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String remotePath = "/UploadFiles/marketing/" + apiCode + "/output/" + syncDate;

        String zipFileName = fileName.replace(".txt", ".zip");
        String localFileName = descPath.concat(fileName);
        String localFileNameZip = descPath.concat(zipFileName);
        log.warn(TITLE + "原文件路径：" + localFileNameZip + " | 推送文件路径:" + remotePath);
        try {
            ZipUtil.compress(localFileName, localFileNameZip);
            sftpClient.connect();
            boolean upload = sftpClient.uploadFile(remotePath, zipFileName, localFileNameZip);
            if (upload) {
                File successFile = new File(localFileNameZip + ".success");
                successFile.createNewFile();
                if (successFile.exists()) {
                    sftpClient.uploadFile(remotePath, zipFileName + ".success", localFileNameZip + ".success");
                }
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PUSH_TO_SFTP.getCode(),
                    TITLE + "文件推送SFTP异常，apiCode：" + apiCode), e);
        } finally {
            try {
                sftpClient.disconnect();
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PUSH_TO_SFTP.getCode(),
                        TITLE + "文件推送SFTP关闭连接异常，apiCode：" + apiCode), e);
            }
        }
    }

}
