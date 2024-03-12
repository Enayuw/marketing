package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSON;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.entity.TransferFileTaskExample;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.RuleRedisServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 国美转化数据提取
 */
@Slf4j
@Service
public class TransferToFileByGomeServiceImpl implements ITransferToFileService {

    @Resource
    SyncConfigService syncConfigService;
    @Resource
    private TransferFileTaskMapper transferFileTaskMapper;
    @Resource
    private RuleRedisServiceImpl ruleRedisService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    TableCreateServiceImpl tableCreateService;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;


    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    final static String EXECUTE_TIME = "10:30:00";

    private final static String FILE_HEADER = "custNum,userType,registerTime,ifLogin,loginTime,ifApply,applyDt,applyResult,auditTime," +
            "auditAmount,ifLent,lentTime,lentAmount,unlentAmount,applyLoan,applyLoanTime,cell";


    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[:SSS]");
    private final static DateTimeFormatter DATE_TIME_2_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        return "";
    }

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode, String myParam) {
        List<TransferFileTask> resultList = new ArrayList<>();
        Date now = new Date();
        //可配置
        String execute = StringUtils.isBlank(marketingCommonConfig.getGomeFileExecTime()) ? EXECUTE_TIME : marketingCommonConfig.getGomeFileExecTime();
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, " " + execute);
        if (now.after(executeTime)) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("国美转化提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                String fileName = String.format("guomeizhuanhua_%s.txt", yyyyMMdd);
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
        log.warn("国美转化提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
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
        String transferFile = "guomeizhuanhua_";
        fileName.append(transferFile).append(recordDate).append(".txt");
        String fileAllPath = descPath.concat(fileName.toString());
        transferFileTask.setFileName(fileName.toString());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8))) {
            fw.append(FILE_HEADER);
            fw.append("\r\n");
            writeGometransferToFile(fw, apiCode, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void writeGometransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask) throws IOException {
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

            // 查询上传表中最新的cell
            List<String> marketingSyncUserList = list.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toList());
            List<List<String>> partition = ListUtils.partition(marketingSyncUserList, 500);
            Map<String, MarketingSyncUser> preUserMap = new HashMap<>();
            for (List<String> strings : partition) {
                Set<String> set = new HashSet<>(strings);
                List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(apiCode, set);
                Map<String, MarketingSyncUser> map = preUserByTask.stream().collect(
                        Collectors.groupingBy(MarketingSyncUser::getCustNum
                                , Collectors.collectingAndThen(
                                        Collectors.reducing((v1, v2) ->
                                                v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2)
                                        , Optional::get)));
                preUserMap.putAll(map);
            }
            for (MarketingTransferSyncUser data : list) {
                    String custNum = data.getCustNum();
                    StringBuilder sb = new StringBuilder();
                    sb.append(deleteNull(custNum))
                            .append(deleteNull(data.getUserType()))
                            .append(formDateStr(data.getRegisterTime()))
                            .append(deleteNull(data.getIfLogin()))
                            .append(formDateStr(data.getLoginTime()))
                            .append(deleteNull(data.getIfApply()))
                            .append(formDateStr(data.getApplyDt()))
                            .append(deleteNull(data.getApplyResult()))
                            .append(formDateStr(data.getAuditTime()))
                            .append(deleteNull(data.getAuditAmount()))
                            .append(deleteNull(data.getIfLent()))
                            .append(formDateStr(data.getLentTime()))
                            .append(deleteNull(data.getLentAmount()))
                            .append(deleteNull(data.getUnlentAmount()))
                            .append(deleteNull(JSON.parseObject(data.getReserveField1()).getString("applyLoan")))
                            .append(deleteNull(JSON.parseObject(data.getReserveField1()).getString("applyLoanTime")));
                    if (preUserMap != null && preUserMap.get(custNum)!=null) {
                        sb.append(DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance().decode(preUserMap.get(custNum).getCell()).getBytes()));
                    }
                    sb.append("\r\n");
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
        log.warn("国美转化提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
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

    String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return apiCode.concat("_").concat(yyyyMMdd).concat("_").concat(contextId.toString());
    }

}
