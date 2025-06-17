package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.Impl.DynamicParameterServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Author 贺东硕
 * @Date 2024/07/13 10:46
 * @Description:奇富360转化数据全量提取
 * 2024/07/09 https://c.100credit.cn/pages/viewpage.action?pageId=166648062
 */
@Slf4j
@Service
public class TransferToFileByQiFuFullServiceImpl extends AbstractTransferToFileByQiFuService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Autowired
    private TableCreateServiceImpl tableCreateService;
    @Autowired
    DynamicParameterServiceImpl dynamicParameterService;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Autowired
    private TransferFileTaskMapper transferFileTaskMapper;
    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        return "";
    }

    @Override
    String getExtractTime(String apiCode) {
        return marketingCommonConfig.getQiFuFullExtDataConfig().get(apiCode).getString("extTime");
    }

    @Override
    String getSuffix(String apiCode) {
        return marketingCommonConfig.getQiFuFullExtDataConfig().get(apiCode).getString("suffix");
    }

    @Override
    void writeQifuTransferToFile(Writer fw, String apiCode,
                                 TransferFileTask transferFileTask, String requestDate, Integer qiFuFullExtDataSoleNum) {
        Long start = System.currentTimeMillis();
        AtomicInteger totalSize = new AtomicInteger(0);
        long timeout = 5L;
        String tcId = tableCreateService.getTcId(apiCode);
        MarketingTransferSyncUser transferSyncUser = new MarketingTransferSyncUser();
        transferSyncUser.settCid(tcId);
        transferSyncUser.setApiCode(apiCode);
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(qiFuFullExtDataSoleNum, qiFuFullExtDataSoleNum, 1);
        for (; ; ) {
            List<Map<String, Object>> result = marketingTransferSyncUserMapper.selectFullTransferWithValid(transferSyncUser);
            if (CollectionUtils.isEmpty(result)) {
                break;
            }
            List<Map<String, Object>> extData = result.stream()
                    .collect(Collectors.
                            groupingBy((Map<String, Object> transfer) -> transfer.get("id").toString()))
                    .values()
                    .stream()
                    .map((List<Map<String, Object>> transfers) ->
                            transfers.stream().max(Comparator.comparing(transfer -> getEndDate(transfer))).orElse(null)
                    ).collect(Collectors.toList());
            List<Map<String, Object>> finalExtData = extData.stream()
                    .map((Map<String, Object> transfer) -> {
                        String id = transfer.get("id").toString();
                        try {
                            Object reserveFieldObj = transfer.get("reserveField1");
                            if (reserveFieldObj != null && StringUtils.isNotBlank(reserveFieldObj.toString())) {
                                JSONObject reserveField1JSON = JSON.parseObject(reserveFieldObj.toString());
                                String expireTime = reserveField1JSON.containsKey("expireDate") ?
                                        reserveField1JSON.getString("expireDate") : "";
                                String effectiveTime = reserveField1JSON.containsKey("effectiveDate") ?
                                        reserveField1JSON.getString("effectiveDate") : "";
                                String expireDate = dateFormat(expireTime);
                                String effectiveDate = dateFormat(effectiveTime);
                                transfer.put("expireDate",expireDate);
                                transfer.put("effectiveDate",effectiveDate);
                            }else {
                                transfer.put("expireDate","");
                                transfer.put("effectiveDate","");
                            }
                        } catch (Exception e) {
                            log.warn(apiCode + "-奇富360转化数据JSON处理异常-id=" + id, e);
                        }
                        return transfer;
                    }).collect(Collectors.toList());
            Map<String, Object> minIdData = finalExtData.stream()
                    .max(Comparator.comparing((Map<String, Object> map) ->
                            Long.parseLong(String.valueOf(map.get("id"))))).orElse(null);
            long minId = Long.parseLong(String.valueOf(minIdData.get("id"))) + 1;
            transferSyncUser.setId(minId);
            threadPool.submit(() -> {
                writeDataForOneQuery(fw, totalSize, finalExtData);
            });
        }
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                if (log.isInfoEnabled()) {
                    long taskCount = threadPool.getTaskCount();
                    long completedTaskCount = threadPool.getCompletedTaskCount();
                    log.warn("奇富360转化数据提取写入文件大约总任务数：{}；大约已完成任务数：{}；大约剩余任务数：{}"
                            , taskCount, completedTaskCount, taskCount - completedTaskCount);
                }
            }
            saveUpdateTask(transferFileTask, totalSize.intValue());
            log.warn("奇富360转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}"
                    , apiCode, System.currentTimeMillis() - start, totalSize.intValue());
        } catch (InterruptedException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_QIFU_ALARM.getCode(),
                    apiCode + "-奇富360转化数据提取-本地文件生成失败！"), e);
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
            transferFileTaskMapper.deleteByPrimaryKey(transferFileTask.getId());
        }
    }

    private String getEndDate(Map<String, Object> transfer) {
        try {
            String reserveField1 = ObjectUtils.isEmpty(transfer.get("reserveField1")) ?
                    "" : transfer.get("reserveField1").toString();
            JSONObject reserveField1JSON = JSON.parseObject(reserveField1);
            String expireTime = reserveField1JSON.containsKey("expireDate") ?
                    reserveField1JSON.getString("expireDate") : "";
            return expireTime;
        } catch (Exception e) {
            return "";
        }
    }

    private void writeDataForOneQuery(Writer fw,
                                      AtomicInteger totalSize,
                                      List<Map<String, Object>> result){

        // 获取表头
        String[] headers = marketingCommonConfig.getQiFuTransferTableHead().split(",");

        // 处理每条记录
        for (Map<String, Object> data : result) {
            String custNum = data.get("custNum") != null ? data.get("custNum").toString() : "";

            try {
                // 合并字段（原始数据+JSON字段）
                Map<String, String> fieldMap = new LinkedHashMap<>();

                // 处理JSON字段
                if (data.get("transferReserveField1") != null) {
                    try {
                        JSONObject json = JSONObject.parseObject(data.get("transferReserveField1").toString());
                        for (String key : json.keySet()) {
                            fieldMap.put(key, json.getString(key) != null ? json.getString(key) : "");
                        }
                    } catch (Exception e) {
                        log.warn("奇富360转化数据提取-transferReserveField1解析失败", e);
                    }
                }

                if (data.get("reserveField1") != null) {
                    try {
                        JSONObject json = JSONObject.parseObject(data.get("reserveField1").toString());
                        for (String key : json.keySet()) {
                            fieldMap.put(key, json.getString(key) != null ? json.getString(key) : "");
                        }
                    } catch (Exception e) {
                        log.warn("奇富360转化数据提取-reserveField1解析失败", e);
                    }
                }

                // 添加原始数据
                for (String key : data.keySet()) {
                    fieldMap.put(key, data.get(key) != null ? data.get(key).toString() : "");
                }

                // 特殊处理时间字段
                String[] timeFields = {"applyDt", "loginTime", "requestTime"};
                for (String field : timeFields) {
                    if (fieldMap.containsKey(field)) {
                        fieldMap.put(field, fieldMap.get(field).replace(":000", ""));
                    }
                }

                // 构建CSV行
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < headers.length; i++) {
                    String value = fieldMap.getOrDefault(headers[i].trim(), "");
                    line.append(value);
                    if (i < headers.length - 1) line.append(",");
                }
                line.append("\r\n");

                // 写入文件
                fw.write(line.toString());
                totalSize.incrementAndGet();

            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(
                        AlarmSendCodeEnum.EXCEPTION_QIFU_ALARM.getCode(),
                        custNum + "-奇富360转化数据提取-文件写入失败！"), e);
            }
        }
    }

    /**
     * 日期格式化
     * @return
     */
    public String dateFormat(String DateStr) throws ParseException {
        if (StringUtils.isEmpty(DateStr)) {
            return "";
        }
        SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date parse = formate.parse(DateStr);
        SimpleDateFormat sdf = new SimpleDateFormat("", Locale.SIMPLIFIED_CHINESE);
        sdf.applyPattern("yyyy-MM-dd");
        return sdf.format(parse);
    }

}
