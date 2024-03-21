package com.br.marketing.service.Impl.xc;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.XieChengCollidingDataLoopCycle;
import com.br.marketing.entity.XieChengCollidingDataRob;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class XieChengCollidingResultHandleService {
    @Resource
    private XieChengCollidingDataLoopCycleMapper xieChengCollidingDataLoopCycleMapper;
    @Resource
    private XieChengCollidingDataRobMapper xieChengCollidingDataRobMapper;
    @Resource
    private RabbitMqProducter rabbitMqProducter;

    @Transactional(rollbackFor = Exception.class)
    public void robDataHandle(List<Result> pushResults, Map<String, XieChengCollidingDataRob> cellMap) {
        for (Result pushResult : pushResults) {
            JSONObject resultJson = JSONObject.parseObject(pushResult.getMessage());
            boolean success = pushResult.getCode().equals(ResultCode.SUCCESS.getValue());
            JSONArray returnDataList = resultJson.getJSONArray("data");
            for (int i = 0; i < returnDataList.size(); i++) {
                JSONObject returnData = returnDataList.getJSONObject(i);
                String cell = returnData.getString("sha256Code");
                XieChengCollidingDataRob robData = cellMap.getOrDefault(cell, new XieChengCollidingDataRob());
                if (success) {
                    //周期表中新增True的数据
                    XieChengCollidingDataLoopCycle xieChengCollidingDataLoopCycle = new XieChengCollidingDataLoopCycle();
                    xieChengCollidingDataLoopCycle.setPackageId(cellMap.getOrDefault(cell, new XieChengCollidingDataRob()).getPackageId());
                    xieChengCollidingDataLoopCycle.setDataSourceType("F");
                    xieChengCollidingDataLoopCycle.setCellSha256CodeList(returnData.getString("sha256Code"));
                    xieChengCollidingDataLoopCycle.setReleaseTime(DateUtil.parse(returnData.getString("releaseTime"), DatePattern.NORM_DATETIME_PATTERN));
                    xieChengCollidingDataLoopCycle.setPushTime(new Date());
                    xieChengCollidingDataLoopCycle.setCreateTime(new Date());
                    xieChengCollidingDataLoopCycle.setUpdateTime(new Date());
                    xieChengCollidingDataLoopCycleMapper.insert(xieChengCollidingDataLoopCycle);
                    //非周期表中做剔除
                    robData.setPushTime(new Date());
                    robData.setIsDelete(1);
                    xieChengCollidingDataRobMapper.updateByPrimaryKey(robData);
                } else {
                    //重试次数加1
                    robData.setPushTime(new Date());
                    robData.setIsDelete(1);
                    // 更新 retry_count + 1
                    robData.setRetryCount(robData.getRetryCount() + 1);
                    xieChengCollidingDataRobMapper.updateByPrimaryKey(robData);
                }
            }
        }
    }
}
