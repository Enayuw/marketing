package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
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
 * @Author lixiang
 * @Date 2024-08-16
 */
@Slf4j
@Service
public class TransferToFileByWuBaSubmitDataServiceImpl implements ITransferToFileService {

    private final static String TITLE = "【58新客-营销名单上报-数据提取】";

    private static final String EXECUTE_TIME_DEFAULT = "10:00:00";

    private static final Integer FILE_TYPE = 2;
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

    /**
     * 自定义参数，例：["20240812","20240815","20240817"]
     * @param apiCode
     * @param jobParameter
     * @return
     */
    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        List<String> startDateList = marketingCommonConfig.getWuBaSubmitDataToFileStartDate();
        if(CollectionUtils.isEmpty(startDateList)){
            return "";
        }
        return JSONObject.toJSONString(startDateList);
    }

    /**
     * 生成提取任务
     * @param apiCode
     * @param myParam
     * @return
     */
    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode, String myParam) {
        // 检查执行时间
        if(!checkExecuteTime()){
            return new Result<>().failure();
        }

        // 解析日期参数
        List<TransferFileTask> taskList = new ArrayList<>();
        List<String> startDateList = parseParam(myParam);
        log.warn(TITLE + "生成提取任务, apiCode：{}, param: {}", apiCode, JSONObject.toJSONString(startDateList));

        // 按日期遍历生成提取任务
        for(String startDate : startDateList) {
            log.warn(TITLE + "开始执行, apiCode：{}, startDate: {}", apiCode, startDate);

            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(startDate).andFileTypeEqualTo(FILE_TYPE);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);

            if (!CollectionUtils.isEmpty(transferFileTasks)) {
                continue;
            }

            Long transferFileContextId = ruleRedisService.getTransferFileContextId();
            String batchNumber = createBatchNumber(apiCode, transferFileContextId);
            String fileName = String.format("%s_%s.txt", apiCode, startDate);

            TransferFileTask transferFileTask = new TransferFileTask();
            transferFileTask.setApiCode(apiCode);
            transferFileTask.setFileType(FILE_TYPE);
            transferFileTask.setBatchNumber(batchNumber);
            transferFileTask.setFileName(fileName);
            transferFileTask.setFileChildDir("submit");
            transferFileTask.setTaskNumber(0);
            transferFileTask.setStartDate(startDate);
            transferFileTask.setContextId(transferFileContextId);
            transferFileTask.setCreateTime(new Date());
            transferFileTask.setUpdateTime(new Date());
            transferFileTaskMapper.insertSelective(transferFileTask);
            taskList.add(transferFileTask);
        }
        return new Result().success().setDate(taskList);
    }

    /**
     * 生成文件表头
     * @param transferFileTask
     * @param jobParameter
     * @return
     */
    @Override
    public Result actionTransferToFile(TransferFileTask transferFileTask, String jobParameter) {
        String apiCode = transferFileTask.getApiCode();
        String startDate = transferFileTask.getStartDate();
        Long start = System.currentTimeMillis();
        log.warn(TITLE + "写入文件开始, apiCode: {}, startDate: {}", apiCode, startDate);
        String childDir = StringUtils.isNotEmpty(transferFileTask.getFileChildDir()) ? (transferFileTask.getFileChildDir() + "/") : "";
        String dirPath = syncConfigService.getPath().concat("transferToFile/").concat(childDir).concat(apiCode).concat("/")
                .concat(startDate).concat("/");
        File writeDic = new File(dirPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        transferFileTask.setFilePath(dirPath);

        // 生成文件表头
        String fullFilePath = dirPath.concat(transferFileTask.getFileName());
        File file = new File(fullFilePath);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));) {
            fw.append("cell,marketingTime");
            fw.append("\r\n");
            writeDataToFile(fw, apiCode, transferFileTask);
        } catch (Exception e) {
            log.error(TITLE+"写入文件异常", e);
            return new Result().failure();
        }
        Long end = System.currentTimeMillis();
        log.warn(TITLE + "写入文件结束, apiCode: {}, startDate: {}, time: {}ms", apiCode, startDate, end - start);
        return new Result().success();
    }

    /**
     * 写入文件内容
     * @param fw
     * @param apiCode
     * @param transferFileTask
     * @throws Exception
     */
    public void writeDataToFile(Writer fw, String apiCode, TransferFileTask transferFileTask) throws Exception {
        String startDate = transferFileTask.getStartDate();
        int pageSize = 2000;
        int totalSize = 0;
        Long indexId = null;

        // 分页遍历提取数据, 写入文件
        while (true) {
            LocalDate startLocalDate = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate pushTimeStartLocalDate = startLocalDate.plusDays(-1);
            LocalDate pushTimeEndLocalDate = startLocalDate;

            List<WubaSubmitConversionData> submitDataList = wubaSubmitConversionDataMapper.findSubmitDataByPushTime(
                    apiCode, pushTimeStartLocalDate.toString(), pushTimeEndLocalDate.toString(), indexId, pageSize);
            if (CollectionUtils.isEmpty(submitDataList)) {
                break;
            }
            indexId = submitDataList.get(submitDataList.size() - 1).getId();

            for (WubaSubmitConversionData data : submitDataList) {
                StringBuilder buffer = new StringBuilder();
                buffer.append(data.getCell()).append(",")
                        .append(data.getMarketingTime())
                        .append("\r\n");
                fw.append(buffer.toString());
                totalSize++;
            }
        }

        // 更新文件任务状态为2-文件生成成功
        updateTaskStatus(transferFileTask, totalSize);
        log.warn(TITLE + "本地文件生成成功, apiCode: {}, startDate: {}, total: {}", apiCode, startDate, totalSize);
    }

    private String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String concat = apiCode.concat("_").concat(yyyyMMdd).concat("_").concat(contextId.toString());
        return concat;
    }

    private boolean checkExecuteTime(){
        String executeTime = marketingCommonConfig.getWuBaSubmitDataToFileExecuteTime();
        if(StringUtils.isBlank(executeTime)){
            executeTime = EXECUTE_TIME_DEFAULT;
        }
        Date now = new Date();
        Date executeDateTime = DateHelper.getDatePlusHourMinuteSecond(now, " " + executeTime);
        if(now.after(executeDateTime)){
            log.warn(TITLE+"到达执行时间");
           return true;
        }
        log.warn(TITLE+"未到达执行时间");
        return false;
    }

    private List<String> parseParam(String param){
        List<String> startDateList = new ArrayList<>();
        if(StringUtils.isEmpty(param)){
            String startDate = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            startDateList.add(startDate);
            return startDateList;
        }
        startDateList = JSONObject.parseObject(param, List.class);
        return startDateList;

    }

    private void updateTaskStatus(TransferFileTask transferFileTask, Integer totalSize){
        TransferFileTask task = new TransferFileTask();
        task.setId(transferFileTask.getId());
        task.setStatus(2);
        task.setFileName(transferFileTask.getFileName());
        task.setFilePath(transferFileTask.getFilePath());
        task.setTaskNumber(totalSize);
        task.setUpdateTime(new Date());
        transferFileTaskMapper.updateByPrimaryKeySelective(task);
    }

}
