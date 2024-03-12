package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSON;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
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
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author 李广秀
 * @Date 2023/08/08 10:31
 * @Description:中邮转化数据提取
 */
@Slf4j
@Service
public class TransferToFileByZhongYouServiceImpl implements ITransferToFileService {

    @Autowired
    SyncConfigService syncConfigService;
    @Autowired
    private TransferFileTaskMapper transferFileTaskMapper;
    @Autowired
    private TableCreateServiceImpl tableCreateService;
    @Autowired
    private RuleRedisServiceImpl ruleRedisService;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    final static String EXECUTE_TIME = " 10:00:00";

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[:SSS]");

    private final static DateTimeFormatter DATE_TIME_2_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    final static String ZHONGYOU_TRANSFER_FILE = "transform_";

    final static String FILE_HEADER = "taskId,custNum,userType,customName,registerTime,ifLogin,loginTime," +
        "ifApply,applyDt,applyResult,auditTime,auditAmount,ifLent,lentTime,lentAmount,unlentAmount," +
        "pushTime,loginChannel,auditRate,couponType,validityAmt,rateType,lentRate,validityRate,applyLentTime,cps," +
        "lentAmountFirst,lentTimeFirst,cpsRate,fileName,firstName,gender,cell,extend01,extend02";

    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        return "";
    }

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode, String myParam) {
        List<TransferFileTask> resultList = new ArrayList<>();
        Date now = new Date();
        //可配置
        String execute = EXECUTE_TIME;
        if (StringUtils.isNotEmpty(marketingCommonConfig.getZhongYouTransferExecuteTime())) {
            execute = " " + marketingCommonConfig.getZhongYouTransferExecuteTime();
        }
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, execute);
        if (now.after(executeTime)) {
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd).andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("中邮转化数据提取-开始执行,apiCode ={}", apiCode);
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
    public Result actionTransferToFile(TransferFileTask transferFileTask, String jobParameter) {
        log.warn("中邮转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        String apiCode = transferFileTask.getApiCode();
        String recordDate = transferFileTask.getStartDate();//yyyyMMdd
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/").concat(recordDate).concat("/");
        File writeDic = new File(descPath);
        if (!writeDic.exists()) {
            writeDic.mkdirs();
        }
        StringBuilder fileName = new StringBuilder();
        fileName.append(ZHONGYOU_TRANSFER_FILE).append(recordDate).append(".txt");
        String fileAllPath = descPath.concat(fileName.toString());
        transferFileTask.setFileName(fileName.toString());
        transferFileTask.setFilePath(descPath);
        File file = new File(fileAllPath);
        try (Writer fw = new BufferedWriter(
            new OutputStreamWriter(
                new FileOutputStream(file), "UTF-8"));) {
            fw.append(FILE_HEADER);
            fw.append("\r\n");
            writeZhongYouTransferToFile(fw, apiCode, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    public void writeZhongYouTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask) {
        Long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        LocalDate date = LocalDate.now();
        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        int totalSize = 0;
        while (mark) {
            Result<List<MarketingTransferSyncUser>> transferData = getOrderTransferData(apiCode, tcId, date.toString(), page);
            if (!ResultCode.SUCCESS.getValue().equals(transferData.getCode())) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            String pushTime = "", loginChannel = "", auditRate = "", couponType = "", validityAmt = "", rateType = "";
            String lentRate = "", validityRate = "", applyLentTime = "", cps = "", lentAmountFirst = "", lentTimeFirst = "";
            String cpsRate = "", fileName = "", firstName = "", gender = "", cell = "", taskId = "";
            String extend01 = "", extend02 = "";

            List<MarketingTransferSyncUser> data = transferData.getData();
            Set<String> custNumSet = new HashSet<>();
            data.forEach(list -> custNumSet.add(list.getCustNum()));
            // 过滤有效期内数据
            Map<String, SyncUserValidityPeriodBO> validityPeriodCustNumBatchFirstVersion = transferDataValidityPeriodService.getValidityPeriodCustNumBatchFirstVersion(custNumSet, apiCode, date);

//            Map<String, SyncUserValidityPeriodBO> validityPeriodCustNumBatchFirstVersion = transferDataValidityPeriodService.getValidityPeriodUserTypeBatchFirstVersion(data, apiCode, date);

            for (MarketingTransferSyncUser transferSyncUser : data) {
                String custNum = transferSyncUser.getCustNum();
//                String userType = transferSyncUser.getUserType();

                SyncUserValidityPeriodBO boMap = validityPeriodCustNumBatchFirstVersion.get(custNum);
//                SyncUserValidityPeriodBO boMap = validityPeriodCustNumBatchFirstVersion.get(custNum + userType);
                if (boMap == null) {
                    log.warn("{}:{}不满足案件编号“有效期内”条件", custNum);
//                    log.warn("{}:{}不满足案件编号“有效期内”条件", custNum, userType);
                    continue;
                }
                try {
                    if (transferSyncUser != null && StringUtils.isNotEmpty(transferSyncUser.getReserveField1())) {
                        try {
                            taskId = getReserFieldVal(transferSyncUser.getReserveField1(), "taskId");
                            pushTime = getReserFieldVal(transferSyncUser.getReserveField1(), "pushTime");
                            loginChannel = getReserFieldVal(transferSyncUser.getReserveField1(), "loginChannel");
                            auditRate = getReserFieldVal(transferSyncUser.getReserveField1(), "auditRate");
                            couponType = getReserFieldVal(transferSyncUser.getReserveField1(), "couponType");
                            validityAmt = getReserFieldVal(transferSyncUser.getReserveField1(), "validityAmt");
                            rateType = getReserFieldVal(transferSyncUser.getReserveField1(), "rateType");
                            lentRate = getReserFieldVal(transferSyncUser.getReserveField1(), "lentRate");
                            validityRate = getReserFieldVal(transferSyncUser.getReserveField1(), "validityRate");
                            applyLentTime = getReserFieldVal(transferSyncUser.getReserveField1(), "applyLentTime");
                            cps = getReserFieldVal(transferSyncUser.getReserveField1(), "cps");
                            lentAmountFirst = getReserFieldVal(transferSyncUser.getReserveField1(), "lentAmountFirst");
                            lentTimeFirst = getReserFieldVal(transferSyncUser.getReserveField1(), "lentTimeFirst");
                            cpsRate = getReserFieldVal(transferSyncUser.getReserveField1(), "cpsRate");
                            fileName = getReserFieldVal(transferSyncUser.getReserveField1(), "fileName");
                            firstName = getReserFieldVal(transferSyncUser.getReserveField1(), "firstName");
                            gender = getReserFieldVal(transferSyncUser.getReserveField1(), "gender");
                            cell = getReserFieldVal(transferSyncUser.getReserveField1(), "cell");
                            extend01 = getReserFieldVal(transferSyncUser.getReserveField1(), "extend01");
                            extend02 = getReserFieldVal(transferSyncUser.getReserveField1(), "extend02");
                        } catch (Exception e) {
                            log.warn("中邮转化数据提取,ReserveField1非JSON格式{}", transferSyncUser.getReserveField1());
                        }
                    }
                    StringBuilder sb = new StringBuilder();

                    sb.append(deleteNull(taskId))
                        .append(deleteNull(transferSyncUser.getCustNum()))
                        .append(deleteNull(transferSyncUser.getUserType()))
                        .append(deleteNull(transferSyncUser.getCustomName()))
                        .append(formDateStr2(transferSyncUser.getRegisterTime()))
                        .append(deleteNull(transferSyncUser.getIfLogin()))
                        .append(formDateStr2(transferSyncUser.getLoginTime()))
                        .append(deleteNull(transferSyncUser.getIfApply()))
                        .append(formDateStr2(transferSyncUser.getApplyDt()))
                        .append(deleteNull(transferSyncUser.getApplyResult()))
                        .append(formDateStr2(transferSyncUser.getAuditTime()))
                        .append(deleteNull(transferSyncUser.getAuditAmount()))
                        .append(deleteNull(transferSyncUser.getIfLent()))
                        .append(formDateStr2(transferSyncUser.getLentTime()))
                        .append(deleteNull(transferSyncUser.getLentAmount()))
                        .append(deleteNull(transferSyncUser.getUnlentAmount()))
                        .append(formDateStr(pushTime))
                        .append(deleteNull(loginChannel))
                        .append(deleteNull(auditRate))
                        .append(deleteNull(couponType))
                        .append(formDateStr2(validityAmt))
                        .append(deleteNull(rateType))
                        .append(deleteNull(lentRate))
                        .append(formDateStr2(validityRate))
                        .append(formDateStr2(applyLentTime))
                        .append(deleteNull(cps))
                        .append(deleteNull(lentAmountFirst))
                        .append(formDateStr2(lentTimeFirst))
                        .append(deleteNull(cpsRate))
                        .append(deleteNull(fileName))
                        .append(deleteNull(firstName))
                        .append(deleteNull(gender))
                        .append(deleteNull(cell))
                        .append(deleteNull(extend01))
                        .append(StringUtils.isNotEmpty(extend02) ? extend02 : "");
                    sb.append("\r\n");

                    fw.append(sb);
                    totalSize = totalSize + 1;
                } catch (Exception e) {
                    log.error("{}:{}数据异常", transferSyncUser.getCustNum(), transferSyncUser.getUserType(), e);
                }
            }
        }
        TransferFileTask updatetask = new TransferFileTask();
        updatetask.setId(transferFileTask.getId());
        updatetask.setStatus(2);
        updatetask.setFileName(transferFileTask.getFileName());
        updatetask.setFilePath(transferFileTask.getFilePath());
        updatetask.setTaskNumber(totalSize);
        updatetask.setUpdateTime(new Date());
        transferFileTaskMapper.updateByPrimaryKeySelective(updatetask);
        log.warn("中邮转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);
    }

    /**
     * 删除null字符串
     *
     * @param value
     * @return
     */
    private static String deleteNull(String value) {
        return (StringUtils.isNotEmpty(value) ? value : "").concat(",");
    }

    /**
     * 格式化时间
     *
     * @param dateTimeStr
     * @return
     */
    private String formDateStr(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr
                , DATE_TIME_FORMATTER).format(DATE_TIME_2_FORMATTER).concat(",");
        } catch (Exception e) {
            return deleteNull(dateTimeStr);
        }
    }

    private static String formDateStr2(String dateString) {
        try {
//            String[] patterns = {"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss:SSS"};
            String[] patterns = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss[:SSS]"};
            Date date = null;

            for (String pattern : patterns) {
                SimpleDateFormat formatter = new SimpleDateFormat(pattern);
                try {
                    date = formatter.parse(dateString);
                    break;
                } catch (ParseException e) {
                    // 解析失败，尝试下一个格式
                }
            }
            if (date != null) {
                SimpleDateFormat targetFormatter = new SimpleDateFormat("yyyy-MM-dd");
                return targetFormatter.format(date).concat(",");
            } else {
                // 未能解析成功，返回原始字符串或者其他默认值
                return deleteNull(dateString);
            }
        } catch (Exception e) {
            return deleteNull(dateString);
        }
    }

    private String getReserFieldVal(String reserStr, String field) {
        return StringUtils.isNotEmpty(JSON.parseObject(reserStr).getString(field)) ? JSON.parseObject(reserStr).getString(field) : "";
    }


    String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String concat = apiCode.concat("_").concat(yyyyMMdd).concat("_").concat(contextId.toString());
        return concat;
    }

    /**
     * 获取转换数据
     *
     * @param tcId
     * @param endDate
     * @param pageIndex
     * @return
     */
    private Result<List<MarketingTransferSyncUser>> getOrderTransferData(String apiCode, String tcId, String endDate, Integer pageIndex) {
        Integer limitStart = pageIndex * 2000;
        List<MarketingTransferSyncUser> transferOrderInsertTime = marketingTransferSyncUserMapper.getTransferDataByDate(apiCode, tcId, endDate, limitStart);
        if (transferOrderInsertTime.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(transferOrderInsertTime);
    }

}