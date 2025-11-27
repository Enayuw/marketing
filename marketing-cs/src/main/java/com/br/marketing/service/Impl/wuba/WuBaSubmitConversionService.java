package com.br.marketing.service.Impl.wuba;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.DateUtils;
import com.br.marketing.client.wuba.WuBaServiceClient;
import com.br.marketing.client.wuba.input.WuBaSubmitDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.WubaCollidingDataBatchNo;
import com.br.marketing.entity.WubaSubmitConversionData;
import com.br.marketing.entity.WubaSubmitConversionDataExample;
import com.br.marketing.entity.WubaSubmitConversionDataLog;
import com.br.marketing.mapper.WubaCollidingBatchNoMapper;
import com.br.marketing.mapper.WubaSubmitConversionDataLogMapper;
import com.br.marketing.mapper.WubaSubmitConversionDataMapper;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Description 58新客提交营销名单
 * @Author lixiang
 * @Date 2024-07-10
 */
@Service
@Slf4j
public class WuBaSubmitConversionService {

    private final static String TITLE = "【58新客提交营销名单】";
    private Integer PARTITION_SIZE = 50;

    ThreadPoolExecutor dbActionPool = BrExecutors.getThreadPool(10, 10);

    @Resource
    private WubaSubmitConversionDataMapper wubaSubmitConversionDataMapper;

    @Resource
    private WubaCollidingBatchNoMapper wubaCollidingBatchNoMapper;

    @Resource
    private WubaSubmitConversionDataLogMapper wubaSubmitConversionDataLogMapper;

    @Resource
    private WuBaServiceClient wuBaServiceClient;

    @Resource
    private WuBaSubmitConversionSoleProcessor soleProcessor;

    @Resource
    private WuBaSubmitConversionValidityProcessor validityProcessor;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    public Result<Map<String, Object>> action(Page2Condition<WubaSubmitConversionData> condition) {
        return scanData(condition);
    }

