package com.br.marketing.service.Impl.xc;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.xiecheng.XieChengServiceNew;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.entity.XieChengCollidingDataLoopCycle;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description 携程TRUE数据撞库作业实现类
 * @Author hong.chen
 * @CreateTime 2024/03/21
 */
@Service
@Slf4j
public class XcLoopCycleDataServiceImpl implements XcLoopCycleDataService {
    @Resource
    private XieChengServiceNew xieChengServiceNew;
    @Resource
    private XieChengCollidingDataLoopCycleMapper dataLoopCycleMapper;
    @Resource
    private RabbitMqProducter rabbitMqProducter;
    @Resource
    private XieChengCollidingResultHandleService handleService;

    @Override
    public void pushDataAndHandleResult(List<XieChengCollidingDataLoopCycle> list, AtomicInteger failNum) {
        Map<String, XieChengCollidingDataLoopCycle> collect =
            list.stream().collect(Collectors.toMap(XieChengCollidingDataLoopCycle::getCellSha256CodeList, Function.identity()));

        List<String> cells = list.stream().map(XieChengCollidingDataLoopCycle::getCellSha256CodeList).collect(Collectors.toList());
        Result resultInfo = xieChengServiceNew.pushXieChengSmsCollidingDataNew(cells);

        JSONObject resMap = JSONObject.parseObject(resultInfo.getMessage());
        String httpcode = resMap.getString("httpcode");
        JSONObject resultJson = JSONObject.parseObject(resMap.getString("content"));
        Integer businessCode = resultJson.getInteger("code");

        JSONArray returnDataList = resultJson.getJSONArray("data");
        if (ResultCode.SUCCESS.getValue().equals(resultInfo.getCode())) {
            // code==0
            // 更新数据表
            for (int i = 0; i < returnDataList.size(); i++) {
                JSONObject returnData = returnDataList.getJSONObject(i);
                String sha256Code = returnData.getString("sha256Code");
                Boolean result = returnData.getBoolean("result");
                String orgChannel = returnData.getString("orgChannel");
                String mktLevel = returnData.getString("mktLevel");
                String info = returnData.getString("info");
                String releaseTime = returnData.getString("releaseTime");

                XieChengCollidingDataLoopCycle loopCycle = collect.get(sha256Code);
                if (loopCycle == null) {
                    // todo 报警
                    continue;
                }

                XieChengCollidingDataLoopCycle dto = new XieChengCollidingDataLoopCycle();
                dto.setId(loopCycle.getId());
                dto.setCellSha256CodeList(sha256Code);
                dto.setPackageId(loopCycle.getPackageId());
                // 更新pushTime
                dto.setPushTime(new Date());
                // 更新retryCount
                dto.setRetryCount(0);

                if (result) {
                    // 更新releaseTime
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime releaseTimeDate = LocalDateTime.parse(releaseTime, formatter);
                    Date releaseDate = Date.from(releaseTimeDate.atZone(ZoneId.systemDefault()).toInstant());
                    dto.setReleaseTime(releaseDate);
                    // dto.setDataSourceType("T");

                    dataLoopCycleMapper.updateByPrimaryKeySelective(dto);
                } else {
                    handleService.cycleDataHandle(dto);
                }

                // 插入log表
                rabbitMqProducter.send(MQConstants.ROUTING_KEY_MARKETING_XIECHENG_COLLIDING_LOG, returnData.toJSONString());
            }

        } else {
            failNum.incrementAndGet();

            for (int i = 0; i < returnDataList.size(); i++) {
                JSONObject returnData = returnDataList.getJSONObject(i);
                String sha256Code = returnData.getString("sha256Code");
                XieChengCollidingDataLoopCycle loopCycle = collect.get(sha256Code);
                if (loopCycle == null) {
                    // todo 报警
                    continue;
                }

                // 更新TRUE数据表
                XieChengCollidingDataLoopCycle dto = new XieChengCollidingDataLoopCycle();
                dto.setRetryCount(loopCycle.getRetryCount() + 1);
                dto.setId(loopCycle.getId());

                dataLoopCycleMapper.updateByPrimaryKeySelective(dto);

                // 发MQ插入log表
                rabbitMqProducter.send(MQConstants.ROUTING_KEY_MARKETING_XIECHENG_COLLIDING_LOG, returnData.toJSONString());
            }
        }
    }
}