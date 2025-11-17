package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.DynamicParameterServiceImpl;
import com.br.marketing.service.Impl.RuleRedisServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.Impl.YiXinTransferServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 宜信转化数据提取V4.0
 * 需求地址： https://c.100credit.cn/pages/viewpage.action?pageId=160816887
 * @Author yu.xia@brgroup.com
 * @Date 2024/5/29 11:29
 */
@Slf4j
@Service
public class TransferToFileByYiXinV4ServiceImpl implements ITransferToFileService {

    @Resource
    SyncConfigService syncConfigService;
    @Resource
    private TransferFileTaskMapper transferFileTaskMapper;
    @Resource
    private RuleRedisServiceImpl ruleRedisService;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private YiXinTransferServiceImpl yiXinTransferServiceImpl;
    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    DynamicParameterServiceImpl dynamicParameterService;
    @Resource
    private TransferDataValidityPeriodService validityPeriodService;
    /**
     * 执行时间
     */
    private static final String EXECUTE_TIME = "09:00:00";

    /**
     * 宜信转化数据提取V4.0 文件头
     */
    private final static String TABLE_HEAD_TRANSFER = "id,是否注册,注册时间,是否进件,进件时间,审核结果,授信金额," +
            "是否申请放款,是否放款,放款时间,放款金额,注册节点,是否申请大额提额,申请大额提额时间,申请大额提额是否通过,创建时间,红包活动," +
            "放款状态,申请提大额方式,提额成功方式,利率,已用额度,剩余额度,结算费率,推荐提大额方式,是否高质";

