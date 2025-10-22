package com.br.marketing.service.Impl.wuba;

import com.br.common.log.AlertLog;
import com.br.marketing.client.wuba.WuBaServiceClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.orika.OrikaBeanMapperUtil;
import com.br.marketing.dto.wuba.ConversionResponseDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.DataCleaningAutoService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Description 58新客提交营销名单结果查询
 * @Author lixiang
 * @Date 2024-07-10
 */
@Service
@Slf4j
public class WuBaQueryConversionResultTransService {

    private static final String TITLE = "【58新客提交营销名单结果查询】";
    private Integer PARTITION_SIZE = 50;

    ThreadPoolExecutor dbActionPool = BrExecutors.getThreadPool(10, 10);

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

    @Resource
    private MarketingCleanDataTaskMapper cleanDataTaskMapper;

    @Transactional(rollbackFor = Exception.class)
    public Result<WubaCollidingDataBatchNo> processCallSuccess(WubaCollidingDataBatchNo wubaCollidingBatchNo,
                                                               List<ConversionResponseDTO> dtoList) throws Exception {
        Result<WubaCollidingDataBatchNo> result = new Result().failure();

        // call success, 上报分流处理
        List<ConversionResponseDTO> successDtoList = new ArrayList<>();
        List<ConversionResponseDTO> failureDtoList = new ArrayList<>();
        String batchNo = wubaCollidingBatchNo.getBatchNo();

        for(ConversionResponseDTO dto: dtoList){
            if(StringUtils.isEmpty(dto.getMobileEncrypt())){
                failureDtoList.add(dto);
                continue;
            }
            boolean a = (!StringUtils.isEmpty(dto.getLastLoginTime()) || !StringUtils.isEmpty(dto.getFinanceApplyTime()));
            boolean b = (!StringUtils.isEmpty(dto.getFinanceCreditStatus()) || !StringUtils.isEmpty(dto.getFinanceCreditFinishTime()));
            boolean c = (!StringUtils.isEmpty(dto.getDebtTime()) || !StringUtils.isEmpty(dto.getDebtPassTime()));
            boolean d = (!StringUtils.isEmpty(dto.getLoanAmt()));
            if (a || b || c || d) {
                successDtoList.add(dto);
                continue;
            }
            failureDtoList.add(dto);
        }

        log.warn(TITLE+"批次{}, 上报成功数量{}, 上报失败数量{}", batchNo, successDtoList.size(), failureDtoList.size());
        // 上报成功数据
        processSuccessSubmit(wubaCollidingBatchNo, successDtoList);
        // 上报失败数据
        processFailureSubmit(wubaCollidingBatchNo, failureDtoList);
        // 上报批次表query_status置为1-已查询
        updateBatchNoStatus(wubaCollidingBatchNo, 1);

        return result.success();
    }

    public Result processSuccessSubmit(WubaCollidingDataBatchNo wubaCollidingBatchNo,
                                       List<ConversionResponseDTO> responseDtoList) throws Exception {
        if(CollectionUtils.isEmpty(responseDtoList)){
            return new Result().success();
        }
        String apiCode = wubaCollidingBatchNo.getApiCode();
        String batchNo = wubaCollidingBatchNo.getBatchNo();

        // 清洗任务
        Long taskId = cleaningAutoService.saveCleanTask(apiCode, 1, "58新客_转化清洗规则勿动");

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
            dataTransferClean.setTaskId(taskId);
            return dataTransferClean;
        }).collect(Collectors.toList());


        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(dbActionPool, marketingCommonConfig.getWuBaQueryConversionBatDBThreadPool());
        PARTITION_SIZE = marketingCommonConfig.getWuBaQueryConversionBatDBPartitionSize();

        // batAddDataTransferClean
        List<CompletableFuture<Void>> dataTransferCleanFutures = Lists.newArrayList();
        List<List<WubaSubmitConversionDataTransferClean>> dataTransferCleanPartitions = Lists.partition(dataTransferCleanList, PARTITION_SIZE);
        for (List<WubaSubmitConversionDataTransferClean> partition : dataTransferCleanPartitions) {
            dataTransferCleanFutures.add(CompletableFuture.runAsync(() -> {
                try {
                    batAddDataTransferClean(partition,  batchNo);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),
                            TITLE + "批量保存转化结果异常"));
                }
            }, dbActionPool));
        }
        CompletableFuture.allOf(dataTransferCleanFutures.toArray(new CompletableFuture[0])).join();
        log.warn(TITLE + "保存转化结果成功, batchNo: {}", batchNo);

        // 上报日志表, add转化数据，submit_result置为1-上报成功
        List<CompletableFuture<Void>> dataLogFutures = Lists.newArrayList();
        List<List<ConversionResponseDTO>> dataLogPartitions = Lists.partition(responseDtoList, PARTITION_SIZE);
        for (List<ConversionResponseDTO> partition : dataLogPartitions) {
            dataLogFutures.add(CompletableFuture.runAsync(() -> {
                try {
                    batUpdateDataLog(partition,  batchNo);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),
                            TITLE + "批量更新上报日志状态异常"));
                }
            }, dbActionPool));
        }
        CompletableFuture.allOf(dataLogFutures.toArray(new CompletableFuture[0])).join();
        log.warn(TITLE + "更新上报日志状态成功, batchNo: {}", batchNo);

        // 营销名单上报表, push_status置为2-推送成功
