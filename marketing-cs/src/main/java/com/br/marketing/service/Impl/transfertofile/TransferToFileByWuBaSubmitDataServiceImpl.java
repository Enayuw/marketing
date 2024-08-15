package com.br.marketing.service.Impl.transfertofile;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.entity.TransferFileTaskExample;
import com.br.marketing.entity.WubaSubmitConversionData;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.mapper.WubaSubmitConversionDataMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.RuleRedisServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 58新客-营销名单上报-数据提取
 */
@Slf4j
@Service
public class TransferToFileByWuBaSubmitDataServiceImpl implements ITransferToFileService {

    private final static String TITLE = "【58新客-营销名单上报-数据提取】";

    private static final String EXECUTE_TIME_DEFAULT = "10:00:00";
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TransferFileTaskMapper transferFileTaskMapper;

    @Resource
    private RuleRedisServiceImpl ruleRedisService;

    @Resource
    private SyncConfigService syncConfigService;

    @Resource
    private WubaSubmitConversionDataMapper wubaSubmitConversionDataMapper;

    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        String startDate = marketingCommonConfig.getWuBaSubmitDataToFileStartDate();
        if(!StringUtils.isEmpty(startDate)){
            return startDate;
        }
        return "";
    }

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode, String myParam) {
        String executeTime = marketingCommonConfig.getWuBaSubmitDataToFileExecuteTime();
        if(StringUtils.isBlank(executeTime)){
            executeTime = EXECUTE_TIME_DEFAULT;
        }
        Date now = new Date();
        Date executeDateTime = DateHelper.getDatePlusHourMinuteSecond(now, " " + executeTime);
        if(now.before(executeDateTime)){
            return new Result<>().failure();
        }

        String yyyyMMdd = "";
        if(!StringUtils.isEmpty(myParam)){
            yyyyMMdd = myParam;
        }else{
            yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
        }

        List<TransferFileTask> taskList = new ArrayList<>();

        TransferFileTaskExample taskExample = new TransferFileTaskExample();
        taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(1);
        List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
        String fileName = String.format("wuba_submit_%s_%s.txt", apiCode, yyyyMMdd);

        if (CollectionUtils.isEmpty(transferFileTasks)) {
            log.warn(TITLE + "开始执行, apiCode：{}, startDate: {}", apiCode, yyyyMMdd);
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
            taskList.add(transferFileTask);
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(taskList);
    }

    @Override
    public Result actionTransferToFile(TransferFileTask transferFileTask, String jobParameter) {
        String apiCode = transferFileTask.getApiCode();
        log.warn(TITLE + "开始写入文件, apiCode ={}", apiCode);
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/")
                .concat(transferFileTask.getStartDate()).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));) {
            fw.append("cell,marketingTime");
            fw.append("\r\n");
            writeDataToFile(fw, apiCode, transferFileTask);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(e.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    public void writeDataToFile(Writer fw, String apiCode, TransferFileTask transferFileTask) throws IOException {
        Long start = System.currentTimeMillis();
        String startDateStr = transferFileTask.getStartDate();
        int pageSize = 2000;
        int totalSize = 0;

        Long indexId = null;
        while (true) {
            LocalDate startLocalDate = LocalDate.parse(startDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate pushTimeStartLocalDate = startLocalDate.plusDays(-1);
            LocalDate pushTimeEndLocalDate = startLocalDate;
            String pushTimeStart = pushTimeStartLocalDate.toString();
            String pushTimeEnd = pushTimeEndLocalDate.toString();

            List<WubaSubmitConversionData> submitDataList = wubaSubmitConversionDataMapper.findSubmitDataByPushTime(
                    apiCode, pushTimeStart, pushTimeEnd, indexId, pageSize);
            if (CollectionUtils.isEmpty(submitDataList)) {
                break;
            }
            indexId = submitDataList.get(submitDataList.size() - 1).getId();

            for (WubaSubmitConversionData data : submitDataList) {
                StringBuilder buffer = new StringBuilder();
                buffer.append(data.getCell().concat(","));
                buffer.append(data.getMarketingTime());
                buffer.append("\r\n");
                fw.append(buffer.toString());
                totalSize++;
            }
        }

        TransferFileTask task = new TransferFileTask();
        task.setId(transferFileTask.getId());
        task.setStatus(2);
        task.setFileName(transferFileTask.getFileName());
        task.setFilePath(transferFileTask.getFilePath());
        task.setTaskNumber(totalSize);
        task.setUpdateTime(new Date());
        transferFileTaskMapper.updateByPrimaryKeySelective(task);
        Long end = System.currentTimeMillis();
        log.warn(TITLE + "本地文件生成成功, apiCode: {}, startDate: {}, time: {}ms, total: {}", apiCode, startDateStr, end - start, totalSize);
    }

    String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String concat = apiCode.concat("_").concat(yyyyMMdd).concat("_").concat(contextId.toString());
        return concat;
    }
}
