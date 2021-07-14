package com.br.marketing.service.Impl;
import java.util.Date;

import com.br.common.util.BrExecutors;
import com.br.common.util.DateUtils;
import com.br.marketing.client.IceClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.Constants;
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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ApiToDbServiceImpl  implements IApiToDbService {

    private static final Logger log = LoggerFactory.getLogger(ApiToDbServiceImpl.class);
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

    final SimpleDateFormat simpleDateFormatOfymd=new SimpleDateFormat("yyyy-MM-dd");

    private final static String redisElasticJobKey = "elasticjob:contextid";

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

        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andStatusEqualTo(new Byte("1"));
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);



        for (MarketingCustomer marketingCustomer : marketingCustomers) {
            String apiCode = marketingCustomer.getApiCode();
            MarketingSyncInfoExample syncInfoExample = new MarketingSyncInfoExample();
            try {
                syncInfoExample.createCriteria()
                        .andApiCodeEqualTo(apiCode)
                        .andCreateTimeGreaterThanOrEqualTo(simpleDateFormatOfymd.parse(preDate))
                        .andCreateTimeLessThan(simpleDateFormatOfymd.parse(nowDate))
                .andIsUploadEqualTo(1);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            List<MarketingSyncInfo> marketingSyncInfos = syncInfoMapper.selectByExample(syncInfoExample);

            boolean b = marketingSyncInfos.stream().anyMatch(t -> t.getStatus().equals(1));
            if(b){
                continue;
            }
            if(marketingSyncInfos.size()<=0){
                continue;
            }

            List<String> groupTypes = marketingUserMapper.selectGroupByCodeAndTime(apiCode, preDate, nextDate);
            GroupStrategyConfigExample configExample = new GroupStrategyConfigExample();
            configExample.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo(1);
            List<GroupStrategyConfig> groupStrategyConfigs = groupStrategyConfigMapper.selectByExample(configExample);

            String tableName="b_marketing_user_"+apiCode;
            marketingUserMapper.createUserTable(tableName);
            List<MarketingSyncInfo> syncInfos = marketingSyncInfos.stream()
                    .filter(t -> t.getApiCode().equals(apiCode)
                            && Arrays.asList(2, 4).contains(t.getStatus())).collect(Collectors.toList());
            ExecutorService threadPool = BrExecutors.getThreadPool(20, 20);
            List<String> taskIds = syncInfos.stream().map(t -> t.getCusBatch()).distinct().collect(Collectors.toList());
            for (String taskId : taskIds) {
                ArrayList<StrategyOfGroupDTO> strategyOfGroupDTOS = new ArrayList<>();
                HashMap<String,String> strategyOfGroupHashMap = new HashMap();
                groupStrategyConfigs.forEach(t->{
                    String yyyyMMddHHmmss = DateUtils.format(new Date(), "yyyyMMddHHmmss");
                    int i = (int) ((Math.random()*9+1)*1000);
                    String batchNumber = String.format("%s_%s_%d", apiCode, yyyyMMddHHmmss,i);
                    StrategyOfGroupDTO strategyOfGroupDTO = new StrategyOfGroupDTO();
                    BeanUtils.copyProperties(t,strategyOfGroupDTO);
                    strategyOfGroupDTO.setBatchNumber(batchNumber);
                    strategyOfGroupDTOS.add(strategyOfGroupDTO);
                    strategyOfGroupHashMap.put(t.getGroupType(),batchNumber);

                });
                for (MarketingSyncInfo syncInfo : syncInfos.stream().filter(sync->sync.getCusBatch().equals(taskId)).collect(Collectors.toList())) {
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
                        }catch(Exception ex){
                            log.error(ex.getMessage(),ex);
                        }
                    });
                }
                threadPool.shutdown();
                boolean isContiue = true;
                while (isContiue){
                    if(threadPool.isTerminated()){
                        isContiue= false;
                    }else{
                        try {
                            Thread.sleep(3000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                groupTypes.forEach(t->{
                    Optional<StrategyOfGroupDTO> first = strategyOfGroupDTOS.stream()
                            .filter(k -> k.getGroupType().equals(t)).findFirst();
                    if(first.isPresent()){
                        StrategyOfGroupDTO strategyOfGroupDTO = first.get();
                        MarketingTask task =new MarketingTask();
                        task.setApiCode(apiCode);
                        task.setBatchNumber(strategyOfGroupDTO.getBatchNumber());
                        //todo 暂时去掉监控
//                        task.setMonitorType(Integer.valueOf(IceClient.getMerchantParam(apiCode).getCallMethod()));
                        task.setMonitorStatus(1);
                        task.setStatus(1);
                        task.setStrategyId(strategyOfGroupDTO.getStrategyId());
                        task.setFileName(String.format("%s",strategyOfGroupDTO.getGroupTypeShort()));
                        task.setCusBatch(taskId);
                        int i = marketingUserMapper.countByPreUser(apiCode, taskId, strategyOfGroupDTO.getGroupType(),preDate);
                        int i1 = marketingUserMapper.countBySureUser(apiCode, strategyOfGroupDTO.getBatchNumber());
                        task.setActualNumber(i1);
                        task.setTaskNumber(i);
                        String s = simpleDateFormatOfymd.format(new Date());
                        task.setMonitorType(strategyOfGroupDTO.getExecType());
                        if(new Integer(1).equals(strategyOfGroupDTO.getExecType())){
                            task.setStartDate(s);
                            task.setCloseDate(s);
                        }else{
                            MarketingTask task1 = marketingTaskMapper.selectCycleTopByApiCode(apiCode);
                            if(task1 != null){
                                task.setStartDate(task1.getStartDate());
                                task.setCloseDate(task1.getCloseDate());
                            }else {
                                task.setStartDate(s);
                                task.setCycleDay(strategyOfGroupDTO.getCycleDay().toString());
                                String e = simpleDateFormatOfymd.format(addDay(new Date(), strategyOfGroupDTO.getCycleDay() * 10));
                                task.setCloseDate(e);
                            }
                        }
                        task.setContextId(getTaskContextId());
                        marketingTaskMapper.insertTask(task);

                        Result<String> baseHeadInfo = iProductResultSimpleService.getBaseHeadInfo(apiCode, t);
                        if(ResultCode.SUCCESS.getValue().equals(baseHeadInfo.getCode())){
                            MarketingTaskExtend taskExtend = new MarketingTaskExtend();
                            taskExtend.setApiCode(apiCode);
                            taskExtend.setTaskId(Long.valueOf(task.getId()));
                            taskExtend.setCusTaskId(taskId);
                            taskExtend.setGroupType(t);
                            taskExtend.setCreateTime(new Date());
                            marketingTaskExtendMapper.insertSelective(taskExtend);
                        }
                    }
                });
            }
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private Date addDay(Date date, Integer addDays) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, addDays);
        Date time = c.getTime();
        return time;
    }
}
