package com.br.marketing.service.Impl.tongcheng;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.tongcheng.TongChengAgentMktClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.TongChengAgentMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    private static final int BATCH_SIZE = 2000;

    @Override
    public void process(String apiCode) {
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(5, 5);
        Long minId = null;
        int num = marketingCommonConfig.getTongChengGroupOperationNum();
        Boolean isContiue = Boolean.TRUE;
        while (isContiue) {
            try {
                if (marketingCommonConfig.getTongChengGroupOperationThreadNum() != null) {
                    pool.setCorePoolSize(marketingCommonConfig.getTongChengGroupOperationThreadNum());
                    pool.setMaximumPoolSize(marketingCommonConfig.getTongChengGroupOperationThreadNum());
                }
                List<TongChengAgent> tongchengAgentList = tongChengAgentMapper.tongChengGroupOperationDataPage(minId, apiCode, num);
                if (tongchengAgentList.size() <= 0) {
                    isContiue = Boolean.FALSE;
                    continue;
                }
                minId = tongchengAgentList.get(tongchengAgentList.size() - 1).getId();
                List<List<TongChengAgent>> partition = Lists.partition(tongchengAgentList, BATCH_SIZE);
                partition.forEach(p -> {
                    pool.submit(() -> buildDataAndPush(p, apiCode));
                });
            } catch (Exception e) {
                log.error("同程集团运营名单捞取异常！", e);
            }
        }

        try {
            pool.shutdown();
            while (!pool.awaitTermination(5L, TimeUnit.SECONDS)) {
            }
        } catch (Exception ex) {
            pool.shutdownNow();
            log.error(ex.getMessage(), ex);
        }
    }


    private void buildDataAndPush(List<TongChengAgent> tongchengAgents, String apiCode) {
        try {
            List<Map<String, String>> dataLists = new ArrayList<>();
            List<Long> ids = new ArrayList<>();
            for (TongChengAgent data : tongchengAgents) {
                Map<String, String> map = new HashMap<>();
                String mobileMd5 = data.getMobileMd5();
                Integer createDate = data.getCreateDate();
                // 获取redis 锁
                String key = RedisKeyConstant.pushTongChengLock.concat(":")
                        .concat(apiCode)
                        .concat(mobileMd5);
                String value = UUID.randomUUID().toString();

                redisChgService.lock(key, value);
                TongChengAgent tongChengAgent = new TongChengAgent();
                tongChengAgent.setId(data.getId());
                //查询当天是否推送过
                TongChengAgentExample tongChengAgentExample = new TongChengAgentExample();
                tongChengAgentExample.createCriteria()
                        .andApiCodeEqualTo(apiCode)
                        .andCreateDateEqualTo(createDate)
                        .andMobileMd5EqualTo(mobileMd5)
                        .andIsDeleteEqualTo(0)
                        .andPushStatusIn(Arrays.asList(1,2,3));
                if (tongChengAgentMapper.countByExample(tongChengAgentExample) == 0) {
                    tongChengAgent.setPushStatus(1);
                    map.put("mobileMd5", mobileMd5);
                    dataLists.add(map);
                    ids.add(data.getId());
                } else {
                    tongChengAgent.setStatus(3);
                    tongChengAgent.setDataMessage("数据重复未推送");
                }
                // 处理返回结果
                tongChengAgent.setUpdateTime(new Date());
                tongChengAgentMapper.updateByPrimaryKeySelective(tongChengAgent);
                // 解锁
                redisChgService.unlock(key, value);
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

        } catch (Exception ex) {
            log.error(String.format("同程集团运营名单推送客户接口子线程异常", ex.getMessage()), ex);
        }
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

}
