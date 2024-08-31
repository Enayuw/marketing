package com.br.marketing.service.Impl.transfertofile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.SushangPushResultDataMapper;
import com.br.marketing.service.Impl.DynamicParameterServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.Impl.RuleRedisServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * <a href="https://c.100credit.cn/pages/viewpage.action?pageId=166656847">苏商自动化回传-3710114</a>
 * @author guangxiu.li
 * @date 2024/7/16 10:06
 */
@Slf4j
@Service
public class TransferToFileBySuShangServiceImpl implements ITransferToFileService {

    final static String EXECUTE_TIME = "10:00:00";

    private final static String FILE_HEADER = "taskId,custNum,touchType,callTime,pushTime";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private TransferFileTaskMapper transferFileTaskMapper;
    @Resource
    private RuleRedisServiceImpl ruleRedisService;
    @Resource
    private SyncConfigService syncConfigService;
    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    private SushangPushResultDataMapper sushangPushResultDataMapper;
    @Resource
    DynamicParameterServiceImpl dynamicParameterService;
    final static DateTimeFormatter YYYYMMDDSHORTLINE = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_FORMAT);


    /**
     * 自定义提取参数
     *
     * @param apiCode apiCode
     * @param jobParameter 作业参数
     * @return {@link String }
     * @author senyang.zheng
     * @date 2024/06/03
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
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode, String myParam) {
        List<TransferFileTask> resultList = new ArrayList<>();
        String extractTime = StringUtils.isBlank(marketingCommonConfig.getSuShangTransferFileExecuteTime())
                ? EXECUTE_TIME : marketingCommonConfig.getSuShangTransferFileExecuteTime();
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
                log.warn("苏商自动化回传-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId, dateyyyymmddStr);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName(String.format("br_returnlist_%s_01.txt", dateyyyymmddStr));
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
    public Result<String> actionTransferToFile(TransferFileTask transferFileTask, String jobParameter) {
        log.warn("苏商自动化回传-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        Result<String> result = new Result<>();
        String apiCode = transferFileTask.getApiCode();
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String requestDate = StringUtils.isBlank(jobParameter) ? LocalDate.now().toString() : jobParameter;
        String descPath = syncConfigService.getPath()
                .concat("transferToFile/").concat(apiCode).concat("/").concat(date).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            boolean mkdirs = writeDic.mkdirs();
            if (!mkdirs) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SUNING_SERVICEERROR.getCode(), descPath + "目录创建失败！"));
            }
        }
        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8))) {
            fw.append(FILE_HEADER);
            fw.append("\r\n");
            writeSuShangTransferToFile(fw, apiCode, transferFileTask, requestDate);
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SUNING_SERVICEERROR.getCode(), "苏商自动化回传写入文件异常！"), e);
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage(e.getMessage());
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    public void writeSuShangTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask, String requestDate) {
        long start = System.currentTimeMillis();
        AtomicInteger totalSize = new AtomicInteger(0);
        long timeout = 5L;
        String localDate = LocalDate.parse(requestDate, YYYYMMDDSHORTLINE).toString();
        // 创建线程池
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(12, 12, 100);
        Integer pageSize = null;
        Long beginId = sushangPushResultDataMapper.minId(apiCode, localDate);
        Long endId = sushangPushResultDataMapper.maxId(apiCode, localDate);
        Long middleId;
        Boolean continueFlag = Boolean.TRUE;
        if (endId == null || endId == 0 || beginId == null || beginId == 0){
            continueFlag = Boolean.FALSE;
        }
        while (continueFlag) {
            pageSize = dynamicParameterService.getPageSize(null);
            middleId = beginId + pageSize;
            if(middleId >= endId){
                middleId = endId+1;
                continueFlag = Boolean.FALSE;
            }
            List<SushangPushResultData> transferDataOriginal = sushangPushResultDataMapper
                    .getTransferByRequestDateSuShang(apiCode, localDate, beginId, middleId);
            beginId = middleId;
            threadPool.submit(() -> {
                for (SushangPushResultData transferFilterData : transferDataOriginal) {
                    String taskId = emptyDefault(transferFilterData.getTaskId());
                    String custNum = emptyDefault(transferFilterData.getCustNum());
                    String touchType = emptyDefault(transferFilterData.getTouchType());
                    String callTime = "";
                    if (StringUtils.isNotEmpty(transferFilterData.getCallTime())){
                        callTime = LocalDate.parse(transferFilterData.getCallTime(), YYYYMMDDSHORTLINE).toString();
                    }
                    String pushTime = "";
                    if (StringUtils.isNotEmpty(transferFilterData.getPushTime())){
                        pushTime = LocalDate.parse(transferFilterData.getPushTime(), YYYYMMDDSHORTLINE).toString();
                    }
                    StringBuilder sb = new StringBuilder();
                    try {
                        sb.append(taskId).append(",")
                                .append(custNum).append(",")
                                .append(touchType).append(",")
                                .append(emptyDefault(callTime)).append(",")
                                .append(emptyDefault(pushTime));
                        sb.append("\r\n");
                        fw.append(sb.toString());
                        totalSize.incrementAndGet();
                    } catch (IOException e) {
                        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SUNING_SERVICEERROR.getCode()
                                , "[" + apiCode + "]苏商自动化回传[" + custNum + "]提取程序异常"), e);
                    }
                }
                try {
                    fw.flush();
                } catch (IOException e) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SUNING_SERVICEERROR.getCode()
                            , "[" + apiCode + "]苏商自动化回传flush异常!"), e);
                }
            });
        }
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                if (log.isInfoEnabled()) {
                    long taskCount = threadPool.getTaskCount();
                    long completedTaskCount = threadPool.getCompletedTaskCount();
                    log.info("苏商自动化回传写入文件大约总任务数：{}；大约已完成任务数：{}；大约剩余任务数：{}"
                            , taskCount, completedTaskCount, taskCount - completedTaskCount);
                }
            }
            saveUpdateTask(transferFileTask, totalSize.intValue());
            log.warn("苏商自动化回传-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode
                    , System.currentTimeMillis() - start, totalSize);
        } catch (InterruptedException e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SUNING_SERVICEERROR.getCode()
                    , "apiCode[" + apiCode + "]苏商自动化回传-本地文件生成失败!"), e);
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
            transferFileTaskMapper.deleteByPrimaryKey(transferFileTask.getId());
        } catch (Exception e){
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SUNING_SERVICEERROR.getCode()
                    , "apiCode[" + apiCode + "]苏商自动化回传-异常!"), e);
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


    private String emptyDefault(String value) {
        return StringUtils.isNotEmpty(value) ? value : "";
    }
}
