package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.entity.TransferFileTaskExample;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.RuleRedisServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.SyncConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * D20230303桔子转化数据提取
 * http://c.100credit.cn/pages/viewpage.action?pageId=103553228
 *
 * @author Guo Zeqiang
 * @dateTime 2023-03-08 17:08
 */
@Slf4j
@Service
public class TransferToFileByOrangeServiceImpl implements ITransferToFileService {
    @Resource
    private SyncConfigService syncConfigService;
    @Resource
    private RuleRedisServiceImpl ruleRedisService;
    @Resource
    private TransferFileTaskMapper transferFileTaskMapper;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private TableCreateServiceImpl tableCreateService;
    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[:SSS]");
    private final static DateTimeFormatter DATE_TIME_2_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        if (StringUtils.isNotBlank(jobParameter)) {
            String[] split = jobParameter.split(";");
            for (String s : split) {
                if (s.contains(apiCode)) {
                    String[] split1 = s.split("#");
                    if (split1.length == 2) {
                        return " " + split1[1];
                    }
                }
            }
        }
        return " 11:00:00";
    }

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode, String myParam) {
        List<TransferFileTask> resultList = new ArrayList<>();
        Date now = new Date();
        //可配置
        String execute = isMyParam(apiCode, myParam);
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, execute);
        if (now.after(executeTime)) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("桔子转化提取-开始执行,apiCode ={}", apiCode);
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
    public Result<?> actionTransferToFile(TransferFileTask transferFileTask, String jobParameter) {
        log.warn("桔子转化提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(recordDate).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            if (!writeDic.mkdirs()) {
                log.error(writeDic.getAbsolutePath() + "创建失败！");
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
        }
        StringBuilder fileName = new StringBuilder();
        String transferFile = "_zhuanhua_";
        fileName.append(apiCode).append(transferFile).append(recordDate).append(".txt");
        String fileAllPath = descPath.concat(fileName.toString());
        transferFileTask.setFileName(fileName.toString());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            fw.append("custNum,ifLogin,customName,loginTime,ifApply,applyDt,auditAmount,applyResult,applyTime,ifLent,lentTime,lentAmount,orderId");
            fw.append("\r\n");
            writePPDTransferToFile(fw, apiCode, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void writePPDTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask) throws IOException {
        long start = System.currentTimeMillis();
        int page = 0;
        int offset = 2000;
        boolean mark = Boolean.TRUE;
        int totalSize = 0;
        String tcId = tableCreateService.getTcId(apiCode);
        MarketingTransferSyncUser syncUser = new MarketingTransferSyncUser();
        syncUser.setApiCode(apiCode);
        syncUser.settCid(tcId);
        String localDateStr = transferFileTask.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        syncUser.setRequestData(localDateStr);
        while (mark) {
            List<MarketingTransferSyncUser> list = marketingTransferSyncUserMapper.findTransferByApiCodeAndCreateTimePage(syncUser
                    , null, null, null, page * offset, offset);
            if (CollectionUtils.isEmpty(list)) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            //判断是否再有效期内
            for (MarketingTransferSyncUser data : list) {
                String custNum = data.getCustNum();
                String orderId = JSONObject.parseObject(data.getReserveField1()).getString("orderId");
                String sb = deleteNull(custNum) +
                        deleteNull(data.getIfLogin()) +
                        deleteNull(data.getCustomName()) +
                        formDateStr(data.getLoginTime()) +
                        deleteNull(data.getIfApply()) +
                        formDateStr(data.getApplyDt()) +
                        deleteNull(data.getAuditAmount()) +
                        deleteNull(data.getApplyResult()) +
                        formDateStr(data.getApplyTime()) +
                        deleteNull(data.getIfLent()) +
                        formDateStr(data.getLentTime()) +
                        deleteNull(data.getLentAmount()) +
                        (StringUtils.isNotEmpty(orderId) ? orderId : "") +
                        "\r\n";
                fw.append(sb);
                totalSize = totalSize + 1;
            }
            list.clear();
        }
        TransferFileTask updatetask = new TransferFileTask();
        updatetask.setId(transferFileTask.getId());
        updatetask.setStatus(2);
        updatetask.setFileName(transferFileTask.getFileName());
        updatetask.setFilePath(transferFileTask.getFilePath());
        updatetask.setTaskNumber(totalSize);
        updatetask.setUpdateTime(new Date());
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("桔子转化提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
    }

    /**
     * 2023-03-08 19:33
     * 删除null字符串
     */
    private String deleteNull(String value) {
        return (StringUtils.isNotEmpty(value) ? value : "").concat(",");
    }

    /**
     * 2023-03-08 19:33
     * 格式化时间
     */
    private String formDateStr(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr
                    , DATE_TIME_FORMATTER).format(DATE_TIME_2_FORMATTER).concat(",");
        } catch (Exception e) {
            return deleteNull(dateTimeStr);
        }
    }

    private String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String concat = apiCode.concat("_").concat(yyyyMMdd).concat("_").concat(contextId.toString());
        return concat;
    }
}
