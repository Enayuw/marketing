package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.PhoneSaleRecordInfoDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.PhoneSaleInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author zhen.li
 * @Date 2022/04/07 15:31
 * @Description:宜信实时转化数据提取
 */
@Slf4j
@Service
public class TransferToFileByYiXinRealTimeServiceImpl implements ITransferToFileService {

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

    @Resource
    PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    final static String EXECUTE_TIME = " 20:00:00";
    final static String EXECUTE_TIME_NO_REALTIME = " 12:00:00";

    final static String YIXINREALTIMEFILE = "livetype_";
    final static String YIXIN_NOREALTIME_RESULT_FILE = "result_";
    final static String YIXIN_NOREALTIME_DAE_FILE = "dae_";
    final static String YIXIN_NOREALTIME_HIST_FILE = "hist_";

    final DateTimeFormatter YYYYMMDDSHORTDF = DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT);

    final DateTimeFormatter YYYYMMDDLINEDF = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_FORMAT);

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode) {
        List<TransferFileTask> resultList = new ArrayList<>();
        //实时数据提取
        Result<List<TransferFileTask>> listResultRealTime = buildTransferTaskRealTime(apiCode);
        if (ResultCode.SUCCESS.getValue().equals(listResultRealTime.getCode()) && listResultRealTime.getData().size() > 0){
            List<TransferFileTask> data = listResultRealTime.getData();
            resultList.addAll(data);
        }
        //非实时数据提取
        Boolean flag = StringUtils.isNotEmpty(marketingCommonConfig.getIsOpenYinXinTransferNoRealTimeExtract())?marketingCommonConfig.getIsOpenYinXinTransferNoRealTimeExtract():false;
        if(flag){
            Result<List<TransferFileTask>> listResultNoRealTime = buildTransferTaskNoRealTime(apiCode);
            if (ResultCode.SUCCESS.getValue().equals(listResultNoRealTime.getCode()) && listResultNoRealTime.getData().size() > 0){
                List<TransferFileTask> data = listResultNoRealTime.getData();
                resultList.addAll(data);
            }
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(resultList);
    }

    public Result<List<TransferFileTask>> buildTransferTaskNoRealTime(String apiCode) {
        List<TransferFileTask> resultList = new ArrayList<>();
        Date now = new Date();
        //可配置
        String execute = EXECUTE_TIME_NO_REALTIME;
        if (StringUtils.isNotEmpty(marketingCommonConfig.getYinXinTransferNoRealTimeExecuteTime())) {
            execute = " " + marketingCommonConfig.getYinXinTransferNoRealTimeExecuteTime();
        }
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, execute);
        if (now.after(executeTime)) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            //非实时数据提取规（type=19）fileType=2
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(2);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("宜信非实时数据提取(result)-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(2);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName("");
                transferFileTask.setFileChildDir("result");
                transferFileTask.setTaskNumber(0);
                transferFileTask.setStartDate(yyyyMMdd);
                transferFileTask.setContextId(transferFileContextId);
                transferFileTask.setCreateTime(new Date());
                transferFileTask.setUpdateTime(new Date());
                transferFileTaskMapper.insertSelective(transferFileTask);
                resultList.add(transferFileTask);
            }
            //非实时数据提取规（type=4、15）fileType=3
            taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(3);
            transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("宜信非实时数据提取(dae)-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(3);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName("");
                transferFileTask.setFileChildDir("dae");
                transferFileTask.setTaskNumber(0);
                transferFileTask.setStartDate(yyyyMMdd);
                transferFileTask.setContextId(transferFileContextId);
                transferFileTask.setCreateTime(new Date());
                transferFileTask.setUpdateTime(new Date());
                transferFileTaskMapper.insertSelective(transferFileTask);
                resultList.add(transferFileTask);
            }
            //非实时数据提取规（type=7、8、15）fileType=4
            taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(4);
            transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("宜信非实时数据提取(hist)-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(4);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName("");
                transferFileTask.setFileChildDir("hist");
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
    public Result actionTransferToFile(TransferFileTask transferFileTask){
        if(1==transferFileTask.getFileType()){
            return actionTransferToFileRealTime(transferFileTask);
        }else if(2==transferFileTask.getFileType()){
            return actionTransferToFileResult(transferFileTask);
        }else if(3==transferFileTask.getFileType()){
            return actionTransferToFileDae(transferFileTask);
        }else if(4==transferFileTask.getFileType()){
            return actionTransferToFileHist(transferFileTask);
        }
        log.error("未找到对应的actionTransferToFile方法,请检查fileType");
        return new Result().setCode(ResultCode.FAIL.getValue()).setDate("未找到对应的actionTransferToFile方法,请检查fileType");
    }

    public Result actionTransferToFileResult(TransferFileTask transferFileTask) {
        log.warn("宜信非实时数据提取(result)-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();//yyyyMMdd
        String date = LocalDate.parse(recordDate, YYYYMMDDSHORTDF).format(YYYYMMDDLINEDF);//yyyy-MM-dd
        String descPath = path.concat("transferToFile/").concat(apiCode).concat("/").concat(recordDate).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        StringBuilder fileName = new StringBuilder();
        fileName.append(YIXIN_NOREALTIME_RESULT_FILE).append(recordDate).append(".txt");
        String fileAllPath = descPath.concat(fileName.toString());
        transferFileTask.setFileName(fileName.toString());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append("custNum,ifRegister,registerTime,ifApply,applyDt,applyResult,auditAmount,applyLoan,ifLent,lentTime,lentAmount,userType,raiseLimit,raiseLimitTime,raiseLimitResult,insertTime,type,loantResult,raiseLimiType,raiseLimiSuccess,rate");
            fw.append("\r\n");
            writeYiXinNoRealTimeResult(fw, apiCode, date, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    public Result actionTransferToFileDae(TransferFileTask transferFileTask) {
        log.warn("宜信非实时数据提取(dae)-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();//yyyyMMdd
        String date = LocalDate.parse(recordDate, YYYYMMDDSHORTDF).format(YYYYMMDDLINEDF);//yyyy-MM-dd
        String descPath = path.concat("transferToFile/").concat(apiCode).concat("/").concat(recordDate).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        StringBuilder fileName = new StringBuilder();
        fileName.append(YIXIN_NOREALTIME_DAE_FILE).append(recordDate).append(".txt");
        String fileAllPath = descPath.concat(fileName.toString());
        transferFileTask.setFileName(fileName.toString());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append("custNum,cell,type,insertime");
            fw.append("\r\n");
            writeYiXinNoRealTimeDae(fw, apiCode, date, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    public Result actionTransferToFileHist(TransferFileTask transferFileTask) {
        log.warn("宜信非实时数据提取(hist)-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();//yyyyMMdd
        String date = LocalDate.parse(recordDate, YYYYMMDDSHORTDF).format(YYYYMMDDLINEDF);//yyyy-MM-dd
        String descPath = path.concat("transferToFile/").concat(apiCode).concat("/").concat(recordDate).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        StringBuilder fileName = new StringBuilder();
        fileName.append(YIXIN_NOREALTIME_HIST_FILE).append(recordDate).append(".txt");
        String fileAllPath = descPath.concat(fileName.toString());
        transferFileTask.setFileName(fileName.toString());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append("custNum,cell,type,insertime");
            fw.append("\r\n");
            writeYiXinNoRealTimeHist(fw, apiCode, date, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void writeYiXinNoRealTimeResult(Writer fw, String apiCode, String date, TransferFileTask transferFileTask) throws IOException {
        Long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        //过滤Type的Set
        HashSet custNumFilterType = new HashSet();
        //去重后的Set
        HashSet custNumResult = new HashSet();
        while (mark) {
            Result<List<MarketingTransferSyncUser>> transferData = getOrderTransferData(tcId, date, page);
            if (!ResultCode.SUCCESS.getValue().equals(transferData.getCode())) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            List<MarketingTransferSyncUser> data = transferData.getData();
            List<MarketingTransferSyncUser> dataFilter = new ArrayList<>();
            for (MarketingTransferSyncUser marketingTransferSyncUser : data) {
                JSONObject reserveField1 = JSON.parseObject(marketingTransferSyncUser.getReserveField1());
                //实时数据transformType非1
                if ((StringUtils.isEmpty(reserveField1.getString("transformType"))) || (!"1".equals(reserveField1.getString("transformType")))) {
                    if (!"19".equals(marketingTransferSyncUser.getType())) {
                        custNumFilterType.add(marketingTransferSyncUser.getCustNum());
                        continue;
                    }
                    //过滤掉 同一custNum的其他insertTime数据，custNumResult
                    if (custNumFilterType.add(marketingTransferSyncUser.getCustNum()) && custNumResult.add(marketingTransferSyncUser.getCustNum())) {
                        dataFilter.add(marketingTransferSyncUser);
                    }

                }
            }
            if (dataFilter.size() <= 0) {
                log.warn("宜信非实时数据提取-该批次无type为19的数据,apiCode = {}", apiCode);
                continue;
            }

            for (MarketingTransferSyncUser transferFilterData : dataFilter) {
                String applyLoan = JSON.parseObject(transferFilterData.getReserveField1()).getString("applyLoan");
                String raiseLimit = JSON.parseObject(transferFilterData.getReserveField1()).getString("raiseLimit");
                String raiseLimitTime = JSON.parseObject(transferFilterData.getReserveField1()).getString("raiseLimitTime");
                String raiseLimitResult = JSON.parseObject(transferFilterData.getReserveField1()).getString("raiseLimitResult");
                String loantResult = JSON.parseObject(transferFilterData.getReserveField1()).getString("loantResult");
                String raiseLimiType = JSON.parseObject(transferFilterData.getReserveField1()).getString("raiseLimiType");
                String raiseLimiSuccess = JSON.parseObject(transferFilterData.getReserveField1()).getString("raiseLimiSuccess");
                String rate = JSON.parseObject(transferFilterData.getReserveField1()).getString("rate");

                StringBuilder sb = new StringBuilder();
                sb.append(transferFilterData.getCustNum().concat(","));
                sb.append(transferFilterData.getIfRegister().concat(","));
                sb.append(transferFilterData.getRegisterTime().concat(","));
                sb.append(transferFilterData.getIfApply().concat(","));
                sb.append(transferFilterData.getApplyDt().concat(","));
                sb.append(transferFilterData.getApplyResult().concat(","));
                sb.append(transferFilterData.getAuditAmount().concat(","));
                sb.append(applyLoan.concat(","));
                sb.append(transferFilterData.getIfLent().concat(","));
                sb.append(transferFilterData.getLentTime().concat(","));
                sb.append(transferFilterData.getLentAmount().concat(","));
                sb.append(transferFilterData.getUserType().concat(","));
                sb.append(raiseLimit.concat(","));
                sb.append(raiseLimitTime.concat(","));
                sb.append(raiseLimitResult.concat(","));
                sb.append(transferFilterData.getInsertTime().concat(","));
                sb.append(transferFilterData.getType().concat(","));
                sb.append(loantResult.concat(","));
                sb.append(raiseLimiType.concat(","));
                sb.append(raiseLimiSuccess.concat(","));
                sb.append(rate);
                sb.append("\r\n");
                fw.append(sb.toString());
            }
            dataFilter.clear();
            data.clear();
        }
        int totalSize = custNumResult.size();
        custNumResult.clear();
        custNumFilterType.clear();
        TransferFileTask updatetask = new TransferFileTask();
        updatetask.setId(transferFileTask.getId());
        updatetask.setStatus(2);
        updatetask.setFileName(transferFileTask.getFileName());
        updatetask.setFilePath(transferFileTask.getFilePath());
        updatetask.setTaskNumber(totalSize);
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("宜信非实时数据提取(result)-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
    }

    private void writeYiXinNoRealTimeDae(Writer fw, String apiCode, String date, TransferFileTask transferFileTask) throws IOException{
        Long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        //过滤Type的Set
        HashSet custNumFilterType = new HashSet();
        //去重后的Set
        HashSet custNumResult = new HashSet();
        while (mark) {
            Result<List<MarketingTransferSyncUser>> transferData = getOrderTransferData(tcId, date, page);
            if (!ResultCode.SUCCESS.getValue().equals(transferData.getCode())) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            List<MarketingTransferSyncUser> data = transferData.getData();
            List<MarketingTransferSyncUser> dataFilter = new ArrayList<>();
            for (MarketingTransferSyncUser marketingTransferSyncUser : data) {
                JSONObject reserveField1 = JSON.parseObject(marketingTransferSyncUser.getReserveField1());
                //实时数据transformType非1
                if ((StringUtils.isEmpty(reserveField1.getString("transformType"))) || (!"1".equals(reserveField1.getString("transformType")))) {
                    if (!"4".equals(marketingTransferSyncUser.getType()) || !"15".equals(marketingTransferSyncUser.getType())) {
                        custNumFilterType.add(marketingTransferSyncUser.getCustNum());
                        continue;
                    }
                    //过滤掉 同一custNum的其他insertTime数据，custNumResult
                    if (custNumFilterType.add(marketingTransferSyncUser.getCustNum()) && custNumResult.add(marketingTransferSyncUser.getCustNum())) {
                        dataFilter.add(marketingTransferSyncUser);
                    }
                }
            }
            if (dataFilter.size() <= 0) {
                log.warn("宜信非实时数据提取-该批次无type为4、15的数据,apiCode = {}", apiCode);
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
                String type = transferFilterData.getType();
                String cell = "";
                if (preUserMap.containsKey(custNum)) {
                    String decode = BrCipherMaker.getInstance().decode(preUserMap.get(custNum).getCell());
                    cell = StringUtils.isBlank(decode) ? preUserMap.get(custNum).getCell() : DigestUtils.md5DigestAsHex(decode.getBytes());
                }
                StringBuilder sb = new StringBuilder();
                sb.append(custNum.concat(","));
                sb.append(cell.concat(","));
                sb.append(type.concat(","));
                sb.append(transferFilterData.getInsertTime());
                sb.append("\r\n");
                fw.append(sb.toString());
            }
            dataFilter.clear();
            data.clear();
        }
        int totalSize = custNumResult.size();
        custNumResult.clear();
        custNumFilterType.clear();
        TransferFileTask updatetask = new TransferFileTask();
        updatetask.setId(transferFileTask.getId());
        updatetask.setStatus(2);
        updatetask.setFileName(transferFileTask.getFileName());
        updatetask.setFilePath(transferFileTask.getFilePath());
        updatetask.setTaskNumber(totalSize);
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("宜信非实时数据提取(dae)-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
    }

    private void writeYiXinNoRealTimeHist(Writer fw, String apiCode, String date, TransferFileTask transferFileTask) throws IOException{
        Long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        //过滤Type的Set
        HashSet custNumFilterType = new HashSet();
        //去重后的Set
        HashSet custNumResult = new HashSet();
        int totalSize = 0;
        while (mark) {
            Result<List<MarketingTransferSyncUser>> transferData = getOrderTransferData(tcId, date, page);
            if (!ResultCode.SUCCESS.getValue().equals(transferData.getCode())) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            List<MarketingTransferSyncUser> data = transferData.getData();
            List<MarketingTransferSyncUser> dataFilter = new ArrayList<>();
            for (MarketingTransferSyncUser marketingTransferSyncUser : data) {
                JSONObject reserveField1 = JSON.parseObject(marketingTransferSyncUser.getReserveField1());
                //实时数据transformType非1
                if ((StringUtils.isEmpty(reserveField1.getString("transformType"))) || (!"1".equals(reserveField1.getString("transformType")))) {
                    if (!"7".equals(marketingTransferSyncUser.getType()) || !"8".equals(marketingTransferSyncUser.getType()) || !"15".equals(marketingTransferSyncUser.getType())) {
                        custNumFilterType.add(marketingTransferSyncUser.getCustNum());
                        continue;
                    }
                    //过滤掉 同一custNum的其他insertTime数据，custNumResult
                    if (custNumFilterType.add(marketingTransferSyncUser.getCustNum()) && custNumResult.add(marketingTransferSyncUser.getCustNum())) {
                        dataFilter.add(marketingTransferSyncUser);
                    }
                }
            }

            Set<String> set = dataFilter.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(apiCode, set);
            Map<String, MarketingSyncUser> preUserMap = preUserByTask.stream().collect(
                    Collectors.groupingBy(MarketingSyncUser::getCustNum
                            , Collectors.collectingAndThen(
                                    Collectors.reducing((v1, v2) ->
                                            v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2)
                                    , Optional::get)));
            //过滤 该type符合转电销type且60天没有变化的数据（推电销type无变化且推电销次数<=2）
            List<MarketingTransferSyncUser> resultFilter = new ArrayList<>();
            String _60startDay = new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.addDays(new Date(), -60));
            String endDay = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            PhoneSaleRecordInfoDTO recordInfoDTO = new PhoneSaleRecordInfoDTO();
            recordInfoDTO.setCustNums(set);
            recordInfoDTO.setApiCode(apiCode);
            recordInfoDTO.setStartDate(_60startDay);
            recordInfoDTO.setEndDate(endDay);
            recordInfoDTO.setTransferType("0");
            List<PhoneSaleInfoVO> _60records = phoneSaleExtendInfoMapper.getDxRecordByTransferType(recordInfoDTO);
            Map<String, List<PhoneSaleInfoVO>> _60filterCustNumsMap = _60records.stream().collect(Collectors.groupingBy(PhoneSaleInfoVO::getCustNum));
            for(MarketingTransferSyncUser transferFilterData : dataFilter){
                List<PhoneSaleInfoVO> phoneSaleInfoVOS = _60filterCustNumsMap.get(transferFilterData.getCustNum());
                if(phoneSaleInfoVOS.size()>2){
                    continue;
                }
                if (phoneSaleInfoVOS != null && phoneSaleInfoVOS.size()>0){
                    PhoneSaleInfoVO vo = phoneSaleInfoVOS.get(0);
                    if (vo.getType().equals(transferFilterData.getType())){
                        if(phoneSaleInfoVOS.size()>1){
                            PhoneSaleInfoVO vo1 = phoneSaleInfoVOS.get(1);
                            if (vo1.getType().equals(transferFilterData.getType())){
                                resultFilter.add(transferFilterData);
                            }
                        }
                    }
                }
            }

            if (resultFilter.size() <= 0) {
                log.warn("宜信非实时数据提取-该批次无符合hist的数据,apiCode = {}", apiCode);
                continue;
            }

            for (MarketingTransferSyncUser transferFilterData : resultFilter) {
                String custNum = transferFilterData.getCustNum();
                String type = transferFilterData.getType();
                String cell = "";
                if (preUserMap.containsKey(custNum)) {
                    String decode = BrCipherMaker.getInstance().decode(preUserMap.get(custNum).getCell());
                    cell = StringUtils.isBlank(decode) ? preUserMap.get(custNum).getCell() : DigestUtils.md5DigestAsHex(decode.getBytes());
                }
                StringBuilder sb = new StringBuilder();
                sb.append(custNum.concat(","));
                sb.append(cell.concat(","));
                sb.append(type.concat(","));
                sb.append(transferFilterData.getInsertTime());
                sb.append("\r\n");
                fw.append(sb.toString());
            }
            dataFilter.clear();
            totalSize = resultFilter.size();
            resultFilter.clear();
            data.clear();
        }
        custNumResult.clear();
        custNumFilterType.clear();
        TransferFileTask updatetask = new TransferFileTask();
        updatetask.setId(transferFileTask.getId());
        updatetask.setStatus(2);
        updatetask.setFileName(transferFileTask.getFileName());
        updatetask.setFilePath(transferFileTask.getFilePath());
        updatetask.setTaskNumber(totalSize);
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("宜信非实时数据提取(hist)-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
    }

    //=======================实时/非实时分隔线============================================================================================================

    public Result<List<TransferFileTask>> buildTransferTaskRealTime(String apiCode) {
        List<TransferFileTask> resultList = new ArrayList<>();
        Date now = new Date();
        //可配置
        String execute = EXECUTE_TIME;
        if (StringUtils.isNotEmpty(marketingCommonConfig.getYinXinTransferRealTimeExecuteTime())) {
            execute = " " + marketingCommonConfig.getYinXinTransferRealTimeExecuteTime();
        }
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, execute);
        if (now.after(executeTime)) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("宜信实时数据提取-开始执行,apiCode ={}", apiCode);
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

    String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String concat = apiCode.concat("_").concat(yyyyMMdd).concat("_").concat(contextId.toString());
        return concat;
    }

    public Result actionTransferToFileRealTime(TransferFileTask transferFileTask) {
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();
        String date = LocalDate.parse(recordDate, YYYYMMDDSHORTDF).format(YYYYMMDDLINEDF);
        String descPath = path.concat("transferToFile/").concat(apiCode).concat("/").concat(recordDate).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        StringBuilder fileName = new StringBuilder();
        fileName.append(YIXINREALTIMEFILE).append(recordDate).append(".txt");
        String fileAllPath = descPath.concat(fileName.toString());
        transferFileTask.setFileName(fileName.toString());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append("custNum,cell,liveType,insertime");
            fw.append("\r\n");
            writeYiXinRealTimeData(fw, apiCode, date, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void writeYiXinRealTimeData(Writer fw, String apiCode, String date, TransferFileTask transferFileTask) throws IOException {
        Long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        //过滤liveType的Set
        HashSet custNumFilterType = new HashSet();
        //去重后的Set
        HashSet custNumResult = new HashSet();
        while (mark) {
            Result<List<MarketingTransferSyncUser>> transferData = getOrderTransferData(tcId, date, page);
            if (!ResultCode.SUCCESS.getValue().equals(transferData.getCode())) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            List<MarketingTransferSyncUser> data = transferData.getData();
            List<MarketingTransferSyncUser> dataFilter = new ArrayList<>();
            for (MarketingTransferSyncUser marketingTransferSyncUser : data) {
                JSONObject reserveField1 = JSON.parseObject(marketingTransferSyncUser.getReserveField1());
                //实时数据transformType=1
                if ((!reserveField1.isEmpty())
                        && ("1".equals(reserveField1.getString("transformType")))) {
                    if (!"4".equals(reserveField1.getString("liveType"))) {
                        custNumFilterType.add(marketingTransferSyncUser.getCustNum());
                        continue;
                    }
                    //过滤掉 同一custNum的其他insertTime数据，custNumResult
                    if (custNumFilterType.add(marketingTransferSyncUser.getCustNum()) && custNumResult.add(marketingTransferSyncUser.getCustNum())) {
                        dataFilter.add(marketingTransferSyncUser);
                    }

                }
            }
            if (dataFilter.size() <= 0) {
                log.warn("宜信实时数据提取-该批次无liveType为4的数据,apiCode = {}", apiCode);
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
                String liveType = JSON.parseObject(transferFilterData.getReserveField1()).getString("liveType");
                String cell = "";
                if (preUserMap.containsKey(custNum)) {
                    String decode = BrCipherMaker.getInstance().decode(preUserMap.get(custNum).getCell());
                    cell = StringUtils.isBlank(decode) ? preUserMap.get(custNum).getCell() : DigestUtils.md5DigestAsHex(decode.getBytes());

                }
                StringBuilder sb = new StringBuilder();
                sb.append(custNum.concat(","));
                sb.append(cell.concat(","));
                sb.append(liveType.concat(","));
                sb.append(transferFilterData.getInsertTime());
                sb.append("\r\n");
                fw.append(sb.toString());
            }
            dataFilter.clear();
            data.clear();
        }
        int totalSize = custNumResult.size();
        custNumResult.clear();
        custNumFilterType.clear();
        TransferFileTask updatetask = new TransferFileTask();
        updatetask.setId(transferFileTask.getId());
        updatetask.setStatus(2);
        updatetask.setFileName(transferFileTask.getFileName());
        updatetask.setFilePath(transferFileTask.getFilePath());
        updatetask.setTaskNumber(totalSize);
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("宜信实时数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
    }


    /**
     * 获取转化数据
     * 按照inserttime排序
     *
     * @param tcId
     * @param date
     * @param pageIndex
     * @return
     */
    private Result<List<MarketingTransferSyncUser>> getOrderTransferData(String tcId, String date, Integer pageIndex) {
        Integer limitStart = pageIndex * 5000;
        List<MarketingTransferSyncUser> transferOrderInsertTime = marketingTransferSyncUserMapper.getTransferOrderInsertTime(tcId, date, limitStart);
        if (transferOrderInsertTime.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(transferOrderInsertTime);
    }

}
