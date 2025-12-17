package com.br.marketing.service.Impl;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Resource;

import com.br.common.log.AlertLog;
import com.br.marketing.common.constants.PulsarSubscription;
import com.br.marketing.common.constants.rocketmq.MarketingUploadConstants;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.enums.clean.DataSourceTypeEnum;
import com.br.marketing.service.PushRuleService;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.arch.geo.pulsar.ProductPulsarClientManager;
import com.br.arch.geo.pulsar.ProductPulsarProducer;
import com.br.common.encryption.Md5Utils;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.PulsarTopic;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.dto.ResponseCustomDTO;
import com.br.marketing.dto.shuhe.Response2ShuheDTO;
import com.br.marketing.dto.shuhe.ResponseShuheDTO;
import com.br.marketing.dto.shuhe.ShuheTransferJsonDTO;
import com.br.marketing.entity.CaseShuheUploadData;
import com.br.marketing.mapper.CaseShuheUploadDataMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.IPushShuheDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.utils.PulsarConsumerSkipUtil;
import com.br.marketing.util.ApiFieldCheckUtils;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * 数禾转化实现类
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 14:25
 */
@Service
@Slf4j
public class PushShuheDataServiceImpl implements IPushShuheDataService {

    @Resource
    private RedisChgService redisChgService;
    @Resource
    private RabbitMqProducter producter;
    @Resource
    private CaseShuheUploadDataMapper caseShuheUploadDataMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private PulsarConsumerSkipUtil pulsarConsumerSkipUtil;
    @Resource
    private RocketMqSwitch rocketMqSwitch;
    @Resource
    private AlarmApiClient alarmClient;
    @Autowired
    private RocketMqTemplate template;

    @Autowired
    PushRuleService pushRuleService;

    @Resource
    TrackingService trackingService;

    private static final ThreadPoolExecutor BR_EXECUTORS = BrExecutors.getThreadPool(1, 2);
    private final String title = "数禾转化数据定制化清洗入库";
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter yyMMddHH = DateTimeFormatter.ofPattern("yyMMdd");
    private static final Map<String, String> USERTYPE_CONTAINS_MAP;

    static {
        USERTYPE_CONTAINS_MAP = new HashMap<>();
        USERTYPE_CONTAINS_MAP.put("首登", "促首登");
        USERTYPE_CONTAINS_MAP.put("申完", "促申完");
        USERTYPE_CONTAINS_MAP.put("重申", "重申");
        USERTYPE_CONTAINS_MAP.put("首借", "促首借");
        USERTYPE_CONTAINS_MAP.put("复借", "促复借");
        USERTYPE_CONTAINS_MAP.put("轻资产", "轻资产");
    }

    @Autowired
    ShuHeUserServiceImpl shuHeUserService;

    @Override
    public ResponseCustomDTO saveShuheTransferDataTwoVersion(String apiCode, String jsonData) {
        ResponseShuheDTO responseShuheDTO = new ResponseShuheDTO();
        responseShuheDTO.success();
        ShuheTransferJsonDTO jsonDTO = null;
        try {
            jsonDTO = JSONObject.parseObject(jsonData, new TypeReference<ShuheTransferJsonDTO>() {}.getType());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            responseShuheDTO.failed("抱歉,解析json数据失败！");
            return responseShuheDTO;
        }
        // 1、校验参数合法性
        String msg = nonNullCheck(jsonDTO);
        if (!"".equals(msg)) {
            responseShuheDTO.failed("抱歉,缺失必填参数！缺失参数为：".concat(msg));
            msg = "缺失必填参数:".concat(msg).concat("\napiCode“" + apiCode).concat("”\nuserType“" + jsonDTO.getBizType())
                .concat("”\n案件编号“" + jsonDTO.getOrderId()).concat("”\n").concat("请及时跟进或与数禾客户及时沟通^_^");
            this.sendAlarmMgsUrgent(title, msg, alarmClient);
            return responseShuheDTO;
        }
        SecureRandom random = new SecureRandom();
        String requestId = Md5Utils.cell32(jsonData.concat("@" + System.currentTimeMillis()).concat("#" + random.nextInt(10000)));
        try {
            shuHeUserService.saveShTransferData(apiCode, jsonData, requestId, null);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            ProductPulsarProducer producer = null;
            try {
                producer = ProductPulsarClientManager.newProducer(PulsarTopic.transferShTopic);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("apiCode", apiCode);
                jsonObject.put("jsonData", jsonData);
                jsonObject.put("requestId", requestId);
                jsonObject.put("time", LocalDateTime.now().format(dateTimeFormatter));
                String jsonString = jsonObject.toJSONString();
                byte[] message = jsonString.getBytes();
                producer.send(message);
                log.warn(String.format("写入Pulsar 主题:%s requestId:%s", PulsarTopic.transferShTopic, requestId));
            } catch (PulsarClientException e) {
                responseShuheDTO.failed();
            }
            return responseShuheDTO;
        }
        return responseShuheDTO;

    }

