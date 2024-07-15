package com.br.marketing.service.Impl.wuba;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.wuba.WuBaServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.WubaCollidingDataLog;
import com.br.marketing.entity.WubaCollidingDataRob;
import com.br.marketing.mapper.WubaCollidingBatchNoMapper;
import com.br.marketing.mapper.WubaCollidingDataLogMapper;
import com.br.marketing.mapper.WubaCollidingDataRobMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Description 58提交撞库实现类
 * @Author hong.chen
 * @CreateTime 2024/07/10
 */
@Service
@Slf4j
public class WuBaCollidingDataSubmitServiceImpl implements WuBaCollidingDataSubmitService {
    @Resource
    WuBaServiceClient wuBaServiceClient;
    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    WubaCollidingDataRobMapper wubaCollidingDataRobMapper;
    @Resource
    WubaCollidingBatchNoMapper wubaCollidingBatchNoMapper;
    @Resource
    WubaCollidingDataLogMapper wubaCollidingDataLogMapper;
    private final static int PARTATION_SIZE = 50;
    ThreadPoolExecutor pool = BrExecutors.getThreadPool(20, 20);

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        // 判断撞库开关
        if (!marketingCommonConfig.getWuBaCollidingDataSwitch()) {
            return;
        }

        Integer pagesize = marketingCommonConfig.getWuBaCollidingDataSubmitPageSize();
        marketingCommonConfig.getWubaCollidingApiCodes().forEach((String apiCode) -> {
            List<WubaCollidingDataRob> robs = wubaCollidingDataRobMapper.selectCollidingData(pagesize, apiCode);
            if (CollectionUtils.isEmpty(robs)) {
                return;
            }

            List<String> cells = robs.stream().map(WubaCollidingDataRob::getCell).collect(Collectors.toList());

            long start = System.currentTimeMillis();
            Result result = wuBaServiceClient.submitCredentialStuffingList(cells);
            log.warn("58提交撞库名单，接口耗时：{}ms", System.currentTimeMillis() - start);

            if (Objects.equals(result.getCode(), ResultCode.FAIL.getValue())) {
                JSONObject resMap = JSONObject.parseObject(result.getData().toString());
                String title = "58提交撞库名单，调用客户接口异常";
                String msg = title + "，响应内容：" + JSON.toJSONString(resMap);
                wuBaServiceClient.sendDingDingAlert("58提交撞库名单，调用客户接口异常", msg);
                return;
            }

            // 保存批次号表
            String batchNo = result.getData().toString();
            log.warn("58提交撞库名单，客户返回batchNo：{}", batchNo);
            wubaCollidingBatchNoMapper.saveDataByBatchNo(batchNo, 1, apiCode);

            // 更新非周期表
            wubaCollidingDataRobMapper.batchUpdatePushTimeById(robs);

            // 保存log表
            pool.setCorePoolSize(marketingCommonConfig.getWubaCollidingDataSyncThreadNum());
            pool.setMaximumPoolSize(marketingCommonConfig.getWubaCollidingDataSyncThreadNum());
            List<List<WubaCollidingDataRob>> partitions = com.google.common.collect.Lists.partition(robs, PARTATION_SIZE);
            for (List<WubaCollidingDataRob> partition : partitions) {
                pool.submit(() -> batchSaveLog(apiCode, partition, batchNo));
            }
        });
    }

    private void batchSaveLog(String apiCode, List<WubaCollidingDataRob> robs, String batchNo) {
        try {
            List<WubaCollidingDataLog> logs = robs.stream().map((WubaCollidingDataRob rob) -> {
                WubaCollidingDataLog log = new WubaCollidingDataLog();
                log.setDataId(rob.getId());
                log.setCell(rob.getCell());
                log.setBatchNo(batchNo);
                log.setApiCode(apiCode);
                return log;
            }).collect(Collectors.toList());

            wubaCollidingDataLogMapper.batchSaveByBatchNo(logs);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage()
                    , "58提交撞库，子线程保存撞库日志处理异常"), e);
        }
    }
}
