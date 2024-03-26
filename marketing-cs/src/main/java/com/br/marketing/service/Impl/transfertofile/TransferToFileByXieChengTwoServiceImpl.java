package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSON;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
    private TableCreateServiceImpl tableCreateService;
    @Autowired
    private RuleRedisServiceImpl ruleRedisService;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    XieChengCollidingDataLogMapper xieChengCollidingDataLogMapper;
    @Resource
    XieChengCollidingDataPackageMapper xieChengCollidingDataPackageMapper;

    final static String EXECUTE_TIME = " 09:00:00";

    final static String ZK_EXECUTE_TIME = " 09:00:00";

    final static String XIECHENG_TRANSFER_FILE = "_zhuanhua_";

    final static String XIECHENG_ZK_FILE = "callbackresult_";

    final static DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    final static DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        return "";
    }

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode, String myParam) {
        List<TransferFileTask> resultList = new ArrayList<>();
        Date now = new Date();
        //可配置
        String execute = EXECUTE_TIME;
        if (marketingCommonConfig.getXieChengTwoTransferExecuteTime() != null
                && marketingCommonConfig.getXieChengTwoTransferExecuteTime().size() > 0) {
            execute = " " + marketingCommonConfig.getXieChengTwoTransferExecuteTime().get(0);
        }
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, execute);
        if (now.after(executeTime)) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("携程转化数据提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName("");
                transferFileTask.setTaskNumber(0);
                transferFileTask.setStartDate(yyyyMMdd);
                transferFileTask.setContextId(transferFileContextId);
                transferFileTask.setCreateTime(new Date());
                transferFileTask.setUpdateTime(new Date());
                transferFileTaskMapper.insertSelective(transferFileTask);
                resultList.add(transferFileTask);
            }
        }

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
        Integer one = Integer.valueOf(1);
        Integer two = Integer.valueOf(2);
        if (one.equals(transferFileTask.getFileType())) {
            return actionTransfer(transferFileTask, jobParameter);
        } else if (two.equals(transferFileTask.getFileType())) {
            return actionZk(transferFileTask, jobParameter);
        } else {
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
    }

    private Result actionTransfer(TransferFileTask transferFileTask, String jobParameter) {
        String requestDate = StringUtils.isBlank(jobParameter)
                ? LocalDate.now().toString() : jobParameter;
        log.warn("携程转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(recordDate).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        StringBuilder fileName = new StringBuilder();
        fileName.append(apiCode).append(XIECHENG_TRANSFER_FILE).append(recordDate).append(".txt");
        String fileAllPath = descPath.concat(fileName.toString());
        transferFileTask.setFileName(fileName.toString());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append("cell,convType,requestTime,isBlack");
            fw.append("\r\n");
            writeXieChengTransferToFile(fw, apiCode, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void writeXieChengTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask) throws IOException {
        Long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        LocalDate date = LocalDate.now();
        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        int totalSize = 0;
        while (mark) {
            Result<List<MarketingTransferSyncUser>> transferData = getOrderTransferData(apiCode, tcId, date.toString(), page);
            if (!ResultCode.SUCCESS.getValue().equals(transferData.getCode())) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            List<MarketingTransferSyncUser> data = transferData.getData();
            //cell,convType
            for (MarketingTransferSyncUser transferFilterData : data) {
                String cell = transferFilterData.getCustNum();
                String convType = "";
                String isBlack = "";
                if (StringUtils.isNotEmpty(transferFilterData.getReserveField1())) {
                    try {
                        convType = getReserFieldVal(transferFilterData.getReserveField1(),"convType");
                        isBlack =  getReserFieldVal(transferFilterData.getReserveField1(),"isBlack");;
                    } catch (Exception e) {
                        log.warn("携程转化数据提取,ReserveField1非JSON格式{}", transferFilterData.getReserveField1());
                    }
                }
                StringBuilder sb = new StringBuilder();
                sb.append(cell.concat(","))
                        .append(convType.concat(","))
                        .append(transferFilterData.getRequestTime().concat(","))
                        .append(isBlack)
                        .append("\r\n");
                fw.append(sb.toString());
            }
            totalSize = totalSize + data.size();
            data.clear();
        }
        TransferFileTask updatetask = new TransferFileTask();
        updatetask.setId(transferFileTask.getId());
        updatetask.setStatus(2);
        updatetask.setFileName(transferFileTask.getFileName());
        updatetask.setFilePath(transferFileTask.getFilePath());
        updatetask.setTaskNumber(totalSize);
        updatetask.setUpdateTime(new Date());
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("携程转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
    }

    private String getReserFieldVal(String reserStr,String field){
        return StringUtils.isNotEmpty(JSON.parseObject(reserStr).getString(field)) ? JSON.parseObject(reserStr).getString(field) : "";
    }

    private Result actionZk(TransferFileTask transferFileTask, String jobParameter) {
        String requestDate = StringUtils.isBlank(jobParameter)
                ? LocalDate.now().toString() : jobParameter;
        log.warn("携程锁定结果数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(recordDate).concat("/");
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
            fw.append("sha256Code,result,orgChannel,mktLevel,info,fileName,ifCycle");
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
        int num = 0;
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(100, 100);

        try {
            String sDateStr = LocalDate.parse(transferFileTask.getStartDate(), YYYYMMDD).minusDays(1L).format(YYYY_MM_DD).concat(" 06:00:00");
            String eDateStr = LocalDate.parse(transferFileTask.getStartDate(), YYYYMMDD).format(YYYY_MM_DD).concat(" 09:00:00");
            Date sDate = DateHelper.parseDate(sDateStr);
            Date eDate = DateHelper.parseDate(eDateStr);
            Long minId = null;

            while (true) {
                XieChengCollidingDataLogExample dataExample = new XieChengCollidingDataLogExample();
                dataExample.setOrderByClause("id asc limit 2000");
                XieChengCollidingDataLogExample.Criteria criteria = dataExample.createCriteria();
                criteria.andIsDeleteEqualTo(0)
                        .andCreateTimeGreaterThanOrEqualTo(sDate)
                        .andCreateTimeLessThan(eDate);
                if (minId != null) {
                    criteria.andIdGreaterThan(minId);
                }

                List<XieChengCollidingDataLog> xieChengCollidingDataLogs = xieChengCollidingDataLogMapper.selectByExample(dataExample);
                if (xieChengCollidingDataLogs.isEmpty()) {
                    break;
                }

                List<Long> packageIds = xieChengCollidingDataLogs.stream()
                        .map(XieChengCollidingDataLog::getPackageId)
                        .distinct()
                        .collect(Collectors.toList());

                num += xieChengCollidingDataLogs.size();

                XieChengCollidingDataPackageExample dataPackageExample = new XieChengCollidingDataPackageExample();
                dataPackageExample.createCriteria().andIdIn(packageIds);
                List<XieChengCollidingDataPackage> dataPackageList = xieChengCollidingDataPackageMapper.selectByExample(dataPackageExample);
                Map<Long, List<XieChengCollidingDataPackage>> dataPackagesMap = dataPackageList.stream()
                        .collect(Collectors.groupingBy(XieChengCollidingDataPackage::getId));
                minId = xieChengCollidingDataLogs.get(xieChengCollidingDataLogs.size() - 1).getId();

                threadPool.submit(() -> {
                    try {
                        for (XieChengCollidingDataLog xieChengCollidingDataLog : xieChengCollidingDataLogs) {
                            List<XieChengCollidingDataPackage> dataPackages = dataPackagesMap.get(xieChengCollidingDataLog.getPackageId());
                            String fileName = "";
                            String ifCycle = StringUtils.isBlank(xieChengCollidingDataLog.getDataSourceType()) ?
                                    "" : xieChengCollidingDataLog.getDataSourceType();
                            if (StringUtils.isNotBlank(ifCycle)){
                                ifCycle = ifCycle.equals("T") ? "1" : "0";
                            }
                            if (!dataPackages.isEmpty()) {
                                fileName = dataPackages.get(0).getPackageName();
                            }
                            String cellSha256CodeList = xieChengCollidingDataLog.getCellSha256CodeList();
                            String result = xieChengCollidingDataLog.getResult() == null ?
                                    "" : xieChengCollidingDataLog.getResult().toString();
                            String orgChannel = StringUtils.isBlank(xieChengCollidingDataLog.getOrgChannel()) ?
                                    "" : xieChengCollidingDataLog.getOrgChannel();
                            String mktLevel = StringUtils.isBlank(xieChengCollidingDataLog.getMktLevel()) ?
                                    "" : xieChengCollidingDataLog.getMktLevel();
                            String info = StringUtils.isBlank(xieChengCollidingDataLog.getInfo()) ?
                                    "" : xieChengCollidingDataLog.getInfo();
                            StringBuilder sb = new StringBuilder();
                            sb.append(cellSha256CodeList.concat(","))
                                    .append(result.concat(","))
                                    .append(orgChannel.concat(","))
                                    .append(mktLevel.concat(","))
                                    .append(info.concat(","))
                                    .append(fileName.concat(","))
                                    .append(ifCycle)
                                    .append("\r\n");
                            fw.append(sb.toString());
                        }
                    } catch (Exception ex) {
                        log.error("携程锁定名单线程错误:" + ex.getMessage(), ex);
                    }
                });
            }
            // 等待所有任务完成
            threadPool.shutdown();
            threadPool.awaitTermination(1, TimeUnit.HOURS);

        } catch (InterruptedException ex) {
            log.error(ex.getMessage(), ex);
            Thread.currentThread().interrupt();
        } finally {
            TransferFileTask updateTask = new TransferFileTask();
            updateTask.setId(transferFileTask.getId());
            updateTask.setStatus(2);
            updateTask.setFileName(transferFileTask.getFileName());
            updateTask.setFilePath(transferFileTask.getFilePath());
            updateTask.setTaskNumber(num);
            transferFileTaskMapper.updateByPrimaryKeySelective(updateTask);

            log.warn("携程锁定结果数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, num);
        }
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
    private Result<List<MarketingTransferSyncUser>> getOrderTransferData(String apiCode, String tcId, String endDate, Integer pageIndex) {
        Integer limitStart = pageIndex * 2000;
        List<MarketingTransferSyncUser> transferOrderInsertTime = marketingTransferSyncUserMapper.getTransferData(apiCode, tcId, endDate, limitStart);
        if (transferOrderInsertTime.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(transferOrderInsertTime);
    }

}
