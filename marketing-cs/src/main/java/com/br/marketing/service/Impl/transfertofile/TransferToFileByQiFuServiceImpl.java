package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingCustomizeDataValidConfigMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.Impl.DynamicParameterServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Author 李广秀
 * @Date 2024/01/04 10:31
 * @Description:奇富360转化数据提取
 * 2024/07/09 https://c.100credit.cn/pages/viewpage.action?pageId=166648062
 */
@Slf4j
@Service
public class TransferToFileByQiFuServiceImpl extends AbstractTransferToFileByQiFuService {

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
    @Resource
    private MarketingCustomizeDataValidConfigMapper customizeDataValidConfigMapper;
    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    final static DateTimeFormatter YYYYMMDDSHORTLINE = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_FORMAT);

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
        List<MarketingCustomizeDataValidConfig> configList = getValidConfigs(apiCode, requestDate);
        if (CollectionUtils.isEmpty(configList)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_QIFU_ALARM.getCode(),
                    apiCode + "-奇富360转化数据提取-有效期配置表数据为空,apiCode！"));
            saveUpdateTask(transferFileTask, totalSize.intValue());
        }
        String validStartDate = configList.stream()
                .min(Comparator.comparing(MarketingCustomizeDataValidConfig::getValidStartDate)).get().getValidStartDate();
        String validEndDate = configList.stream()
                .max(Comparator.comparing(MarketingCustomizeDataValidConfig::getValidEndDate)).get().getValidEndDate();
        String tcId = tableCreateService.getTcId(apiCode);
        MarketingTransferSyncUser syncUser = new MarketingTransferSyncUser();
        syncUser.settCid(tcId);
        syncUser.setApiCode(apiCode);
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(qiFuFullExtDataSoleNum, qiFuFullExtDataSoleNum, 1);
        LocalDate startDate = LocalDate.parse(validStartDate, YYYYMMDDSHORTLINE).minusDays(1);
        LocalDate endDate = LocalDate.parse(validEndDate, YYYYMMDDSHORTLINE).plusDays(1);
        for (; ; ) {
            List<MarketingTransferSyncUser> transferData = marketingTransferSyncUserMapper
                    .selectTransferWithValid(syncUser, startDate.toString(), endDate.toString());
            if (CollectionUtils.isEmpty(transferData)) {
                break;
            }
            Long minId = transferData.get(transferData.size() - 1).getId() + 1;
            syncUser.setId(minId);
            //有效期过滤
            List<List<MarketingTransferSyncUser>> partitionList = Lists.partition(transferData, 50);
            partitionList.forEach((List<MarketingTransferSyncUser> list) -> {
                Set<String> custNumSet = list.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
                threadPool.submit(() -> {
                    Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum =
                            transferDataValidityPeriodService.getValidityPeriodsByCustNumAndTaskId(custNumSet, apiCode,
                                    LocalDate.parse(requestDate, YYYYMMDDSHORTLINE));
                    List<MarketingTransferSyncUser> transferDataNew = list.stream().filter((MarketingTransferSyncUser transfer) -> {
                        String custNum = transfer.getCustNum();
                        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = validityPeriodsByCustNum.get(custNum);
                        return syncUserValidityPeriodsBO != null;
                    }).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(transferDataNew)) {
                        writeDataForOneQuery(fw, totalSize, transferDataNew, validityPeriodsByCustNum);
                    }
                });
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

    /**
     * 获取有效期配置
     * @param apiCode
     * @param requestDate
     * @return
     */
    private List<MarketingCustomizeDataValidConfig> getValidConfigs(String apiCode, String requestDate) {
        MarketingCustomizeDataValidConfigExample example = new MarketingCustomizeDataValidConfigExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo(1)
                .andValidStartDateLessThanOrEqualTo(requestDate)
                .andValidEndDateGreaterThanOrEqualTo(requestDate);
        List<MarketingCustomizeDataValidConfig> configList = customizeDataValidConfigMapper.selectByExample(example);
        return configList;
    }

    /**
     * 数据写入
     * @param fw
     * @param totalSize
     * @param transferDataNew
     * @param validityPeriodsByCustNum
     */
    private void writeDataForOneQuery(Writer fw,
                                             AtomicInteger totalSize,
                                             List<MarketingTransferSyncUser> transferDataNew,
                                             Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum) {
        // 解析文件头字段
        String qiFuTransferTableHead = marketingCommonConfig.getQiFuTransferTableHead();
        String[] headers = qiFuTransferTableHead.split(",");
        String custNum ="";
        for (MarketingTransferSyncUser transferFilterData : transferDataNew) {
            try {
                // 获取所有可能需要的字段
                String reserveField1 = transferFilterData.getReserveField1();
                JSONObject object = StringUtils.isBlank(reserveField1) ? null : JSONObject.parseObject(reserveField1);

                custNum = transferFilterData.getCustNum();
                custNum = StringUtils.isNotEmpty(custNum) ? custNum : "";

                // 构建字段映射
                Map<String, String> fieldMap = new LinkedHashMap<>();

                // 处理固定字段
                fieldMap.put("custNum", custNum);
                fieldMap.put("applyDt", StringUtils.isNotEmpty(transferFilterData.getApplyDt())
                        ? transferFilterData.getApplyDt().replace(":000", "") : "");
                fieldMap.put("applyResult", StringUtils.isNotEmpty(transferFilterData.getApplyResult())
                        ? transferFilterData.getApplyResult() : "");
                fieldMap.put("loginTime", StringUtils.isNotEmpty(transferFilterData.getLoginTime())
                        ? transferFilterData.getLoginTime().replace(":000", "") : "");
                fieldMap.put("requestTime", StringUtils.isNotEmpty(transferFilterData.getRequestTime())
                        ? transferFilterData.getRequestTime().replace(":000", "") : "");
                fieldMap.put("userType", StringUtils.isNotEmpty(transferFilterData.getUserType())
                        ? transferFilterData.getUserType() : "");

                // 处理需要计算的字段
                if (validityPeriodsByCustNum.containsKey(custNum)) {
                    MarketingSyncUser marketingSyncUser = validityPeriodsByCustNum.get(custNum).getSyncUsers().get(0);
                    PeriodOfValidityBO bo = validityPeriodsByCustNum.get(custNum).getBuilders().get(0).builder();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                    fieldMap.put("taskId", marketingSyncUser.getCusBatch());
                    fieldMap.put("expireDate", simpleDateFormat.format(bo.getEnDate()));
                    fieldMap.put("effectiveDate", simpleDateFormat.format(bo.getBeginDate()));
                } else {
                    fieldMap.put("taskId", "");
                    fieldMap.put("expireDate", "");
                    fieldMap.put("effectiveDate", "");
                }

                // 处理JSON中的字段
                if (object != null) {
                    for (String key : object.keySet()) {
                        fieldMap.put(key, object.getString(key));
                    }
                }

                // 根据文件头顺序构建行
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < headers.length; i++) {
                    String header = headers[i].trim();
                    if (fieldMap.containsKey(header)) {
                        sb.append(fieldMap.get(header));
                    } else {
                        // 如果字段不存在，填充空值
                        sb.append("");
                    }

                    if (i < headers.length - 1) {
                        sb.append(",");
                    }
                }
                sb.append("\r\n");

                // 写入文件
                fw.append(sb.toString());
                totalSize.incrementAndGet();

            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_QIFU_ALARM.getCode(),
                        custNum + "-奇富360转化数据提取-文件写入失败！"), e);
            }
        }
    }
}
