package com.br.marketing.api.customer.black.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.arch.geo.pulsar.ProductPulsarClientManager;
import com.br.arch.geo.pulsar.ProductPulsarProducer;
import com.br.common.log.AlertLog;
import com.br.marketing.api.customer.black.adapter.BaseBlackDataAdaptee;
import com.br.marketing.api.customer.black.handler.CustomerBlackDataHandleSingleton;
import com.br.marketing.api.customer.black.handler.CustomerBlackDataHandler;
import com.br.marketing.api.customer.black.handler.CustomerBlackHandlerEnum;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.PulsarTopic;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.CustomerResponseDTO;
import com.br.marketing.dto.ResponseCustomDTO;
import com.br.marketing.entity.CustomizeBlackData;
import com.br.marketing.entity.CustomizeUploadData;
import com.br.marketing.mapper.CustomizeBlackDataMapper;
import com.br.marketing.mapper.CustomizeUploadDataMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.PushRuleService;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;
import javax.annotation.Resource;

import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.stereotype.Service;

/**
 * 定制客户黑名单数据处理
 *
 * @author senyang.zheng
 * @date 2024/08/05
 */
@Service
@Slf4j
public class CustomerBlackDataServiceImpl implements CustomerBlackDataService {

    @Resource
    private CustomerBlackDataHandleSingleton customerBlackDataHandleSingleton;

    @Resource
    private PushRuleService pushRuleService;

    @Resource
    private CustomizeBlackDataMapper customizeBlackDataMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private TrackingService trackingService;

