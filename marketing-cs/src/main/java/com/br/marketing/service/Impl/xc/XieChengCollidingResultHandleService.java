package com.br.marketing.service.Impl.xc;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.entity.XieChengCollidingDataLog;
import com.br.marketing.entity.XieChengCollidingDataLoopCycle;
import com.br.marketing.entity.XieChengCollidingDataRob;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.google.api.client.util.Lists;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

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
    public void cycleDataHandle(XieChengCollidingDataLoopCycle loopCycleDto) {
        // 更新true数据表
        loopCycleDto.setIsDelete(1);
        xieChengCollidingDataLoopCycleMapper.updateByPrimaryKeySelective(loopCycleDto);

        // 插入false数据表
        XieChengCollidingDataRob robDto = new XieChengCollidingDataRob();
        robDto.setPackageId(loopCycleDto.getPackageId());
        robDto.setDataSourceType("T");
        robDto.setCellSha256CodeList(loopCycleDto.getCellSha256CodeList());
        robDto.setPushTime(new Date());
        robDto.setIsDelete(0);
        robDto.setRetryCount(0);
        xieChengCollidingDataRobMapper.insert(robDto);
    }

    @Transactional(rollbackFor = Exception.class)
    public void robDataHandle(Result collidingResult, Map<String, XieChengCollidingDataRob> cellMap, AtomicInteger failNum) {
        JSONObject resultJson = JSONObject.parseObject(collidingResult.getMessage());
        boolean success = collidingResult.getCode().equals(ResultCode.SUCCESS.getValue());
        JSONArray returnDataList = resultJson.getJSONArray("data");
        List<XieChengCollidingDataLog> collidingLogs = Lists.newArrayList();
        if (success) {
            for (int i = 0; i < returnDataList.size(); i++) {
                JSONObject returnData = returnDataList.getJSONObject(i);
                String cell = returnData.getString("sha256Code");
                Boolean result = returnData.getBoolean("result");
                XieChengCollidingDataRob robData = cellMap.getOrDefault(cell, new XieChengCollidingDataRob());
                if (result) {
                    // 周期表中新增True的数据
                    XieChengCollidingDataLoopCycle xieChengCollidingDataLoopCycle = new XieChengCollidingDataLoopCycle();
                    xieChengCollidingDataLoopCycle.setPackageId(cellMap.getOrDefault(cell, new XieChengCollidingDataRob()).getPackageId());
                    xieChengCollidingDataLoopCycle.setDataSourceType("F");
                    xieChengCollidingDataLoopCycle.setCellSha256CodeList(returnData.getString("sha256Code"));
                    xieChengCollidingDataLoopCycle
                        .setReleaseTime(DateUtil.parse(returnData.getString("releaseTime"), DatePattern.NORM_DATETIME_PATTERN));
                    xieChengCollidingDataLoopCycle.setPushTime(new Date());
                    xieChengCollidingDataLoopCycle.setCreateTime(new Date());
                    xieChengCollidingDataLoopCycle.setUpdateTime(new Date());
                    xieChengCollidingDataLoopCycleMapper.insert(xieChengCollidingDataLoopCycle);
                    // 非周期表中做剔除
                    robData.setIsDelete(1);
                    robData.setPushTime(new Date());
                    xieChengCollidingDataRobMapper.updateByPrimaryKey(robData);
                } else {
                    robData.setPushTime(new Date());
                    xieChengCollidingDataRobMapper.updateByPrimaryKey(robData);
                }
                collidingLogs.add(buildXieChengCollidingDataLog(robData, returnData));
            }
            pushLogMessage(collidingLogs);
        } else {
            String msg = resultJson.getString("msg");
            for (Map.Entry<String, XieChengCollidingDataRob> entry : cellMap.entrySet()) {
                failNum.getAndIncrement();
                XieChengCollidingDataRob robData = entry.getValue();
                robData.setPushTime(new Date());
                robData.setRetryCount(robData.getRetryCount() + 1);
                xieChengCollidingDataRobMapper.updateByPrimaryKey(robData);
                collidingLogs.add(buildFailXieChengCollidingDataLog(robData, msg));
            }
            pushLogMessage(collidingLogs);
        }
    }

    private void pushLogMessage(List<XieChengCollidingDataLog> collidingLogs) {
        try {
            rabbitMqProducter.send(MQConstants.ROUTING_KEY_MARKETING_XIECHENG_COLLIDING_LOG, JSONObject.toJSONString(collidingLogs));
        } catch (Exception e) {
            log.error("推送携程撞库日志消息异常", e);
        }

    }

    private XieChengCollidingDataLog buildXieChengCollidingDataLog(XieChengCollidingDataRob robData, JSONObject returnData) {
        String sha256Code = returnData.getString("sha256Code");
        Boolean result = returnData.getBoolean("result");
        String orgChannel = returnData.getString("orgChannel");
        String mktLevel = returnData.getString("mktLevel");
        String info = returnData.getString("info");
        String releaseTime = returnData.getString("releaseTime");

        XieChengCollidingDataLog xieChengCollidingDataLog = new XieChengCollidingDataLog();
        xieChengCollidingDataLog.setSmsCollidingDataId(robData.getId());
        xieChengCollidingDataLog.setPackageId(robData.getPackageId());
        xieChengCollidingDataLog.setDataSourceType("F");
        xieChengCollidingDataLog.setCellSha256CodeList(sha256Code);
        xieChengCollidingDataLog.setReleaseTime(releaseTime);
        xieChengCollidingDataLog.setOrgChannel(orgChannel);
        xieChengCollidingDataLog.setMktLevel(mktLevel);
        xieChengCollidingDataLog.setInfo(info);
        xieChengCollidingDataLog.setResult(result);
        xieChengCollidingDataLog.setReturnContent(returnData.toJSONString());
        xieChengCollidingDataLog.setCreateTime(new Date());
        xieChengCollidingDataLog.setUpdateTime(new Date());

        return xieChengCollidingDataLog;
    }

    private XieChengCollidingDataLog buildFailXieChengCollidingDataLog(XieChengCollidingDataRob robData, String msg) {
        XieChengCollidingDataLog xieChengCollidingDataLog = new XieChengCollidingDataLog();
        xieChengCollidingDataLog.setSmsCollidingDataId(robData.getId());
        xieChengCollidingDataLog.setPackageId(robData.getPackageId());
        xieChengCollidingDataLog.setDataSourceType("F");
        xieChengCollidingDataLog.setCellSha256CodeList(robData.getCellSha256CodeList());
        xieChengCollidingDataLog.setReturnContent(msg);
        xieChengCollidingDataLog.setCreateTime(new Date());
        xieChengCollidingDataLog.setUpdateTime(new Date());
        return xieChengCollidingDataLog;
    }

}
