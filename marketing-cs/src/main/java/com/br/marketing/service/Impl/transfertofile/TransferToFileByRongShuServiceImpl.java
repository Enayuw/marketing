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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.service.TransferDataValidityPeriodService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.entity.TransferFileTaskExample;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.Impl.RuleRedisServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * <a href="https://c.100credit.cn/pages/viewpage.action?pageId=98022314">榕树转化数据提取-4004643</a>
 *
 * @author senyang.zheng
 * @date 2024/06/03
 */
@Slf4j
@Service
public class TransferToFileByRongShuServiceImpl implements ITransferToFileService {

    final static String EXECUTE_TIME = "01:00:00";

    private final static String FILE_HEADER = "requestId,requestTime,custNum,cell,userType,userType1" +
            ",registerTime,ifApply,applyDt,applyResult,auditTime,auditAmount,ifLent,lentTime,lentAmount" +
            ",applyLoan,applyLoanTime,applyLoanAmount,ifActivity,activityTime,unlentAmount,caseEffective,isBlack,taskId";

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
    private TransferDataValidityPeriodService validityPeriodService;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
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
        String extractTime = StringUtils.isBlank(marketingCommonConfig.getRongShuFileExecTime())
                ? EXECUTE_TIME : marketingCommonConfig.getRongShuFileExecTime();
        LocalTime localTime = LocalTime.parse(extractTime);
        boolean isParam = StringUtils.isNotBlank(myParam);
        if (LocalTime.now().isAfter(localTime) || isParam) {
            // 指定日期提取，生成指定日期的记录，不是当天的记录
            String dateyyyymmddStr = isParam ? myParam : LocalDate.now().toString();
            LocalDate localDate = LocalDate.parse(dateyyyymmddStr, YYYYMMDDSHORTLINE);
            String yesterday = localDate.minusDays(1).toString();
            String newYesterday = yesterday.replace("-", "");
            String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(date)
                    .andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("榕树转化提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId, dateyyyymmddStr);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                String fileName = apiCode + "_zhuanhua_" + newYesterday + ".txt";
                transferFileTask.setFileName(fileName);
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
        log.warn("榕树转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
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
                log.error("{}目录创建失败！", descPath);
            }
        }
        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8))) {
            fw.append(FILE_HEADER);
            fw.append("\r\n");
            writeTransferToFile(fw, apiCode, transferFileTask, requestDate);
        } catch (Exception e) {
            log.error("榕树转化提取写入文件异常", e);
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage(e.getMessage());
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    public void writeTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask, String requestDate) {
        long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        AtomicInteger totalSize = new AtomicInteger(0);
        long timeout = 5L;
        Long minId = null;
        boolean mark = true;
        LocalDate localDate = LocalDate.parse(requestDate, YYYYMMDDSHORTLINE);
        String yesterday = localDate.minusDays(1).toString();
        MarketingTransferSyncUser syncUser = new MarketingTransferSyncUser();
        syncUser.setRequestData(yesterday);
        syncUser.settCid(tcId);
        syncUser.setApiCode(apiCode);
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(100, 100, 1);
        while (mark) {
            List<MarketingTransferSyncUser> transferSyncUsers =
                    marketingTransferSyncUserMapper.getRongShuTransferDatatikv_(tcId, apiCode, yesterday, minId);
            if (transferSyncUsers.isEmpty()) {
                mark = false;
                continue;
            }
            minId = transferSyncUsers.get(transferSyncUsers.size() - 1).getId();
            Set<String> set = transferSyncUsers.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            threadPool.submit(() -> {
                //判断转化数据是否在有效期内
                Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum = validityPeriodService
                        .getValidityPeriodsByCustNum(set, apiCode, yesterday);

                for (MarketingTransferSyncUser transferSyncUser : transferSyncUsers) {
                    String custNum = emptyDefault(transferSyncUser.getCustNum());
                    SyncUserValidityPeriodsBO boMap = validityPeriodsByCustNum.get(custNum);
                    if (boMap == null) {
                        log.warn("{}不满足案件编号“有效期内”条件", custNum);
                        continue;
                    }
                    MarketingSyncUser marketingSyncUser = boMap.getSyncUsers().get(0);
                    String cell = "";
                    String userType = "";
                    String userType1 = emptyDefault(transferSyncUser.getUserType());
                    if (StringUtils.isNotEmpty(marketingSyncUser.getCell())){
                        cell = emptyDefault(marketingSyncUser.getCellMd5());
                        userType = emptyDefault(marketingSyncUser.getUserType());
                    }
                    String requestId = emptyDefault(transferSyncUser.getRequestId());
                    String requestTime = removeMillisecond(emptyDefault(transferSyncUser.getRequestTime()));
                    String registerTime = removeMillisecond(emptyDefault(transferSyncUser.getRegisterTime()));
                    String ifApply = emptyDefault(transferSyncUser.getIfApply());
                    String applyDt = removeMillisecond(emptyDefault(transferSyncUser.getApplyDt()));
                    String applyResult = emptyDefault(transferSyncUser.getApplyResult());
                    String auditTime = removeMillisecond(emptyDefault(transferSyncUser.getAuditTime()));
                    String auditAmount = emptyDefault(transferSyncUser.getAuditAmount());
                    String ifLent = emptyDefault(transferSyncUser.getIfLent());
                    String lentTime = removeMillisecond(emptyDefault(transferSyncUser.getLentTime()));
                    String lentAmount = emptyDefault(transferSyncUser.getLentAmount());
                    String reserveField1 = transferSyncUser.getReserveField1();
                    String ifActivity = "";
                    String activityTime = "";
                    String applyLoan = "";
                    String applyLoanTime = "";
                    String applyLoanAmount = "";
                    String unlentAmount = "";
                    String isBlack = "";
                    String taskId = "";
                    if (StringUtils.isNotBlank(reserveField1)) {
                        JSONObject jsonObject = JSON.parseObject(reserveField1);
                        ifActivity = jsonObject.getString("ifActivity");
                        activityTime = jsonObject.getString("activityTime");
                        applyLoan = jsonObject.getString("applyLoan");
                        applyLoanTime = jsonObject.getString("applyLoanTime");
                        applyLoanAmount = jsonObject.getString("applyLoanAmount");
                        unlentAmount = jsonObject.getString("unlentAmount");
                        isBlack = jsonObject.getString("isBlack");
                        if (StringUtils.isNotEmpty(isBlack) && !"1".equals(isBlack) && !"0".equals(isBlack)){
                            isBlack = "是".equals(isBlack) ? "1" : "0";
                        }
                        taskId = jsonObject.getString("taskId");
                    }
                    StringBuilder sb = new StringBuilder();
                    String tableFieldUnlentAmount = emptyDefault(transferSyncUser.getUnlentAmount());
                    String finalAmount = StringUtils.isNotBlank(unlentAmount) ? unlentAmount : tableFieldUnlentAmount;
                    String caseEffective = emptyDefault(transferSyncUser.getCaseEffective());
                    sb.append(requestId.concat(","))
                        .append(requestTime.concat(","))
                        .append(custNum.concat(","))
                        .append(cell.concat(","))
                        .append(userType.concat(","))
                        .append(userType1.concat(","))
                        .append(registerTime.concat(","))
                        .append(ifApply.concat(","))
                        .append(applyDt.concat(","))
                        .append(applyResult.concat(","))
                        .append(auditTime.concat(","))
                        .append(auditAmount.concat(","))
                        .append(ifLent.concat(","))
                        .append(lentTime.concat(","))
                        .append(lentAmount.concat(","))
                        .append(emptyDefault(applyLoan).concat(","))
                        .append(removeMillisecond(emptyDefault(applyLoanTime)).concat(","))
                        .append(emptyDefault(applyLoanAmount).concat(","))
                        .append(emptyDefault(ifActivity).concat(","))
                        .append(removeMillisecond(emptyDefault(activityTime)).concat(","))
                        .append(emptyDefault(finalAmount).concat(","))
                        .append(caseEffective.concat(","))
                        .append(emptyDefault(isBlack).concat(","))
                        .append(emptyDefault(taskId))
                        .append("\r\n");
                    try {
                        fw.append(sb.toString());
                        totalSize.incrementAndGet();
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            });
        }
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                if (log.isInfoEnabled()) {
                    long taskCount = threadPool.getTaskCount();
                    long completedTaskCount = threadPool.getCompletedTaskCount();
                    log.info("榕树转化数据提取写入文件大约总任务数：{}；大约已完成任务数：{}；大约剩余任务数：{}"
                            , taskCount, completedTaskCount, taskCount - completedTaskCount);
                }
            }
            saveUpdateTask(transferFileTask, totalSize.intValue());
            log.warn("榕树转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}"
                    , apiCode, System.currentTimeMillis() - start, totalSize.intValue());
        } catch (InterruptedException e) {
            log.error("榕树转化数据提取-本地文件生成失败！" + e.getMessage(), e);
            threadPool.shutdownNow();
            transferFileTaskMapper.deleteByPrimaryKey(transferFileTask.getId());
            Thread.currentThread().interrupt();
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
