package com.br.marketing.service.Impl.transfertofile;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.enums.ThreeKeyEncryptEnum;
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
import com.br.marketing.util.EncAndDecUtil;
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
 * @Author 李广秀
 * @Date 2024/01/04 10:31
 * @Description:众邦转化数据提取
 */
@Slf4j
@Service
public class TransferToFileByZhongBangTransferServiceImpl implements ITransferToFileService {

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
    private MarketingDataValidConfigMapper marketingDataValidConfigMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private TransferDataValidityPeriodService validityPeriodService;


    private final static String TABLE_HEAD_TRANSFER = "custNum,cell,firstName,userType,ifRegister,registerTime,ifLogin," +
            "loginTime,ifApply,applyDt,applyResult,applyTime,refuseTime,auditTime,auditAmount,ifLent,lentTime,lentAmount,unlentAmount," +
            "applyLoan,applyLoanTime,isLock,isBlack";

    final static String EXECUTE_TIME = "10:00:00";


    final static DateTimeFormatter YYYYMMDDSHORTLINE = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_FORMAT);

    /**
     * 2024-01-04 10:50
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
        String extractTime = StringUtils.isBlank(marketingCommonConfig.getZhongBangExecuteTime())
                ? EXECUTE_TIME : marketingCommonConfig.getZhongBangExecuteTime();
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
                log.warn("众邦转化数据提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId, dateyyyymmddStr);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName(String.format("transform_%s_%s.txt", apiCode, dateyyyymmddStr));
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
        log.warn("众邦转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        Result<String> result = new Result<>();
        String apiCode = transferFileTask.getApiCode();
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String requestDate = StringUtils.isBlank(jobParameter) ? LocalDate.now().toString() : jobParameter;
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
            writeZhongBangTransferToFile(fw, apiCode, transferFileTask, requestDate);
        } catch (Exception ex) {
            log.error("写入文件错误！",ex);
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage(ex.getMessage());
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    public void writeZhongBangTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask, String requestDate) {
        Long start = System.currentTimeMillis();
        String tCId = tableCreateService.getTcId(apiCode);
        AtomicInteger totalSize = new AtomicInteger(0);
        long timeout = 5L;

        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(50, 50, 1);

        List<MarketingDataValidConfig> configList = validityPeriodService
                .getDataValidityPeriodPageList(apiCode, requestDate, null, null);
        if (CollectionUtil.isEmpty(configList)) {
            return;
        }
        Set<String> dateSet = Collections.singleton(requestDate);
        for(String curDateStr : dateSet) {
            MarketingTransferSyncUser syncUser = new MarketingTransferSyncUser();
            syncUser.settCid(tCId);
            syncUser.setApiCode(apiCode);
            syncUser.setRequestData(curDateStr);
            Integer page = 0;
            Integer pageSize = dynamicParameterService.getPageSize(null);
            for (; ; ) {
                List<MarketingTransferSyncUser> transferData = marketingTransferSyncUserMapper
                        .findTransferByApiCodeAndCreateTimePage(syncUser, null, null, null, page * pageSize, pageSize);
                if (CollectionUtils.isEmpty(transferData)) {
                    break;
                }
                page++;

                //判断转化数据是否在有效期内
                Set<String> set = transferData.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
                LocalDate requestLocalDate = LocalDate.parse(requestDate);
                LocalDate invalidDate = requestLocalDate.plusDays(-1);
                Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum = validityPeriodService
                        .getValidityPeriodsByCustNum(set, apiCode, invalidDate);

                threadPool.submit(() -> {
                    for (MarketingTransferSyncUser transferFilterData : transferData) {
                        String custNum = transferFilterData.getCustNum();
                        String cell = "";
                        String firstName = "";

                        SyncUserValidityPeriodsBO boMap = validityPeriodsByCustNum.get(custNum);
                        if (boMap == null) {
                            log.warn("{}不满足案件编号“有效期内”条件", custNum);
                            continue;
                        }
                        MarketingSyncUser marketingSyncUser = boMap.getSyncUsers().get(0);
                        if (StringUtils.isNotEmpty(marketingSyncUser.getCell())) {
                            cell = EncAndDecUtil.logTodigest(marketingSyncUser.getCell(), ThreeKeyEncryptEnum.md5);
                        }
                        if (StringUtils.isNotEmpty(marketingSyncUser.getReserveField1())) {
                            firstName = JSON.parseObject(marketingSyncUser.getReserveField1()).getString("firstName");
                            ;
                            firstName = StringUtils.isNotEmpty(firstName) ? firstName : "";
                        }
                        //custNum,cell,firstName,userType,ifRegister,registerTime,ifLogin,loginTime,ifApply,applyDt,applyResult
                        // ,applyTime,refuseTime,auditTime,auditAmount,ifLent,lentTime,lentAmount,unlentAmount
                        StringBuilder sb = new StringBuilder();
                        custNum = StringUtils.isNotEmpty(transferFilterData.getCustNum())
                                ? transferFilterData.getCustNum() : "";
                        String userType = StringUtils.isNotEmpty(transferFilterData.getUserType())
                                ? transferFilterData.getUserType() : "";
                        String ifRegister = StringUtils.isNotEmpty(transferFilterData.getIfRegister())
                                ? transferFilterData.getIfRegister() : "";
                        String registerTime = StringUtils.isNotEmpty(transferFilterData.getRegisterTime())
                                ? transferFilterData.getRegisterTime().replace(":000", "") : "";
                        String ifLogin = StringUtils.isNotEmpty(transferFilterData.getIfLogin())
                                ? transferFilterData.getIfLogin() : "";
                        String loginTime = StringUtils.isNotEmpty(transferFilterData.getLoginTime())
                                ? transferFilterData.getLoginTime().replace(":000", "") : "";
                        String ifApply = StringUtils.isNotEmpty(transferFilterData.getIfApply())
                                ? transferFilterData.getIfApply() : "";
                        String applyDt = StringUtils.isNotEmpty(transferFilterData.getApplyDt())
                                ? transferFilterData.getApplyDt().replace(":000", "") : "";
                        String applyResult = StringUtils.isNotEmpty(transferFilterData.getApplyResult())
                                ? transferFilterData.getApplyResult() : "";
                        String applyTime = StringUtils.isNotEmpty(transferFilterData.getApplyTime())
                                ? transferFilterData.getApplyTime().replace(":000", "") : "";
                        String refuseTime = StringUtils.isNotEmpty(transferFilterData.getRefuseTime())
                                ? transferFilterData.getRefuseTime().replace(":000", "") : "";
                        String auditTime = StringUtils.isNotEmpty(transferFilterData.getAuditTime())
                                ? transferFilterData.getAuditTime().replace(":000", "") : "";
                        String auditAmount = StringUtils.isNotEmpty(transferFilterData.getAuditAmount())
                                ? transferFilterData.getAuditAmount() : "";
                        String ifLent = StringUtils.isNotEmpty(transferFilterData.getIfLent())
                                ? transferFilterData.getIfLent() : "";
                        String lentTime = StringUtils.isNotEmpty(transferFilterData.getLentTime())
                                ? transferFilterData.getLentTime().replace(":000", "") : "";
                        String lentAmount = StringUtils.isNotEmpty(transferFilterData.getLentAmount())
                                ? transferFilterData.getLentAmount() : "";
                        String unlentAmount = StringUtils.isNotEmpty(transferFilterData.getUnlentAmount())
                                ? transferFilterData.getUnlentAmount() : "";
                        JSONObject jo = JSONObject.parseObject(transferFilterData.getReserveField1());

                        String applyLoan = "";
                        String applyLoanTime = "";
                        String isLock = "";
                        String isBlack = "";
                        if (jo != null) {
                            applyLoan = StringUtils.isNotEmpty(jo.getString("applyLoan"))
                                    ? jo.getString("applyLoan") : "";
                            applyLoanTime = StringUtils.isNotEmpty(jo.getString("applyLoanTime"))
                                    ? jo.getString("applyLoanTime") : "";
                            isLock = StringUtils.isNotEmpty(jo.getString("isLock"))
                                    ? jo.getString("isLock") : "";
                            isBlack = StringUtils.isNotEmpty(jo.getString("isBlack"))
                                    ? jo.getString("isBlack") : "";
                        }
                        sb.append(custNum.concat(","))
                                .append(cell.concat(","))
                                .append(firstName.concat(","))
                                .append(userType.concat(","))
                                .append(ifRegister.concat(","))
                                .append(registerTime.concat(","))
                                .append(ifLogin.concat(","))
                                .append(loginTime.concat(","))
                                .append(ifApply.concat(","))
                                .append(applyDt.concat(","))
                                .append(applyResult.concat(","))
                                .append(applyTime.concat(","))
                                .append(refuseTime.concat(","))
                                .append(auditTime.concat(","))
                                .append(auditAmount.concat(","))
                                .append(ifLent.concat(","))
                                .append(lentTime.concat(","))
                                .append(lentAmount.concat(","))
                                .append(unlentAmount.concat(","))
                                .append(applyLoan.concat(","))
                                .append(applyLoanTime.concat(","))
                                .append(isLock.concat(","))
                                .append(isBlack)
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
        }
        threadPool.shutdown();

        try {
            while (!threadPool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                if (log.isInfoEnabled()) {
                    long taskCount = threadPool.getTaskCount();
                    long completedTaskCount = threadPool.getCompletedTaskCount();
                    log.info("众邦转化数据提取写入文件大约总任务数：{}；大约已完成任务数：{}；大约剩余任务数：{}"
                            , taskCount, completedTaskCount, taskCount - completedTaskCount);
                }
            }
            saveUpdateTask(transferFileTask, totalSize.intValue());
            log.warn("众邦转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}"
                    , apiCode, System.currentTimeMillis() - start, totalSize.intValue());
        } catch (InterruptedException e) {
            log.error("众邦转化数据提取-本地文件生成失败！" , e);
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

}
