package com.br.marketing.service.Impl.wuba;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.orika.OrikaBeanMapperUtil;
import com.br.marketing.dto.wuba.ConversionReponseDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.WubaCollidingBatchNoMapper;
import com.br.marketing.mapper.WubaSubmitConversionDataLogMapper;
import com.br.marketing.mapper.WubaSubmitConversionDataMapper;
import com.br.marketing.mapper.WubaSubmitConversionDataTransferCleanMapper;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
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
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Description 58新客提交营销名单结果查询
 * @Author lixiang
 * @Date 2024-07-10
 */
@Service
@Slf4j
public class WuBaQueryConversionResultService {

    private final static String TITLE = "【58新客提交营销名单结果查询】";

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


    public void action(Page2Condition<WubaCollidingBatchNo> condition) {
        scanData(condition);
    }

    public void scanData(Page2Condition<WubaCollidingBatchNo> condition) {
        Result<?> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());
        // queryPool
        ThreadPoolExecutor queryPool = BrExecutors.getThreadPool(12, 12, 20);
        // futureList
        List<Future<Result<WubaCollidingBatchNo>>> futureList = new ArrayList<>();

        WubaCollidingBatchNo param = condition.getParam();
        Integer batchType = param.getBatchType();
        Date pushTime = param.getPushTime();
        Integer queryStatus = param.getQueryStatus();

        WubaCollidingBatchNoExample batchNoExample = new WubaCollidingBatchNoExample();
        batchNoExample.createCriteria().andBatchTypeEqualTo(batchType).andQueryStatusEqualTo(queryStatus)
                .andPushTimeBetween(new Date(), new Date());
        final List<WubaCollidingBatchNo> batchNoList = batchNoMapper.selectByExample(batchNoExample);

        if (CollectionUtils.isEmpty(batchNoList)) {
            log.warn(TITLE+"未获取到数据");
            return;
        }

