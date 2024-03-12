package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
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
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


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

    @Autowired
    private MarketingDataValidConfigMapper marketingDataValidConfigMapper;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

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
            fw.append("custNum,data,extend");
            fw.append("\r\n");
            newWriteDiDiTransferToFile(fw, apiCode, transferFileTask);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new Result().setCode(ResultCode.FAIL.getValue()).setDate(ex.getMessage());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    public void newWriteDiDiTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask) throws IOException {
        Long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        LocalDate date = LocalDate.now();
        String today = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        int page = 0;
        int offset = 2000;
        int totalSize = 0;

        Boolean mark = Boolean.TRUE;
        while (mark) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList = marketingTransferSyncUserMapper.getTransferDataByRequestDataAndApiCode(tcId, apiCode, today, page * offset);
            if (CollectionUtils.isEmpty(marketingTransferSyncUserList)) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;

            for (MarketingTransferSyncUser marketingTransferSyncUser : marketingTransferSyncUserList) {
                //可配置的剔除data
                List<String> reservedField = marketingCommonConfig.getResverfiled1Data();
//                ArrayList<String> reservedField = new ArrayList<>();
//                reservedField.add("0");
                String data = "" , extend = "";

                if(StringUtils.isNotBlank(marketingTransferSyncUser.getReserveField1())) {
                    JSONObject jsonObject = JSON.parseObject(marketingTransferSyncUser.getReserveField1());
                    data = jsonObject.getString("data");
                    if(StringUtils.isEmpty(data)){
                        String msg = AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_DIDI.getCode(), "滴滴联合建模接口返回data为空，custNum="+marketingTransferSyncUser.getCustNum());
                        log.warn(msg);
                    }
                    if (StringUtils.isNotEmpty(reservedField)){
                        if (reservedField.contains(data)) {
                            continue;
                        }
                    } else {
                        if ("00000000".equals(data) || "0".equals(data)) {
                            continue;
                        }
                    }

                    extend = jsonObject.getString("extend");
                }

                StringBuilder sb = new StringBuilder();
                sb.append(marketingTransferSyncUser.getCustNum().concat(","));
                sb.append((StringUtils.isNotEmpty(data) ? data : "").concat(","));
                sb.append((StringUtils.isNotEmpty(extend) ? extend : ""));
                sb.append("\r\n");
                fw.append(sb.toString());
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
        log.warn("滴滴转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);

    }

    private void writeDiDiTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask) throws IOException {
        Long start = System.currentTimeMillis();
        String tcId = tableCreateService.getTcId(apiCode);
        int totalSize = 0;
        List<MarketingDataValidConfig> configList = findConfigByBetweenDate(apiCode, LocalDate.now().minusDays(1).toString());
        List<String> appletDateList = configList.stream().map(marketingDataValidConfig -> marketingDataValidConfig.getAppletDate()).collect(Collectors.toList());
        Set<String> CustNumSets = new HashSet<>();
        for (String appleDate : appletDateList) {
            Integer page = 0;
            Boolean mark = Boolean.TRUE;
            while (mark) {
                Result<List<MarketingSyncUser>> syncUserOrderData = getSyncUserOrderData(apiCode, appleDate, page);
                if (!ResultCode.SUCCESS.getValue().equals(syncUserOrderData.getCode())) {
                    mark = Boolean.FALSE;
                    continue;
                }
                page++;
                List<MarketingSyncUser> marketingSyncUsers = syncUserOrderData.getData();
                //custNum去重
                marketingSyncUsers.removeIf(marketingSyncUser -> !CustNumSets.add(marketingSyncUser.getCustNum()));
                //获取最新的转化数据
                if(CollectionUtils.isEmpty(marketingSyncUsers)){
                    continue;
                }
                List<String> CustNums = marketingSyncUsers.stream().map(MarketingSyncUser::getCustNum).collect(Collectors.toList());
                List<MarketingTransferSyncUser> marketingTransferSyncUserList = marketingTransferSyncUserMapper.getTransferByCustNumOrderDatatikv_(tcId, CustNums);
                Map<String, List<MarketingTransferSyncUser>> transferDataMap = marketingTransferSyncUserList.stream().collect(
                        Collectors.groupingBy(MarketingTransferSyncUser::getCustNum));
                for (MarketingSyncUser marketingSyncUser : marketingSyncUsers) {
                    String custNum = marketingSyncUser.getCustNum();
                    String data = "", extend = "";
                    if (transferDataMap.containsKey(marketingSyncUser.getCustNum())) {
                        MarketingTransferSyncUser transferSyncUser = transferDataMap.get(marketingSyncUser.getCustNum()).stream().filter(marketingTransferSyncUser -> marketingTransferSyncUser.getCustNum().equals(custNum)).findAny().orElse(null);
                        if(StringUtils.isNotBlank(transferSyncUser.getReserveField1())) {
                            JSONObject jsonObject = JSON.parseObject(transferSyncUser.getReserveField1());
                            data = jsonObject.getString("data");
                            if(StringUtils.isEmpty(data)){
                                String msg = AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_DIDI.getCode(), "滴滴联合建模接口返回data为空，custNum="+custNum);
                                log.warn(msg);
                            }
                            if ("00000".equals(data) || "0".equals(data)) {
                                continue;
                            }
                            extend = jsonObject.getString("extend");
                        }
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append(marketingSyncUser.getCustNum().concat(","));
                    sb.append((StringUtils.isNotEmpty(data) ? data : "").concat(","));
                    sb.append((StringUtils.isNotEmpty(extend) ? extend : ""));
                    sb.append("\r\n");
                    fw.append(sb.toString());
                    totalSize++;
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
        log.warn("滴滴转化数据提取-本地文件生成成功,apiCode = {},time = {}ms,total = {}", apiCode, System.currentTimeMillis() - start, totalSize);

    }


    String createBatchNumber(String apiCode, Long contextId) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String concat = apiCode.concat("_").concat(yyyyMMdd).concat("_").concat(contextId.toString());
        return concat;
    }


    /**
     * apicode有效期配置
     * userType = 1
     */
    private List<MarketingDataValidConfig> findConfigByBetweenDate(String apiCode, String date) {
        MarketingDataValidConfigExample example = new MarketingDataValidConfigExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andUserTypeEqualTo("1").andValidStartDateLessThanOrEqualTo(date)
                .andValidEndDateGreaterThanOrEqualTo(date).andIsDelEqualTo(1);
        example.setOrderByClause("applet_date desc");
        return marketingDataValidConfigMapper.selectByExample(example);
    }

    /**
     * 获取上传数据
     * 按照createtime排序
     *
     * @param pageIndex
     * @return
     */
    private Result<List<MarketingSyncUser>> getSyncUserOrderData(String apiCode, String requestDate, Integer pageIndex) {
        Integer limitStart = pageIndex * 2000;
        List<MarketingSyncUser> marketingSyncUserList = marketingSyncUserMapper.getSyncUserByAppletDatePage(apiCode, requestDate, "1", limitStart);
        if (marketingSyncUserList.size() <= 0) {
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(marketingSyncUserList);
    }
}
