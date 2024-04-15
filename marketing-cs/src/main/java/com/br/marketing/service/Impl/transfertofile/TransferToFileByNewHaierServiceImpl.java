package com.br.marketing.service.Impl.transfertofile;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
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

/**
 * @Author 李广秀
 * @Date 2023/12/22 10:31
 * @Description:海尔新系统转化数据提取
 */
@Slf4j
@Service
public class TransferToFileByNewHaierServiceImpl implements ITransferToFileService {

    private static final String EXECUTE_TIME = "10:00:00";
    private final static String TABLE_HEAD_TRANSFER = "custNum,userType,customName,registerTime,applyDt,auditTime,requestTime";
    @Autowired
    DynamicParameterServiceImpl dynamicParameterService;
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
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    /**
     * 2023-12-22 10:50
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
        String extractTime = StringUtils.isBlank(marketingCommonConfig.getNewHaierTransferExecuteTime())
                ? EXECUTE_TIME : marketingCommonConfig.getNewHaierTransferExecuteTime();
        LocalTime localTime = LocalTime.parse(extractTime);
        boolean isParam = StringUtils.isNotBlank(myParam);
        // 指定日期提取时不限制时间
        if (LocalTime.now().isAfter(localTime) || isParam) {
            // 指定日期提取，生成指定日期的记录，不是当天的记录
            String dateyyyymmddStr = isParam ? myParam.replace("-", "") : LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(date)
                    .andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("海尔新系统转化数据提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId, dateyyyymmddStr);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName(String.format("%s_transform_%s.txt", apiCode, dateyyyymmddStr));
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
        String requestDate = StringUtils.isBlank(jobParameter) ? LocalDate.now().toString() : jobParameter;
        Result<String> result = new Result<>();
        String apiCode = transferFileTask.getApiCode();
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String descPath = syncConfigService.getPath()
                .concat("transferToFile/")
                .concat(apiCode)
                .concat("/")
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
            writeTransferToFile(fw, apiCode, transferFileTask, requestDate);
        } catch (Exception ex) {
            log.error("写入文件错误！",ex);
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage(ex.getMessage());
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    public void writeTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask, String requestDate) {
        long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        int page = 0;
        int totalSize = 0;
        long timeout = 5L;
        String startTime = "2001-01-01";
        String endTime = requestDate + " 23:59:59";
        MarketingTransferSyncUser syncUser = new MarketingTransferSyncUser();
        syncUser.settCid(tcId);
        syncUser.setApiCode(apiCode);
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(100, 100, 1);
        Integer pageSize = dynamicParameterService.getPageSize(null);
        for (; ; ) {
            List<MarketingTransferSyncUser> transferOrderInsertTime = marketingTransferSyncUserMapper
                    .findTransferByApiCodeAndCreateTimePage(syncUser, startTime, endTime, null, page * pageSize, pageSize);
            if (CollectionUtils.isEmpty(transferOrderInsertTime)) {
                break;
            }
            page++;
            threadPool.submit(() -> {
                for (MarketingTransferSyncUser transferFilterData : transferOrderInsertTime) {
                    StringBuilder sb = new StringBuilder();
                    String custNum = emptyDefault(transferFilterData.getCustNum());
                    String userType = emptyDefault(transferFilterData.getUserType());
                    String customName = emptyDefault(transferFilterData.getCustomName());
                    String registerTime = emptyDefault(transferFilterData.getRegisterTime());
                    String applyDt = removeMillisecond(emptyDefault(transferFilterData.getApplyDt()));
                    String auditTime = removeMillisecond(emptyDefault(transferFilterData.getAuditTime()));
                    String requestTime = removeMillisecond(emptyDefault(transferFilterData.getRequestTime()));
                    requestTime = conversionData(requestTime);
                    //custNum,userType,customName,registerTime,applyDt,auditTime,requestTime
                    sb.append(custNum.concat(","))
                            .append(userType.concat(","))
                            .append(customName.concat(","))
                            .append(registerTime.concat(","))
                            .append(applyDt.concat(","))
                            .append(auditTime.concat(","))
                            .append(requestTime)
                            .append("\r\n");
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
                    log.info("海尔新系统转化数据提取写入文件大约总任务数：{}；大约已完成任务数：{}；大约剩余任务数：{}"
                            , taskCount, completedTaskCount, taskCount - completedTaskCount);
                }
            }
            saveUpdateTask(transferFileTask, totalSize);
            log.warn("海尔新系统转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode
                    , System.currentTimeMillis() - start, totalSize);
        } catch (InterruptedException e) {
            log.error("海尔新系统转化数据提取-本地文件生成失败！" + e.getMessage(), e);
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

    /**
     * 2023-12-22 11:20
     * 值为null时，赋值''
     */
    private String emptyDefault(String value) {
        return StringUtils.isNotEmpty(value) ? value : "";
    }

    private String removeMillisecond(String timeStr) {
        return timeStr.replace(":000", "");
    }

    private String conversionData(String timeStr) {
        if (StringUtils.isNotEmpty(timeStr)){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDate date = LocalDate.parse(timeStr, formatter);
            timeStr = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        }
        return timeStr;
    }
}