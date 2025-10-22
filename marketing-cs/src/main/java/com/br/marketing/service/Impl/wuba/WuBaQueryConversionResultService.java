package com.br.marketing.service.Impl.wuba;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.wuba.WuBaServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.wuba.ConversionResponseDTO;
import com.br.marketing.dto.wuba.WubaQueryConversionDto;
import com.br.marketing.entity.WubaCollidingDataBatchNo;
import com.br.marketing.entity.WubaCollidingDataBatchNoExample;
import com.br.marketing.mapper.WubaCollidingBatchNoMapper;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description 58新客提交营销名单结果查询
 * @Author lixiang
 * @Date 2024-07-10
 */
@Service
@Slf4j
public class WuBaQueryConversionResultService {

    private static final String TITLE = "【58新客提交营销名单结果查询】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private WubaCollidingBatchNoMapper batchNoMapper;

    @Resource
    private WuBaServiceClient wuBaServiceClient;

    @Resource
    private WuBaDingDingService wuBaDingDingService;

    @Resource
    private WuBaQueryConversionResultTransService transService;

    public void action(Page2Condition<WubaQueryConversionDto> condition) {
        scanData(condition);
    }

    public Result scanData(Page2Condition<WubaQueryConversionDto> condition) {
        Result result = new Result<>().failure();

        // 扫描批次, BatchType 2-上报, QueryStatus 0-未查询
        // param
        WubaQueryConversionDto param = condition.getParam();
        Integer batchType = param.getBatchType();
        Integer queryStatus = param.getQueryStatus();
        String apiCode = param.getApiCode();
        Date pushTimeStart = param.getPushTimeStart();
        Date pushTimeEnd = param.getPushTimeEnd();

        WubaCollidingDataBatchNoExample batchNoExample = new WubaCollidingDataBatchNoExample();
        batchNoExample.createCriteria().andBatchTypeEqualTo(batchType).andQueryStatusEqualTo(queryStatus)
                .andApiCodeEqualTo(apiCode)
                .andPushTimeGreaterThanOrEqualTo(pushTimeStart)
                .andPushTimeLessThan(pushTimeEnd)
                .andIsDeletedEqualTo(0);
        final List<WubaCollidingDataBatchNo> batchNoList = batchNoMapper.selectByExample(batchNoExample);

        if (CollectionUtils.isEmpty(batchNoList)) {
            log.warn(TITLE+"未获取到批次数据");
            return result;
        }

        // queryPool
        ThreadPoolExecutor queryPool = BrExecutors.getThreadPool(12, 12, 20);

        // futureList
        List<Future<Result<WubaCollidingDataBatchNo>>> futureList = new ArrayList<>();
        for(WubaCollidingDataBatchNo wubaCollidingBatchNo: batchNoList) {
            setThreadPoolParam(queryPool);
            futureList.add(queryPool.submit(() -> processData(wubaCollidingBatchNo)));
        }

        for (Future<Result<WubaCollidingDataBatchNo>> future : futureList) {
            try {
                future.get(1, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),e.getMessage()
                        , TITLE), e);
//                future.cancel(true);
                result.setCode(ResultCode.FAIL.getValue());
            }
        }

        long taskCount = -1;
        queryPool.shutdown();
        try {
            while (!queryPool.awaitTermination(30, TimeUnit.SECONDS)) {
                long completedTask2Count = queryPool.getCompletedTaskCount();
                if (taskCount == completedTask2Count) {
                    result.setCode(ResultCode.FAIL.getValue());
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),
                            TITLE + "业务线程等待超时"));
                    break;
                }
                taskCount = completedTask2Count;
            }
        } catch (InterruptedException e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),
                    TITLE + "业务线程中断"));
            result.setCode(ResultCode.FAIL.getValue());
            Thread.currentThread().interrupt();
        }

        return result.success();
    }

    public Result<WubaCollidingDataBatchNo> processData(WubaCollidingDataBatchNo wubaCollidingBatchNo) throws Exception {
        Result<WubaCollidingDataBatchNo> result = new Result().failure();
        String batchNo = wubaCollidingBatchNo.getBatchNo();
        log.warn(TITLE + "processData start, batchNo: {}", batchNo);
        try {
            // callClient
            Result<List<ConversionResponseDTO>> callResult = callClient(wubaCollidingBatchNo);
            //
            if (callResult == null) {
                return result;
            }
            if (!callResult.isSuccess()) {
                // code 9991
                if (callResult.getCode() == 9991 || callResult.getCode() == 500) {
                    return result;
                }
                // 上报批次表query_status置为2-查询异常
                Result updateBatchNoResult = transService.updateBatchNoStatus(wubaCollidingBatchNo, 2);
                if (updateBatchNoResult == null || !updateBatchNoResult.isSuccess()) {
                    return result;
                }
                return result;
            }

            // call success
            List<ConversionResponseDTO> dtoList = callResult.getData();
            if (CollectionUtils.isEmpty(dtoList)) {
                log.warn(TITLE + "返回列表为空");
                return result;
            }

            transService.processCallSuccess(wubaCollidingBatchNo, dtoList);
        } catch (Exception e){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),
                    TITLE+ e.getMessage()));
        }
        log.warn(TITLE + "processData end, batchNo: {}", batchNo);
        return result.success();
    }

    public Result<List<ConversionResponseDTO>> callClient(WubaCollidingDataBatchNo wubaCollidingBatchNo) {
        Result<List<ConversionResponseDTO>> result = new Result<>().failure();

        // call queryConversionResult
        long startTime = System.currentTimeMillis();
        Result callResult = wuBaServiceClient.queryConversionResult(wubaCollidingBatchNo.getBatchNo());

        // call failure
        if(callResult == null){
            return result;
        }
        if(!callResult.isSuccess()){
            // Alert
            String msg = String.format(TITLE + "调用接口失败, batchNo: %s, resMap: %s",
                    wubaCollidingBatchNo.getBatchNo(), JSONObject.toJSONString(callResult.getData()));
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg));
            // wuBaDingDingService.sendAlert(TITLE, msg);

            if(callResult.getData()== null){
                return result;
            }
            String data = String.valueOf(callResult.getData());
            JSONObject resultMap = JSONObject.parseObject(data);
            if (!"200".equals(resultMap.get("httpcode")) || StringUtils.isEmpty(resultMap.get("content"))) {
                return result.setCode(500);
            }
            JSONObject content = resultMap.getJSONObject("content");
            Integer code = content.getInteger("code");
            if (code == 9991){
                return result.setCode(9991);
            }
            return result;
        }

        // call success
        String data = String.valueOf(callResult.getData());
        JSONArray ja = JSONObject.parseArray(data);
        List<ConversionResponseDTO> dtoList = ja.stream().map((Object obj) -> {
            JSONObject jo = (JSONObject) obj;
            ConversionResponseDTO dto = JSONObject.parseObject(JSONObject.toJSONString(jo), ConversionResponseDTO.class);
            Set<String> knowFields = marketingCommonConfig.getWuBaQueryConversionKnowFields();
            dto.setExtend(getExtraFields(jo, knowFields));
            return dto;
        }).collect(Collectors.toList());

        long endTime = System.currentTimeMillis();
        log.warn(TITLE+"callClient, 耗时{}", (endTime-startTime));
        return result.setCode(ResultCode.SUCCESS.getValue()).setDate(dtoList);
    }

    private void setThreadPoolParam(ThreadPoolExecutor queryPool) {
        List<Integer> list = marketingCommonConfig.getWuBaQueryConversionThreadPool();
        int queryPoolSize = list.get(0);

        if (ObjectUtils.isEmpty(queryPoolSize) || queryPoolSize < 1) {
            queryPoolSize = Runtime.getRuntime().availableProcessors() * 10;
        }

        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(queryPool, queryPoolSize);
    }

    private String getExtraFields(JSONObject jo, Set<String> knowFields){
        JSONObject res = new JSONObject();
        Set<String> keySet = jo.keySet();
        for (String key : keySet) {
            if(!knowFields.contains(key)){
                res.put(key, jo.get(key));
            }
        }
        return res.toJSONString();
    }
}
