package com.br.marketing.service.mark.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.FlagData;
import com.br.marketing.entity.FlagDataExample;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.enums.EsSyncStatusEnum;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.mark.DataMarkCommonService;
import com.br.marketing.service.mark.DataWriteBackFileMarkService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
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
    public void process() {
        marketingCommonConfig.getDataMarkApiCodes().forEach((String apiCode) -> {
            if (checkEsStatus(apiCode)) {
                // 同步数据写入doris
                syncData(apiCode);
                // 推送文件至SFTP
                pushFileSftp(apiCode);
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

        StraHisFile straHisFile = dataMarkCommonService.getStraHisFile(apiCode);
        if (null == straHisFile) {
            return;
        }
        String batchNumber = straHisFile.getBatchNumber();
        Integer threadPoolSize = marketingCommonConfig.getDataMarkThreadNum();
        int dataMarkPageSize = marketingCommonConfig.getDataMarkPageSize() == null ? 2000 : marketingCommonConfig.getDataMarkPageSize();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);
        try {
            Long minId = null;
            boolean isContiue = Boolean.TRUE;
            while (isContiue) {
                // 分页查询打标数据
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
                threadPool.submit(() -> writeBackFileMark(flagDataList,apiCode,batchNumber));
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

    @RetryMethod(retryNowNum = 3, isOrNoDbRetry = true)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    private void writeBackFileMark(List<FlagData> flagDataList, String apiCode,String batchNumber) {
        // 写入doris
        List<Map<String, Object>> dataList = insertMarkData(flagDataList,batchNumber);
        // 写入文件
        writeDataToFile(dataList, apiCode);
    }

    private List<Map<String, Object>> insertMarkData(List<FlagData> flagDataList,String batchNumber) {
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

            flagDataResult.forEach((Map<String, Object> resultMap) -> {
                FlagData flagData = groupByCellMd5.get(resultMap.get("cell"));
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
            List<String> columnNames = new ArrayList<>(flagDataResult.get(0).keySet());
            // 构建批量插入语句
            List<String> valueClauses = flagDataResult.stream()
                    .map(resultMap -> "(" + columnNames.stream()
                            .map(columnName -> resultMap.get(columnName) != null ? "'" + resultMap.get(columnName).toString().replace("'", "''") + "'" : "NULL")
                            .collect(Collectors.joining(", ")) + ")")
                    .collect(Collectors.toList());

            String tableName = marketingCommonConfig.getDataMarkTableName();
            String batchInsertSql = "INSERT INTO " + tableName.replace("\"","") + " (" + String.join(", ", columnNames) + ") VALUES " + String.join(", ", valueClauses);
            // 写入doris
            flagDataMapper.insertbI_(batchInsertSql);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    TITLE + "出现异常，" + "errorMessage=" + e.getMessage()), e);
        }
        return flagDataResult;
    }

    private void writeDataToFile(List<Map<String, Object>> dataList,String apiCode) {
        if(CollectionUtil.isEmpty(dataList)){
            return;
        }
        String syncDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String descPath = syncConfigService.getPath().concat("ppMarkToFile/").concat(apiCode).concat("/").concat(syncDate).concat("/");
        String fileName = "pp_"+apiCode+"_"+syncDate+".txt";
        String fileAllPath = descPath.concat(fileName);
        File file = new File(descPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File decodeFile = new File(descPath.concat(fileName));
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(decodeFile, true),  StandardCharsets.UTF_8))) { // 修改为追加模式
            // 判断是否添加文件头
            checkHeaderExists(fileAllPath, dataList, writer);
            // 写入每行数据
            for (Map<String, Object> resultMap : dataList) {
                StringBuilder row = new StringBuilder();
                Collection<Object> values = resultMap.values();
                for (Object v : values){
                    if(v == null){
                        row.append(",");
                    }else {
                        row.append(v).append(",");
                    }
                }
                // 写入数据行
                writer.append(row.toString()).append(System.lineSeparator());
            }
        }catch (IOException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                    TITLE + "写入文件时发生异常"), e);
        }
    }

    private void checkHeaderExists(String filePath, List<Map<String, Object>> dataList,
                                   Writer writer) throws IOException {
        FileReader read = new FileReader(filePath);
        BufferedReader br = new BufferedReader(read);
        String firstLine = br.readLine();
        if (firstLine == null) {
            List<String> columnNames = new ArrayList<>(dataList.get(0).keySet());
            String fileHeader = String.join(",", columnNames);
            writer.append(fileHeader).append(System.lineSeparator());
        }
    }

    /**
     * 推送文件至SFTP
     */
    private void pushFileSftp(String apiCode) {
        SftpClient sftpClient = new SftpClient(sftpHost, sftpPort, sftpUsername, sftpPwd);
        String syncDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String descPath = syncConfigService.getPath().concat("ppMarkToFile/").concat(apiCode).concat("/").concat(syncDate).concat("/");
        String fileName = "pp_"+apiCode+"_"+syncDate+".txt";
        String fileAllPath = descPath.concat(fileName);
        log.warn(TITLE + "推送文件路径：" + fileAllPath);
        try {
            sftpClient.connect();
            sftpClient.uploadFile("/UploadFiles/marketing/" + apiCode + "/output/" + syncDate, fileName, fileAllPath);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PUSH_TO_SFTP.getCode(),
                    TITLE + "文件推送SFTP异常，apiCode：" + apiCode), e);
        } finally {
            try {
                sftpClient.disconnect();
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PUSH_TO_SFTP.getCode(),
                        TITLE + "文件推送SFTP关闭连接异常，apiCode："+ apiCode), e);
            }
        }
    }

}
