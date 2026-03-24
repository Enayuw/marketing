package com.br.marketing.api.customer.upload.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.arch.geo.pulsar.ProductPulsarClientManager;
import com.br.arch.geo.pulsar.ProductPulsarProducer;
import com.br.common.log.AlertLog;
import com.br.marketing.api.customer.upload.adapter.BaseUploadDataAdaptee;
import com.br.marketing.api.customer.upload.handler.CustomerUploadDataHandleSingleton;
import com.br.marketing.api.customer.upload.handler.CustomerUploadDataHandler;
import com.br.marketing.api.customer.upload.handler.CustomerUploadHandlerEnum;
import com.br.marketing.api.customer.upload.service.guomei.dto.GuMeUploadResponseDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.PulsarSubscription;
import com.br.marketing.common.constants.PulsarTopic;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.CustomerResponseDTO;
import com.br.marketing.dto.ResponseCustomDTO;
import com.br.marketing.entity.CustomizeUploadData;
import com.br.marketing.mapper.CustomizeUploadDataMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.utils.PulsarConsumerSkipUtil;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

/**
 * 定制客户上传数据处理
 *
 * @author senyang.zheng
 * @date 2024/08/05
 */
@Service
@Slf4j
public class CustomerUploadDataServiceImpl implements CustomerUploadDataService {

    @Resource
    private CustomerUploadDataHandleSingleton customerUploadDataHandleSingleton;

    @Resource
    private PushRuleService pushRuleService;

    @Resource
    private PulsarConsumerSkipUtil pulsarConsumerSkipUtil;

    @Resource
    private CustomizeUploadDataMapper customizeUploadDataMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private TrackingService trackingService;

