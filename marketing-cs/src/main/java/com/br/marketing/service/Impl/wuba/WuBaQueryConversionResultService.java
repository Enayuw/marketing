package com.br.marketing.service.Impl.wuba;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.wuba.WuBaServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.orika.OrikaBeanMapperUtil;
import com.br.marketing.dto.wuba.ConversionResponseDTO;
import com.br.marketing.dto.wuba.WubaQueryConversionDto;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.WubaCollidingBatchNoMapper;
import com.br.marketing.mapper.WubaSubmitConversionDataLogMapper;
import com.br.marketing.mapper.WubaSubmitConversionDataMapper;
import com.br.marketing.mapper.WubaSubmitConversionDataTransferCleanMapper;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.service.DataCleaningAutoService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

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
    private WubaSubmitConversionDataMapper dataMapper;

    @Resource
    private WubaCollidingBatchNoMapper batchNoMapper;

    @Resource
    private WubaSubmitConversionDataLogMapper dataLogMapper;

    @Resource
    private WubaSubmitConversionDataTransferCleanMapper dataTransferCleanMapper;

    @Resource
    private WuBaServiceClient wuBaServiceClient;

    @Resource
    private WuBaDingDingService wuBaDingDingService;

    @Resource
    private WubaSubmitConversionDataTransferCleanMapper transferCleanMapper;

    @Resource
    private DataCleaningAutoService cleaningAutoService;


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

        WubaCollidingBatchNoExample batchNoExample = new WubaCollidingBatchNoExample();
        batchNoExample.createCriteria().andBatchTypeEqualTo(batchType).andQueryStatusEqualTo(queryStatus)
                .andPushTimeBetween(pushTimeStart, pushTimeEnd);
        final List<WubaCollidingBatchNo> batchNoList = batchNoMapper.selectByExample(batchNoExample);

        if (CollectionUtils.isEmpty(batchNoList)) {
            log.warn(TITLE+"未获取到批次数据");
            return result;
        }
        // queryPool
        ThreadPoolExecutor queryPool = BrExecutors.getThreadPool(12, 12, 20);

        // futureList
        List<Future<Result<WubaCollidingBatchNo>>> futureList = new ArrayList<>();
        for(WubaCollidingBatchNo wubaCollidingBatchNo: batchNoList) {
            setThreadPoolParam(queryPool);
            futureList.add(queryPool.submit(() -> processData(wubaCollidingBatchNo, condition)));
        }

        for (Future<Result<WubaCollidingBatchNo>> future : futureList) {
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

        List<String> batchNos = batchNoList.stream().map(WubaCollidingBatchNo::getBatchNo).collect(Collectors.toList());
        int cleanCount = getCleanCountByBatchNos(batchNos, apiCode);
        if (cleanCount <= 0) {
            result.success();
        }

        cleaningAutoService.saveCleanTask(apiCode, 1, "58新客_转化清洗规则勿动");
        return result.success();
    }

    public Result<WubaCollidingBatchNo> processData(WubaCollidingBatchNo wubaCollidingBatchNo,
                                                    Page2Condition<WubaQueryConversionDto> condition) throws Exception {
        Result<WubaCollidingBatchNo> result = new Result().failure();
        try {
            // callClient
            String batchNo = wubaCollidingBatchNo.getBatchNo();
            Result<List<ConversionResponseDTO>> callResult = callClient(wubaCollidingBatchNo);
            //
            if (callResult == null) {
                return result;
            }
            if (!callResult.isSuccess()) {
                // code 9991
                if (callResult.getCode() == 9991) {
                    return result;
                }
                // 上报批次表query_status置为2-查询异常
                Result updateBatchNoResult = updateBatchNoStatus(wubaCollidingBatchNo, 2);
                if (updateBatchNoResult == null || !updateBatchNoResult.isSuccess()) {
                    return result;
                }
                // Alert
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),
                        TITLE + "调用接口失败, batchNo: " + batchNo));
                wuBaDingDingService.sendAlert(TITLE, "调用接口失败, batchNo: " + batchNo);
                return result;
            }

            // call success
            List<ConversionResponseDTO> dtoList = callResult.getData();
            if (CollectionUtils.isEmpty(dtoList)) {
                log.warn(TITLE + "返回列表为空");
                return result;
            }

            processCallSuccess(wubaCollidingBatchNo, dtoList);
        } catch (Exception e){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),
                    TITLE+ e.getMessage()));
        }
        return result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<WubaCollidingBatchNo> processCallSuccess(WubaCollidingBatchNo wubaCollidingBatchNo,
                                                           List<ConversionResponseDTO> dtoList) throws Exception {
        Result<WubaCollidingBatchNo> result = new Result().failure();

        // call success, 上报分流处理
        List<ConversionResponseDTO> successDtoList = new ArrayList<>();
        List<ConversionResponseDTO> failureDtoList = new ArrayList<>();
        String batchNo = wubaCollidingBatchNo.getBatchNo();

        for(ConversionResponseDTO dto: dtoList){
            if(StringUtils.isEmpty(dto.getMobileEncrypt())){
                failureDtoList.add(dto);
                continue;
            }
            if(!StringUtils.isEmpty(dto.getLastLoginTime())
                    && !StringUtils.isEmpty(dto.getFinanceApplyTime())
                    && !StringUtils.isEmpty(dto.getFinanceCreditFinishTime())){
                successDtoList.add(dto);
                continue;
            }
            failureDtoList.add(dto);
        }

        log.warn("批次{}, 上报成功数量{}, 上报失败数量{}", batchNo, successDtoList.size(), failureDtoList.size());
        // 上报成功数据
        processSuccessSubmit(wubaCollidingBatchNo, successDtoList);
        // 上报失败数据
        processFailureSubmit(wubaCollidingBatchNo, failureDtoList);
        // 上报批次表query_status置为1-已查询
        updateBatchNoStatus(wubaCollidingBatchNo, 1);

        return result.success();
    }

    public Result<List<ConversionResponseDTO>> callClient(WubaCollidingBatchNo wubaCollidingBatchNo) {
        Result<List<ConversionResponseDTO>> result = new Result<>().failure();

        // call queryConversionResult
        long startTime = System.currentTimeMillis();
        Result callResult = wuBaServiceClient.queryConversionResult(wubaCollidingBatchNo.getBatchNo());

        // call failure
        if(callResult == null){
            return result;
        }
        if(!callResult.isSuccess()){
            if(callResult.getData()== null){
                return result;
            }
            String data = String.valueOf(callResult.getData());
            JSONObject resultMap = JSONObject.parseObject(data);
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

    public Result processSuccessSubmit(WubaCollidingBatchNo wubaCollidingBatchNo,
            List<ConversionResponseDTO> responseDtoList) throws Exception {
        if(CollectionUtils.isEmpty(responseDtoList)){
            return new Result().success();
        }

        // 转化结果表增加记录
        List<WubaSubmitConversionDataTransferClean> dataTransferCleanList = responseDtoList.stream()
                .map((ConversionResponseDTO dto) -> {
            WubaSubmitConversionDataTransferClean dataTransferClean = OrikaBeanMapperUtil
                    .map(dto, WubaSubmitConversionDataTransferClean.class);
            dataTransferClean.setApiCode(wubaCollidingBatchNo.getApiCode());
            dataTransferClean.setCell(dto.getMobileEncrypt());
            dataTransferClean.setBatchNo(wubaCollidingBatchNo.getBatchNo());
            dataTransferClean.setPushTime(wubaCollidingBatchNo.getPushTime());
            dataTransferClean.setCleanStatus(0);
            return dataTransferClean;
        }).collect(Collectors.toList());

        int batchAddResult = dataTransferCleanMapper.batchAdd(dataTransferCleanList);
        if(batchAddResult != dataTransferCleanList.size()){
            throw new Exception(TITLE+"保存转化结果异常");
        }

        // 上报日志表, add转化数据，submit_result置为1-上报成功
        for(ConversionResponseDTO dto: responseDtoList){
            WubaSubmitConversionDataLog dataLog = OrikaBeanMapperUtil.map(dto, WubaSubmitConversionDataLog.class);
            dataLog.setSubmitResult(1);
            //
            WubaSubmitConversionDataLogExample dataLogExample = new WubaSubmitConversionDataLogExample();
            dataLogExample.createCriteria().andBatchNoEqualTo(wubaCollidingBatchNo.getBatchNo())
                    .andCellEqualTo(dto.getMobileEncrypt());
            dataLogMapper.updateByExampleSelective(dataLog, dataLogExample);
        }

        // 营销名单上报表, push_status置为2-推送成功
        List<String> successCellList = responseDtoList.stream().map(ConversionResponseDTO::getMobileEncrypt).collect(Collectors.toList());
        updateDataStatus(successCellList,2);

        return new Result().success();
    }

    public Result<?> processFailureSubmit(WubaCollidingBatchNo wubaCollidingBatchNo,
                                          List<ConversionResponseDTO> responseDtoList) throws Exception {
        if(CollectionUtils.isEmpty(responseDtoList)){
            return new Result().success();
        }

        List<String> failureCellList = responseDtoList.stream().map(ConversionResponseDTO::getMobileEncrypt).collect(Collectors.toList());
        // 上报日志表, submit_result置为2-上报失败
        updateDataLogStatus(wubaCollidingBatchNo, failureCellList, 2);

        // 营销名单上报表, push_status置为3-推送失败
        updateDataStatus(failureCellList, 3);

        return new Result().success();
    }

    public Result updateBatchNoStatus(WubaCollidingBatchNo wubaCollidingBatchNo, Integer queryStatus) throws Exception {
        WubaCollidingBatchNo batchNoUpdate = new WubaCollidingBatchNo();
        batchNoUpdate.setBatchType(2);
        batchNoUpdate.setQueryStatus(queryStatus);
        //
        WubaCollidingBatchNoExample batchNoUpdateExample = new WubaCollidingBatchNoExample();
        String batchNo = wubaCollidingBatchNo.getBatchNo();
        batchNoUpdateExample.createCriteria().andBatchNoEqualTo(batchNo);
        int batchNoUpdateResult = batchNoMapper.updateByExampleSelective(batchNoUpdate, batchNoUpdateExample);
        if (batchNoUpdateResult < 1) {
            String errorMsg = String.format("更新上报批次表状态异常, batchNo: %d, queryStatus: %s", queryStatus, batchNo);
            log.warn(TITLE + errorMsg);
            throw new Exception(TITLE + errorMsg);
        }
        return new Result().success();
    }

    public Result updateDataStatus(List<String> cellList, Integer pushStatus) throws Exception {
        WubaSubmitConversionData dataUpdate = new WubaSubmitConversionData();
        dataUpdate.setPushStatus(pushStatus);
        //
        WubaSubmitConversionDataExample dataUpdateExample = new WubaSubmitConversionDataExample();
        dataUpdateExample.createCriteria().andCellIn(cellList);
        int dataUpdateResult = dataMapper.updateByExampleSelective(dataUpdate, dataUpdateExample);
        if(dataUpdateResult != cellList.size()){
            log.warn(TITLE+"更新营销名单上报状态{}, 批次返回cell与上报表不一致", pushStatus);
            // throw new Exception(TITLE+"营销名单上报表更新状态异常");
        }
        return new Result().success();
    }

    public Result updateDataLogStatus(WubaCollidingBatchNo wubaCollidingBatchNo, List<String> cellList,
                                      Integer submitResult) throws Exception {
        WubaSubmitConversionDataLog dataLogUpdate = new WubaSubmitConversionDataLog();
        dataLogUpdate.setSubmitResult(submitResult);
        //
        WubaSubmitConversionDataLogExample dataLogUpdateExample = new WubaSubmitConversionDataLogExample();
        dataLogUpdateExample.createCriteria().andBatchNoEqualTo(wubaCollidingBatchNo.getBatchNo()).andCellIn(cellList);
        int dataLogUpdateResult = dataLogMapper.updateByExampleSelective(dataLogUpdate, dataLogUpdateExample);
        if(dataLogUpdateResult != cellList.size()){
            log.warn(TITLE+"更新营销名单上报日志状态{}, 批次返回cell与上报表不一致", submitResult);
            // throw new Exception(TITLE+"营销名单上报日志表更新状态异常");
        }
        return new Result().success();
    }

    private void setThreadPoolParam(ThreadPoolExecutor queryPool) {
        List<Integer> list = marketingCommonConfig.getWuBaQueryConversionThreadPool();
        int queryPoolSize = list.get(0);

        if (ObjectUtils.isEmpty(queryPoolSize) || queryPoolSize < 1) {
            queryPoolSize = Runtime.getRuntime().availableProcessors() * 10;
        }

        queryPool.setCorePoolSize(queryPoolSize);
        queryPool.setMaximumPoolSize(queryPoolSize);
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

    private int getCleanCountByBatchNos(List<String> batchNos, String apiCode) {
        WubaSubmitConversionDataTransferCleanExample example = new WubaSubmitConversionDataTransferCleanExample();
        example.createCriteria().andIsDeletedEqualTo(0)
                .andBatchNoIn(batchNos).andApiCodeEqualTo(apiCode)
                .andCleanStatusEqualTo(0);
        List<WubaSubmitConversionDataTransferClean> transferCleans = transferCleanMapper.selectByExample(example);
        return transferCleans.size();
    }
}
