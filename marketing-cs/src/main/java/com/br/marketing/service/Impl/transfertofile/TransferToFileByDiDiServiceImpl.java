package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.RuleRedisServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * 滴滴转化数据提取
 */
@Slf4j
@Service
public class TransferToFileByDiDiServiceImpl implements ITransferToFileService {

    private static final String EXECUTE_TIME = "11:00:00";
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TransferFileTaskMapper transferFileTaskMapper;

    @Autowired
    private RuleRedisServiceImpl ruleRedisService;

    @Autowired
    SyncConfigService syncConfigService;

    @Autowired
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        return "";
    }

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode, String myParam) {
        List<TransferFileTask> resultList = new ArrayList<>();
        Date now = new Date();
        //可配置
        String execute = StringUtils.isBlank(marketingCommonConfig.getDidiModeingFileExecTime()) ? EXECUTE_TIME : marketingCommonConfig.getDidiModeingFileExecTime();
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, " " + execute);
        if (now.after(executeTime)) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            String fileName = String.format("didi_zhuanhua_%s.txt", yyyyMMdd);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("滴滴转化数据提取-开始执行,apiCode ={}", apiCode);
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
    public Result actionTransferToFile(TransferFileTask transferFileTask, String jobParameter) {
        log.warn("滴滴转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(transferFileTask.getStartDate()).concat("/");
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
            fw.append("custNum,data");
            fw.append("\r\n");
            writeDiDiTransferToFile(fw, apiCode, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void writeDiDiTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask) throws IOException {
        Long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        int totalSize = 0;
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.createCriteria().andApiCodeEqualTo(apiCode);
        example.setOrderByClause("create_time desc limit 1");
        example.settCid(tcId);
        List<MarketingTransferSyncUser> transferList = marketingTransferSyncUserMapper.selectByExample(example);
        String requestDate = transferList.get(0).getRequestData();
        Long minId = null;
        Boolean isContiue = Boolean.TRUE;
        while (isContiue) {
            //查询最新一天的全量转化数据
            List<MarketingTransferSyncUser> marketingTransferSyncUsers = marketingTransferSyncUserMapper.getTransferByRequestDate(tcId, apiCode, requestDate, minId);
            if (marketingTransferSyncUsers.size() <= 0) {
                isContiue = Boolean.FALSE;
                continue;
            }
            minId = marketingTransferSyncUsers.get(marketingTransferSyncUsers.size() - 1).getId() + 1;
            for (MarketingTransferSyncUser transferSyncUser : marketingTransferSyncUsers) {

                JSONObject jsonObject = JSON.parseObject(transferSyncUser.getReserveField1());
                StringBuilder sb = new StringBuilder();
                sb.append(transferSyncUser.getCustNum().concat(","));
                sb.append(jsonObject.getString("data"));
                sb.append("\r\n");
                fw.append(sb.toString());
            }
            totalSize = totalSize + marketingTransferSyncUsers.size();
        }
        TransferFileTask updatetask = new TransferFileTask();
        updatetask.setId(transferFileTask.getId());
        updatetask.setStatus(2);
        updatetask.setFileName(transferFileTask.getFileName());
        updatetask.setFilePath(transferFileTask.getFilePath());
        updatetask.setTaskNumber(totalSize);
        updatetask.setUpdateTime(new Date());
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("滴滴转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
    }


    String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String concat = apiCode.concat("_").concat(yyyyMMdd).concat("_").concat(contextId.toString());
        return concat;
    }
}
