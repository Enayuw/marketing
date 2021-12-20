package com.br.marketing.service.Impl;
import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.util.BrCipherMaker;
//import com.br.common.util.BrExecutors;
import com.br.common.util.DateUtils;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.common.TaskExecCommonField;
import com.br.marketing.common.enums.TaskTypeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.StrategyOfGroupDTO;
import com.br.marketing.dto.TaskUserDataConditionDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.*;
import com.br.marketing.vo.*;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class ApiToDbServiceImpl  implements IApiToDbService {

    private static final Logger log = LoggerFactory.getLogger(ApiToDbServiceImpl.class);

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;
    @Value("${otherConfig.warning.path:00}")
    private String path;
    @Autowired
    MarketingCustomerMapper marketingCustomerMapper;

    @Autowired
    MarketingSyncInfoMapper syncInfoMapper;

    @Autowired
    MarketingUserMapper marketingUserMapper;

    @Autowired
    GroupStrategyConfigMapper groupStrategyConfigMapper;

    @Autowired
    MarketingTaskMapper marketingTaskMapper;

    @Autowired
    MarketingTaskExtendMapper marketingTaskExtendMapper;

    @Autowired
    RedisChgService redisChgService;

    @Autowired
    TaskBatchnumberPreMapper taskBatchnumberPreMapper;

    @Autowired
    LoanFileMapper loanFileMapper;

    @Autowired
    TaskStatusMapper taskStatusMapper;

    @Autowired
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
    public Long getTaskContextId(){
        return redisChgService.incr(redisElasticJobKey);
    }

    @Autowired
    MarketingSyncUserMapper marketingSyncUserMapper;

    @Autowired
    FastFileRelationMapper fastFileRelationMapper;

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
        if(StringUtils.isNotBlank(code)){
            criteria.andApiCodeEqualTo(code).andStatusEqualTo(Byte.valueOf("1"));
        }else{
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
            if(!ResultCode.SUCCESS.getValue().equals(scoreConfig.getCode())){
                continue;
            }
            List<CustomerScoreRuleVO> scoreConfigList = scoreConfig.getData();
            outrule:for (CustomerScoreRuleVO customerScoreRuleVO : scoreConfigList) {
                Boolean isToFile=customerScoreRuleVO.getTaskType().compareTo(Integer.valueOf(1))==0?Boolean.TRUE:Boolean.FALSE;
                if(TaskExecCommonField.isBuildTaskJob.equals(2)){
                    TaskExecCommonField.isBuildTaskJob =3;
                    break outrule;
                }
                //region 遍历规则

                //region 时间处理
                String startTime = customerScoreRuleVO.getStartTime();
                LocalDateTime nowTime = LocalDateTime.now();
                LocalDate nowData = LocalDate.now();
                String validTimeStr = nowData.format(ymd).concat(" " + startTime + ":00");
                LocalDateTime validTime = LocalDateTime.parse(validTimeStr,ymdhms);
                //筛选数据范围时间
                String sTimeStr = "",eTimeStr = "";
                Date sTime =null,eTime = null;
                //任务的开始时间和结束时间
                String taskStart="",taskEnd="";
                if(nowTime.compareTo(validTime)>0){
                    if("00:00".equals(startTime)){
                        sTimeStr = nowData.minusDays(1L).format(ymd).concat(" 00:00:00");
                        eTimeStr = validTime.format(ymdhms);
                        taskStart = LocalDate.now().format(ymd);
                        taskEnd = LocalDate.now().plusDays(1L).format(ymd);
                    }else {
                        sTimeStr = nowData.format(ymd).concat(" 00:00:00");
                        eTimeStr = validTime.format(ymdhms);
                        taskStart = LocalDate.now().format(ymd);
                        taskEnd = LocalDate.now().plusDays(1L).format(ymd);
                    }
                }else{
                    sTimeStr = nowData.minusDays(1L).format(ymd).concat(" 00:00:00");
                    eTimeStr = validTime.minusDays(1L).format(ymdhms);
                    taskStart = LocalDate.now().minusDays(1L).format(ymd);
                    taskEnd = LocalDate.now().format(ymd);
                }
                try {
                    sTime =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(sTimeStr);
                    eTime =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(eTimeStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Date ruleOpenTime = customerScoreRuleVO.getUpdateTime();
                if(ruleOpenTime == null){
                    ruleOpenTime = customerScoreRuleVO.getCreateTime();
                }
                String ruleOpenDay = new SimpleDateFormat("yyyy-MM-dd").format(ruleOpenTime);
                String nowDay = LocalDate.now().format(ymd);
                // 规则启用日期和生成任务日期相同 需要比较 生效时间是小于等于规则开启时间 认为历史的任务不予生成
                if(ruleOpenDay.equals(nowDay)&&eTime.compareTo(ruleOpenTime)<=0){
                    continue;
                }
                //endregion

                //region 条件解析
                Result<String> conditionRes = soleStrategyService.analysisCondition(customerScoreRuleVO.getConditionInfo());
                if(!ResultCode.SUCCESS.getValue().equals(conditionRes.getCode())){
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
                if(isUploadCount>0){
                    continue;
                }
                String number = "";
                Long minId = syncInfoMapper
                        .getMinIdByRuleScore(apiCode, sTimeStr, eTimeStr, conditionRes.getData());
                if(minId!=null&&minId>0){
                    String time = LocalDateTime.parse(eTimeStr,ymdhms).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    Result<String> batchNumberRes = buildBatchNumber(apiCode
                            ,customerScoreRuleVO.getId().toString(),customerScoreRuleVO.getRuleNameShort()
                            ,time,null);
                    if(!ResultCode.SUCCESS.getValue().equals(batchNumberRes.getCode())){
                        continue;
                    }
                    number=batchNumberRes.getData();
                }else{
                    continue;
                }

                MarketingTask hasTask = marketingTaskMapper.getByBatchNumber(number);
                if(hasTask!=null){
                    continue;
                }

                BaseHeadConfigVO baseHeadConfigVO = JSON.parseObject(customerScoreRuleVO.getBaseInfo()
                        , new TypeReference<BaseHeadConfigVO>() {}.getType());
                //endregion

                //region 处理marketingUser
                Long maxId = syncInfoMapper
                        .getMaxIdByRuleScore(apiCode, sTimeStr, eTimeStr, conditionRes.getData());
                ExecutorService threadPool = BrExecutors.getThreadPool(20, 50);
                boolean execMark = true;
                int currentPage = 1;
                Integer taskNum = 0;
                String filePath=path.concat("/").concat(Constants.monitorTypeMap.get(String.valueOf(customerScoreRuleVO.getExecType()))).concat("/").concat(apiCode).concat("/")
                        .concat(number).concat("/").concat(new SimpleDateFormat("yyyy-MM-dd").format(new Date())).concat("/").concat("0");
                while (execMark&& TaskExecCommonField.isBuildTaskJob.equals(1)) {
                    String batchNumber = number;
                    Long nowMaxId = minId+5000;

                    if(nowMaxId>=maxId){
                        execMark = false;
                    }
                    List<MarketingSyncUser> syncUserByRuleScore = syncInfoMapper
                            .getSyncUserByRuleScore(apiCode, sTimeStr, eTimeStr, minId,nowMaxId,conditionRes.getData());
                    minId = nowMaxId+1;
                    taskNum+=syncUserByRuleScore.size();
                    if(syncUserByRuleScore.size()<=0){
                        continue;
                    }
                    if(isToFile){
                        String separator=marketingSepService.querySepByApiCode(apiCode);
                        dataToFile(syncUserByRuleScore,baseHeadConfigVO,threadPool,filePath,currentPage,separator);
                    }else {
                        dataToDB(syncUserByRuleScore,apiCode,batchNumber,baseHeadConfigVO,threadPool);
                    }
                    currentPage++;
                }
                threadPool.shutdown();
                boolean isContiue = true;
                while (isContiue){
                    if(threadPool.isTerminated()){
                        isContiue= false;
                    }else{
                        try {
                            Thread.sleep(3000L);
                        } catch (Exception e) {
                            log.error("Thread.sleep error", e);
                        }
                    }
                }
                //endregion

                if(TaskExecCommonField.isBuildTaskJob.equals(2)){
                    TaskExecCommonField.isBuildTaskJob =3;
                    StringBuilder content = new StringBuilder();
                    content.append("停止生成的任务批次号：".concat(number).concat("\r\n"));
                    alarmClient.sendAlarm(content.toString(),"api人员数据生成任务",appName,secretKey,
                            Constants.sendCodeMap.get("uploadSuccess"));
                    break outrule;
                }

                //region 处理task
//                int i = marketingUserMapper.countByPreUser(apiCode, taskId, strategyOfGroupDTO.getGroupType(),preDate);
                int actNum = isToFile?taskNum:marketingUserMapper.countBySureUser(apiCode, number);
                if(actNum>0) {
                    MarketingTask task = new MarketingTask();
                    task.setApiCode(apiCode);
                    task.setBatchNumber(number);
                    task.setMonitorStatus(isToFile?2:1);
                    task.setTaskType(customerScoreRuleVO.getTaskType());
                    task.setProductInfo(customerScoreRuleVO.getProductInfo());
                    task.setStatus(1);
                    task.setStrategyId(customerScoreRuleVO.getStrategyId());
                    task.setFileName(String.format("%s_%s", customerScoreRuleVO.getId().toString(), customerScoreRuleVO.getRuleNameShort()));
                    task.setCusBatch(customerScoreRuleVO.getId().toString());
                    task.setActualNumber(actNum);
                    task.setTaskNumber(taskNum);
                    String s = DateUtils.format(new Date(), "yyyy-MM-dd");
                    task.setMonitorType(customerScoreRuleVO.getExecType());
                    if(Integer.valueOf(4).equals(customerScoreRuleVO.getExecType())) {
                        MarketingTask task1 = marketingTaskMapper.selectCycleTopByApiCode(apiCode);
                        if (task1 != null) {
                            task.setStartDate(task1.getStartDate());
                            task.setCloseDate(task1.getCloseDate());
                        } else {
                            task.setStartDate(taskStart);
                            task.setCloseDate(customerScoreRuleVO.getCycleEndDay());
                        }
                        task.setCycleDay(customerScoreRuleVO.getCycleDay().toString());
                    }else if(Integer.valueOf(3).equals(customerScoreRuleVO.getExecType())){
                        task.setMonitorType(4);
                        task.setStartDate(taskStart);
                        task.setCloseDate(customerScoreRuleVO.getCycleEndDay());
                        task.setCycleDay(customerScoreRuleVO.getCycleDay().toString());
                    }else{
                        task.setStartDate(taskStart);
                        task.setCloseDate(taskEnd);
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
                    taskExtend.setUploadTime(eTimeStr);
                    taskExtend.setExtendShowTitle(baseHeadConfigVO!=null?Joiner.on(",").join(baseHeadConfigVO.getShowBaseHead()):"");
                    taskExtend.setStrategyProductJson(customerScoreRuleVO.getStrategyProductJson());
                    marketingTaskExtendMapper.insertSelective(taskExtend);
                    TaskBatchnumberPreExample updateBatchExample = new TaskBatchnumberPreExample();
                    updateBatchExample.createCriteria().andBatchNumberEqualTo(number);
                    TaskBatchnumberPre updateBatchnumber = new TaskBatchnumberPre();
                    updateBatchnumber.setStatus(2);
                    taskBatchnumberPreMapper.updateByExampleSelective(updateBatchnumber,updateBatchExample);

                    if(isToFile){
                        LoanFile loanFile=saveStraHisFile(task,customerScoreRuleVO,eTimeStr,filePath);
                        saveTaskStatusDistribute(task,loanFile);
                    }
                    try{
                        StringBuilder content = new StringBuilder();
                        content.append("apiCode：".concat(apiCode).concat("\r\n"))
                                .append("ruleId：".concat(customerScoreRuleVO.getId().toString()).concat("\r\n"))
                                .append("ruleName：".concat(customerScoreRuleVO.getRuleName()).concat("\r\n"))
                                .append("time：".concat(eTimeStr).concat("\r\n"))
                                .append("batchNumber：".concat(number).concat("\r\n"))
                                .append(String.format("预计数量: %d,入库数量：%d",taskNum,actNum));
                        alarmClient.sendAlarm(content.toString(),"api人员数据生成任务",appName,secretKey,
                                Constants.sendCodeMap.get("uploadSuccess"));
                    }catch (Exception ex){
                        log.error(ex.getMessage(),ex);
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
    private void dataToDB(List<MarketingSyncUser> syncUserByRuleScore,String apiCode,String batchNumber,BaseHeadConfigVO baseHeadConfigVO,ExecutorService threadPool){
            threadPool.submit(()->{
                try {
                    if(!TaskExecCommonField.isBuildTaskJob.equals(1)){
                        return;
                    }
                    for (MarketingSyncUser syncUser : syncUserByRuleScore) {
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
                        marketingSyncUserMapper.updateSyncUserStatus(apiCode,syncUser.getId(),2);
                    }
                }catch (Exception ex){
                    log.error(ex.getMessage(),ex);
                }
            });
    }

    private void dataToDB(MarketingSyncUser syncUser,String apiCode,String batchNumber,BaseHeadConfigVO baseHeadConfigVO){
        try {
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
            marketingSyncUserMapper.updateSyncUserStatus(apiCode,syncUser.getId(),2);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
        };
    }
    /**
     * 写入文件
     * @param syncUserByRuleScore
     * @param baseHeadConfigVO
     * @param threadPool
     * @param filePath
     * @param currentPage
     * @param sep
     */
    private void  dataToFile(List<MarketingSyncUser> syncUserByRuleScore,BaseHeadConfigVO baseHeadConfigVO,ExecutorService threadPool,String filePath,int currentPage,String sep){

        File writeName = new File(filePath );
        if (!writeName.exists()) {
            writeName.mkdirs();
        }
        threadPool.submit(()->{
            File file1 = new File(filePath + "/" + currentPage + ".txt");
            try(Writer fw = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(file1), "UTF-8"));) {
                for (MarketingSyncUser syncUser : syncUserByRuleScore) {
                    //region 用户上传表头配置处理
                    if (baseHeadConfigVO != null) {
                        JSONObject extendJson = getCustomerHead(syncUser, baseHeadConfigVO);
                        StringBuilder sb=new StringBuilder();
                        for (String s : baseHeadConfigVO.getShowBaseHead()) {
                            String ss = extendJson.getString(s);
                            if(StringUtils.isNotBlank(ss)){
                                sb.append(ss).append(sep);
                            }else {
                                sb.append(sep);
                            }
                        }
                        fw.append(sb).append("\r\n");
                    }
                }
            }catch (Exception ex){
                log.error(ex.getMessage(),ex);
            }
        });
    }

    private JSONObject getCustomerHead(MarketingSyncUser syncUser,BaseHeadConfigVO baseHeadConfigVO){
        if(baseHeadConfigVO == null){
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

    private LoanFile saveStraHisFile(MarketingTask task,CustomerScoreRuleVO customerScoreRuleVO,String uploadTime,String filePath){

        LoanFile blf=new LoanFile();
        blf.setApiCode(task.getApiCode());
        blf.setFilePath(filePath.substring(0,filePath.lastIndexOf("/")));
        blf.setStatus(1);
        if(1 == task.getMonitorType()){
            blf.setType(2);
        }else if(4==task.getMonitorType()){
            blf.setType(1);
        }
        blf.setIndexNum(1);
        blf.setBatchNumber(task.getBatchNumber());
        blf.setExpectedNum(task.getActualNumber());
        blf.setShowTitle(createShowTitle(task,customerScoreRuleVO,uploadTime));
        loanFileMapper.insertFile(blf);
        return blf;
    }
    private void saveTaskStatusDistribute(MarketingTask task,LoanFile loanFile){
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

    private void saveTaskStatus(MarketingTask task,LoanFile loanFile){
        TaskStatus bts=new TaskStatus();
        if(1 == task.getMonitorType()){
            bts.setOnceStatus(1);
        }else if(4==task.getMonitorType()){
            bts.setAllStatus(1);
        }
        bts.setApiCode(task.getApiCode());
        bts.setBatchNumber(task.getBatchNumber());
        bts.setFileId(loanFile.getId());
        taskStatusMapper.insertTaskStatus(bts);
    }
    private String createShowTitle(MarketingTask task,CustomerScoreRuleVO customerScoreRuleVO,String uploadTime){
        return task.getApiCode().concat("_")
                .concat(customerScoreRuleVO.getId().toString().concat("_"))
                .concat(customerScoreRuleVO.getRuleNameShort().concat("_"))
                .concat(uploadTime.concat("_"))
                .concat(new SimpleDateFormat("yyyyMMdd").format(new Date()));
    }
    @Override
    public Result pushToDb(String code,HashMap<String,String> params){
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
        if(StringUtils.isNotBlank(code)){
            criteria.andApiCodeEqualTo(code).andStatusEqualTo(Byte.valueOf("1"));
        }else{
            criteria.andStatusEqualTo(Byte.valueOf("1"));
        }
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        for (MarketingCustomer marketingCustomer : marketingCustomers) {
            String apiCode = marketingCustomer.getApiCode();
            tableCreateService.createMarketingSyncUserTable(apiCode);
            tableCreateService.createMarketingUserTable(apiCode);
            Result<List<CustomerScoreRuleVO>> scoreConfig = iRuleConfigService.getScoreConfig(apiCode);
            if(!ResultCode.SUCCESS.getValue().equals(scoreConfig.getCode())){
                continue;
            }
            List<CustomerScoreRuleVO> scoreConfigList = scoreConfig.getData();
            if(StringUtils.isNotEmpty(userTypeJob)){
                scoreConfigList = soleStrategyService.matchScoreRule(scoreConfigList,userTypeJob);
            }
            for (CustomerScoreRuleVO customerScoreRuleVO : scoreConfigList) {
                //region 遍历规则

                //region 条件解析
                Result<String> conditionRes = soleStrategyService.analysisCondition(customerScoreRuleVO.getConditionInfo());
                if(!ResultCode.SUCCESS.getValue().equals(conditionRes.getCode())){
                    continue;
                }
                String number = "";
                int taskNum = syncInfoMapper.countByPreUserWithRule(apiCode, startTimeJob, endTimeJob, conditionRes.getData());
                if(taskNum>0){
                    String time = LocalDateTime.parse(endTimeJob,ymdhms).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    Result<String> batchNumberRes = buildBatchNumber(apiCode
                            ,customerScoreRuleVO.getId().toString(),customerScoreRuleVO.getRuleNameShort()
                            ,time,1);
                    if(!ResultCode.SUCCESS.getValue().equals(batchNumberRes.getCode())){
                        continue;
                    }
                    number=batchNumberRes.getData();
                }else{
                    continue;
                }
                MarketingTask hasTask = marketingTaskMapper.getByBatchNumber(number);
                if(hasTask!=null){
                    continue;
                }
                BaseHeadConfigVO baseHeadConfigVO = JSON.parseObject(customerScoreRuleVO.getBaseInfo()
                        , new TypeReference<BaseHeadConfigVO>() {}.getType());
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
                    Long nowMaxId = minId+5000;

                    if(nowMaxId>=maxId){
                        execMark = false;
                    }
                    List<MarketingSyncUser> syncUserByRuleScore = syncInfoMapper
                            .getSyncUserByRuleScore(apiCode, startTimeJob, endTimeJob, minId,nowMaxId, conditionRes.getData());
                    minId = nowMaxId+1;
                    if(syncUserByRuleScore.size()<=0){
                        continue;
                    }
                    for (int i = 0; i < syncUserByRuleScore.size(); i++) {
                        MarketingSyncUser marketingSyncUser = syncUserByRuleScore.get(i);
                        threadPool.submit(()->{
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
                                marketingSyncUserMapper.updateSyncUserStatus(apiCode,marketingSyncUser.getId(),2);
                            }catch (Exception ex){
                                log.error(ex.getMessage(),ex);
                            }
                        });
                    }
                }
                threadPool.shutdown();
                boolean isContiue = true;
                while (isContiue){
                    if(threadPool.isTerminated()){
                        isContiue= false;
                    }else{
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
                if(actNum>0) {
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
                    String s = DateUtils.format(new Date(), "yyyy-MM-dd");
                    task.setMonitorType(customerScoreRuleVO.getExecType());
                    if(Integer.valueOf(4).equals(customerScoreRuleVO.getExecType())) {
                        MarketingTask task1 = marketingTaskMapper.selectCycleTopByApiCode(apiCode);
                        if (task1 != null) {
                            task.setStartDate(task1.getStartDate());
                            task.setCloseDate(task1.getCloseDate());
                        } else {
                            task.setStartDate(scoreBeginData);
                            task.setCloseDate(customerScoreRuleVO.getCycleEndDay());
                        }
                        task.setCycleDay(customerScoreRuleVO.getCycleDay().toString());
                    }else if(Integer.valueOf(3).equals(customerScoreRuleVO.getExecType())){
                        task.setMonitorType(4);
                        task.setStartDate(scoreBeginData);
                        task.setCloseDate(customerScoreRuleVO.getCycleEndDay());
                        task.setCycleDay(customerScoreRuleVO.getCycleDay().toString());
                    }else{
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
                    taskExtend.setExtendShowTitle(baseHeadConfigVO!=null?Joiner.on(",").join(baseHeadConfigVO.getShowBaseHead()):"");
                    taskExtend.setStrategyProductJson(customerScoreRuleVO.getStrategyProductJson());
                    marketingTaskExtendMapper.insertSelective(taskExtend);
                    TaskBatchnumberPreExample updateBatchExample = new TaskBatchnumberPreExample();
                    updateBatchExample.createCriteria().andBatchNumberEqualTo(number);
                    TaskBatchnumberPre updateBatchnumber = new TaskBatchnumberPre();
                    updateBatchnumber.setStatus(2);
                    taskBatchnumberPreMapper.updateByExampleSelective(updateBatchnumber,updateBatchExample);

                    try{
                        StringBuilder content = new StringBuilder();
                        content.append("apiCode：".concat(apiCode).concat("\r\n"))
                                .append("ruleId：".concat(customerScoreRuleVO.getId().toString()).concat("\r\n"))
                                .append("ruleName：".concat(customerScoreRuleVO.getRuleName()).concat("\r\n"))
                                .append("time：".concat(endTimeJob).concat("\r\n"))
                                .append("batchNumber：".concat(number).concat("\r\n"))
                                .append(String.format("预计数量: %d,入库数量：%d",taskNum,actNum));
                        alarmClient.sendAlarm(content.toString(),"api人员数据生成任务",appName,secretKey,
                                Constants.sendCodeMap.get("uploadSuccess"));
                    }catch (Exception ex){
                        log.error(ex.getMessage(),ex);
                    }
                }
                //endregion

                //endregion
            }
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result<String> buildBatchNumber(String apiCode, String cusBatch, String groupType, String time,Integer isOnly) {
        boolean res = false;
        int i = 0;
        while (!res){
            Result<String> stringResult = this.buildBatchNumberCore(apiCode, cusBatch, groupType, time,isOnly);
            res = ResultCode.SUCCESS.getValue()
                    .equals(stringResult.getCode());
            if(res){
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(stringResult.getData());
            }
            i++;
            try {
                Thread.sleep(500L);
            } catch (Exception e) {
                log.error("Thread.sleep error", e);
            }
            if(i==4){
                res = true;
            }
        }
        return new Result<String>().setCode(ResultCode.FAIL.getValue());
    }

    /**
     *
     * @param apiCode
     * @param cusBatch/score_rule的Id/fast_condition的Id
     * @param groupType/score_rule的编号/fast_condition的编号
     * @param time
     * @param isOnly
     * @return
     */
    private Result<String> buildBatchNumberCore(String apiCode, String cusBatch, String groupType, String time,Integer isOnly) {

        if(isOnly != null&& isOnly.equals(1)){
            int i = (int) ((Math.random()*9+1)*1000);
            String batchNumber = String.format("%s_%s_%d", apiCode, time,i);
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

        TaskBatchnumberPreExample preExample= new TaskBatchnumberPreExample();
        preExample.createCriteria().andApiCodeEqualTo(apiCode)
                .andCusBatchEqualTo(cusBatch)
                .andGroupTypeEqualTo(groupType)
                .andRecordDateEqualTo(time);
        List<TaskBatchnumberPre> taskBatchnumberPres = taskBatchnumberPreMapper.selectByExample(preExample);
        if(taskBatchnumberPres.size()>0){
            TaskBatchnumberPre taskBatchnumberPre = taskBatchnumberPres.get(0);
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(taskBatchnumberPre.getBatchNumber());
        }else{
            String keyCourrent = key.concat(":courrent");
//            String yyyyMMddHHmmss = DateUtils.format(new Date(), "yyyyMMddHHmmss");
            int i = (int) ((Math.random()*9+1)*1000);
            String batchNumber = String.format("%s_%s_%d", apiCode, time,i);
            if(redisChgService.setnx(keyCourrent,batchNumber,2).equals(1L)){
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
                redisChgService.set(key,batchNumber);
                redisChgService.expire(key,l.intValue());
                if(batchNumber.equals(redisChgService.get(keyCourrent))){
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
            Result<String> stringResult = this.buildBatchNumber(apiCode, cusBatch, marketingSyncUser.getGroupType(), "",null);
            if(!ResultCode.SUCCESS.getValue().equals(stringResult.getCode())){
                continue;
            }
            String s = DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            String batchNumber = stringResult.getData();
            // api_code,batch_number,cus_num,cell,create_time,update_time,decodeFailType
            valuesStr.append(String.format("('%s','%s','%s','%s','%s','%s','%s')"
                    ,apiCode,batchNumber,marketingSyncUser.getCustNum()
                    ,marketingSyncUser.getCell(),s,s,marketingSyncUser.getFailType()));
            if(i<list.size()-1){
                valuesStr.append(",");
            }
        }
        String s1 = valuesStr.toString();
        if(StringUtils.isNotBlank(s1)){
            marketingUserMapper.insertByRequestId(apiCode, s1);
            MarketingSyncInfo updateSync = new MarketingSyncInfo();
            updateSync.setId(syncInfo.getId());
            updateSync.setIsUpload(2);
            syncInfoMapper.updateByPrimaryKeySelective(updateSync);
        }
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);
    }

    Result faskRuleToDb(List<MarketingCustomer> customers){
        for (MarketingCustomer customer : customers) {
            String apiCode = customer.getApiCode();
            Result<List<FastTaskRule>> fastTaskRule = iRuleConfigService.getFastTaskRule(apiCode);
            if(!ResultCode.SUCCESS.getValue().equals(fastTaskRule.getCode())||fastTaskRule.getData().size()<=0){
                continue;
            }
            List<FastTaskRule> ruls = fastTaskRule.getData();
            outFor: for (FastTaskRule rule : ruls) {
                LocalDate startDate = LocalDate.parse(rule.getTaskTime(), ymd);
                String closeDate = startDate.plusDays(1).format(ymd);
                BaseHeadConfigVO baseHeadConfigVO = null;
                if(StringUtils.isNotBlank(rule.getCallbackInfo())){
                    baseHeadConfigVO = JSON.parseObject(rule.getCallbackInfo(), new TypeReference<BaseHeadConfigVO>() {
                    }.getType());
                }
                ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(20, 20);
                String batchNumber = "";
                //region check
                Result checkRes = iRuleConfigService.checkFastTaskRule(rule);
                if(!ResultCode.SUCCESS.getValue().equals(checkRes.getCode())){
                    continue;
                }
                List<TaskUserDataConditionDTO> dataConditionDTOList = JSON.parseObject(rule.getDataCondition()
                        , new TypeReference<List<TaskUserDataConditionDTO>>() {}.getType());
                if(dataConditionDTOList==null||dataConditionDTOList.size()<=0){
                    log.warn(String.format("该规则没有数据范围 id:%d",rule.getId()));
                    continue;
                }
                if(TaskTypeEnum.PRODUCTDATA.getValue().equals(rule.getTaskType())
                &&StringUtils.isBlank(rule.getProductInfo())){
                    log.warn(String.format("该产品规则无产品配置信息 id:%d",rule.getId()));
                    continue;
                }
                if(TaskTypeEnum.STRATYGYDATA.getValue().equals(rule.getTaskType())
                        &&StringUtils.isBlank(rule.getStrategyId())){
                    log.warn(String.format("该产品规则无策略配置信息 id:%d",rule.getId()));
                    continue;
                }
                //endregion
                Map<String, List<String>> conditions = dataConditionDTOList.stream()
                        .collect(Collectors.groupingBy(m -> m.getAppletDate()
                                , Collectors.collectingAndThen(Collectors.toList()
                                        , m -> m.stream().map(t -> t.getUserType()).collect(Collectors.toList()))));
                Integer dataType = Integer.valueOf(2).equals(rule.getDataType())?1:null;
                AtomicInteger preNum = new AtomicInteger();
                for (String s : conditions.keySet()) {
                    List<String> userTypes = conditions.get(s);
                    Long minId = marketingSyncUserMapper.minId(apiCode, s,dataType, userTypes);
                    if(minId<=0){
                        continue;
                    }
                    if(StringUtils.isBlank(batchNumber)) {
                        Result<String> resBatchNumber = buildBatchNumber(apiCode, rule.getId().toString()
                                , rule.getRuleNumber(), rule.getTaskTime().replaceAll("-","")
                                , null);
                        if (!ResultCode.SUCCESS.getValue().equals(resBatchNumber.getCode())) {
                            continue outFor;
                        }
                        batchNumber = resBatchNumber.getData();
                    }
                    if(StringUtils.isBlank(batchNumber)){
                        continue ;
                    }
                    Long maxId = marketingSyncUserMapper.maxId(apiCode, s, dataType, userTypes);
                    BaseHeadConfigVO headvo = baseHeadConfigVO;
                    String number = batchNumber;
                    while(minId<=maxId){
                        Long nowMaxId = minId+5000;
                        Long nowMinId = minId;
                        threadPool.submit(()->{
                            List<MarketingSyncUser> users = marketingSyncUserMapper.getUserById(apiCode, nowMinId, nowMaxId, dataType);
                            List<MarketingSyncUser> canUsers = users.stream().filter(t -> userTypes.contains(t.getUserType()) && s.equals(t.getAppletDate())).collect(Collectors.toList());
                            preNum.getAndAdd(canUsers.size());
                            for (MarketingSyncUser canUser : canUsers) {
                                dataToDB(canUser,apiCode,number,headvo);
                            }
                        });
                        minId = nowMaxId+1;
                    }
                }
                threadPool.shutdown();
                boolean isContiue = true;
                while (isContiue){
                    if(threadPool.isTerminated()){
                        isContiue= false;
                    }else{
                        try {
                            Thread.sleep(3000L);
                        } catch (Exception e) {
                            log.error("Thread.sleep error", e);
                        }
                    }
                }
                if(StringUtils.isNotBlank(batchNumber)){
                    //region 处理task
//                int i = marketingUserMapper.countByPreUser(apiCode, taskId, strategyOfGroupDTO.getGroupType(),preDate);
                    int actNum = marketingUserMapper.countBySureUser(apiCode, batchNumber);
                    if(actNum>0) {
                        MarketingTask task = new MarketingTask();
                        task.setApiCode(apiCode);
                        task.setBatchNumber(batchNumber);
                        task.setMonitorStatus(1);
                        task.setTaskType(rule.getTaskType());
                        task.setStatus(1);
                        task.setStrategyId(rule.getStrategyId());
                        task.setFileName(String.format("%s_%s", rule.getId().toString(), rule.getRuleNumber()));
                        task.setCusBatch(rule.getId().toString());
                        task.setActualNumber(actNum);
                        task.setTaskNumber(preNum.get());
                        String s = DateUtils.format(new Date(), "yyyy-MM-dd");
                        task.setMonitorType(1);
                        task.setStartDate(rule.getTaskTime());
                        task.setCloseDate(closeDate);
                        task.setContextId(getTaskContextId());
                        marketingTaskMapper.insertTask(task);
                        FastFileRelation relation = new FastFileRelation();
                        relation.setFastTaskId(rule.getId());
                        relation.setTaskId(task.getId());
                        relation.setCreateTime(new Date());
                        fastFileRelationMapper.insertSelective(relation);
                        MarketingTaskExtend taskExtend = new MarketingTaskExtend();
                        taskExtend.setApiCode(apiCode);
                        taskExtend.setTaskId(Long.valueOf(task.getId()));
                        taskExtend.setCusTaskId(rule.getId().toString());
                        taskExtend.setRuleId(rule.getId());
                        taskExtend.setGroupType(rule.getRuleNumber());
                        taskExtend.setCreateTime(new Date());
                        taskExtend.setUploadTime(rule.getTaskTime());
                        taskExtend.setExtendShowTitle(baseHeadConfigVO!=null?Joiner.on(",").join(baseHeadConfigVO.getShowBaseHead()):"");
                        taskExtend.setStrategyProductJson(rule.getProductField());
                        marketingTaskExtendMapper.insertSelective(taskExtend);
                        TaskBatchnumberPreExample updateBatchExample = new TaskBatchnumberPreExample();
                        updateBatchExample.createCriteria().andBatchNumberEqualTo(batchNumber);
                        TaskBatchnumberPre updateBatchnumber = new TaskBatchnumberPre();
                        updateBatchnumber.setStatus(2);
                        taskBatchnumberPreMapper.updateByExampleSelective(updateBatchnumber,updateBatchExample);
                        try{
                            StringBuilder content = new StringBuilder();
                            content.append("apiCode：".concat(apiCode).concat("\r\n"))
                                    .append("ruleId：".concat(rule.getId().toString()).concat("\r\n"))
                                    .append("ruleName：".concat(rule.getRuleName()).concat("\r\n"))
                                    .append("time：".concat(rule.getTaskTime()).concat("\r\n"))
                                    .append("batchNumber：".concat(batchNumber).concat("\r\n"))
                                    .append(String.format("预计数量: %d,入库数量：%d",preNum.get(),actNum));
                            alarmClient.sendAlarm(content.toString(),"批量数据生成任务",appName,secretKey,
                                    Constants.sendCodeMap.get("uploadSuccess"));
                        }catch (Exception ex){
                            log.error(ex.getMessage(),ex);
                        }
                    }
                    //endregion
                }
            }
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }
}
