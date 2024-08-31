package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.entity.TransferFileTaskExample;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.DynamicParameterServiceImpl;
import com.br.marketing.service.Impl.RuleRedisServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.SyncConfigService;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 奇富360促动支转化数据提取
 * @author guangxiu.li
 * @date 2024/6/25 11:18
 */
@Slf4j
@Service
public class TransferToFileByCuDongZhiServiceImpl implements ITransferToFileService {

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


    private final static String TABLE_HEAD_TRANSFER = "custNum,userType,loginTime,applyDt,applyResult,auditAmount,ifLent,firstName" +
            ",cell,stopMarketingSign,gender,isLightMarkting,operationScene,applyLoan,succAmtType";

    final static String EXECUTE_TIME = "23:00:00";


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
        String extractTime = StringUtils.isBlank(marketingCommonConfig.getCuDongZhiTransferFileExecuteTime())
                ? EXECUTE_TIME : marketingCommonConfig.getCuDongZhiTransferFileExecuteTime();
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
                log.warn("奇富360促动支转化数据提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId, dateyyyymmddStr);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName(String.format("%s_360cudong_zhuahua_%s.txt", apiCode, dateyyyymmddStr));
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
        log.warn("奇富360促动支转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
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
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFU_SERVICEERROR.getCode()
                        , descPath + "目录创建失败！"));
            }
        }
        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            fw.append(TABLE_HEAD_TRANSFER);
            fw.append("\r\n");
            writeCuDongZhiTransferToFile(fw, apiCode, transferFileTask, requestDate);
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFU_SERVICEERROR.getCode()
                    , "奇富360促动支转化数据提取写入文件错误！"), ex);
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage(ex.getMessage());
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    public void writeCuDongZhiTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask, String requestDate) {
        long start = System.currentTimeMillis();
        AtomicInteger totalSize = new AtomicInteger(0);
        long timeout = 5L;
        String tcId = tableCreateService.getTcId(apiCode);
        String localDate = LocalDate.parse(requestDate, YYYYMMDDSHORTLINE).toString();
        // 创建线程池
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(12, 12, 100);
        Integer pageSize = null;
        Long beginId = marketingTransferSyncUserMapper.minId(apiCode, localDate, tcId);
        Long endId = marketingTransferSyncUserMapper.maxId(apiCode, localDate, tcId);
        Long middleId;
        Boolean continueFlag = Boolean.TRUE;
        if (endId == null || endId == 0 || beginId == null || beginId == 0){
            continueFlag = Boolean.FALSE;
        }
        MarketingTransferSyncUser syncUser = new MarketingTransferSyncUser();
        syncUser.settCid(tcId);
        syncUser.setApiCode(apiCode);
        syncUser.setRequestData(localDate);
        while (continueFlag) {
            pageSize = dynamicParameterService.getPageSize(null);
            middleId = beginId + pageSize;
            if(middleId >= endId){
                middleId = endId+1;
                continueFlag = Boolean.FALSE;
            }
            List<MarketingTransferSyncUser> transferData = marketingTransferSyncUserMapper.getTransferBySyncUser(syncUser, beginId, middleId);
            beginId = middleId;
            threadPool.submit(() -> {
                for (MarketingTransferSyncUser transferFilterData : transferData) {
                    String custNum = emptyDefault(transferFilterData.getCustNum());
                    String userType = emptyDefault(transferFilterData.getUserType());
                    String loginTime = removeMillisecond(emptyDefault(transferFilterData.getLoginTime()));
                    String applyDt = removeMillisecond(emptyDefault(transferFilterData.getApplyDt()));
                    String applyResult = emptyDefault(transferFilterData.getApplyResult());
                    String auditAmount = emptyDefault(transferFilterData.getAuditAmount());
                    String ifLent = emptyDefault(transferFilterData.getIfLent());
                    String firstName = "";
                    String cell = "";
                    String stopMarketingSign = "";
                    String gender = "";
                    String isLightMarkting = "";
                    String operationScene = "";
                    String applyLoan = "";
                    String succAmtType = "";
                    String reserveField1 = transferFilterData.getReserveField1();
                    if (StringUtils.isNotBlank(reserveField1)) {
                        JSONObject jsonObject = JSON.parseObject(reserveField1);
                        firstName = jsonObject.getString("firstName");
                        cell = jsonObject.getString("cell");
                        stopMarketingSign = jsonObject.getString("stopMarketingSign");
                        gender = jsonObject.getString("gender");
                        isLightMarkting = jsonObject.getString("isLightMarkting");
                        operationScene = jsonObject.getString("operationScene");
                        applyLoan = jsonObject.getString("applyLoan");
                        succAmtType = jsonObject.getString("succAmtType");
                    }
                    StringBuilder sb = new StringBuilder();
                    try {
                        sb.append(custNum.concat(","))
                                .append(userType.concat(","))
                                .append(loginTime.concat(","))
                                .append(applyDt.concat(","))
                                .append(applyResult.concat(","))
                                .append(auditAmount.concat(","))
                                .append(ifLent.concat(","))
                                .append(emptyDefault(firstName).concat(","))
                                .append(emptyDefault(cell).concat(","))
                                .append(emptyDefault(stopMarketingSign).concat(","))
                                .append(emptyDefault(gender).concat(","))
                                .append(emptyDefault(isLightMarkting).concat(","))
                                .append(emptyDefault(operationScene).concat(","))
                                .append(emptyDefault(applyLoan).concat(","))
                                .append(emptyDefault(succAmtType))
                                .append("\r\n");
                        fw.append(sb.toString());
                        totalSize.incrementAndGet();
                    } catch (IOException e) {
                        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFU_SERVICEERROR.getCode()
                                , "[" + apiCode + "]奇富360促动支转化数据提取[" + custNum + "]提取程序异常"), e);
                    }
                }
                try {
                    fw.flush();
                } catch (IOException e) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFU_SERVICEERROR.getCode()
                            , "[" + apiCode + "]奇富360促动支转化数据提取flush异常!"), e);
                }
            });
        }
        threadPool.shutdown();

        try {
            while (!threadPool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                if (log.isInfoEnabled()) {
                    long taskCount = threadPool.getTaskCount();
                    long completedTaskCount = threadPool.getCompletedTaskCount();
                    log.warn("奇富360促动支转化数据提取写入文件大约总任务数：{}；大约已完成任务数：{}；大约剩余任务数：{}"
                            , taskCount, completedTaskCount, taskCount - completedTaskCount);
                }
            }
            saveUpdateTask(transferFileTask, totalSize.intValue());
            log.warn("奇富360促动支转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}"
                    , apiCode, System.currentTimeMillis() - start, totalSize.intValue());
        } catch (InterruptedException e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFU_SERVICEERROR.getCode()
                    , "奇富360促动支转化数据提取-本地文件生成失败！"), e);
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
