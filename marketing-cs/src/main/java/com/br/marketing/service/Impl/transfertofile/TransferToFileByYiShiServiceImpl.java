package com.br.marketing.service.Impl.transfertofile;

import cn.hutool.core.util.ObjectUtil;
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
 *  医时转化数据提取
 * @author guangxiu.li
 * @date 2024/5/24 15:41
 */
@Slf4j
@Service
public class TransferToFileByYiShiServiceImpl implements ITransferToFileService {

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


    private final static String TABLE_HEAD_TRANSFER = "custNum,userType,callId,isBlack,extend01,extend02";

    final static String EXECUTE_TIME = "10:00:00";


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
        String extractTime = StringUtils.isBlank(marketingCommonConfig.getYiShiTransferFileExecuteTime())
                ? EXECUTE_TIME : marketingCommonConfig.getYiShiTransferFileExecuteTime();
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
                log.warn("医时转化数据提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId, dateyyyymmddStr);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName(String.format("transform_yishi_%s.txt", dateyyyymmddStr));
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
        log.warn("医时转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
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
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YISHI_SERVICEERROR.getCode(),
                        descPath + "医时化数据提取目录创建失败！"));
            }
        }
        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            fw.append(TABLE_HEAD_TRANSFER);
            fw.append("\r\n");
            writeYiShiTransferToFile(fw, apiCode, transferFileTask, requestDate);
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.YISHI_SERVICEERROR.getCode(), "医时化数据提取写入文件错误！"), ex);
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage(ex.getMessage());
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    public void writeYiShiTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask, String requestDate) {
        Long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        Integer page = 0;
        AtomicInteger totalSize = new AtomicInteger(0);
        long timeout = 5L;
        LocalDate localDate = LocalDate.parse(requestDate, YYYYMMDDSHORTLINE);
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(12, 12, 100);

        MarketingTransferSyncUser syncUser = new MarketingTransferSyncUser();
        syncUser.settCid(tcId);
        syncUser.setApiCode(apiCode);
        syncUser.setRequestData(localDate.toString());
        Integer pageSize = dynamicParameterService.getPageSize(null);
        for (; ; ) {
            List<MarketingTransferSyncUser> transferData = marketingTransferSyncUserMapper
                    .findTransferByApiCodeAndCreateTimePage(syncUser, null, null, null, page * pageSize, pageSize);
            if (CollectionUtils.isEmpty(transferData)) {
                break;
            }
            page++;
            threadPool.submit(() -> {
                for (MarketingTransferSyncUser transferFilterData : transferData) {
                    String custNum = transferFilterData.getCustNum();
                    String userType = transferFilterData.getUserType();
                    String callId ="";
                    String isBlack = "";
                    String extend01 = "";
                    String extend02 = "";
                    if (ObjectUtil.isNotEmpty(transferFilterData.getReserveField1())) {
                        JSONObject jsonObject = JSON.parseObject(transferFilterData.getReserveField1());
                        callId = jsonObject.getString("callId");
                        isBlack = jsonObject.getString("isBlack");
                        extend01 = jsonObject.getString("extend01");
                        extend02 = jsonObject.getString("extend02");
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append(emptyDefault(custNum).concat(","))
                            .append(emptyDefault(userType).concat(","))
                            .append(emptyDefault(callId).concat(","))
                            .append(characterMapping(isBlack).concat(","))
                            .append(characterMapping(extend01).concat(","))
                            .append(emptyDefault(extend02))
                            .append("\r\n");
                    try {
                        fw.append(sb.toString());
                        totalSize.incrementAndGet();
                    } catch (IOException e) {
                        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.YISHI_SERVICEERROR.getCode(), "医时转化数据写入文件异常"), e);
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
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.YISHI_SERVICEERROR.getCode()
                            , "医时转化数据提取写入文件大约总任务数：" + taskCount
                                    + "；大约已完成任务数：" + completedTaskCount
                                    + "；大约剩余任务数：" + (taskCount - completedTaskCount)));

                }
            }
            saveUpdateTask(transferFileTask, totalSize.intValue());
            log.warn("医时转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}"
                    , apiCode, System.currentTimeMillis() - start, totalSize.intValue());
        } catch (InterruptedException e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.YISHI_SERVICEERROR.getCode()
                    , "医时转化数据提取-本地文件生成失败！"), e);
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

    /**
     * 2023-12-22 11:20
     * 值为null时，赋值''
     */
    private String emptyDefault(String value) {
        return StringUtils.isNotEmpty(value) ? value : "";
    }

    /**
     * 1是0否
     * 值为null时，赋值''
     */
    private String characterMapping(String s) {
        if (StringUtils.isNotBlank(s)) {
            if ("1".equals(s) || "0".equals(s)) {
                return s;
            } else if ("是".equals(s) || "否".equals(s)) {
                s = "是".equals(s) ? "1" : "0";
                return s;
            }
        }
        return s;
    }

    private String createBatchNumber(String apiCode, Long contextId, String dateStr) {
        return apiCode.concat("_").concat(dateStr).concat("_").concat(contextId.toString());
    }

}
