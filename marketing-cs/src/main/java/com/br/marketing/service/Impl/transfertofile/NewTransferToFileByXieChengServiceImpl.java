package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSON;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
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
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.PeriodOfValidityServiceImpl;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author 李广秀
 * @Date 2023/07/13 16:31
 * @Description:携程新场景转化数据提取
 */
@Slf4j
@Service
public class NewTransferToFileByXieChengServiceImpl implements ITransferToFileService {

    @Autowired
    SyncConfigService syncConfigService;
    @Autowired
    private TransferFileTaskMapper transferFileTaskMapper;
    @Autowired
    private TableCreateServiceImpl tableCreateService;
    @Autowired
    private RuleRedisServiceImpl ruleRedisService;
    @Autowired
    private PeriodOfValidityServiceImpl periodOfValidityService;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    XieChengSmsCollidingDataLogMapper xieChengSmsCollidingDataLogMapper;
    @Resource
    LocalFileMapper localFileMapper;


    final static String EXECUTE_TIME = " 10:00:00";

    final static String ZK_EXECUTE_TIME = " 00:00:00";

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
        if (StringUtils.isNotEmpty(marketingCommonConfig.getXieChengNewTransferExecuteTime()) ) {
            execute = " " + marketingCommonConfig.getXieChengNewTransferExecuteTime();
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

        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(resultList);
    }

    @Override
    public Result actionTransferToFile(TransferFileTask transferFileTask, String jobParameter) {
        if (new Integer(1).equals(transferFileTask.getFileType())) {
            return actionTransfer(transferFileTask, jobParameter);
        } else {
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
    }

    private Result actionTransfer(TransferFileTask transferFileTask, String jobParameter) {
        log.warn("携程转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();//yyyyMMdd
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
            fw.append("cell,convType,requestTime,result,orgChannel,mktLevel");
            fw.append("\r\n");
            writeXieChengTransferToFile(fw, apiCode, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    public void writeXieChengTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask) throws IOException {
        Long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        int totalSize = 0;
        while (mark) {
            Result<List<MarketingNewTransferData>> transferData = getOrderTransferData(page);
            if (!ResultCode.SUCCESS.getValue().equals(transferData.getCode())) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            // 批量的
            List<MarketingNewTransferData> newTransferDatabaseCollision = transferData.getData();

            Set<String> custNumList = newTransferDatabaseCollision.stream()
                    .map(MarketingNewTransferData :: getSha256CodeList)
                    .collect(Collectors.toSet());

            List<MarketingTransferSyncUser> newestByCusnum = marketingTransferSyncUserMapper.getNewTransferDataByCellList(apiCode , tcId, custNumList);
            Map<String, MarketingTransferSyncUser> transferSyncUserMap = newestByCusnum.stream().collect(Collectors.toMap(MarketingTransferSyncUser::getCustNum, Function.identity(), (v1, v2) -> v1));
            for (MarketingNewTransferData marketingNewTransferData : newTransferDatabaseCollision) {

                String cell = marketingNewTransferData.getSha256CodeList();
                MarketingTransferSyncUser marketingTransferSyncUser = transferSyncUserMap.get(cell);
                String requestTime = "";
                String convType = "";
                String result = StringUtils.isNotEmpty(marketingNewTransferData.getResult()) ? marketingNewTransferData.getResult() : "";
                String orgChannel = StringUtils.isNotEmpty(marketingNewTransferData.getOrgChannel()) ? marketingNewTransferData.getOrgChannel() : "";
                String mktLevel = StringUtils.isNotEmpty(marketingNewTransferData.getMktLevel()) ? marketingNewTransferData.getMktLevel() : "";
                if (StringUtils.isNotEmpty(result)){
                    result = result.equals("1") ? "true" : "false";
                }
                if (marketingTransferSyncUser != null && StringUtils.isNotEmpty(marketingTransferSyncUser.getReserveField1())) {
                    try {
                        convType = getReserFieldVal(marketingTransferSyncUser.getReserveField1(),"convType");
                    } catch (Exception e) {
                        log.warn("携程新场景转化数据提取,ReserveField1非JSON格式{}", marketingTransferSyncUser.getReserveField1());
                    }
                    requestTime = marketingTransferSyncUser.getRequestTime();
                } else {
                    convType = "";
                    requestTime = "";
                }
                String sb = cell.concat(",") +
                        convType.concat(",") +
                        requestTime.concat(",") +
                        result.concat(",") +
                        orgChannel.concat(",") +
                        mktLevel + "\r\n";
                fw.append(sb);
            }
            totalSize = totalSize + newTransferDatabaseCollision.size();
            newTransferDatabaseCollision.clear();
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


    String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String concat = apiCode.concat("_").concat(yyyyMMdd).concat("_").concat(contextId.toString());
        return concat;
    }

    /**
     * 获取撞库数据
     * @param pageIndex
     * @return
     */
    private Result<List<MarketingNewTransferData>> getOrderTransferData(Integer pageIndex) {
        Integer limitStart = pageIndex * 2000;
        List<MarketingNewTransferData> transferOrderInsertTime = marketingTransferSyncUserMapper.getTransferByCpsLog(limitStart);
        if (transferOrderInsertTime.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(transferOrderInsertTime);
    }

}