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
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.XieChengCpsCollidingDataLoopCycle;
import com.br.marketing.entity.XieChengCpsCollidingDataRob;
import com.br.marketing.mapper.XieChengCpsCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCpsCollidingDataRobMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 携程CPS非周期数据撞库服务实现类
 * @Author chenh
 * @Date 2025-06-26
 */
@Service
@Slf4j
public class XieChengCpsRobDataCollidingServiceImpl implements XieChengCpsRobDataCollidingService {

    @Resource
    private XieChengService xieChengService;

    @Resource
    private XieChengCpsCollidingDataRobMapper cpsRobMapper;

    @Resource
    private XieChengCpsCollidingDataLoopCycleMapper cpsLoopCycleMapper;

    @Resource
    private XieChengCpsCollidingResultHandleService handleService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    private final static int PARTITION_SIZE = 50;

    @Override
    public void process() {
        // 创建线程池
        TpDynamicExecutor threadPool = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.XIECHENG_CPS_ROB_3710090.getName(), 5, 10);

        // 分页大小
        Integer pageSize = marketingCommonConfig.getXieChengCpsCollidingDataSyncPageSize();

        Long minId = null;
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        while (true) {
            // 查询未撞库数据：retryCount=0，push_time=null
            List<XieChengCpsCollidingDataRob> list = cpsRobMapper.selectUnprocessedRobData(minId, pageSize);
            if (CollectionUtils.isEmpty(list)) {
                break;
            }

            minId = list.get(list.size() - 1).getId();

            // 异步处理
            List<List<XieChengCpsCollidingDataRob>> partitions = Lists.partition(list, PARTITION_SIZE);
            for (List<XieChengCpsCollidingDataRob> partition : partitions) {
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        pushDataAndHandleResult(partition);
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                                , "携程CPS非周期数据撞库，处理异常"), e);
                    }
                }, threadPool));
            }
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.warn("携程CPS非周期数据撞库完成，处理批次数：{}", futures.size());

        // 关闭线程池
        threadPool.shutdownAndAwaitTermination();
    }

//    /**
//     * 处理非周期数据批次
//     * @param list 非周期数据列表
//     */
//    private void processRobDataBatch(List<XieChengCpsCollidingDataRob> list) {
//        if (CollectionUtils.isEmpty(list)) {
//            return;
//        }
//
//        try {
//            log.info("携程CPS非周期数据撞库开始，数据量：{}", list.size());
//
//            // 组装撞库用cell
//            List<String> originalCells = list.stream()
//                    .map(XieChengCpsCollidingDataRob::getCellSha256CodeList)
//                    .collect(Collectors.toList());
//
//            // 调用携程CPS撞库接口
//            Result<String> resultInfo = xieChengService.pushXieChengCpsCollidingData(originalCells);
//
//            // 根据手机号对实体分组
//            Map<String, XieChengCpsCollidingDataRob> cellMaps = list.stream()
//                    .collect(Collectors.toMap(XieChengCpsCollidingDataRob::getCellSha256CodeList,
//                            Function.identity(), (t1, t2) -> t1));
//
//            // 处理撞库结果
//            handleRobCollidingResult(resultInfo, cellMaps);
//
//            log.info("携程CPS非周期数据撞库完成，数据量：{}", list.size());
//
//        } catch (Exception e) {
//            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
//                    , "携程CPS非周期数据撞库异常"), e);
//            throw e;
//        }
//    }

//    /**
//     * 处理非周期撞库结果
//     * @param collidingResult 撞库结果
//     * @param cellMap 手机号映射
//     */
//    private void handleRobCollidingResult(Result<String> collidingResult, Map<String, XieChengCpsCollidingDataRob> cellMap) {
//        JSONObject resJson = JSONObject.parseObject(collidingResult.getData());
//        String httpcode = resJson.getString("httpcode");
//
//        if (ResultCode.FAIL.getValue().equals(collidingResult.getCode())) {
//            // httpcode非200或code非0：更新retry_count=retry_count+1
//            handleFailureResult(cellMap, resJson);
//            return;
//        }
//
//        // httpcode=200且code=0：处理成功结果
//        handleSuccessResult(resJson, cellMap, httpcode);
//    }