//        List<String> successCellList = responseDtoList.stream().map(ConversionResponseDTO::getMobileEncrypt).collect(Collectors.toList());
//        updateDataStatus(successCellList,2, apiCode, wubaCollidingBatchNo.getPushTime());
//        log.warn(TITLE + "更新营销名单上报状态成功, batchNo: {}", batchNo);

        // 更新清洗任务表
        MarketingCleanDataTask cleanDataTaskUpdate = new MarketingCleanDataTask();
        cleanDataTaskUpdate.setId(taskId);
        cleanDataTaskUpdate.setCleanStatus(0);
        cleanDataTaskMapper.updateByPrimaryKeySelective(cleanDataTaskUpdate);
        log.warn(TITLE + "更新清洗任务成功, batchNo: {}", batchNo);

        return new Result().success();
    }

    public Result<?> processFailureSubmit(WubaCollidingDataBatchNo wubaCollidingBatchNo,
                                          List<ConversionResponseDTO> responseDtoList) throws Exception {
        if(CollectionUtils.isEmpty(responseDtoList)){
            return new Result().success();
        }

        List<String> failureCellList = responseDtoList.stream().map(ConversionResponseDTO::getMobileEncrypt).collect(Collectors.toList());
        // 上报日志表, submit_result置为2-上报失败
        updateDataLogStatus(wubaCollidingBatchNo, failureCellList, 2);

        // 营销名单上报表, push_status置为3-推送失败
//        updateDataStatus(failureCellList, 3, wubaCollidingBatchNo.getApiCode(), wubaCollidingBatchNo.getPushTime());

        return new Result().success();
    }

    public Result updateBatchNoStatus(WubaCollidingDataBatchNo wubaCollidingBatchNo, Integer queryStatus) throws Exception {
        WubaCollidingDataBatchNo batchNoUpdate = new WubaCollidingDataBatchNo();
        batchNoUpdate.setBatchType(2);
        batchNoUpdate.setQueryStatus(queryStatus);
        //
        WubaCollidingDataBatchNoExample batchNoUpdateExample = new WubaCollidingDataBatchNoExample();
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

    public Result updateDataLogStatus(WubaCollidingDataBatchNo wubaCollidingBatchNo, List<String> cellList,
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

    private void batAddDataTransferClean(List<WubaSubmitConversionDataTransferClean> batList, String batchNo) {
        dataTransferCleanMapper.batchAdd(batList);
        log.warn(TITLE + "保存转化结果成功, batchNo: {}", batchNo);
    }

    private void batUpdateDataLog(List<ConversionResponseDTO> dtoList, String batchNo) {
        for(ConversionResponseDTO dto: dtoList){
            WubaSubmitConversionDataLog dataLog = OrikaBeanMapperUtil.map(dto, WubaSubmitConversionDataLog.class);
            dataLog.setSubmitResult(1);
            //
            WubaSubmitConversionDataLogExample dataLogExample = new WubaSubmitConversionDataLogExample();
            dataLogExample.createCriteria().andBatchNoEqualTo(batchNo).andCellEqualTo(dto.getMobileEncrypt());
            dataLogMapper.updateByExampleSelective(dataLog, dataLogExample);
        }
        log.warn(TITLE + "更新上报日志表成功, batchNo: {}", batchNo);
    }
}
