package com.br.marketing.service.mark.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.FlagData;
import com.br.marketing.entity.FlagDataExample;
import com.br.marketing.enums.EsSyncStatusEnum;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.mark.DataWriteBackFileMarkService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    FlagDataMapper flagDataMapper;
    private static final String fileName = "test0220.txt";
    private static final String path = "C:\\Users\\bingxu.kong\\Desktop\\test0220";
    private static final String remotePath = "远程服务器地址";
    private static final String TITLE = "【pp停车文件数据回写】";

    @Override
    public void process() {
        marketingCommonConfig.getDataMarkApiCodes().forEach((String apiCode) -> {
            if (checkEsStatus(apiCode)) {
                // 同步数据写入doris
                syncData(apiCode);
                // 推送文件至SFTP
                pushFileSftp();
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
                .andAppletDateEqualTo(new Date())
                .andEsSyncStatusNotEqualTo(EsSyncStatusEnum.COMPLETE.getValue());
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
    private void syncData(String apiCode) {
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(5, 5);
        try {
            Files.createDirectories(Paths.get(path));
            Path filePath = Paths.get(path, fileName);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(filePath), StandardCharsets.UTF_8));

            Long minId = null;
            boolean isContiue = Boolean.TRUE;
            while (isContiue) {
                // 分页查询打标数据
                FlagDataExample flagDataExample = new FlagDataExample();
                flagDataExample.setOrderByClause("id limit 2000");

                FlagDataExample.Criteria criteria = flagDataExample.createCriteria()
                        .andApiCodeEqualTo(apiCode)
                        .andAppletDateEqualTo(new Date())
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
                threadPool.submit(() -> {
                    // 写入doris
                    List<Map<String, Object>> dataList = insertMarkData(flagDataList);
                    // 写入文件
                    writeDataToFile(dataList, writer);
                });
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

    private List<Map<String, Object>> insertMarkData(List<FlagData> flagDataList) {
        List<Map<String, Object>> flagDataResult = new ArrayList<>();
        try {
            List<String> cellMd5List = flagDataList.stream().map(FlagData::getCellMd5).collect(Collectors.toList());

            Map<String, FlagData> groupedByCellMd5 = flagDataList.stream()
                    .collect(Collectors.toMap(FlagData::getCellMd5, data -> data, (oldValue, newValue) -> newValue));

            // 根据cells查询doris数据
            String cell = cellMd5List.stream()
                    .map(md5 -> "'" + md5 + "'")
                    .collect(Collectors.joining(","));

            String scoreSql = "select * from b_score_".concat("batchNumber ").concat("where md5_phone in(").concat(cell).concat(")");
            flagDataResult = flagDataMapper.queryDataByCellbI_(scoreSql);
            flagDataResult.forEach((Map<String, Object> resultMap) -> {
                FlagData flagData = groupedByCellMd5.get(resultMap.get("md5_phone"));
                resultMap.put("flag_new_cust",flagData.getFlagNewCust());
                resultMap.put("flag_riskgroup",flagData.getFlagRiskgroup());
                resultMap.put("flag_interest",flagData.getFlagInterest());
                resultMap.put("flag_age",flagData.getFlagAge());
                resultMap.put("flag_province",flagData.getFlagProvince());
                resultMap.put("flag_special_small",flagData.getFlagSpecialSmall());
                resultMap.put("flag_specialrisklevel_rule",flagData.getFlagSpecialrisklevelRule());
                resultMap.put("flag_indexcs",flagData.getFlagIndexcs());
                resultMap.put("flag_applyloan",flagData.getFlagApplyloan());
                resultMap.put("flag_intellaudio_blacklist",flagData.getFlagIntellaudioBlacklist());
                resultMap.put("flag_without_willingness",flagData.getFlagWithoutWillingness());
                resultMap.put("flag_score_whitelist",flagData.getFlagScoreWhitelist());
                resultMap.put("flag_whitelist",flagData.getFlagWhitelist());
            });
            List<String> columnNames = new ArrayList<>(flagDataResult.get(0).keySet());
            // 构建批量插入语句
            List<String> valueClauses = flagDataResult.stream()
                    .map(resultMap -> "(" + columnNames.stream()
                            .map(columnName -> resultMap.get(columnName) != null ? "'" + resultMap.get(columnName).toString().replace("'", "''") + "'" : "NULL")
                            .collect(Collectors.joining(", ")) + ")")
                    .collect(Collectors.toList());
            String batchInsertSql = "INSERT INTO ods_flag_data" + " (" + String.join(", ", columnNames) + ") VALUES " + String.join(", ", valueClauses);
            // 写入doris
            flagDataMapper.insertbI_(batchInsertSql);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    TITLE + "出现异常，" + "errorMessage=" + e.getMessage()), e);
        }
        return flagDataResult;
    }

    private void writeDataToFile(List<Map<String, Object>> dataList, BufferedWriter writer) {
        try {
            if (CollectionUtil.isEmpty(dataList)) {
                return;
            }
            // 检查文件是否已经存在文件头
            boolean headerExists = checkHeaderExists(path+"/"+fileName, dataList);
            // 如果文件头不存在，则写入文件头
            if (!headerExists) {
                List<String> columnNames = new ArrayList<>(dataList.get(0).keySet());
                String fileHeader = String.join(",", columnNames);
                writer.append(fileHeader).append(System.lineSeparator());
            }

            // 写入每行数据
            for (Map<String, Object> resultMap : dataList) {
                String row = resultMap.values().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(","));
                // 写入数据行
                writer.append(row).append(System.lineSeparator());
            }
        } catch (IOException e) {
            System.err.println(TITLE + "写入文件时发生异常: " + e.getMessage());
        }
    }

    private boolean checkHeaderExists(String filePath, List<Map<String, Object>> dataList) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.notExists(path)) {
            return false; // 文件不存在
        }
        // 读取文件的第一行
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String firstLine = reader.readLine();
            if (firstLine == null) {
                return false; // 文件为空
            }
            // 获取预期的文件头
            List<String> columnNames = new ArrayList<>(dataList.get(0).keySet());
            String expectedHeader = String.join(",", columnNames);
            // 比较文件的第一行和预期的文件头
            return firstLine.equals(expectedHeader);
        }
    }

    /**
     * 推送文件至SFTP
     */
    private void pushFileSftp() {
        SftpClient sftpClient = new SftpClient(sftpHost, sftpPort, sftpUsername, sftpPwd);
        try {
            sftpClient.uploadFile(remotePath, fileName, path+"/"+fileName);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PUSH_TO_SFTP.getCode(),
                    TITLE + "文件推送SFTP异常，apiCode："), e);
        } finally {
            try {
                sftpClient.disconnect();
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PUSH_TO_SFTP.getCode(),
                        TITLE + "文件推送SFTP异常，apiCode："), e);
            }
        }
    }

}
