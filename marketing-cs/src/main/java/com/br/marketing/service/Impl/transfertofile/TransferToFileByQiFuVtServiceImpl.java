package com.br.marketing.service.Impl.transfertofile;

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
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Author 贺东硕
 * @Date 2024/07/18 09:46
 * @Description:奇富360转化数据提取VT
 * 2024/07/09 https://c.100credit.cn/pages/viewpage.action?pageId=166648062
 */
@Slf4j
@Service
public class TransferToFileByQiFuVtServiceImpl extends AbstractTransferToFileByQiFuService {

    @Autowired
    SyncConfigService syncConfigService;
    @Autowired
    DynamicParameterServiceImpl dynamicParameterService;
    @Autowired
    private TransferFileTaskMapper transferFileTaskMapper;
    @Autowired
    private TableCreateServiceImpl tableCreateService;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    private final static String CUST_NUM = "cust_num";

    private final static String CELL_MD5 = "cell_md5";

    /**
     * 2023-12-11 10:50
     * 指定日期提取参数格式：
     * apiCode#yyyy-MM-dd
     * eg:7492900#2023-05-09
     */
    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        if (jobParameter.contains(apiCode)) {
            String[] split = jobParameter.split(";");
            for (String s : split) {
                if (s.contains(apiCode)) {
                    return s.split("#")[1];
                }
            }
        }
        return "";
    }

    @Override
    String getExtractTime(String apiCode) {
        return marketingCommonConfig.getQiFuExtDataConfig().get(apiCode).getString("extTime");
    }

    @Override
    String getSuffix(String apiCode) {
        return marketingCommonConfig.getQiFuExtDataConfig().get(apiCode).getString("suffix");
    }

    @Override
    public void writeQifuTransferToFile(Writer fw, String apiCode,
                                        TransferFileTask transferFileTask, String requestDate, Integer qiFuFullExtDataSoleNum) {
        Long start = System.currentTimeMillis();
        AtomicInteger totalSize = new AtomicInteger(0);
        long timeout = 5L;
        String tcId = tableCreateService.getTcId(apiCode);
        MarketingTransferSyncUser transferSyncUser = new MarketingTransferSyncUser();
        transferSyncUser.settCid(tcId);
        transferSyncUser.setApiCode(apiCode);
        transferSyncUser.setRequestData(requestDate);
        Integer pageSize = marketingCommonConfig.getQiFuExtDataConfig().get(apiCode).getInteger("pageSize") == null ?
                10000 : marketingCommonConfig.getQiFuExtDataConfig().get(apiCode).getInteger("pageSize");
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(qiFuFullExtDataSoleNum, qiFuFullExtDataSoleNum, 1);
        for (; ; ) {
            List<Map<String, Object>> transferData = getTransferData(transferSyncUser, pageSize);
            if (CollectionUtils.isEmpty(transferData)) {
                break;
            }
            List<Map<String, Object>> extData = transferData.stream()
                    .collect(Collectors.
                            groupingBy((Map<String, Object> transfer) -> transfer.get("id").toString()))
                    .values()
                    .stream()
                    .map((List<Map<String, Object>> transfers) ->
                            transfers.stream().max(Comparator.comparing(transfer ->
                                    transfer.getOrDefault("expireDate", "").toString())).orElse(null)
                    ).collect(Collectors.toList());
            Map<String, Object> minIdData = transferData.stream()
                    .max(Comparator.comparing((Map<String, Object> map) ->
                            Long.parseLong(String.valueOf(map.get("id"))))).orElse(null);
            long minId = Long.parseLong(String.valueOf(minIdData.get("id"))) + 1;
            transferSyncUser.setId(minId);
            threadPool.submit(() -> {
                writeDataForOneQuery(fw, totalSize, extData);
            });

        }
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                if (log.isInfoEnabled()) {
                    long taskCount = threadPool.getTaskCount();
                    long completedTaskCount = threadPool.getCompletedTaskCount();
                    log.warn("奇富360转化数据提取VT写入文件大约总任务数：{}；大约已完成任务数：{}；大约剩余任务数：{}"
                            , taskCount, completedTaskCount, taskCount - completedTaskCount);
                }
            }
            saveUpdateTask(transferFileTask, totalSize.intValue());
            log.warn("奇富360转化数据提取VT-本地文件生成成功,apiCode = {},time = {}ms,total = {}"
                    , apiCode, System.currentTimeMillis() - start, totalSize.intValue());
        } catch (InterruptedException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_QIFU_ALARM.getCode(),
                    apiCode + "-奇富360转化数据提取VT-本地文件生成失败！"), e);
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
            transferFileTaskMapper.deleteByPrimaryKey(transferFileTask.getId());
        }
    }

    /**
     * @description 查询数据
     * @param transferSyncUser
     * @param pageSize
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @author hedongshuo
     * @date 2024/8/15 14:01
     **/
    private List<Map<String, Object>> getTransferData(MarketingTransferSyncUser transferSyncUser, Integer pageSize) {
        String apiCode = transferSyncUser.getApiCode();
        String custNumMapping = CUST_NUM;
        HashMap<String, List<String>> config = marketingCommonConfig.getQiFuExtDataCustNumMapConfig();
        Iterator<Map.Entry<String, List<String>>> iterator = config.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<String>> entry = iterator.next();
            List<String> apiCodes = entry.getValue();
            if (apiCodes.contains(apiCode)) {
                custNumMapping = entry.getKey();
                break;
            }
        }
        List<Map<String, Object>> transferData = null;
        if (CUST_NUM.equals(custNumMapping)) {
            transferData = marketingTransferSyncUserMapper
                    .selectTransferWithValidtiflash_(transferSyncUser, pageSize);
        } else if (CELL_MD5.equals(custNumMapping)) {
            transferData = marketingTransferSyncUserMapper
                    .selectTransferWithValidByCelltiflash_(transferSyncUser, pageSize);
        }
        return transferData;
    }

    /**
     * 数据写入
     * @param fw
     * @param totalSize
     * @param transferDataNew
     */
    private void writeDataForOneQuery(Writer fw,
                                      AtomicInteger totalSize,
                                      List<Map<String, Object>> transferDataNew){
        // 获取表头
        String[] headers = marketingCommonConfig.getQiFuTransferTableHead().split(",");

        // 处理每条记录
        for (Map<String, Object> data : transferDataNew) {
            String custNum = data.get("custNum") != null ? data.get("custNum").toString() : "";

            try {
                // 合并字段（原始数据+JSON字段）
                Map<String, String> fieldMap = new LinkedHashMap<>();

                // 处理JSON字段
                if (data.get("reserveField1") != null) {
                    try {
                        JSONObject json = JSONObject.parseObject(data.get("reserveField1").toString());
                        for (String key : json.keySet()) {
                            fieldMap.put(key, json.getString(key) != null ? json.getString(key) : "");
                        }
                    } catch (Exception e) {
                        log.warn("reserveField1解析失败", e);
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
                        custNum + "-奇富360转化数据提取VT-文件写入失败！"), e);
            }
        }
    }

}
