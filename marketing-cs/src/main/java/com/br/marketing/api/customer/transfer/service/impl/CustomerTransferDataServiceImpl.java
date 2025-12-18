package com.br.marketing.api.customer.transfer.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.arch.geo.pulsar.ProductPulsarClientManager;
import com.br.arch.geo.pulsar.ProductPulsarProducer;
import com.br.common.encryption.Md5Utils;
import com.br.common.log.AlertLog;
import com.br.marketing.api.customer.transfer.adapter.CustomerDataAdapter;
import com.br.marketing.api.customer.transfer.adapter.TransferDataAdaptee;
import com.br.marketing.api.customer.transfer.handler.CustomerDataHandleSingleton;
import com.br.marketing.api.customer.transfer.handler.CustomerDataHandler;
import com.br.marketing.api.customer.transfer.handler.CustomerHandlerEnum;
import com.br.marketing.api.customer.transfer.service.CustomerTransferDataService;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.PulsarSubscription;
import com.br.marketing.common.constants.PulsarTopic;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.utils.PulsarConsumerSkipUtil;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.CustomerResponseDTO;
import com.br.marketing.dto.ResponseCustomDTO;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.CustomerTransferDataReceive;
import com.br.marketing.mapper.CustomerTransferDataReceiveMapper;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ApiFieldCheckUtils;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 定制客户转化数据处理
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-18 16:05
 */
@Service
@Slf4j
public class CustomerTransferDataServiceImpl implements CustomerTransferDataService {

    @Resource
    private CustomerDataHandleSingleton customerDataHandleSingleton;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private PulsarConsumerSkipUtil pulsarConsumerSkipUtil;

    @Resource
    private PushRuleService pushRuleService;

    @Resource
    private CustomerDataAdapter customerDataAdapter;

    @Resource
    private CustomerTransferDataReceiveMapper customerTransferDataReceiveMapper;

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private TrackingService trackingService;

    private static final ThreadPoolExecutor BR_EXECUTORS = BrExecutors.getThreadPool(1, 10
            , "订制转化数据接入字段检查");

    @Override
    public ResponseCustomDTO receiveTransferDataHandler(String apiCode, String jsonData) {
        CustomerDataHandler customDataHandleImpl = customerDataHandleSingleton.getCustomerDataHandleImpl(apiCode
                , CustomerHandlerEnum.T_ALIEN_DEFAULT);
        try {
            CustomerTransferDataReceive receive = new CustomerTransferDataReceive();
            receive.setRequestJsonData(jsonData);
            receive.setReceiveDate(LocalDate.now().toString());
            receive.setApiCode(apiCode);
            receive.setCreateTime(new Date());
            receive.setUpdateTime(receive.getCreateTime());
            TransferDataAdaptee adaptee = null;
            CustomerResponseDTO respCustomer = null;
            try {
                customDataHandleImpl.isValidJson(jsonData);
                // 1. 解析json
                adaptee = customDataHandleImpl.parseObject(jsonData);
            } catch (Exception e) {
                respCustomer = customDataHandleImpl.jsonErrorResponse(e);
                log.error(e.getMessage() + jsonData, e);
            }
            String requestId = null;
            if (respCustomer == null) {
                try {
                    customDataHandleImpl.setSourceParam(apiCode, jsonData, adaptee);
                    // 2. 有数据验证,包括字段空值及验签
                    respCustomer = customDataHandleImpl.verifyFields(adaptee);
                    if (CustomerResponseDTO.StatusEnum.VALID.equals(respCustomer.getStatusEnum())) {
                        // 3. 计算业务数据量
                        int number = customDataHandleImpl.countBizDataNumber(adaptee);
                        receive.setBizDataNumber(number);
                        // 4. 适配
                        TransferDataDTO<TransferDataItemDTO> transferDataDTO = customerDataAdapter.transferDataRequest(adaptee);
                        if (transferDataDTO != null) {
                            requestId = getRequestId(apiCode, transferDataDTO.getRequestId());
                            transferDataDTO.setRequestId(requestId);
                            // 5. 发送标准接口
                            try {
                                Result<?> result = pushRuleService.insertTransferData(apiCode, JSON.toJSONString(transferDataDTO)
                                        , transferDataDTO);
                                if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                                    receive.setSyncStatus(1);
                                }
                            } catch (Exception e) {
                                log.error(e.getMessage() + jsonData, e);
                            }
                        }
                    }
                } catch (Exception e) {
                    respCustomer = customDataHandleImpl.bizErrorResponse(e);
                    log.error(e.getMessage() + jsonData, e);
                }
            }
            receive.setStatus(respCustomer.getStatusEnum().getValue());
            receive.setResponseCode(respCustomer.getResponseCode().toString());
            receive.setResponseData(JSON.toJSONString(respCustomer.getResponseCustomDTO()));
            receive.setRequestId(requestId == null ? (requestId = getRequestId(apiCode)) : requestId);
            // 6. 保存前置数据
            try {
                pushRuleService.mockDbOrRedisError(1, apiCode);
                int i = customerTransferDataReceiveMapper.insertSelective(receive);
                if (i != 1) {
                    throw new RuntimeException("定制化客户".concat(customDataHandleImpl.customer().getName())
                            .concat("(").concat(apiCode).concat(")保存失败,入库数据量:") + i);
                }
                // 7. 检查新增字段
                checkField(customDataHandleImpl, jsonData, apiCode, requestId);
            } catch (Exception e) {
                log.error(e.getMessage() + jsonData, e);
                // 6.1 数据库容灾
                respCustomer = sendMq(customDataHandleImpl, receive, respCustomer);
            }

            try {
                // 埋点
                JSONObject condition = new JSONObject();
                condition.put("request_id", receive.getRequestId());
                trackingService.trackBusinessLog(DataFlowDirection.IN
                        , apiCode
                        , "定制通用转化接口"
                        , "b_customer_transfer_data_receive"
                        , JSON.toJSONString(condition)
                        , Long.valueOf(receive.getBizDataNumber())
                        , TrackingContext.generateBatchId());
            } catch (Exception ex) {
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
            log.error(e.getMessage() + jsonData, e);
            return customDataHandleImpl.fallbackResponse(e).getResponseCustomDTO();
        }
    }