    @Override
    public Result<Boolean> consumerShTransfer(String msg) {
        // 检查是否需要跳过业务逻辑
        if (pulsarConsumerSkipUtil.shouldSkipBusinessLogic(PulsarSubscription.transferShSubscription)) {
            log.warn("【pulsar】数禾转化数据执行跳过逻辑");
            return new Result<>().setCode(ResultCode.SUCCESS.getValue());
        }
        
        JSONObject jb = JSON.parseObject(msg);
        String requestId = jb.getString("requestId");
        String jsonData = jb.getString("jsonData");
        String apiCode = jb.getString("apiCode");
        String time = jb.getString("time");
        Date createTime = null;
        try {
            createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        try {
            // todo 测试pulsar 上线删除
            JSONObject testJb = JSONObject.parseObject(jsonData);
            if ("1".equals(testJb.getString("test"))) {
                testJb.remove("test");
                jsonData = JSON.toJSONString(testJb);
            }
            shuHeUserService.saveShTransferData(apiCode, jsonData, requestId, createTime);
        } catch (DuplicateKeyException keyException) {
            log.error(String.format("数禾转化数据pulsar消费重复requestId requestId:%s,jsonData:%s,apiCode:%s", requestId, jsonData, apiCode));
            return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue());
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue());
    }

    private String nonNullCheck(ShuheTransferJsonDTO jsonDTO) {
        String msg = "";
        if (StringUtils.isEmpty(jsonDTO.getOrderId())) {
            msg += "orderId,释义：批量上传案件编号；";
        }
        if (StringUtils.isEmpty(jsonDTO.getMobile())) {
            msg += "mobile,释义：手机号；";
        }
        if (jsonDTO.getDataItem() == null || jsonDTO.getDataItem().size() < 1) {
            msg += "dataItem,释义：扩展字段；";
        }
        return msg;
    }

