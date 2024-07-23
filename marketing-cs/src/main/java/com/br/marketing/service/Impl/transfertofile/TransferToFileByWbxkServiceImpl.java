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
import org.springframework.util.ObjectUtils;
import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 58新客转化数据提取
 * @author dongshuo.he
 * @date 2024/7/19 14:37
 * 需求：https://c.100credit.cn/pages/viewpage.action?pageId=166656419
 */
@Slf4j
@Service
public class TransferToFileByWbxkServiceImpl implements ITransferToFileService {

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

    private final static String TABLE_HEAD_TRANSFER =
            "custNum,userType,loginTime,applyDt,applyResult,auditTime,lentTime,lentAmount,applyLoanTime";

    final static String EXECUTE_TIME = "09:00:00";

    final static Integer SOLE_NUM = 50;

    final static Integer PAGE_SIZE = 2000;

    final static DateTimeFormatter YYYYMMDDSHORTLINE = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_FORMAT);

    /**
     * 无参数需求
     */
    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        return "";
    }

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode,String myParam) {
        List<TransferFileTask> resultList = new ArrayList<>();
        String extractTime = StringUtils.isBlank(marketingCommonConfig.getWbxkExtDataConfig().get(apiCode).getString("extTime"))
                ? EXECUTE_TIME : marketingCommonConfig.getWbxkExtDataConfig().get(apiCode).getString("extTime");
        LocalTime localTime = LocalTime.parse(extractTime);
        boolean isParam = StringUtils.isNotBlank(myParam);
        if (LocalTime.now().isAfter(localTime) || isParam) {
            // 指定日期提取，生成指定日期的记录，不是当天的记录
            String dateyyyymmddStr = isParam ?
                    myParam.replace("-", "") : LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(date)
                    .andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("58新客数据提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId, dateyyyymmddStr);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName(String.format("%s_zhuahua_%s.txt", apiCode, dateyyyymmddStr));
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
        log.warn("58新客转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
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
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WBXK_ALARM.getCode(),
                        descPath + "目录创建失败！"));
            }
        }
        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            fw.append(TABLE_HEAD_TRANSFER);
            fw.append("\r\n");
            writeQifuCuDongZhiTransferToFile(fw, apiCode, transferFileTask, requestDate);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WBXK_ALARM.getCode(),
                    descPath + "-58新客转化数据提取-文件写入失败！"), ex);
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage(ex.getMessage());
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    public void writeQifuCuDongZhiTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask, String requestDate) {
        Long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        AtomicInteger totalSize = new AtomicInteger(0);
        long timeout = 5L;
        Integer soleNum = marketingCommonConfig.getWbxkExtDataConfig().get(apiCode).getInteger("soleNum") == null ?
                SOLE_NUM : marketingCommonConfig.getWbxkExtDataConfig().get(apiCode).getInteger("soleNum");
        Integer pageSize = marketingCommonConfig.getWbxkExtDataConfig().get(apiCode).getInteger("pageSize") == null ?
                PAGE_SIZE : marketingCommonConfig.getWbxkExtDataConfig().get(apiCode).getInteger("pageSize");
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(soleNum, soleNum, 1);
        MarketingTransferSyncUser syncUser = new MarketingTransferSyncUser();
        syncUser.settCid(tcId);
        syncUser.setApiCode(apiCode);
        Long minId;
        for (; ; ) {
            List<Map<String, Object>> transferData = marketingTransferSyncUserMapper
                    .selectTransferByWbxc(syncUser, pageSize);
            if (CollectionUtils.isEmpty(transferData)) {
                break;
            }
            minId = Long.parseLong(String.valueOf(transferData.get(transferData.size() - 1).get("id"))) + 1;
            syncUser.setId(minId);
            //析出applyLoanTime
            List<Map<String, Object>> extData = transferData.stream()
                    .map((Map<String, Object> transfer) -> {
                        String id = transfer.get("id").toString();
                        try {
                            String reserveField1 = ObjectUtils.isEmpty(transfer.get("reserveField1")) ?
                                    "" : transfer.get("reserveField1").toString();
                            JSONObject reserveField1JSON = JSON.parseObject(reserveField1);
                            String applyLoanTime = reserveField1JSON.containsKey("applyLoanTime") ?
                                    reserveField1JSON.getString("applyLoanTime") : "";
                            transfer.put("applyLoanTime", applyLoanTime);
                        } catch (Exception e) {
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WBXK_ALARM.getCode(),
                                    apiCode + "-58新客转化数据JSON处理异常-id=" + id), e);
                        }
                        return transfer;
                    }).collect(Collectors.toList());
            threadPool.submit(() ->{
                writeDataForOneQuery(fw, totalSize, extData);
            });
        }
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                if (log.isInfoEnabled()) {
                    long taskCount = threadPool.getTaskCount();
                    long completedTaskCount = threadPool.getCompletedTaskCount();
                    log.warn("58新客转化数据提取写入文件大约总任务数：{}；大约已完成任务数：{}；大约剩余任务数：{}"
                            , taskCount, completedTaskCount, taskCount - completedTaskCount);
                }
            }
            saveUpdateTask(transferFileTask, totalSize.intValue());
            log.warn("58新客转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}"
                    , apiCode, System.currentTimeMillis() - start, totalSize.intValue());
        } catch (InterruptedException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WBXK_ALARM.getCode(),
                    apiCode + "-58新客转化数据提取-本地文件生成失败！"), e);
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
            transferFileTaskMapper.deleteByPrimaryKey(transferFileTask.getId());
        }
    }

    private void writeDataForOneQuery(Writer fw, AtomicInteger totalSize, List<Map<String, Object>> extData) {
        for (Map<String, Object> data : extData) {
            String custNum = ObjectUtils.isEmpty(data.get("custNum")) ?
                    "" : String.valueOf(data.get("custNum"));
            String userType = ObjectUtils.isEmpty(data.get("userType")) ?
                    "" : String.valueOf(data.get("userType"));
            String loginTime = ObjectUtils.isEmpty(data.get("loginTime")) ?
                    "" : String.valueOf(data.get("loginTime")).replace(":000","");
            String applyDt = ObjectUtils.isEmpty(data.get("applyDt")) ?
                    "" : String.valueOf(data.get("applyDt")).replace(":000","");
            String applyResult = ObjectUtils.isEmpty(data.get("applyResult")) ?
                    "" : String.valueOf(data.get("applyResult"));
            String auditTime = ObjectUtils.isEmpty(data.get("auditTime")) ?
                    "" : String.valueOf(data.get("auditTime")).replace(":000","");
            String lentTime = ObjectUtils.isEmpty(data.get("lentTime")) ?
                    "" : String.valueOf(data.get("lentTime")).replace(":000","");
            String lentAmount = ObjectUtils.isEmpty(data.get("lentAmount")) ?
                    "" : String.valueOf(data.get("lentAmount"));
            String applyLoanTime = ObjectUtils.isEmpty(data.get("applyLoanTime")) ?
                    "" : String.valueOf(data.get("applyLoanTime")).replace(":000","");
            StringBuilder sb = new StringBuilder();
            sb.append(custNum.concat(","))
                    .append(userType.concat(","))
                    .append(loginTime.concat(","))
                    .append(applyDt.concat(","))
                    .append(applyResult.concat(","))
                    .append(auditTime.concat(","))
                    .append(lentTime.concat(","))
                    .append(lentAmount.concat(","))
                    .append(applyLoanTime)
                    .append("\r\n");
            try {
                fw.append(sb.toString());
                totalSize.incrementAndGet();
            } catch (IOException e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_QIFU_ALARM.getCode(),
                        custNum + "-58新客转化数据提取-文件写入失败！"), e);
            }
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

}
