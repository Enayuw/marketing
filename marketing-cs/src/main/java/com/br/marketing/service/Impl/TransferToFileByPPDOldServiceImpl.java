package com.br.marketing.service.Impl;

import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.IPeriodOfValidityService;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.PeriodOfValidityHelper;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @Author songjuanjnuan
 * @Date 2023/02/09 16:28
 * @Description:D20230206拍拍贷老客转化数据提取-3710015
 * http://c.100credit.cn/pages/viewpage.action?pageId=98025339
 */
@Slf4j
@Service
public class TransferToFileByPPDOldServiceImpl implements ITransferToFileService {

    @Autowired
    SyncConfigService syncConfigService;
    @Autowired
    private TransferFileTaskMapper transferFileTaskMapper;
    @Autowired
    private RuleRedisServiceImpl ruleRedisService;
    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;
    @Autowired
    private IPeriodOfValidityService periodOfValidityService;

    final static String EXECUTE_TIME = " 11:00:00";

    final static String VALIDITY_DATSTR = "T+33";

    final static String PPD_TRANSFER_FILE = "push_";

    final static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
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
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("拍拍贷老客转人工数据提取-开始执行,apiCode ={}", apiCode);
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
        log.warn("拍拍贷老客转人工数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();//yyyyMMdd
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(recordDate).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        StringBuilder fileName = new StringBuilder();
        fileName.append(PPD_TRANSFER_FILE).append(recordDate).append(".txt");
        String fileAllPath = descPath.concat(fileName.toString());
        transferFileTask.setFileName(fileName.toString());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append("custNum,cell,push_dx_time,status");
            fw.append("\r\n");
            writePPDTransferToFile(fw, apiCode, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void writePPDTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask) throws IOException, IllegalAccessException {
        Long start = System.currentTimeMillis();
        String ppdOldValidityDayStr = StringUtils.isNotEmpty(marketingCommonConfig.getPpdOldValidityDayStr()) ? marketingCommonConfig.getPpdOldValidityDayStr() : VALIDITY_DATSTR;
        Integer day = PeriodOfValidityHelper.getPeriodOfValidityDay(ppdOldValidityDayStr);
        PeriodOfValidityBO builder = periodOfValidityService.getPeriodOfValidityRange(-day, new Date()).builder();
        Date beginDate = builder.getBeginDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(beginDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND,0);
        beginDate = calendar.getTime();
        Date enDate = builder.getEnDate();
        int pageSize = 2000;
        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        int totalSize =0;
        while (mark) {
            PhoneSaleExtendInfoExample phoneSaleExtendInfoExample = new PhoneSaleExtendInfoExample();
            phoneSaleExtendInfoExample.createCriteria().andApiCodeEqualTo(apiCode)
                    .andPushDxTimeGreaterThanOrEqualTo(beginDate)
                    .andPushDxTimeLessThanOrEqualTo(enDate);
            phoneSaleExtendInfoExample.setOrderByClause(" create_time desc,id desc limit ".concat(String.format("%s,%s", page * pageSize, pageSize)));
            List<PhoneSaleExtendInfo> phoneSaleExtendInfos = phoneSaleExtendInfoMapper.selectByExample(phoneSaleExtendInfoExample);
            if (CollectionUtils.isEmpty(phoneSaleExtendInfos)) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            //到上传表取最新cell
            List<String> list = phoneSaleExtendInfos.stream().map(PhoneSaleExtendInfo::getCustNum).collect(Collectors.toList());
            List<List<String>> partition = ListUtils.partition(list, 500);
            Map<String, MarketingSyncUser> preUserMap = new HashMap<>();
            for (List<String> strings : partition) {
                Set<String> set = strings.stream().collect(Collectors.toSet());
                List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(apiCode, set);
                Map<String, MarketingSyncUser> map = preUserByTask.stream().collect(
                        Collectors.groupingBy(MarketingSyncUser::getCustNum
                                , Collectors.collectingAndThen(
                                        Collectors.reducing((v1, v2) ->
                                                v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2)
                                        , Optional::get)));
                preUserMap.putAll(map);
            }
            //判断是否再有效期内
            for (PhoneSaleExtendInfo data : phoneSaleExtendInfos){
                String custNum = data.getCustNum();
                if (preUserMap.containsKey(custNum)){
                    Date validityDate = preUserMap.get(custNum).getAppletTime() != null ? preUserMap.get(custNum).getAppletTime() : preUserMap.get(custNum).getCreateTime();
                    boolean notExpire = periodOfValidityService.isNotExpire(new Date(), ppdOldValidityDayStr, validityDate);
                    if(notExpire){
                        //custNum,cell,push_dx_time,status
                        String cell = "";
                        if (preUserMap.containsKey(custNum)) {
                            String decode = BrCipherMaker.getInstance().decode(preUserMap.get(custNum).getCell());
                            cell = StringUtils.isBlank(decode) ? preUserMap.get(custNum).getCell() : DigestUtils.md5DigestAsHex(decode.getBytes());
                        }
                        String status = data.getStatus();
                        String push_dx_time =  sdf2.format(data.getPushDxTime());
                        StringBuilder sb = new StringBuilder();
                        sb.append((StringUtils.isNotEmpty(custNum) ? custNum : "").concat(","));
                        sb.append(cell.concat(","));
                        sb.append(push_dx_time.concat(","));
                        sb.append(status);
                        sb.append("\r\n");
                        fw.append(sb.toString());
                        totalSize = totalSize + 1;
                    }
                }
            }
            phoneSaleExtendInfos.clear();
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

    String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String concat = apiCode.concat("_").concat(yyyyMMdd).concat("_").concat(contextId.toString());
        return concat;
    }

}
