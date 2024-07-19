package com.br.marketing.service.Impl.transfertofile;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
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
import org.springframework.util.ObjectUtils;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
            List<Map<String, Object>> transferData = marketingTransferSyncUserMapper
                    .selectTransferWithValidtiflash_(transferSyncUser, pageSize);
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
     * 数据写入
     * @param fw
     * @param totalSize
     * @param transferDataNew
     */
    private static void writeDataForOneQuery(Writer fw,
                                             AtomicInteger totalSize,
                                             List<Map<String, Object>> transferDataNew) {
        for (Map<String, Object> data : transferDataNew) {
            String custNum = ObjectUtils.isEmpty(data.get("custNum")) ?
                    "" : String.valueOf(data.get("custNum"));
            String applyDt = ObjectUtils.isEmpty(data.get("applyDt")) ?
                    "" : String.valueOf(data.get("applyDt")).replace(":000","");
            String applyResult = ObjectUtils.isEmpty(data.get("applyResult")) ?
                    "" : String.valueOf(data.get("applyResult"));
            String loginTime = ObjectUtils.isEmpty(data.get("loginTime")) ?
                    "" : String.valueOf(data.get("loginTime")).replace(":000","");
            String requestTime = ObjectUtils.isEmpty(data.get("requestTime")) ?
                    "" : String.valueOf(data.get("requestTime")).replace(":000","");
            String userType = ObjectUtils.isEmpty(data.get("userType")) ?
                    "" : String.valueOf(data.get("userType"));
            String taskId = ObjectUtils.isEmpty(data.get("taskId")) ?
                    "" : String.valueOf(data.get("taskId"));
            String expireDate = ObjectUtils.isEmpty(data.get("expireDate")) ?
                    "" : String.valueOf(data.get("expireDate"));
            String effectiveDate = ObjectUtils.isEmpty(data.get("effectiveDate")) ?
                    "" : String.valueOf(data.get("effectiveDate"));
            StringBuilder sb = new StringBuilder();
            sb.append(custNum.concat(","))
                    .append(applyDt.concat(","))
                    .append(applyResult.concat(","))
                    .append(loginTime.concat(","))
                    .append(requestTime.concat(","))
                    .append(userType.concat(","))
                    .append(taskId.concat(","))
                    .append(expireDate.concat(","))
                    .append(effectiveDate)
                    .append("\r\n");
            try {
                fw.append(sb.toString());
                totalSize.incrementAndGet();
            } catch (IOException e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_QIFU_ALARM.getCode(),
                        custNum + "-奇富360转化数据提取VT-文件写入失败！"), e);
            }
        }
    }

}
