package com.br.marketing.service.Impl;

import com.br.common.util.BrExecutors;
import com.br.common.util.DateUtils;
import com.br.marketing.client.IceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.dto.StrategyOfGroupDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.IApiToDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public Result pushToDb() {
        Date date = new Date();
        String nowDate = DateUtils.format(date, "yyyy-MM-dd");
        String preDate = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andStatusEqualTo(new Byte("1"));
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);

        MarketingSyncInfoExample syncInfoExample = new MarketingSyncInfoExample();
        syncInfoExample.createCriteria().andCreateTimeGreaterThanOrEqualTo(DateUtils.getDate2(preDate))
                .andCreateTimeLessThan(DateUtils.getDate2(nowDate));
        List<MarketingSyncInfo> marketingSyncInfos = syncInfoMapper.selectByExample(syncInfoExample);

        for (MarketingCustomer marketingCustomer : marketingCustomers) {
            String apiCode = marketingCustomer.getApiCode();
            boolean b = marketingSyncInfos.stream().anyMatch(t -> t.getApiCode().equals(apiCode)
                    && t.getStatus().equals(1));
            if(b){
                continue;
            }

            List<String> groupTypes = marketingUserMapper.selectGroupByCodeAndTime(apiCode, preDate, nowDate);
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
                StringBuilder sbStrategyOfGroup = new StringBuilder();
                sbStrategyOfGroup.append("( case");
                ArrayList<StrategyOfGroupDTO> strategyOfGroupDTOS = new ArrayList<>();
                groupStrategyConfigs.forEach(t->{
                    String yyyyMMddHHmmss = DateUtils.format(new Date(), "yyyyMMddHHmmss");
                    int i = (int) ((Math.random()*9+1)*1000);
                    String batchNumber = String.format("%s_%s_%s_%d", apiCode, yyyyMMddHHmmss,taskId,i);
                    StrategyOfGroupDTO strategyOfGroupDTO = new StrategyOfGroupDTO();
                    strategyOfGroupDTO.setGroupType(t.getGroupType());
                    strategyOfGroupDTO.setBatchNumber(batchNumber);
                    strategyOfGroupDTO.setStrategyId(t.getStrategyId());
                    strategyOfGroupDTOS.add(strategyOfGroupDTO);
                    sbStrategyOfGroup.append(String.format(" when cus_batch= '%s' and group_type = '%s' then '%s'",taskId,t.getGroupType(),batchNumber));

                });
                sbStrategyOfGroup.append(" else '' end ) as batch_number");
                for (MarketingSyncInfo syncInfo : syncInfos.stream().filter(sync->sync.getCusBatch().equals(taskId)).collect(Collectors.toList())) {
                    String requestBatch = syncInfo.getRequestBatch();
                    threadPool.submit(()->{
                        String s = DateUtils.formatForDate2(new Date());
                        marketingUserMapper.insertSelectByRequestId(apiCode,sbStrategyOfGroup.toString(),s,requestBatch);
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
                        task.setMonitorType(Integer.valueOf(IceClient.getMerchantParam(apiCode).getCallMethod()));
                        task.setMonitorStatus(0);
                        task.setStatus(2);
                        task.setStrategyId(strategyOfGroupDTO.getStrategyId());
                        task.setFileName(String.format("%s_%s_%s_%s",apiCode,taskId,strategyOfGroupDTO.getStrategyId(),strategyOfGroupDTO.getBatchNumber()));
                        task.setCusBatch(taskId);
                        int i = marketingUserMapper.countByPreUser(apiCode, taskId, strategyOfGroupDTO.getGroupType(), preDate, nowDate);
                        int i1 = marketingUserMapper.countBySureUser(apiCode, strategyOfGroupDTO.getBatchNumber());
                        task.setActualNumber(i);
                        task.setTaskNumber(i1);
                        marketingTaskMapper.insertTask(task);
                    }
                });
            }
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }
}
