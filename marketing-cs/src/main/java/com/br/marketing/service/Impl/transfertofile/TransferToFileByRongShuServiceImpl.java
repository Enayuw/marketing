package com.br.marketing.service.Impl.transfertofile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.ListUtils;
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
import com.br.marketing.mapper.MarketingSyncInfoMapper;
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

    private final static String FILE_HEADER = "requestId,requestTime,custNum,cell,userType,userType1,registerTime,ifApply,applyDt,applyResult,"
        + "auditTime,auditAmount,ifLent,lentTime,lentAmount,applyLoan,applyLoanTime,applyLoanAmount,"
        + "ifActivity,activityTime,unlentAmount,caseEffective";

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
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

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
        return "";
    }

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode, String myParam) {
        List<TransferFileTask> resultList = new ArrayList<>();
        Date now = new Date();
        // 可配置
        String execute =
            StringUtils.isBlank(marketingCommonConfig.getRongShuFileExecTime()) ? EXECUTE_TIME : marketingCommonConfig.getRongShuFileExecTime();
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, " " + execute);
        if (now.after(executeTime)) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("榕树转化提取-开始执行,apiCode ={}", apiCode);
                Long contextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, contextId, yyyyMMdd);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                String fileName = apiCode + "_zhuanhua_" + yyyyMMdd + ".txt";
                transferFileTask.setFileName(fileName);
                transferFileTask.setTaskNumber(0);
                transferFileTask.setStartDate(yyyyMMdd);
                transferFileTask.setContextId(contextId);
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
        Result<String> result = new Result<>();
        String apiCode = transferFileTask.getApiCode();
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(date).concat("/");
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
            writeTransferToFile(fw, apiCode, transferFileTask);
        } catch (Exception e) {
            log.error("榕树转化提取写入文件异常", e);
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage(e.getMessage());
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    private void writeTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask) {
        long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        int totalSize = 0;
        long timeout = 5L;
        Long minId = null;
        boolean mark = true;
        String date = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        MarketingTransferSyncUser syncUser = new MarketingTransferSyncUser();
        syncUser.setRequestData(date);
        syncUser.settCid(tcId);
        syncUser.setApiCode(apiCode);
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(100, 100, 1);
        while (mark) {
            List<MarketingTransferSyncUser> transferSyncUsers = marketingTransferSyncUserMapper.getTransferByRequestDate(tcId, apiCode, date, minId);
            if (transferSyncUsers.isEmpty()) {
                mark = false;
                continue;
            }
            minId = transferSyncUsers.get(transferSyncUsers.size() - 1).getId();
            threadPool.submit(() -> {
                // 查询上传表中最新的cell
                List<String> marketingSyncUserList =
                    transferSyncUsers.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toList());
                List<List<String>> partition = ListUtils.partition(marketingSyncUserList, 500);
                Map<String, MarketingSyncUser> preUserMap = new HashMap<>();
                for (List<String> strings : partition) {
                    Set<String> set = new HashSet<>(strings);
                    List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(apiCode, set);
                    Map<String, MarketingSyncUser> map = preUserByTask.stream().collect(Collectors.toMap(MarketingSyncUser::getCustNum,
                        Function.identity(), (v1, v2) -> v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2));
                    preUserMap.putAll(map);
                }
                for (MarketingTransferSyncUser transferSyncUser : transferSyncUsers) {
                    String custNum = transferSyncUser.getCustNum();
                    StringBuilder sb = new StringBuilder();
                    String reserveField1 = transferSyncUser.getReserveField1();
                    String ifActivity = null;
                    String activityTime = null;
                    String applyLoan = null;
                    String applyLoanTime = null;
                    String applyLoanAmount = null;
                    String unlentAmount = null;
                    if (StringUtils.isNotBlank(reserveField1)) {
                        JSONObject jsonObject = JSON.parseObject(reserveField1);
                        ifActivity = jsonObject.getString("ifActivity");
                        activityTime = jsonObject.getString("activityTime");
                        applyLoan = jsonObject.getString("applyLoan");
                        applyLoanTime = jsonObject.getString("applyLoanTime");
                        applyLoanAmount = jsonObject.getString("applyLoanAmount");
                        unlentAmount = jsonObject.getString("unlentAmount");
                    }
                    sb.append(emptyDefault(transferSyncUser.getRequestId())).append(",")
                        .append(removeMillisecond(emptyDefault(transferSyncUser.getRequestTime()))).append(",")
                        .append(emptyDefault(transferSyncUser.getCustNum())).append(",")
                        .append(emptyDefault(preUserMap.get(custNum) != null ? preUserMap.get(custNum).getCellMd5() : "")).append(",")
                        .append(emptyDefault(preUserMap.get(custNum) != null ? preUserMap.get(custNum).getUserType() : "")).append(",")
                        .append(emptyDefault(transferSyncUser.getUserType())).append(",")
                        .append(removeMillisecond(emptyDefault(transferSyncUser.getRegisterTime()))).append(",")
                        .append(emptyDefault(transferSyncUser.getIfApply())).append(",")
                        .append(removeMillisecond(emptyDefault(transferSyncUser.getApplyDt()))).append(",")
                        .append(emptyDefault(transferSyncUser.getApplyResult())).append(",")
                        .append(removeMillisecond(emptyDefault(transferSyncUser.getAuditTime()))).append(",")
                        .append(emptyDefault(transferSyncUser.getAuditAmount())).append(",").append(emptyDefault(transferSyncUser.getIfLent()))
                        .append(",").append(removeMillisecond(emptyDefault(transferSyncUser.getLentTime()))).append(",")
                        .append(emptyDefault(transferSyncUser.getLentAmount())).append(",").append(emptyDefault(applyLoan)).append(",")
                        .append(removeMillisecond(emptyDefault(applyLoanTime))).append(",").append(emptyDefault(applyLoanAmount)).append(",")
                        .append(emptyDefault(ifActivity)).append(",").append(removeMillisecond(emptyDefault(activityTime))).append(",");
                    String tableFieldUnlentAmount = emptyDefault(transferSyncUser.getUnlentAmount());
                    String finalAmount = StringUtils.isNotBlank(unlentAmount) ? unlentAmount : tableFieldUnlentAmount;
                    sb.append(emptyDefault(finalAmount)).append(",")
                    .append(emptyDefault(transferSyncUser.getCaseEffective()))
                    .append("\r\n");
                    try {
                        fw.append(sb.toString());
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            });
            totalSize = totalSize + transferSyncUsers.size();
        }
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                if (log.isInfoEnabled()) {
                    long taskCount = threadPool.getTaskCount();
                    long completedTaskCount = threadPool.getCompletedTaskCount();
                    log.info("榕树转化数据提取写入文件大约总任务数：{}；大约已完成任务数：{}；大约剩余任务数：{}", taskCount, completedTaskCount, taskCount - completedTaskCount);
                }
            }
            saveUpdateTask(transferFileTask, totalSize);
            log.warn("榕树转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
        } catch (InterruptedException e) {
            log.error("榕树转化数据提取-本地文件生成失败！" + e.getMessage(), e);
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

    private String removeMillisecond(String timeStr) {
        return timeStr.replace(":000", "");
    }

    private String emptyDefault(String value) {
        return StringUtils.isNotEmpty(value) ? value : "";
    }
}
