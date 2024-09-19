package com.br.marketing.service.Impl.transfertofile;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
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
 * D20240828*国美转化数据提取-3710076
 * @author guangxiu.li
 * @date 2024/9/19 17:22 
 */
@Slf4j
@Service
public class TransferToFileByGuoMeiServiceImpl implements ITransferToFileService {

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
    private MarketingDataValidConfigMapper marketingDataValidConfigMapper;
    @Resource
    private TransferDataValidityPeriodService validityPeriodService;

    public final static String TABLE_HEAD_TRANSFER = "group,userId,registrationDate,isLogin,loginTime,isApplyCredit,applyCreditTime,isCreditPass" +
            ",creditPassTime,creditAmount,isApplyWithdrawals,withdrawalsTime,isRiskPass,riskPassAmount,lendersDate,lendersAmount,customName";

    final static String EXECUTE_TIME = "09:00:00";


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
        String extractTime = StringUtils.isBlank(marketingCommonConfig.getGuoMeiTransferFileExecuteTime())
                ? EXECUTE_TIME : marketingCommonConfig.getGuoMeiTransferFileExecuteTime();
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
                log.warn("国美转化数据提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId, dateyyyymmddStr);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName(String.format("transform_qifujuxin_%s.txt", dateyyyymmddStr));
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
        log.warn("国美转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        Result<String> result = new Result<>();
        String apiCode = transferFileTask.getApiCode();
        String date = LocalDate.now().toString();
        date = date.replace("-", "");
        String requestDate = StringUtils.isBlank(jobParameter) ? LocalDate.now().toString() : jobParameter;
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/")
                .concat(date).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            boolean mkdirs = writeDic.mkdirs();
            if (!mkdirs) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.GUOMEI_SERVICEERROR.getCode()
                        , descPath + "目录创建失败！"));
            }
        }
        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            fw.append(TABLE_HEAD_TRANSFER);
            fw.append("\r\n");
            writeGuoMeiTransferToFile(fw, apiCode, transferFileTask, requestDate);
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.GUOMEI_SERVICEERROR.getCode()
                    , "国美转化数据提取写入文件错误！"), ex);
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage(ex.getMessage());
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    public void writeGuoMeiTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask, String requestDate) {
        long start = System.currentTimeMillis();
        AtomicInteger totalSize = new AtomicInteger(0);
        long timeout = 5L;
        String tcId = tableCreateService.getTcId(apiCode);
        LocalDate localDate = LocalDate.parse(requestDate, YYYYMMDDSHORTLINE);
        String appletDate = LocalDate.parse(requestDate, YYYYMMDDSHORTLINE).minusDays(1).toString();
        LocalDate startDate = LocalDate.parse(requestDate, YYYYMMDDSHORTLINE).minusDays(31);
        LocalDate endDate = localDate;
        Boolean continueFlag = Boolean.TRUE;
        // 创建线程池
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(20, 20, 100);
        Integer pageSize = null;
        List<MarketingDataValidConfig> validityDataByApiCode = marketingDataValidConfigMapper.getValidityDataByApiCode(apiCode, appletDate);
        if (validityDataByApiCode.size() <= 0){
            log.warn("国美有效期列表可能为空");
            continueFlag = Boolean.FALSE;
        }
        Optional<MarketingDataValidConfig> minDateConfig = validityDataByApiCode.stream()
                .min(Comparator.comparing(MarketingDataValidConfig::getValidStartDate));
        if (minDateConfig.isPresent()) {
            startDate = LocalDate.parse(minDateConfig.get().getValidStartDate(), YYYYMMDDSHORTLINE);
        } else {
            log.warn("列表为空，无法获取最小的startDate");
        }
        Optional<MarketingDataValidConfig> maxDateConfig = validityDataByApiCode.stream()
                .max(Comparator.comparing(MarketingDataValidConfig::getValidEndDate));
        if (maxDateConfig.isPresent()) {
            endDate = LocalDate.parse(maxDateConfig.get().getValidEndDate(), YYYYMMDDSHORTLINE);
            if (endDate.isBefore(localDate) || endDate.isEqual(localDate)){
                endDate = localDate.plusDays(1);
            }
        } else {
            log.warn("列表为空，无法获取最大的ValidEndDate");
        }

        Long beginId = marketingTransferSyncUserMapper.getMinIdByTime(apiCode, startDate, endDate, tcId);
        Long endId = marketingTransferSyncUserMapper.getMaxIdByTime(apiCode, startDate, endDate, tcId);
        Long middleId;
        if (endId == null || endId == 0 || beginId == null || beginId == 0){
            continueFlag = Boolean.FALSE;
        }
        MarketingTransferSyncUser syncUser = new MarketingTransferSyncUser();
        syncUser.settCid(tcId);
        syncUser.setApiCode(apiCode);
        syncUser.setRequestData(localDate.toString());
        while (continueFlag) {
            pageSize = dynamicParameterService.getPageSize(null);
            middleId = beginId + pageSize;
            if(middleId >= endId){
                middleId = endId+1;
                continueFlag = Boolean.FALSE;
            }
            List<MarketingTransferSyncUser> transferData = marketingTransferSyncUserMapper.getTransferBySyncUserAndTime(syncUser,
                    startDate, endDate, beginId, middleId);
            Set<String> set = transferData.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            beginId = middleId;
            threadPool.submit(() -> {
                //判断转化数据是否在有效期内
                Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum = validityPeriodService
                        .getValidityPeriodsByCustNum(set, apiCode, localDate);
                for (MarketingTransferSyncUser transferFilterData : transferData) {
                    String custNum = emptyDefault(transferFilterData.getCustNum());
                    SyncUserValidityPeriodsBO boMap = validityPeriodsByCustNum.get(custNum);
                    if (boMap == null) {
                        log.warn("国美转化数据提取：{}不满足案件编号“有效期内”条件", custNum);
                        continue;
                    }
                    MarketingSyncUser marketingSyncUser = boMap.getSyncUsers().get(0);
                    String group = "";
                    String userId = "";
                    String registrationDate = "";
                    String isLogin = "";
                    String loginTime = removeMillisecond(emptyDefault(transferFilterData.getLoginTime()));
                    String isApplyCredit = "";
                    String applyCreditTime = "";
                    String isCreditPass = "";
                    String creditPassTime = "";
                    String creditAmount = "";
                    String isApplyWithdrawals = "";
                    String withdrawalsTime = "";
                    String isRiskPass = "";
                    String riskPassAmount = "";
                    String lendersDate = "";
                    String lendersAmount = "";
                    String customName = "";
                    if (ObjectUtil.isNotEmpty(marketingSyncUser)){
                        JSONObject jsonObject = ObjectUtil.isNotEmpty(marketingSyncUser.getReserveField1())
                                ? JSON.parseObject(marketingSyncUser.getReserveField1())
                                : null;
                        customName = ObjectUtil.isNotEmpty(jsonObject) ? jsonObject.getString("group") : "";
                    }
                    String reserveField1 = transferFilterData.getReserveField1();
                    if (StringUtils.isNotBlank(reserveField1)) {
                        JSONObject jsonObject = JSON.parseObject(reserveField1);
                        group = jsonObject.getString("group");
                        userId = jsonObject.getString("userId");
                        registrationDate = jsonObject.getString("registrationDate");
                        isLogin = jsonObject.getString("isLogin");
                        isApplyCredit = jsonObject.getString("isApplyCredit");
                        applyCreditTime = jsonObject.getString("applyCreditTime");
                        isCreditPass = jsonObject.getString("isCreditPass");
                        creditPassTime = jsonObject.getString("creditPassTime");
                        creditAmount = jsonObject.getString("creditAmount");
                        isApplyWithdrawals = jsonObject.getString("isApplyWithdrawals");
                        isRiskPass = jsonObject.getString("isRiskPass");
                        riskPassAmount = jsonObject.getString("riskPassAmount");
                        lendersDate = jsonObject.getString("lendersDate");
                        lendersAmount = jsonObject.getString("lendersAmount");
                    }
                    StringBuilder sb = new StringBuilder();
                    try {
                        sb.append(emptyDefault(group).concat(","))
                                .append(emptyDefault(userId).concat(","))
                                .append(removeMillisecond(emptyDefault(registrationDate)).concat(","))
                                .append(emptyDefault(isLogin).concat(","))
                                .append(loginTime.concat(","))
                                .append(emptyDefault(isApplyCredit).concat(","))
                                .append(removeMillisecond(emptyDefault(applyCreditTime)).concat(","))
                                .append(emptyDefault(isCreditPass).concat(","))
                                .append(removeMillisecond(emptyDefault(creditPassTime)).concat(","))
                                .append(emptyDefault(creditAmount).concat(","))
                                .append(emptyDefault(isApplyWithdrawals).concat(","))
                                .append(removeMillisecond(emptyDefault(withdrawalsTime)).concat(","))
                                .append(emptyDefault(isRiskPass).concat(","))
                                .append(emptyDefault(riskPassAmount).concat(","))
                                .append(removeMillisecond(emptyDefault(lendersDate)).concat(","))
                                .append(emptyDefault(lendersAmount).concat(","))
                                .append(emptyDefault(customName))
                                .append("\r\n");
                        fw.append(sb.toString());
                        totalSize.incrementAndGet();
                    } catch (IOException e) {
                        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.GUOMEI_SERVICEERROR.getCode()
                                , "[" + apiCode + "]国美转化数据提取[" + custNum + "]提取程序异常"), e);
                    }
                }
                try {
                    fw.flush();
                } catch (IOException e) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.GUOMEI_SERVICEERROR.getCode()
                            , "[" + apiCode + "]国美转化数据提取flush异常!"), e);
                }
            });
        }
        threadPool.shutdown();

        try {
            while (!threadPool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                if (log.isInfoEnabled()) {
                    long taskCount = threadPool.getTaskCount();
                    long completedTaskCount = threadPool.getCompletedTaskCount();
                    log.warn("国美转化数据提取写入文件大约总任务数：{}；大约已完成任务数：{}；大约剩余任务数：{}"
                            , taskCount, completedTaskCount, taskCount - completedTaskCount);
                }
            }
            saveUpdateTask(transferFileTask, totalSize.intValue());
            log.warn("国美转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}"
                    , apiCode, System.currentTimeMillis() - start, totalSize.intValue());
        } catch (InterruptedException e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.GUOMEI_SERVICEERROR.getCode()
                    , "国美转化数据提取-本地文件生成失败！"), e);
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
            transferFileTaskMapper.deleteByPrimaryKey(transferFileTask.getId());
        }
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

    private String removeMillisecond(String timeStr) {
        return timeStr.replace(":000", "");
    }

    private String emptyDefault(String value) {
        return StringUtils.isNotEmpty(value) ? value : "";
    }

}