        for(WubaCollidingBatchNo b: batchNoList){
            setThreadPoolParam(queryPool);
            futureList.add(queryPool.submit(() -> processData(b, condition)));
        }

    }

    @Transactional
    public Result<WubaCollidingBatchNo> processData(WubaCollidingBatchNo wubaCollidingBatchNo, Page2Condition<WubaCollidingBatchNo> condition) {
        Result<WubaCollidingBatchNo> result = new Result();
        result.setCode(ResultCode.FAIL.getValue());
        // callClient
        Result<List<ConversionReponseDTO>> callResult = callClient(wubaCollidingBatchNo, condition);
        if(!callResult.isSuccess() || callResult.getData()==null){
            return result;
        }

        List<ConversionReponseDTO> dtoList= callResult.getData();
        if (CollectionUtils.isEmpty(dtoList)) {
            log.warn(TITLE+"返回列表为空");
            return result;
        }

        // 返回数据分流处理
        List<WubaSubmitConversionDataTransferClean> dataTransferCleanList = new ArrayList<>();
        List<WubaSubmitConversionDataLog> datalogSuccessList = new ArrayList<>();
        List<WubaSubmitConversionDataLog> datalogFailureList = new ArrayList<>();

        for(ConversionReponseDTO dto: dtoList){
            if(dto.getId()==null || StringUtils.isEmpty(dto.getMobileEncrypt())){
                processFailureDto(dto, datalogFailureList);
                continue;
            }
            if(!StringUtils.isEmpty(dto.getLastLoginTime()) && !StringUtils.isEmpty(dto.getFinanceApplyTime())
                    && !StringUtils.isEmpty(dto.getFinanceCreditStatus())
                    && !StringUtils.isEmpty(dto.getFinanceCreditFinishTime())){
                processSuccessDto(dto, dataTransferCleanList, datalogSuccessList);
                continue;
            }
            processFailureDto(dto, datalogFailureList);
        }

        // 上报成功数据
        processSuccessSubmit(wubaCollidingBatchNo, dataTransferCleanList, datalogSuccessList);

        // 上报失败数据
        processFailureSubmit(wubaCollidingBatchNo, datalogFailureList);

        // 上报批次表query_status置为1-已查询
        Result<?> updateBatchNoStatusResult = updateBatchNoStatus(wubaCollidingBatchNo, 1);


        return new Result<>();
    }

    public Result<List<ConversionReponseDTO>> callClient(WubaCollidingBatchNo wubaCollidingBatchNo, Page2Condition<WubaCollidingBatchNo> condition) {
        Result<List<ConversionReponseDTO>> result = new Result<>();
        result.setCode(ResultCode.FAIL.getValue());
        // call
        long startTime = System.currentTimeMillis();
        // byApiServiceClient.pushBaiying(reqBlacklistDTO,0);
        long endTime = System.currentTimeMillis();
        log.warn(TITLE+"callClient, 耗时{}", (endTime-startTime));
        return result;
    }

    public void processSuccessDto(ConversionReponseDTO dto, List<WubaSubmitConversionDataTransferClean> dataTransferCleanList,
                                  List<WubaSubmitConversionDataLog> datalogList) {
        WubaSubmitConversionDataTransferClean dataTransferClean = OrikaBeanMapperUtil
                .map(dto, WubaSubmitConversionDataTransferClean.class);
        dataTransferClean.setCell(dto.getMobileEncrypt());
        dataTransferClean.setPushTime(new Date());
        dataTransferClean.setCleanStatus(0);
        dataTransferClean.setExtend("");
        dataTransferCleanList.add(dataTransferClean);

        WubaSubmitConversionDataLog dataLog = OrikaBeanMapperUtil
                .map(dto, WubaSubmitConversionDataLog.class);
        dataLog.setSubmitResult(1);
        datalogList.add(dataLog);
    }

    public void processFailureDto(ConversionReponseDTO dto, List<WubaSubmitConversionDataLog> datalogList) {
        WubaSubmitConversionDataTransferClean dataTransferClean = OrikaBeanMapperUtil
                .map(dto, WubaSubmitConversionDataTransferClean.class);
        dataTransferClean.setCell(dto.getMobileEncrypt());
    }

    public Result<?> processSuccessSubmit(WubaCollidingBatchNo wubaCollidingBatchNo,
            List<WubaSubmitConversionDataTransferClean> dataTransferCleanList,
            List<WubaSubmitConversionDataLog> datalogSuccessList) {
        // 转化结果表增加记录
        int batchAddResult = dataTransferCleanMapper.batchAdd(dataTransferCleanList);
        if(batchAddResult != dataTransferCleanList.size()){
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }

        // 上报日志表记录转化数据，submit_result置为1-上报成功
        for(WubaSubmitConversionDataLog dataLog: datalogSuccessList){
            WubaSubmitConversionDataLogExample dataLogExample = new WubaSubmitConversionDataLogExample();
            dataLogExample.createCriteria().andCellEqualTo(dataLog.getCell());
            dataLogMapper.updateByExample(dataLog, dataLogExample);
        }

        // 营销名单上报表push_status置为2-推送成功
        List<String> successCellList = datalogSuccessList.stream().map(WubaSubmitConversionDataLog::getCell).collect(Collectors.toList());
        updateDataStatus(successCellList,2);

        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    public Result<?> processFailureSubmit(WubaCollidingBatchNo wubaCollidingBatchNo,
            List<WubaSubmitConversionDataLog> datalogFailureList) {

        List<String> cellList = datalogFailureList.stream().map(WubaSubmitConversionDataLog::getCell).collect(Collectors.toList());
        // 上报日志表submit_result置为2-上报失败
        Result<?> updateDataLongStatusResult = updateDataLongStatus(wubaCollidingBatchNo, cellList, 2);

        // 营销名单上报表push_status置为3-推送失败
        Result<?> updateDataStatusResult = updateDataStatus(cellList, 3);

        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    public Result<?> updateBatchNoStatus(WubaCollidingBatchNo wubaCollidingBatchNo, Integer queryStatus){
        // 上报批次表query_status置为1-已查询
        WubaCollidingBatchNo batchNoUpdate = new WubaCollidingBatchNo();
        batchNoUpdate.setBatchType(2);
        batchNoUpdate.setPushTime(new Date());
        batchNoUpdate.setQueryStatus(queryStatus);
        //
        WubaCollidingBatchNoExample batchNoUpdateExample = new WubaCollidingBatchNoExample();
        batchNoUpdateExample.createCriteria().andBatchNoEqualTo(wubaCollidingBatchNo.getBatchNo());
        int batchNoUpdateResult = batchNoMapper.updateByExample(batchNoUpdate, batchNoUpdateExample);
        if(batchNoUpdateResult < 1){
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    public Result<?> updateDataStatus(List<String> cellList, Integer pushStatus){
        WubaSubmitConversionData dataUpdate = new WubaSubmitConversionData();
        dataUpdate.setPushStatus(pushStatus);
        //
        WubaSubmitConversionDataExample dataUpdateExample = new WubaSubmitConversionDataExample();
        dataUpdateExample.createCriteria().andCellIn(cellList);
        int dataUpdateResult = dataMapper.updateByExample(dataUpdate, dataUpdateExample);
        if(dataUpdateResult != cellList.size()){
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    public Result<?> updateDataLongStatus(WubaCollidingBatchNo wubaCollidingBatchNo, List<String> cellList, Integer submitResult){
        WubaSubmitConversionDataLog dataLogUpdate = new WubaSubmitConversionDataLog();
        dataLogUpdate.setSubmitResult(submitResult);
        //
        WubaSubmitConversionDataLogExample dataLogUpdateExample = new WubaSubmitConversionDataLogExample();
        dataLogUpdateExample.createCriteria().andBatchNoEqualTo(wubaCollidingBatchNo.getBatchNo()).andCellIn(cellList);
        int dataLogUpdateResult = dataLogMapper.updateByExample(dataLogUpdate, dataLogUpdateExample);
        if(dataLogUpdateResult != cellList.size()){
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private void setThreadPoolParam(ThreadPoolExecutor queryPool) {
        List<Integer> list = marketingCommonConfig.getWuBaSubmitConversionThreadPool();
        int queryPoolSize = list.get(0);

        if (ObjectUtils.isEmpty(queryPoolSize) || queryPoolSize < 1) {
            queryPoolSize = Runtime.getRuntime().availableProcessors() * 10;
        }

        queryPool.setCorePoolSize(queryPoolSize);
        queryPool.setMaximumPoolSize(queryPoolSize);
    }
}