    private String getRequestId(String apiCode, String requestId) {

        Map<String, List<String>> customerHandlerEnumConfigMap = marketingCommonConfig.getCustomerHandlerEnumConfigMap();
        if (customerHandlerEnumConfigMap.containsKey(CustomerHandlerEnum.T_HENGCHANG.toString())) {
            List<String> apiCodes = customerHandlerEnumConfigMap.get(CustomerHandlerEnum.T_HENGCHANG.toString());
            if (apiCodes.contains(apiCode)) {
                return requestId;
            }
        }

        return (StringUtils.isBlank(requestId) ? getRequestId(apiCode) : apiCode.concat("_").concat(requestId));
    }

    private String getRequestId(String apiCode) {
        return apiCode.concat("_br_").concat(Md5Utils.cell32(RandomStringUtils.randomAlphabetic(32)
                .concat("&") + System.nanoTime()));
    }

    private CustomerResponseDTO sendMq(CustomerDataHandler customDataHandleImpl
            , CustomerTransferDataReceive receive
            , CustomerResponseDTO responseDTO) {
        try {
            ProductPulsarProducer producer = ProductPulsarClientManager.newProducer(PulsarTopic.transferCustomTopic);
            byte[] messageByte = JSON.toJSONString(receive).getBytes();
            producer.send(messageByte);
        } catch (PulsarClientException clientException) {
            log.error(clientException.getMessage(), clientException);
            return customDataHandleImpl.fallbackResponse(clientException);
        }
        return responseDTO;
    }

    private void checkField(CustomerDataHandler customDataHandleImpl
            , final String jsonData
            , final String apiCode
            , final String requestId) {
        BR_EXECUTORS.execute(() -> {
            try {
                Set<String> bizAllFields = customDataHandleImpl.getBizAllFields(jsonData);
                if (bizAllFields != null) {
                    ApiFieldCheckUtils.checkField(bizAllFields, redisChgService, apiCode
                            , customDataHandleImpl.customer().getName(), "receiveTransferData", requestId);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    @Override
    public Result<Boolean> consumerTransferPayData(String msg) {
        // 检查是否需要跳过业务逻辑
        if (pulsarConsumerSkipUtil.shouldSkipBusinessLogic(PulsarSubscription.transferCustomSubscription)) {
            log.warn("【pulsar】定制客户转化数据执行跳过逻辑");
            return new Result<>().setCode(ResultCode.SUCCESS.getValue());
        }
        
        Result<Boolean> result = new Result<>();
        try {
            CustomerTransferDataReceive receive = JSONObject.parseObject(
                    msg, new TypeReference<CustomerTransferDataReceive>() {
                    }.getType());
            pushRuleService.mockDbOrRedisError(1, receive.getApiCode());
            int i = customerTransferDataReceiveMapper.insertSelective(receive);
            result.setCode(i > 0 ? ResultCode.SUCCESS.getValue() : ResultCode.FAIL.getValue());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.setCode(ResultCode.FAIL.getValue());
        }
        return result;
    }
}
