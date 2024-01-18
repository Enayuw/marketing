package com.br.marketing.service.Impl.transfertofile;

import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.RuleRedisServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
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
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    final static String EXECUTE_TIME = " 11:00:00";

    final static String PPD_TRANSFER_FILE = "push_";

    @Override
    public String isMyParam(String apiCode, String jobParameter) {
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
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/")
                .concat(recordDate).concat("/");
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

    private void writePPDTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask)
            throws IOException {
        long start = System.currentTimeMillis();
        int page = 0;
        int pageSize = 2000;
        int totalSize = 0;
        StringBuilder whereStr = new StringBuilder();
        for (; ; ) {
            List<MarketingDataValidConfig> dataValidityPeriodPageList =
                    transferDataValidityPeriodService.getDataValidityPeriodPageList(apiCode, new Date(), page, pageSize);
            for (MarketingDataValidConfig config : dataValidityPeriodPageList) {
                String startDateStr = LocalDate.parse(config.getValidStartDate()).toString();
                String endDateStr = LocalDate.parse(config.getValidEndDate()).toString();
                whereStr.append("(push_dx_time>='")
                        .append(startDateStr)
                        .append(" 00:00:00")
                        .append("' and push_dx_time<='")
                        .append(endDateStr)
                        .append(" 23:59:59') or");
            }
            if (dataValidityPeriodPageList.size() < pageSize) {
                break;
            }
            page++;
        }
        int length = whereStr.length();
        if (length > 0) {
            whereStr.replace(length - 3, length, "");
        } else {
            whereStr.append("1!=1");
        }
        boolean mark = Boolean.TRUE;
        int dxPage = 1;
        while (mark) {
            PhoneSaleExtendInfoExample phoneSaleExtendInfoExample = new PhoneSaleExtendInfoExample();
            phoneSaleExtendInfoExample.createCriteria().andApiCodeEqualTo(apiCode);
            phoneSaleExtendInfoExample.setOrderByClause(" create_time desc,id desc ");
            List<PhoneSaleExtendInfo> phoneSaleExtendInfos =
                    phoneSaleExtendInfoMapper.findListPageByExampleSqlStr(phoneSaleExtendInfoExample
                            , " and (".concat(whereStr.toString()).concat(")"), dxPage, pageSize);
            if (CollectionUtils.isEmpty(phoneSaleExtendInfos)) {
                mark = Boolean.FALSE;
                continue;
            }
            dxPage++;
            //到上传表取最新cell
            Set<String> custNumSet = phoneSaleExtendInfos.stream().map(PhoneSaleExtendInfo::getCustNum)
                    .collect(Collectors.toSet());
            Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum =
                    transferDataValidityPeriodService.getValidityPeriodsByCustNum(custNumSet, apiCode
                            , LocalDate.now().minusDays(1));
            //判断是否再有效期内
            for (PhoneSaleExtendInfo data : phoneSaleExtendInfos) {
                String custNum = data.getCustNum();
                SyncUserValidityPeriodsBO bo = validityPeriodsByCustNum.get(custNum);
                // bo 为空时不在有效期，直接跳过
                if (bo == null) {
                    continue;
                }
                String cell = bo.getSyncUsers().get(0).getCell();
                String decode = BrCipherMaker.getInstance().decode(cell);
                String status = data.getStatus();
                String pushDxTime = data.getPushDxTime().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate().toString();
                String sb = (StringUtils.isNotEmpty(custNum) ? custNum : "").concat(",") +
                        (StringUtils.isBlank(decode) ? cell : DigestUtils.md5DigestAsHex(decode.getBytes())) + "," +
                        pushDxTime.concat(",") +
                        status +
                        "\r\n";
                fw.append(sb);
                totalSize = totalSize + 1;
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
