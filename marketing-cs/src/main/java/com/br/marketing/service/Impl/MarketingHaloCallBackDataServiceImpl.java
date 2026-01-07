package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.HaloCallBackDataApiClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.HaLuoCallBackSecureRules;
import com.br.marketing.entity.MarketingHaloCallBackData;
import com.br.marketing.enums.HaloCallBackDealStatusEnum;
import com.br.marketing.mapper.MarketingHaloCallBackDataMapper;
import com.br.marketing.service.MarketingHaloCallBackDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    private HaloCallBackDataApiClient haLoCallBackDataApiClient;

    @Resource
    private MarketingHaloCallBackDataMapper marketingHaLuoCallBackDataMapper;

    @Override
    public void process(String apiCode) {
        long startTime = System.currentTimeMillis();
        log.warn("TITLE:{},apiCode:{},开始处理",TITLE,apiCode);
        TpDynamicExecutor actionPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.HALO_CALLBACK_DATA_3710217.getName(), 20, 20);
        try {
            while (true) {
                //查询
                List<MarketingHaloCallBackData> marketingHaloCallBackDataList =
                        marketingHaLuoCallBackDataMapper.selectDataList(apiCode,0,2000);
                if (CollectionUtils.isEmpty(marketingHaloCallBackDataList)) {
                    break;
                }
                //修改中间态
                List<Long> idList = marketingHaloCallBackDataList.stream().map(MarketingHaloCallBackData::getId).collect(Collectors.toList());
                marketingHaLuoCallBackDataMapper.updateDealStatusByIdList(idList, HaloCallBackDealStatusEnum.DEAL_MIDDLE.getValue(),"");
                //并发处理
                callBackDataDeal(apiCode, marketingHaloCallBackDataList,actionPool);
            }
            log.warn("TITLE:{},apiCode:{}处理完成,消耗时间:{}ms",TITLE,apiCode,System.currentTimeMillis()-startTime);
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HALUO_CALLBACK_DATA_INTERFACEERROR.getCode(), e.getMessage(), TITLE), e);
        }finally {
            actionPool.shutdownAndAwaitTermination();
        }
    }

    private void callBackDataDeal(String apiCode, List<MarketingHaloCallBackData> marketingHaloCallBackDataList, TpDynamicExecutor actionPool) {
        List<List<MarketingHaloCallBackData>> partitionList = ListUtils.partition(
                marketingHaloCallBackDataList, 20);
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
                Result result = new Result().success();
                if(marketingCommonConfig.getHaloCallBackDataConfig().getInteger("mockStatus")!=1) {
                    String httpUrl = marketingCommonConfig.getHaloCallBackDataConfig().getString("openApiUrl");
                    result = haLoCallBackDataApiClient.dealMarketingCallBack(apiCode,httpUrl,requestJson);
                }else {
                    log.warn("TITLE:{},apiCode:{} mock测试,requestJson:{}",TITLE,apiCode,requestJson);
                }
                //3. 修改状态(返回处理成功,失败状态)
                if (result.getCode().equals(ResultCode.SUCCESS.getValue())) {
                    marketingHaLuoCallBackDataMapper.updateDealStatusByIdList(idList,HaloCallBackDealStatusEnum.DEAL_SUCCESS.getValue(),"");
                }else {
                    //存储 错误返回的客户信息
                    marketingHaLuoCallBackDataMapper.updateDealStatusByIdList(idList,
                            HaloCallBackDealStatusEnum.DEAL_FAIL.getValue(), result.getMessage());
                }
            }
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HALUO_CALLBACK_DATA_INTERFACEERROR.getCode(), e.getMessage(), TITLE), e);
            marketingHaLuoCallBackDataMapper.updateDealStatusByIdList(idList,HaloCallBackDealStatusEnum.DEAL_FAIL.getValue(),"");
        }
    }

    private JSONObject buildCallBackRequestJson(List<MarketingHaloCallBackData> itemList) {
        List<Long> idList = itemList.stream().map(MarketingHaloCallBackData::getId).collect(Collectors.toList());
        try {
            String appKey = marketingCommonConfig.getHaloCallBackDataConfig().getString("appKey");
            String appSecret = marketingCommonConfig.getHaloCallBackDataConfig().getString("appSecret");
            String method = marketingCommonConfig.getHaloCallBackDataConfig().getString("method");

            JSONObject obj = new JSONObject();
            obj.put("method", method);
            obj.put("appKey", appKey);
            obj.put("timestamp", LocalDateTime.now().format(ymdhmsFormat));
            obj.put("encry","MD5");
            obj.put("channelNo", "BR");

            JSONObject dataObj = new JSONObject();
            String openSerialNo = UUID.randomUUID().toString().replace("-", "");
            int randomNumber = 10000 + ThreadLocalRandom.current().nextInt(90000);
            String batchNo = openSerialNo  +"_"+ LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"))+"_"+randomNumber;
            dataObj.put("openSerialNo", openSerialNo);
            dataObj.put("bizScene", "LOAN_AGENT");
            List<JSONObject> dataItmes = new ArrayList<>();
            for (MarketingHaloCallBackData item : itemList) {
                JSONObject dataItemObj = new JSONObject();
                dataItemObj.put("id", item.getCustNum());
                dataItemObj.put("phone",item.getCell());
                dataItemObj.put("startTime",getStartTimeBySftp(item.getStartTime()));
                dataItemObj.put("thirdPartyUserId",item.getCustNum());
                dataItemObj.put("batchNo",batchNo);
                dataItemObj.put("customerNo",item.getCustNum());
                dataItemObj.put("userType","1");
                Map<Object,Object> extra = new HashMap<>();
                extra.put("modelCode",item.getModelCode());
                dataItemObj.put("extra",extra);
                dataItmes.add(dataItemObj);
            }
            dataObj.put("dataItems", dataItmes);
            obj.put("data", dataObj);
            HaLuoCallBackSecureRules.signTopRequest(obj,appSecret);
            return obj;
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.HALUO_CALLBACK_DATA_INTERFACEERROR.getCode(), e.getMessage(), TITLE), e);
            marketingHaLuoCallBackDataMapper.updateDealStatusByIdList(idList,HaloCallBackDealStatusEnum.DEAL_FAIL.getValue(),"封装请求数据失败");
            return null;
        }
    }

    /**
     * 验证文件上传的startTime为秒级时间戳
     * @param startTime
     * @return
     */
    private Long getStartTimeBySftp(String startTime) {
        if (startTime == null || "".equals(startTime)) {
            return 0L;
        }else {
            ZoneId zoneId = ZoneId.of("Asia/Shanghai");
            LocalDateTime localDateTime = LocalDateTime.parse(startTime, ymdhmsFormat);
            return localDateTime.atZone(zoneId).toEpochSecond();
        }
    }
}