    @Override
    public ResponseCustomDTO saveUploadData(String apiCode, String jsonData) {
        String requestId = buildRequestId(apiCode);
        CaseShuheUploadData shuheUploadData = new CaseShuheUploadData();
        shuheUploadData.setJsonData(jsonData);
        shuheUploadData.setUploadDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        shuheUploadData.setCreateTime(new Date());
        shuheUploadData.setUpdateTime(shuheUploadData.getCreateTime());
        shuheUploadData.setApiCode(apiCode);
        shuheUploadData.setRequestId(requestId);
        Response2ShuheDTO response2ShuheDTO = new Response2ShuheDTO();
        response2ShuheDTO.setMsgId(shuheUploadData.getRequestId());
        if (org.apache.commons.lang3.StringUtils.isBlank(jsonData)) {
            response2ShuheDTO.failed(",内容不可为空");
            exceptionSave(shuheUploadData, response2ShuheDTO, null);
            return response2ShuheDTO;
        }
        final JSONObject uploadDataDTO;
        try {
            uploadDataDTO = JSONObject.parseObject(jsonData);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            response2ShuheDTO.failed(",JSON结构解析失败");
            exceptionSave(shuheUploadData, response2ShuheDTO, e);
            return response2ShuheDTO;
        }
        if (marketingCommonConfig.getShuheDxApiCodes().contains(apiCode) && !JSONObject.isValidObject(uploadDataDTO.getString("taskCode"))) {
            log.error("数禾上传数据定制化接口taskCode非JSON结构,taskCode:{}", uploadDataDTO.getString("taskCode"));
            exceptionSave(shuheUploadData, response2ShuheDTO, null);
            return response2ShuheDTO.success();
        }
        final JSONArray listInfo = uploadDataDTO.getJSONArray("listInfo");
        if (requiredCheck(listInfo, response2ShuheDTO)) {
            exceptionSave(shuheUploadData, response2ShuheDTO, null);
            return response2ShuheDTO;
        }
        shuheUploadData.setUserType(getUserType(uploadDataDTO));
        Long infoId = null;
        try {
            infoId = shuHeUserService.saveShUploadData(shuheUploadData, uploadDataDTO, listInfo);
            //发送json解析MQ
            pushRuleService.sendJsonParseMq(shuheUploadData.getApiCode(), infoId.toString(), DataSourceTypeEnum.GENERAL_INTERFACE.getCode());

        } catch (DuplicateKeyException keyException) {
            log.error(String.format("数禾上传数据重复requestId requestId:%s,jsonData:%s,apiCode:%s", requestId, jsonData, apiCode));
            return response2ShuheDTO.success();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            ProductPulsarProducer producer = null;
            try {
                producer = ProductPulsarClientManager.newProducer(PulsarTopic.upLoadShTopic);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("apiCode", apiCode);
                jsonObject.put("requestId", requestId);
                jsonObject.put("jsonData", jsonData);
                jsonObject.put("time", LocalDateTime.now().format(dateTimeFormatter));
                String jsonString = jsonObject.toJSONString();
                byte[] message = jsonString.getBytes();
                producer.send(message);
                log.warn(String.format("写入Pulsar 主题:%s 数据:%s", PulsarTopic.upLoadShTopic, jsonString));
            } catch (PulsarClientException e) {
                response2ShuheDTO.failed(",内部错误");
                return response2ShuheDTO;
            }
        }
        if (infoId != null) {
            if(rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingUploadConstants.TAG_MARKETING_PRE_USER_SHUHE_RECEIVE)){
                rocketMqSwitch.syncSend(MarketingUploadConstants.TOPIC, MarketingUploadConstants.TAG_MARKETING_PRE_USER_SHUHE_RECEIVE
                        , infoId.toString());
            }else{
                producter.send(MQConstants.ROUTING_KEY_MARKETING_PRE_USER_SHUHERECEIVE, infoId.toString());
            }
        }
        BR_EXECUTORS.execute(() -> checkField(uploadDataDTO, listInfo, apiCode, requestId));
        // 埋点
        try {
            JSONObject condition = new JSONObject();
            condition.put("request_id", shuheUploadData.getRequestId());
            trackingService.trackBusinessLog(DataFlowDirection.IN
                    , apiCode
                    , "数禾定制上传接口"
                    , "b_case_shuhe_upload_data"
                    , JSON.toJSONString(condition)
                    , 1L
                    , TrackingContext.generateBatchId());
        }catch (Exception ex){
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                            , ex.getMessage()
                            , "埋点异常")
                    , ex);
        }
        return response2ShuheDTO.success();
    }

    /**
     * 2024-05-08 11:05 【紧急】D20240507数禾电销上传接口迁移-3710117 https://c.100credit.cn/pages/viewpage.action?pageId=145112459 兼容场景，简称映射
     */
    private String getUserType(JSONObject uploadDataDTO) {
        if (uploadDataDTO.containsKey("extraInfo")) {
            String userType = uploadDataDTO.getString("extraInfo");
            if (StringUtils.hasText(userType)) {
                return userType;
            }
        }
        String templateName = uploadDataDTO.getString("templateName");
        if (StringUtils.isEmpty(templateName)) {
            return "";
        }
        for (Map.Entry<String, String> entry : USERTYPE_CONTAINS_MAP.entrySet()) {
            if (templateName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return templateName;
    }

    @Override
    public Result<Boolean> consumerShUpload(String msg) {
        // 检查是否需要跳过业务逻辑
        if (pulsarConsumerSkipUtil.shouldSkipBusinessLogic(PulsarSubscription.upLoadShSubscription)) {
            log.warn("【pulsar】数禾上传数据执行跳过逻辑");
            return new Result<>().setCode(ResultCode.SUCCESS.getValue());
        }
        
        JSONObject jb = JSON.parseObject(msg);
        String requestId = jb.getString("requestId");
        String jsonData = jb.getString("jsonData");
        String apiCode = jb.getString("apiCode");
        String time = jb.getString("time");
        Date dataTime = null;
        try {
            dataTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        CaseShuheUploadData shuheUploadData = new CaseShuheUploadData();
        shuheUploadData.setJsonData(jsonData);
        shuheUploadData.setUploadDate(new SimpleDateFormat("yyyy-MM-dd").format(dataTime));
        shuheUploadData.setCreateTime(dataTime);
        shuheUploadData.setUpdateTime(shuheUploadData.getCreateTime());
        shuheUploadData.setApiCode(apiCode);
        shuheUploadData.setRequestId(requestId);
        JSONObject uploadDataDTO = JSONObject.parseObject(jsonData);
        shuheUploadData.setUserType(getUserType(uploadDataDTO));
        final JSONArray listInfo = uploadDataDTO.getJSONArray("listInfo");
        Long infoId = null;
        try {
            // todo 测试pulsar 上线删除
            if ("1".equals(uploadDataDTO.getString("test"))) {
                uploadDataDTO.remove("test");
            }
            infoId = shuHeUserService.saveShUploadData(shuheUploadData, uploadDataDTO, listInfo);
            //发送json解析MQ
            pushRuleService.sendJsonParseMq(shuheUploadData.getApiCode(), infoId.toString(), DataSourceTypeEnum.GENERAL_INTERFACE.getCode());
        } catch (DuplicateKeyException keyException) {
            log.error(String.format("数禾上传数据pulsar消费重复requestId requestId:%s,jsonData:%s,apiCode:%s", requestId, jsonData, apiCode));
            return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue());
        }
        if (infoId != null) {
            String idString = infoId.toString();
            if(rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingUploadConstants.TAG_MARKETING_PRE_USER_SHUHE_RECEIVE)){
                rocketMqSwitch.syncSend(MarketingUploadConstants.TOPIC, MarketingUploadConstants.TAG_MARKETING_PRE_USER_SHUHE_RECEIVE
                        , idString);
            }else{
                producter.send(MQConstants.ROUTING_KEY_MARKETING_PRE_USER_SHUHERECEIVE, idString);
            }
            return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue());
        }
        return new Result<Boolean>().setCode(ResultCode.FAIL.getValue());
    }

    /**
     * 2022/9/1 9:06 异常保存
     */
    private void exceptionSave(CaseShuheUploadData shuheUploadData, Response2ShuheDTO response2ShuheDTO, Exception e) {
        shuheUploadData.setErrorInfo(response2ShuheDTO.getDesc() + (e == null ? "" : "\n" + e.getMessage()));
        shuheUploadData.setStatus(1);
        shuheUploadData.setSaveInfoStatus(1);
        try {
            int i = caseShuheUploadDataMapper.insertSelective(shuheUploadData);
            if (i == 1) {
                response2ShuheDTO.setMsgId(serialNumberAddId(shuheUploadData));
            } else {
                String mgs = "数禾上传数据异常数据入库失败";
                BusinessException exception = new BusinessException(mgs);
                exception.setExceptionMessage(mgs);
                throw exception;
            }
        } catch (Exception exception) {
            log.error(exception.getMessage() + "\nrequestId:" + shuheUploadData.getRequestId() + "\napiCode:" + shuheUploadData.getApiCode()
                + "\njsonData:" + shuheUploadData.getJsonData(), exception);
        }
    }

    /**
     * 2022/9/1 10:45 必填检查
     */
    private boolean requiredCheck(JSONArray listInfo, Response2ShuheDTO response2ShuheDTO) {
        if (CollectionUtils.isEmpty(listInfo)) {
            try {
                response2ShuheDTO.failed(",名单列表内容为空");
                BusinessException exception = new BusinessException(response2ShuheDTO.getDesc());
                exception.setExceptionMessage(response2ShuheDTO.getDesc());
                throw exception;
            } catch (BusinessException e) {
                log.error(e.getMessage(), e);
                return true;
            }
        }
        return false;
    }

    /**
     * 2023-12-25 10:56 数禾上传数据检查字段
     *
     * @param uploadDataDTO 上传数据外层
     * @param listInfo 上传数据内层
     * @param apiCode 客户编号
     * @param requestId 请求流水号
     */
    private void checkField(JSONObject uploadDataDTO, JSONArray listInfo, String apiCode, String requestId) {
        try {
            Set<String> keySet = new HashSet<>(uploadDataDTO.keySet());
            int size = listInfo.size();
            for (int i = 0; i < size; i++) {
                JSONObject info = listInfo.getJSONObject(i);
                keySet.addAll(info.keySet());
                JSONObject varData = info.getJSONObject("varData");
                if (varData != null) {
                    keySet.addAll(varData.keySet());
                }
            }
            ApiFieldCheckUtils.checkField(keySet, redisChgService, apiCode, "上海数禾", "receiveShuHeUploadData", requestId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 数禾上传接口创建requestID
     *
     * @param apiCode 日期（6，年后2+月2+日2）_apiCode（7）_毫秒（13）_随机数（6）
     * @return
     */
    private String buildRequestId(String apiCode) {
        SecureRandom random = new SecureRandom();
        return LocalDateTime.now().format(yyMMddHH).concat("_" + apiCode).concat("_" + System.currentTimeMillis())
            .concat("_" + random.nextInt(999999));
    }

    /**
     * 2022/8/30 18:04 业务流水流水号生成规则： 1.流水号+数据库id 2.业务流水号总长：19+id长度，如使用bigint类型，则：19+19=38 eg: 2208300004611020223123456 id为123456
     */
    private String serialNumberAddId(CaseShuheUploadData data) {
        return data.getRequestId() + (data.getId() > 0 ? data.getId() : "");
    }
}
