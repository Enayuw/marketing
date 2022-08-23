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
import com.br.marketing.mapper.*;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
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
    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    final static String EXECUTE_TIME = " 20:00:00";
    final static String EXECUTE_TIME_NO_REALTIME = " 12:00:00";
    final static String EXECUTE_TIME_REALPASS = " 12:00:00";

    final static String YIXINREALTIMEFILE = "livetype_";
    final static String YIXIN_NOREALTIME_RESULT_FILE = "result_";
    final static String YIXIN_NOREALTIME_DAE_FILE = "dae_";
    final static String YIXIN_NOREALTIME_HIST_FILE = "hist_";

    final static String YIXIN_NOREALTIME_PASS_FILE = "pass_";
    final static String YIXIN_REALTIME_PASS_FILE = "yixin_real_";

    final DateTimeFormatter YYYYMMDDSHORTDF = DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT);

    final DateTimeFormatter YYYYMMDDLINEDF = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_FORMAT);

    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        return "";
    }

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
        //实时数real-pass据提取
        Result<List<TransferFileTask>> listResultRealPass = buildTransferTaskRealPass(apiCode);
        if (ResultCode.SUCCESS.getValue().equals(listResultRealPass.getCode()) && listResultRealPass.getData().size() > 0){
            List<TransferFileTask> data = listResultRealPass.getData();
            resultList.addAll(data);
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(resultList);
    }

    public Result<List<TransferFileTask>> buildTransferTaskRealPass(String apiCode) {
        List<TransferFileTask> resultList = new ArrayList<>();
        Date now = new Date();
        //可配置
        String execute = EXECUTE_TIME_REALPASS;
        if (StringUtils.isNotEmpty(marketingCommonConfig.getYinXinTransferRealPassExecuteTime())) {
            execute = " " + marketingCommonConfig.getYinXinTransferRealPassExecuteTime();
        }
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, execute);
        if (now.after(executeTime)) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            //D20220706宜信实时数据逻辑处理-3710012
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(6);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("宜信实时数据提取(real-pass)-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                //Long transferFileContextId = 123L;
                String batchNumber = createBatchNumber(apiCode, transferFileContextId);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(6);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName("");
                transferFileTask.setFileChildDir("data_yixin_pass");
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

            //宜信非实时数据提取-pass-3710012
            taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(5);
            transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("宜信非实时数据提取-pass,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(5);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName("");
                transferFileTask.setFileChildDir("data_yixin_pass");
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
    public Result actionTransferToFile(TransferFileTask transferFileTask,String jobParameter){
        if(1==transferFileTask.getFileType()){
            return actionTransferToFileRealTime(transferFileTask);
        }else if(2==transferFileTask.getFileType()){
            return actionTransferToFileResult(transferFileTask);
        }else if(3==transferFileTask.getFileType()){
            return actionTransferToFileDae(transferFileTask);
        }else if(4==transferFileTask.getFileType()){
            return actionTransferToFileHist(transferFileTask);
        }else if(5==transferFileTask.getFileType()){
            return actionTransferToFilePass(transferFileTask);
        }else if(6==transferFileTask.getFileType()){
            return actionTransferToFileRealPass(transferFileTask);
        }
        log.error("未找到对应的actionTransferToFile方法,请检查fileType");
        return new Result().setCode(ResultCode.FAIL.getValue()).setDate("未找到对应的actionTransferToFile方法,请检查fileType");
    }

    private Result actionTransferToFileRealPass(TransferFileTask transferFileTask) {
        log.warn("宜信实时数据提取real-pass)-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();//yyyyMMdd
        String descPath = path.concat("transferToFile/").concat(apiCode).concat("/").concat(recordDate).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        StringBuilder fileName = new StringBuilder();
        fileName.append(YIXIN_REALTIME_PASS_FILE).append(recordDate).append(".txt");
        String fileAllPath = descPath.concat(fileName.toString());
        transferFileTask.setFileName(fileName.toString());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append("custNum,cell,liveType,createTime");
            fw.append("\r\n");
            writeYiXinRealTimePass(fw, apiCode, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void writeYiXinRealTimePass(Writer fw, String apiCode, TransferFileTask transferFileTask) throws IOException {
        Long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        LocalDate startDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().minusDays(7);
        LocalDate endDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().minusDays(1);
        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        int totalSize = 0;
        //去重后的Set
        HashSet custNumResult = new HashSet();
        while(true){
            while (mark) {
                List<MarketingTransferSyncUser> transferData = marketingTransferSyncUserMapper.getTransferByTransformTypeAndStatus(tcId, apiCode, endDate.toString(),page * 2000);
                if (CollectionUtils.isEmpty(transferData)){
                    mark = Boolean.FALSE;
                    continue;
                }
                page++;
                List<MarketingTransferSyncUser> custNumFilter = new ArrayList<>();
                for (MarketingTransferSyncUser marketingTransferSyncUser : transferData) {
                    if(StringUtils.isNotEmpty(marketingTransferSyncUser.getReserveField1())){
                        JSONObject reserveField1 = JSON.parseObject(marketingTransferSyncUser.getReserveField1());
                        //实时数据transformType=1
                        if ((StringUtils.isNotEmpty(reserveField1.getString("transformType"))) && ("1".equals(reserveField1.getString("transformType")))) {
                            //过滤掉 同一custNum的其他insertTime数据，custNumResult
                            if (custNumResult.add(marketingTransferSyncUser.getCustNum())) {
                                custNumFilter.add(marketingTransferSyncUser);
                            }
                        }
                    }
                }
                if (CollectionUtils.isEmpty(custNumFilter)) {
                    continue;
                }
                Set<String> custNums = custNumFilter.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
                List<MarketingSyncUser> newestByCustNum = marketingSyncUserMapper.getNewestByCustNums(apiCode, custNums);

                List<MarketingTransferSyncUser> dataFilter = new ArrayList<>();
                for(MarketingTransferSyncUser custNumData : custNumFilter){
                    for(MarketingSyncUser custNum : newestByCustNum){
                        if(StringUtils.isNotEmpty(custNumData.getCustNum()) && StringUtils.isNotEmpty(custNum.getCustNum()) && custNumData.getCustNum().equals(custNum.getCustNum())){
                            //上传表取最新的案件状态
                            if(custNum.getStatus()!=null && custNum.getStatus()==1){
                                dataFilter.add(custNumData);
                            }
                        }
                    }
                }
                if (CollectionUtils.isEmpty(dataFilter)) {
                    continue;
                }
                //剔除caseEffecctive=0的案件编号
                List<MarketingTransferSyncUser> resultData = eliminateCaseEffective(tcId,dataFilter,apiCode);
                if (resultData.size() <= 0) {
                    continue;
                }
                Set<String> set = resultData.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
                List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(apiCode, set);
                Map<String, MarketingSyncUser> preUserMap = preUserByTask.stream().collect(
                        Collectors.groupingBy(MarketingSyncUser::getCustNum
                                , Collectors.collectingAndThen(
                                        Collectors.reducing((v1, v2) ->
                                                v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2)
                                        , Optional::get)));

                for (MarketingTransferSyncUser transferFilterData : resultData) {
                    String custNum = transferFilterData.getCustNum();
                    String cell = "";
                    if (preUserMap.containsKey(custNum)) {
                        String decode = BrCipherMaker.getInstance().decode(preUserMap.get(custNum).getCell());
                        cell = StringUtils.isBlank(decode) ? preUserMap.get(custNum).getCell() : DigestUtils.md5DigestAsHex(decode.getBytes());
                    }
                    String createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(transferFilterData.getCreateTime());
                    //custNum,cell,liveType,createTime
                    JSONObject reserveField1 = JSON.parseObject(transferFilterData.getReserveField1());
                    String liveType = StringUtils.isNotEmpty(reserveField1.getString("liveType"))?reserveField1.getString("liveType"):"";
                    StringBuilder sb = new StringBuilder();
                    sb.append(custNum.concat(","));
                    sb.append(cell.concat(","));
                    sb.append(liveType.concat(","));
                    sb.append(createTime);
                    sb.append("\r\n");
                    fw.append(sb.toString());
                }
                totalSize = totalSize + resultData.size();
                dataFilter.clear();
                resultData.clear();
                custNumFilter.clear();
                transferData.clear();
            }
            endDate = endDate.minusDays(1);
            mark = Boolean.TRUE;
            page = 0;
            if(endDate.isBefore(startDate)){
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
        updatetask.setFileType(6);
        updatetask.setUpdateTime(new Date());
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("宜信实时数据提取(real-pass)-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);

    }
    private Result actionTransferToFilePass(TransferFileTask transferFileTask) {
        log.warn("宜信非实时数据提取pass)-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();//yyyyMMdd
        String descPath = path.concat("transferToFile/").concat(apiCode).concat("/").concat(recordDate).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        StringBuilder fileName = new StringBuilder();
        fileName.append(YIXIN_NOREALTIME_PASS_FILE).append(recordDate).append(".txt");
        String fileAllPath = descPath.concat(fileName.toString());
        transferFileTask.setFileName(fileName.toString());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append("custNum,cell,applyResult,applyDt,userType,type,createtime");
            fw.append("\r\n");
            writeYiXinNoRealTimePass(fw, apiCode, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void writeYiXinNoRealTimePass(Writer fw, String apiCode, TransferFileTask transferFileTask) throws IOException {
        Long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        //去重后的Set
        Set<String> custNumResult = new HashSet();
        int totalSize = 0;
        while (mark) {
            List<MarketingTransferSyncUser> transferData = marketingTransferSyncUserMapper.getTransferByApplyDt(tcId, apiCode, page * 2000);
            if (CollectionUtils.isEmpty(transferData)){
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            List<MarketingTransferSyncUser> dataFilter = new ArrayList<>();
            for (MarketingTransferSyncUser marketingTransferSyncUser : transferData) {
                JSONObject reserveField1 = JSON.parseObject(marketingTransferSyncUser.getReserveField1());
                //非实时数据 transformType!=1
                if (!custNumResult.contains(marketingTransferSyncUser.getCustNum())){
                    if (reserveField1 == null){
                        dataFilter.add(marketingTransferSyncUser);
                        custNumResult.add(marketingTransferSyncUser.getCustNum());
                    }else{
                        String transformType = reserveField1.getString("transformType");
                        if (StringUtils.isBlank(transformType) || !"1".equals(transformType)){
                            dataFilter.add(marketingTransferSyncUser);
                            custNumResult.add(marketingTransferSyncUser.getCustNum());
                        }
                    }
                }
            }
            if (CollectionUtils.isEmpty(dataFilter)) {
                continue;
            }
            //剔除caseEffecctive=0的案件编号
            List<MarketingTransferSyncUser> resultData = eliminateCaseEffective(tcId,dataFilter,apiCode);
            if (resultData.size() <= 0) {
                continue;
            }
            Set<String> set = resultData.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(apiCode, set);
            Map<String, MarketingSyncUser> preUserMap = preUserByTask.stream().collect(
                    Collectors.groupingBy(MarketingSyncUser::getCustNum
                            , Collectors.collectingAndThen(
                                    Collectors.reducing((v1, v2) ->
                                            v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2)
                                    , Optional::get)));

            for (MarketingTransferSyncUser transferFilterData : resultData) {
                String custNum = transferFilterData.getCustNum();
                String cell = "";
                if (preUserMap.containsKey(custNum)) {
                    String decode = BrCipherMaker.getInstance().decode(preUserMap.get(custNum).getCell());
                    cell = StringUtils.isBlank(decode) ? preUserMap.get(custNum).getCell() : DigestUtils.md5DigestAsHex(decode.getBytes());
                }
                String applyResult = transferFilterData.getApplyResult();
                String applyDt = transferFilterData.getApplyDt();
                try {
                    Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(applyDt);
                    applyDt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(parse);
                } catch (ParseException e) {
                    log.warn("非时间格式 {} -- {}",transferFilterData.getId(),applyDt);
                }
                String userType = transferFilterData.getUserType();
                String type = transferFilterData.getType();
                String createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(transferFilterData.getCreateTime());
                StringBuilder sb = new StringBuilder();
                sb.append(custNum.concat(","));
                sb.append(cell.concat(","));
                sb.append(applyResult.concat(","));
                sb.append(applyDt.concat(","));
                sb.append(userType.concat(","));
                sb.append(type.concat(","));
                sb.append(createTime);
                sb.append("\r\n");
                fw.append(sb.toString());
            }
            totalSize = totalSize + resultData.size();
            dataFilter.clear();
            resultData.clear();
            transferData.clear();
        }

        custNumResult.clear();
        TransferFileTask updatetask = new TransferFileTask();
        updatetask.setId(transferFileTask.getId());
        updatetask.setStatus(2);
        updatetask.setFileName(transferFileTask.getFileName());
        updatetask.setFilePath(transferFileTask.getFilePath());
        updatetask.setTaskNumber(totalSize);
        updatetask.setFileType(5);
        updatetask.setUpdateTime(new Date());
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("宜信非实时数据提取(pass)-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);

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
                String applyLoan = "";
                String raiseLimit = "";
                String raiseLimitTime = "";
                String raiseLimitResult = "";
                String loantResult = "";
                String raiseLimiType = "";
                String raiseLimiSuccess = "";
                String rate = "";
                if(StringUtils.isNotEmpty(transferFilterData.getReserveField1())){
                    applyLoan = StringUtils.isNotEmpty(JSON.parseObject(transferFilterData.getReserveField1()).getString("applyLoan"))?JSON.parseObject(transferFilterData.getReserveField1()).getString("applyLoan"):"";
                    raiseLimit = StringUtils.isNotEmpty(JSON.parseObject(transferFilterData.getReserveField1()).getString("raiseLimit"))?JSON.parseObject(transferFilterData.getReserveField1()).getString("raiseLimit"):"";
                    raiseLimitTime = StringUtils.isNotEmpty(JSON.parseObject(transferFilterData.getReserveField1()).getString("raiseLimitTime"))?JSON.parseObject(transferFilterData.getReserveField1()).getString("raiseLimitTime"):"";
                    raiseLimitResult = StringUtils.isNotEmpty(JSON.parseObject(transferFilterData.getReserveField1()).getString("raiseLimitResult"))?JSON.parseObject(transferFilterData.getReserveField1()).getString("raiseLimitResult"):"";
                    loantResult = StringUtils.isNotEmpty(JSON.parseObject(transferFilterData.getReserveField1()).getString("loantResult"))?JSON.parseObject(transferFilterData.getReserveField1()).getString("loantResult"):"";
                    raiseLimiType = StringUtils.isNotEmpty(JSON.parseObject(transferFilterData.getReserveField1()).getString("raiseLimiType"))?JSON.parseObject(transferFilterData.getReserveField1()).getString("raiseLimiType"):"";
                    raiseLimiSuccess = StringUtils.isNotEmpty(JSON.parseObject(transferFilterData.getReserveField1()).getString("raiseLimiSuccess"))?JSON.parseObject(transferFilterData.getReserveField1()).getString("raiseLimiSuccess"):"";
                    rate = StringUtils.isNotEmpty(JSON.parseObject(transferFilterData.getReserveField1()).getString("rate"))?JSON.parseObject(transferFilterData.getReserveField1()).getString("rate"):"";
                }
                StringBuilder sb = new StringBuilder();
                sb.append((StringUtils.isNotEmpty(transferFilterData.getCustNum())?transferFilterData.getCustNum():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getIfRegister())?transferFilterData.getIfRegister():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getRegisterTime())?transferFilterData.getRegisterTime():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getIfApply())?transferFilterData.getIfApply():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getApplyDt())?transferFilterData.getApplyDt():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getApplyResult())?transferFilterData.getApplyResult():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getAuditAmount())?transferFilterData.getAuditAmount():"").concat(","));
                sb.append(applyLoan.concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getIfLent())?transferFilterData.getIfLent():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getLentTime())?transferFilterData.getLentTime():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getLentAmount())?transferFilterData.getLentAmount():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getUserType())?transferFilterData.getUserType():"").concat(","));
                sb.append(raiseLimit.concat(","));
                sb.append(raiseLimitTime.concat(","));
                sb.append(raiseLimitResult.concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getInsertTime())?transferFilterData.getInsertTime():"").concat(","));
                sb.append((StringUtils.isNotEmpty(transferFilterData.getType())?transferFilterData.getType():"").concat(","));
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
        updatetask.setFileType(2);
        updatetask.setUpdateTime(new Date());
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
                    if (!"4".equals(marketingTransferSyncUser.getType()) && !"15".equals(marketingTransferSyncUser.getType())) {
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
                continue;
            }
            //剔除caseEffecctive=0的案件编号
            List<MarketingTransferSyncUser> resultData = eliminateCaseEffective(tcId,dataFilter,apiCode);
            if (resultData.size() <= 0) {
                continue;
            }
            Set<String> set = resultData.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(apiCode, set);
            Map<String, MarketingSyncUser> preUserMap = preUserByTask.stream().collect(
                    Collectors.groupingBy(MarketingSyncUser::getCustNum
                            , Collectors.collectingAndThen(
                                    Collectors.reducing((v1, v2) ->
                                            v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2)
                                    , Optional::get)));

            for (MarketingTransferSyncUser transferFilterData : resultData) {
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
            totalSize = totalSize + resultData.size();
            dataFilter.clear();
            resultData.clear();
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
        updatetask.setFileType(3);
        updatetask.setUpdateTime(new Date());
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
                    if (!"7".equals(marketingTransferSyncUser.getType()) && !"8".equals(marketingTransferSyncUser.getType()) && !"15".equals(marketingTransferSyncUser.getType())) {
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
            //过滤 该type符合转电销type且60天没有变化的数据（推电销type无变化且推电销次数<=2）
            List<MarketingTransferSyncUser> resultFilter = new ArrayList<>();
            String _60beforeDay = new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.addDays(new Date(), -60));
            String _60endDay = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            PhoneSaleRecordInfoDTO recordInfoDTO = new PhoneSaleRecordInfoDTO();
            recordInfoDTO.setCustNums(set);
            recordInfoDTO.setApiCode(apiCode);
            recordInfoDTO.setStartDate(_60beforeDay);
            recordInfoDTO.setEndDate(_60endDay);
            recordInfoDTO.setTransferType("0");
            List<PhoneSaleInfoVO> _60records = phoneSaleExtendInfoMapper.getDxRecordByTransferType(recordInfoDTO);
            Map<String, List<PhoneSaleInfoVO>> _60filterCustNumsMap = _60records.stream().collect(Collectors.groupingBy(PhoneSaleInfoVO::getCustNum));
            for(MarketingTransferSyncUser transferFilterData : dataFilter){
                List<PhoneSaleInfoVO> phoneSaleInfoVOS = _60filterCustNumsMap.get(transferFilterData.getCustNum());
                if(phoneSaleInfoVOS != null && phoneSaleInfoVOS.size()>2){
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
                        }else {
                            resultFilter.add(transferFilterData);
                        }
                    }
                }
            }

            if (resultFilter.size() <= 0) {
               continue;
            }

            //剔除caseEffecctive=0的案件编号
            List<MarketingTransferSyncUser> resultData = eliminateCaseEffective(tcId,resultFilter,apiCode);
            if (resultData.size() <= 0) {
                continue;
            }

            for (MarketingTransferSyncUser transferFilterData : resultData) {
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
            totalSize = totalSize + resultData.size();
            resultFilter.clear();
            resultData.clear();
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
        updatetask.setFileType(4);
        updatetask.setUpdateTime(new Date());
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("宜信非实时数据提取(hist)-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
    }

    /**
     * 剔除caseEffecctive=0的案件编号
     * @param tcId
     * @param dataFilter
     * @param apiCode
     * @return
     */
    private List<MarketingTransferSyncUser> eliminateCaseEffective(String tcId,List<MarketingTransferSyncUser> dataFilter, String apiCode) {
        Set<String> set = dataFilter.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
        List<MarketingTransferSyncUser> resultFilter = marketingTransferSyncUserMapper.getByInCustAndCaseEffective(tcId,apiCode, set);
        Set<String> custNumFilter = resultFilter.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
        //Map<String, MarketingTransferSyncUser> filterMap = resultFilter.stream().collect(Collectors.toMap(MarketingTransferSyncUser::getCustNum, MarketingTransferSyncUser -> MarketingTransferSyncUser));
        //dataFilter.removeIf(data -> filterMap.containsKey(data.getCustNum()));
        dataFilter.removeIf(data -> custNumFilter.contains(data.getCustNum()));
        return dataFilter;
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
        updatetask.setFileType(1);
        updatetask.setUpdateTime(new Date());
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
        Integer limitStart = pageIndex * 2000;
        List<MarketingTransferSyncUser> transferOrderInsertTime = marketingTransferSyncUserMapper.getTransferOrderInsertTime(tcId, date, limitStart);
        if (transferOrderInsertTime.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(transferOrderInsertTime);
    }
}
