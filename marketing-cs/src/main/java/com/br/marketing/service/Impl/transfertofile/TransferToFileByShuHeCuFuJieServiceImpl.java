package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSON;
import com.br.common.encryption.Sha256Util;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.DynamicParameterServiceImpl;
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
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Author 李广秀
 * @Date 2024/04/14 10:31
 * @Description:数禾促复借数据提取
 */
@Slf4j
@Service
public class TransferToFileByShuHeCuFuJieServiceImpl implements ITransferToFileService {

    @Autowired
    SyncConfigService syncConfigService;
    @Autowired
    DynamicParameterServiceImpl dynamicParameterService;
    @Autowired
    private TransferFileTaskMapper transferFileTaskMapper;
    @Autowired
    private TableCreateServiceImpl tableCreateService;
    @Autowired
    private RuleRedisServiceImpl ruleRedisService;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private MarketingDataValidConfigMapper marketingDataValidConfigMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private TransferDataValidityPeriodService validityPeriodService;


    private final static String TABLE_HEADER_CUFUJIE = "apicode,taskid,groupType,cust_num,cell,is_turn,is_black" +
            ",clc_usr_lst_app_sta_tim,clc_usr_lst_non_dcp_trs_tim,off_usr_lst_ord_tim_all,clc_usr_avl_lmt_lv0" +
            ",clc_usr_adt_lmt_lv0,createtime,clc_usr_lst_ord_tim_all_wizard,clc_usr_adt_lmt_fst_all,clc_usr_lst_adt_apy_tim_hvy";

    final static String EXECUTE_TIME = "06:00:00";