    public Result<Map<String, Object>> scanData(Page2Condition<WubaSubmitConversionData> condition) {
        Result result = new Result().success();
        Map<String, Object> data= new HashMap<>();

        WubaSubmitConversionData param = condition.getParam();
        String apiCode = param.getApiCode();
        Integer status = param.getStatus();
        Integer pushStatus = param.getPushStatus();
        String userType = param.getUserType();
        Integer createDate = param.getCreateDate();
        Integer pageSize = condition.getPageSize();

        try{
            // 循环获取条件数据，每次pageSize条
            Map<String, String> marketingTimeInterval = calculateMarketingTimeInterval(createDate);
            String marketingTimeStart = marketingTimeInterval.get("marketingTimeStart");
            String marketingTimeEnd = marketingTimeInterval.get("marketingTimeEnd");
            List<WubaSubmitConversionData> pageList = wubaSubmitConversionDataMapper.findWithMarketingTimeByPage(
                    apiCode, status, pushStatus, userType, marketingTimeStart, marketingTimeEnd, pageSize);
            if (CollectionUtils.isEmpty(pageList)) {
                log.warn(TITLE+"scanData, 未获取到数据");
                data.put("hasScanData", "0");
                return new Result().success().setDate(data);
            }
            log.warn(TITLE + "scanData 获取到数据, 条数{}", pageList.size());

            // 去重
            List<Long> noPushIds = soleProcessor.checkExists(pageList, param);
            if(!CollectionUtils.isEmpty(noPushIds)){
                // 营销名单上报表status置为3-重复数据
                WubaSubmitConversionData dataUpdate = new WubaSubmitConversionData();
                dataUpdate.setStatus(3);
                WubaSubmitConversionDataExample dataExample = new WubaSubmitConversionDataExample();
                dataExample.createCriteria().andIdIn(noPushIds);
                wubaSubmitConversionDataMapper.updateByExampleSelective(dataUpdate, dataExample);
            }
            log.warn(TITLE + "scanData, 去重条数{}, 推送条数{}", noPushIds.size(), pageList.size());

            // 判断有效期
            List<Long> noValidIds = validityProcessor.validate(pageList, param);
            if(!CollectionUtils.isEmpty(noValidIds)){
                WubaSubmitConversionData dataUpdate = new WubaSubmitConversionData();
                dataUpdate.setStatus(4);
                WubaSubmitConversionDataExample dataExample = new WubaSubmitConversionDataExample();
                dataExample.createCriteria().andIdIn(noValidIds);
                wubaSubmitConversionDataMapper.updateByExampleSelective(dataUpdate, dataExample);
            }
            log.warn(TITLE + "scanData, 不在有效期条数{}, 推送条数{}", noValidIds.size(), pageList.size());

            // process submit data
            processData(pageList, condition);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), TITLE+ e.getMessage()));
            Thread.currentThread().interrupt();
        }
        return result;
    }

    public Result processData(List<WubaSubmitConversionData> pageList, Page2Condition<WubaSubmitConversionData> condition)
            throws Exception {
        Result result = new Result().failure();
        if (CollectionUtils.isEmpty(pageList)) {
            log.warn(TITLE+"processData无数据");
            return result.success();
        }

        String apiCode = condition.getParam().getApiCode();
        // callClient
        Result<String> callResult = callClient(pageList);
        if (callResult == null || !callResult.isSuccess() || callResult.getData() == null) {
            return result;
        }
        log.warn(TITLE + "调用接口成功{}", apiCode);

        // call success
        String batchNo = callResult.getData();
        if (StringUtils.isEmpty(batchNo)) {
            return result;
        }
        log.warn(TITLE + "batchNo{}", batchNo);

        // 上报批次表增加记录，query_status置为0-未查询
        WubaCollidingDataBatchNo batchRecord = new WubaCollidingDataBatchNo();
        batchRecord.setApiCode(apiCode);
        batchRecord.setBatchNo(batchNo);
        batchRecord.setBatchType(2);
        batchRecord.setPushTime(new Date());
        batchRecord.setQueryStatus(0);
        int insert = wubaCollidingBatchNoMapper.insertSelective(batchRecord);
        if (insert < 1) {
            throw new Exception(TITLE + "上报批次表增加记录异常");
        }
        log.warn(TITLE + "上报批次表增加记录成功, batchNo{}", batchNo);

        WuBaSubmitConversionService service = (WuBaSubmitConversionService) AopContext.currentProxy();
        service.processSuccess(pageList, batchNo, condition);
        return result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public Result processSuccess(List<WubaSubmitConversionData> pushList, String batchNo,
                                 Page2Condition<WubaSubmitConversionData> condition)
        throws Exception {
        Result result = new Result().failure();
        if (CollectionUtils.isEmpty(pushList)) {
            log.warn(TITLE+"processData无数据");
            return result.success();
        }

        // 上报日志表增加记录，submit_result置为0-上报中
        List<WubaSubmitConversionDataLog> dataLogList = pushList.stream().map((WubaSubmitConversionData data) -> {
            WubaSubmitConversionDataLog dataLogRecord = new WubaSubmitConversionDataLog();
            dataLogRecord.setApiCode(data.getApiCode());
            dataLogRecord.setDataId(data.getId());
            dataLogRecord.setCell(data.getCell());
            dataLogRecord.setBatchNo(batchNo);
            dataLogRecord.setSubmitResult(0);
            return dataLogRecord;
        }).collect(Collectors.toList());

        // batAddDataTransferClean
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(dbActionPool, marketingCommonConfig.getWuBaQueryConversionBatDBThreadPool());
        PARTITION_SIZE = marketingCommonConfig.getWuBaQueryConversionBatDBPartitionSize();

        List<CompletableFuture<Void>> dataLogFutures = Lists.newArrayList();
        List<List<WubaSubmitConversionDataLog>> dataLogPartitions = Lists.partition(dataLogList, PARTITION_SIZE);
        for (List<WubaSubmitConversionDataLog> partition : dataLogPartitions) {
            dataLogFutures.add(CompletableFuture.runAsync(() -> {
                try {
                    wubaSubmitConversionDataLogMapper.batchAdd(partition);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),
                            TITLE + "上报日志表增加记录异常"));
                }
            }, dbActionPool));
        }
        CompletableFuture.allOf(dataLogFutures.toArray(new CompletableFuture[0])).join();
        log.warn(TITLE + "上报日志表增加记录成功, batchNo{}", batchNo);

        // 营销名单上报表push_status置为1-推送中
        WubaSubmitConversionData dataUpdate = new WubaSubmitConversionData();
        dataUpdate.setPushStatus(1);
        //
        List<Long> ids = pushList.stream().map((WubaSubmitConversionData data) -> data.getId()).collect(Collectors.toList());
        WubaSubmitConversionDataExample dataExample = new WubaSubmitConversionDataExample();
        dataExample.createCriteria().andIdIn(ids);
        wubaSubmitConversionDataMapper.updateByExampleSelective(dataUpdate, dataExample);
        log.warn(TITLE + "营销名单上报表push_status置为1成功, batchNo{}", batchNo);

        // addDistributeLog
        soleProcessor.addDistributeLog(pushList, condition);
        log.warn(TITLE + "去重表增加记录成功, batchNo{}", batchNo);
        return result.success();
    }

    public Result<String> callClient(List<WubaSubmitConversionData> outputDataList) {
        Result result = new Result<>().failure();
        if (CollectionUtils.isEmpty(outputDataList)) {
            return result;
        }

        int magnitudes = outputDataList.size();
        long startTime = System.currentTimeMillis();
        List<WuBaSubmitDTO> wuBaSubmitDTOS = outputDataList.stream().map((WubaSubmitConversionData data) -> {
//            String marketingTime ="";
//            try {
//                String createDateStr = String.valueOf(data.getCreateDate());
//                Date createDate = DateUtils.parse(createDateStr, "yyyyMMdd");
//                marketingTime = DateUtils.format(createDate, "yyyy-MM-dd 00:00:00");
//            } catch (ParseException e) {
//                marketingTime = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00"));
//            }
            WuBaSubmitDTO wuBaSubmitDTO = new WuBaSubmitDTO();
            wuBaSubmitDTO.setMobile(data.getCell());
            wuBaSubmitDTO.setMarketingTime(data.getMarketingTime());
            return wuBaSubmitDTO;
        }).collect(Collectors.toList());

        // call submitConversionList
        Result callResult = wuBaServiceClient.submitConversionList(wuBaSubmitDTOS);
        if(callResult==null || !callResult.isSuccess() || callResult.getData()==null){
            // Alert
            String resMapStr;
            if (callResult == null || callResult.getData() == null) {
                resMapStr = "调用接口失败";
            } else {
                resMapStr = JSONObject.toJSONString(callResult.getData());
            }
            String msg = String.format(TITLE + "调用接口失败, resMap: %s", resMapStr);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), msg));
            // wuBaDingDingService.sendAlert(TITLE, msg);
            return result;
        }
        String batchNo = (String) callResult.getData();
        if(StringUtils.isEmpty(batchNo)){
            return result;
        }
        long endTime = System.currentTimeMillis();
        log.warn(TITLE+"callClient, 量级{}, 耗时{}", magnitudes, (endTime-startTime));
        return result.success().setDate(batchNo);
    }

    private Map<String, String> calculateMarketingTimeInterval(Integer bizDateInteger) throws ParseException {
        Map<String, String> res = new HashMap<>();
        Date bizDate = DateUtils.parse(String.valueOf(bizDateInteger), "yyyyMMdd");
        Date endDate = new Date(bizDate.getTime() + 86400000L);

        String marketingTimeStart = DateUtils.format(bizDate, "yyyy-MM-dd 00:00:00");
        String marketingTimeEnd = DateUtils.format(endDate, "yyyy-MM-dd 00:00:00");

        res.put("marketingTimeStart", marketingTimeStart);
        res.put("marketingTimeEnd", marketingTimeEnd);
        return res;
    }
}
