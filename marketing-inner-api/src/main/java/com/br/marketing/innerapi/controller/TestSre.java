package com.br.marketing.innerapi.controller;

import com.alibaba.fastjson.JSON;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.constants.rocketmq.MarketingAssistConstants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.dto.dataclean.mq.MqDataJsonParse;
import com.br.marketing.entity.MarketingCustomerOriginalData;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.entity.RequestLog;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.mapper.rulecleaning.MarketingCustomerOriginalDataMapper;
import com.br.marketing.rpcclient.rpcclientImpl.BrokerGrpcClient;
import com.br.marketing.rpcclient.rpcclientImpl.DecodeGrpcClient;
import com.br.marketing.rpcclient.rpcclientImpl.UserCenterGrpcClient;
import com.br.marketing.service.clean.common.DataCleanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerService;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

/**
 * 后面遇到架构升级不方便测试或自测的时候可以在这里提供调用的入口
 * @author yu.xia@brgroup.com
 * @Date 2024/1/23 11:15
 */
@RestController
@RequestMapping("/sre/")
@Slf4j
public class TestSre {

    @Resource
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    private RocketMqTemplate template;
    @Resource
    private RocketMqSwitch rocketMqSwitch;

    @Autowired
    InterfaceHandlerService interfaceHandlerService;

    @Resource
    MarketingCustomerOriginalDataMapper marketingCustomerOriginalDataMapper;

    @Autowired
    RedisChgService redisChgService;

    @Resource
    private DataCleanService dataCleanService;

    @GetMapping("/testMq")
    public void testMq(String msg){
        dataCleanService.customerDataJsonParse(msg);
    }

    @GetMapping("/testToPolicy")
    public void testToPolicy(String msg){
        interfaceHandlerService.handleDataDirection(msg);
    }

    @GetMapping("/testSre")
    public String testApiToDb(@RequestParam("all") String all,@RequestParam("key") String key){
        boolean allFlag = false;
        if(null != all && "WhoAreYou".equals(all)){
            allFlag = true;
        }
        if(allFlag || "log".equals(key)){
            log.warn("warn-testSre-key:[{}]", key);
            log.error("error-testSre-key:[{}]", key);
        }
        if(allFlag || "speed".equals(key)){
            log.warn("testSre-speed-key:[{}]-value:[{}]", key, JSON.toJSONString(marketingCommonConfig));
        }
        if(allFlag || "grpc".equals(key)){
            // 解密grpc
            String query = DecodeGrpcClient.query("913fb4b537fb433d437edabdfe23b256", "cell"
                    , "md5", "7410086_20240123135700_1234");
            log.warn("解密grpc-:[{}]", query);
            // 商户中心
            String companyMsg = UserCenterGrpcClient.getCompanyMsg("7410086");
            log.warn("商户中心grpc-:[{}]", companyMsg);
            // 用户中心
            MerchantParam merchantParam = UserCenterGrpcClient.getMerchantParam("7410785");
            log.warn("用户中心grpc-:[{}]", JSON.toJSONString(merchantParam));
            // 给MOM发送上传数据
            BrokerGrpcClient.sendUploadLog("{\"one\":\"value1\",\"two\":\"value2\"}");
            // 给MOM发送请求数据
            RequestLog requestLog = new RequestLog();
            requestLog.setRequestTime(new Date());
            requestLog.setApiCode("111111");
            requestLog.setRequestStr("{\"proxy_source\":\"1\",\"name\":\"C11TBlBcVFUΒ4FDlBSUwlXAA5cU1AOUgUECV0CUVcEBwc\"" +
                    ",\"strategy_id\":\"DTA_BR0002113\",\"id\":\"CwxRVgJRBlhdBQEIV1xUAΒ7QUNDARWVFUKW1QHAgBeAAI\"," +
                    "\"cell\":\"Ww9SDgZdAwMCDAJTVgQFBQAOWgQCAwgCVF8DΒ4UlACUQQ\",\"custom_request\":\"Md5\"}");
            requestLog.setResponseStr("{\"code\":\"00\",\"swift_number\":\"3030994_20240117143038_37761B55A19\"," +
                    "\"DataStrategy\":{\"strategy_version\":\"1.1\",\"product_type\":\"100081\"," +
                    "\"strategy_id\":\"DTA_BR0002113\",\"product_name\":\"预置_借贷意向验证\",\"scene\":\"lend\"}," +
                    "\"Flag\":{\"applyloanstr\":\"1\",\"datastrategy\":\"1\"}}");
            requestLog.setResponseTime(new Date());
            requestLog.setCostTime(10);
            requestLog.setSwiftNumber("111111_20240117_2222");
            requestLog.setUrl("test");
            requestLog.setCode("00");
            BrokerGrpcClient.sendRequestLog(requestLog);
        }
        return "success";
    }

