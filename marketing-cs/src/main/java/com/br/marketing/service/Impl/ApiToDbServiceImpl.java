package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.common.TaskExecCommonField;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.TaskTypeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.TaskUserDataConditionDTO;
import com.br.marketing.entity.*;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.es.util.UuidUtils;
import com.br.marketing.mapper.*;
import com.br.marketing.service.*;
import com.br.marketing.vo.BaseHead;
import com.br.marketing.vo.BaseHeadConfigVO;
import com.br.marketing.vo.CustomerScoreRuleVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Service
public class ApiToDbServiceImpl implements IApiToDbService {

    private static final Logger log = LoggerFactory.getLogger(ApiToDbServiceImpl.class);

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;
    @Autowired
    SyncConfigService syncConfigService;
    @Resource
    MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    MarketingSyncInfoMapper syncInfoMapper;

    @Resource
    MarketingUserMapper marketingUserMapper;

    @Resource
    MarketingTaskMapper marketingTaskMapper;

    @Resource
    MarketingTaskExtendMapper marketingTaskExtendMapper;

    @Autowired
    RedisChgService redisChgService;

    @Resource
    TaskBatchnumberPreMapper taskBatchnumberPreMapper;

    @Resource
    LoanFileMapper loanFileMapper;

    @Resource
    TaskStatusMapper taskStatusMapper;

    @Resource
    TaskStatusDistributeMapper taskStatusDistributeMapper;

    private final static String redisElasticJobKey = "elasticjob:contextid";

    private final static String redisBatchNumberKey = "batchnumber:pre";

    @Autowired
    IProductResultSimpleService iProductResultSimpleService;

    @Autowired
    IRuleConfigService iRuleConfigService;

    @Autowired
    SoleStrategyService soleStrategyService;

    final static DateTimeFormatter ymdhms = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    final static DateTimeFormatter ymd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Autowired
    MarketingSepService marketingSepService;

    @Override
    public Long getTaskContextId() {
        return redisChgService.incr(redisElasticJobKey);
    }

    @Resource
    MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    FastFileRelationMapper fastFileRelationMapper;

    @Resource
    FastTaskRuleMapper fastTaskRuleMapper;

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Autowired
    MarketingHistoryEsServiceImpl marketingHistoryEsService;

    @Override
    public Result pushToDb(String apiCode) {
        return pushToDb(apiCode, 0, null);
    }

