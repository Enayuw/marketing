package com.br.marketing.service.Impl.xc;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

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
     * 
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
        robDto.setIsDelete(0);
        robDto.setCreateTime(new Date());
        robDto.setUpdateTime(new Date());

        cpsRobMapper.insertSelective(robDto);
    }

    /**
     * CPS非周期数据撞库结果处理
     * 
     * @param collidingResult 撞库结果
     * @param cellMap 手机号映射
     */
    public void robDataHandle(Result collidingResult, Map<String, XieChengCpsCollidingDataRob> cellMap) {
        JSONObject resJson = JSONObject.parseObject((String)collidingResult.getData());
        boolean success = collidingResult.getCode().equals(ResultCode.SUCCESS.getValue());
        List<XieChengCollidingDataLog> collidingLogs = Lists.newArrayList();
        String httpcode = resJson.getString("httpcode");
        
        if (success) {
            JSONObject contentJson = JSONObject.parseObject(resJson.getString("content"));
            Integer businessCode = contentJson.getInteger("code");
            JSONArray returnDataList = contentJson.getJSONArray("data");
            
            for (int i = 0; i < returnDataList.size(); i++) {
                JSONObject returnData = returnDataList.getJSONObject(i);
                String cell = returnData.getString("sha256Code");
                Boolean result = returnData.getBoolean("result");
                XieChengCpsCollidingDataRob robData = cellMap.getOrDefault(cell, new XieChengCpsCollidingDataRob());
                
                if (result) {
                    // TRUE结果：转入周期表
                    try {
                        trueDataHandle(cellMap, cell, returnData, robData);
                    } catch (Exception e) {
                        log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), 
                                e.getMessage(), "CPS非周期数据撞得True，处理异常，手机号：" + cell), e);
                    }
                } else {
                    // FALSE结果：更新非周期表
                    robData.setPushTime(new Date());
                    robData.setRetryCount(0);
                    robData.setUpdateTime(new Date());
                    cpsRobMapper.updateByPrimaryKeySelective(robData);
                }
                
                collidingLogs.add(cpsLogService.buildSuccessXieChengCollidingDataLog(robData.getId(), robData.getPackageId(),
                    null, "F", returnData, httpcode, businessCode));
            }
            
            cpsLogService.pushLogMessage(collidingLogs);
            
        } else {
            // 撞库失败：更新重试次数
            for (Map.Entry<String, XieChengCpsCollidingDataRob> entry : cellMap.entrySet()) {
                XieChengCpsCollidingDataRob robData = entry.getValue();
                robData.setPushTime(new Date());
                robData.setRetryCount(robData.getRetryCount() + 1);
                robData.setUpdateTime(new Date());
                cpsRobMapper.updateByPrimaryKeySelective(robData);
                
                collidingLogs.add(cpsLogService.buildFailXieChengCollidingDataLog(robData.getId(), robData.getPackageId(),
                    null, "F", robData.getCellSha256CodeList(), resJson));
            }
            
            cpsLogService.pushLogMessage(collidingLogs);
        }
    }

    /**
     * CPS TRUE数据处理：从非周期表转入周期表
     * 
     * @param cellMap 手机号映射
     * @param cell 手机号
     * @param returnData 返回数据
     * @param robData 非周期数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void trueDataHandle(Map<String, XieChengCpsCollidingDataRob> cellMap, String cell, JSONObject returnData, XieChengCpsCollidingDataRob robData) {
        // 周期表中新增True的数据
        XieChengCpsCollidingDataLoopCycle cpsLoopCycle = new XieChengCpsCollidingDataLoopCycle();
        cpsLoopCycle.setPackageId(cellMap.getOrDefault(cell, new XieChengCpsCollidingDataRob()).getPackageId());
        cpsLoopCycle.setDataSourceType("F"); // 来源于非周期数据
        cpsLoopCycle.setCellSha256CodeList(returnData.getString("sha256Code"));
        
        // 解析释放时间
        String releaseTimeStr = returnData.getString("releaseTime");
        if (StringUtils.isNotEmpty(releaseTimeStr)) {
            try {
                cpsLoopCycle.setReleaseTime(DateUtil.parse(releaseTimeStr, DatePattern.NORM_DATETIME_PATTERN));
            } catch (Exception e) {
                log.warn("CPS解析释放时间异常，使用默认时间，releaseTime：{}，error：{}", releaseTimeStr, e.getMessage());
                // 默认24小时后释放
                cpsLoopCycle.setReleaseTime(DateUtil.offsetHour(new Date(), 24));
            }
        } else {
            // 默认24小时后释放
            cpsLoopCycle.setReleaseTime(DateUtil.offsetHour(new Date(), 24));
        }
        
        cpsLoopCycle.setPushTime(new Date());
        cpsLoopCycle.setRetryCount(0);
        cpsLoopCycle.setIsDelete(0);
        cpsLoopCycle.setCreateTime(new Date());
        cpsLoopCycle.setUpdateTime(new Date());
        cpsLoopCycle.setExtend("CPS非周期数据TRUE结果转入");
        
        cpsLoopCycleMapper.insertSelective(cpsLoopCycle);
        
        // 非周期表中做剔除
        robData.setIsDelete(1);
        robData.setRetryCount(0);
        robData.setPushTime(new Date());
        robData.setUpdateTime(new Date());
        cpsRobMapper.updateByPrimaryKeySelective(robData);
        
        log.info("CPS非周期数据处理成功，手机号：{}，从非周期表转入周期表，释放时间：{}", 
                cell, DateUtil.formatDateTime(cpsLoopCycle.getReleaseTime()));
    }

    /**
     * CPS促活数据处理：从非周期表转入周期表
     * 
     * @param robData 非周期数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void activateDataByFalseToTrue(XieChengCpsCollidingDataRob robData) {
        // 周期表中新增
        XieChengCpsCollidingDataLoopCycle cpsLoopCycle = new XieChengCpsCollidingDataLoopCycle();
        cpsLoopCycle.setReleaseTime(DateUtil.offsetHour(new Date(), 24)); // 默认24小时后释放
        cpsLoopCycle.setPackageId(robData.getPackageId());
        cpsLoopCycle.setDataSourceType("F"); // 来源于非周期数据
        cpsLoopCycle.setCellSha256CodeList(robData.getCellSha256CodeList());
        cpsLoopCycle.setPushTime(robData.getPushTime());
        cpsLoopCycle.setRetryCount(0);
        cpsLoopCycle.setIsDelete(0);
        cpsLoopCycle.setCreateTime(new Date());
        cpsLoopCycle.setUpdateTime(new Date());
        cpsLoopCycle.setExtend("CPS促活数据转入");
        
        cpsLoopCycleMapper.insertSelective(cpsLoopCycle);

        // 非周期表剔除
        robData.setIsDelete(1);
        robData.setRetryCount(0);
        robData.setUpdateTime(new Date());
        cpsRobMapper.updateByPrimaryKeySelective(robData);
        
        log.info("CPS促活数据处理成功，手机号：{}，从非周期表转入周期表", robData.getCellSha256CodeList());
    }
} 