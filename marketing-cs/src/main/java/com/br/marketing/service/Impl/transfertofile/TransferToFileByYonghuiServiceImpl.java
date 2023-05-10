package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.entity.TransferFileTaskExample;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.RuleRedisServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 永辉转化数据提取
 *
 * @author xxx
 */
@Slf4j
@Service
public class TransferToFileByYonghuiServiceImpl implements ITransferToFileService {

    private static final String EXECUTE_TIME = "11:00:00";
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private SyncConfigService syncConfigService;
    @Resource
    private TransferFileTaskMapper transferFileTaskMapper;
    @Resource
    private RuleRedisServiceImpl ruleRedisService;
    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    private final static String TABLE_HEAD_TRANSFER = "custNum,userType,registerTime,ifLogin,loginTime,ifApply,applyDt,applyResult,auditTime,auditAmount,applyLoan,applyLoanTime,applyLoanAmount,ifLent,lentTime,lentAmount";

    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        if (jobParameter.contains(apiCode)) {
            String[] split = jobParameter.split(";");
            for (String s : split) {
                if (s.contains(apiCode)) {
                    return s.split("#")[1];
                }
            }
        }
        return "";
    }

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode, String myParam) {
        List<TransferFileTask> resultList = new ArrayList<>();
        //执行时间可配置
        String extractTime = StringUtils.isBlank(marketingCommonConfig.getYonghuiTransferExtractTime())
                ? EXECUTE_TIME : marketingCommonConfig.getYonghuiTransferExtractTime();
        LocalTime localTime = LocalTime.parse(extractTime);
        if (LocalTime.now().isAfter(localTime)) {
            String dateyyyymmddStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(dateyyyymmddStr)
                    .andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("永辉转化数据提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId, dateyyyymmddStr);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName(String.format("%s_zhuanhua_%s.txt", apiCode, dateyyyymmddStr));
                transferFileTask.setTaskNumber(0);
                transferFileTask.setStartDate(dateyyyymmddStr);
                transferFileTask.setContextId(transferFileContextId);
                transferFileTask.setCreateTime(new Date());
                transferFileTask.setUpdateTime(new Date());
                transferFileTask.setFileChildDir("yonghuizhuanhua");
                transferFileTaskMapper.insertSelective(transferFileTask);
                resultList.add(transferFileTask);
            }
        }
        Result<List<TransferFileTask>> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());
        result.setDate(resultList);
        return result;
    }

    @Override
    public Result<String> actionTransferToFile(TransferFileTask transferFileTask, String jobParameter) {
        String requestDate = org.apache.commons.lang3.StringUtils.isBlank(jobParameter)
                ? LocalDate.now().toString() : jobParameter;
        Result<String> result = new Result<>();
        String apiCode = transferFileTask.getApiCode();
        String descPath = syncConfigService.getPath().concat("transferToFile/")
                .concat(apiCode).concat("/").concat(transferFileTask.getStartDate()).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            boolean mkdirs = writeDic.mkdirs();
            if (!mkdirs) {
                log.error(descPath + "目录创建失败！");
            }
        }
        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            fw.append(TABLE_HEAD_TRANSFER);
            fw.append("\r\n");
            writeTransferToFile(fw, apiCode, transferFileTask, requestDate);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage(ex.getMessage());
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    private void writeTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask
            , String requestDate) throws IOException {
        long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        int page = 0;
        int totalSize = 0;
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.createCriteria().andApiCodeEqualTo(apiCode)
                .andRequestDataEqualTo(requestDate);
        example.settCid(tcId);
        for (; ; ) {
            example.setOrderByClause("id limit " + (page * 2000) + ",2000");
            List<MarketingTransferSyncUser> transferOrderInsertTime = marketingTransferSyncUserMapper.selectByExample(example);
            if (CollectionUtils.isEmpty(transferOrderInsertTime)) {
                break;
            }
            page++;
            for (MarketingTransferSyncUser transferFilterData : transferOrderInsertTime) {
                StringBuilder sb = new StringBuilder();
                String reserveField1 = transferFilterData.getReserveField1();
                String applyLoan = null;
                String applyLoanTime = null;
                String applyLoanAmount = null;
                if (org.apache.commons.lang3.StringUtils.isNotBlank(reserveField1)) {
                    JSONObject jsonObject = JSON.parseObject(reserveField1);
                    applyLoan = jsonObject.getString("applyLoan");
                    applyLoanTime = jsonObject.getString("applyLoanTime");
                    applyLoanAmount = jsonObject.getString("applyLoanAmount");
                }
                sb.append(emptyDefault(transferFilterData.getCustNum())).append(",");
                sb.append(emptyDefault(transferFilterData.getUserType())).append(",");
                sb.append(removeMillisecond(emptyDefault(transferFilterData.getRegisterTime()))).append(",");
                sb.append(removeMillisecond(emptyDefault(transferFilterData.getIfLogin()))).append(",");
                sb.append(removeMillisecond(emptyDefault(transferFilterData.getLoginTime()))).append(",");
                sb.append(emptyDefault(transferFilterData.getIfApply())).append(",");
                sb.append(removeMillisecond(emptyDefault(transferFilterData.getApplyDt()))).append(",");
                sb.append(emptyDefault(transferFilterData.getApplyResult())).append(",");
                sb.append(removeMillisecond(emptyDefault(transferFilterData.getAuditTime()))).append(",");
                sb.append(emptyDefault(transferFilterData.getAuditAmount())).append(",");
                sb.append(emptyDefault(applyLoan)).append(",");
                sb.append(removeMillisecond(emptyDefault(applyLoanTime))).append(",");
                sb.append(emptyDefault(applyLoanAmount)).append(",");
                sb.append(emptyDefault(transferFilterData.getIfLent())).append(",");
                sb.append(removeMillisecond(emptyDefault(transferFilterData.getLentTime()))).append(",");
                sb.append(emptyDefault(transferFilterData.getLentAmount()));
                sb.append("\r\n");
                fw.append(sb.toString());
            }
            totalSize = totalSize + transferOrderInsertTime.size();
        }
        saveUpdateTask(transferFileTask, totalSize);
        log.warn("永辉转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
    }

    private void saveUpdateTask(TransferFileTask transferFileTask, int totalSize) {
        TransferFileTask task = new TransferFileTask();
        task.setId(transferFileTask.getId());
        task.setFileName(transferFileTask.getFileName());
        task.setFilePath(transferFileTask.getFilePath());
        task.setStatus(2);
        task.setTaskNumber(totalSize);
        task.setUpdateTime(new Date());
        transferFileTaskMapper.updateByPrimaryKeySelective(task);
    }

    private String createBatchNumber(String apiCode, Long contextId, String dateStr) {
        return apiCode.concat("_").concat(dateStr).concat("_").concat(contextId.toString());
    }

    /**
     * 2023-05-10 11:20
     * 值为null时，赋值''
     */
    private String emptyDefault(String value) {
        return StringUtils.isNotEmpty(value) ? value : "";
    }

    private String removeMillisecond(String timeStr) {
        return timeStr.replace(":000", "");
    }
}
