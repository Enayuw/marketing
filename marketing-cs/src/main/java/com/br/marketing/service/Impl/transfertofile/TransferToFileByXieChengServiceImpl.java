package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.mapper.XieChengSmsCollidingDataLogMapper;
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
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author songjuanjnuan
 * @Date 2022/12/13 16:31
 * @Description:携程转化数据提取
 */
@Slf4j
@Service
public class TransferToFileByXieChengServiceImpl implements ITransferToFileService {

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
    XieChengSmsCollidingDataLogMapper xieChengSmsCollidingDataLogMapper;
    @Resource
    LocalFileMapper localFileMapper;

    final static String EXECUTE_TIME = " 09:00:00";

    final static String ZK_EXECUTE_TIME = " 09:00:00";

    final static String XIECHENG_TRANSFER_FILE = "_zhuanhua_";

    final static String XIECHENG_ZK_FILE = "callbackresult_";

    final static DateTimeFormatter ymd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    final static DateTimeFormatter ymdShort = DateTimeFormatter.ofPattern("yyyyMMdd");

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
        if (marketingCommonConfig.getXieChengTransferExecuteTime() != null && marketingCommonConfig.getXieChengTransferExecuteTime().size() > 0) {
            execute = " " + marketingCommonConfig.getXieChengTransferExecuteTime().get(0);
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

        String _zkexecute = ZK_EXECUTE_TIME;
        if (marketingCommonConfig.getXieChengTransferExecuteTime() != null && marketingCommonConfig.getXieChengTransferExecuteTime().size() > 1) {
            _zkexecute = " " + marketingCommonConfig.getXieChengTransferExecuteTime().get(1);
        }
        Date executeTimeByZk = DateHelper.getDatePlusHourMinuteSecond(now, _zkexecute);
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
        if (new Integer(1).equals(transferFileTask.getFileType())) {
            return actionTransfer(transferFileTask, jobParameter);
        } else if (new Integer(2).equals(transferFileTask.getFileType())) {
            return actionZk(transferFileTask, jobParameter);
        } else {
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
    }

    private Result actionTransfer(TransferFileTask transferFileTask, String jobParameter) {
        log.warn("携程转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();//yyyyMMdd
        String childDir = StringUtils.isNotEmpty(transferFileTask.getFileChildDir()) ? (transferFileTask.getFileChildDir() + "/") : "";
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(childDir).concat(apiCode).concat("/")
                .concat(recordDate).concat("/");
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
            log.error("携程转化数据提取文件生成错误！", ex);
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void writeXieChengTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask) throws IOException {
        Long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        LocalDate date = LocalDate.now();
        int totalSize = 0;
        Long indexId = null;
        Integer pageSize = 2000;
        while (true) {
            List<MarketingTransferSyncUser> data = marketingTransferSyncUserMapper.getTransferData(apiCode, tcId,
                    date.toString(), pageSize, indexId);
            if (CollectionUtils.isEmpty(data)) {
                break;
            }

            indexId = data.get(data.size() - 1).getId();

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
                sb.append(cell.concat(","));
                sb.append(convType.concat(","));
                sb.append(transferFilterData.getRequestTime().concat(","));
                sb.append(isBlack);
                sb.append("\r\n");
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
        log.warn("携程锁定结果数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();//yyyyMMdd
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
            fw.append("sha256Code,result,orgChannel,mktLevel,info,fileName");
            fw.append("\r\n");
            writeZk(fw, apiCode, transferFileTask);
        } catch (Exception ex) {
            log.error("携程锁定结果数据提取文件生成错误！", ex);
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void writeZk(Writer fw, String apiCode, TransferFileTask transferFileTask) throws IOException {
        Long start = System.currentTimeMillis();
        String sDateStr = LocalDate.parse(transferFileTask.getStartDate(), ymdShort).minusDays(1L).format(ymd).concat(" 06:00:00");
        String eDateStr = LocalDate.parse(transferFileTask.getStartDate(), ymdShort).format(ymd).concat(" 09:00:00");
        Date sDate = DateHelper.parseDate(sDateStr);
        Date eDate = DateHelper.parseDate(eDateStr);
        Long minId = null;
        int num = 0;
        Boolean isContiue = Boolean.TRUE;
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(50, 50);
        while (isContiue) {
            XieChengSmsCollidingDataLogExample dataExample = new XieChengSmsCollidingDataLogExample();
            dataExample.setOrderByClause(" id asc limit 2000");
            XieChengSmsCollidingDataLogExample.Criteria criteria = dataExample.createCriteria();
            criteria.andApiCodeEqualTo(apiCode)
                    .andStatusEqualTo(2)
                    .andCreateTimeGreaterThanOrEqualTo(sDate)
                    .andCreateTimeLessThan(eDate);
            if (minId != null) {
                criteria.andIdGreaterThan(minId);
            }

            List<XieChengSmsCollidingDataLog> xieChengSmsCollidingDataLogs = xieChengSmsCollidingDataLogMapper.selectByExample(dataExample);
            List<Long> localIds = xieChengSmsCollidingDataLogs.stream().map(t -> t.getLocalId()).distinct().collect(Collectors.toList());
            if (xieChengSmsCollidingDataLogs.size() <= 0) {
                isContiue = Boolean.FALSE;
                continue;
            }
            num += xieChengSmsCollidingDataLogs.size();
            LocalFileExample fileExample = new LocalFileExample();
            fileExample.createCriteria().andIdIn(localIds);
            List<LocalFile> localFiles = localFileMapper.selectByExample(fileExample);
            Map<Long, List<LocalFile>> fileMap = localFiles.stream().collect(Collectors.groupingBy(LocalFile::getId));
            minId = xieChengSmsCollidingDataLogs.get(xieChengSmsCollidingDataLogs.size() - 1).getId();
            threadPool.submit(()->{
                try {
                    for (XieChengSmsCollidingDataLog xieChengSmsCollidingDataLog : xieChengSmsCollidingDataLogs) {
                        List<LocalFile> localFiles1 = fileMap.get(xieChengSmsCollidingDataLog.getLocalId());
                        String fileName = "";
                        if (localFiles1.size() > 0) {
                            fileName = localFiles1.get(0).getFileName();
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append(xieChengSmsCollidingDataLog.getSha256CodeList().concat(","));
                        sb.append((xieChengSmsCollidingDataLog.getResult() == null ? "" : xieChengSmsCollidingDataLog.getResult().toString()).concat(","));
                        sb.append((StringUtils.isBlank(xieChengSmsCollidingDataLog.getOrgChannel()) ? "" : xieChengSmsCollidingDataLog.getOrgChannel()).concat(","));
                        sb.append((StringUtils.isBlank(xieChengSmsCollidingDataLog.getMktLevel()) ? "" : xieChengSmsCollidingDataLog.getMktLevel()).concat(","));
                        sb.append((StringUtils.isBlank(xieChengSmsCollidingDataLog.getInfo()) ? "" : xieChengSmsCollidingDataLog.getInfo()).concat(","));
                        sb.append(fileName);
                        sb.append("\r\n");
                        fw.append(sb.toString());
                    }
                }catch (Exception ex){
                    log.error("携程锁定名单线程错误:"+ex.getMessage(),ex);
                }
            });
        }
        threadPool.shutdown();
        try{
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {

            }}catch (Exception ex){
            log.error(ex.getMessage(),ex);
        }
        TransferFileTask updatetask = new TransferFileTask();
        updatetask.setId(transferFileTask.getId());
        updatetask.setStatus(2);
        updatetask.setFileName(transferFileTask.getFileName());
        updatetask.setFilePath(transferFileTask.getFilePath());
        updatetask.setTaskNumber(num);
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("携程锁定结果数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, num);
    }

    String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String concat = apiCode.concat("_").concat(yyyyMMdd).concat("_").concat(contextId.toString());
        return concat;
    }
}
