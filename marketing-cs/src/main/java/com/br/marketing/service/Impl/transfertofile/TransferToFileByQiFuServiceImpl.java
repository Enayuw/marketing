package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingCustomizeDataValidConfigMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.DynamicParameterServiceImpl;
import com.br.marketing.service.Impl.RuleRedisServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
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
public class TransferToFileByQiFuServiceImpl implements ITransferToFileService {

    @Autowired
    SyncConfigService syncConfigService;
    @Autowired
    DynamicParameterServiceImpl dynamicParameterService;
    @Autowired
    private TransferFileTaskMapper transferFileTaskMapper;
    @Autowired
    private TableCreateServiceImpl tableCreateService;
    @Autowired
    private RuleRedisServiceImpl ruleRedisService;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;
    @Resource
    private MarketingCustomizeDataValidConfigMapper customizeDataValidConfigMapper;
    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    private final static String TABLE_HEAD_TRANSFER = "custNum,applyDt,applyResult,loginTime,requestTime,userType,taskId,expireDate,effectiveDate";

    final static String EXECUTE_TIME = "12:00:00";

    final static String SUFFIX = "";

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
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode,String myParam) {
        List<TransferFileTask> resultList = new ArrayList<>();
        String extractTime = marketingCommonConfig.getQiFuExtDataConfig().get(apiCode).getString("extTime");
        String suffix = marketingCommonConfig.getQiFuExtDataConfig().get(apiCode).getString("suffix");
        LocalTime localTime = LocalTime.parse(extractTime);
        boolean isParam = StringUtils.isNotBlank(myParam);
        if (LocalTime.now().isAfter(localTime) || isParam) {
            // 指定日期提取，生成指定日期的记录，不是当天的记录
            String dateyyyymmddStr = isParam ? myParam.replace("-", "") : LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(date)
                    .andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("奇富360转化数据提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId, dateyyyymmddStr);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName(String.format("transform_qifu%s_%s.txt", suffix, dateyyyymmddStr));
                transferFileTask.setTaskNumber(0);
                transferFileTask.setStartDate(date);
                transferFileTask.setContextId(transferFileContextId);
                transferFileTask.setCreateTime(new Date());
                transferFileTask.setUpdateTime(new Date());
                transferFileTaskMapper.insertSelective(transferFileTask);
                resultList.add(transferFileTask);
            }

        }
        Result<List<TransferFileTask>> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());
        result.setDate(resultList);
        return result;
    }

    @Override
    public Result actionTransferToFile(TransferFileTask transferFileTask,String jobParameter) {
        log.warn("奇富360转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        Result<String> result = new Result<>();
        String apiCode = transferFileTask.getApiCode();
        String date = LocalDate.now().toString();
        date = date.replace("-", "");
        String requestDate = StringUtils.isBlank(jobParameter) ? LocalDate.now().minusDays(1).toString() : jobParameter;
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/")
                .concat(date).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            boolean mkdirs = writeDic.mkdirs();
            if (!mkdirs) {
                log.error(descPath + "目录创建失败！");
            }
        }
        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            fw.append(TABLE_HEAD_TRANSFER);
            fw.append("\r\n");
            writeQifuTransferToFile(fw, apiCode, transferFileTask, requestDate);
        } catch (Exception ex) {
            log.error("写入文件错误！",ex);
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage(ex.getMessage());
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    public void writeQifuTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask, String requestDate) {
        Long start = System.currentTimeMillis();
        AtomicInteger totalSize = new AtomicInteger(0);
        long timeout = 5L;
        List<MarketingCustomizeDataValidConfig> configList = getValidConfigs(apiCode, requestDate);
        if (CollectionUtils.isEmpty(configList)) {
            log.warn("奇富360转化数据提取-有效期配置表数据为空,apiCode = {},time = {}ms"
                    , apiCode, System.currentTimeMillis() - start);
            saveUpdateTask(transferFileTask, totalSize.intValue());
        }
        String tcId = tableCreateService.getTcId(apiCode);
        MarketingTransferSyncUser syncUser = new MarketingTransferSyncUser();
        syncUser.settCid(tcId);
        syncUser.setApiCode(apiCode);
        Integer pageSize = dynamicParameterService.getPageSize(null);
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(100, 100, 1);
        for (MarketingCustomizeDataValidConfig config : configList) {
            LocalDate startDate = LocalDate.parse(config.getValidStartDate(), YYYYMMDDSHORTLINE).minusDays(1);
            LocalDate endDate = LocalDate.parse(config.getValidEndDate(), YYYYMMDDSHORTLINE).plusDays(1);
            Integer page = 0;
            for (; ; ) {
                List<MarketingTransferSyncUser> transferData = marketingTransferSyncUserMapper
                        .getTransferByStartAndEndDate(syncUser, startDate.toString(), endDate.toString(), null, page * pageSize, pageSize);
                if (CollectionUtils.isEmpty(transferData)) {
                    break;
                }
                //有效期过滤
                List<MarketingTransferSyncUser> transferDataNew = filterTransferDataWithValPerd(apiCode, requestDate, transferData);
                page++;
                threadPool.submit(() -> {
                    writeDataForOneQuery(fw, totalSize, config, transferDataNew);
                });
            }
            threadPool.shutdown();
            try {
                while (!threadPool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                    if (log.isInfoEnabled()) {
                        long taskCount = threadPool.getTaskCount();
                        long completedTaskCount = threadPool.getCompletedTaskCount();
                        log.info("奇富360转化数据提取写入文件大约总任务数：{}；大约已完成任务数：{}；大约剩余任务数：{}"
                                , taskCount, completedTaskCount, taskCount - completedTaskCount);
                    }
                }
                saveUpdateTask(transferFileTask, totalSize.intValue());
                log.warn("奇富360转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}"
                        , apiCode, System.currentTimeMillis() - start, totalSize.intValue());
            } catch (InterruptedException e) {
                log.error("奇富360转化数据提取-本地文件生成失败！" , e);
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
                transferFileTaskMapper.deleteByPrimaryKey(transferFileTask.getId());
            }
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
     * @param config
     * @param transferDataNew
     */
    private static void writeDataForOneQuery(Writer fw, AtomicInteger totalSize, MarketingCustomizeDataValidConfig config, List<MarketingTransferSyncUser> transferDataNew) {
        for (MarketingTransferSyncUser transferFilterData : transferDataNew) {
            String custNum = transferFilterData.getCustNum();
            custNum = StringUtils.isNotEmpty(custNum) ? custNum : "";
            String applyDt = StringUtils.isNotEmpty(transferFilterData.getApplyDt())
                    ? transferFilterData.getApplyDt().replace(":000","") : "";
            String applyResult = StringUtils.isNotEmpty(transferFilterData.getApplyResult())
                    ? transferFilterData.getApplyResult() : "";
            String loginTime = StringUtils.isNotEmpty(transferFilterData.getLoginTime())
                    ? transferFilterData.getLoginTime().replace(":000","") : "";
            String requestTime = StringUtils.isNotEmpty(transferFilterData.getRequestTime())
                    ? transferFilterData.getRequestTime().replace(":000","") : "";
            String userType = StringUtils.isNotEmpty(transferFilterData.getUserType())
                    ? transferFilterData.getUserType() : "";
            //custNum,applyDt,applyResult,loginTime,requestTime,userType,taskId
            StringBuilder sb = new StringBuilder();
            sb.append(custNum.concat(","))
                    .append(applyDt.concat(","))
                    .append(applyResult.concat(","))
                    .append(loginTime.concat(","))
                    .append(requestTime.concat(","))
                    .append(userType.concat(","))
                    .append(config.getTaskId().concat(","))
                    .append(config.getValidEndDate().concat(","))
                    .append(config.getValidStartDate())
                    .append("\r\n");
            try {
                fw.append(sb.toString());
                totalSize.incrementAndGet();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 使用公共方法对转化数据做有效期过滤
     * @param apiCode
     * @param requestDate
     * @param transferData
     * @return
     */
    private List<MarketingTransferSyncUser> filterTransferDataWithValPerd(String apiCode, String requestDate, List<MarketingTransferSyncUser> transferData) {
        Set<String> custNumSet = transferData.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
        Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum =
                transferDataValidityPeriodService.getValidityPeriodsByCustNumAndTaskId(custNumSet, apiCode, LocalDate.parse(requestDate, YYYYMMDDSHORTLINE));
        transferData = transferData.stream().filter(data -> {
            String custNum = data.getCustNum();
            SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = validityPeriodsByCustNum.get(custNum);
            return syncUserValidityPeriodsBO != null;
        }).collect(Collectors.toList());
        return transferData;
    }

    private void saveUpdateTask(TransferFileTask transferFileTask, int totalSize) {
        TransferFileTask task = new TransferFileTask();
        task.setId(transferFileTask.getId());
        task.setFileName(transferFileTask.getFileName());
        task.setFilePath(transferFileTask.getFilePath());
        task.setStatus(2);
        task.setTaskNumber(totalSize);
        task.setUpdateTime(new Date());
        transferFileTaskMapper.updateByPrimaryKeySelective(task);
    }

    private String createBatchNumber(String apiCode, Long contextId, String dateStr) {
        return apiCode.concat("_").concat(dateStr).concat("_").concat(contextId.toString());
    }

    public static LocalDate[] getFirstAndLastDayOfMonth(LocalDate date) {
        LocalDate firstDayOfMonth;
        LocalDate lastDayOfMonth;
        if (date.getDayOfMonth() == 1) {
            firstDayOfMonth = date.minusMonths(1);
            lastDayOfMonth = date;
        } else {
            firstDayOfMonth = date.withDayOfMonth(1);
            lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth());
        }
        return new LocalDate[]{firstDayOfMonth, lastDayOfMonth};
    }

    public static LocalDate[] getStartAndEndDate(LocalDate date) {
        LocalDate firstDayOfMonth;
        LocalDate lastDayOfMonth;
        if (date.getDayOfMonth() == 1) {
            firstDayOfMonth = date.minusMonths(1);
            lastDayOfMonth = date;
        } else {
            firstDayOfMonth = date.withDayOfMonth(1);
            lastDayOfMonth = date.withDayOfMonth(date.lengthOfMonth());
            if (date.isBefore(lastDayOfMonth)) {
                lastDayOfMonth = date;
            }
        }
        return new LocalDate[]{firstDayOfMonth, lastDayOfMonth};
    }

}
