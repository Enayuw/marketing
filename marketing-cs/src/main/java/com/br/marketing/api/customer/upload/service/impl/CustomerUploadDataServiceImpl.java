package com.br.marketing.api.customer.upload.service.impl;

import java.time.LocalDate;
import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.arch.geo.pulsar.ProductPulsarClientManager;
import com.br.arch.geo.pulsar.ProductPulsarProducer;
import com.br.common.encryption.Md5Utils;
import com.br.marketing.api.customer.upload.adapter.CustomerUploadDataAdapter;
import com.br.marketing.api.customer.upload.adapter.UploadDataAdaptee;
import com.br.marketing.api.customer.upload.handler.CustomerUploadDataHandleSingleton;
import com.br.marketing.api.customer.upload.handler.CustomerUploadDataHandler;
import com.br.marketing.api.customer.upload.handler.CustomerUploadHandlerEnum;
import com.br.marketing.api.customer.upload.service.CustomerUploadDataService;
import com.br.marketing.api.customer.upload.service.guomei.dto.GuMeUploadResponseDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.PulsarTopic;
import com.br.marketing.dto.CustomerResponseDTO;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.ResponseCustomDTO;
import com.br.marketing.entity.CustomizeUploadData;
import com.br.marketing.mapper.CustomizeUploadDataMapper;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.Impl.TableCreateServiceImpl;

import lombok.extern.slf4j.Slf4j;

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
    private CustomerUploadDataAdapter customerUploadDataAdapter;

    @Resource
    private CustomizeUploadDataMapper customizeUploadDataMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Override
    public ResponseCustomDTO receiveCustomizeUploadData(String apiCode, String jsonData) {
        CustomerUploadDataHandler customerUploadDataHandler =
            customerUploadDataHandleSingleton.getCustomerDataHandleImpl(apiCode, CustomerUploadHandlerEnum.U_ALIEN_DEFAULT);
        try {
            CustomizeUploadData uploadData = new CustomizeUploadData();
            uploadData.setRequestJsonData(jsonData);
            uploadData.setReceiveDate(LocalDate.now().toString());
            uploadData.setApiCode(apiCode);
            uploadData.setCreateTime(new Date());
            uploadData.setUpdateTime(new Date());
            UploadDataAdaptee adapter = null;
            CustomerResponseDTO respCustomer = null;
            String tCid = tableCreateService.getTcId(apiCode);
            if (StringUtils.isEmpty(tCid)) {
                log.error("创建客户定制上传前置表，未查询到该apiCode:{},对应客户信息，请关注！！！", apiCode);
                // 若没查询到cid 入pulsar 待恢复后消费
                respCustomer = sendMq(customerUploadDataHandler, uploadData);
                return respCustomer.getResponseCustomDTO();
            } else {
                // 创建定制上传表
                uploadData.setTCid(tCid);
                customizeUploadDataMapper.createCustomizeUploadDataTable(uploadData.getTCid());
            }
            try {
                customerUploadDataHandler.isValidJson(jsonData);
                // 1. 解析json
                adapter = customerUploadDataHandler.parseObject(jsonData);
            } catch (Exception e) {
                respCustomer = customerUploadDataHandler.jsonErrorResponse(e);
                log.error(e.getMessage() + jsonData, e);
            }
            String requestId = null;
            if (respCustomer == null) {
                try {
                    customerUploadDataHandler.setSourceParam(apiCode, jsonData, adapter);
                    // 2. 有数据验证,包括字段空值及验签
                    respCustomer = customerUploadDataHandler.verifyFields(adapter);
                    if (CustomerResponseDTO.StatusEnum.VALID.equals(respCustomer.getStatusEnum())) {
                        // 3. 计算业务数据量
                        int number = customerUploadDataHandler.countBizDataNumber(adapter);
                        uploadData.setBizDataNumber(number);
                        // 4. 适配
                        MarketingPreUserDTO marketingPreUserDTO = customerUploadDataAdapter.adapteeCustomerUploadData(adapter);
                        if (marketingPreUserDTO != null) {
                            requestId = getRequestId(apiCode, marketingPreUserDTO.getRequestId());
                        }
                    }
                } catch (Exception e) {
                    respCustomer = customerUploadDataHandler.bizErrorResponse(e);
                    log.error(e.getMessage() + jsonData, e);
                }
            }
            uploadData.setStatus(respCustomer.getStatusEnum().getValue());
            uploadData.setResponseCode(respCustomer.getResponseCode().toString());
            uploadData.setResponseData(JSON.toJSONString(respCustomer.getResponseCustomDTO()));
            uploadData.setRequestId(requestId == null ? getRequestId(apiCode) : requestId);
            // 6. 保存前置数据
            try {
                pushRuleService.mockDbOrRedisError(1, apiCode);
                uploadData.setTCid(tCid);
                int i = customizeUploadDataMapper.insertSelective(uploadData);
                if (i != 1) {
                    throw new RuntimeException(
                        "定制化客户".concat(customerUploadDataHandler.customer().getName()).concat("(").concat(apiCode).concat(")保存失败,入库数据量:") + i);
                }
            } catch (Exception e) {
                log.error(e.getMessage() + jsonData, e);
                // 6.1 数据库容灾
                respCustomer = sendMq(customerUploadDataHandler, uploadData);
            }
            // 8. 返回响应
            return respCustomer.getResponseCustomDTO();
        } catch (Exception e) {
            log.error(e.getMessage() + jsonData, e);
            return customerUploadDataHandler.fallbackResponse(e).getResponseCustomDTO();
        }
    }

    private CustomerResponseDTO sendMq(CustomerUploadDataHandler customerUploadDataHandler, CustomizeUploadData uploadData) {
        try {
            ProductPulsarProducer producer = ProductPulsarClientManager.newProducer(PulsarTopic.uploadCustomTopic);
            byte[] messageByte = JSON.toJSONString(uploadData).getBytes();
            producer.send(messageByte);
            GuMeUploadResponseDTO responseGuMeDTO = new GuMeUploadResponseDTO();
            responseGuMeDTO.success();
            return new CustomerResponseDTO(responseGuMeDTO, CustomerResponseDTO.StatusEnum.INVALID, responseGuMeDTO.getCode());
        } catch (PulsarClientException clientException) {
            log.error(clientException.getMessage(), clientException);
            return customerUploadDataHandler.fallbackResponse(clientException);
        }
    }

    private String getRequestId(String apiCode, String requestId) {
        return (StringUtils.isBlank(requestId) ? getRequestId(apiCode) : requestId);
    }

    private String getRequestId(String apiCode) {
        return apiCode.concat("_br_").concat(Md5Utils.cell32(RandomStringUtils.randomAlphabetic(32).concat("&") + System.nanoTime()));
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
        Result<Boolean> result = new Result<>();
        try {
            CustomizeUploadData receive = JSONObject.parseObject(msg, CustomizeUploadData.class);
            pushRuleService.mockDbOrRedisError(1, receive.getApiCode());
            int i = customizeUploadDataMapper.insertSelective(receive);
            result.setCode(i > 0 ? ResultCode.SUCCESS.getValue() : ResultCode.FAIL.getValue());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.setCode(ResultCode.FAIL.getValue());
        }
        return result;
    }
}
