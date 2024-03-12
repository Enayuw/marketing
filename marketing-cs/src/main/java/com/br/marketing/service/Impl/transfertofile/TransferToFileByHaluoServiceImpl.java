package com.br.marketing.service.Impl.transfertofile;

import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.PhoneSaleExtendHaluo;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.entity.TransferFileTaskExample;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.PhoneSaleExtendHaluoMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.RuleRedisServiceImpl;
import com.br.marketing.service.SyncConfigService;
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
 * @Author zhen.li
 * @Date 2022/03/07 11:31
 * @Description:哈罗的实现
 */
@Slf4j
@Service
public class TransferToFileByHaluoServiceImpl implements ITransferToFileService {

    @Autowired
    TransferFileTaskMapper transferFileTaskMapper;

    @Autowired
    MarketingCustomerMapper customerMapper;

    @Autowired
    MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Autowired
    RuleRedisServiceImpl ruleRedisService;

    @Autowired
    PhoneSaleExtendHaluoMapper phoneSaleExtendHaluoMapper;

    @Autowired
    SyncConfigService syncConfigService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    final DateTimeFormatter YYYYMMDDSHORTDF = DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT);

    final DateTimeFormatter YYYYMMDDLINEDF = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_FORMAT);

    final static String HALUODXFILE = "xianshangrengong_";

    final static String EXECUTE_TIME = " 01:00:00";

    final static String FIRST_TIME = "2022-02-17 00:00:00";

    final static String ONLINE_TIME = "2022-03-15 23:59:59";

    final static String CELL = "cell";

    final static String GROUP_TYPE = "group_type";


    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        if(StringUtils.isNotEmpty(jobParameter)){
            String[] split = jobParameter.split(";");
            for(String s : split){
                String paramApiCode = s.split("#")[0];
                if(apiCode.equals(paramApiCode)  && marketingCommonConfig.getHaLuoTransferFileApiCodes().contains(paramApiCode)){
                    return s.split("#")[1];
                }
            }
        }
        return "";
    }

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode,String myParam) {
        List<TransferFileTask> resultList = new ArrayList<>();
        Date now = new Date();
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, EXECUTE_TIME);
        //每天01:00:00之后执行
        if (now.after(executeTime)) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("哈罗转人工数据提取-开始执行,apiCode ={}",apiCode);
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

    @Override
    public Result actionTransferToFile(TransferFileTask transferFileTask,String jobParameter) {
        String apiCode = transferFileTask.getApiCode();
        Date now = new Date();
        String recordDate = transferFileTask.getStartDate();
        String startDate = LocalDate.parse(recordDate, YYYYMMDDSHORTDF).minusDays(1L).format(YYYYMMDDLINEDF);
        if (StringUtils.isNotEmpty(jobParameter)) {
            startDate = jobParameter;
        }
        String endDate = LocalDate.parse(recordDate, YYYYMMDDSHORTDF).format(YYYYMMDDLINEDF);
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(recordDate).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        StringBuilder fileName = new StringBuilder();
        fileName.append(HALUODXFILE).append(recordDate).append(".txt");
        String fileAllPath = descPath.concat(fileName.toString());
        transferFileTask.setFileName(fileName.toString());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append("cust_num,cell,groupType,taskId,createtime,status,insertime");
            fw.append("\r\n");
            writeHaluoDx(fw, apiCode, startDate, endDate, transferFileTask);
        } catch (Exception ex) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void writeHaluoDx(Writer fw, String apiCode, String startDate, String endDate, TransferFileTask transferFileTask) throws IOException {
        Long start=System.currentTimeMillis();
        Long minId = null;
        int phoneSaleNum = 0;
        Boolean isContiue = Boolean.TRUE;
        while (isContiue) {
            List<PhoneSaleExtendHaluo> phoneSaleExtendHaluos = phoneSaleExtendHaluoMapper.selectHaluoPhoneSaleExtend(minId, startDate, endDate);
            phoneSaleNum += phoneSaleExtendHaluos.size();
            if (phoneSaleExtendHaluos.size() <= 0) {
                isContiue = Boolean.FALSE;
                continue;
            }
            minId = phoneSaleExtendHaluos.get(phoneSaleExtendHaluos.size() - 1).getId() + 1;
            List<String> taskIds = phoneSaleExtendHaluos.stream().map(t -> t.getTaskId()).collect(Collectors.toList());
            List<String> custNums = phoneSaleExtendHaluos.stream().map(t -> t.getCustNum()).collect(Collectors.toList());
            List<MarketingSyncUser> users = marketingSyncInfoMapper.getSyncUserByTaskAndCust(apiCode, taskIds, custNums);
            HashMap<String, Map<String, String>> marketingSyncMap = new HashMap<>();
            users.forEach(t -> {
                String key = t.getCusBatch().concat("_").concat(t.getCustNum());
                Map<String, String> map = new HashMap();
                if (!marketingSyncMap.containsKey(key)) {
                    String decode = BrCipherMaker.getInstance().decode(t.getCell());
                    String cell = StringUtils.isBlank(decode) ? t.getCell() : DigestUtils.md5DigestAsHex(decode.getBytes());
                    map.put(CELL, cell);
                    map.put(GROUP_TYPE, t.getUserType());
                    marketingSyncMap.put(key, map);
                }
            });

            for (PhoneSaleExtendHaluo phoneSaleExtendHaluo : phoneSaleExtendHaluos) {
                String marketingSyncKey = phoneSaleExtendHaluo.getTaskId().concat("_").concat(phoneSaleExtendHaluo.getCustNum());
                String cell = "";
                String groupType = "";
                String taskId = phoneSaleExtendHaluo.getTaskId();
                if (!CollectionUtils.isEmpty(marketingSyncMap.get(marketingSyncKey))) {
                    cell = marketingSyncMap.get(marketingSyncKey).get(CELL);
                    groupType = marketingSyncMap.get(marketingSyncKey).get(GROUP_TYPE);
                }
                StringBuilder sb = new StringBuilder();
                sb.append(phoneSaleExtendHaluo.getCustNum().concat(","));
                sb.append(cell.concat(","));
                sb.append(groupType.concat(","));
                if (taskId.endsWith("upload")) {
                    String taskIdBySyncInfo = marketingSyncInfoMapper.getTaskIdByCustNumNotLikeUpload(apiCode, phoneSaleExtendHaluo.getCustNum());
                    sb.append((StringUtils.isNotEmpty(taskIdBySyncInfo) ? taskIdBySyncInfo : "").concat(","));
                } else {
                    sb.append(taskId.concat(","));
                }
                sb.append(DateUtils.format(phoneSaleExtendHaluo.getCreateTime(), DateHelper.LINE_DATE_COLON_TIME_FORMAT).concat(","));
                sb.append(phoneSaleExtendHaluo.getStatus().concat(","));
                sb.append(phoneSaleExtendHaluo.getAppletTime());
                sb.append("\r\n");
                fw.append(sb.toString());
            }
        }
        TransferFileTask updatetask = new TransferFileTask();
        updatetask.setId(transferFileTask.getId());
        updatetask.setStatus(2);
        updatetask.setFileName(transferFileTask.getFileName());
        updatetask.setFilePath(transferFileTask.getFilePath());
        updatetask.setTaskNumber(phoneSaleNum);
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("哈罗转人工数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, phoneSaleNum);
    }

}


