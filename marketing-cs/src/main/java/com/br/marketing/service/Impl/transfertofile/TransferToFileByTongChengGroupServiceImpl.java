package com.br.marketing.service.Impl.transfertofile;

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
 * 同程集团对同程金融转化数据提取，用于转化效果评估
 */
@Slf4j
@Service
public class TransferToFileByTongChengGroupServiceImpl implements ITransferToFileService {

    /**
     * 同程集团对同程金融转化数据提取 文件头
     */
    private final static String TABLE_HEAD_TRANSFER = "requestTime,custNum,userType,registerTime,applyDt,applyResult," +
            "auditTime,ifLent,lentTime,lentAmount,applyLoan,applyLoanTime,isBlack,blackTime,loginChannel,loanResult,cell";
    /**
     * 时间格式 yyyy-MM-dd
     */
    final DateTimeFormatter YYYYMMDDLINEDF = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_FORMAT);
    /**
     * 时间格式 yyyy-MM-dd HH:mm:ss
     */
    final DateTimeFormatter YYYYMMDDLINEDCTF = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_COLON_TIME_FORMAT);
    /**
     * 执行时间
     */
    private static final String EXECUTE_TIME = "08:00:00";
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
    DynamicParameterServiceImpl dynamicParameterService;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private MarketingDataValidConfigMapper marketingDataValidConfigMapper;
    @Resource
    private TransferDataValidityPeriodService validityPeriodService;

    /**
     * 获取对应apicode的参数
     * 比如自定义参数为 7410785#2022-07-11,true;7412003#123 此时的apicode是7410785，返回的为20220711,true
     *
     * @param apiCode apiCode
     * @param jobParameter job中的参数
     * @return java.lang.String 处理后的参数
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
        String extractTime = StringUtils.isBlank(marketingCommonConfig.getTongChengGroupTransferExecuteTime())
                ? EXECUTE_TIME : marketingCommonConfig.getTongChengGroupTransferExecuteTime();
        LocalTime localTime = LocalTime.parse(extractTime);
        boolean isParam = StringUtils.isNotBlank(myParam);
        // 指定日期提取时不限制时间
        if (LocalTime.now().isAfter(localTime) || isParam) {
            // 当天的记录
            String dateyyyymmddStr = isParam ? myParam.replace("-", "")
                    : LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String localDateStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(localDateStr)
                    .andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("同程集团转化数据提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId, dateyyyymmddStr);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName(String.format("tongchengjituan_zhuanhua_%s.txt", dateyyyymmddStr));
                transferFileTask.setTaskNumber(0);
                transferFileTask.setStartDate(localDateStr);
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
    public Result actionTransferToFile(TransferFileTask transferFileTask, String jobParameter) {
        String requestDate = StringUtils.isBlank(jobParameter)
                ? LocalDate.now().toString() : jobParameter;
        Result<String> result = new Result<>();
        String apiCode = transferFileTask.getApiCode();
        String descPath = syncConfigService.getPath().concat("transferToFile/")
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
            log.error("apiCode[{}]同程集团转化数据写入文件异常-", apiCode, ex);
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage(ex.getMessage());
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    /**
     * 给文件中写具体的提取数据
     * @param fw 写对象
     * @param apiCode apiCode
     * @param transferFileTask 转化数据对象
     * @param requestDate 当前时间或者job中的时间
     */
    public void writeTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask, String requestDate) {
        long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        int page = 0;
        AtomicInteger totalSize = new AtomicInteger(0);
        long timeout = 5L;
        LocalDate dateT = LocalDate.parse(requestDate, YYYYMMDDLINEDF);
        String requestDataMinusOne = dateT.minusDays(1L).format(YYYYMMDDLINEDF);
        MarketingTransferSyncUser syncUser = new MarketingTransferSyncUser();
        syncUser.settCid(tcId);
        syncUser.setApiCode(apiCode);
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(100, 100, 1);
        Integer pageSize = dynamicParameterService.getPageSize("TongChengGroupGet");

        MarketingDataValidConfig configList = marketingDataValidConfigMapper
                .queryStartDateEndDatetikv_(apiCode, requestDataMinusOne, null);
        if(null != configList ){
            // 周期数据范围开始时间
            String startDate = configList.getValidStartDate();
            // 周期数据范围结束时间
            String endDate = configList.getValidEndDate();
            if(StringUtils.isNotBlank(endDate)){
                boolean after = dateT.isAfter(LocalDate.parse(endDate, YYYYMMDDLINEDF));
                if(!after){
                    endDate = requestDate;
                }
            }else{
                endDate = requestDate;
            }
            for (; ; ) {
                List<MarketingTransferSyncUser> transferData = marketingTransferSyncUserMapper
                        .getTransferByStartAndEndDate(syncUser, startDate, endDate, null, page * pageSize, pageSize);
                if (CollectionUtils.isEmpty(transferData)) {
                    break;
                }
                page++;
                Set<String> custNumSet = transferData.parallelStream()
                        .map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
                //判断转化数据是否在有效期内
                Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum = validityPeriodService
                        .getValidityPeriodsByCustNum(custNumSet, apiCode, requestDataMinusOne);
                threadPool.submit(() -> {
                    for (MarketingTransferSyncUser transferFilterData : transferData) {
                        String custNum = transferFilterData.getCustNum();
                        SyncUserValidityPeriodsBO boMap = validityPeriodsByCustNum.get(custNum);
                        if (boMap == null) {
                            log.warn("apiCode[{}]custNum[{}]不满足同程集团案件编号“有效期内”条件", apiCode, custNum);
                            continue;
                        }
                        MarketingSyncUser marketingSyncUser = boMap.getSyncUsers().get(0);
                        String cell = marketingSyncUser.getCell();
                        if (StringUtils.isNotBlank(cell)){
                            cell = EncAndDecUtil.logTodigest(cell, ThreeKeyEncryptEnum.md5);
                        }else{
                            cell = "";
                        }
                        String reserveField1 = transferFilterData.getReserveField1();
                        String applyLoan = null;
                        String applyLoanTime = null;
                        String isBlack = null;
                        String blackTime = null;
                        String loginChannel = null;
                        String loanResult = null;
                        if (StringUtils.isNotBlank(reserveField1)) {
                            JSONObject jsonObject = JSON.parseObject(reserveField1);
                            applyLoan = jsonObject.getString("applyLoan");
                            applyLoanTime = jsonObject.getString("applyLoanTime");
                            isBlack = jsonObject.getString("isBlack");
                            blackTime = jsonObject.getString("blackTime");
                            loginChannel = jsonObject.getString("loginChannel");
                            loanResult = jsonObject.getString("loanResult");
                        }
                        StringBuilder sb = new StringBuilder();
                        String requestTime = transferFilterData.getRequestTime();
                        if(StringUtils.isNotBlank(requestTime)){
                            requestTime = LocalDate.parse(requestTime, YYYYMMDDLINEDCTF).format(YYYYMMDDLINEDF);
                        }else{
                            requestTime = "";
                        }
                        try {
                            sb.append(removeMillisecond(requestTime)).append(",")
                                    .append(emptyDefault(transferFilterData.getCustNum())).append(",")
                                    .append(emptyDefault(transferFilterData.getUserType())).append(",")
                                    .append(removeMillisecond(emptyDefault(transferFilterData.getRegisterTime()))).append(",")
                                    .append(removeMillisecond(emptyDefault(transferFilterData.getApplyDt()))).append(",")
                                    .append(emptyDefault(transferFilterData.getApplyResult())).append(",")
                                    .append(removeMillisecond(emptyDefault(transferFilterData.getAuditTime()))).append(",")
                                    .append(emptyDefault(transferFilterData.getIfLent())).append(",")
                                    .append(removeMillisecond(emptyDefault(transferFilterData.getLentTime()))).append(",")
                                    .append(emptyDefault(transferFilterData.getLentAmount())).append(",")
                                    .append(emptyDefault(applyLoan)).append(",")
                                    .append(removeMillisecond(emptyDefault(applyLoanTime))).append(",")
                                    .append(emptyDefault(isBlack)).append(",")
                                    .append(emptyDefault(blackTime)).append(",")
                                    .append(emptyDefault(loginChannel)).append(",")
                                    .append(emptyDefault(loanResult)).append(",")
                                    .append(cell);
                            sb.append("\r\n");
                            fw.append(sb.toString());
                            fw.flush();
                            totalSize.incrementAndGet();
                        } catch (IOException e) {
                            log.error("[{}]同程集团转化数据[{}]提取程序异常", apiCode, custNum, e);
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
                        log.info("同程集团转化数据提取写入文件大约总任务数：{}；大约已完成任务数：{}；大约剩余任务数：{}"
                                , taskCount, completedTaskCount, taskCount - completedTaskCount);
                    }
                }
                saveUpdateTask(transferFileTask, totalSize.intValue());
                log.warn("同程集团转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode
                        , System.currentTimeMillis() - start, totalSize);
            } catch (InterruptedException e) {
                log.error("apiCode[{}]同程集团转化数据提取-本地文件生成失败-", apiCode, e);
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
                transferFileTaskMapper.deleteByPrimaryKey(transferFileTask.getId());
            } catch (Exception e){
                log.error("apiCode[{}]同程集团转化数据提取-异常-", apiCode, e);
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
                transferFileTaskMapper.deleteByPrimaryKey(transferFileTask.getId());
            }
        }

    }

    /**
     * 更新 b_transfer_file_task 转化文件任务表
     * @param transferFileTask 原对象
     * @param totalSize 任务上传数据量
     */
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
        return StringUtils.isNotBlank(value) ? value : "";
    }

    /**
     * 处理掉时间格式【yyyy-MM-dd HH:mm:ss[:SSS]】最后的 【:000】
     * @param timeStr 待处理的时间格式
     * @return java.lang.String 处理后的时间
     */
    private String removeMillisecond(String timeStr) {
        return timeStr.replace(":000", "");
    }

}
