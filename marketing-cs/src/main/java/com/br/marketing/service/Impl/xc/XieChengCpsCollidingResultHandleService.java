package com.br.marketing.service.Impl.xc;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.br.marketing.entity.XieChengCpsCollidingDataLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.XieChengCollidingDataLog;
import com.br.marketing.entity.XieChengCpsCollidingDataLoopCycle;
import com.br.marketing.entity.XieChengCpsCollidingDataRob;
import com.br.marketing.mapper.XieChengCpsCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCpsCollidingDataRobMapper;
import com.google.api.client.util.Lists;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 携程CPS撞库结果处理服务
 * @Author chenh
 * @Date 2025-06-26
 */
@Service
@Slf4j
public class XieChengCpsCollidingResultHandleService {

    @Resource
    private XieChengCpsCollidingDataLoopCycleMapper cpsLoopCycleMapper;

    @Resource
    private XieChengCpsCollidingDataRobMapper cpsRobMapper;

    @Resource
    private XieChengCpsCollidingDataLogService cpsLogService;

    /**
     * CPS周期数据处理：将FALSE结果从周期表删除并插入非周期表
     * @param loopCycleDto CPS周期数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void cycleDataHandle(XieChengCpsCollidingDataLoopCycle loopCycleDto) {
        // 更新CPS周期数据表，标记为删除
        loopCycleDto.setIsDelete(1);
        loopCycleDto.setPushTime(new Date());
        loopCycleDto.setUpdateTime(new Date());
        cpsLoopCycleMapper.updateByPrimaryKeySelective(loopCycleDto);

        // 插入CPS非周期数据表
        XieChengCpsCollidingDataRob robDto = new XieChengCpsCollidingDataRob();
        robDto.setDataSourceType("T"); // 来源于周期数据
        robDto.setCellSha256CodeList(loopCycleDto.getCellSha256CodeList());
        robDto.setPushTime(new Date());
        robDto.setRetryCount(0);
        robDto.setIsDelete(1);
        robDto.setCreateTime(new Date());
        robDto.setUpdateTime(new Date());

        cpsRobMapper.insertSelective(robDto);
    }

    /**
     * CPS非周期数据撞库结果处理
     * @param collidingResult 撞库结果
     * @param cellMap         手机号映射
     */
    public void robDataHandle(Result collidingResult, Map<String, XieChengCpsCollidingDataRob> cellMap) {
        JSONObject resJson = JSONObject.parseObject((String) collidingResult.getData());
        boolean success = collidingResult.getCode().equals(ResultCode.SUCCESS.getValue());
        List<XieChengCpsCollidingDataLog> collidingLogs = Lists.newArrayList();
        String httpcode = resJson.getString("httpcode");

        if (success) {
            // httpcode200且code为0
            JSONObject contentJson = JSONObject.parseObject(resJson.getString("content"));
            Integer businessCode = contentJson.getInteger("code");
            JSONArray returnDataList = contentJson.getJSONArray("data");

            for (int i = 0; i < returnDataList.size(); i++) {
                JSONObject returnData = returnDataList.getJSONObject(i);
                String cell = returnData.getString("sha256Code");
                Boolean result = returnData.getBoolean("result");
                String releaseTime = returnData.getString("releaseTime");
                XieChengCpsCollidingDataRob robData = cellMap.getOrDefault(cell, new XieChengCpsCollidingDataRob());

                if (result) {
                    // TRUE结果：转入周期表
                    try {
                        trueDataHandle(cell, releaseTime, robData);
                    } catch (Exception e) {
                        log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                                e.getMessage(), "CPS非周期数据撞得True，处理异常，手机号：" + cell), e);
                    }
                } else {
                    // FALSE结果：从非周期表中做剔除
                    robData.setPushTime(new Date());
                    robData.setUpdateTime(new Date());
                    robData.setIsDelete(1);
                    cpsRobMapper.updateByPrimaryKeySelective(robData);
                }

                collidingLogs.add(cpsLogService.buildSuccessXieChengCpsCollidingDataLog(robData.getId(), robData.getPackageId(),
                        null, "F", returnData, httpcode, businessCode));
            }

            cpsLogService.pushLogMessage(collidingLogs);
            cpsLogService.pushRobotMessage(collidingLogs);
        } else {
            // httpcode非200或code非0：更新重试次数
            for (Map.Entry<String, XieChengCpsCollidingDataRob> entry : cellMap.entrySet()) {
                XieChengCpsCollidingDataRob robData = entry.getValue();
                robData.setPushTime(new Date());
                robData.setRetryCount(robData.getRetryCount() + 1);
                robData.setUpdateTime(new Date());
                cpsRobMapper.updateByPrimaryKeySelective(robData);

                collidingLogs.add(cpsLogService.buildFailXieChengCpsCollidingDataLog(robData.getId(), robData.getPackageId(),
                        null, "F", robData.getCellSha256CodeList(), resJson));
            }

            cpsLogService.pushLogMessage(collidingLogs);
        }
    }

    /**
     * CPS TRUE数据处理：从非周期表转入周期表
     * @param cell    手机号
     * @param robData 非周期数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void trueDataHandle(String cell, String releaseTime, XieChengCpsCollidingDataRob robData) {
        // 周期表中新增True的数据
        XieChengCpsCollidingDataLoopCycle cpsLoopCycle = new XieChengCpsCollidingDataLoopCycle();
        cpsLoopCycle.setPackageId(robData.getPackageId());
        cpsLoopCycle.setDataSourceType("F"); // 来源于非周期数据
        cpsLoopCycle.setCellSha256CodeList(cell);
        // 解析释放时间
        cpsLoopCycle.setReleaseTime(DateUtil.parse(releaseTime, DatePattern.NORM_DATETIME_PATTERN));
        cpsLoopCycle.setPushTime(new Date());
        cpsLoopCycle.setRetryCount(0);
        cpsLoopCycle.setIsDelete(0);
        cpsLoopCycle.setCreateTime(new Date());
        cpsLoopCycle.setUpdateTime(new Date());
        cpsLoopCycleMapper.insertSelective(cpsLoopCycle);

        // 非周期表中做剔除
        robData.setIsDelete(1);
        robData.setPushTime(new Date());
        robData.setUpdateTime(new Date());
        cpsRobMapper.updateByPrimaryKeySelective(robData);
    }
}