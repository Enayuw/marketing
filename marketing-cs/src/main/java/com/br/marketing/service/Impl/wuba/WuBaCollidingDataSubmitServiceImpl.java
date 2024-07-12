package com.br.marketing.service.Impl.wuba;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.wuba.WuBaServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.WubaCollidingDataLog;
import com.br.marketing.entity.WubaCollidingDataRob;
import com.br.marketing.mapper.WubaCollidingBatchNoMapper;
import com.br.marketing.mapper.WubaCollidingDataLogMapper;
import com.br.marketing.mapper.WubaCollidingDataRobMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.google.api.client.util.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
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

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        // 判断撞库开关
        if (!marketingCommonConfig.getWuBaCollidingDataSwitch()) {
            return;
        }

        Integer pagesize = marketingCommonConfig.getWuBaCollidingDataSubmitPageSize();
        List<WubaCollidingDataRob> robs = wubaCollidingDataRobMapper.selectCollidingData(pagesize);
        if (CollectionUtils.isEmpty(robs)) {
            return;
        }

        List<String> cells = robs.stream().map(WubaCollidingDataRob::getCell).collect(Collectors.toList());

        Result result = wuBaServiceClient.submitCredentialStuffingList(cells);

        if (Objects.equals(result.getCode(), ResultCode.FAIL.getValue())) {
            JSONObject resMap = JSONObject.parseObject(result.getData().toString());
            String title = "58提交撞库名单，调用客户接口异常";
            String msg = title + "，响应内容：" + JSON.toJSONString(resMap);
            wuBaServiceClient.sendDingDingAlert("58提交撞库名单，调用客户接口异常", msg);
            return;
        }

        // 保存批次号表
        String batchNo = result.getData().toString();
        wubaCollidingBatchNoMapper.saveDataByBatchNo(batchNo, 1);

        // 更新非周期表
        wubaCollidingDataRobMapper.batchUpdatePushTimeById(robs);

        // 保存log表
        List<WubaCollidingDataLog> logList = Lists.newArrayList();
        for (WubaCollidingDataRob rob : robs) {
            WubaCollidingDataLog log = new WubaCollidingDataLog();
            log.setDataId(rob.getId());
            log.setCell(rob.getCell());
            log.setBatchNo(batchNo);
            logList.add(log);
        }
        wubaCollidingDataLogMapper.batchSaveByBatchNo(logList);
    }
}
