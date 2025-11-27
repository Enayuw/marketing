package com.br.marketing.service.Impl.tongcheng;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.tongcheng.TongChengAgentMktClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.tongcheng.TongChengPushQueryQuantityDTO;
import com.br.marketing.entity.TongChengAgent;
import com.br.marketing.entity.TongChengAgentExample;
import com.br.marketing.mapper.TongChengAgentMapper;
import com.br.marketing.service.LocalFileService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author guangxiu.li
 * @Description 同程集团迁移可营销名单推送客户实现类
 * @dateTime 2024/01/25 16:13
 */
@Service
@Slf4j
public class TongChengOperationPushToCustomerServiceImpl implements TongChengOperationPushToCustomerService {
    @Autowired
    RedisChgService redisChgService;

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    TongChengAgentMapper tongChengAgentMapper;

    @Autowired
    TongChengAgentMktClient tongChengAgentMktClient;

    @Resource
    LocalFileService localFileService;

    private static final int BATCH_SIZE = 2000;

    @Override
    public void process(String apiCode) {
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(5, 5);
        Long minId = null;
        int num = marketingCommonConfig.getTongChengGroupOperationNum();
        boolean hasCollectedDate = false;
        Date pushStartTime = new Date();
        while (true) {
            try {
                if (marketingCommonConfig.getTongChengGroupOperationThreadNum() != null) {
                    ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, marketingCommonConfig.getTongChengGroupOperationThreadNum());
                }
                List<TongChengAgent> tongchengAgentList = tongChengAgentMapper.tongChengGroupOperationDataPage(minId, apiCode, num);
                if (tongchengAgentList.size() <= 0) {
                    break;
                }
                hasCollectedDate = true;
                minId = tongchengAgentList.get(tongchengAgentList.size() - 1).getId();
                List<List<TongChengAgent>> partition = Lists.partition(tongchengAgentList, BATCH_SIZE);
                partition.forEach((List<TongChengAgent> p) -> {
                    pool.submit(() -> buildDataAndPush(p, apiCode));
                });
            } catch (Exception e) {
                log.error("同程集团运营名单捞取异常！", e);
            }
        }
        Date pushEndTime = new Date();
        try {
            pool.shutdown();
            while (!pool.awaitTermination(5L, TimeUnit.SECONDS)) {
                log.warn("线程终止");
            }
        } catch (Exception ex) {
            pool.shutdownNow();
            log.error(ex.getMessage(), ex);
            Thread.currentThread().interrupt();
        }