    @Override
    public ResponseCustomDTO receiveCustomizeBlackData(String apiCode, String jsonData) {
        CustomerBlackDataHandler customerBlackDataHandler =
                customerBlackDataHandleSingleton.getCustomerDataHandleImpl(apiCode, CustomerBlackHandlerEnum.B_ALIEN_DEFAULT);
        try {
            BaseBlackDataAdaptee adapter = null;
            CustomerResponseDTO respCustomer = null;
            CustomizeBlackData blackData = new CustomizeBlackData();
            String decryptData = jsonData;
            if (customerBlackDataHandler.customer().getIsNeedDecrypt()) {
                decryptData = customerBlackDataHandler.decryptJsonData(apiCode, jsonData);
                blackData.setExtend(jsonData);
            }
            blackData.setRequestJsonData(decryptData);
            blackData.setReceiveDate(LocalDate.now().toString());
            blackData.setApiCode(apiCode);
            blackData.setCreateTime(new Date());
            blackData.setUpdateTime(new Date());
            String tCid = tableCreateService.getTcId(apiCode);
            if (StringUtils.isEmpty(tCid)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                        "创建客户定制黑名单前置表，未查询到该apiCode:" + apiCode + "对应客户信息，请关注！！！"));
                // 若没查询到cid 入pulsar 待恢复后消费
                respCustomer = sendMq(customerBlackDataHandler, apiCode, jsonData);
                return respCustomer.getResponseCustomDTO();
            } else {
                // 创建定制黑名单表
                blackData.setTCid(tCid);
                customizeBlackDataMapper.createCustomizeBlackDataTable(blackData.getTCid());
            }
            try {
                customerBlackDataHandler.isValidJson(decryptData);
                // 1. 解析json
                adapter = customerBlackDataHandler.parseObject(decryptData);
            } catch (Exception e) {
                respCustomer = customerBlackDataHandler.jsonErrorResponse(e);
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                        "该apiCode:" + apiCode + "定制黑名单接口传参jsonData非json格式！！！"));
            }
            if (respCustomer == null) {
                String requestId = customerBlackDataHandler.getRequestId(apiCode, adapter);
                blackData.setRequestId(requestId);
                try {
                    customerBlackDataHandler.setSourceParam(apiCode, decryptData, adapter);
                    // 2. 有数据验证,包括字段空值及验签
                    respCustomer = customerBlackDataHandler.verifyFields(adapter);
                    if (CustomerResponseDTO.StatusEnum.VALID.equals(respCustomer.getStatusEnum())) {
                        // 3. 计算业务数据量
                        int number = customerBlackDataHandler.countBizDataNumber(adapter);
                        blackData.setBizDataNumber(number);
                    }
                } catch (Exception e) {
                    respCustomer = customerBlackDataHandler.bizErrorResponse(e);
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "该apiCode:" + apiCode + "定制黑名单数据适配标准异常"),
                            e);

                }
            }
            blackData.setStatus(respCustomer.getStatusEnum().getValue());
            blackData.setResponseCode(respCustomer.getResponseCode().toString());
            blackData.setResponseData(JSON.toJSONString(respCustomer.getResponseCustomDTO()));
            // 6. 保存前置数据
            try {
                pushRuleService.mockDbOrRedisError(1, apiCode);
                int i = customizeBlackDataMapper.insertSelective(blackData);
                if (i != 1) {
                    throw new RuntimeException(
                            "定制化客户".concat(customerBlackDataHandler.customer().getName()).concat("(").concat(apiCode).concat(")保存失败,入库数据量:") + i);
                }
                // 7.数据有效，且存储前置完成后进行数据下发（按需实现，默认不处理），
                if (Objects.equals(blackData.getStatus(), CustomerResponseDTO.StatusEnum.VALID.getValue())) {
                    customerBlackDataHandler.dataDirection(tCid, blackData.getId());
                }
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "该apiCode:" + apiCode + "定制黑名单数据写入客户定制黑名单前置表异常"),
                        e);
                // 6.1 数据库容灾
                respCustomer = sendMq(customerBlackDataHandler, apiCode, jsonData);
            }
            try{
                JSONObject condition = new JSONObject();
                condition.put("request_id",blackData.getRequestId());
                trackingService.trackBusinessLog(DataFlowDirection.IN
                        , apiCode
                        , "定制黑名单接口"
                        , String.format("b_customize_black_data_%s", tCid)
                        , JSON.toJSONString(condition)
                        , Long.valueOf(blackData.getBizDataNumber())
                        , TrackingContext.generateBatchId());
            }catch (Exception ex){
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                , ex.getMessage()
                                , "埋点异常")
                        , ex);
            }
            // 8. 返回响应
            return respCustomer.getResponseCustomDTO();
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                    "该apiCode:" + apiCode + "定制黑名单数据接入异常，jsonData:" + jsonData), e);
            return customerBlackDataHandler.fallbackResponse(e).getResponseCustomDTO();
        }
    }

    /**
     * 发送容灾消息，若存在加密情况jsonData需是原文
     *
     * @param customerBlackDataHandler 适配器
     * @param apiCode                  API代码
     * @param jsonData                 json数据
     * @return {@link CustomerResponseDTO }
     * @author senyang.zheng
     * @date 2024/09/11
     */
    private CustomerResponseDTO sendMq(CustomerBlackDataHandler customerBlackDataHandler, String apiCode, String jsonData) {
        try {
            ProductPulsarProducer producer = ProductPulsarClientManager.newProducer(PulsarTopic.uploadCustomTopic);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("apiCode", apiCode);
            jsonObject.put("jsonData", jsonData);
            byte[] messageByte = JSON.toJSONString(jsonObject).getBytes();
            producer.send(messageByte);
            return customerBlackDataHandler.defaultSuccessResponse();
        } catch (PulsarClientException clientException) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "定制黑名单该apiCode:" + apiCode + "入pulsar容灾队列异常"),
                    clientException);
            return customerBlackDataHandler.fallbackResponse(clientException);
        }
    }

    /**
     * 异常消息重新入库
     *
     * @param msg 补偿数据
     * @return {@link Result }<{@link Boolean }>
     * @author senyang.zheng
     * @date 2024/08/07
     */
    @Override
    public Result<Boolean> consumerBlackPayData(String msg) {
        Result<Boolean> result = new Result<>();
        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            String apiCode = jsonObject.getString("apiCode");
            String jsonData = jsonObject.getString("jsonData");
            receiveCustomizeBlackData(apiCode, jsonData);
            result.setCode(ResultCode.SUCCESS.getValue());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "pulsar容灾队列消费异常，pulsar消息：" + msg), e);
            result.setCode(ResultCode.FAIL.getValue());
        }
        return result;
    }
}