    @Override
    public Result pushToDb(String code, int shardingTotalCount, List<Integer> shardingItems) {
        /**
         * ->遍历客户表->遍历客户规则->根据用户规则的时间范围判断是否有用户上传数据
         *  ->1如果上传则跳出该规则
         *  ->2如果上传的数据状态都结束->根据时间范围获取所有的数据->遍历数据->根据去重规则去重
         *      ->2.1如果数据重复则跳出
         *      ->2.3如果数据去重失败则跳出
         *      ->2.2如果数据未重复->匹配当前的跑分规则
         *          ->2.2.1如果匹配则入表，不匹配则跳出
         */
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        MarketingCustomerExample.Criteria criteria = customerExample.createCriteria();
        if (StringUtils.isNotBlank(code)) {
            criteria.andApiCodeEqualTo(code).andStatusEqualTo(Byte.valueOf("1"));
        } else {
            criteria.andStatusEqualTo(Byte.valueOf("1"));
        }
        List<MarketingCustomer> marketingCustomers;
        if (shardingTotalCount < 2 && (shardingItems == null || shardingItems.size() < 2)) {
            marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        } else {
            marketingCustomers = marketingCustomerMapper.selectByExampleAndShard(customerExample
                    , shardingTotalCount, shardingItems);
        }
        for (MarketingCustomer marketingCustomer : marketingCustomers) {
            String apiCode = marketingCustomer.getApiCode();
            tableCreateService.createMarketingSyncUserTable(apiCode);
            tableCreateService.createMarketingUserTable(apiCode);
            Result<List<CustomerScoreRuleVO>> scoreConfig = iRuleConfigService.getScoreConfig(apiCode);
            if (!ResultCode.SUCCESS.getValue().equals(scoreConfig.getCode())) {
                continue;
            }
            List<CustomerScoreRuleVO> scoreConfigList = scoreConfig.getData();
            outrule:
            for (CustomerScoreRuleVO customerScoreRuleVO : scoreConfigList) {
                Boolean isToFile = customerScoreRuleVO.getTaskType().compareTo(Integer.valueOf(1)) == 0 ? Boolean.TRUE : Boolean.FALSE;
                if (TaskExecCommonField.isBuildTaskJob.equals(2)) {
                    TaskExecCommonField.isBuildTaskJob = 3;
                    break outrule;
                }
                //region 遍历规则

                //region 时间处理
                String startTime = customerScoreRuleVO.getStartTime();
                LocalDateTime nowTime = LocalDateTime.now();
                LocalDate nowData = LocalDate.now();
                String validTimeStr = nowData.format(ymd).concat(" " + startTime + ":00");
                LocalDateTime validTime = LocalDateTime.parse(validTimeStr, ymdhms);
                //筛选数据范围时间
                String sTimeStr = "", eTimeStr = "";
                Date sTime = null, eTime = null;
                //任务的开始时间和结束时间
                String taskStart = "", taskEnd = "";
                if (nowTime.compareTo(validTime) > 0) {
                    if ("00:00".equals(startTime)) {
                        sTimeStr = nowData.minusDays(1L).format(ymd).concat(" 00:00:00");
                        eTimeStr = validTime.format(ymdhms);
                        taskStart = LocalDate.now().format(ymd);
                        taskEnd = LocalDate.now().plusDays(1L).format(ymd);
                    } else {
                        sTimeStr = nowData.format(ymd).concat(" 00:00:00");
                        eTimeStr = validTime.format(ymdhms);
                        taskStart = LocalDate.now().format(ymd);
                        taskEnd = LocalDate.now().plusDays(1L).format(ymd);
                    }
                } else {
                    sTimeStr = nowData.minusDays(1L).format(ymd).concat(" 00:00:00");
                    eTimeStr = validTime.minusDays(1L).format(ymdhms);
                    taskStart = LocalDate.now().minusDays(1L).format(ymd);
                    taskEnd = LocalDate.now().format(ymd);
                }
                try {
                    sTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sTimeStr);
                    eTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(eTimeStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Date ruleOpenTime = customerScoreRuleVO.getUpdateTime();
                if (ruleOpenTime == null) {
                    ruleOpenTime = customerScoreRuleVO.getCreateTime();
                }
                String ruleOpenDay = new SimpleDateFormat("yyyy-MM-dd").format(ruleOpenTime);
                String nowDay = LocalDate.now().format(ymd);
                // 规则启用日期和生成任务日期相同 需要比较 生效时间是小于等于规则开启时间 认为历史的任务不予生成
                if (ruleOpenDay.equals(nowDay) && eTime.compareTo(ruleOpenTime) <= 0) {
                    continue;
                }
                //endregion

                //region 条件解析
                Result<String> conditionRes = soleStrategyService.analysisCondition(customerScoreRuleVO.getConditionInfo());
                if (!ResultCode.SUCCESS.getValue().equals(conditionRes.getCode())) {
                    continue;
                }

                MarketingSyncInfoExample syncInfoIngExample = new MarketingSyncInfoExample();
                syncInfoIngExample.createCriteria()
                        .andApiCodeEqualTo(apiCode)
                        .andCreateTimeGreaterThanOrEqualTo(sTime)
                        .andCreateTimeLessThan(eTime)
                        .andStatusEqualTo(1)
                        .andIsUploadEqualTo(1);
                int isUploadCount = syncInfoMapper.countByExample(syncInfoIngExample);
                if (isUploadCount > 0) {
                    continue;
                }
                String number = "";
                Long minId = syncInfoMapper
                        .getMinIdByRuleScore(apiCode, sTimeStr, eTimeStr, conditionRes.getData());
                if (minId != null && minId > 0) {
                    String time = LocalDateTime.parse(eTimeStr, ymdhms).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    Result<String> batchNumberRes = buildBatchNumber(apiCode
                            , customerScoreRuleVO.getId().toString(), customerScoreRuleVO.getRuleNameShort()
                            , time, null);
                    if (!ResultCode.SUCCESS.getValue().equals(batchNumberRes.getCode())) {
                        continue;
                    }
                    number = batchNumberRes.getData();
                } else {
                    continue;
                }

                MarketingTask hasTask = marketingTaskMapper.getByBatchNumber(number);
                if (hasTask != null) {
                    continue;
                }

                BaseHeadConfigVO baseHeadConfigVO = JSON.parseObject(customerScoreRuleVO.getBaseInfo()
                        , new TypeReference<BaseHeadConfigVO>() {
                        }.getType());
                //endregion

                //region 处理marketingUser
                Long maxId = syncInfoMapper
                        .getMaxIdByRuleScore(apiCode, sTimeStr, eTimeStr, conditionRes.getData());
                ExecutorService threadPool = BrExecutors.getThreadPool(20, 50);
                boolean execMark = true;
                int currentPage = 1;
                Integer taskNum = 0;
                String separator = marketingSepService.querySepByApiCode(apiCode);
                String filePath = syncConfigService.getPath().concat(Constants.monitorTypeMap.get(String.valueOf(customerScoreRuleVO.getExecType()))).concat("/").concat(apiCode).concat("/")
                        .concat(number).concat("/").concat(new SimpleDateFormat("yyyy-MM-dd").format(new Date())).concat("/").concat("0");
                StraHisFile file = null;
                if (isToFile) {
                    file = saveTask(apiCode, number, filePath, null, customerScoreRuleVO, baseHeadConfigVO, null, null, isToFile, taskStart, taskEnd);
                }

                while (execMark && TaskExecCommonField.isBuildTaskJob.equals(1)) {
                    String batchNumber = number;
                    Long nowMaxId = minId + 5000;

                    if (nowMaxId >= maxId) {
                        execMark = false;
                    }
                    List<MarketingSyncUser> syncUserByRuleScore = syncInfoMapper
                            .getSyncUserByRuleScore(apiCode, sTimeStr, eTimeStr, minId, nowMaxId, conditionRes.getData());
                    minId = nowMaxId + 1;
                    taskNum += syncUserByRuleScore.size();
                    if (syncUserByRuleScore.size() <= 0) {
                        continue;
                    }
                    if (isToFile) {
                        dataToFile(syncUserByRuleScore, baseHeadConfigVO, threadPool, filePath, currentPage, separator, file);
                    } else {
                        dataToDB(syncUserByRuleScore, apiCode, batchNumber, baseHeadConfigVO, threadPool);
                    }
                    currentPage++;
                }
                threadPool.shutdown();
                boolean isContiue = true;
                while (isContiue) {
                    if (threadPool.isTerminated()) {
                        isContiue = false;
                    } else {
                        try {
                            Thread.sleep(3000L);
                        } catch (Exception e) {
                            log.error("Thread.sleep error", e);
                        }
                    }
                }
                //endregion

                if (TaskExecCommonField.isBuildTaskJob.equals(2)) {
                    TaskExecCommonField.isBuildTaskJob = 3;
                    StringBuilder content = new StringBuilder();
                    content.append("停止生成的任务批次号：".concat(number).concat("\r\n"));
                    alarmClient.sendAlarm(content.toString(), "api人员数据生成任务",
                            AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
                    break outrule;
                }

                //region 处理task
//                int i = marketingUserMapper.countByPreUser(apiCode, taskId, strategyOfGroupDTO.getGroupType(),preDate);
                int actNum = isToFile ? taskNum : marketingUserMapper.countBySureUser(apiCode, number);
                if (actNum > 0) {
                    if (!isToFile) {
                        AtomicInteger num = new AtomicInteger(taskNum);
                        saveTask(apiCode, number, filePath, null, customerScoreRuleVO, baseHeadConfigVO, actNum, num, isToFile, taskStart, taskEnd);
                    } else {
                        file.setActualNum(actNum);
                        updateFile(file);
                    }
                    try {
                        StringBuilder content = new StringBuilder();
                        content.append("apiCode：".concat(apiCode).concat("\r\n"))
                                .append("ruleId：".concat(customerScoreRuleVO.getId().toString()).concat("\r\n"))
                                .append("ruleName：".concat(customerScoreRuleVO.getRuleName()).concat("\r\n"))
                                .append("time：".concat(eTimeStr).concat("\r\n"))
                                .append("batchNumber：".concat(number).concat("\r\n"))
                                .append(String.format("预计数量: %d,入库数量：%d", taskNum, actNum));
                        alarmClient.sendAlarm(content.toString(), "api人员数据生成任务", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                }
                //endregion

                //endregion
            }

        }

        faskRuleToDb(marketingCustomers);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    /**
     * 写入数据库
     *
     * @param syncUserByRuleScore
     * @param apiCode
     * @param batchNumber
     * @param baseHeadConfigVO
     * @param threadPool
     */
    private void dataToDB(List<MarketingSyncUser> syncUserByRuleScore, String apiCode, String batchNumber, BaseHeadConfigVO baseHeadConfigVO, ExecutorService threadPool) {
        dataToDB(syncUserByRuleScore, apiCode, batchNumber, baseHeadConfigVO, threadPool, null, null, null);
    }

    /**
     * 线程内进行数据筛选
     *
     * @param syncUserByRuleScore
     * @param apiCode
     * @param batchNumber
     * @param baseHeadConfigVO
     * @param threadPool
     * @param userTypes
     * @param appletDate
     */
    private void dataToDB(List<MarketingSyncUser> syncUserByRuleScore, String apiCode, String batchNumber, BaseHeadConfigVO baseHeadConfigVO, ExecutorService threadPool, List<String> userTypes, String appletDate, AtomicInteger preNum) {
        threadPool.submit(() -> {
            try {
                if (!TaskExecCommonField.isBuildTaskJob.equals(1)) {
                    return;
                }
                Integer len = 0;
                for (MarketingSyncUser syncUser : syncUserByRuleScore) {
                    if (userTypes != null && appletDate != null) {
                        if (!(userTypes.contains(syncUser.getUserType()) && appletDate.equals(syncUser.getAppletDate()))) {
                            continue;
                        }
                    }
                    JSONObject extendJson = getCustomerHead(syncUser, baseHeadConfigVO);
                    String s = LocalDateTime.now().format(ymdhms);
                    // api_code,batch_number,cus_num,cell,create_time,update_time,decodeFailType,status,extend_json
                    String dataSql = String.format("('%s','%s','%s','%s','%s','%s','%s','%s','%s',%d,'%s','%s','%s')"
                            , apiCode, batchNumber, syncUser.getCustNum()
                            , syncUser.getCell()
                            , StringUtils.isBlank(syncUser.getIdCard()) ? "" : syncUser.getIdCard()
                            , StringUtils.isBlank(syncUser.getName()) ? "" : syncUser.getName(), s, s
                            , syncUser.getFailType() == null ? "" : syncUser.getFailType()
                            , syncUser.getStatus()
                            , JSON.toJSONString(extendJson)
                            , syncUser.getCusBatch()
                            , syncUser.getUserType());
                    marketingUserMapper.insertByRequestId(apiCode, dataSql);
                    marketingSyncUserMapper.updateSyncUserStatus(apiCode, syncUser.getId(), 2);
                    len++;
                }
                if (preNum != null) {
                    preNum.getAndAdd(len);
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        });
    }

    /**
     * 写入文件
     *
     * @param syncUserByRuleScore
     * @param baseHeadConfigVO
     * @param threadPool
     * @param filePath
     * @param currentPage
     * @param sep
     */
    private void dataToFile(List<MarketingSyncUser> syncUserByRuleScore, BaseHeadConfigVO baseHeadConfigVO, ExecutorService threadPool, String filePath, int currentPage, String sep, StraHisFile file) {
        dataToFile(syncUserByRuleScore, baseHeadConfigVO, threadPool, filePath, currentPage, sep, file, null, null, null);
    }

    /**
     * 线程内进行数据筛选
     *
     * @param syncUserByRuleScore
     * @param baseHeadConfigVO
     * @param threadPool
     * @param filePath
     * @param currentPage
     * @param sep
     * @param userTypes
     * @param appletDate
     */
    private void dataToFile(List<MarketingSyncUser> syncUserByRuleScore, BaseHeadConfigVO baseHeadConfigVO, ExecutorService threadPool, String filePath, int currentPage, String sep, StraHisFile file, List<String> userTypes, String appletDate, AtomicInteger preNum) {

        File writeName = new File(filePath);
        if (!writeName.exists()) {
            writeName.mkdirs();
        }
        threadPool.submit(() -> {
            File file1 = new File(filePath + "/" + currentPage + ".txt");
            try (Writer fw = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(file1), "UTF-8"));) {
                Integer len = 0;
                for (MarketingSyncUser syncUser : syncUserByRuleScore) {
                    if (userTypes != null && appletDate != null) {
                        if (!(userTypes.contains(syncUser.getUserType()) && appletDate.equals(syncUser.getAppletDate()))) {
                            continue;
                        }
                    }
                    //region 用户上传表头配置处理
                    if (baseHeadConfigVO != null) {
                        JSONObject extendJson = getCustomerHead(syncUser, baseHeadConfigVO);
                        StringBuilder sb = new StringBuilder();
                        MarketingHistory mh = new MarketingHistory();
                        List<MarketingCondition> conditionList = new ArrayList<>();
                        for (String s : baseHeadConfigVO.getShowBaseHead()) {
                            String ss = extendJson.getString(s);
                            if (StringUtils.isNotBlank(ss)) {
                                sb.append(ss).append(sep);
                            } else {
                                sb.append(sep);
                            }
                            buildEs(s, ss, mh, conditionList,extendJson);
                        }
                        fw.append(sb).append("\r\n");
                        insertEs(file, mh, conditionList);
                        len++;
                    }
                }
                if (preNum != null) {
                    preNum.getAndAdd(len);
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        });
    }

    private JSONObject getCustomerHead(MarketingSyncUser syncUser, BaseHeadConfigVO baseHeadConfigVO) {
        if (baseHeadConfigVO == null) {
            return new JSONObject();
        }
        //region 用户上传表头配置处理
        JSONObject extendJson = new JSONObject();
        Integer ia = 0, ib = 1, ic = 2;
        if (baseHeadConfigVO != null) {
            JSONObject icData = null;
            if (StringUtils.isNotBlank(syncUser.getReserveField1())) {
                try {
                    icData = JSON.parseObject(syncUser.getReserveField1());
                } catch (Exception ex) {
                    log.error("用户上传数据非法的扩展信息：apiCode:{},id:{}"
                            , syncUser.getApiCode(), syncUser.getId());
                }
            }
            for (BaseHead head : baseHeadConfigVO.getBaseHead()) {
                String str = "";
                if (ia.equals(head.getType())) {
                    str = "";
                } else if (ib.equals(head.getType())) {
                    switch (head.getName().toLowerCase()) {
                        case "apicode":
                            str = syncUser.getApiCode();
                            break;
                        case "cusbatch":
                            str = syncUser.getCusBatch();
                            break;
                        case "taskid":
                            str = syncUser.getCusBatch();
                            break;
                        case "requestbatch":
                            str = syncUser.getRequestBatch();
                            break;
                        case "requestid":
                            str = syncUser.getRequestBatch();
                            break;
                        case "custnum":
                            str = syncUser.getCustNum();
                            break;
                        case "idcard":
                            str = StringUtils.isBlank(syncUser.getFailType())
                                    && StringUtils.isNotBlank(syncUser.getIdCard())
                                    ? DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance()
                                    .decode(syncUser.getIdCard()).getBytes())
                                    : syncUser.getIdCard();
                            break;
                        case "id":
                            str = StringUtils.isBlank(syncUser.getFailType())
                                    && StringUtils.isNotBlank(syncUser.getIdCard())
                                    ? DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance()
                                    .decode(syncUser.getIdCard()).getBytes())
                                    : syncUser.getIdCard();
                            break;
                        case "cell":
                            str = StringUtils.isBlank(syncUser.getFailType())
                                    && StringUtils.isNotBlank(syncUser.getCell())
                                    ? DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance()
                                    .decode(syncUser.getCell()).getBytes())
                                    : syncUser.getCell();
                            extendJson.put("cellSource", syncUser.getCell());
                            break;
                        case "name":
                            str = StringUtils.isBlank(syncUser.getFailType())
                                    && StringUtils.isNotBlank(syncUser.getName())
                                    ? DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance()
                                    .decode(syncUser.getName()).getBytes())
                                    : syncUser.getName();
                            break;
                        case "grouptype":
                            str = syncUser.getGroupType();
                            break;
                        case "usertype":
                            str = syncUser.getUserType();
                            break;
                        case "registerdate":
                            str = syncUser.getRegisterDate();
                            break;
                        /* 2021-8-18 14:41:12
                         * 回传文件结果表头新增字段：
                         * createTime 基础字段
                         */
                        case "createtime": // 客户数据上传日期（精确到日）
                            str = syncUser.getAppletDate();
                            break;
                        default:
                            str = "";
                    }
                } else if (ic.equals(head.getType())) {
                    if (icData != null) {
                        str = icData.getString(head.getName());
                    }
                } else {
                    str = "";
                }
                extendJson.put(head.getName(), str);
            }
            ;
        }
        //endregion
        return extendJson;
    }

    private LoanFile saveStraHisFile(MarketingTask task, CustomerScoreRuleVO customerScoreRuleVO, String uploadTime, String filePath) {

        LoanFile blf = new LoanFile();
        blf.setApiCode(task.getApiCode());
        blf.setFilePath(filePath.substring(0, filePath.lastIndexOf("/")));
        blf.setStatus(1);
        if (1 == task.getMonitorType()) {
            blf.setType(2);
        } else if (4 == task.getMonitorType()) {
            blf.setType(1);
        }
        blf.setIndexNum(1);
        blf.setBatchNumber(task.getBatchNumber());
        blf.setExpectedNum(task.getActualNumber());
        if (customerScoreRuleVO != null) {
            blf.setShowTitle(createShowTitle(task, customerScoreRuleVO, uploadTime));
        }
        loanFileMapper.insertFile(blf);
        return blf;
    }

    private void saveTaskStatusDistribute(MarketingTask task, LoanFile loanFile) {
        TaskStatusDistribute statusDistribute = new TaskStatusDistribute();
        statusDistribute.setFileId(Long.valueOf(loanFile.getId()));
        statusDistribute.setApiCode(task.getApiCode());
        statusDistribute.setBatchNumber(task.getBatchNumber());
        statusDistribute.setDistributeIndex(0);
        statusDistribute.setActualNum(task.getActualNumber().longValue());
        Date date = new Date();
        statusDistribute.setCreateTime(date);
        statusDistribute.setUpdateTime(date);
        taskStatusDistributeMapper.insertSelective(statusDistribute);
    }

    private void updateFile(StraHisFile straHisFile) {

        TaskStatusDistribute taskStatusDistribute = new TaskStatusDistribute();
        taskStatusDistribute.setStatus(2);
        taskStatusDistribute.setActualNum(Long.valueOf(straHisFile.getActualNum()));
        TaskStatusDistributeExample example = new TaskStatusDistributeExample();
        example.createCriteria().andFileIdEqualTo(straHisFile.getId());
        taskStatusDistributeMapper.updateByExampleSelective(taskStatusDistribute, example);

        StraHisFile file = new StraHisFile();
        file.setId(straHisFile.getId());
        file.setStatus(1);
        file.setActualNum(straHisFile.getActualNum());
        straHisFileMapper.updateByPrimaryKeySelective(file);

        MarketingTask updateTask = new MarketingTask();
        updateTask.setActualNumber(straHisFile.getActualNum());
        updateTask.setBatchNumber(straHisFile.getBatchNumber());
        updateTask.setApiCode(straHisFile.getApiCode());
        marketingTaskMapper.modifyTaskActualNum(updateTask);
    }

    private void saveTaskStatus(MarketingTask task, LoanFile loanFile) {
        TaskStatus bts = new TaskStatus();
        if (1 == task.getMonitorType()) {
            bts.setOnceStatus(1);
        } else if (4 == task.getMonitorType()) {
            bts.setAllStatus(1);
        }
        bts.setApiCode(task.getApiCode());
        bts.setBatchNumber(task.getBatchNumber());
        bts.setFileId(loanFile.getId());
        taskStatusMapper.insertTaskStatus(bts);
    }

    private String createShowTitle(MarketingTask task, CustomerScoreRuleVO customerScoreRuleVO, String uploadTime) {
        return task.getApiCode().concat("_")
                .concat(customerScoreRuleVO.getId().toString().concat("_"))
                .concat(customerScoreRuleVO.getRuleNameShort().concat("_"))
                .concat(uploadTime.concat("_"))
                .concat(new SimpleDateFormat("yyyyMMdd").format(new Date()));
    }

    @Override
    public Result pushToDb(String code, HashMap<String, String> params) {
        /**
         * ->遍历客户表->遍历客户规则->根据用户规则的时间范围判断是否有用户上传数据
         *  ->1如果上传则跳出该规则
         *  ->2如果上传的数据状态都结束->根据时间范围获取所有的数据->遍历数据->根据去重规则去重
         *      ->2.1如果数据重复则跳出
         *      ->2.3如果数据去重失败则跳出
         *      ->2.2如果数据未重复->匹配当前的跑分规则
         *          ->2.2.1如果匹配则入表，不匹配则跳出
         */
        String userTypeJob = params.get("userType");
        String startTimeJob = params.get("startTime");
        String endTimeJob = params.get("endTime");
        String scoreBeginData = params.get("scoreDate");
        LocalDate sScoreDate = LocalDate.parse(scoreBeginData, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate eScoreDate = sScoreDate.plusDays(1L);
        String scoreEndData = eScoreDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        MarketingCustomerExample.Criteria criteria = customerExample.createCriteria();
        if (StringUtils.isNotBlank(code)) {
            criteria.andApiCodeEqualTo(code).andStatusEqualTo(Byte.valueOf("1"));
        } else {
            criteria.andStatusEqualTo(Byte.valueOf("1"));
        }
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        for (MarketingCustomer marketingCustomer : marketingCustomers) {
            String apiCode = marketingCustomer.getApiCode();
            tableCreateService.createMarketingSyncUserTable(apiCode);
            tableCreateService.createMarketingUserTable(apiCode);
            Result<List<CustomerScoreRuleVO>> scoreConfig = iRuleConfigService.getScoreConfig(apiCode);
            if (!ResultCode.SUCCESS.getValue().equals(scoreConfig.getCode())) {
                continue;
            }
            List<CustomerScoreRuleVO> scoreConfigList = scoreConfig.getData();
            if (StringUtils.isNotEmpty(userTypeJob)) {
                scoreConfigList = soleStrategyService.matchScoreRule(scoreConfigList, userTypeJob);
            }
            for (CustomerScoreRuleVO customerScoreRuleVO : scoreConfigList) {
                //region 遍历规则

                //region 条件解析
                Result<String> conditionRes = soleStrategyService.analysisCondition(customerScoreRuleVO.getConditionInfo());
                if (!ResultCode.SUCCESS.getValue().equals(conditionRes.getCode())) {
                    continue;
                }
                String number = "";
                int taskNum = syncInfoMapper.countByPreUserWithRule(apiCode, startTimeJob, endTimeJob, conditionRes.getData());
                if (taskNum > 0) {
                    String time = LocalDateTime.parse(endTimeJob, ymdhms).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    Result<String> batchNumberRes = buildBatchNumber(apiCode
                            , customerScoreRuleVO.getId().toString(), customerScoreRuleVO.getRuleNameShort()
                            , time, 1);
                    if (!ResultCode.SUCCESS.getValue().equals(batchNumberRes.getCode())) {
                        continue;
                    }
                    number = batchNumberRes.getData();
                } else {
                    continue;
                }
                MarketingTask hasTask = marketingTaskMapper.getByBatchNumber(number);
                if (hasTask != null) {
                    continue;
                }
                BaseHeadConfigVO baseHeadConfigVO = JSON.parseObject(customerScoreRuleVO.getBaseInfo()
                        , new TypeReference<BaseHeadConfigVO>() {
                        }.getType());
                //endregion

                //region 处理marketingUser
                Long minId = syncInfoMapper
                        .getMinIdByRuleScore(apiCode, startTimeJob, endTimeJob, conditionRes.getData());
                Long maxId = syncInfoMapper
                        .getMaxIdByRuleScore(apiCode, startTimeJob, endTimeJob, conditionRes.getData());
                ExecutorService threadPool = BrExecutors.getThreadPool(20, 50);
                boolean execMark = true;
                while (execMark) {
                    String batchNumber = number;
                    Long nowMaxId = minId + 5000;

                    if (nowMaxId >= maxId) {
                        execMark = false;
                    }
                    List<MarketingSyncUser> syncUserByRuleScore = syncInfoMapper
                            .getSyncUserByRuleScore(apiCode, startTimeJob, endTimeJob, minId, nowMaxId, conditionRes.getData());
                    minId = nowMaxId + 1;
                    if (syncUserByRuleScore.size() <= 0) {
                        continue;
                    }
                    for (int i = 0; i < syncUserByRuleScore.size(); i++) {
                        MarketingSyncUser marketingSyncUser = syncUserByRuleScore.get(i);
                        threadPool.submit(() -> {
                            try {
                                //region 用户上传表头配置处理
                                JSONObject extendJson = new JSONObject();
                                Integer ia = 0, ib = 1, ic = 2;
                                if (baseHeadConfigVO != null) {
                                    JSONObject icData = null;
                                    if (StringUtils.isNotBlank(marketingSyncUser.getReserveField1())) {
                                        try {
                                            icData = JSON.parseObject(marketingSyncUser.getReserveField1());
                                        } catch (Exception ex) {
                                            log.error("用户上传数据非法的扩展信息：apiCode:{},id:{}"
                                                    , marketingSyncUser.getApiCode(), marketingSyncUser.getId());
                                        }
                                    }
                                    for (BaseHead head : baseHeadConfigVO.getBaseHead()) {
                                        String str = "";
                                        if (ia.equals(head.getType())) {
                                            str = "";
                                        } else if (ib.equals(head.getType())) {
                                            switch (head.getName().toLowerCase()) {
                                                case "apicode":
                                                    str = marketingSyncUser.getApiCode();
                                                    break;
                                                case "cusbatch":
                                                    str = marketingSyncUser.getCusBatch();
                                                    break;
                                                case "taskid":
                                                    str = marketingSyncUser.getCusBatch();
                                                    break;
                                                case "requestbatch":
                                                    str = marketingSyncUser.getRequestBatch();
                                                    break;
                                                case "requestid":
                                                    str = marketingSyncUser.getRequestBatch();
                                                    break;
                                                case "custnum":
                                                    str = marketingSyncUser.getCustNum();
                                                    break;
                                                case "idcard":
                                                    str = StringUtils.isBlank(marketingSyncUser.getFailType())
                                                            && StringUtils.isNotBlank(marketingSyncUser.getIdCard())
                                                            ? DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance()
                                                            .decode(marketingSyncUser.getIdCard()).getBytes())
                                                            : marketingSyncUser.getIdCard();
                                                    break;
                                                case "id":
                                                    str = StringUtils.isBlank(marketingSyncUser.getFailType())
                                                            && StringUtils.isNotBlank(marketingSyncUser.getIdCard())
                                                            ? DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance()
                                                            .decode(marketingSyncUser.getIdCard()).getBytes())
                                                            : marketingSyncUser.getIdCard();
                                                    break;
                                                case "cell":
                                                    str = StringUtils.isBlank(marketingSyncUser.getFailType())
                                                            && StringUtils.isNotBlank(marketingSyncUser.getCell())
                                                            ? DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance()
                                                            .decode(marketingSyncUser.getCell()).getBytes())
                                                            : marketingSyncUser.getCell();
                                                    break;
                                                case "name":
                                                    str = StringUtils.isBlank(marketingSyncUser.getFailType())
                                                            && StringUtils.isNotBlank(marketingSyncUser.getName())
                                                            ? DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance()
                                                            .decode(marketingSyncUser.getName()).getBytes())
                                                            : marketingSyncUser.getName();
                                                    break;
                                                case "grouptype":
                                                    str = marketingSyncUser.getGroupType();
                                                    break;
                                                case "usertype":
                                                    str = marketingSyncUser.getUserType();
                                                    break;
                                                case "registerdate":
                                                    str = marketingSyncUser.getRegisterDate();
                                                    break;
                                                /* 2021-8-18 14:41:12
                                                 * 回传文件结果表头新增字段：
                                                 * createTime 基础字段
                                                 */
                                                case "createtime": // 客户数据上传日期（精确到日）
                                                    str = marketingSyncUser.getAppletDate();
                                                    break;
                                                default:
                                                    str = "";
                                            }
                                        } else if (ic.equals(head.getType())) {
                                            if (icData != null) {
                                                str = icData.getString(head.getName());
                                            }
                                        } else {
                                            str = "";
                                        }
                                        extendJson.put(head.getName(), str);
                                    }
                                    ;
                                }
                                //endregion
                                String s = LocalDateTime.now().format(ymdhms);
                                // api_code,batch_number,cus_num,cell,create_time,update_time,decodeFailType,status,extend_json
                                String dataSql = String.format("('%s','%s','%s','%s','%s','%s','%s','%s','%s',%d,'%s','%s','%s')"
                                        , apiCode, batchNumber, marketingSyncUser.getCustNum()
                                        , marketingSyncUser.getCell()
                                        , StringUtils.isBlank(marketingSyncUser.getIdCard()) ? "" : marketingSyncUser.getIdCard()
                                        , StringUtils.isBlank(marketingSyncUser.getName()) ? "" : marketingSyncUser.getName(), s, s
                                        , marketingSyncUser.getFailType() == null ? "" : marketingSyncUser.getFailType()
                                        , marketingSyncUser.getStatus()
                                        , JSON.toJSONString(extendJson)
                                        , marketingSyncUser.getCusBatch()
                                        , marketingSyncUser.getUserType());
                                marketingUserMapper.insertByRequestId(apiCode, dataSql);
                                marketingSyncUserMapper.updateSyncUserStatus(apiCode, marketingSyncUser.getId(), 2);
                            } catch (Exception ex) {
                                log.error(ex.getMessage(), ex);
                            }
                        });
                    }
                }
                threadPool.shutdown();
                boolean isContiue = true;
                while (isContiue) {
                    if (threadPool.isTerminated()) {
                        isContiue = false;
                    } else {
                        try {
                            Thread.sleep(3000L);
                        } catch (Exception e) {
                            log.error("Thread.sleep error", e);
                        }
                    }
                }
                //endregion

                //region 处理task
//                int i = marketingUserMapper.countByPreUser(apiCode, taskId, strategyOfGroupDTO.getGroupType(),preDate);
                int actNum = marketingUserMapper.countBySureUser(apiCode, number);
                if (actNum > 0) {
                    MarketingTask task = new MarketingTask();
                    task.setApiCode(apiCode);
                    task.setBatchNumber(number);
                    task.setMonitorStatus(1);
                    task.setStatus(1);
                    task.setStrategyId(customerScoreRuleVO.getStrategyId());
                    task.setFileName(String.format("%s_%s", customerScoreRuleVO.getId().toString(), customerScoreRuleVO.getRuleNameShort()));
                    task.setCusBatch(customerScoreRuleVO.getId().toString());
                    task.setActualNumber(actNum);
                    task.setTaskNumber(taskNum);
                    task.setTaskType(customerScoreRuleVO.getTaskType());
                    task.setProductInfo(customerScoreRuleVO.getProductInfo());
                    String s = DateUtils.format(new Date(), "yyyy-MM-dd");
                    task.setMonitorType(customerScoreRuleVO.getExecType());
                    if (Integer.valueOf(4).equals(customerScoreRuleVO.getExecType())) {
                        MarketingTask task1 = marketingTaskMapper.selectCycleTopByApiCode(apiCode);
                        if (task1 != null) {
                            task.setStartDate(task1.getStartDate());
                            task.setCloseDate(task1.getCloseDate());
                        } else {
                            task.setStartDate(scoreBeginData);
                            task.setCloseDate(customerScoreRuleVO.getCycleEndDay());
                        }
                        task.setCycleDay(customerScoreRuleVO.getCycleDay().toString());
                    } else if (Integer.valueOf(3).equals(customerScoreRuleVO.getExecType())) {
                        task.setMonitorType(4);
                        task.setStartDate(scoreBeginData);
                        task.setCloseDate(customerScoreRuleVO.getCycleEndDay());
                        task.setCycleDay(customerScoreRuleVO.getCycleDay().toString());
                    } else {
                        task.setStartDate(scoreBeginData);
                        task.setCloseDate(scoreEndData);
                    }
                    task.setContextId(getTaskContextId());
                    marketingTaskMapper.insertTask(task);
                    MarketingTaskExtend taskExtend = new MarketingTaskExtend();
                    taskExtend.setApiCode(apiCode);
                    taskExtend.setTaskId(Long.valueOf(task.getId()));
                    taskExtend.setCusTaskId(customerScoreRuleVO.getId().toString());
                    taskExtend.setRuleId(customerScoreRuleVO.getId());
                    taskExtend.setGroupType(customerScoreRuleVO.getRuleNameShort());
                    taskExtend.setCreateTime(new Date());
                    taskExtend.setUploadTime(endTimeJob);
                    taskExtend.setExtendShowTitle(baseHeadConfigVO != null ? JSON.toJSONString(baseHeadConfigVO) : null);
                    taskExtend.setStrategyProductJson(customerScoreRuleVO.getStrategyProductJson());
                    marketingTaskExtendMapper.insertSelective(taskExtend);
                    TaskBatchnumberPreExample updateBatchExample = new TaskBatchnumberPreExample();
                    updateBatchExample.createCriteria().andBatchNumberEqualTo(number);
                    TaskBatchnumberPre updateBatchnumber = new TaskBatchnumberPre();
                    updateBatchnumber.setStatus(2);
                    taskBatchnumberPreMapper.updateByExampleSelective(updateBatchnumber, updateBatchExample);

                    try {
                        StringBuilder content = new StringBuilder();
                        content.append("apiCode：".concat(apiCode).concat("\r\n"))
                                .append("ruleId：".concat(customerScoreRuleVO.getId().toString()).concat("\r\n"))
                                .append("ruleName：".concat(customerScoreRuleVO.getRuleName()).concat("\r\n"))
                                .append("time：".concat(endTimeJob).concat("\r\n"))
                                .append("batchNumber：".concat(number).concat("\r\n"))
                                .append(String.format("预计数量: %d,入库数量：%ds", taskNum, actNum));
                        alarmClient.sendAlarm(content.toString(), "api人员数据生成任务", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                }
                //endregion

                //endregion
            }
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result<String> buildBatchNumber(String apiCode, String cusBatch, String groupType, String time, Integer isOnly) {
        boolean res = false;
        int i = 0;
        while (!res) {
            Result<String> stringResult = this.buildBatchNumberCore(apiCode, cusBatch, groupType, time, isOnly);
            res = ResultCode.SUCCESS.getValue()
                    .equals(stringResult.getCode());
            if (res) {
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(stringResult.getData());
            }
            i++;
            try {
                Thread.sleep(500L);
            } catch (Exception e) {
                log.error("Thread.sleep error", e);
            }
            if (i == 4) {
                res = true;
            }
        }
        return new Result<String>().setCode(ResultCode.FAIL.getValue());
    }

    /**
     * @param apiCode
     * @param cusBatch/score_rule的Id/fast_condition的Id
     * @param groupType/score_rule的编号/fast_condition的编号
     * @param time
     * @param isOnly
     * @return
     */
    private Result<String> buildBatchNumberCore(String apiCode, String cusBatch, String groupType, String time, Integer isOnly) {

        if (isOnly != null && isOnly.equals(1)) {
            int i = (int) ((Math.random() * 9 + 1) * 1000);
            String batchNumber = String.format("%s_%s_%d", apiCode, time, i);
            TaskBatchnumberPre batchnumberPre = new TaskBatchnumberPre();
            batchnumberPre.setApiCode(apiCode);
            batchnumberPre.setCusBatch(cusBatch);
            batchnumberPre.setGroupType(groupType);
            batchnumberPre.setStrategyId("");
            batchnumberPre.setBatchNumber(batchNumber);
            batchnumberPre.setCreateTime(new Date());
            taskBatchnumberPreMapper.insertSelective(batchnumberPre);
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(batchNumber);
        }

        String key = redisBatchNumberKey.concat(":")
                .concat(apiCode).concat(":")
                .concat(cusBatch).concat(":")
                .concat(groupType).concat(":")
                .concat(time);

        String s = redisChgService.get(key);
        if (StringUtils.isNotBlank(s)) {
            return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(s);
        }

        TaskBatchnumberPreExample preExample = new TaskBatchnumberPreExample();
        preExample.createCriteria().andApiCodeEqualTo(apiCode)
                .andCusBatchEqualTo(cusBatch)
                .andGroupTypeEqualTo(groupType)
                .andRecordDateEqualTo(time);
        List<TaskBatchnumberPre> taskBatchnumberPres = taskBatchnumberPreMapper.selectByExample(preExample);
        if (taskBatchnumberPres.size() > 0) {
            TaskBatchnumberPre taskBatchnumberPre = taskBatchnumberPres.get(0);
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(taskBatchnumberPre.getBatchNumber());
        } else {
            String keyCourrent = key.concat(":courrent");
//            String yyyyMMddHHmmss = DateUtils.format(new Date(), "yyyyMMddHHmmss");
            int i = (int) ((Math.random() * 9 + 1) * 1000);
            String batchNumber = String.format("%s_%s_%d", apiCode, time, i);
            if (redisChgService.setnx(keyCourrent, batchNumber, 2)) {
                TaskBatchnumberPre batchnumberPre = new TaskBatchnumberPre();
                batchnumberPre.setApiCode(apiCode);
                batchnumberPre.setCusBatch(cusBatch);
                batchnumberPre.setGroupType(groupType);
                batchnumberPre.setRecordDate(time);
                batchnumberPre.setStrategyId("");
                batchnumberPre.setBatchNumber(batchNumber);
                batchnumberPre.setCreateTime(new Date());
                taskBatchnumberPreMapper.insertSelective(batchnumberPre);
                String endSecond = DateHelper.date2TimeStamp(DateHelper.getDateAdd(1).concat(" 00:00:00"), "yyyy-MM-dd HH:mm:ss");
                Long l = Long.parseLong(endSecond) - System.currentTimeMillis() / 1000;
                redisChgService.set(key, batchNumber);
                redisChgService.expire(key, l.intValue());
                if (batchNumber.equals(redisChgService.get(keyCourrent))) {
                    redisChgService.del(keyCourrent);
                }
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(batchNumber);
            }
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
    }

    @Override
    public Result<Boolean> consumerUserToDb(Long synInfoId) {
        MarketingSyncInfo syncInfo = syncInfoMapper.selectByPrimaryKey(synInfoId);
        String apiCode = syncInfo.getApiCode();
        String cusBatch = syncInfo.getCusBatch();
        List<MarketingSyncUser> list = marketingUserMapper.selectSyncUser(apiCode, cusBatch);
        StringBuilder valuesStr = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            MarketingSyncUser marketingSyncUser = list.get(i);
            Result<String> stringResult = this.buildBatchNumber(apiCode, cusBatch, marketingSyncUser.getGroupType(), "", null);
            if (!ResultCode.SUCCESS.getValue().equals(stringResult.getCode())) {
                continue;
            }
            String s = DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            String batchNumber = stringResult.getData();
            // api_code,batch_number,cus_num,cell,create_time,update_time,decodeFailType
            valuesStr.append(String.format("('%s','%s','%s','%s','%s','%s','%s')"
                    , apiCode, batchNumber, marketingSyncUser.getCustNum()
                    , marketingSyncUser.getCell(), s, s, marketingSyncUser.getFailType()));
            if (i < list.size() - 1) {
                valuesStr.append(",");
            }
        }
        String s1 = valuesStr.toString();
        if (StringUtils.isNotBlank(s1)) {
            marketingUserMapper.insertByRequestId(apiCode, s1);
            MarketingSyncInfo updateSync = new MarketingSyncInfo();
            updateSync.setId(syncInfo.getId());
            updateSync.setIsUpload(2);
            syncInfoMapper.updateByPrimaryKeySelective(updateSync);
        }
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);
    }

    Result faskRuleToDb(List<MarketingCustomer> customers) {
        for (MarketingCustomer customer : customers) {
            String apiCode = customer.getApiCode();
            Result<List<FastTaskRule>> fastTaskRule = iRuleConfigService.getFastTaskRule(apiCode);
            if (!ResultCode.SUCCESS.getValue().equals(fastTaskRule.getCode()) || fastTaskRule.getData().size() <= 0) {
                continue;
            }
            List<FastTaskRule> ruls = fastTaskRule.getData();
            outFor:
            for (FastTaskRule rule : ruls) {
                ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(20, 20);
                String batchNumber = "";
                String filePath = "";

                //region check不定时不定量跑分规则数据是否正常
                Result checkRes = iRuleConfigService.checkFastTaskRule(rule);
                if (!ResultCode.SUCCESS.getValue().equals(checkRes.getCode())) {
                    continue;
                }
                List<TaskUserDataConditionDTO> dataConditionDTOList = JSON.parseObject(rule.getDataCondition()
                        , new TypeReference<List<TaskUserDataConditionDTO>>() {
                        }.getType());
                if (dataConditionDTOList == null || dataConditionDTOList.size() <= 0) {
                    log.warn(String.format("该规则没有数据范围 id:%d", rule.getId()));
                    continue;
                }
                if (TaskTypeEnum.PRODUCTDATA.getValue().equals(rule.getTaskType())
                        && StringUtils.isBlank(rule.getProductInfo())) {
                    log.warn(String.format("该产品规则无产品配置信息 id:%d", rule.getId()));
                    continue;
                }
                if (TaskTypeEnum.STRATYGYDATA.getValue().equals(rule.getTaskType())
                        && StringUtils.isBlank(rule.getStrategyId())) {
                    log.warn(String.format("该产品规则无策略配置信息 id:%d", rule.getId()));
                    continue;
                }
                //endregion

                //region 基于日期和场景 创建查询条件
                BaseHeadConfigVO baseHeadConfigVO = null;
                if (StringUtils.isNotBlank(rule.getCallbackInfo())) {
                    baseHeadConfigVO = JSON.parseObject(rule.getCallbackInfo(), new TypeReference<BaseHeadConfigVO>() {
                    }.getType());
                }

                Map<String, List<String>> conditions = dataConditionDTOList.stream()
                        .collect(Collectors.groupingBy(m -> m.getAppletDate()
                                , Collectors.collectingAndThen(Collectors.toList()
                                        , m -> m.stream().map(t -> t.getUserType()).collect(Collectors.toList()))));
                Integer dataType = Integer.valueOf(2).equals(rule.getDataType()) ? 1 : null;
                //endregion

                AtomicInteger preNum = new AtomicInteger();
                boolean isToFile = rule.getTaskType().equals(new Integer(1));
                StraHisFile file = null;
                Integer currentPage = 1;
                for (String s : conditions.keySet()) {

                    //region 校验范围内是否存在数据
                    List<String> userTypes = conditions.get(s);
                    Long minId = marketingSyncUserMapper.minId(apiCode, s, dataType, userTypes);
                    if (minId == null || minId <= 0) {
                        FastTaskRule updateRule = new FastTaskRule();
                        updateRule.setStatus(0);
                        updateRule.setId(rule.getId());
                        fastTaskRuleMapper.updateByPrimaryKeySelective(updateRule);
                        continue;
                    }
                    if (StringUtils.isBlank(batchNumber)) {
                        Result<String> resBatchNumber = buildBatchNumber(apiCode, rule.getId().toString()
                                , rule.getRuleNumber(), rule.getTaskTime().replaceAll("-", "")
                                , null);
                        if (!ResultCode.SUCCESS.getValue().equals(resBatchNumber.getCode())) {
                            continue outFor;
                        }
                        batchNumber = resBatchNumber.getData();
                        filePath = syncConfigService.getPath().concat(Constants.monitorTypeMap.get("1").concat("/").concat(apiCode).concat("/")
                                .concat(batchNumber).concat("/").concat(new SimpleDateFormat("yyyy-MM-dd").format(new Date())).concat("/").concat("0"));
                    }
                    if (StringUtils.isBlank(batchNumber)) {
                        continue;
                    }
                    //endregion

                    if (isToFile && file == null) {
                        file = saveTask(apiCode, batchNumber, filePath, rule, null, baseHeadConfigVO, null, preNum, isToFile, null, null);
                    }
                    Long maxId = marketingSyncUserMapper.maxId(apiCode, s, dataType, userTypes);
                    BaseHeadConfigVO headvo = baseHeadConfigVO;
                    String number = batchNumber;
                    String separator = marketingSepService.querySepByApiCode(apiCode);
                    while (minId <= maxId) {
                        Long nowMaxId = minId + 5000;
                        Long nowMinId = minId;
                        List<MarketingSyncUser> users = marketingSyncUserMapper.getUserById(apiCode, nowMinId, nowMaxId, dataType);
                        if (isToFile) {
                            dataToFile(users, headvo, threadPool, filePath, currentPage, separator, file, userTypes, s, preNum);
                        } else {
                            dataToDB(users, apiCode, number, headvo, threadPool, userTypes, s, preNum);
                        }
                        currentPage++;
                        minId = nowMaxId + 1;
                    }
                }
                threadPool.shutdown();
                boolean isContiue = true;
                while (isContiue) {
                    if (threadPool.isTerminated()) {
                        isContiue = false;
                    } else {
                        try {
                            Thread.sleep(3000L);
                        } catch (Exception e) {
                            log.error("Thread.sleep error", e);
                        }
                    }
                }
                if (StringUtils.isNotBlank(batchNumber)) {
                    //region 处理task
                    int actNum = isToFile ? preNum.get() : marketingUserMapper.countBySureUser(apiCode, batchNumber);
                    if (actNum > 0) {
                        if (!isToFile) {
                            saveTask(apiCode, batchNumber, filePath, rule, null, baseHeadConfigVO, actNum, preNum, isToFile, null, null);
                        } else {
                            file.setActualNum(preNum.get());
                            updateFile(file);
                        }
                        try {
                            StringBuilder content = new StringBuilder();
                            content.append("apiCode：".concat(apiCode).concat("\r\n"))
                                    .append("ruleId：".concat(rule.getId().toString()).concat("\r\n"))
                                    .append("ruleName：".concat(rule.getRuleName()).concat("\r\n"))
                                    .append("time：".concat(rule.getTaskTime()).concat("\r\n"))
                                    .append("batchNumber：".concat(batchNumber).concat("\r\n"))
                                    .append(String.format("预计数量: %d,入库数量：%d", preNum.get(), actNum));
                            alarmClient.sendAlarm(content.toString(), "批量数据生成任务", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
                        } catch (Exception ex) {
                            log.error(ex.getMessage(), ex);
                        }
                    }
                    //endregion
                }
            }
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private StraHisFile saveTask(String apiCode, String batchNumber, String filePath, FastTaskRule rule, CustomerScoreRuleVO ruleVO, BaseHeadConfigVO baseHeadConfigVO, Integer actNum, AtomicInteger preNum, boolean isToFile, String taskStart, String taskEnd) {

        //region 处理task
        MarketingTask task = new MarketingTask();
        task.setApiCode(apiCode);
        task.setBatchNumber(batchNumber);
        task.setMonitorStatus(isToFile ? 2 : 1);
        task.setStatus(1);
        task.setActualNumber(actNum == null ? 0 : actNum);
        task.setTaskNumber(preNum == null ? 0 : preNum.get());
        if (rule != null) {
            LocalDate startDate = LocalDate.parse(rule.getTaskTime(), ymd);
            String closeDate = startDate.plusDays(1).format(ymd);
            task.setTaskType(rule.getTaskType());
            task.setStrategyId(rule.getStrategyId());
            task.setProductInfo(rule.getProductInfo());
            task.setFileName(String.format("%s_%s", rule.getId().toString(), rule.getRuleNumber()));
            task.setCusBatch(rule.getId().toString());
            task.setStartDate(rule.getTaskTime());
            task.setCloseDate(closeDate);
            task.setMonitorType(1);
        }
        if (ruleVO != null) {
            task.setTaskType(ruleVO.getTaskType());
            task.setStrategyId(ruleVO.getStrategyId());
            task.setProductInfo(ruleVO.getProductInfo());
            task.setFileName(String.format("%s_%s", ruleVO.getId().toString(), ruleVO.getRuleNameShort()));
            task.setCusBatch(ruleVO.getId().toString());
            task.setMonitorType(ruleVO.getExecType());
            if (Integer.valueOf(4).equals(ruleVO.getExecType())) {
                MarketingTask task1 = marketingTaskMapper.selectCycleTopByApiCode(apiCode);
                if (task1 != null) {
                    task.setStartDate(task1.getStartDate());
                    task.setCloseDate(task1.getCloseDate());
                } else {
                    task.setStartDate(taskStart);
                    task.setCloseDate(ruleVO.getCycleEndDay());
                }
                task.setCycleDay(ruleVO.getCycleDay().toString());
            } else if (Integer.valueOf(3).equals(ruleVO.getExecType())) {
                task.setMonitorType(4);
                task.setStartDate(taskStart);
                task.setCloseDate(ruleVO.getCycleEndDay());
                task.setCycleDay(ruleVO.getCycleDay().toString());
            } else {
                task.setStartDate(taskStart);
                task.setCloseDate(taskEnd);
            }
        }
        task.setContextId(getTaskContextId());
        marketingTaskMapper.insertTask(task);

        //endregion

        //region 不定时不定量跑分关系表
        FastFileRelation relation = null;
        if (rule != null) {
            relation = new FastFileRelation();
            relation.setFastTaskId(rule.getId());
            relation.setTaskId(task.getId());
            relation.setCreateTime(new Date());
            fastFileRelationMapper.insertSelective(relation);
        }
        //endregion

        //region跑分扩展表
        MarketingTaskExtend taskExtend = new MarketingTaskExtend();
        taskExtend.setApiCode(apiCode);
        taskExtend.setTaskId(Long.valueOf(task.getId()));
        taskExtend.setCreateTime(new Date());
        taskExtend.setExtendShowTitle(baseHeadConfigVO != null ? JSON.toJSONString(baseHeadConfigVO) : null);
        if (rule != null) {
            taskExtend.setCusTaskId(rule.getId().toString());
            taskExtend.setRuleId(rule.getId());
            taskExtend.setGroupType(rule.getRuleNumber());
            taskExtend.setStrategyProductJson(rule.getProductField());
            taskExtend.setUploadTime(rule.getTaskTime());
        }
        if (ruleVO != null) {
            taskExtend.setCusTaskId(ruleVO.getId().toString());
            taskExtend.setRuleId(ruleVO.getId());
            taskExtend.setGroupType(ruleVO.getRuleNameShort());
            taskExtend.setStrategyProductJson(ruleVO.getStrategyProductJson());
            taskExtend.setUploadTime(taskStart);
        }
        marketingTaskExtendMapper.insertSelective(taskExtend);
        //endregion

        //region 跑分编号表
        TaskBatchnumberPreExample updateBatchExample = new TaskBatchnumberPreExample();
        updateBatchExample.createCriteria().andBatchNumberEqualTo(batchNumber);
        TaskBatchnumberPre updateBatchnumber = new TaskBatchnumberPre();
        updateBatchnumber.setStatus(2);
        taskBatchnumberPreMapper.updateByExampleSelective(updateBatchnumber, updateBatchExample);
        //endregion

        //region 跑分记录表
        if (isToFile) {
            StraHisFile blf = new StraHisFile();
            blf.setApiCode(task.getApiCode());
            blf.setBatchNumber(task.getBatchNumber());
            blf.setFilePath(filePath.substring(0, filePath.lastIndexOf("/")));
            blf.setCreateTime(new Date());
            blf.setUpdateTime(new Date());
            blf.setStatus(3);
            if (1 == task.getMonitorType()) {
                blf.setType(2);
            } else if (4 == task.getMonitorType()) {
                blf.setType(1);
            }
            blf.setIndexNum(1);
            straHisFileMapper.insertSelective(blf);

            TaskStatusDistribute statusDistribute = new TaskStatusDistribute();
            statusDistribute.setFileId(blf.getId());
            statusDistribute.setApiCode(task.getApiCode());
            statusDistribute.setBatchNumber(task.getBatchNumber());
            statusDistribute.setDistributeIndex(0);
            statusDistribute.setActualNum(task.getActualNumber().longValue());
            Date date = new Date();
            statusDistribute.setCreateTime(date);
            statusDistribute.setUpdateTime(date);
            taskStatusDistributeMapper.insertSelective(statusDistribute);

            if (relation != null) {
                FastFileRelation update = new FastFileRelation();
                update.setFileId(blf.getId());
                update.setId(relation.getId());
                fastFileRelationMapper.updateByPrimaryKeySelective(update);
            }
            return blf;
        }
        //endregion

        return null;

    }

    private void buildEs(String field, String value, MarketingHistory mh, List<MarketingCondition> conditionList,JSONObject json) {
        String _lowField = field.toLowerCase();
        if ((_lowField.equals("id")
                || field.equals("idcard"))
                && StringUtils.isNotNull(value)) {
            mh.setIdCard(value);
        }else if (_lowField.equals("cell")
                && StringUtils.isNotNull(value)) {
            String cellSource = json.getString("cellSource");
            mh.setCell(cellSource);
        } else if (_lowField.equals("name")
                && StringUtils.isNotNull(value)) {
            mh.setName(value);
        } else if ((_lowField.equals("taskid")
                || _lowField.equals("cusbatch"))
                && StringUtils.isNotNull(value)) {
            mh.setTaskId(value);
        } else if ((_lowField.equals("usertype"))
                && StringUtils.isNotNull(value)) {
            mh.setUserType(value);
        } else if(_lowField.equals("custnum")
                && StringUtils.isNotNull(value)){
            mh.setCusNum(value);
        } else {
            if (StringUtils.isNotNull(value)) {
                MarketingCondition marketingConditionStr = new MarketingCondition();
                marketingConditionStr.setFieldKey(field);
                marketingConditionStr.setStrValue(value);
                conditionList.add(marketingConditionStr);
            }

        }
    }

    private void insertEs(StraHisFile file, MarketingHistory mh, List<MarketingCondition> conditionList) {
        mh.setCondition(conditionList);
        mh.setBatchNumber(file.getBatchNumber());
        mh.setFileId(file.getId().toString());
        mh.setApiCode(file.getApiCode());
        mh.setRequestTime(new Date());
        String id = UuidUtils.getUuid();
        marketingHistoryEsService.insert(mh, id);
    }
}
