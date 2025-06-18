package com.br.marketing.service.Impl.transfertofile;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.entity.TransferFileTaskExample;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.RuleRedisServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @Author 贺东硕
 * @Date 2024/07/13 11：06
 * @Description:奇富360转化数据提取抽象类
 */
@Slf4j
public abstract class AbstractTransferToFileByQiFuService implements ITransferToFileService {

    @Autowired
    SyncConfigService syncConfigService;

    @Autowired
    private TransferFileTaskMapper transferFileTaskMapper;

    @Autowired
    private RuleRedisServiceImpl ruleRedisService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    private final static String TABLE_HEAD_TRANSFER =
            "custNum,applyDt,applyResult,loginTime,requestTime,userType,taskId,expireDate,effectiveDate";

    /**
     * 实现ITransferToFileService的方法，需要被实现类具体实现
     * @author hedongshuo
     * @param apiCode
     * @param jobParameter
     * @return
     */
    @Override
    public abstract String isMyParam(String apiCode, String jobParameter);

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode, String myParam) {
        List<TransferFileTask> resultList = new ArrayList<>();
        String extractTime = getExtractTime(apiCode);
        String suffix = getSuffix(apiCode);
        LocalTime localTime = LocalTime.parse(extractTime);
        boolean isParam = StringUtils.isNotBlank(myParam);
        if (LocalTime.now().isAfter(localTime) || isParam) {
            // 指定日期提取，生成指定日期的记录，不是当天的记录
            String dateyyyymmddStr = isParam ? myParam.replace("-", "")
                    : LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(date)
                    .andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("奇富360转化数据提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId, dateyyyymmddStr);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName(String.format("transform_qifu%s_%s.txt", suffix, dateyyyymmddStr));
                transferFileTask.setTaskNumber(0);
                transferFileTask.setStartDate(date);
                transferFileTask.setContextId(transferFileContextId);
                transferFileTask.setCreateTime(new Date());
                transferFileTask.setUpdateTime(new Date());
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
    public Result actionTransferToFile(TransferFileTask transferFileTask, String jobParameter) {
        log.warn("奇富360转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        Result<String> result = new Result<>();
        String apiCode = transferFileTask.getApiCode();
        String date = LocalDate.now().toString();
        date = date.replace("-", "");
        String requestDate = StringUtils.isBlank(jobParameter) ? LocalDate.now().minusDays(1).toString() : jobParameter;
        String descPath = syncConfigService.getPath()
                .concat("transferToFile/")
                .concat(apiCode)
                .concat("/")
                .concat(date).concat("/");
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
            fw.append(marketingCommonConfig.getQiFuTransferTableHead());
            fw.append("\r\n");
            writeQifuTransferToFile(fw, apiCode,
                    transferFileTask, requestDate, marketingCommonConfig.getQiFuFullExtDataSoleNum());
        } catch (Exception ex) {
            log.error("写入文件错误！",ex);
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage(ex.getMessage());
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    /**
     * 获得提取时间
     * @author hedongshuo
     * @param apiCode
     * @return
     */
    abstract String getExtractTime(String apiCode);

    /**
     * 获得文件路径后缀
     * @author hedongshuo
     * @param apiCode
     * @return
     */
    abstract String getSuffix(String apiCode);

    /**
     * 取数逻辑，数据写入文件
     * @author hedongshuo
     * @param fw
     * @param apiCode
     * @param transferFileTask
     * @param requestDate
     * @param qiFuFullExtDataSoleNum
     */
    abstract void writeQifuTransferToFile(Writer fw, String apiCode,
                                          TransferFileTask transferFileTask, String requestDate, Integer qiFuFullExtDataSoleNum);

    private String createBatchNumber(String apiCode, Long contextId, String dateStr) {
        return apiCode.concat("_").concat(dateStr).concat("_").concat(contextId.toString());
    }

    void saveUpdateTask(TransferFileTask transferFileTask, int totalSize) {
        TransferFileTask task = new TransferFileTask();
        task.setId(transferFileTask.getId());
        task.setFileName(transferFileTask.getFileName());
        task.setFilePath(transferFileTask.getFilePath());
        task.setStatus(2);
        task.setTaskNumber(totalSize);
        task.setUpdateTime(new Date());
        transferFileTaskMapper.updateByPrimaryKeySelective(task);
    }
}
