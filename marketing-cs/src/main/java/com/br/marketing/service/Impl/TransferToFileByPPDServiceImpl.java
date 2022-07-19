package com.br.marketing.service.Impl;

import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.stream.Collectors;


/**
 * @Author songjuanjnuan
 * @Date 2022/07/12 11:28
 * @Description:D20220706拍拍贷新客实时数据逻辑处理-3710014
 * http://c.100credit.cn/pages/viewpage.action?pageId=76646919
 */
@Slf4j
@Service
public class TransferToFileByPPDServiceImpl implements ITransferToFileService {

    @Value("${otherConfig.warning.path:00}")
    private String path;
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

    final static String EXECUTE_TIME = " 10:00:00";

    final static String PPD_TRANSFER_FILE = "rengong_";

    final static DateTimeFormatter YYYYMMDDSHORTDFLINE = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_FORMAT);
    final static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
    @Override
    public String isMyParam(String apiCode,String jobParameter) {
        if(StringUtils.isNotEmpty(jobParameter)){
            String[] split = jobParameter.split(";");
            for(String s : split){
                String paramApiCode = s.split("#")[0];
                if(apiCode.equals(paramApiCode)  && marketingCommonConfig.getPPDTransferFileApiCodes().contains(paramApiCode)){
                    return s.split("#")[1];
                }
            }
        }
        return "";
    }

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode) {
        List<TransferFileTask> resultList = new ArrayList<>();
        Date now = new Date();
        //可配置
        String execute = EXECUTE_TIME;
        if (StringUtils.isNotEmpty(marketingCommonConfig.getPPDTransferFileExecuteTime())) {
            execute = " " + marketingCommonConfig.getPPDTransferFileExecuteTime();
        }
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, execute);
        if (now.after(executeTime)) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("拍拍贷新客转人工数据提取-开始执行,apiCode ={}", apiCode);
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
        log.warn("拍拍贷新客转人工数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();//yyyyMMdd
        String descPath = path.concat("transferToFile/").concat(apiCode).concat("/").concat(recordDate).concat("/");
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
            fw.append("custNum,userType,cell,status,push_dx_time");
            fw.append("\r\n");
            //判断是否是首次提取
            LocalDate startDate;
            LocalDate endDate;
            if(StringUtils.isNotEmpty(jobParameter) && "true".equals(jobParameter.split(",")[0])){
                startDate = LocalDate.parse("2022-06-27", YYYYMMDDSHORTDFLINE);
                endDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }else {
                startDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().minusDays(1);
                endDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
            writePPDTransferToFile(fw, apiCode, startDate, endDate,transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void writePPDTransferToFile(Writer fw, String apiCode, LocalDate startDate, LocalDate endDate,TransferFileTask transferFileTask) throws IOException {
        Long start = System.currentTimeMillis();
        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        int totalSize =0;
        while (mark) {
            List<PhoneSaleExtendInfo> infoData = phoneSaleExtendInfoMapper.getPPDToDxData(apiCode,startDate.toString(),endDate.toString(), page * 2000);
            if (CollectionUtils.isEmpty(infoData)){
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            if (infoData.size() <= 0) {
                log.warn("拍拍贷新客转人工数据提取-该批次无符合要求的数据,apiCode = {}", apiCode);
                continue;
            }
            List<String> list = infoData.stream().map(PhoneSaleExtendInfo::getCustNum).collect(Collectors.toList());
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

            for (PhoneSaleExtendInfo data : infoData) {
                //custNum,userType,cell,status,push_dx_time
                String custNum = data.getCustNum();
                String userType = data.getUserType();
                String cell = "";
                if (preUserMap.containsKey(custNum)) {
                    String decode = BrCipherMaker.getInstance().decode(preUserMap.get(custNum).getCell());
                    cell = StringUtils.isBlank(decode) ? preUserMap.get(custNum).getCell() : DigestUtils.md5DigestAsHex(decode.getBytes());
                }
                String status = data.getStatus();
                String push_dx_time = data.getPushDxTime() == null ? "" : sdf2.format(data.getPushDxTime());;
                StringBuilder sb = new StringBuilder();
                sb.append((StringUtils.isNotEmpty(custNum)?custNum:"").concat(","));
                sb.append((StringUtils.isNotEmpty(userType)?userType:"").concat(","));
                sb.append(cell.concat(","));
                sb.append((StringUtils.isNotEmpty(status)?status:"").concat(","));
                sb.append(push_dx_time);
                sb.append("\r\n");
                fw.append(sb.toString());
            }
            totalSize = totalSize + infoData.size();
            infoData.clear();
        }
        TransferFileTask updatetask = new TransferFileTask();
        updatetask.setId(transferFileTask.getId());
        updatetask.setStatus(2);
        updatetask.setFileName(transferFileTask.getFileName());
        updatetask.setFilePath(transferFileTask.getFilePath());
        updatetask.setTaskNumber(totalSize);
        updatetask.setUpdateTime(new Date());
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("拍拍贷新客转人工数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
    }

    String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String concat = apiCode.concat("_").concat(yyyyMMdd).concat("_").concat(contextId.toString());
        return concat;
    }

}
