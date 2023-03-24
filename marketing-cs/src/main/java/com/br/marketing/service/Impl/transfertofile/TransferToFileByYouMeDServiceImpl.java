package com.br.marketing.service.Impl.transfertofile;

import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.IPeriodOfValidityService;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.RuleRedisServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.PeriodOfValidityHelper;
import com.br.marketing.vo.TransferOfRdRFVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 你我贷转化数据提取
 */
@Slf4j
@Service
public class TransferToFileByYouMeDServiceImpl implements ITransferToFileService {

    @Autowired
    SyncConfigService syncConfigService;
    @Autowired
    private TransferFileTaskMapper transferFileTaskMapper;
    @Autowired
    private RuleRedisServiceImpl ruleRedisService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    MarketingSyncUserMapper syncUserMapper;
    @Autowired
    private IPeriodOfValidityService periodOfValidityService;

    final static String EXECUTE_TIME = " 10:00:00";

    final static String VALIDITY_DATSTR = "[T+33]";

    final static String PPD_TRANSFER_FILE = "push_";

    final static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");

    @Resource
    MarketingTransferSyncUserMapper transferSyncUserMapper;

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
        if (StringUtils.isNotEmpty(marketingCommonConfig.getPPDOldTransferFileExecuteTime())) {
            execute = " " + marketingCommonConfig.getPPDOldTransferFileExecuteTime();
        }
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, execute);
        if (now.after(executeTime)) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            String fileName = String.format("niwodai_transform_%s.txt",yyyyMMdd);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("你我贷转化数据提取-开始执行,apiCode ={}", apiCode);
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
    public Result actionTransferToFile(TransferFileTask transferFileTask,String jobParameter) {
        log.warn("你我贷转化数据落库-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();//yyyyMMdd
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(recordDate).concat("/");
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
            fw.append("custNum,userType,cell,A,loginTime,B,applyDt,C,applyDtApp,D,applyTime,E,F,lentTime,G,requestData");
            fw.append("\r\n");
            writeYMDTransferToFile(fw, apiCode, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void writeYMDTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask) throws IOException, IllegalAccessException {
        Long start = System.currentTimeMillis();
        String ppdOldValidityDayStr = StringUtils.isNotEmpty(marketingCommonConfig.getYouMeDValidityDayStr()) ? marketingCommonConfig.getYouMeDValidityDayStr() : VALIDITY_DATSTR;
        //endDate-上传数据的有效结束时间，beginDate-上传数据的有效开始时间
        LocalDate yDate = LocalDate.now().minusDays(1L);
        Date _uploadEndDate = Date.from(yDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        Integer day = PeriodOfValidityHelper.getPeriodOfValidityDay(ppdOldValidityDayStr);
        PeriodOfValidityBO builder = periodOfValidityService.getPeriodOfValidityRange(-day, _uploadEndDate).builder();
        Date _uploadBeginDate = builder.getBeginDate();
        String _uploadBeginDateStr = new SimpleDateFormat("yyyy-MM-dd").format(_uploadBeginDate);
        String _uploadEndDateStr = new SimpleDateFormat("yyyy-MM-dd").format(_uploadEndDate);

        Date _transferEndDate = Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        PeriodOfValidityBO builder1 = periodOfValidityService.getPeriodOfValidityRange(-day, _transferEndDate).builder();
        Date _transferBeginDate = builder1.getBeginDate();

        int pageSize = 5000;

        Boolean dateMark = Boolean.TRUE;
        int totalSize =0;
        HashSet uploadCustNum = new HashSet();
        Integer datePage = 0;
        while (dateMark){
            Date date = Date.from(yDate.minusDays(datePage).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
            if(date.compareTo(_uploadBeginDate)<0){
                dateMark = false;
                continue;
            }
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(date);
            Integer count = syncUserMapper.countByAppletDate(apiCode, dateStr);
            if(count>0) {
                Boolean mark = Boolean.TRUE;
                Long minId = null;
                while (mark) {
                    List<MarketingSyncUser> syncUserSources = syncUserMapper.getNewSyncUserByDate(apiCode,dateStr,pageSize, minId);
                    if (CollectionUtils.isEmpty(syncUserSources)) {
                        mark = Boolean.FALSE;
                        continue;
                    }
                    minId = syncUserSources.get(syncUserSources.size()-1).getId();
                    List<MarketingSyncUser> syncUsers = syncUserSources.stream().filter(t -> uploadCustNum.add(t.getCustNum())).collect(Collectors.toList());


                }
            }
            datePage++;
        }


        TransferFileTask updatetask = new TransferFileTask();
        updatetask.setId(transferFileTask.getId());
        updatetask.setStatus(2);
        updatetask.setFileName(transferFileTask.getFileName());
        updatetask.setFilePath(transferFileTask.getFilePath());
        updatetask.setTaskNumber(totalSize);
        updatetask.setUpdateTime(new Date());
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("拍拍贷老客转人工数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
    }

    void fieldAction(List<MarketingSyncUser> users,String transferBegin,String apiCode,String tcid,Writer fw){
        Map<String,MarketingSyncUser> userMap = users.stream().collect(Collectors.toMap(MarketingSyncUser::getCustNum
                        ,Function.identity(), BinaryOperator.maxBy(Comparator.comparing(MarketingSyncUser::getAppletTime))));
        List<String> custNums = users.stream().map(t -> t.getCustNum()).collect(Collectors.toList());
        List<TransferOfRdRFVO> transferOfRdRFs = transferSyncUserMapper.getTransferOfRdRFs(transferBegin, custNums, tcid, apiCode);
        Map<String, List<TransferOfRdRFVO>> collect = transferOfRdRFs.stream().sorted(Comparator.comparing(TransferOfRdRFVO::getRequestData)).collect(Collectors.groupingBy(TransferOfRdRFVO::getCustNum));
        for (String custNum : collect.keySet()) {
            List<TransferOfRdRFVO> transferOfRdRFVOS = collect.get(custNum);
            if(transferOfRdRFVOS.size()<=0){
                continue;
            }
            Boolean _Bhave = Boolean.FALSE;
            Boolean _Bhave = Boolean.FALSE;
            for (int i = 0; i < transferOfRdRFVOS.size(); i++) {

            }
        }

    }


    String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String concat = apiCode.concat("_").concat(yyyyMMdd).concat("_").concat(contextId.toString());
        return concat;
    }

}