        // refreshLocalFile
        if(hasCollectedDate) {
            refreshLocalFile(apiCode, pushStartTime, pushEndTime);
        }
    }


    private void buildDataAndPush(List<TongChengAgent> tongchengAgents, String apiCode) {
        List<Map<String, String>> dataLists = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        ThreadPoolExecutor thread = BrExecutors.getThreadPool(50, 50);
        List<Callable<TongChengAgent>> callableList = new ArrayList<>();
        try {
            for (TongChengAgent data : tongchengAgents) {
                callableList.add(() -> processAgent(data, apiCode));
            }
            List<Future<TongChengAgent>> futures = thread.invokeAll(callableList);
            futures.forEach((Future<TongChengAgent> t) -> {
                try {
                    TongChengAgent agent = t.get();
                    if (agent.getPushStatus() == 1){
                        HashMap<String, String> map = new HashMap<>();
                        map.put("mobileMd5",agent.getMobileMd5());
                        dataLists.add(map);
                        ids.add(agent.getId());
                    }

                } catch (InterruptedException e) {
                    log.error("同城集团运营名单数据拼接异常！" , e);
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e){
                    log.error("同城集团运营名单数据拼接异常！" , e);
                }
            });
        } catch (InterruptedException e) {
            log.error("同程集团运营名单数据组装线程异常！" , e);
            Thread.currentThread().interrupt();
        }
        try {
            thread.shutdown();
            while (!thread.awaitTermination(5L, TimeUnit.SECONDS)) {
                log.warn("线程终止");
            }
        } catch (InterruptedException ex) {
            thread.shutdownNow();
            log.error("同程集团运营名单数据组装线程关闭异常！",ex);
            Thread.currentThread().interrupt();
        }
        if (dataLists.isEmpty()) {
            log.warn("同程本批次可推送数据为0！");
            return;
        }
        Result result = tongChengAgentMktClient.pushToTongChengAgentMkt(dataLists, apiCode, null);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            //更新成功
            updateStatus(ids, 2, result.getMessage());
        } else {
            //更新失败
            updateStatus(ids, 3, result.getMessage());
        }

    }

    private TongChengAgent processAgent(TongChengAgent data, String apiCode) {
        try {
            String mobileMd5 = data.getMobileMd5();
            Integer createDate = data.getCreateDate();
            // 获取redis 锁
            String key = RedisKeyConstant.PUSH_TONG_CHENG_LOCK.concat(":")
                    .concat(apiCode)
                    .concat(mobileMd5);
            String value = UUID.randomUUID().toString();

            boolean lock = redisChgService.lock(key, value, 3000L);
            if (lock == true) {
                //查询当天是否推送过
                TongChengAgentExample tongChengAgentExample = new TongChengAgentExample();
                tongChengAgentExample.createCriteria()
                        .andApiCodeEqualTo(apiCode)
                        .andCreateDateEqualTo(createDate)
                        .andMobileMd5EqualTo(mobileMd5)
                        .andIsDeleteEqualTo(0)
                        .andPushStatusIn(Arrays.asList(1, 2, 3));
                if (tongChengAgentMapper.countByExample(tongChengAgentExample) == 0) {
                    data.setPushStatus(1);
                } else {
                    data.setStatus(3);
                    data.setDataMessage("数据重复未推送");
                }
                // 处理返回结果
                data.setUpdateTime(new Date());
                tongChengAgentMapper.updateByPrimaryKeySelective(data);
                // 解锁
                redisChgService.unlock(key, value);
            }
        } catch (Exception e) {
            log.error("数据组装异常！", e.getMessage(), e);
        }
        return data;
    }

    private void updateStatus(List<Long> ids, int status, String message) {
        if (ids.size() > 0) {
            TongChengAgentExample updateExample = new TongChengAgentExample();
            updateExample.createCriteria().andIdIn(ids);
            TongChengAgent record = new TongChengAgent();
            record.setPushStatus(status);
            record.setDataMessage(message);
            tongChengAgentMapper.updateByExampleSelective(record, updateExample);
        }
    }

    /**
     * 已确认，每个localId每天只执行1次，刷新逻辑为直接更新PushNumber字段
     * 后续业务有变更，需要更新此方法
     */
    private void refreshLocalFile(String apiCode, Date pushStartTime, Date pushEndTime){
        try {
            TongChengPushQueryQuantityDTO params = new TongChengPushQueryQuantityDTO();
            params.setApiCode(apiCode);
            params.setPushStatus(2);
            params.setStatus(1);
            String curTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00"));
            params.setStartTime(curTimeStr);

            List<Long> localIdList = tongChengAgentMapper.queryLocalFileIdList(params);
            if(localIdList == null || localIdList.size() < 1){
                return;
            }

            List<Map<String, Object>> queryQuantityList = tongChengAgentMapper.queryQuantityGroupByLocalId(params);
            if(queryQuantityList == null || queryQuantityList.size() < 1){
                queryQuantityList = new ArrayList<Map<String, Object>>();
            }
            Map<String, Long> localIdToQuantityMap = queryQuantityList.stream().collect(Collectors.toMap(
                    (data1) -> String.valueOf(data1.get("localId")),
                    (data2) -> Long.parseLong(String.valueOf(data2.get("quantity")))
            ));

            List<Map<String, Object>> quantityList = new ArrayList<>();
            for(Long localId :localIdList){
                Map<String, Object> map = new HashMap<>();
                map.put("localId", localId);
                if (localIdToQuantityMap.get(String.valueOf(localId)) != null) {
                    map.put("quantity", localIdToQuantityMap.get(String.valueOf(localId)));
                } else {
                    map.put("quantity", 0L);
                }
                quantityList.add(map);
            }
            localFileService.refreshPushNumber(quantityList, pushStartTime, pushEndTime);
        }catch (Exception e){
            log.warn("更新推送量级异常", e);
        }
    }

}
