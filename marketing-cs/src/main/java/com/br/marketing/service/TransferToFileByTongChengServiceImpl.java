package com.br.marketing.service;

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
import com.br.marketing.service.Impl.RuleRedisServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author songjuanjnuan
 * @Date 2022/09/19 16:31
 * @Description:同程转化数据提取
 */
@Slf4j
@Service
public class TransferToFileByTongChengServiceImpl implements ITransferToFileService {

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
    private MarketingSyncInfoMapper marketingSyncInfoMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    final static String EXECUTE_TIME = " 08:00:00";

    final static String TONGCHENG_TRANSFER_FILE = "tongcheng_zhuanhua_";

    final static DateTimeFormatter YYYYMMDDSHORTDF = DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT);
    final static DateTimeFormatter YYYYMMDDSHORTLINE = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_FORMAT);

    @Override
    public String isMyParam(String apiCode,String jobParameter) {
        return "";
    }

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode,String myParam) {
        List<TransferFileTask> resultList = new ArrayList<>();
        Date now = new Date();
        //可配置
        String execute = EXECUTE_TIME;
        if (StringUtils.isNotEmpty(marketingCommonConfig.getTongChengTransferExecuteTime())) {
            execute = " " + marketingCommonConfig.getTongChengTransferExecuteTime();
        }
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, execute);
        if (now.after(executeTime)) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("同程转化数据提取-开始执行,apiCode ={}", apiCode);
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
    public Result actionTransferToFile(TransferFileTask transferFileTask,String jobParameter) {
        log.warn("同程转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();//yyyyMMdd
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(recordDate).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        StringBuilder fileName = new StringBuilder();
        fileName.append(TONGCHENG_TRANSFER_FILE).append(recordDate).append(".txt");
        String fileAllPath = descPath.concat(fileName.toString());
        transferFileTask.setFileName(fileName.toString());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append("custNum,cell,userType,applyDt,applyResult,auditTime,ifLent,lentTime,lentAmount,effectiveTime,applyLoan");
            fw.append("\r\n");
            LocalDate localDate = LocalDate.parse(recordDate, YYYYMMDDSHORTDF);
            LocalDate startDate = localDate.minusDays(31);
            LocalDate endDate = localDate;
            writeTongChengTransferToFile(fw, apiCode, startDate, endDate,transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void writeTongChengTransferToFile(Writer fw, String apiCode, LocalDate startDate, LocalDate endDate,TransferFileTask transferFileTask) throws IOException {
        Long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        //去重后的Set
        HashSet custNumResult = new HashSet();
        int totalSize = 0;
        LocalDate date = endDate;
        while(true){
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
                Map<String, MarketingSyncUser> preUserMap = preUserByTask.stream().collect(
                        Collectors.groupingBy(MarketingSyncUser::getCustNum
                                , Collectors.collectingAndThen(
                                        Collectors.reducing((v1, v2) ->
                                                v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2)
                                        , Optional::get)));
                for (MarketingTransferSyncUser transferFilterData : dataFilter) {
                    String custNum = transferFilterData.getCustNum();
                    if (preUserMap.containsKey(custNum)) {
                        String appletDate = preUserMap.get(custNum).getAppletDate();
                        String effectiveTime = "";
                        String applyLoan ="";
                        LocalDate appletDateLocal = LocalDate.parse(appletDate, YYYYMMDDSHORTLINE);
                        if(appletDateLocal.isBefore(startDate) || appletDateLocal.isEqual(startDate)|| appletDateLocal.isAfter(endDate)){
                            continue;
                        }
                        String decode = BrCipherMaker.getInstance().decode(preUserMap.get(custNum).getCell());
                        String cell = StringUtils.isBlank(decode) ? preUserMap.get(custNum).getCell() : DigestUtils.md5DigestAsHex(decode.getBytes());
                        if(StringUtils.isNotEmpty(preUserMap.get(custNum).getReserveField1())){
                            effectiveTime = JSON.parseObject(preUserMap.get(custNum).getReserveField1()).getString("effectiveTime");
                        }
                        if(StringUtils.isNotEmpty(transferFilterData.getReserveField1())) {
                            applyLoan= JSON.parseObject(transferFilterData.getReserveField1()).getString("applyLoan");
                        }
                        //custNum、cell、userType、applyDt、applyResult、auditTime、ifLent、lentTime、lentAmount、effectiveTime、applyLoan
                        StringBuilder sb = new StringBuilder();
                        sb.append((StringUtils.isNotEmpty(transferFilterData.getCustNum())?transferFilterData.getCustNum():"").concat(","));
                        sb.append(cell.concat(","));
                        sb.append((StringUtils.isNotEmpty(transferFilterData.getUserType())?transferFilterData.getUserType():"").concat(","));
                        sb.append((StringUtils.isNotEmpty(transferFilterData.getApplyDt())?transferFilterData.getApplyDt().replace(":000",""):"").concat(","));
                        sb.append((StringUtils.isNotEmpty(transferFilterData.getApplyResult())?transferFilterData.getApplyResult():"").concat(","));
                        sb.append((StringUtils.isNotEmpty(transferFilterData.getAuditTime())?transferFilterData.getAuditTime():"").concat(","));
                        sb.append((StringUtils.isNotEmpty(transferFilterData.getIfLent())?transferFilterData.getIfLent():"").concat(","));
                        sb.append((StringUtils.isNotEmpty(transferFilterData.getLentTime())?transferFilterData.getLentTime().replace(":000",""):"").concat(","));
                        sb.append((StringUtils.isNotEmpty(transferFilterData.getLentAmount())?transferFilterData.getLentAmount():"").concat(","));
                        sb.append((StringUtils.isNotEmpty(effectiveTime)?effectiveTime.replace(":000",""):"").concat(","));
                        sb.append(StringUtils.isNotEmpty(applyLoan) ? applyLoan : "");
                        sb.append("\r\n");
                        fw.append(sb.toString());
                        totalSize = totalSize + 1;
                    }
                }
                dataFilter.clear();
                data.clear();
            }
            date = date.minusDays(1);
            mark = Boolean.TRUE;
            page = 0;
            if(date.isBefore(startDate)){
                break;
            }
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
        log.warn("同程转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
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
    private Result<List<MarketingTransferSyncUser>> getOrderTransferData(String tcId,String endDate,Integer pageIndex) {
        Integer limitStart = pageIndex * 2000;
        List<MarketingTransferSyncUser> transferOrderInsertTime = marketingTransferSyncUserMapper.getTransferByRequestData(tcId, endDate,limitStart);
        if (transferOrderInsertTime.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(transferOrderInsertTime);
    }

}
