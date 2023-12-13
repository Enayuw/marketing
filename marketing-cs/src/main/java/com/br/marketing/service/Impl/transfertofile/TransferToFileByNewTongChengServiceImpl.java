package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSON;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.RuleRedisServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
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
    private MarketingSyncInfoMapper marketingSyncInfoMapper;


    private final static String TABLE_HEAD_TRANSFER = "custNum、cell、userType、applyDt、applyResult、auditTime、ifLent、lentTime、lentAmount、effectiveTime、applyLoan";

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
        Date now = new Date();//执行时间可配置
        String extractTime = StringUtils.isBlank(marketingCommonConfig.getZhongBangTransferExecuteTime())
                ? EXECUTE_TIME : marketingCommonConfig.getZhongBangTransferExecuteTime();
        LocalTime localTime = LocalTime.parse(extractTime);
        boolean isParam = StringUtils.isNotBlank(myParam);
        if (LocalTime.now().isAfter(localTime) || isParam) {
            // 指定日期提取，生成指定日期的记录，不是当天的记录
            String dateyyyymmddStr = isParam ? myParam.replace("-", "") : LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(dateyyyymmddStr)
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
                transferFileTask.setStartDate(dateyyyymmddStr);
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
        String requestDate = StringUtils.isBlank(jobParameter) ? LocalDate.now().toString() : jobParameter;
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(transferFileTask.getStartDate()).concat("/");
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
            log.error(ex.getMessage());
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
        int totalSize = 0;
        long timeout = 5L;
        LocalDate localDate = LocalDate.parse(requestDate, YYYYMMDDSHORTLINE);
        LocalDate startDate = localDate.minusDays(31);
        LocalDate endDate = localDate;
        String appletDate = localDate.minusDays(1).toString();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(50, 50, 1);
        MarketingDataValidConfig validityDataByApiCode = marketingDataValidConfigMapper.getValidityDataByApiCode(apiCode, appletDate);
        //去重后的Set
        HashSet custNumResult = new HashSet();
        if (validityDataByApiCode.getValidStartDate().isEmpty() || validityDataByApiCode.getValidEndDate().isEmpty()) {
            log.warn("列表可能为空，无法获取ValidStartDate和ValidEndDate");
        } else {
            startDate = LocalDate.parse(validityDataByApiCode.getValidStartDate(), YYYYMMDDSHORTLINE);
            endDate = LocalDate.parse(validityDataByApiCode.getValidEndDate(),YYYYMMDDSHORTLINE).plusDays(1);
        }
        while (mark) {
            Result<List<MarketingTransferSyncUser>> transferData = getOrderTransferData(tcId, startDate.toString(), endDate.toString(), page);
            if (!ResultCode.SUCCESS.getValue().equals(transferData.getCode())) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            List<MarketingTransferSyncUser> dataFilter = transferData.getData();
            if (dataFilter.size() <= 0) {
                continue;
            }
            Set<String> set = dataFilter.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(apiCode, set);
            Map<String, MarketingSyncUser> preUserMap = preUserByTask.stream().collect(
                    Collectors.groupingBy(MarketingSyncUser::getCustNum
                            , Collectors.collectingAndThen(
                                    Collectors.reducing((v1, v2) ->
                                            v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2)
                                    , Optional::get)));
            LocalDate finalStartDate = startDate;
            LocalDate finalEndDate = endDate;
            threadPool.submit(() -> {
                for (MarketingTransferSyncUser transferFilterData : dataFilter) {
                    String custNum = transferFilterData.getCustNum();
                    if (preUserMap.containsKey(custNum)) {
                        String requestData = preUserMap.get(custNum).getAppletDate();
                        String effectiveTime = "";
                        String applyLoan ="";
                        LocalDate appletDateLocal = LocalDate.parse(requestData, YYYYMMDDSHORTLINE);
                        if(appletDateLocal.isBefore(finalStartDate) || appletDateLocal.isEqual(finalStartDate)|| appletDateLocal.isAfter(finalEndDate)){
                            continue;
                        }
                        String cell = null;
                        try {
                            String decode = BrCipherMaker.getInstance().decode(preUserMap.get(custNum).getCell());
                            cell = StringUtils.isBlank(decode) ? preUserMap.get(custNum).getCell() : DigestUtils.md5DigestAsHex(decode.getBytes());
                            if (StringUtils.isNotEmpty(preUserMap.get(custNum).getReserveField1())) {
                                effectiveTime = JSON.parseObject(preUserMap.get(custNum).getReserveField1()).getString("effectiveTime");
                            }
                            if (StringUtils.isNotEmpty(transferFilterData.getReserveField1())) {
                                applyLoan = JSON.parseObject(transferFilterData.getReserveField1()).getString("applyLoan");
                            }
                        } catch (Exception e) {
                            log.warn(e.getMessage(),e);
                        }
                        //custNum、cell、userType、applyDt、applyResult、auditTime、ifLent、lentTime、lentAmount、effectiveTime、applyLoan
                        StringBuilder sb = new StringBuilder();
                        sb.append((StringUtils.isNotEmpty(transferFilterData.getCustNum()) ? transferFilterData.getCustNum() : "").concat(","));
                        sb.append(cell.concat(","));
                        sb.append((StringUtils.isNotEmpty(transferFilterData.getUserType()) ? transferFilterData.getUserType() : "").concat(","));
                        sb.append((StringUtils.isNotEmpty(transferFilterData.getApplyDt()) ? transferFilterData.getApplyDt().replace(":000", "") : "").concat(","));
                        sb.append((StringUtils.isNotEmpty(transferFilterData.getApplyResult()) ? transferFilterData.getApplyResult() : "").concat(","));
                        sb.append((StringUtils.isNotEmpty(transferFilterData.getAuditTime()) ? transferFilterData.getAuditTime() : "").concat(","));
                        sb.append((StringUtils.isNotEmpty(transferFilterData.getIfLent()) ? transferFilterData.getIfLent() : "").concat(","));
                        sb.append((StringUtils.isNotEmpty(transferFilterData.getLentTime()) ? transferFilterData.getLentTime().replace(":000", "") : "").concat(","));
                        sb.append((StringUtils.isNotEmpty(transferFilterData.getLentAmount()) ? transferFilterData.getLentAmount() : "").concat(","));
                        sb.append((StringUtils.isNotEmpty(effectiveTime) ? effectiveTime.replace(":000", "") : "").concat(","));
                        sb.append(StringUtils.isNotEmpty(applyLoan) ? applyLoan : "");
                        sb.append("\r\n");
                        try {
                            fw.append(sb.toString());
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
                dataFilter.clear();
            });
            totalSize = totalSize + dataFilter.size();
        }

        threadPool.shutdown();
        custNumResult.clear();
        try {
            while (!threadPool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                if (log.isInfoEnabled()) {
                    long taskCount = threadPool.getTaskCount();
                    long completedTaskCount = threadPool.getCompletedTaskCount();
                    log.info("同程新系统转化数据提取写入文件大约总任务数：{}；大约已完成任务数：{}；大约剩余任务数：{}", taskCount, completedTaskCount, taskCount - completedTaskCount);
                }
            }
            saveUpdateTask(transferFileTask, totalSize);
            log.warn("同程新系统转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
        } catch (InterruptedException e) {
            log.error("同程新系统转化数据提取-本地文件生成失败！" + e.getMessage(), e);
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

    String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String concat = apiCode.concat("_").concat(yyyyMMdd).concat("_").concat(contextId.toString());
        return concat;
    }

    /**
     * 获取转化数据
     * 按照inserttime排序
     *
     * @param tcId
     * @param endDate
     * @param pageIndex
     * @return
     */
    private Result<List<MarketingTransferSyncUser>> getOrderTransferData(String tcId, String startDate, String endDate,Integer pageIndex) {
        Integer limitStart = pageIndex * 2000;
        List<MarketingTransferSyncUser> transferOrderInsertTime = marketingTransferSyncUserMapper.getTransferByStartAndEndDate(tcId, startDate, endDate,limitStart);
        if (transferOrderInsertTime.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(transferOrderInsertTime);
    }

    private String createBatchNumber(String apiCode, Long contextId, String dateStr) {
        return apiCode.concat("_").concat(dateStr).concat("_").concat(contextId.toString());
    }
}
