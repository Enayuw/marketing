package com.br.marketing.service.Impl.xc;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.xiecheng.XieChengService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.entity.XieChengCpsCollidingDataLog;
import com.br.marketing.entity.XieChengCpsCollidingDataLoopCycle;
import com.br.marketing.mapper.XieChengCpsCollidingDataLoopCycleMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 携程CPS周期撞库数据服务实现类
 * @Author chenh
 * @Date 2025-06-26
 */
@Service
@Slf4j
public class XieChengCpsLoopCycleDataServiceImpl implements XieChengCpsLoopCycleDataService {

    @Autowired
    XieChengService xieChengService;

    @Resource
    private XieChengCpsCollidingDataLoopCycleMapper dataLoopCycleMapper;
    @Resource
    private XieChengCpsCollidingDataLogService logService;

    @Resource
    private XieChengCpsCollidingResultHandleService handleService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    private final static int PARTITION_SIZE = 50;

    private TpDynamicExecutor threadPool;

    private TpDynamicExecutor getThreadPool(){
        if (threadPool == null) {
            threadPool = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.XIECHENG_CPS_LOOP_CYCLE_3710090.getName(), 5, 10);
        }
        return threadPool;
    }

    @Override
    public void process() {
        Long minId = null;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        while (true) {
            // 分页大小
            Integer pageSize = marketingCommonConfig.getXieChengSmsCollidingDataVtPageSize();
            // 结束时间：当前时间
            Date endDate = new Date();
            List<XieChengCpsCollidingDataLoopCycle> list = dataLoopCycleMapper.selectCycleDataByReleaseTime(minId, endDate, pageSize);
            if (CollectionUtils.isEmpty(list)) {
                break;
            }

            minId = list.get(list.size() - 1).getId();

            List<List<XieChengCpsCollidingDataLoopCycle>> partitions = Lists.partition(list, PARTITION_SIZE);
            for (List<XieChengCpsCollidingDataLoopCycle> partition : partitions) {
                futures.add(CompletableFuture.runAsync(() -> pushDataAndHandleResult(partition), getThreadPool()));
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * 50条数据一个批次，推送撞库手机号并处理返回结果
     * @param list CPS周期撞库数据列表
     */
    public void pushDataAndHandleResult(List<XieChengCpsCollidingDataLoopCycle> list) {
        try {
            // 组装撞库用cell
            List<String> originalCells = list.stream().map(XieChengCpsCollidingDataLoopCycle::getCellSha256CodeList).collect(Collectors.toList());
            Result<String> resultInfo = xieChengService.pushXieChengCpsCollidingData(originalCells);
            JSONObject resMap = JSONObject.parseObject(resultInfo.getData());
            String httpcode = resMap.getString("httpcode");

            if (ResultCode.FAIL.getValue().equals(resultInfo.getCode())) {
                // httpcode非200或code非0
                // 更新TRUE数据表retry_count=retry_count+1
                List<Long> ids = list.stream().map(XieChengCpsCollidingDataLoopCycle::getId).collect(Collectors.toList());
                dataLoopCycleMapper.updateBatchByIdOfRetryCount(ids);

                // 发送mq记录日志
                List<XieChengCpsCollidingDataLog> collidingLogs = list.stream()
                        .map(t -> logService.buildFailXieChengCpsCollidingDataLog(t.getId(), t.getPackageId(), null, "T", t.getCellSha256CodeList(),
                                resMap))
                        .collect(Collectors.toList());

                logService.pushLogMessage(collidingLogs);
                return;
            }

            JSONObject resultJson = JSONObject.parseObject(resMap.getString("content"));
            Integer businessCode = resultJson.getInteger("code");
            JSONArray returnDataList = resultJson.getJSONArray("data");

            if (CollectionUtils.isEmpty(returnDataList)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), JSON.toJSONString(resMap)
                        , "携程CPS周期撞库，接口返回code为0，但数据为空。resMap"));
                return;
            }

            // 根据手机号对实体分组
            Map<String, XieChengCpsCollidingDataLoopCycle> cellMaps =
                    list.stream().collect(Collectors.toMap(XieChengCpsCollidingDataLoopCycle::getCellSha256CodeList, Function.identity(),
                            (t1, t2) -> t1));

            // true数据处理
            trueHandle(returnDataList, cellMaps);

            // false数据处理
            falseHandle(returnDataList, cellMaps);

            // 发送mq记录日志
            List<XieChengCpsCollidingDataLog> collidingLogs = returnDataList.stream().map(t -> (JSONObject) t)
                    .map(t -> logService.buildSuccessXieChengCpsCollidingDataLog(cellMaps.get(t.get("sha256Code")).getId(),
                            cellMaps.get(t.get("sha256Code")).getPackageId(), null, "T", t, httpcode, businessCode))
                    .collect(Collectors.toList());
            logService.pushLogMessage(collidingLogs);

            // 撞得false数据推送外呼
            logService.pushRobotMessage(collidingLogs);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程CPS周期数据撞库，单线程处理异常"), e);
        }
    }

    /**
     * 返回为TRUE的结果处理
     * @param returnDataList 返回数据列表
     * @param cellMaps       手机号映射
     */
    private void trueHandle(JSONArray returnDataList, Map<String, XieChengCpsCollidingDataLoopCycle> cellMaps) {
        List<XieChengCpsCollidingDataLoopCycle> trueList = returnDataList.stream().map(t -> (JSONObject) t)
                .filter(t -> t.getBoolean("result").equals(Boolean.TRUE))
                .map(t -> buildTrueDataDto(t, cellMaps))
                .collect(Collectors.toList());
        trueList.forEach((XieChengCpsCollidingDataLoopCycle t) -> dataLoopCycleMapper.updateByTrueData(t));
    }

    /**
     * 返回为FALSE的结果处理
     * @param returnDataList 返回数据列表
     * @param cellMaps       手机号映射
     */
    private void falseHandle(JSONArray returnDataList, Map<String, XieChengCpsCollidingDataLoopCycle> cellMaps) {
        returnDataList.stream().map(obj -> (JSONObject) obj).filter(returnData -> !returnData.getBoolean("result"))
                .forEach((JSONObject returnData) -> {
                    String sha256Code = returnData.getString("sha256Code");
                    XieChengCpsCollidingDataLoopCycle loopCycle = cellMaps.get(sha256Code);
                    if (loopCycle == null) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), sha256Code
                                , "携程CPS周期数据撞库，返回未知sha256Code"));
                        return;
                    }
                    XieChengCpsCollidingDataLoopCycle dto = new XieChengCpsCollidingDataLoopCycle();
                    dto.setId(loopCycle.getId());
                    dto.setCellSha256CodeList(sha256Code);
                    try {
                        handleService.cycleDataHandle(dto);
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                                , "携程CPS周期数据撞库，同时写true和false表失败"), e);
                    }
                });
    }

    /**
     * 构建TRUE数据DTO
     */
    private XieChengCpsCollidingDataLoopCycle buildTrueDataDto(JSONObject t, Map<String, XieChengCpsCollidingDataLoopCycle> cellMaps) {
        XieChengCpsCollidingDataLoopCycle dto = new XieChengCpsCollidingDataLoopCycle();
        String sha256Code = t.getString("sha256Code");
        XieChengCpsCollidingDataLoopCycle loopCycle = cellMaps.get(sha256Code);
        if (loopCycle == null) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), sha256Code
                    , "携程CPS周期数据撞库，返回未知sha256Code"));
            return new XieChengCpsCollidingDataLoopCycle();
        }

        dto.setId(loopCycle.getId());
        // 更新pushTime
        dto.setPushTime(new Date());
        dto.setUpdateTime(new Date());
        // 更新retryCount
        dto.setRetryCount(0);
        // 更新releaseTime
        try {
            dto.setReleaseTime(DateUtil.parse(t.getString("releaseTime"), DatePattern.NORM_DATETIME_PATTERN));
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程CPS周期数据撞库，releaseTime格式异常,数据：" + t), e);
        }
        return dto;
    }
}