    @RequestMapping("/testSend")
    public SendResult testSend(@RequestParam("topic") String topic
            , @RequestParam("tag") String tag
            , @RequestParam("msg") Object msg
            , @RequestParam("type") String type) {
        if("syncSend".equalsIgnoreCase(type)){
            return rocketMqSwitch.syncSend(topic, tag, msg.toString());
        }else if("syncSendDelay".equalsIgnoreCase(type)){
            return rocketMqSwitch.syncSendDelaySecond(topic, tag, msg.toString(), 100);
        }else if("sendSyncOrderly".equalsIgnoreCase(type)){
            return template.sendSyncOrderly(topic, tag, msg,"orderly");
        }
        return null;
    }

    @RequestMapping("/testRocketMQSwitchFlag")
    public String testRocketMQSwitchFlag(@RequestParam("tag") String tag
            , @RequestParam("apiCode") String apiCode
            , @RequestParam("type") String type) {
        if("1".equalsIgnoreCase(type) && rocketMqSwitch.rocketMQSwitchFlag(apiCode, tag)){
            return "MQ";
        }
        if("2".equalsIgnoreCase(type) && rocketMqSwitch.rocketLogSwitchFlag(tag)){
            return "log";
        }
        return "null";
    }


    /**
     * 智能营销定制数据落库接口
     *
     * @param apiCode
     * @param jsonData
     * @return
     */
    @Operation(summary = "定制接入营销数据")
    @PostMapping("/receiveMarketingData")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public ApiNoDataResult receiveMarketingData(@RequestParam("apiCode") String apiCode, @RequestParam("jsonData") String jsonData) {
        Result result = new Result().setCode(ResultCode.SUCCESS.getValue());
        MarketingCustomerOriginalData originalData = new MarketingCustomerOriginalData();
        originalData.setApiCode(apiCode);
        originalData.setRequestId(UUID.randomUUID().toString());
        originalData.setJsonData(jsonData);
        originalData.setDataType(DataProcessEnum.DataTypeEnum.UPLOAD.getCode());
        originalData.setAcceptType(DataProcessEnum.AcceptTypeEnum.CUSTOM.getCode());
        originalData.setReceiveDate(LocalDate.now().toString());
        marketingCustomerOriginalDataMapper.insertSelective(originalData);
        //发送消息
        //一个apiCode，一天只发送一条消息进行json结构解析
        String redisKey = RedisKeyConstant.ORIGINAL_DATA_JSON_PARSE.concat(apiCode).concat(":").concat(DataProcessEnum.DataTypeEnum.UPLOAD.getCode().toString()).concat(":")
                .concat(DataProcessEnum.AcceptTypeEnum.CUSTOM.getCode().toString()).concat(":").concat(LocalDate.now().toString());
        boolean exists = redisChgService.exists(redisKey);
        if (exists) {
            return new ApiNoDataResult().fromResult(result);
        }
        MqDataJsonParse mqDataJsonParse = new MqDataJsonParse();
        mqDataJsonParse.setDataId(originalData.getId());
        mqDataJsonParse.setSystemType(DataProcessEnum.SystemTypeEnum.MARKETING.getCode());
        mqDataJsonParse.setDataType(DataProcessEnum.DataTypeEnum.UPLOAD.getCode());
        mqDataJsonParse.setAcceptType(DataProcessEnum.AcceptTypeEnum.CUSTOM.getCode());
//        producter.send(MQConstants.ROUTING_KEY_MARKETING_CUSTOMER_DATA_JSON_PARSE, JSON.toJSONString(mqDataJsonParse));
        rocketMqSwitch.sendMessage(apiCode, MarketingAssistConstants.TOPIC, MarketingAssistConstants.TAG_MARKETING_CUSTOMER_DATA_JSON_PARSE,
                JSON.toJSONString(mqDataJsonParse), MQConstants.ROUTING_KEY_MARKETING_CUSTOMER_DATA_JSON_PARSE);
        return new ApiNoDataResult().fromResult(result);
    }

}
