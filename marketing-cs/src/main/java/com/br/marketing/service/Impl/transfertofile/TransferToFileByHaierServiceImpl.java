package com.br.marketing.service.Impl.transfertofile;

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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 海尔转化数据提取
 */
@Slf4j
@Service
public class TransferToFileByHaierServiceImpl implements ITransferToFileService {

    private static final String EXECUTE_TIME = "11:00:00";
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Autowired
    SyncConfigService syncConfigService;
    @Resource
    private TransferFileTaskMapper transferFileTaskMapper;
    @Autowired
    private RuleRedisServiceImpl ruleRedisService;

    @Autowired
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        return "";
    }

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode, String myParam) {
        List<TransferFileTask> resultList = new ArrayList<>();
        Date now = new Date();
        //可配置
        String execute = StringUtils.isBlank(marketingCommonConfig.getHaierTransferFileExecTime()) ? EXECUTE_TIME : marketingCommonConfig.getHaierTransferFileExecTime();
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, " " + execute);
        if (now.after(executeTime)) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            String fileName = String.format("haier_zhuanhua_%s.txt", yyyyMMdd);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("海尔转化数据提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName(fileName);
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
        log.warn("海尔转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(transferFileTask.getStartDate()).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append("custNum,userType,customName,registerTime,loginTime,applyDt,applyResult,auditTime,auditAmount,ifLent,lentTime,lentAmount,unlentAmount,settleTime,transformTime,loanStatus,applyLoanTime,transformTimeNew,lentAmountNew,couponType,couponTime,cell");
            fw.append("\r\n");
            writeHaierTransferToFile(fw, apiCode, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void writeHaierTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask) throws IOException {
        Long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        LocalDate date = LocalDate.now();
        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        int totalSize = 0;
        //去重后的Set
        HashSet custNumResult = new HashSet();
        while (mark) {
            Result<List<MarketingTransferSyncUser>> transferData = getOrderTransferData(tcId, date.toString(), page);
            if (!ResultCode.SUCCESS.getValue().equals(transferData.getCode())) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            List<MarketingTransferSyncUser> data = transferData.getData();
            List<MarketingTransferSyncUser> dataFilter = new ArrayList<>();
            for (MarketingTransferSyncUser marketingTransferSyncUser : data) {
                //过滤掉 同一custNum的其他insertTime数据，custNumResult
                if (StringUtils.isNotEmpty(marketingTransferSyncUser.getCustNum()) && custNumResult.add(marketingTransferSyncUser.getCustNum())) {
                    dataFilter.add(marketingTransferSyncUser);
                }
            }
            if (dataFilter.size() <= 0) {
                continue;
            }
            Set<String> set = dataFilter.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(apiCode, set);
            Map<String, MarketingSyncUser> userMap = preUserByTask.stream().collect(Collectors.toMap(MarketingSyncUser::getCustNum
                    , Function.identity(), BinaryOperator.maxBy(Comparator.comparing(MarketingSyncUser::getCreateTime))));
            //custNum,userType,customName,registerTime,loginTime,applyDt,applyResult,auditTime,auditAmount,ifLent,lentTime,lentAmount,unlentAmount,settleTime,transformTime,loanStatus,applyLoanTime,transformTimeNew,lentAmountNew,couponType,couponTime,cell            for (MarketingTransferSyncUser transferFilterData : dataFilter) {
            for (MarketingTransferSyncUser transferFilterData : dataFilter) {
                String custNum = transferFilterData.getCustNum();
                String loanStatus = "", applyLoanTime = "", transformTimeNew = "", lentAmountNew = "", couponType = "", couponTime = "", cell = "";
                if (userMap.containsKey(custNum)) {
                    String decode = BrCipherMaker.getInstance().decode(userMap.get(custNum).getCell());
                    cell = StringUtils.isBlank(decode) ? userMap.get(custNum).getCell() : DigestUtils.md5DigestAsHex(decode.getBytes());
                }
                if (StringUtils.isNotEmpty(transferFilterData.getReserveField1())) {
                    loanStatus = StringUtils.isNotEmpty(JSON.parseObject(transferFilterData.getReserveField1()).getString("loanStatus")) ? JSON.parseObject(transferFilterData.getReserveField1()).getString("loanStatus") : "";
                    applyLoanTime = StringUtils.isNotEmpty(JSON.parseObject(transferFilterData.getReserveField1()).getString("applyLoanTime")) ? JSON.parseObject(transferFilterData.getReserveField1()).getString("applyLoanTime") : "";
                    transformTimeNew = StringUtils.isNotEmpty(JSON.parseObject(transferFilterData.getReserveField1()).getString("transformTimeNew")) ? JSON.parseObject(transferFilterData.getReserveField1()).getString("transformTimeNew") : "";
                    lentAmountNew = StringUtils.isNotEmpty(JSON.parseObject(transferFilterData.getReserveField1()).getString("lentAmountNew")) ? JSON.parseObject(transferFilterData.getReserveField1()).getString("lentAmountNew") : "";
                    couponType = StringUtils.isNotEmpty(JSON.parseObject(transferFilterData.getReserveField1()).getString("couponType")) ? JSON.parseObject(transferFilterData.getReserveField1()).getString("couponType") : "";
                    couponTime = StringUtils.isNotEmpty(JSON.parseObject(transferFilterData.getReserveField1()).getString("couponTime")) ? JSON.parseObject(transferFilterData.getReserveField1()).getString("couponTime") : "";
                }
                StringBuilder sb = new StringBuilder();
                sb.append((StringUtils.isNotEmpty(transferFilterData.getCustNum()) ? transferFilterData.getCustNum() : "").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getUserType()) ? transferFilterData.getUserType() : "").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getCustomName()) ? transferFilterData.getCustomName() : "").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getRegisterTime()) ? transferFilterData.getRegisterTime().replace(":000", "") : "").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getLoginTime()) ? transferFilterData.getLoginTime().replace(":000", "") : "").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getApplyDt()) ? transferFilterData.getApplyDt().replace(":000", "") : "").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getApplyResult()) ? transferFilterData.getApplyResult().replace(":000", "") : "").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getAuditTime()) ? transferFilterData.getAuditTime().replace(":000", "") : "").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getAuditAmount()) ? transferFilterData.getAuditAmount() : "").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getIfLent()) ? transferFilterData.getIfLent() : "").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getLentTime()) ? transferFilterData.getLentTime().replace(":000", "") : "").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getLentAmount()) ? transferFilterData.getLentAmount() : "").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getUnlentAmount()) ? transferFilterData.getUnlentAmount() : "").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getSettleTime()) ? transferFilterData.getSettleTime().replace(":000", "") : "").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getTransformTime()) ? transferFilterData.getTransformTime().replace(":000", "") : "").concat(","));
                sb.append(loanStatus.concat(","));
                sb.append(applyLoanTime.replace(":000", "").concat(","));
                sb.append(transformTimeNew.concat(","));
                sb.append(lentAmountNew.concat(","));
                sb.append(couponType.concat(","));
                sb.append(couponTime.replace(":000", "").concat(","));
                sb.append(cell);
                sb.append("\r\n");
                fw.append(sb.toString());
            }
            totalSize = totalSize + dataFilter.size();
            dataFilter.clear();
            data.clear();
        }
        custNumResult.clear();
        TransferFileTask updatetask = new TransferFileTask();
        updatetask.setId(transferFileTask.getId());
        updatetask.setStatus(2);
        updatetask.setFileName(transferFileTask.getFileName());
        updatetask.setFilePath(transferFileTask.getFilePath());
        updatetask.setTaskNumber(totalSize);
        updatetask.setUpdateTime(new Date());
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("海尔转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);

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
    private Result<List<MarketingTransferSyncUser>> getOrderTransferData(String tcId, String endDate, Integer pageIndex) {
        Integer limitStart = pageIndex * 2000;
        List<MarketingTransferSyncUser> transferOrderInsertTime = marketingTransferSyncUserMapper.getTransferByRequestData(tcId, endDate, limitStart);
        if (transferOrderInsertTime.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(transferOrderInsertTime);
    }
}
