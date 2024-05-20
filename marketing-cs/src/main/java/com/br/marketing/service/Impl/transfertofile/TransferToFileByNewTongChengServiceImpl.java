package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSON;
import com.br.common.util.BrCipherMaker;
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
import org.springframework.util.DigestUtils;

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
 * @Date 2023/12/11 10:31
 * @Description:同程新系统转化数据提取
 */
@Slf4j
@Service
public class TransferToFileByNewTongChengServiceImpl implements ITransferToFileService {

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


    private final static String TABLE_HEAD_TRANSFER = "custNum,cell,userType,applyDt,applyResult,auditTime," +
            "ifLent,lentTime,lentAmount,effectiveTime,applyLoan";

    final static String EXECUTE_TIME = "08:00:00";


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
        String extractTime = StringUtils.isBlank(marketingCommonConfig.getNewTongChengTransferExecuteTime())
                ? EXECUTE_TIME : marketingCommonConfig.getNewTongChengTransferExecuteTime();
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
                log.warn("同程新系统转化数据提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId, dateyyyymmddStr);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName(String.format("tongcheng_zhuanhua_%s.txt", dateyyyymmddStr));
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
        log.warn("同程新系统转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
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
            writeNewTongChengTransferToFile(fw, apiCode, transferFileTask, requestDate);
        } catch (Exception ex) {
            log.error("写入文件错误！",ex);
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage(ex.getMessage());
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    public void writeNewTongChengTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask, String requestDate) {
        Long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        AtomicInteger totalSize = new AtomicInteger(0);
        long timeout = 5L;
        LocalDate localDate = LocalDate.parse(requestDate, YYYYMMDDSHORTLINE);
        LocalDate startDate = localDate.minusDays(31);
        LocalDate endDate = localDate;
        LocalDate today = LocalDate.parse(requestDate, YYYYMMDDSHORTLINE);
        String appletDate = localDate.minusDays(1).toString();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(50, 50, 1);
        List<MarketingDataValidConfig> validityDataByApiCode = marketingDataValidConfigMapper.getValidityDataByApiCode(apiCode, appletDate);
        if (validityDataByApiCode.size() <= 0){
            log.warn("列表可能为空");
            mark = Boolean.FALSE;
        }
        Optional<MarketingDataValidConfig> minDateConfig = validityDataByApiCode.stream()
                .min(Comparator.comparing(MarketingDataValidConfig::getValidStartDate));
        if (minDateConfig.isPresent()) {
            startDate = LocalDate.parse(minDateConfig.get().getValidStartDate(), YYYYMMDDSHORTLINE);
        } else {
            log.warn("列表为空，无法获取最小的startDate");
        }
        Optional<MarketingDataValidConfig> maxDateConfig = validityDataByApiCode.stream()
                .max(Comparator.comparing(MarketingDataValidConfig::getValidEndDate));
        if (maxDateConfig.isPresent()) {
            endDate = LocalDate.parse(maxDateConfig.get().getValidEndDate(), YYYYMMDDSHORTLINE);
            if (endDate.isBefore(today) || endDate.isEqual(today)){
                endDate = today.plusDays(1);
            }
        } else {
            log.warn("列表为空，无法获取最大的ValidEndDate");
        }

        MarketingTransferSyncUser syncUser = new MarketingTransferSyncUser();
        syncUser.settCid(tcId);
        syncUser.setApiCode(apiCode);
        Integer pageSize = dynamicParameterService.getPageSize(null);
        while (mark) {
            List<MarketingTransferSyncUser> transferData = marketingTransferSyncUserMapper
                    .getTransferByStartAndEndDate(syncUser, startDate.toString(), endDate.toString(), null, page * pageSize, pageSize);
            if (CollectionUtils.isEmpty(transferData)) {
                break;
            }
            page++;
            Set<String> set = transferData.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            //判断转化数据是否在有效期内
            Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum = validityPeriodService
                    .getValidityPeriodsByCustNum(set, apiCode, appletDate);
            threadPool.submit(() -> {
                for (MarketingTransferSyncUser transferFilterData : transferData) {
                    String custNum = transferFilterData.getCustNum();
                    String effectiveTime = "";
                    String applyLoan = "";
                    String cell = "";
                    SyncUserValidityPeriodsBO boMap = validityPeriodsByCustNum.get(custNum);
                    if (boMap == null) {
                        log.warn("{}不满足案件编号“有效期内”条件", custNum);
                        continue;
                    }
                    MarketingSyncUser marketingSyncUser = boMap.getSyncUsers().get(0);
                    if (StringUtils.isNotEmpty(marketingSyncUser.getCell())){
                        cell = EncAndDecUtil.logTodigest(marketingSyncUser.getCell(), ThreeKeyEncryptEnum.md5);
                    }
                    if (StringUtils.isNotEmpty(marketingSyncUser.getReserveField1())) {
                        effectiveTime = JSON.parseObject(marketingSyncUser.getReserveField1()).getString("effectiveTime");
                        effectiveTime = StringUtils.isNotEmpty(effectiveTime) ? effectiveTime.replace(":000","") : "";
                    }
                    if (StringUtils.isNotEmpty(transferFilterData.getReserveField1())) {
                        applyLoan = JSON.parseObject(transferFilterData.getReserveField1()).getString("applyLoan");
                        applyLoan = StringUtils.isNotEmpty(applyLoan) ? applyLoan : "";
                    }
                    StringBuilder sb = new StringBuilder();
                    custNum = StringUtils.isNotEmpty(transferFilterData.getCustNum())
                            ? transferFilterData.getCustNum() : "";
                    String userType = StringUtils.isNotEmpty(transferFilterData.getUserType())
                            ? transferFilterData.getUserType() : "";
                    String applyDt = StringUtils.isNotEmpty(transferFilterData.getApplyDt())
                            ? transferFilterData.getApplyDt().replace(":000","") : "";
                    String applyResult = StringUtils.isNotEmpty(transferFilterData.getApplyResult())
                            ? transferFilterData.getApplyResult() : "";
                    String auditTime = StringUtils.isNotEmpty(transferFilterData.getAuditTime())
                            ? transferFilterData.getAuditTime().replace(":000","")  : "";
                    String ifLent = StringUtils.isNotEmpty(transferFilterData.getIfLent())
                            ? transferFilterData.getIfLent() : "";
                    String lentTime = StringUtils.isNotEmpty(transferFilterData.getLentTime())
                            ? transferFilterData.getLentTime().replace(":000","") : "";
                    String lentAmount = StringUtils.isNotEmpty(transferFilterData.getLentAmount())
                            ? transferFilterData.getLentAmount() : "";
                    sb.append(custNum.concat(","))
                            .append(cell.concat(","))
                            .append(userType.concat(","))
                            .append(applyDt.concat(","))
                            .append(applyResult.concat(","))
                            .append(auditTime.concat(","))
                            .append(ifLent.concat(","))
                            .append(lentTime.concat(","))
                            .append(lentAmount.concat(","))
                            .append(effectiveTime.concat(","))
                            .append(applyLoan)
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
                    log.info("同程新系统转化数据提取写入文件大约总任务数：{}；大约已完成任务数：{}；大约剩余任务数：{}"
                            , taskCount, completedTaskCount, taskCount - completedTaskCount);
                }
            }
            saveUpdateTask(transferFileTask, totalSize.intValue());
            log.warn("同程新系统转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}"
                    , apiCode, System.currentTimeMillis() - start, totalSize.intValue());
        } catch (InterruptedException e) {
            log.error("同程新系统转化数据提取-本地文件生成失败！" , e);
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
