package com.br.marketing.service.Impl;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.BrExecutors;
import com.br.common.util.DateUtils;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.StrategyOfGroupDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.IApiToDbService;
import com.br.marketing.service.IProductResultSimpleService;
import com.br.marketing.service.IRuleConfigService;
import com.br.marketing.service.SoleStrategyService;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;

@Service
public class ApiToDbServiceImpl  implements IApiToDbService {

    private static final Logger log = LoggerFactory.getLogger(ApiToDbServiceImpl.class);

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;

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

    @Override
    public Long getTaskContextId(){
        return redisChgService.incr(redisElasticJobKey);
    }

//    @Override
    public Result pushToDbOld() {
        Date date = new Date();
        String nowDate = DateUtils.format(date, "yyyy-MM-dd");
        String preDate = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String nextDate = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Date preDateDay = null;
        Date nowDateDay = null;
        try {
            preDateDay = new SimpleDateFormat("yyyy-MM-dd").parse(preDate);
            nowDateDay = new SimpleDateFormat("yyyy-MM-dd").parse(nowDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andStatusEqualTo(Byte.valueOf("1"));
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);

        /**
         * 1.遍历客户apicode
         * 2.遍历该apicode的T日客户批次号
         * 3.查询该客户 该批次T日的数据
         */
        for (MarketingCustomer marketingCustomer : marketingCustomers) {
            String apiCode = marketingCustomer.getApiCode();
            GroupStrategyConfigExample configExample = new GroupStrategyConfigExample();
            configExample.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo(1);
            List<GroupStrategyConfig> groupStrategyConfigs = groupStrategyConfigMapper.selectByExample(configExample);

            String tableName="b_marketing_user_"+apiCode;
            marketingUserMapper.createUserTable(tableName);

            List<String> taskIds = syncInfoMapper.getCusBatchByApiAndTime(apiCode, preDate, nowDate);

            for (String taskId : taskIds) {
                //region 校验数据条数
                MarketingSyncInfoExample syncInfoIngExample = new MarketingSyncInfoExample();
                syncInfoIngExample.createCriteria()
                        .andApiCodeEqualTo(apiCode)
                        .andCusBatchEqualTo(taskId)
                        .andCreateTimeGreaterThanOrEqualTo(preDateDay)
                        .andCreateTimeLessThan(nowDateDay)
                        .andStatusEqualTo(1)
                        .andIsUploadEqualTo(1);
                int ii = syncInfoMapper.countByExample(syncInfoIngExample);
                if(ii>0){
                    continue;
                }

                MarketingSyncInfoExample syncInfoEdExample = new MarketingSyncInfoExample();
                syncInfoEdExample.createCriteria()
                        .andApiCodeEqualTo(apiCode)
                        .andCusBatchEqualTo(taskId)
                        .andCreateTimeGreaterThanOrEqualTo(preDateDay)
                        .andCreateTimeLessThan(nowDateDay)
                        .andStatusIn(Arrays.asList(2, 4))
                        .andIsUploadEqualTo(1);
                int i2 = syncInfoMapper.countByExample(syncInfoEdExample);
                if(i2<=0){
                    continue;
                }
                //endregion

                //region 生成批次号
                List<String> groupTypes = marketingUserMapper.selectGroupByCodeAndCusAndTime(apiCode,taskId,preDate);
//                ArrayList<StrategyOfGroupDTO> strategyOfGroupDTOS = new ArrayList<>();
                HashMap<String,StrategyOfGroupDTO> strategyOfGroupHashMap = new HashMap();
                groupTypes.forEach(t->{
                    Optional<GroupStrategyConfig> first = groupStrategyConfigs.stream()
                            .filter(k -> k.getGroupType().equals(t)).findFirst();
                    if (first.isPresent()) {
                        Result<String> stringResult = buildBatchNumber(apiCode, taskId, t
                                , DateUtils.format(new Date(), "yyyy-MM-dd"));
                        if(ResultCode.SUCCESS.getValue().equals(stringResult.getCode())) {
                            StrategyOfGroupDTO strategyOfGroupDTO = new StrategyOfGroupDTO();
                            BeanUtils.copyProperties(first.get(), strategyOfGroupDTO);
                            strategyOfGroupDTO.setBatchNumber(stringResult.getData());
                            if(StringUtils.isNotBlank(strategyOfGroupDTO.getBaseInfo())){
                                BaseHeadConfigVO vo =JSON.parseObject(strategyOfGroupDTO.getBaseInfo()
                                        ,new TypeReference<BaseHeadConfigVO>(){}.getType());
                                BaseHeadConfigVO orderBaseHeadInfo = iProductResultSimpleService.getOrderBaseHeadInfo(vo);
                                strategyOfGroupDTO.setConfigVO(orderBaseHeadInfo);
                            }else{
                                strategyOfGroupDTO.setConfigVO(null);
                            }
                            strategyOfGroupHashMap.put(t, strategyOfGroupDTO);
                    }
                }
                });
                //endregion

                //region 处理marketingUser
                ExecutorService threadPool = BrExecutors.getThreadPool(20, 20);
                Long aLong = syncInfoMapper.minSyncId(apiCode, taskId, preDate, nowDate);
                boolean dbMark = true;
                Integer ia=0,ib=1,ic=2;
                while(dbMark){
                    List<MarketingSyncInfo> syncInfos = syncInfoMapper.getDatalimit(apiCode, taskId, aLong, preDate, nowDate);
                    int size = syncInfos.size();
                    if(size >0){
                        for (MarketingSyncInfo syncInfo : syncInfos) {
                            String requestBatch = syncInfo.getRequestBatch();
                            threadPool.submit(()->{
                                try {
                                    StringBuilder valuesStr = new StringBuilder();
                                    String s = DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
                                    List<MarketingSyncUser> list = marketingUserMapper.selectSyncUser(apiCode, requestBatch);
                                    for (int i = 0; i < list.size(); i++) {
                                        MarketingSyncUser marketingSyncUser = list.get(i);
                                        StrategyOfGroupDTO strategy = strategyOfGroupHashMap.get(marketingSyncUser.getGroupType());
                                        if(strategy==null||!StringUtils.isNotBlank(strategy.getBatchNumber())){
                                            continue;
                                        }
                                        JSONObject extendJson = new JSONObject();
                                        if(strategy.getConfigVO()!=null){
                                            JSONObject icData = null;
                                            if(StringUtils.isNotBlank(marketingSyncUser.getReserveField1())){
                                                try {
                                                    icData = JSON.parseObject(marketingSyncUser.getReserveField1());
                                                }catch (Exception ex){
                                                    log.error("用户上传数据非法的扩展信息：apiCode:{},id:{}"
                                                            ,marketingSyncUser.getApiCode(),marketingSyncUser.getId());
                                                }
                                            }
                                            for (BaseHead head : strategy.getConfigVO().getBaseHead()) {
                                                String str = "";
                                                if(ia.equals(head.getType())){
                                                    str = "";
                                                }else if(ib.equals(head.getType())){
                                                    switch (head.getName().toLowerCase()){
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
                                                            str =StringUtils.isBlank(marketingSyncUser.getFailType())
                                                                    &&StringUtils.isNotBlank(marketingSyncUser.getIdCard())
                                                                    ? DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance()
                                                                    .decode(marketingSyncUser.getIdCard()).getBytes())
                                                            :marketingSyncUser.getIdCard();
                                                            break;
                                                        case "id":
                                                            str =StringUtils.isBlank(marketingSyncUser.getFailType())
                                                                    &&StringUtils.isNotBlank(marketingSyncUser.getIdCard())
                                                                    ? DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance()
                                                                    .decode(marketingSyncUser.getIdCard()).getBytes())
                                                                    :marketingSyncUser.getIdCard();
                                                            break;
                                                        case "cell":
                                                            str =StringUtils.isBlank(marketingSyncUser.getFailType())
                                                                    &&StringUtils.isNotBlank(marketingSyncUser.getCell())
                                                                    ? DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance()
                                                                    .decode(marketingSyncUser.getCell()).getBytes())
                                                                    :marketingSyncUser.getCell();
                                                            break;
                                                        case "name":
                                                            str =StringUtils.isBlank(marketingSyncUser.getFailType())
                                                                    &&StringUtils.isNotBlank(marketingSyncUser.getName())
                                                                    ? DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance()
                                                                    .decode(marketingSyncUser.getName()).getBytes())
                                                                    :marketingSyncUser.getName();
                                                            break;
                                                        case "grouptype":
                                                            str = marketingSyncUser.getGroupType();
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
                                                }else if(ic.equals(head.getType())){
                                                    if(icData!=null){
                                                        str = icData.getString(head.getName());
                                                    }
                                                }else{
                                                    str = "";
                                                }
                                                extendJson.put(head.getName(),str);
                                            };
                                        }
                                        // api_code,batch_number,cus_num,cell,create_time,update_time,decodeFailType,status,extend_json
                                        valuesStr.append(String.format("('%s','%s','%s','%s','%s','%s','%s','%s','%s',%d,'%s')"
                                                ,apiCode,strategy.getBatchNumber(),marketingSyncUser.getCustNum()
                                                ,marketingSyncUser.getCell()
                                                ,StringUtils.isBlank(marketingSyncUser.getIdCard())?"":marketingSyncUser.getIdCard()
                                                ,StringUtils.isBlank(marketingSyncUser.getName())?"":marketingSyncUser.getName(),s,s
                                                ,marketingSyncUser.getFailType()==null?"":marketingSyncUser.getFailType()
                                                ,marketingSyncUser.getStatus()
                                                ,JSON.toJSONString(extendJson)));
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
                                }catch(Exception ex){
                                    log.error(ex.getMessage(),ex);
                                }
                            });
                        }

                        MarketingSyncInfo syncInfo = syncInfos.get(size - 1);
                        aLong = syncInfo.getId()+1;
                    }else{
                        dbMark = false;
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

                //region处理marketingTask
                groupTypes.forEach(t->{
                    StrategyOfGroupDTO strategyOfGroupDTO = strategyOfGroupHashMap.get(t);
                    if(strategyOfGroupDTO!=null){
                        int i = marketingUserMapper.countByPreUser(apiCode, taskId, strategyOfGroupDTO.getGroupType(),preDate);
                        int i1 = marketingUserMapper.countBySureUser(apiCode, strategyOfGroupDTO.getBatchNumber());
                        MarketingTask hasTask = marketingTaskMapper.getByBatchNumber(strategyOfGroupDTO.getBatchNumber());
                        if(hasTask!=null){
                            return;
                        }
                        if(i1>0) {
                            MarketingTask task = new MarketingTask();
                            task.setApiCode(apiCode);
                            task.setBatchNumber(strategyOfGroupDTO.getBatchNumber());
                            task.setMonitorStatus(1);
                            task.setStatus(1);
                            task.setStrategyId(strategyOfGroupDTO.getStrategyId());
                            task.setFileName(String.format("%s_%s", taskId, strategyOfGroupDTO.getGroupTypeShort()));
                            task.setCusBatch(taskId);
                            task.setActualNumber(i1);
                            task.setTaskNumber(i);
                            String s = DateUtils.format(new Date(), "yyyy-MM-dd");
                            task.setMonitorType(strategyOfGroupDTO.getExecType());
                            if (Integer.valueOf(1).equals(strategyOfGroupDTO.getExecType())) {
                                task.setStartDate(s);
                                task.setCloseDate(nextDate);
                            } else {
                                MarketingTask task1 = marketingTaskMapper.selectCycleTopByApiCode(apiCode);
                                if (task1 != null) {
                                    task.setStartDate(task1.getStartDate());
                                    task.setCloseDate(task1.getCloseDate());
                                } else {
                                    task.setStartDate(s);
                                    task.setCloseDate(strategyOfGroupDTO.getCycleEndDay());
                                }
                                task.setCycleDay(strategyOfGroupDTO.getCycleDay().toString());
                            }
                            task.setContextId(getTaskContextId());
                            marketingTaskMapper.insertTask(task);
                            BaseHeadConfigVO configVO = strategyOfGroupDTO.getConfigVO();
                            MarketingTaskExtend taskExtend = new MarketingTaskExtend();
                            taskExtend.setApiCode(apiCode);
                            taskExtend.setTaskId(Long.valueOf(task.getId()));
                            taskExtend.setCusTaskId(taskId);
                            taskExtend.setGroupType(t);
                            taskExtend.setCreateTime(new Date());
                            taskExtend.setUploadTime(preDate);
                            taskExtend.setExtendShowTitle(configVO!=null?Joiner.on(",").join(configVO.getShowBaseHead()):"");
                            marketingTaskExtendMapper.insertSelective(taskExtend);
                            TaskBatchnumberPreExample updateBatchExample = new TaskBatchnumberPreExample();
                            updateBatchExample.createCriteria().andBatchNumberEqualTo(strategyOfGroupDTO.getBatchNumber());
                            TaskBatchnumberPre updateBatchnumber = new TaskBatchnumberPre();
                            updateBatchnumber.setStatus(2);
                            taskBatchnumberPreMapper.updateByExampleSelective(updateBatchnumber,updateBatchExample);

                            try{
                                StringBuilder content = new StringBuilder();
                                content.append("apiCode：".concat(apiCode).concat("\r\n"))
                                        .append("taskId：".concat(taskId).concat("\r\n"))
                                        .append("groupType：".concat(t).concat("\r\n"))
                                        .append("time：".concat(nowDate).concat("\r\n"))
                                        .append("batchNumber：".concat(strategyOfGroupDTO.getBatchNumber()));
                                alarmClient.sendAlarm(content.toString(),"api人员数据生成任务",appName,secretKey,
                                        Constants.sendCodeMap.get("uploadSuccess"));
                            }catch (Exception ex){
                                log.error(ex.getMessage(),ex);
                            }
                        }
                    }
                });
                //endregion
            }
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result pushToDb(String code){
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
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        for (MarketingCustomer marketingCustomer : marketingCustomers) {
            String apiCode = marketingCustomer.getApiCode();
            tableCreateService.createMarketingSyncUserTable(apiCode);
            tableCreateService.createMarketingUserTable(apiCode);
            Result<List<CustomerScoreRuleVO>> scoreConfig = iRuleConfigService.getScoreConfig(apiCode);
            Result<List<CustomerSoleRuleVO>> soleConfig = iRuleConfigService.getSoleConfig(apiCode);
            if(!ResultCode.SUCCESS.getValue().equals(scoreConfig.getCode())){
                continue;
            }
            List<CustomerScoreRuleVO> scoreConfigList = scoreConfig.getData();
            for (CustomerScoreRuleVO customerScoreRuleVO : scoreConfigList) {
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
                int taskNum = syncInfoMapper.countByPreUserWithRule(apiCode, sTimeStr, eTimeStr, conditionRes.getData());
                if(taskNum>0){
                    String time = LocalDateTime.parse(eTimeStr,ymdhms).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    Result<String> batchNumberRes = buildBatchNumber(apiCode
                            ,customerScoreRuleVO.getId().toString(),customerScoreRuleVO.getRuleNameShort()
                            ,time);
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
                        .getMinIdByRuleScore(apiCode, sTimeStr, eTimeStr, conditionRes.getData());
                ExecutorService threadPool = BrExecutors.getThreadPool(20, 50);
                boolean execMark = true;
                while (execMark) {
                    String batchNumber = number;
                    List<MarketingSyncUser> syncUserByRuleScore = syncInfoMapper
                            .getSyncUserByRuleScore(apiCode, sTimeStr, eTimeStr, minId, conditionRes.getData());
                    if(syncUserByRuleScore.size()<=0){
                        execMark = false;
                        continue;
                    }
                    for (int i = 0; i < syncUserByRuleScore.size(); i++) {
                        MarketingSyncUser marketingSyncUser = syncUserByRuleScore.get(i);
                        if (i == syncUserByRuleScore.size() - 1) {
                            minId = marketingSyncUser.getId() + 1;
                        }
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
        return new Result().setCode(ResultCode.SUCCESS.getValue());
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
            Result<List<CustomerSoleRuleVO>> soleConfig = iRuleConfigService.getSoleConfig(apiCode);
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
                            ,time);
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
                ExecutorService threadPool = BrExecutors.getThreadPool(20, 50);
                boolean execMark = true;
                while (execMark) {
                    String batchNumber = number;
                    List<MarketingSyncUser> syncUserByRuleScore = syncInfoMapper
                            .getSyncUserByRuleScore(apiCode, startTimeJob, endTimeJob, minId, conditionRes.getData());
                    if(syncUserByRuleScore.size()<=0){
                        execMark = false;
                        continue;
                    }
                    for (int i = 0; i < syncUserByRuleScore.size(); i++) {
                        MarketingSyncUser marketingSyncUser = syncUserByRuleScore.get(i);
                        if (i == syncUserByRuleScore.size() - 1) {
                            minId = marketingSyncUser.getId() + 1;
                        }
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
    public Result<String> buildBatchNumber(String apiCode, String cusBatch, String groupType, String time) {
        boolean res = false;
        int i = 0;
        while (!res){
            Result<String> stringResult = this.buildBatchNumberCore(apiCode, cusBatch, groupType, time);
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

    private Result<String> buildBatchNumberCore(String apiCode, String cusBatch, String groupType, String time) {
        String key = redisBatchNumberKey.concat(":")
                .concat(apiCode).concat(":")
                .concat(cusBatch).concat(":")
                .concat(groupType).concat(":")
                .concat(time);
        String s = redisChgService.get(key);
        if(StringUtils.isNotBlank(s)){
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
            Result<String> stringResult = this.buildBatchNumber(apiCode, cusBatch, marketingSyncUser.getGroupType(), "");
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
}