    final static DateTimeFormatter YYYYMMDDSHORTLINE = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_FORMAT);

    /**
     * 2024-01-04 10:50
     * 指定日期提取参数格式：
     * apiCode#yyyy-MM-dd
     * eg:7492900#2023-05-09
     */
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
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode,String myParam) {
        List<TransferFileTask> resultList = new ArrayList<>();
        String extractTime = StringUtils.isBlank(marketingCommonConfig.getShuHeCuFuJieTransferFileExecuteTime())
                ? EXECUTE_TIME : marketingCommonConfig.getShuHeCuFuJieTransferFileExecuteTime();
        LocalTime localTime = LocalTime.parse(extractTime);
        boolean isParam = StringUtils.isNotBlank(myParam);
        if (LocalTime.now().isAfter(localTime) || isParam) {
            // 指定日期提取，生成指定日期的记录，不是当天的记录
            String dateyyyymmddStr = isParam ? myParam.replace("-", "") : LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            TransferFileTaskExample taskExample = new TransferFileTaskExample();
            taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(date)
                    .andFileTypeEqualTo(1);
            List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
            if (CollectionUtils.isEmpty(transferFileTasks)) {
                log.warn("数禾促复借转化数据提取-开始执行,apiCode ={}", apiCode);
                Long transferFileContextId = ruleRedisService.getTransferFileContextId();
                String batchNumber = createBatchNumber(apiCode, transferFileContextId, dateyyyymmddStr);
                TransferFileTask transferFileTask = new TransferFileTask();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
                transferFileTask.setBatchNumber(batchNumber);
                transferFileTask.setFileName(String.format("%s_cufujie_%s.txt", apiCode, dateyyyymmddStr));
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
    public Result actionTransferToFile(TransferFileTask transferFileTask,String jobParameter) {
        log.warn("数禾促复借转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        Result<String> result = new Result<>();
        String apiCode = transferFileTask.getApiCode();
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String requestDate = StringUtils.isBlank(jobParameter) ? LocalDate.now().toString() : jobParameter;
        String descPath = syncConfigService.getPath().concat("transferToFile/").concat(apiCode).concat("/")
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
            fw.append(TABLE_HEADER_CUFUJIE);
            fw.append("\r\n");
            writeShuHeCuFuJieTransferToFile(fw, apiCode, transferFileTask, requestDate);
        } catch (Exception ex) {
            log.error("写入文件错误！",ex);
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage(ex.getMessage());
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    public void writeShuHeCuFuJieTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask, String requestDate) {
        Long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        Integer page = 0;
        Boolean mark = Boolean.TRUE;
        AtomicInteger totalSize = new AtomicInteger(0);
        long timeout = 5L;
        LocalDate localDate = LocalDate.parse(requestDate, YYYYMMDDSHORTLINE).minusDays(1L);
        String yesterday = localDate.toString();
        LocalDate startDate = localDate.minusDays(30);
        LocalDate endDate = localDate;
        LocalDate today = LocalDate.parse(requestDate, YYYYMMDDSHORTLINE);
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(100, 100, 1);
        List<MarketingDataValidConfig> validityDataByApiCode = marketingDataValidConfigMapper.getValidityDataByApiCode(apiCode, yesterday);
        if (validityDataByApiCode.size() <= 0){
            log.warn("列表可能为空");
            mark = Boolean.FALSE;
        }
        Optional<MarketingDataValidConfig> minDateConfig = validityDataByApiCode.stream()
                .min(Comparator.comparing(MarketingDataValidConfig::getValidStartDate));
        if (minDateConfig.isPresent()) {
            startDate = LocalDate.parse(minDateConfig.get().getValidStartDate(), YYYYMMDDSHORTLINE);
        } else {
            log.warn("列表为空，无法获取最小的startDate");
        }
        Optional<MarketingDataValidConfig> maxDateConfig = validityDataByApiCode.stream()
                .max(Comparator.comparing(MarketingDataValidConfig::getValidEndDate));
        if (maxDateConfig.isPresent()) {
            endDate = LocalDate.parse(maxDateConfig.get().getValidEndDate(), YYYYMMDDSHORTLINE);
            if (endDate.isBefore(today) || endDate.isEqual(today)){
                endDate = today.plusDays(1);
            }
        } else {
            log.warn("列表为空，无法获取最大的ValidEndDate");
        }

        MarketingTransferSyncUser syncUser = new MarketingTransferSyncUser();
        syncUser.settCid(tcId);
        syncUser.setApiCode(apiCode);
        Integer pageSize = dynamicParameterService.getPageSize(null);
        while (mark) {
            List<MarketingTransferSyncUser> transferData = marketingTransferSyncUserMapper
                    .getTransferByStartAndEndDate(syncUser, startDate.toString(), endDate.toString(), null, page * pageSize, pageSize);
            if (CollectionUtils.isEmpty(transferData)) {
                break;
            }
            page++;
            Set<String> set = transferData.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            //判断转化数据是否在有效期内
            Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum = validityPeriodService
                    .getValidityPeriodsByCustNum(set, apiCode, yesterday);
            threadPool.submit(() -> {
                for (MarketingTransferSyncUser transferFilterData : transferData) {
                    String custNum = transferFilterData.getCustNum();
                    String cell = "";
                    String taskId = "";
                    String isTurn = "";
                    String isBlack = "";
                    String clcUsrLstAppStaTim = "";
                    String clcUsrLstNonCcpTrsTim = "";
                    String offUsrLstOrdTimAll = "";
                    String clcUsrAvlLmtLv0 = "";
                    String clcUsrAdtLmtLv0 = "";
                    String clcUsrLstOrdTimAllWizard = "";
                    String clcUsrAdtLmtFstAll = "";
                    String clcUsrLstAdtApyTimHvy = "";

                    SyncUserValidityPeriodsBO boMap = validityPeriodsByCustNum.get(custNum);
                    if (boMap == null) {
                        log.warn("{}不满足案件编号“有效期内”条件", custNum);
                        continue;
                    }
                    if (StringUtils.isNotEmpty(transferFilterData.getReserveField1())) {
                        taskId = JSON.parseObject(transferFilterData.getReserveField1()).getString("taskId");
                        String cellLog = JSON.parseObject(transferFilterData.getReserveField1()).getString("cell");
                        cell = Sha256Util.getSHA256Encrypt(BrCipherMaker.getInstance().decode(cellLog));
                        isTurn = JSON.parseObject(transferFilterData.getReserveField1()).getString("is_turn");
                        isBlack = JSON.parseObject(transferFilterData.getReserveField1()).getString("is_black");
                        clcUsrLstAppStaTim = JSON.parseObject(transferFilterData.getReserveField1())
                                .getString("clc_usr_lst_app_sta_tim");
                        clcUsrLstNonCcpTrsTim = JSON.parseObject(transferFilterData.getReserveField1())
                                .getString("clc_usr_lst_non_dcp_trs_tim");
                        offUsrLstOrdTimAll = JSON.parseObject(transferFilterData.getReserveField1())
                                .getString("off_usr_lst_ord_tim_all");
                        clcUsrAvlLmtLv0 = JSON.parseObject(transferFilterData.getReserveField1())
                                .getString("clc_usr_avl_lmt_lv0");
                        clcUsrAdtLmtLv0 = JSON.parseObject(transferFilterData.getReserveField1())
                                .getString("clc_usr_adt_lmt_lv0");
                        clcUsrLstOrdTimAllWizard = JSON.parseObject(transferFilterData.getReserveField1())
                                .getString("clc_usr_lst_ord_tim_all_wizard");
                        clcUsrAdtLmtFstAll = JSON.parseObject(transferFilterData.getReserveField1())
                                .getString("clc_usr_adt_lmt_fst_all");
                        clcUsrLstAdtApyTimHvy = JSON.parseObject(transferFilterData.getReserveField1())
                                .getString("clc_usr_lst_adt_apy_tim_hvy");
                    }
                    taskId = StringUtils.isNotEmpty(taskId) ? taskId : "";
                    String userType = StringUtils.isNotEmpty(transferFilterData.getUserType()) ? transferFilterData.getUserType() : "";
                    custNum = StringUtils.isNotEmpty(transferFilterData.getCustNum()) ? transferFilterData.getCustNum() : "";
                    cell = StringUtils.isNotEmpty(cell) ? cell : "";
                    isTurn = StringUtils.isNotEmpty(isTurn) ? isTurn : "";
                    isBlack = StringUtils.isNotEmpty(isBlack) ? isBlack : "";
                    clcUsrLstAppStaTim = StringUtils.isNotEmpty(clcUsrLstAppStaTim) ? clcUsrLstAppStaTim : "";
                    clcUsrLstNonCcpTrsTim = StringUtils.isNotEmpty(clcUsrLstNonCcpTrsTim) ? clcUsrLstNonCcpTrsTim : "";
                    offUsrLstOrdTimAll = StringUtils.isNotEmpty(offUsrLstOrdTimAll) ? offUsrLstOrdTimAll : "";
                    clcUsrAvlLmtLv0 = StringUtils.isNotEmpty(clcUsrAvlLmtLv0) ? clcUsrAvlLmtLv0 : "";
                    clcUsrAdtLmtLv0 = StringUtils.isNotEmpty(clcUsrAdtLmtLv0) ? clcUsrAdtLmtLv0 : "";
                    String createTime = StringUtils.isNotEmpty(transferFilterData.getCreateTime())
                            ? DateUtils.format(transferFilterData.getCreateTime(), "yyyy-MM-dd HH:mm:ss") : "";
                    clcUsrLstOrdTimAllWizard = StringUtils.isNotEmpty(clcUsrLstOrdTimAllWizard) ? clcUsrLstOrdTimAllWizard : "";
                    clcUsrAdtLmtFstAll = StringUtils.isNotEmpty(clcUsrAdtLmtFstAll) ? clcUsrAdtLmtFstAll : "";
                    clcUsrLstAdtApyTimHvy = StringUtils.isNotEmpty(clcUsrLstAdtApyTimHvy) ? clcUsrLstAdtApyTimHvy : "";
                    StringBuilder sb = new StringBuilder();
                    sb.append(transferFilterData.getApiCode().concat(","))
                            .append(taskId.concat(","))
                            .append(userType.concat(","))
                            .append(custNum.concat(","))
                            .append(cell.concat(","))
                            .append(isTurn.concat(","))
                            .append(isBlack.concat(","))
                            .append(clcUsrLstAppStaTim.concat(","))
                            .append(clcUsrLstNonCcpTrsTim.concat(","))
                            .append(offUsrLstOrdTimAll.concat(","))
                            .append(clcUsrAvlLmtLv0.concat(","))
                            .append(clcUsrAdtLmtLv0.concat(","))
                            .append(createTime.concat(","))
                            .append(clcUsrLstOrdTimAllWizard.concat(","))
                            .append(clcUsrAdtLmtFstAll.concat(","))
                            .append(clcUsrLstAdtApyTimHvy)
                            .append("\r\n");
                    try {
                        fw.append(sb.toString());
                        totalSize.incrementAndGet();
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            });

        }
        threadPool.shutdown();

        try {
            while (!threadPool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                if (log.isInfoEnabled()) {
                    long taskCount = threadPool.getTaskCount();
                    long completedTaskCount = threadPool.getCompletedTaskCount();
                    log.info("数禾促复借转化数据提取写入文件大约总任务数：{}；大约已完成任务数：{}；大约剩余任务数：{}"
                            , taskCount, completedTaskCount, taskCount - completedTaskCount);
                }
            }
            saveUpdateTask(transferFileTask, totalSize.intValue());
            log.warn("数禾促复借转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}"
                    , apiCode, System.currentTimeMillis() - start, totalSize.intValue());
        } catch (InterruptedException e) {
            log.error("数禾促复借转化数据提取-本地文件生成失败！" , e);
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
            transferFileTaskMapper.deleteByPrimaryKey(transferFileTask.getId());
        }

    }

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


    private String createBatchNumber(String apiCode, Long contextId, String dateStr) {
        return apiCode.concat("_").concat(dateStr).concat("_").concat(contextId.toString());
    }
}
