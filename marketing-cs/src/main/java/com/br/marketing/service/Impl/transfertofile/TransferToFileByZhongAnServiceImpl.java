package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.es.util.BrCipherMaker;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.mapper.ZhonganMarketingBanMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.RuleRedisServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author songjuanjnuan
 * @Date 2022/12/08 16:31
 * @Description:众安转化数据提取
 */
@Slf4j
@Service
public class TransferToFileByZhongAnServiceImpl implements ITransferToFileService {

    @Autowired
    SyncConfigService syncConfigService;
    @Autowired
    private TransferFileTaskMapper transferFileTaskMapper;
    @Autowired
    private RuleRedisServiceImpl ruleRedisService;
    @Resource
    private ZhonganMarketingBanMapper zhonganMarketingBanMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    // 众安异业撞库时间
    final static String EXECUTE_TIME = " 10:00:00";

    final static String ZHONGAN_FILE = "_zhonganzhuanhua_";

    /**
     * 时间格式 yyyy-MM-dd
     */
    final DateTimeFormatter YYYYMMDDLINEDF = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_FORMAT);

    final static DateTimeFormatter YYYYMMDDSHORTDF = DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT);

    // 众安转化数据提取时间
    final static String EXECUTE_TIME_ZHUANHUA = "9:00:00";

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    public static final String ZHUANHUA_COLUMU_NAME = "custNum,cell,userType,createTime,bizType,eventTime,eventType," +
            "amountStatus,highApplyStatus,auditAmountGroup,lentAmountGroup";

    @Resource
    TransferDataValidityPeriodService validityPeriodService;

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
        // 异业撞库
        buildTransferTaskByYiYe(apiCode, resultList);
        // 转化数据提取
        buildTransferTaskByZhuanHua(apiCode, myParam);

        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(resultList);
    }

    @Override
    public Result actionTransferToFile(TransferFileTask transferFileTask, String jobParameter) {
        // 1:异业撞库；2：转化数据提取
        if (1 == transferFileTask.getFileType()) {
            return actionTransferToFileByYiYe(transferFileTask);
        } else if (2 == transferFileTask.getFileType()) {
            return actionTransferToFileByZhuanHua(transferFileTask, jobParameter);
        }

        log.error("未找到对应的actionTransferToFile方法,请检查fileType");
        return new Result().setCode(ResultCode.FAIL.getValue()).setDate("未找到对应的actionTransferToFile方法,请检查fileType");
    }

    private void buildTransferTaskByYiYe(String apiCode, List<TransferFileTask> resultList) {
        Date now = new Date();
        //可配置
        String execute = EXECUTE_TIME;
        if (StringUtils.isNotEmpty(marketingCommonConfig.getZhongAnTransferExecuteTime())) {
            execute = " " + marketingCommonConfig.getZhongAnTransferExecuteTime();
        }
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, execute);
        if (now.after(executeTime)) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("众安异业撞库数据提取-开始执行,apiCode ={}", apiCode);
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
    }

    private void buildTransferTaskByZhuanHua(String apiCode, String myParam) {
        List<TransferFileTask> resultList = new ArrayList<>();
        //可配置
        String execute = StringUtils.isBlank(marketingCommonConfig.getZhongAnFileExecTime()) ? EXECUTE_TIME_ZHUANHUA :
                marketingCommonConfig.getZhongAnFileExecTime();
        LocalTime localTime = LocalTime.parse(execute);
        boolean isParam = StringUtils.isNotBlank(myParam);
        // 指定日期提取时不限制时间
        if (LocalTime.now().isAfter(localTime) || isParam) {
            // 当天的记录
            String dateyyyymmddStr = isParam ? myParam.replace("-", "")
                    : LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String localDateStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            // fileType 1:异业撞库；2：转化数据提取
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(localDateStr).andFileTypeEqualTo(2);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("众安转化数据提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(2);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName(String.format("zhongandai_zhuanhua_%s.txt", dateyyyymmddStr));
                transferFileTask.setTaskNumber(0);
                transferFileTask.setStartDate(localDateStr);
                transferFileTask.setContextId(transferFileContextId);
                transferFileTask.setCreateTime(new Date());
                transferFileTask.setUpdateTime(new Date());
                transferFileTask.setFileChildDir("child1");
                transferFileTaskMapper.insertSelective(transferFileTask);
                resultList.add(transferFileTask);
            }
        }
    }

    private Result actionTransferToFileByYiYe(TransferFileTask transferFileTask) {
        log.warn("众安异业撞库数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(recordDate).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        StringBuilder fileName = new StringBuilder();
        fileName.append(apiCode).append(ZHONGAN_FILE).append(recordDate).append(".txt");
        String fileAllPath = descPath.concat(fileName.toString());
        transferFileTask.setFileName(fileName.toString());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file), "UTF-8"));) {
            fw.append("cell,zk_date");
            fw.append("\r\n");
            writeZhongAnTransferToFileByYiYe(fw, apiCode, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private Result actionTransferToFileByZhuanHua(TransferFileTask transferFileTask, String jobParameter) {
        log.warn("众安转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String requestDate = StringUtils.isBlank(jobParameter)
                ? LocalDate.now().toString() : jobParameter;
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

        String fileAllPath = descPath.concat(transferFileTask.getFileName());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            fw.append(ZHUANHUA_COLUMU_NAME);
            fw.append("\r\n");
            writeZhongAnTransferToFileZhuanHua(fw, apiCode, transferFileTask, requestDate);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }


    private void writeZhongAnTransferToFileByYiYe(Writer fw, String apiCode, TransferFileTask transferFileTask) throws IOException {
        Long start = System.currentTimeMillis();
        String recordDate = transferFileTask.getStartDate();
        LocalDate localDate = LocalDate.parse(recordDate, YYYYMMDDSHORTDF);
        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        int totalSize = 0;
        while (mark) {
            Result<List<ZhonganMarketingBan>> transferData = getOrderTransferData(apiCode, localDate.toString(), page);
            if (!ResultCode.SUCCESS.getValue().equals(transferData.getCode())) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            List<ZhonganMarketingBan> data = transferData.getData();
            //cell,applet_date
            for (ZhonganMarketingBan transferFilterData : data) {
                if (StringUtils.isNotBlank(transferFilterData.getCell())) {
                    String md5 = DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance().decode(transferFilterData.getCell()).getBytes());
                    StringBuilder sb = new StringBuilder();
                    sb.append(md5.concat(","));
                    sb.append(transferFilterData.getZkDate());
                    sb.append("\r\n");
                    fw.append(sb.toString());
                    totalSize = totalSize + 1;
                }
            }
            data.clear();
        }

        TransferFileTask updatetask = new TransferFileTask();
        updatetask.setId(transferFileTask.getId());
        updatetask.setStatus(2);
        updatetask.setFileName(transferFileTask.getFileName());
        updatetask.setFilePath(transferFileTask.getFilePath());
        updatetask.setTaskNumber(totalSize);
        updatetask.setUpdateTime(new Date());
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("众安异业撞库数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
    }

    private void writeZhongAnTransferToFileZhuanHua(Writer fw, String apiCode, TransferFileTask transferFileTask, String requestDate) {
        long start = System.currentTimeMillis();
        int page = 0;
        int offset = 2000;
        boolean mark = Boolean.TRUE;
        int totalSize = 0;
        String tcId = tableCreateService.getTcId(apiCode);
        LocalDate localDate = LocalDate.parse(requestDate, YYYYMMDDLINEDF);
        while (mark) {
            // 获取前一天的日期
            LocalDate yesterday = localDate.minusDays(1);
            List<MarketingTransferSyncUser> list = marketingTransferSyncUserMapper
                    .getTransferDataByRequestDataAndApiCode(tcId, apiCode, yesterday.toString(),
                    page * offset);
            if (CollectionUtils.isEmpty(list)) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            // 过滤有效期内数据
            List<MarketingTransferSyncUser> periodList = new ArrayList<>(offset);
            Set<String> set = list.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            //判断转化数据是否在有效期内
            Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum = validityPeriodService
                    .getValidityPeriodsByCustNum(set, apiCode, yesterday);


            for (MarketingTransferSyncUser transferSyncUser : list) {
                String custNum = transferSyncUser.getCustNum();
                String userType = transferSyncUser.getUserType();

                SyncUserValidityPeriodsBO boMap = validityPeriodsByCustNum.get(custNum + userType);
                if (boMap == null) {
                    log.warn("{}:{}不满足案件编号“有效期内”条件", custNum, userType);
                    continue;
                }
                periodList.add(transferSyncUser);
            }

            for (MarketingTransferSyncUser data : periodList) {
                String custNum = data.getCustNum();
                String userType = data.getUserType();
                try {
                    JSONObject reserveFieldJson = JSON.parseObject(data.getReserveField1());

                    Object cell = reserveFieldJson.get("initCustNum");
                    Object bizType = reserveFieldJson.get("bizType");
                    Object eventTime = reserveFieldJson.get("eventTime");
                    Object eventType = reserveFieldJson.get("eventType");
                    Object createTime = reserveFieldJson.get("uploadCreateTime");

                    Object amountStatus = reserveFieldJson.get("amountStatus");
                    Object highApplyStatus = reserveFieldJson.get("highApplyStatus");
                    Object auditAmountGroup = reserveFieldJson.get("auditAmountGroup");
                    Object lentAmountGroup = reserveFieldJson.get("lentAmountGroup");

                    String sb = deleteNull(custNum) +
                            deleteNull(cell) +
                            deleteNull(userType) +
                            deleteNull(createTime) +
                            deleteNull(bizType) +
                            deleteNull(eventTime) +
                            deleteNull(eventType) +
                            deleteNull(amountStatus) +
                            deleteNull(highApplyStatus) +
                            deleteNull(auditAmountGroup) +
                            (lentAmountGroup != null ? lentAmountGroup.toString() : "") +
                            "\r\n";

                    fw.append(sb);
                    totalSize = totalSize + 1;
                } catch (Exception e) {
                    log.error("{}:{}数据异常", custNum, userType);
                }
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
        log.warn("众安转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
    }

    /**
     * 删除null字符串
     */
    private static String deleteNull(String value) {
        return (StringUtils.isNotEmpty(value) ? value : "").concat(",");
    }

    /**
     * 删除null字符串
     */
    private String deleteNull(Object value) {
        return (value != null ? value.toString() : "").concat(",");
    }

    String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String concat = apiCode.concat("_").concat(yyyyMMdd).concat("_").concat(contextId.toString());
        return concat;
    }

    /**
     * 获取转化数据
     * 按照inserttime排序
     * @param date
     * @param pageIndex
     * @return
     */
    private Result<List<ZhonganMarketingBan>> getOrderTransferData(String apiCode, String date, Integer pageIndex) {
        Integer limitStart = pageIndex * 2000;
        List<ZhonganMarketingBan> zhonganMarketingBans = zhonganMarketingBanMapper.getByZKData(apiCode, date, limitStart);
        if (zhonganMarketingBans.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(zhonganMarketingBans);
    }

}
