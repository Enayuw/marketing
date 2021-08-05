package com.br.marketing.service.Impl;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    final SimpleDateFormat simpleDateFormatOfymd=new SimpleDateFormat("yyyy-MM-dd");

    private final static String redisElasticJobKey = "elasticjob:contextid";

    private final static String redisBatchNumberKey = "batchnumber:pre";

    @Autowired
    IProductResultSimpleService iProductResultSimpleService;

    @Override
    public Long getTaskContextId(){
        return redisChgService.incr(redisElasticJobKey);
    }

    @Override
    public Result pushToDb() {
        Date date = new Date();
        String nowDate = DateUtils.format(date, "yyyy-MM-dd");
        String preDate = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String nextDate = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Date preDateDay = null;
        Date nowDateDay = null;
        try {
            preDateDay = simpleDateFormatOfymd.parse(preDate);
            nowDateDay = simpleDateFormatOfymd.parse(nowDate);
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
                ArrayList<StrategyOfGroupDTO> strategyOfGroupDTOS = new ArrayList<>();
                HashMap<String,String> strategyOfGroupHashMap = new HashMap();
                groupTypes.forEach(t->{
                    Optional<GroupStrategyConfig> first = groupStrategyConfigs.stream().filter(k -> k.getGroupType().equals(t)).findFirst();
                    if (first.isPresent()) {
                        Result<String> stringResult = buildBatchNumber(apiCode, taskId, t, DateUtils.format(new Date(), "yyyy-MM-dd"));
                        if(ResultCode.SUCCESS.getValue().equals(stringResult.getCode())) {
                            StrategyOfGroupDTO strategyOfGroupDTO = new StrategyOfGroupDTO();
                            BeanUtils.copyProperties(first.get(), strategyOfGroupDTO);
                            strategyOfGroupDTO.setBatchNumber(stringResult.getData());
                            strategyOfGroupDTOS.add(strategyOfGroupDTO);
                            strategyOfGroupHashMap.put(t, stringResult.getData());
                    }
                }
                });
                //endregion

                //region 处理marketingUser
                ExecutorService threadPool = BrExecutors.getThreadPool(20, 20);
                Long aLong = syncInfoMapper.minSyncId(apiCode, taskId, preDate, nowDate);
                boolean dbMark = true;
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
                                        String batchNumber = strategyOfGroupHashMap.get(marketingSyncUser.getGroupType());
                                        if(!StringUtils.isNotBlank(batchNumber)){
                                            continue;
                                        }
                                        JSONObject extendJson = new JSONObject();
                                        extendJson.put("groupType",marketingSyncUser.getGroupType());
                                        extendJson.put("taskId",marketingSyncUser.getCusBatch());
                                        // api_code,batch_number,cus_num,cell,create_time,update_time,decodeFailType,status,extend_json
                                        valuesStr.append(String.format("('%s','%s','%s','%s','%s','%s','%s','%s','%s',%d,'%s')"
                                                ,apiCode,batchNumber,marketingSyncUser.getCustNum()
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
                    Optional<StrategyOfGroupDTO> first = strategyOfGroupDTOS.stream()
                            .filter(k -> k.getGroupType().equals(t)).findFirst();
                    if(first.isPresent()){
                        StrategyOfGroupDTO strategyOfGroupDTO = first.get();
                        int i = marketingUserMapper.countByPreUser(apiCode, taskId, strategyOfGroupDTO.getGroupType(),preDate);
                        int i1 = marketingUserMapper.countBySureUser(apiCode, strategyOfGroupDTO.getBatchNumber());
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

                            Result<String> baseHeadInfo = iProductResultSimpleService.getBaseHeadInfo(apiCode, t);
                            if (ResultCode.SUCCESS.getValue().equals(baseHeadInfo.getCode())) {
                                MarketingTaskExtend taskExtend = new MarketingTaskExtend();
                                taskExtend.setApiCode(apiCode);
                                taskExtend.setTaskId(Long.valueOf(task.getId()));
                                taskExtend.setCusTaskId(taskId);
                                taskExtend.setGroupType(t);
                                taskExtend.setCreateTime(new Date());
                                taskExtend.setUploadTime(preDate);
                                marketingTaskExtendMapper.insertSelective(taskExtend);
                            }

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
            String yyyyMMddHHmmss = DateUtils.format(new Date(), "yyyyMMddHHmmss");
            int i = (int) ((Math.random()*9+1)*1000);
            String batchNumber = String.format("%s_%s_%d", apiCode, yyyyMMddHHmmss,i);
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