    final static DateTimeFormatter YYYYMMDDSHORTDFLINE = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_FORMAT);

    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        if(StringUtils.isNotEmpty(jobParameter)){
            String[] split = jobParameter.split(";");
            for(String s : split){
                String paramApiCode = s.split("#")[0];
                if(apiCode.equals(paramApiCode)  && marketingCommonConfig.getYinXinTransferV4ApiCodes().contains(paramApiCode)){
                    return s.split("#")[1];
                }
            }
        }
        return "";
    }

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode,String myParam) {
        // 宜信转化数据提取V4.0-3710012
        List<TransferFileTask> resultList = new ArrayList<>();
        //执行时间可配置
        String extractTime = StringUtils.isBlank(marketingCommonConfig.getYiXinV4TransferExecuteTime())
                ? EXECUTE_TIME : marketingCommonConfig.getYiXinV4TransferExecuteTime();
        LocalTime jobStartTime = LocalTime.parse(extractTime);
        boolean isParam = StringUtils.isNotBlank(myParam);
        // 指定日期提取时不限制时间
        if (LocalTime.now().isAfter(jobStartTime) || isParam){
            LocalDate localDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if(StringUtils.isNotEmpty(myParam)){
                localDate = LocalDate.parse(myParam, YYYYMMDDSHORTDFLINE);
            }
            String today = localDate.format(YYYYMMDDSHORTDFLINE);
            // 判断数据是否满足 T日 last=1 且数据已经处理完成（由于是异步处理，所以需要做数据处理完成判断）
            Result<Date> result = yiXinTransferServiceImpl.checkPush(apiCode, today);
            if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                // 当天的记录
                boolean isParamNotBlack = StringUtils.isNotBlank(myParam);
                String yyyyMMdd = isParamNotBlack ? myParam.replace("-", "")
                        : LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
                TransferFileTaskExample taskExample = new TransferFileTaskExample();
                // 宜信转化数据提取V4.0-3710012 fileType 8
                int fileType = 8;
                taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(yyyyMMdd)
                        .andFileTypeEqualTo(fileType);
                List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
                if (CollectionUtils.isEmpty(transferFileTasks)) {
                    log.warn("宜信转化数据提取V4.0-开始执行,apiCode ={}", apiCode);
                    Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                    String batchNumber = createBatchNumber(apiCode, transferFileContextId);
                    TransferFileTask transferFileTask = new TransferFileTask();
                    transferFileTask.setApiCode(apiCode);
                    transferFileTask.setFileType(fileType);
                    transferFileTask.setBatchNumber(batchNumber);
                    List<String> yinXinTransferRealTimeApiCodes = marketingCommonConfig.getYinXinTransferV4ApiCodes();
                    String fileName;
                    if(null != yinXinTransferRealTimeApiCodes && yinXinTransferRealTimeApiCodes.size()>0){
                        String newToday = today.replace("-", "");
                        fileName = String.format("%s_%s.csv", yinXinTransferRealTimeApiCodes.get(0), newToday);
                    }else{
                        fileName = String.format("yixinzhuanhua_all_%s.csv", today);
                    }
                    transferFileTask.setFileName(fileName);
                    transferFileTask.setFileChildDir("data_yixin");
                    transferFileTask.setTaskNumber(0);
                    transferFileTask.setStartDate(yyyyMMdd);
                    transferFileTask.setContextId(transferFileContextId);
                    transferFileTask.setCreateTime(new Date());
                    transferFileTask.setUpdateTime(transferFileTask.getCreateTime());
                    transferFileTaskMapper.insertSelective(transferFileTask);
                    resultList.add(transferFileTask);
                }
            }
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(resultList);
    }
    @Override
    public Result actionTransferToFile(TransferFileTask transferFileTask,String jobParameter){

        Result<String> result = new Result<>();
        String apiCode = transferFileTask.getApiCode();
        String childDir = StringUtils.isNotEmpty(transferFileTask.getFileChildDir()) ? (transferFileTask.getFileChildDir() + "/") : "";
        String descPath = syncConfigService.getPath()
                .concat("transferToFile/")
                .concat(childDir)
                .concat(apiCode).concat("/")
                .concat(transferFileTask.getStartDate()).concat("/");
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
            writeTransferToFile(fw, apiCode, transferFileTask, jobParameter);
        } catch (Exception ex) {
            log.error("apiCode[{}]宜信转化数据提取V4.0-转化数据写入文件异常-", apiCode, ex);
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage(ex.getMessage());
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;

    }

    /**
     * 给文件中写具体的提取数据
     * @param fw 写对象
     * @param apiCode apiCode
     * @param transferFileTask 转化数据对象
     * @param jobParameter job参数
     */
    public void writeTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask, String jobParameter) {
        // 当前时间或者job中的时间
        String requestDate = StringUtils.isBlank(jobParameter)
                ? LocalDate.now().toString() : jobParameter;
        long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        AtomicInteger totalSize = new AtomicInteger(0);
        long timeout = 5L;
        LocalDate dateToday = LocalDate.parse(requestDate, YYYYMMDDSHORTDFLINE);
        // T日站在T-1日的角度，判断该条转化数据是否在有效期内
        String requestDataMinusOne = dateToday.minusDays(1L).format(YYYYMMDDSHORTDFLINE);
        // 创建线程池
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(12, 12, 100);
        Integer pageSize = null;
        Long beginId = marketingTransferSyncUserMapper.minIdByCid(tcId, apiCode, requestDate);
        Long endId = marketingTransferSyncUserMapper.maxIdByCid(tcId, apiCode, requestDate);
        Long middleId;
        Boolean continueFlag = Boolean.TRUE;
        while (continueFlag) {
            pageSize = dynamicParameterService.getPageSize("YiXinV4Get");
            middleId = beginId + pageSize;
            if(middleId >= endId){
                middleId = endId+1;
                continueFlag = Boolean.FALSE;
            }
            List<MarketingTransferSyncUser> transferDataOriginal = marketingTransferSyncUserMapper
                    .getTransferByStartAndEndDateYiXinV4(tcId, apiCode, requestDate, beginId, middleId);
            beginId = middleId;
            List<MarketingTransferSyncUser> transferData = new ArrayList<>();
            Set<String> custNumSet = new HashSet<>();
            for(MarketingTransferSyncUser syncUser : transferDataOriginal){
                String reserveField1 = syncUser.getReserveField1();
                if(StringUtils.isNotBlank(reserveField1)){
                    JSONObject field1 = JSON.parseObject(reserveField1);
                    boolean transformType1 = "1".equals(field1.getString("transformType"));
                    if(transformType1){
                        // 需求是要获取 transformType!=1 的数据
                        continue;
                    }else{
                        // do nothing
                    }
                }else{
                    // do nothing
                }
                // 保留 transformType!=1 的数据
                String custNum = syncUser.getCustNum();
                custNumSet.add(custNum);
                transferData.add(syncUser);
            }
            threadPool.submit(() -> {
                //判断转化数据是否在有效期内
                Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum = validityPeriodService
                        .getValidityPeriodsByCustNum(custNumSet, apiCode, requestDataMinusOne);
                for (MarketingTransferSyncUser transferFilterData : transferData) {
                    String custNum = transferFilterData.getCustNum();
                    SyncUserValidityPeriodsBO boMap = validityPeriodsByCustNum.get(custNum);
                    if (boMap == null || null == boMap.getSyncUsers()) {
                        log.warn("apiCode[{}]custNum[{}]不满足yixinV4案件编号[有效期内]条件", apiCode, custNum);
                        continue;
                    }
                    String reserveField1 = transferFilterData.getReserveField1();
                    String applyLoan = null;
                    String raiseLimit = null;
                    String raiseLimitTime = null;
                    String raiseLimitResult = null;
                    String loantResult = null;
                    String raiseLimiType = null;
                    String raiseLimiSuccess = null;
                    String rate = null;
                    String usedAmount = null;
                    String availableAmount = null;
                    String settleRatio = null;
                    String recommendType = null;
                    String highQuality = null;
                    if (StringUtils.isNotBlank(reserveField1)) {
                        JSONObject jsonObject = JSON.parseObject(reserveField1);
                        applyLoan = emptyDefault(jsonObject.getString("applyLoan"));
                        raiseLimit = emptyDefault(jsonObject.getString("raiseLimit"));
                        raiseLimitTime = emptyDefault(jsonObject.getString("raiseLimitTime"));
                        raiseLimitResult = emptyDefault(jsonObject.getString("raiseLimitResult"));
                        loantResult = emptyDefault(jsonObject.getString("loanResult"));
                        raiseLimiType = emptyDefault(jsonObject.getString("raiseLimiType"));
                        raiseLimiSuccess = emptyDefault(jsonObject.getString("raiseLimiSuccess"));
                        rate = emptyDefault(jsonObject.getString("rate"));
                        usedAmount = emptyDefault(jsonObject.getString("usedAmount"));
                        availableAmount = emptyDefault(jsonObject.getString("availableAmount"));
                        settleRatio = emptyDefault(jsonObject.getString("settleRatio"));
                        recommendType = emptyDefault(jsonObject.getString("recommendType"));
                        highQuality = emptyDefault(jsonObject.getString("highQuality"));
                    }
                    StringBuilder sb = new StringBuilder();
                    try {
                        String[] y1n0Key = {"1","0"};
                        String[] y1n0Value = {"Y","N"};
                        String ifRegister = emptyDefault(transferFilterData.getIfRegister());
                        ifRegister = getMapByList(ifRegister,y1n0Key,y1n0Value);
                        String ifApply = emptyDefault(transferFilterData.getIfApply());
                        ifApply = getMapByList(ifApply,y1n0Key,y1n0Value);
                        String[] d0p1w2Key = {"0","1","2"};
                        String[] d0p1w2Value = {"DENY","PASS","WEAK_DENY"};
                        String applyResult = emptyDefault(transferFilterData.getApplyResult());
                        applyResult = getMapByList(applyResult,d0p1w2Key,d0p1w2Value);
                        applyLoan = getMapByList(applyLoan,y1n0Key,y1n0Value);
                        String ifLent = emptyDefault(transferFilterData.getIfLent());
                        ifLent = getMapByList(ifLent,y1n0Key,y1n0Value);
                        raiseLimit = getMapByList(raiseLimit,y1n0Key,y1n0Value);
                        raiseLimitResult = getMapByList(raiseLimitResult,y1n0Key,y1n0Value);
                        highQuality = getMapByList(highQuality,y1n0Key,y1n0Value);
                        sb.append(emptyDefault(custNum)).append(",")
                                .append(ifRegister).append(",")
                                .append(emptyDefault(transferFilterData.getRegisterTime())).append(",")
                                .append(ifApply).append(",")
                                .append(emptyDefault(transferFilterData.getApplyDt())).append(",")
                                .append(applyResult).append(",")
                                .append(emptyDefault(transferFilterData.getAuditAmount())).append(",")
                                .append(applyLoan).append(",")
                                .append(ifLent).append(",")
                                .append(emptyDefault(transferFilterData.getLentTime())).append(",")
                                .append(emptyDefault(transferFilterData.getLentAmount())).append(",")
                                .append(emptyDefault(transferFilterData.getUserType())).append(",")
                                .append(raiseLimit).append(",")
                                .append(raiseLimitTime).append(",")
                                .append(raiseLimitResult).append(",")
                                .append(emptyDefault(transferFilterData.getInsertTime())).append(",")
                                .append(emptyDefault(transferFilterData.getType())).append(",")
                                .append(loantResult).append(",")
                                .append(raiseLimiType).append(",")
                                .append(raiseLimiSuccess).append(",")
                                .append(rate).append(",")
                                .append(usedAmount).append(",")
                                .append(availableAmount).append(",")
                                .append(settleRatio).append(",")
                                .append(recommendType).append(",")
                                .append(highQuality);
                        sb.append("\r\n");
                        fw.append(sb.toString());
                        totalSize.incrementAndGet();
                    } catch (IOException e) {
                        log.error("[{}]yixinV4转化数据[{}]提取程序异常", apiCode, custNum, e);
                    }
                }
                try {
                    fw.flush();
                } catch (IOException e) {
                    log.error("[{}]yixinV4转化数据flush异常-", apiCode, e);
                }
            });
        }
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                if (log.isInfoEnabled()) {
                    long taskCount = threadPool.getTaskCount();
                    long completedTaskCount = threadPool.getCompletedTaskCount();
                    log.info("yixinV4转化数据提取写入文件大约总任务数：{}；大约已完成任务数：{}；大约剩余任务数：{}"
                            , taskCount, completedTaskCount, taskCount - completedTaskCount);
                }
            }
            saveUpdateTask(transferFileTask, totalSize.intValue());
            log.warn("yixinV4转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode
                    , System.currentTimeMillis() - start, totalSize);
        } catch (InterruptedException e) {
            log.error("apiCode[{}]yixinV4转化数据提取-本地文件生成失败-", apiCode, e);
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
            transferFileTaskMapper.deleteByPrimaryKey(transferFileTask.getId());
        } catch (Exception e){
            log.error("apiCode[{}]yixinV4转化数据提取-异常-", apiCode, e);
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
            transferFileTaskMapper.deleteByPrimaryKey(transferFileTask.getId());
        }
    }

    private String getMapByList(String value, String[] keyArray, String[] valueArray){
        if(keyArray.length != valueArray.length){
            log.warn("参数长度不一致value:{}-keyArray[{}]valueArray[{}]",value,keyArray,valueArray);
            return value;
        }
        try{
            for (int i = 0; i < keyArray.length; i++) {
                if(value.equals(keyArray[i])){
                    return valueArray[i];
                }
            }
        }catch (Exception e){
            log.warn("参数映射异常value:{}-keyArray[{}]valueArray[{}]--",value,keyArray,valueArray,e);
        }
        return value;
    }
    /**
     * 2023-05-10 11:20
     * 值为null时，赋值''
     */
    private String emptyDefault(String value) {
        return StringUtils.isNotBlank(value) ? value : "";
    }

    /**
     * 更新 b_transfer_file_task 转化文件任务表
     * @param transferFileTask 原对象
     * @param totalSize 任务上传数据量
     */
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
    String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String concat = apiCode.concat("_").concat(yyyyMMdd).concat("_").concat(contextId.toString());
        return concat;
    }

}
