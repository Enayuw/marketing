package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.HaLuoCallBackSecureRules;
import com.br.marketing.entity.MarketingHaloCallBackData;
import com.br.marketing.mapper.MarketingHaLuoCallBackDataMapper;
import com.br.marketing.service.MarketingHaloCallBackDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MarketingHaloCallBackDataServiceImpl implements MarketingHaloCallBackDataService {

    private final static String TITLE = "【哈啰-三方营销数据回传任务】";

    public static final DateTimeFormatter ymdhmsFormat = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_COLON_TIME_FORMAT);

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    private MarketingHaLuoCallBackDataMapper marketingHaLuoCallBackDataMapper;

    @Override
    public void process(String apiCode) {
        log.warn("TITLE:{},apiCode:{},dealDate:{}",TITLE,apiCode, LocalDateTime.now());
        TpDynamicExecutor actionPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.TCYR_CPA_COLLIDING_DEAL.getName(), 50, 50);
        try {
            while (true) {
                //查询
                Integer searchSize = marketingCommonConfig.getHaloCallBackDataConfig().getInteger("searchSize");
                Integer dealStatus = marketingCommonConfig.getHaloCallBackDataConfig().getInteger("dealStatus");
                LocalDateTime startSearchTime = LocalDateTime.parse(
                        marketingCommonConfig.getHaloCallBackDataConfig().getString("startSearchTime"),
                        DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_COLON_TIME_FORMAT)
                );
                List<MarketingHaloCallBackData> marketingHaloCallBackDataList =
                        marketingHaLuoCallBackDataMapper.selectDataList(apiCode,dealStatus,searchSize,startSearchTime);
                if (CollectionUtils.isEmpty(marketingHaloCallBackDataList)) {
                    break;
                }
                //修改中间态
                List<Long> idList = marketingHaloCallBackDataList.stream().map(MarketingHaloCallBackData::getId).collect(Collectors.toList());
                marketingHaLuoCallBackDataMapper.updateDealStatusByIdList(idList,1,"");
                //并发处理
                callBackDataDeal(apiCode, marketingHaloCallBackDataList,actionPool);
            }
            log.warn("TITLE:{},apiCode:{},dealDate:{}",TITLE,apiCode, LocalDateTime.now());
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HALUO_CALLBACK_DATA_INTERFACEERROR.getCode(), e.getMessage(), TITLE), e);
        }finally {
            actionPool.shutdownAndAwaitTermination();
        }
    }

    private void callBackDataDeal(String apiCode, List<MarketingHaloCallBackData> marketingHaloCallBackDataList, TpDynamicExecutor actionPool) {
        List<List<MarketingHaloCallBackData>> partitionList = ListUtils.partition(
                marketingHaloCallBackDataList, marketingCommonConfig.getHaloCallBackDataConfig().getInteger("requestBatchSize"));
        for (List<MarketingHaloCallBackData> itemList : partitionList) {
            actionPool.submit(()->
                    callBackDataRequestDeal(apiCode,itemList)
            );
        }
    }

    private void callBackDataRequestDeal(String apiCode,List<MarketingHaloCallBackData> itemList) {
        List<Long> idList = itemList.stream().map(MarketingHaloCallBackData::getId).collect(Collectors.toList());
        try {
            //1. 封装dataItemList
            JSONObject requestJson = buildCallBackRequestJson(itemList);
            if (requestJson!=null) {
                //2. 调用接口
                Result result = methodRetryHandlerService.haloCallBackData(apiCode,requestJson);
                //3. 修改状态(返回处理成功,失败状态)
                if (result.getCode().equals(ResultCode.SUCCESS.getValue())) {
                    marketingHaLuoCallBackDataMapper.updateDealStatusByIdList(idList,2,"");
                }else {
                    marketingHaLuoCallBackDataMapper.updateDealStatusByIdList(idList,-1,result.getMessage());
                }
            }
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HALUO_CALLBACK_DATA_INTERFACEERROR.getCode(), e.getMessage(), TITLE), e);
            marketingHaLuoCallBackDataMapper.updateDealStatusByIdList(idList,-1,e.getMessage());
        }
    }

    private JSONObject buildCallBackRequestJson(List<MarketingHaloCallBackData> itemList) {
        try {
            String appKey = marketingCommonConfig.getHaloCallBackDataConfig().getString("appKey");
            String appSecret = marketingCommonConfig.getHaloCallBackDataConfig().getString("appSecret");
            String method = marketingCommonConfig.getHaloCallBackDataConfig().getString("method");

            JSONObject obj = new JSONObject();
            int randomNumber = 10000 + ThreadLocalRandom.current().nextInt(90000);
            String openSerialNo = UUID.randomUUID().toString().replace("-", "");
            String batchNo = openSerialNo  +"_"+ LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"))+"_"+randomNumber;
            obj.put("method", method);
            obj.put("appKey", appKey);
            obj.put("timestamp", LocalDateTime.now().format(ymdhmsFormat));
            obj.put("encry","MD5");
            obj.put("channelNo", "BR");

            JSONObject dataObj = new JSONObject();
            dataObj.put("openSerialNo", openSerialNo);
            dataObj.put("bizScene", "LOAN_AGENT");
            List<JSONObject> dataItmes = new ArrayList<>();
            for (MarketingHaloCallBackData item : itemList) {
                JSONObject dataItemObj = new JSONObject();
                dataItemObj.put("id", item.getCustNum());
                dataItemObj.put("phone",item.getCell());
                // TODO 验证文件上传的startTime为秒级时间戳
                dataItemObj.put("startTime",Long.parseLong(item.getStartTime()));
                dataItemObj.put("thirdPartyUserId",item.getCustNum());
                dataItemObj.put("batchNo",batchNo);
                dataItemObj.put("customerNo",item.getCustNum());
                dataItemObj.put("userType","1");
                dataItmes.add(dataItemObj);
            }
            dataObj.put("dataItems", dataItmes);
            obj.put("data", dataObj);
            HaLuoCallBackSecureRules.signTopRequest(obj,appSecret);
            return obj;
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HALUO_CALLBACK_DATA_INTERFACEERROR.getCode(), e.getMessage(), TITLE), e);
            return null;
        }
    }
}
