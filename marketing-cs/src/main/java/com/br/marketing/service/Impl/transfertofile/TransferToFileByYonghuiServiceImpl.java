package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.MD5Utils;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.entity.TransferFileTaskExample;
import com.br.marketing.mapper.MarketingSyncUserMapper;
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
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 永辉转化数据提取
 *
 * @author xxx
 */
@Slf4j
@Service
public class TransferToFileByYonghuiServiceImpl implements ITransferToFileService {

    private static final String EXECUTE_TIME = "11:00:00";
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private SyncConfigService syncConfigService;
    @Resource
    private TransferFileTaskMapper transferFileTaskMapper;
    @Resource
    private RuleRedisServiceImpl ruleRedisService;
    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Autowired
    DynamicParameterServiceImpl dynamicParameterService;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    private final static String TABLE_HEAD_TRANSFER = "custNum,userType,registerTime,ifLogin,loginTime,ifApply,applyDt,applyResult,auditTime,auditAmount,applyLoan,applyLoanTime,applyLoanAmount,ifLent,lentTime,lentAmount,cell,channel";

    /**
     * 2023-05-10 18:50
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
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode, String myParam) {
        List<TransferFileTask> resultList = new ArrayList<>();
        //执行时间可配置
        String extractTime = StringUtils.isBlank(marketingCommonConfig.getYonghuiTransferExtractTime())
                ? EXECUTE_TIME : marketingCommonConfig.getYonghuiTransferExtractTime();
        LocalTime localTime = LocalTime.parse(extractTime);
        boolean isParam = org.apache.commons.lang3.StringUtils.isNotBlank(myParam);
        // 指定日期提取时不限制时间
        if (LocalTime.now().isAfter(localTime) || isParam) {
            // 指定日期提取，生成指定日期的记录，不是当天的记录
            String dateyyyymmddStr = isParam ? myParam.replace("-", "")
                    : LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String localDateStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(localDateStr)
                    .andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("永辉转化数据提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId, dateyyyymmddStr);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName(String.format("%s_zhuanhua_%s.txt", apiCode, dateyyyymmddStr));
                transferFileTask.setTaskNumber(0);
                transferFileTask.setStartDate(localDateStr);
                transferFileTask.setContextId(transferFileContextId);
                transferFileTask.setCreateTime(new Date());
                transferFileTask.setUpdateTime(new Date());
                transferFileTask.setFileChildDir("yonghuizhuanhua");
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
        String requestDate = org.apache.commons.lang3.StringUtils.isBlank(jobParameter)
                ? LocalDate.now().toString() : jobParameter;
        Result<String> result = new Result<>();
        String apiCode = transferFileTask.getApiCode();
        String childDir = StringUtils.isNotEmpty(transferFileTask.getFileChildDir()) ? (transferFileTask.getFileChildDir() + "/") : "";
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(childDir)
                .concat(apiCode).concat("/").concat(transferFileTask.getStartDate()).concat("/");
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
            writeTransferToFile(fw, apiCode, transferFileTask, requestDate);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage(ex.getMessage());
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    private void writeTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask
            , String requestDate) {
        long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        int page = 0;
        int totalSize = 0;
        long timeout = 5L;
        MarketingTransferSyncUser syncUser = new MarketingTransferSyncUser();
        syncUser.setRequestData(requestDate);
        syncUser.settCid(tcId);
        syncUser.setApiCode(apiCode);
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(100, 100, 1);
        Integer pageSize = dynamicParameterService.getPageSize("yhGet");
        for (; ; ) {
            List<MarketingTransferSyncUser> transferOrderInsertTime = marketingTransferSyncUserMapper
                    .findTransferByApiCodeAndCreateTimePage(syncUser, null, null, null, page * pageSize, pageSize);
            if (CollectionUtils.isEmpty(transferOrderInsertTime)) {
                break;
            }
            page++;
            threadPool.submit(() -> {
                Set<String> custNumSet = transferOrderInsertTime.parallelStream()
                        .map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
                Map<String, String> cellMap = marketingSyncUserMapper.getCellLastByCustNums(apiCode, custNumSet)
                        .parallelStream().collect(Collectors.toMap(MarketingSyncUser::getCustNum, MarketingSyncUser::getCell));
                for (MarketingTransferSyncUser transferFilterData : transferOrderInsertTime) {
                    StringBuilder sb = new StringBuilder();
                    String reserveField1 = transferFilterData.getReserveField1();
                    String applyLoan = null;
                    String applyLoanTime = null;
                    String applyLoanAmount = null;
                    String channel = null;
                    if (org.apache.commons.lang3.StringUtils.isNotBlank(reserveField1)) {
                        JSONObject jsonObject = JSON.parseObject(reserveField1);
                        applyLoan = jsonObject.getString("applyLoan");
                        applyLoanTime = jsonObject.getString("applyLoanTime");
                        applyLoanAmount = jsonObject.getString("applyLoanAmount");
                        channel = jsonObject.getString("channel");
                    }
                    sb.append(emptyDefault(transferFilterData.getCustNum())).append(",");
                    sb.append(emptyDefault(transferFilterData.getUserType())).append(",");
                    sb.append(removeMillisecond(emptyDefault(transferFilterData.getRegisterTime()))).append(",");
                    sb.append(removeMillisecond(emptyDefault(transferFilterData.getIfLogin()))).append(",");
                    sb.append(removeMillisecond(emptyDefault(transferFilterData.getLoginTime()))).append(",");
                    sb.append(emptyDefault(transferFilterData.getIfApply())).append(",");
                    sb.append(removeMillisecond(emptyDefault(transferFilterData.getApplyDt()))).append(",");
                    sb.append(emptyDefault(transferFilterData.getApplyResult())).append(",");
                    sb.append(removeMillisecond(emptyDefault(transferFilterData.getAuditTime()))).append(",");
                    sb.append(emptyDefault(transferFilterData.getAuditAmount())).append(",");
                    sb.append(emptyDefault(applyLoan)).append(",");
                    sb.append(removeMillisecond(emptyDefault(applyLoanTime))).append(",");
                    sb.append(emptyDefault(applyLoanAmount)).append(",");
                    sb.append(emptyDefault(transferFilterData.getIfLent())).append(",");
                    sb.append(removeMillisecond(emptyDefault(transferFilterData.getLentTime()))).append(",");
                    sb.append(emptyDefault(transferFilterData.getLentAmount())).append(",");
                    sb.append(cellMap.containsKey(transferFilterData.getCustNum()) ? MD5Utils.cell32(
                            BrCipherMaker.getInstance().decode(cellMap.get(transferFilterData.getCustNum()))) : "").append(",");
                    sb.append(emptyDefault(channel));
                    sb.append("\r\n");
                    try {
                        fw.append(sb.toString());
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            });
            totalSize = totalSize + transferOrderInsertTime.size();
        }
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                if (log.isInfoEnabled()) {
                    long taskCount = threadPool.getTaskCount();
                    long completedTaskCount = threadPool.getCompletedTaskCount();
                    log.info("永辉转化数据提取写入文件大约总任务数：{}；大约已完成任务数：{}；大约剩余任务数：{}"
                            , taskCount, completedTaskCount, taskCount - completedTaskCount);
                }
            }
            saveUpdateTask(transferFileTask, totalSize);
            log.warn("永辉转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode
                    , System.currentTimeMillis() - start, totalSize);
        } catch (InterruptedException e) {
            log.error("永辉转化数据提取-本地文件生成失败！" + e.getMessage(), e);
            threadPool.shutdownNow();
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

    /**
     * 2023-05-10 11:20
     * 值为null时，赋值''
     */
    private String emptyDefault(String value) {
        return StringUtils.isNotEmpty(value) ? value : "";
    }

    private String removeMillisecond(String timeStr) {
        return timeStr.replace(":000", "");
    }
}