//    /**
//     * 处理失败结果：更新retry_count=retry_count+1
//     * @param cellMap 手机号映射
//     * @param resJson 响应JSON
//     */
//    private void handleFailureResult(Map<String, XieChengCpsCollidingDataRob> cellMap, JSONObject resJson) {
//        for (Map.Entry<String, XieChengCpsCollidingDataRob> entry : cellMap.entrySet()) {
//            XieChengCpsCollidingDataRob robData = entry.getValue();
//            robData.setPushTime(new Date());
//            robData.setRetryCount(robData.getRetryCount() + 1);
//            robData.setUpdateTime(new Date());
//            cpsRobMapper.updateByPrimaryKeySelective(robData);
//        }
//
//        log.warn("携程CPS非周期数据撞库失败，更新重试次数，响应：{}", JSON.toJSONString(resJson));
//    }
//
//    /**
//     * 处理成功结果
//     * @param resJson 响应JSON
//     * @param cellMap 手机号映射
//     * @param httpcode HTTP状态码
//     */
//    private void handleSuccessResult(JSONObject resJson, Map<String, XieChengCpsCollidingDataRob> cellMap, String httpcode) {
//        JSONObject resultJson = JSONObject.parseObject(resJson.getString("content"));
//        Integer businessCode = resultJson.getInteger("code");
//        JSONArray returnDataList = resultJson.getJSONArray("data");
//
//        if (CollectionUtils.isEmpty(returnDataList)) {
//            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), JSON.toJSONString(resJson)
//                    , "携程CPS非周期数据撞库，接口返回code为0，但数据为空"));
//            return;
//        }
//
//        for (int i = 0; i < returnDataList.size(); i++) {
//            JSONObject returnData = returnDataList.getJSONObject(i);
//            String cell = returnData.getString("sha256Code");
//            Boolean result = returnData.getBoolean("result");
//            XieChengCpsCollidingDataRob robData = cellMap.get(cell);
//
//            if (robData == null) {
//                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), cell
//                        , "携程CPS非周期数据撞库，返回未知sha256Code"));
//                continue;
//            }
//
//            if (result) {
//                // 撞得true：写入周期表，设置下次撞库时间
//                handleTrueResult(returnData, robData);
//            } else {
//                // 撞得false：更新为删除状态
//                handleFalseResult(robData);
//            }
//        }
//    }

//    /**
//     * 处理TRUE结果：写入周期表，设置下次撞库时间
//     * @param returnData 返回数据
//     * @param robData 非周期数据
//     */
//    private void handleTrueResult(JSONObject returnData, XieChengCpsCollidingDataRob robData) {
//        try {
//            // 写入周期表
//            XieChengCpsCollidingDataLoopCycle cycleData = new XieChengCpsCollidingDataLoopCycle();
//            cycleData.setPackageId(robData.getPackageId());
//            cycleData.setDataSourceType("F"); // 来源于非周期数据
//            cycleData.setCellSha256CodeList(robData.getCellSha256CodeList());
//
//            // 解析释放时间
//            String releaseTimeStr = returnData.getString("releaseTime");
//            cycleData.setReleaseTime(DateUtil.parse(releaseTimeStr, DatePattern.NORM_DATETIME_PATTERN));
//
//            cycleData.setPushTime(new Date());
//            cycleData.setRetryCount(0);
//            cycleData.setIsDelete(0);
//            cycleData.setCreateTime(new Date());
//            cycleData.setUpdateTime(new Date());
//            cpsLoopCycleMapper.insertSelective(cycleData);
//
//            // 非周期表中标记为删除
//            robData.setIsDelete(1);
//            robData.setPushTime(new Date());
//            robData.setUpdateTime(new Date());
//            cpsRobMapper.updateByPrimaryKeySelective(robData);
//        } catch (Exception e) {
//            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
//                    e.getMessage(), "CPS非周期数据撞得True，处理异常，手机号：" + robData.getCellSha256CodeList()), e);
//        }
//    }

    /**
     * 处理FALSE结果：更新为删除状态
     * @param robData 非周期数据
     */
    private void handleFalseResult(XieChengCpsCollidingDataRob robData) {
        robData.setIsDelete(1);
        robData.setPushTime(new Date());
        robData.setUpdateTime(new Date());
        cpsRobMapper.updateByPrimaryKeySelective(robData);

        log.info("CPS非周期数据撞得False，手机号：{}，已标记删除", robData.getCellSha256CodeList());
    }

    @Override
    public void pushDataAndHandleResult(List<XieChengCpsCollidingDataRob> list) {
        try {
            // 组装撞库用cell
            List<String> originalCells = list.stream()
                    .map(XieChengCpsCollidingDataRob::getCellSha256CodeList)
                    .collect(Collectors.toList());

            // 调用携程CPS撞库接口
            Result<String> resultInfo = xieChengService.pushXieChengCpsCollidingData(originalCells);

            // 根据手机号对实体分组
            Map<String, XieChengCpsCollidingDataRob> cellMaps = list.stream()
                    .collect(Collectors.toMap(XieChengCpsCollidingDataRob::getCellSha256CodeList,
                            Function.identity(), (t1, t2) -> t1));

            // 处理撞库结果
            handleService.robDataHandle(resultInfo, cellMaps);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程CPS非周期数据撞库异常"), e);
        }
    }
} 