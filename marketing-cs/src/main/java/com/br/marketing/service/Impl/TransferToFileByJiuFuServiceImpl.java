package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.entity.TransferFileTaskExample;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author songjuanjnuan
 * @Date 2022/05/11 14:31
 * @Description:久富转化数据提取
 */
@Slf4j
@Service
public class TransferToFileByJiuFuServiceImpl implements ITransferToFileService {

    @Value("${otherConfig.warning.path:00}")
    private String path;
    @Autowired
    private TransferFileTaskMapper transferFileTaskMapper;
    @Autowired
    private TableCreateServiceImpl tableCreateService;
    @Autowired
    private RuleRedisServiceImpl ruleRedisService;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    final static String EXECUTE_TIME = " 10:00:00";

    final static String JIUFU_TRANSFER_FILE = "jiufu_zhuanhua_";

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode) {
        List<TransferFileTask> resultList = new ArrayList<>();
        Date now = new Date();
        //可配置
        String execute = EXECUTE_TIME;
        if (StringUtils.isNotEmpty(marketingCommonConfig.getJiuFuTransferExecuteTime())) {
            execute = " " + marketingCommonConfig.getJiuFuTransferExecuteTime();
        }
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, execute);
        if (now.after(executeTime)) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("玖富转化数据提取-开始执行,apiCode ={}", apiCode);
                //Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                Long transferFileContextId = 888L;
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
    public Result actionTransferToFile(TransferFileTask transferFileTask) {
        log.warn("玖富转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();//yyyyMMdd
        //判断是否本月为1日
        LocalDate localDate = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;
        if(localDate.getDayOfMonth()==1){
            LocalDate lastMonth = localDate.minusMonths(1); // 当前月份减1
            startDate = lastMonth.with(TemporalAdjusters.firstDayOfMonth()); // 获取当前月的第一天
            endDate = lastMonth.with(TemporalAdjusters.lastDayOfMonth()); // 获取当前月的最后一天
        }else {
            startDate = localDate.with(TemporalAdjusters.firstDayOfMonth()); // 获取当前月的第一天
            endDate = localDate;
        }
        String descPath = path.concat("transferToFile/").concat(apiCode).concat("/").concat(recordDate).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        StringBuilder fileName = new StringBuilder();
        fileName.append(JIUFU_TRANSFER_FILE).append(recordDate).append(".txt");
        String fileAllPath = descPath.concat(fileName.toString());
        transferFileTask.setFileName(fileName.toString());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append("requestId,orgName,custNum,source,userType,ifLogin,loginTime,ifApply,applyDt,applyResult,auditAmount,lentTime,lentAmount,insertTime,applyLoan,applyLoanTime,cell");
            fw.append("\r\n");
            writeJiuFuTransferToFile(fw, apiCode, startDate.toString(), endDate.toString(),transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void writeJiuFuTransferToFile(Writer fw, String apiCode, String startDate, String endDate,TransferFileTask transferFileTask) throws IOException {
        Long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        //去重后的Set
        HashSet custNumResult = new HashSet();
        while (mark) {
            Result<List<MarketingTransferSyncUser>> transferData = getOrderTransferData(tcId, startDate, endDate, page);
            if (!ResultCode.SUCCESS.getValue().equals(transferData.getCode())) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            List<MarketingTransferSyncUser> data = transferData.getData();
            List<MarketingTransferSyncUser> dataFilter = new ArrayList<>();
            for (MarketingTransferSyncUser marketingTransferSyncUser : data) {
                //过滤掉 同一custNum的其他insertTime数据，custNumResult
                if (custNumResult.add(marketingTransferSyncUser.getCustNum())) {
                    dataFilter.add(marketingTransferSyncUser);
                }
            }
            if (dataFilter.size() <= 0) {
                log.warn("玖富转化数据提取-该批次无符合要求的数据,apiCode = {}", apiCode);
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
            //requestId,orgName,custNum,source,userType,ifLogin,loginTime,ifApply,applyDt,applyResult,auditAmount,lentTime,lentAmount,insertTime,applyLoan,applyLoanTime,cell
            for (MarketingTransferSyncUser transferFilterData : dataFilter) {
                String custNum = transferFilterData.getCustNum();
                String applyLoan = "";
                String applyLoanTime = "";
                String cell = "";
                if (preUserMap.containsKey(custNum)) {
                    String decode = BrCipherMaker.getInstance().decode(preUserMap.get(custNum).getCell());
                    cell = StringUtils.isBlank(decode) ? preUserMap.get(custNum).getCell() : DigestUtils.md5DigestAsHex(decode.getBytes());
                }
                if(StringUtils.isNotEmpty(transferFilterData.getReserveField1())){
                    applyLoan = StringUtils.isNotEmpty(JSON.parseObject(transferFilterData.getReserveField1()).getString("applyLoan"))?JSON.parseObject(transferFilterData.getReserveField1()).getString("applyLoan"):"";
                    applyLoanTime = StringUtils.isNotEmpty(JSON.parseObject(transferFilterData.getReserveField1()).getString("applyLoanTime"))?JSON.parseObject(transferFilterData.getReserveField1()).getString("applyLoanTime"):"";
                }
                StringBuilder sb = new StringBuilder();
                sb.append((StringUtils.isNotEmpty(transferFilterData.getRequestId())?transferFilterData.getRequestId():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getOrgName())?transferFilterData.getOrgName():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getCustNum())?transferFilterData.getCustNum():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getSource())?transferFilterData.getSource():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getIfLogin())?transferFilterData.getIfLogin():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getLoginTime())?transferFilterData.getLoginTime():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getIfApply())?transferFilterData.getIfApply():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getApplyDt())?transferFilterData.getApplyDt():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getApplyResult())?transferFilterData.getApplyResult():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getAuditAmount())?transferFilterData.getAuditAmount():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getLentTime())?transferFilterData.getLentTime():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getLentAmount())?transferFilterData.getLentAmount():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getInsertTime())?transferFilterData.getInsertTime():"").concat(","));
                sb.append(applyLoan.concat(","));
                sb.append(applyLoanTime.concat(","));
                sb.append(cell);
                sb.append("\r\n");
                fw.append(sb.toString());
            }
            dataFilter.clear();
            data.clear();
        }
        int totalSize = custNumResult.size();
        custNumResult.clear();
        TransferFileTask updatetask = new TransferFileTask();
        updatetask.setId(transferFileTask.getId());
        updatetask.setStatus(2);
        updatetask.setFileName(transferFileTask.getFileName());
        updatetask.setFilePath(transferFileTask.getFilePath());
        updatetask.setTaskNumber(totalSize);
        updatetask.setUpdateTime(new Date());
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("玖富转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
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
     * @param startDate
     * @param endDate
     * @param pageIndex
     * @return
     */
    private Result<List<MarketingTransferSyncUser>> getOrderTransferData(String tcId, String startDate, String endDate,Integer pageIndex) {
        Integer limitStart = pageIndex * 2000;
        List<MarketingTransferSyncUser> transferOrderInsertTime = marketingTransferSyncUserMapper.getTransferByRequestData(tcId, startDate, endDate,limitStart);
        if (transferOrderInsertTime.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(transferOrderInsertTime);
    }

}
