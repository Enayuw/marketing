package com.br.marketing.service.Impl.transfertofile;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.mapper.XieChengCollidingDataLogMapper;
import com.br.marketing.mapper.XieChengCollidingDataPackageMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.JobManager;
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
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Author guangxiu.li
 * @Date 2024/03/25 16:31
 * @Description:携程v2转化数据提取
 */
@Slf4j
@Service
public class TransferToFileByXieChengTwoServiceImpl implements ITransferToFileService {

    @Autowired
    SyncConfigService syncConfigService;
    @Autowired
    private TransferFileTaskMapper transferFileTaskMapper;
    @Autowired
    private RuleRedisServiceImpl ruleRedisService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    XieChengCollidingDataLogMapper xieChengCollidingDataLogMapper;
    @Resource
    XieChengCollidingDataPackageMapper xieChengCollidingDataPackageMapper;
    @Resource
    private JobManager jobManager;

    final static String ZK_EXECUTE_TIME = " 09:00:00";

    final static String XIECHENG_ZK_FILE = "callbackresult_";

    final static DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 2024-05-07 14:30
     * 撞库范围默认值
     */
    private final static List<Map<Integer, String>> DEFAULT_TIME_RANGE;

    static {
        DEFAULT_TIME_RANGE = new ArrayList<>();
        // 前一天
        DEFAULT_TIME_RANGE.add(Collections.singletonMap(-1, "06:00"));
        // 当天
        DEFAULT_TIME_RANGE.add(Collections.singletonMap(0, "07:00"));
    }

    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        return "";
    }

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode, String myParam) {
        List<TransferFileTask> resultList = new ArrayList<>();
        Date now = new Date();
        //可配置
        String zkexecute = ZK_EXECUTE_TIME;
        if (marketingCommonConfig.getXieChengTwoTransferExecuteTime() != null
                && marketingCommonConfig.getXieChengTwoTransferExecuteTime().size() > 1) {
            zkexecute = " " + marketingCommonConfig.getXieChengTwoTransferExecuteTime().get(1);
        }
        Date executeTimeByZk = DateHelper.getDatePlusHourMinuteSecond(now, zkexecute);
        if (now.after(executeTimeByZk)) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(2);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("携程撞库数据提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(2);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName("");
                transferFileTask.setTaskNumber(0);
                transferFileTask.setStartDate(yyyyMMdd);
                transferFileTask.setContextId(transferFileContextId);
                transferFileTask.setFileChildDir("zk");
                transferFileTask.setCreateTime(new Date());
                transferFileTask.setUpdateTime(new Date());
                transferFileTaskMapper.insertSelective(transferFileTask);
                resultList.add(transferFileTask);
            }
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(resultList);
    }

    @Override
    public Result actionTransferToFile(TransferFileTask transferFileTask, String jobParameter) {
        Integer two = Integer.valueOf(2);
        if (two.equals(transferFileTask.getFileType())) {
            return actionZk(transferFileTask, jobParameter);
        } else {
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
    }


    private Result actionZk(TransferFileTask transferFileTask, String jobParameter) {
        String requestDate = StringUtils.isBlank(jobParameter)
                ? LocalDate.now().toString() : jobParameter;
        log.warn("携程锁定结果数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();
        String childDir = StringUtils.isNotEmpty(transferFileTask.getFileChildDir()) ? (transferFileTask.getFileChildDir() + "/") : "";
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(childDir).concat(apiCode).concat("/")
                .concat(recordDate).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        StringBuilder fileName = new StringBuilder();
        fileName.append(XIECHENG_ZK_FILE).append(recordDate).append(".txt");
        String fileAllPath = descPath.concat(fileName.toString());
        transferFileTask.setFileName(fileName.toString());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append("sha256Code,result,orgChannel,mktLevel,info,fileName,releaseTime,couponCode,couponDesc");
            fw.append("\r\n");
            writeZk(fw, apiCode, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void writeZk(Writer fw, String apiCode, TransferFileTask transferFileTask) {
        Long start = System.currentTimeMillis();
        AtomicInteger totalSize = new AtomicInteger(0);
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(100, 100);
        try {
            List<Map<Integer, String>> timeRange = initCallBackResultTimeRange();
            Map<Integer, String> startTimeMap = timeRange.get(0);
            Map<Integer, String> endTimeMap = timeRange.get(1);
            Date sDate;
            Date eDate;
            final LocalDate localDate = LocalDate.parse(transferFileTask.getStartDate(), YYYYMMDD);
            LocalDate startDate = localDate.plusDays(-1);
            LocalTime startTime = LocalTime.parse("06:00:00");
            LocalDate endDate = localDate;
            LocalTime endTime = LocalTime.parse("07:00:00");
            for (Map.Entry<Integer, String> entry : endTimeMap.entrySet()) {
                endDate = localDate.plusDays(entry.getKey() == null ? 0 : entry.getKey());
                endTime = LocalTime.parse(StringUtils.isBlank(entry.getValue()) ? "07:00:00" : entry.getValue());
                if (entry.getKey() != null || entry.getValue() != null) {
                    break;
                }
            }
            eDate = Date.from(endDate.atTime(endTime).plusSeconds(1).atZone(ZoneId.systemDefault()).toInstant());
            for (Map.Entry<Integer, String> entry : startTimeMap.entrySet()) {
                startDate = localDate.plusDays(entry.getKey() == null
                        ? -1 : entry.getKey());
                startTime = LocalTime.parse(StringUtils.isBlank(entry.getValue())
                        ? "06:00:00" : entry.getValue());
                if (entry.getKey() != null || entry.getValue() != null) {
                    break;
                }
            }
            TransferActionFront actionFront = new TransferActionFront();
            actionFront.setApiCode(apiCode);
            // 获取截至时间之前最大的时间
            sDate = getBeforeTime(localDate, startDate, startTime, eDate, actionFront);
            Long minId = null;
            Boolean isContiue = Boolean.TRUE;
            final boolean bool = startTime.isBefore(endTime) || startTime.equals(endTime);
            while (isContiue) {
                XieChengCollidingDataLogExample dataExample = new XieChengCollidingDataLogExample();
                dataExample.setOrderByClause("id asc limit 2000");
                XieChengCollidingDataLogExample.Criteria criteria = dataExample.createCriteria();
                criteria.andIsDeleteEqualTo(0)
                        .andHttpCodeEqualTo(200)
                        .andBusinessCodeEqualTo(0)
                        .andCreateTimeGreaterThanOrEqualTo(sDate)
                        .andResultEqualTo(true)
                        .andCreateTimeLessThan(eDate);
                if (minId != null) {
                    criteria.andIdGreaterThan(minId);
                }

                List<XieChengCollidingDataLog> xieChengCollidingDataLogs = xieChengCollidingDataLogMapper.selectByExample(dataExample);
                if (xieChengCollidingDataLogs.size() <= 0) {
                    isContiue = Boolean.FALSE;
                    continue;
                }

                List<Long> packageIds = xieChengCollidingDataLogs.stream()
                        .map(XieChengCollidingDataLog::getPackageId)
                        .distinct()
                        .collect(Collectors.toList());

                XieChengCollidingDataPackageExample dataPackageExample = new XieChengCollidingDataPackageExample();
                dataPackageExample.createCriteria().andIdIn(packageIds);
                List<XieChengCollidingDataPackage> dataPackageList = xieChengCollidingDataPackageMapper.selectByExample(dataPackageExample);
                Map<Long, List<XieChengCollidingDataPackage>> dataPackagesMap = dataPackageList.stream()
                        .collect(Collectors.groupingBy(XieChengCollidingDataPackage::getId));
                minId = xieChengCollidingDataLogs.get(xieChengCollidingDataLogs.size() - 1).getId();

                final LocalTime finalEndTime = endTime;
                final LocalTime finalStartTime = startTime;
                final LocalDate finalStartDate = startDate;
                threadPool.submit(() -> {
                    try {
                        for (XieChengCollidingDataLog xieChengCollidingDataLog : xieChengCollidingDataLogs) {
                            if (checkOverlapTimeRepeat(bool, xieChengCollidingDataLog, localDate, finalEndTime
                                    , finalStartTime, sDate, finalStartDate)) {
                                continue;
                            }
                            List<XieChengCollidingDataPackage> dataPackages = dataPackagesMap.get(xieChengCollidingDataLog.getPackageId());
                            String fileName = "";
                            String releaseTime = removeMillisecond(emptyDefault(xieChengCollidingDataLog.getReleaseTime()));
                            String couponCode = removeMillisecond(emptyDefault(xieChengCollidingDataLog.getCouponCode()));
                            String couponDesc = removeMillisecond(emptyDefault(xieChengCollidingDataLog.getCouponDesc()));
                            if (!dataPackages.isEmpty()) {
                                fileName = dataPackages.get(0).getPackageName();
                            }
                            String cellSha256CodeList = emptyDefault(xieChengCollidingDataLog.getCellSha256CodeList());
                            String result = emptyDefault(xieChengCollidingDataLog.getResult().toString());
                            String orgChannel = emptyDefault(xieChengCollidingDataLog.getOrgChannel());
                            String mktLevel = emptyDefault(xieChengCollidingDataLog.getMktLevel());
                            String info = emptyDefault(xieChengCollidingDataLog.getInfo());
                            StringBuilder sb = new StringBuilder();
                            sb.append(cellSha256CodeList.concat(","))
                                    .append(result.concat(","))
                                    .append(orgChannel.concat(","))
                                    .append(mktLevel.concat(","))
                                    .append(info.concat(","))
                                    .append(fileName.concat(","))
                                    .append(releaseTime.concat(","))
                                    .append(couponCode.concat(","))
                                    .append(couponDesc)
                                    .append("\r\n");
                            fw.append(sb.toString());
                            totalSize.incrementAndGet();
                        }
                    } catch (Exception ex) {
                        log.error("携程锁定名单线程错误:" + ex.getMessage(), ex);
                    }
                });
            }
            // 等待所有任务完成
            threadPool.shutdown();
            threadPool.awaitTermination(1, TimeUnit.HOURS);
            jobManager.updateFrontDataStatus(actionFront.getId(), 2);
        } catch (InterruptedException ex) {
            log.error(ex.getMessage(), ex);
            Thread.currentThread().interrupt();
        }

        TransferFileTask updateTask = new TransferFileTask();
        updateTask.setId(transferFileTask.getId());
        updateTask.setStatus(2);
        updateTask.setFileName(transferFileTask.getFileName());
        updateTask.setFilePath(transferFileTask.getFilePath());
        updateTask.setTaskNumber(totalSize.intValue());
        transferFileTaskMapper.updateByPrimaryKeySelective(updateTask);

        log.warn("携程锁定结果数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize.intValue());

    }


    String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String concat = apiCode.concat("_").concat(yyyyMMdd).concat("_").concat(contextId.toString());
        return concat;
    }

    /**
     * 2024-05-13 16:05
     * 初始化时间范围
     */
    private List<Map<Integer, String>> initCallBackResultTimeRange() {
        List<Map<Integer, String>> timeRange = marketingCommonConfig.getXieChengCallBackResultTimeRange();
        if (timeRange.size() < 1) {
            timeRange.addAll(DEFAULT_TIME_RANGE);
        } else if (timeRange.size() < 2) {
            timeRange.add(DEFAULT_TIME_RANGE.get(1));
        }
        Map<Integer, String> startTimeMap = timeRange.get(0);
        if (startTimeMap.size() < 1) {
            startTimeMap.putAll(DEFAULT_TIME_RANGE.get(0));
        }
        Map<Integer, String> endTimeMap = timeRange.get(1);
        if (endTimeMap.size() < 1) {
            startTimeMap.putAll(DEFAULT_TIME_RANGE.get(1));
        }
        return timeRange;
    }

    /**
     * 2024-05-13 15:49
     * 获取历史提取后的最后日期时间
     *
     * @param localDate   提取任务的日期
     * @param startDate   开始日期
     * @param startTime   开始时间
     * @param eDate       编号
     * @param actionFront 任务记录
     * @return 开始日期时间
     */
    private Date getBeforeTime(LocalDate localDate, LocalDate startDate, LocalTime startTime, Date eDate
            , TransferActionFront actionFront) {
        Date sDate;
        // 查询上次一次
        String startDateStr = startDate.toString();
        TransferActionFront frontData = jobManager.getFrontData(actionFront.getApiCode(), startDateStr, 1, null);
        if (frontData == null || StringUtils.isBlank(frontData.getRemark())) {
            sDate = Date.from(startDate.atTime(startTime).atZone(ZoneId.systemDefault()).toInstant());
        } else {
            // 存在就赋值上一次的提取截止最大时间，在原时间增加一秒，规避临界值
            sDate = Date.from(LocalDateTime.parse(frontData.getRemark()).plusSeconds(1).atZone(ZoneId.systemDefault()).toInstant());
        }
        // 查询截至时间之前最大的时间
        Date dateMax = xieChengCollidingDataLogMapper.selectMaxCreateTimeByCreateTime(sDate, eDate);
        String localDateStr = localDate.toString();
        actionFront.setIsDel(9);
        jobManager.updateActionFrontInfo(actionFront.getApiCode(), localDateStr, 1, null, actionFront);
        actionFront.setIsDel(1);
        actionFront.setActionType(1);
        actionFront.setActionData(localDateStr);
        actionFront.setRemark(dateMax == null ? ""
                : dateMax.toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        actionFront.setCreateTime(new Date());
        actionFront.setUpdateTime(actionFront.getCreateTime());
        actionFront.setStatus(1);
        jobManager.saveFrontData(actionFront);
        return sDate;
    }

    /**
     * 2024-05-13 15:34
     * 重叠时间重复检查
     *
     * @param bool                     开始和结束时间对比结果
     * @param xieChengCollidingDataLog 提取处理的数据
     * @param localDate                提取任务的日期
     * @param finalEndTime             结束时间
     * @param finalStartTime           开始时间
     * @param sDate                    提取任务开始的日期时间
     * @param finalStartDate           提取任务开始的日期
     * @return true 已提取过
     */
    private boolean checkOverlapTimeRepeat(boolean bool, XieChengCollidingDataLog xieChengCollidingDataLog
            , LocalDate localDate, LocalTime finalEndTime, LocalTime finalStartTime
            , Date sDate, LocalDate finalStartDate) {
        ZonedDateTime createTime = xieChengCollidingDataLog.getCreateTime().toInstant().atZone(ZoneId.systemDefault());
        if (bool && createTime.toLocalDate().isBefore(localDate)) {
            LocalTime localTime = createTime.toLocalTime();
            if ((localTime.isBefore(finalEndTime) || localTime.equals(finalEndTime))
                    && (localTime.isAfter(finalStartTime) || localTime.equals(finalStartTime))) {
                XieChengCollidingDataLogExample countExample = new XieChengCollidingDataLogExample();
                countExample.createCriteria()
                        .andCellSha256CodeListEqualTo(xieChengCollidingDataLog.getCellSha256CodeList())
                        .andIsDeleteEqualTo(0)
                        .andHttpCodeEqualTo(200)
                        .andBusinessCodeEqualTo(0)
                        .andCreateTimeGreaterThanOrEqualTo(sDate)
                        .andResultEqualTo(true)
                        .andCreateTimeLessThan(Date.from(finalStartDate.atTime(finalEndTime)
                                .atZone(ZoneId.systemDefault()).toInstant()));
                int count = xieChengCollidingDataLogMapper.countByExample(countExample);
                return count > 0;
            }
        }
        return false;
    }

    private String removeMillisecond(String timeStr) {
        return timeStr.replace(":000", "");
    }

    private String emptyDefault(String value) {
        return StringUtils.isNotEmpty(value) ? value : "";
    }

}