    @Override
    public ResponseCustomDTO receiveCustomizeUploadData(String apiCode, String jsonData) {
        CustomerUploadDataHandler customerUploadDataHandler =
                customerUploadDataHandleSingleton.getCustomerDataHandleImpl(apiCode, CustomerUploadHandlerEnum.U_ALIEN_DEFAULT);
        try {
            BaseUploadDataAdaptee adapter = null;
            CustomerResponseDTO respCustomer = null;
            CustomizeUploadData uploadData = new CustomizeUploadData();
            String decryptData = jsonData;
            if (customerUploadDataHandler.customer().getIsNeedDecrypt()) {
                decryptData = customerUploadDataHandler.decryptJsonData(apiCode, jsonData);
                uploadData.setExtend(jsonData);
            }
            uploadData.setRequestJsonData(decryptData);
            uploadData.setReceiveDate(LocalDate.now().toString());
            uploadData.setApiCode(apiCode);
            uploadData.setCreateTime(new Date());
            uploadData.setUpdateTime(new Date());
            String tCid = tableCreateService.getTcId(apiCode);
            if (StringUtils.isEmpty(tCid)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                        "创建客户定制上传前置表，未查询到该apiCode:" + apiCode + "对应客户信息，请关注！！！"));
                // 若没查询到cid 入pulsar 待恢复后消费
                respCustomer = sendMq(customerUploadDataHandler, apiCode, jsonData);
                return respCustomer.getResponseCustomDTO();
            } else {
                // 创建定制上传表
                uploadData.setTCid(tCid);
                customizeUploadDataMapper.createCustomizeUploadDataTable(uploadData.getTCid());
            }
            try {
                customerUploadDataHandler.isValidJson(decryptData);
                // 1. 解析json
                adapter = customerUploadDataHandler.parseObject(decryptData);
            } catch (Exception e) {
                respCustomer = customerUploadDataHandler.jsonErrorResponse(e);
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                        "该apiCode:" + apiCode + "定制上传接口传参jsonData非json格式！！！"));
            }
            if (respCustomer == null) {
                String requestId = customerUploadDataHandler.getRequestId(apiCode, adapter);
                uploadData.setRequestId(requestId);
                try {
                    customerUploadDataHandler.setSourceParam(apiCode, decryptData, adapter);
                    // 2. 有数据验证,包括字段空值及验签
                    respCustomer = customerUploadDataHandler.verifyFields(adapter);
                    if (CustomerResponseDTO.StatusEnum.VALID == respCustomer.getStatusEnum()) {
                        // 3. 计算业务数据量
                        int number = customerUploadDataHandler.countBizDataNumber(adapter);
                        uploadData.setBizDataNumber(number);
                    }
                } catch (Exception e) {
                    respCustomer = customerUploadDataHandler.bizErrorResponse(e);
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "该apiCode:" + apiCode + "定制上传数据适配标准上传异常"),
                            e);

                }
            }
            uploadData.setStatus(respCustomer.getStatusEnum().getValue());
            uploadData.setResponseCode(respCustomer.getResponseCode().toString());
            uploadData.setResponseData(JSON.toJSONString(respCustomer.getResponseCustomDTO()));
            // 6. 保存前置数据
            try {
                pushRuleService.mockDbOrRedisError(1, apiCode);
                int i = customizeUploadDataMapper.insertSelective(uploadData);
                if (i != 1) {
                    throw new RuntimeException(
                            "定制化客户".concat(customerUploadDataHandler.customer().getName()).concat("(").concat(apiCode).concat(")保存失败,入库数据量:") + i);
                }
                // 7.数据有效，且存储前置完成后进行数据下发（按需实现，默认不处理），
                if (Objects.equals(uploadData.getStatus(), CustomerResponseDTO.StatusEnum.VALID.getValue())) {
                    customerUploadDataHandler.dataDirection(tCid, uploadData.getId());
                }
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "该apiCode:" + apiCode + "定制上传数据写入客户定制上传前置表异常"),
                        e);
                // 6.1 数据库容灾
                respCustomer = sendMq(customerUploadDataHandler, apiCode, jsonData);
            }

            try {
                // 埋点
                JSONObject condition = new JSONObject();
                condition.put("request_id", uploadData.getRequestId());
                trackingService.trackBusinessLog(DataFlowDirection.IN
                        , apiCode
                        , "通用定制上传接口"
                        , String.format("b_customize_upload_data_%s", tCid)
                        , JSON.toJSONString(condition)
                        , Long.valueOf(uploadData.getBizDataNumber())
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
                    "该apiCode:" + apiCode + "定制上传数据接入异常，jsonData:" + jsonData), e);
            return customerUploadDataHandler.fallbackResponse(e).getResponseCustomDTO();
        }
    }

    /**
     * 发送容灾消息，若存在加密情况jsonData需是原文
     *
     * @param customerUploadDataHandler 适配器
     * @param apiCode                   API代码
     * @param jsonData                  json数据
     * @return {@link CustomerResponseDTO }
     * @author senyang.zheng
     * @date 2024/09/11
     */
    private CustomerResponseDTO sendMq(CustomerUploadDataHandler customerUploadDataHandler, String apiCode, String jsonData) {
        try {
            ProductPulsarProducer producer = ProductPulsarClientManager.newProducer(PulsarTopic.uploadCustomTopic);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("apiCode", apiCode);
            jsonObject.put("jsonData", jsonData);
            byte[] messageByte = JSON.toJSONString(jsonObject).getBytes();
            producer.send(messageByte);
            log.warn(String.format("定制写入Pulsar 主题:%s 数据:%s", PulsarTopic.upLoadTopic, jsonObject.toJSONString()));
            return customerUploadDataHandler.defaultSuccessResponse();
        } catch (PulsarClientException clientException) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "该apiCode:" + apiCode + "入pulsar容灾队列异常"),
                    clientException);
            return customerUploadDataHandler.fallbackResponse(clientException);
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
    public Result<Boolean> consumerUploadPayData(String msg) {
        // 检查是否需要跳过业务逻辑
        if (pulsarConsumerSkipUtil.shouldSkipBusinessLogic(PulsarSubscription.uploadCustomSubscription)) {
            log.warn("【pulsar】定制客户上传数据执行跳过逻辑");
            return new Result<>().setCode(ResultCode.SUCCESS.getValue());
        }

        Result<Boolean> result = new Result<>();
        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            String apiCode = jsonObject.getString("apiCode");
            String jsonData = jsonObject.getString("jsonData");
            receiveCustomizeUploadData(apiCode, jsonData);
            result.setCode(ResultCode.SUCCESS.getValue());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "pulsar容灾队列消费异常，pulsar消息：" + msg), e);
            result.setCode(ResultCode.FAIL.getValue());
        }
        return result;
    }
}
