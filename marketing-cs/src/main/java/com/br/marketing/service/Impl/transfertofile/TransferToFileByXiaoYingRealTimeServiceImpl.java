package com.br.marketing.service.Impl.transfertofile;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 小赢断点数据分类
 *
 * @author Guo Zeqiang
 * @dateTime 2022/5/27 16:16
 */
@Slf4j
@Service
public class TransferToFileByXiaoYingRealTimeServiceImpl implements ITransferToFileService {

    @Autowired
    SyncConfigService syncConfigService;
    @Resource
    private TransferFileTaskMapper transferFileTaskMapper;
    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    private RuleRedisServiceImpl ruleRedisService;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    private final String DEFAULT_EXTRACT_TIME = "10:00:00";

    private final DateTimeFormatter LINE_DATE_COLON_TIME_FORMAT = DateTimeFormatter.ofPattern(
            DateHelper.LINE_DATE_COLON_TIME_FORMAT);

    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[:SSS]");

    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        if(StringUtils.isNotEmpty(jobParameter)){
            String[] split = jobParameter.split(";");
            for(String s : split){
                String paramApiCode = s.split("#")[0];
                if(apiCode.equals(paramApiCode)  && marketingCommonConfig.getXiaoYingTransferExtractApiCodes().contains(paramApiCode)){
                    return s.split("#")[1];
                }
            }
        }
        return "";
    }

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode,String myParam) {
        List<TransferFileTask> list = new ArrayList<>();
        //1.登录未申请授信（断点）
        List<TransferFileTask> duanDian = buildTransferTaskDengLuDuanDian(apiCode);
        //2.促提
        List<TransferFileTask> cuTis = buildTransferTaskCuTi(apiCode);
        //3.小赢全量转化数据提取
        List<TransferFileTask> full = buildTransferTaskFullLoad(apiCode);
        list.addAll(duanDian);
        list.addAll(cuTis);
        list.addAll(full);
        Result<List<TransferFileTask>> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());
        result.setDate(list);
        return result;
    }

    public List<TransferFileTask> buildTransferTaskFullLoad(String apiCode) {
        int full = 3;
        List<TransferFileTask> resultList = new ArrayList<>();
        String extractTime = marketingCommonConfig.getXiaoYingFullLoadTransferExtractJobTime();
        if (StringUtils.isNull(extractTime)) {
            extractTime = DEFAULT_EXTRACT_TIME;
        }
        LocalTime localTime = LocalTime.parse(extractTime);
        if (LocalTime.now().isAfter(localTime)) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            TransferFileTaskExample example = new TransferFileTaskExample();
            example.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd)
                    .andFileTypeEqualTo(full);
            if (CollectionUtils.isEmpty(transferFileTaskMapper.selectByExample(example))) {
                log.warn("小赢全量转化-添加到任务,apiCode:{}", apiCode);
                String name = "xiaoyingzhuanhua";
                TransferFileTask transferFileTask = insertFileTask(apiCode, name, full
                        , name, yyyyMMdd);
                resultList.add(transferFileTask);
            }
        }
        return resultList;
    }

    /**
     * 2022/5/27 16:36
     * 登录断点
     */
    private List<TransferFileTask> buildTransferTaskDengLuDuanDian(String apiCode) {
        int dengLuDuanDian = 1;
        List<TransferFileTask> resultList = new ArrayList<>();
        String extractTime = marketingCommonConfig.getXiaoYingDengLuDuanDianTransferExtractJobTime();
        if (StringUtils.isNull(extractTime)) {
            extractTime = DEFAULT_EXTRACT_TIME;
            marketingCommonConfig.setXiaoYingDengLuDuanDianTransferExtractJobTime(DEFAULT_EXTRACT_TIME);
        }
        if (StringUtils.isBlank(extractTime)) {
            return resultList;
        }
        LocalTime localTime = LocalTime.parse(extractTime);
        if (LocalTime.now().isAfter(localTime)) {
            String yyyyMmDd = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            TransferFileTaskExample example = new TransferFileTaskExample();
            example.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMmDd)
                    .andFileTypeEqualTo(dengLuDuanDian);
            if (CollectionUtils.isEmpty(transferFileTaskMapper.selectByExample(example))) {
                log.warn("小赢登录未申请授信-添加到任务,apiCode:{}", apiCode);
                String name = "xiaoyingdengluduandian";
                TransferFileTask transferFileTask = insertFileTask(apiCode, name, dengLuDuanDian
                        , name, yyyyMmDd);
                resultList.add(transferFileTask);
            }
        }
        return resultList;
    }

    /**
     * 2022/5/27 16:36
     * 促提
     */
    private List<TransferFileTask> buildTransferTaskCuTi(String apiCode) {
        int cuti = 2;
        List<TransferFileTask> resultList = new ArrayList<>();
        String extractTime = marketingCommonConfig.getXiaoYingCuTiTransferExtractJobTime();
        if (StringUtils.isNull(extractTime)) {
            extractTime = DEFAULT_EXTRACT_TIME;
            marketingCommonConfig.setXiaoYingCuTiTransferExtractJobTime(DEFAULT_EXTRACT_TIME);
        }
        if (StringUtils.isBlank(extractTime)) {
            return resultList;
        }
        LocalTime localTime = LocalTime.parse(extractTime);
        if (LocalTime.now().isAfter(localTime)) {
            String yyyyMmDd = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            TransferFileTaskExample example = new TransferFileTaskExample();
            example.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMmDd)
                    .andFileTypeEqualTo(cuti);
            if (CollectionUtils.isEmpty(transferFileTaskMapper.selectByExample(example))) {
                log.warn("小赢促提-添加到任务,apiCode:{}", apiCode);
                String name = "xiaoyingcuti";
                TransferFileTask transferFileTask = insertFileTask(apiCode, name, cuti, name, yyyyMmDd);
                resultList.add(transferFileTask);
            }
        }
        return resultList;
    }

    /**
     * 2022/5/27 17:22
     * 插入任务
     */
    private TransferFileTask insertFileTask(String apiCode, String fileName
            , int fileType, String fileChildDir, String startDate) {
        Long transferFileContextId = ruleRedisService.getTransferFileContextId();
        String batchNumber = createBatchNumber(apiCode, transferFileContextId);
        TransferFileTask transferFileTask = new TransferFileTask();
        transferFileTask.setApiCode(apiCode);
        transferFileTask.setFileType(fileType);
        transferFileTask.setBatchNumber(batchNumber);
        transferFileTask.setStartDate(startDate);
        transferFileTask.setFileName(fileName.concat("_")
                .concat(startDate).concat(".txt"));
        transferFileTask.setFileChildDir(fileChildDir);
        transferFileTask.setTaskNumber(0);
        transferFileTask.setContextId(transferFileContextId);
        transferFileTask.setCreateTime(new Date());
        transferFileTask.setUpdateTime(new Date());
        transferFileTaskMapper.insertSelective(transferFileTask);
        return transferFileTask;
    }

    /**
     * jobParameter 格式：yyyy-MM-dd HH:mm:ss,yyyy-MM-dd HH:mm:ss
     * eg: 2022-05-15 09:12:36,2022-06-15 09:12:36
     */
    @Override
    public Result<Object> actionTransferToFile(TransferFileTask transferFileTask, String jobParameter) {
        Result<Object> result = new Result<>();
        String[] timeStrs = StringUtils.isBlank(jobParameter) ? new String[]{""} : jobParameter.split(",");
        File file = createMkdirFile(transferFileTask);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)
                , StandardCharsets.UTF_8))) {
            writeTransferTaskFile(fw, transferFileTask, timeStrs[0], timeStrs.length > 1 ? timeStrs[1] : "");
            result.setCode(ResultCode.SUCCESS.getValue());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.setCode(ResultCode.FAIL.getValue());
            result.setDate(e.getMessage());
        }
        return result;
    }


    /**
     * 2022/5/27 16:59
     * 生成批次号
     */
    private String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMmDd = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return apiCode.concat("_").concat(yyyyMmDd).concat("_").concat(contextId.toString());
    }


    /**
     * 2022/5/27 16:59
     * 创建目录文件
     */
    private File createMkdirFile(TransferFileTask transferFileTask) {
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();
        String childDir = StringUtils.isNotEmpty(transferFileTask.getFileChildDir()) ? (transferFileTask.getFileChildDir() + "/") : "";
        String descPath = syncConfigService.getPath().concat("transferToFile").concat(File.separator).concat(childDir).concat(apiCode).
                concat(File.separator).concat(recordDate).concat(File.separator);
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            if (!writeDic.mkdirs()) {
                log.error("小赢提取文件目录创建失败！目录:{},transferFileTask:{}", descPath, transferFileTask);
            }
        }
        transferFileTask.setFilePath(descPath);
        return new File(descPath.concat(transferFileTask.getFileName()));
    }

    /**
     * 2022/5/27 16:59
     * 组装文件
     */
    private void writeTransferTaskFile(Writer fw
            , TransferFileTask transferFileTask, String firstTime, String lastTime) throws IOException {
        String startTime;
        String endTime;
        String extractTime;
        MarketingTransferSyncUser transferSyncUser = new MarketingTransferSyncUser();
        switch (transferFileTask.getFileType()) {
            case 1:
                fw.append("custNum,cell,ifLogin,ifApply,loginTime").append("\r\n");
                extractTime = marketingCommonConfig.getXiaoYingDengLuDuanDianTransferExtractJobTime();
                startTime = getStartTime(extractTime, firstTime);
                endTime = getEndTime(extractTime, lastTime);
                transferSyncUser.setIfApply("0");
                transferSyncUser.setIfLogin("1");
                writeFile(transferFileTask, transferSyncUser, startTime, endTime, (transfer, cell) -> {
                    try {
                        writeTransferTaskDengLuDuanDian(transfer, cell, fw);
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                });
                break;
            case 2:
                fw.append("custNum,cell,applyResult,ifLent,loginTime,ifApply,ifLogin,auditTime").append("\r\n");
                extractTime = marketingCommonConfig.getXiaoYingDengLuDuanDianTransferExtractJobTime();
                startTime = getStartTime(extractTime, firstTime);
                endTime = getEndTime(extractTime, lastTime);
                transferSyncUser.setIfLent("0");
                transferSyncUser.setApplyResult("1");
                writeFile(transferFileTask, transferSyncUser, startTime, endTime, (transfer, cell) -> {
                    try {
                        writeTransferTaskCuTi(transfer, cell, fw);
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                });
                break;
            case 3:
                fw.append("custNum,cell,ifLogin,loginTime,ifApply,applyDt,applyResult,auditTime,auditAmount,ifLent,lentTime,lentAmount").append("\r\n");
                extractTime = marketingCommonConfig.getXiaoYingFullLoadTransferExtractJobTime();
                startTime = getStartTime(extractTime, firstTime);
                endTime = getEndTime(extractTime, lastTime);
                writeFile(transferFileTask, transferSyncUser, startTime, endTime, (transfer, cell) -> {
                    try {
                        writeTransferTaskFull(transfer, cell, fw);
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                });
                break;
            default:
                log.error("小赢任务未找到对应的actionTransferToFile方法,请检查fileType, transferFileTask:{}"
                        , transferFileTask);
        }
    }

    private void writeTransferTaskFull(MarketingTransferSyncUser syncUser, String cell, Writer fw) throws IOException {
        fw.append(syncUser.getCustNum()).append(",");
        fw.append(cell).append(",");
        fw.append(handleNull(syncUser.getIfLogin())).append(",");
        fw.append(dateTimeFormatter(syncUser.getLoginTime())).append(",");
        fw.append(handleNull(syncUser.getIfApply())).append(",");
        fw.append(dateTimeFormatter(syncUser.getApplyDt())).append(",");
        fw.append(handleNull(syncUser.getApplyResult())).append(",");
        fw.append(dateTimeFormatter(syncUser.getAuditTime())).append(",");
        fw.append(handleNull(syncUser.getAuditAmount())).append(",");
        fw.append(handleNull(syncUser.getIfLent())).append(",");
        fw.append(dateTimeFormatter(syncUser.getLentTime())).append(",");
        fw.append(handleNull(syncUser.getLentAmount()));
        fw.append("\r\n");
        fw.flush();
    }

    /**
     * 2022/5/27 21:55
     * 获取开始时间
     */
    private String getStartTime(String extractTime, String firstTime) {
        LocalDateTime localDateTime = LocalTime.parse(extractTime).atDate(LocalDate.now())
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        return StringUtils.isNotBlank(firstTime) ? firstTime
                : localDateTime.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toLocalDateTime()
                .format(LINE_DATE_COLON_TIME_FORMAT);
    }

    /**
     * 2022/5/27 21:55
     * 获取结束时间
     */
    private String getEndTime(String extractTime, String lastTime) {
        return StringUtils.isNotBlank(lastTime) ? lastTime
                : LocalTime.parse(extractTime).atDate(LocalDate.now()).atZone(ZoneId.systemDefault())
                .toLocalDateTime().format(LINE_DATE_COLON_TIME_FORMAT);
    }

    /**
     * 2022/5/27 21:56
     * 登录未申请授信
     */
    private void writeTransferTaskDengLuDuanDian(MarketingTransferSyncUser syncUser, String cell
            , Writer fw) throws IOException {
        fw.append(syncUser.getCustNum()).append(",");
        fw.append(cell).append(",");
        fw.append(syncUser.getIfLogin()).append(",");
        fw.append(syncUser.getIfApply()).append(",");
        fw.append(dateTimeFormatter(syncUser.getLoginTime()));
        fw.append("\r\n");
        fw.flush();
    }

    /**
     * 2022/5/27 21:56
     * 促提
     */
    private void writeTransferTaskCuTi(MarketingTransferSyncUser syncUser, String cell
            , Writer fw) throws IOException {
        fw.append(syncUser.getCustNum()).append(",");
        fw.append(cell).append(",");
        fw.append(syncUser.getApplyResult()).append(",");
        fw.append(syncUser.getIfLent()).append(",");
        fw.append(dateTimeFormatter(syncUser.getLoginTime())).append(",");
        fw.append(handleNull(syncUser.getIfApply())).append(",");
        fw.append(handleNull(syncUser.getIfLogin())).append(",");
        fw.append(dateTimeFormatter(syncUser.getAuditTime()));
        fw.append("\r\n");
        fw.flush();
    }

    /**
     * 2022/5/28 14:40
     * 调整时间格式为 yyyy-MM-dd HH:mm:ss
     */
    private String dateTimeFormatter(String dateTimeStr) {
        if(StringUtils.isBlank(dateTimeStr)){
            return "";
        }else{
            try {
                return LocalDateTime.parse(dateTimeStr
                        , DATE_TIME_FORMATTER).format(LINE_DATE_COLON_TIME_FORMAT);
            } catch (Exception e) {
                log.warn("格式有误 -- {}", dateTimeStr);
                return dateTimeStr;
            }
        }
    }

    /**
     * 2022/5/30 10:00
     * <p>
     * 处理null
     */
    private String handleNull(String value) {
        return StringUtils.isNull(value) ? "" : value;
    }

    /**
     * 2022/5/27 21:48
     * 开始写文件
     */
    private void writeFile(TransferFileTask transferFileTask, MarketingTransferSyncUser transferSyncUser, String startTime
            , String endTime, BiConsumer<MarketingTransferSyncUser, String> function) {
        long start = System.currentTimeMillis();
        String apiCode = transferFileTask.getApiCode();
        String tcId = tableCreateService.getTcId(apiCode);
        int page = 0;
        int offset = 2000;
        transferSyncUser.settCid(tcId);
        transferSyncUser.setApiCode(apiCode);
        int sum = 0;
        while (true) {
            int rowCount = page * offset;
            List<MarketingTransferSyncUser> list = marketingTransferSyncUserMapper
                    .findTransferByApiCodeAndCreateTimePage(transferSyncUser, startTime, endTime, ""
                            , rowCount, offset);
            if (CollectionUtils.isEmpty(list)) {
                break;
            }
            page++;
            sum += list.size();
            Map<String, MarketingSyncUser> syncUserMap = getSyncUserMap(list, apiCode);
            for (MarketingTransferSyncUser syncUser : list) {
                String custNum = syncUser.getCustNum();
                String cell = "";
                if (syncUserMap.containsKey(custNum)) {
                    cell = syncUserMap.get(custNum).getCell();
                    String decode = BrCipherMaker.getInstance().decode(cell);
                    cell = StringUtils.isBlank(decode) ? cell : DigestUtils.md5DigestAsHex(decode.getBytes());
                }
                function.accept(syncUser, cell);
            }
            if (list.size() < offset) {
                break;
            }
        }
        TransferFileTask updatetask = new TransferFileTask();
        updatetask.setId(transferFileTask.getId());
        updatetask.setStatus(2);
        updatetask.setTaskNumber(sum);
        updatetask.setUpdateTime(new Date());
        updatetask.setFilePath(transferFileTask.getFilePath());
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("小赢数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, sum);
    }

    /**
     * 2022/5/27 21:47
     * 获取上传标准中案件编号对应是数据
     */
    private Map<String, MarketingSyncUser> getSyncUserMap(List<MarketingTransferSyncUser> list, String apiCode) {
        Set<String> set = list.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
        List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(apiCode, set);
        return preUserByTask.stream().collect(
                Collectors.toMap(MarketingSyncUser::getCustNum, Function.identity(), (v1, v2) ->
                        v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2));
    }
}
