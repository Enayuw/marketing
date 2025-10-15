package com.br.marketing.service.Impl.xc;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.XieChengCollidingDataLog;
import com.br.marketing.entity.XieChengCollidingDataLoopCycle;
import com.br.marketing.entity.XieChengCollidingDataRob;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
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
    private XieChengCollidingResultHandleService resultHandleService;
    @Resource
    private XieChengCollidingDataLogService xieChengCollidingDataLogService;

    @Transactional(rollbackFor = Exception.class)
    public void cycleDataHandle(XieChengCollidingDataLoopCycle loopCycleDto, Long packageId, Date releaseDate) {
        // 更新true数据表
        loopCycleDto.setIsDelete(1);
        loopCycleDto.setPushTime(new Date());
        xieChengCollidingDataLoopCycleMapper.updateByPrimaryKeySelective(loopCycleDto);

        // 插入false数据表
        XieChengCollidingDataRob robDto = new XieChengCollidingDataRob();
        robDto.setPackageId(packageId);
        robDto.setDataSourceType("T");
        robDto.setCellSha256CodeList(loopCycleDto.getCellSha256CodeList());
        robDto.setPushTime(new Date());
        robDto.setReleaseDate(releaseDate);
        robDto.setCreateTime(new Date());
        robDto.setUpdateTime(new Date());

        robDto.setIsDelete(1);
        robDto.setRetryCount(0);
        xieChengCollidingDataRobMapper.insertSelective(robDto);
    }

    public void robDataHandle(Result collidingResult, Map<String, XieChengCollidingDataRob> cellMap) {
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
                XieChengCollidingDataRob robData = cellMap.getOrDefault(cell, new XieChengCollidingDataRob());
                robData.setCollidingCount(robData.getCollidingCount() + 1);
                if (result) {
                    // 增加try-catch保证50条一批其他数据正常处理，异常数据单条告警
                    try {
                        resultHandleService.trueDataHandle(cellMap, cell, returnData, robData);
                    } catch (Exception e) {
                        log.error("携程非周期数据撞得True，周期True表存在重复cell:{}", cell);
                    }
                } else {
                    robData.setPushTime(new Date());
                    robData.setRetryCount(0);
                    robData.setUpdateTime(new Date());
                    Date releaseDate = StringUtils.isNotEmpty(returnData.getString("releaseDate"))
                        ? DateUtil.parse(returnData.getString("releaseDate")) : null;
                    robData.setReleaseDate(releaseDate);
                    xieChengCollidingDataRobMapper.updateByPrimaryKeySelective(robData);
                }
                collidingLogs.add(xieChengCollidingDataLogService.buildSuccessXieChengCollidingDataLog(robData.getId(), robData.getPackageId(),
                    robData.getPackageRuleId(), "F", returnData, httpcode, businessCode));
            }
            xieChengCollidingDataLogService.pushLogMessage(collidingLogs);
        } else {
            // 异常没有httpCode和businessCode
            for (Map.Entry<String, XieChengCollidingDataRob> entry : cellMap.entrySet()) {
                XieChengCollidingDataRob robData = entry.getValue();
                robData.setPushTime(new Date());
                robData.setRetryCount(robData.getRetryCount() + 1);
                xieChengCollidingDataRobMapper.updateByPrimaryKeySelective(robData);
                collidingLogs.add(xieChengCollidingDataLogService.buildFailXieChengCollidingDataLog(robData.getId(), robData.getPackageId(),
                    robData.getPackageRuleId(), "F", robData.getCellSha256CodeList(), resJson));
            }
            xieChengCollidingDataLogService.pushLogMessage(collidingLogs);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void trueDataHandle(Map<String, XieChengCollidingDataRob> cellMap, String cell, JSONObject returnData, XieChengCollidingDataRob robData) {
        // 周期表中新增True的数据
        XieChengCollidingDataLoopCycle xieChengCollidingDataLoopCycle = new XieChengCollidingDataLoopCycle();
        xieChengCollidingDataLoopCycle.setPackageId(cellMap.getOrDefault(cell, new XieChengCollidingDataRob()).getPackageId());
        xieChengCollidingDataLoopCycle.setDataSourceType("F");
        xieChengCollidingDataLoopCycle.setCellSha256CodeList(returnData.getString("sha256Code"));
        xieChengCollidingDataLoopCycle.setReleaseTime(DateUtil.parse(returnData.getString("releaseTime"), DatePattern.NORM_DATETIME_PATTERN));
        try {
            JSONArray jsonArray = returnData.getJSONArray("marketCouponList");
            if (jsonArray != null && !jsonArray.isEmpty()) {
                xieChengCollidingDataLoopCycle.setMarketCouponList(jsonArray.toJSONString());
                JSONObject firstCoupon = jsonArray.getJSONObject(0);
                String couponCode = firstCoupon.getString("couponCode");
                String couponDesc = firstCoupon.getString("couponDesc");
                xieChengCollidingDataLoopCycle.setCouponCode(couponCode);
                xieChengCollidingDataLoopCycle.setCouponDesc(couponDesc);
            }
        } catch (Exception e) {
            xieChengCollidingDataLoopCycle.setMarketCouponList(String.valueOf(returnData.get("marketCouponList")));
            log.error("携程非周期撞库析出marketCouponList异常", e);
        }
        xieChengCollidingDataLoopCycle.setPushTime(new Date());
        xieChengCollidingDataLoopCycle.setRetryCount(0);
        xieChengCollidingDataLoopCycle.setCreateTime(new Date());
        xieChengCollidingDataLoopCycle.setUpdateTime(new Date());
        xieChengCollidingDataLoopCycle.setCustomerGroup(1);
        xieChengCollidingDataLoopCycle.setInfo(returnData.getString("info"));
        xieChengCollidingDataLoopCycleMapper.insertSelective(xieChengCollidingDataLoopCycle);
        // 非周期表中做剔除
        robData.setIsDelete(1);
        robData.setRetryCount(0);
        robData.setPushTime(new Date());
        robData.setUpdateTime(new Date());
        xieChengCollidingDataRobMapper.updateByPrimaryKeySelective(robData);
    }

    @Transactional(rollbackFor = Exception.class)
    public void activateDataByFalseToTrue(XieChengCollidingDataRob robData) {
        // 周期表中新增
        XieChengCollidingDataLoopCycle xieChengCollidingDataLoopCycle = new XieChengCollidingDataLoopCycle();
        xieChengCollidingDataLoopCycle.setReleaseTime(robData.getReleaseTime());
        xieChengCollidingDataLoopCycle.setCustomerGroup(2);
        xieChengCollidingDataLoopCycle.setPackageId(robData.getPackageId());
        xieChengCollidingDataLoopCycle.setDataSourceType("F");
        xieChengCollidingDataLoopCycle.setCellSha256CodeList(robData.getCellSha256CodeList());
        xieChengCollidingDataLoopCycle.setPushTime(robData.getPushTime());
        xieChengCollidingDataLoopCycle.setRetryCount(0);
        xieChengCollidingDataLoopCycle.setCreateTime(new Date());
        xieChengCollidingDataLoopCycle.setUpdateTime(new Date());
        xieChengCollidingDataLoopCycleMapper.insertSelective(xieChengCollidingDataLoopCycle);

        // 非周期表剔除
        robData.setIsDelete(1);
        robData.setRetryCount(0);
        robData.setUpdateTime(new Date());
        xieChengCollidingDataRobMapper.updateByPrimaryKeySelective(robData);
    }

}
