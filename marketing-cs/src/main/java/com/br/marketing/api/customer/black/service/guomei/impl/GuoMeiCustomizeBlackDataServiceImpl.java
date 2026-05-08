package com.br.marketing.api.customer.black.service.guomei.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.api.customer.black.adapter.BaseBlackDataAdaptee;
import com.br.marketing.api.customer.black.handler.CustomerBlackHandlerEnum;
import com.br.marketing.api.customer.black.service.guomei.GuoMeiCustomizeBlackDataService;
import com.br.marketing.api.customer.black.service.guomei.dto.GuoMeiBlackJsonDTO;
import com.br.marketing.api.customer.black.service.guomei.dto.GuoMeiBlackResponseDTO;
import com.br.marketing.common.constants.rocketmq.MarketingTransferConstants;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.dto.CustomerResponseDTO;
import com.br.marketing.handle.SnowflakeRedisGeneratorHandle;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import javax.annotation.Resource;

import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 国美自定义上传策略实现
 *
 * @author senyang.zheng
 * @date 2024/08/07
 */
@Service
@Slf4j
public class GuoMeiCustomizeBlackDataServiceImpl implements GuoMeiCustomizeBlackDataService {

    @Resource
    private RabbitMqProducter rabbitMqProducter;
    @Resource
    private RocketMqSwitch rocketMqSwitch;
    @Resource
    private RocketMqTemplate template;
    @Resource
    private SnowflakeRedisGeneratorHandle snowflakeRedisGeneratorHandle;

    /**
     * 解密jsonData
     *
     * @param apiCode  apiCode
     * @param jsonData jsonData
     * @return {@link String }
     * @author senyang.zheng
     * @date 2024/09/11
     */
    @Override
    public String decryptJsonData(String apiCode, String jsonData) {
        return jsonData;
    }

    /**
     * 2023-10-18 16:45 客户
     *
     * @return 客户枚举
     */
    @Override
    public CustomerBlackHandlerEnum customer() {
        return CustomerBlackHandlerEnum.B_GUME;
    }

    /**
     * 2023-10-18 16:45 反序列化客户定制数据
     *
     * @param jsonData json 字符串
     * @return 转化适配者
     */
    @Override
    public BaseBlackDataAdaptee parseObject(String jsonData) {
        return JSONObject.parseObject(jsonData, GuoMeiBlackJsonDTO.class);
    }

    /**
     * 2023-10-23 17:37 校验字段
     *
     * @param adaptee 客户定制数据
     * @return 封装了响应结果与标记客户数据的状况
     */
    @Override
    public CustomerResponseDTO verifyFields(BaseBlackDataAdaptee adaptee) {
        GuoMeiBlackJsonDTO uploadJsonDTO = (GuoMeiBlackJsonDTO) adaptee;
        GuoMeiBlackResponseDTO guoMeiBlackResponseDTO = new GuoMeiBlackResponseDTO();
        StringBuilder errorMessage = new StringBuilder();
        if (StringUtils.isBlank(uploadJsonDTO.getRequestId())) {
            errorMessage.append(",requestId不可为空");
        } else if (StringUtils.isBlank(uploadJsonDTO.getInstitutionCode())) {
            errorMessage.append(",institutionCode不可为空");
        } else if (uploadJsonDTO.getEndFlag() == null) {
            errorMessage.append(",endFlag不可为空");
        } else if (!uploadJsonDTO.getEndFlag() && (uploadJsonDTO.getUserList() == null || uploadJsonDTO.getUserList().isEmpty())) {
            errorMessage.append(",endFlag为").append(uploadJsonDTO.getEndFlag()).append(",userList不可为空");
        }
        if (errorMessage.length() > 0) {
            guoMeiBlackResponseDTO.failed(GuoMeiBlackResponseDTO.ResultEnum.FAILED_FIELD_CHECK_ERROR, errorMessage.toString());
            return new CustomerResponseDTO(guoMeiBlackResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, guoMeiBlackResponseDTO.getCode());
        } else {
            guoMeiBlackResponseDTO.success();
        }
        return new CustomerResponseDTO(guoMeiBlackResponseDTO, CustomerResponseDTO.StatusEnum.VALID, guoMeiBlackResponseDTO.getCode());
    }

    /**
     * 获取requestId
     *
     * @param apiCode apiCode
     * @param adaptee 适配器
     * @return {@link String }
     * @author senyang.zheng
     * @date 2024/08/07
     */
    @Override
    public String getRequestId(String apiCode, BaseBlackDataAdaptee adaptee) {
        GuoMeiBlackJsonDTO uploadJsonDTO = (GuoMeiBlackJsonDTO)adaptee;
        return uploadJsonDTO.getRequestId();
    }

    /**
     * 2023-10-23 17:37 获取业务数据量
     *
     * @param adaptee 客户定制数据
     * @return 传输的业务数据量
     */
    @Override
    public int countBizDataNumber(BaseBlackDataAdaptee adaptee) {
        GuoMeiBlackJsonDTO uploadJsonDTO = (GuoMeiBlackJsonDTO)adaptee;
        return uploadJsonDTO.getUserList() != null ? uploadJsonDTO.getUserList().size() : 0;
    }


    /**
     * 2023-10-24 19:17 json解析错误,对应响应
     *
     * @param e 业务异常
     * @return 定制化客户响
     */
    @Override
    public CustomerResponseDTO jsonErrorResponse(Exception e) {
        GuoMeiBlackResponseDTO guoMeiBlackResponseDTO = new GuoMeiBlackResponseDTO();
        guoMeiBlackResponseDTO.failed(GuoMeiBlackResponseDTO.ResultEnum.FAILED_JSON_ERROR);
        return new CustomerResponseDTO(guoMeiBlackResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, guoMeiBlackResponseDTO.getCode());
    }

    /**
     * 2023-10-24 19:17 业务中发生异常时,对应响应
     *
     * @param e 业务异常
     * @return 定制化客户响
     */
    @Override
    public CustomerResponseDTO bizErrorResponse(Exception e) {
        return fallbackResponse(e);
    }

    /**
     * 2023-10-24 19:17 回退响应,未知异常时,提升客户体验
     *
     * @param e 未知异常
     * @return 定制化客户响
     */
    @Override
    public CustomerResponseDTO fallbackResponse(Exception e) {
        GuoMeiBlackResponseDTO guoMeiBlackResponseDTO = new GuoMeiBlackResponseDTO();
        guoMeiBlackResponseDTO.failed();
        return new CustomerResponseDTO(guoMeiBlackResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, guoMeiBlackResponseDTO.getCode());
    }

    /**
     * 入库异常默认成功响应
     *
     * @return {@link CustomerResponseDTO }
     * @author senyang.zheng
     * @date 2024/10/30
     */
    @Override
    public CustomerResponseDTO defaultSuccessResponse() {
        GuoMeiBlackResponseDTO guoMeiBlackResponseDTO = new GuoMeiBlackResponseDTO();
        guoMeiBlackResponseDTO.success();
        return new CustomerResponseDTO(guoMeiBlackResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, guoMeiBlackResponseDTO.getCode());
    }



    /**
     * 数据下发
     *
     * @param tCid     tCid
     * @param sourceId 数据源主键id
     * @author senyang.zheng
     * @date 2024/09/25
     */
    @Override
    public void dataDirection(String tCid, Long sourceId) {
        try {
            JSONObject json = new JSONObject();
            json.put("tCid", tCid);
            json.put("sourceId", sourceId);
            json.put("idempotentKey", snowflakeRedisGeneratorHandle.nextId());
            String msg = json.toJSONString();
            if(rocketMqSwitch.rocketMQSwitchFlag(null, MarketingTransferConstants.TAG_MARKETING_GUOMEI_BLACK_DATA_CLEAN)){
                rocketMqSwitch.syncSend(MarketingTransferConstants.TOPIC
                        , MarketingTransferConstants.TAG_MARKETING_GUOMEI_BLACK_DATA_CLEAN, msg);
            }else{
                rabbitMqProducter.send(MQConstants.ROUTING_KEY_MARKETING_GUOMEI_BLACK_DATA_CLEAN, msg);
            }
            log.warn("国美定制黑名单数据下发 tCid:{},sourceId:{}", tCid, sourceId);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.GUOMEI_SERVICEERROR.getCode(), e.getMessage()
                    , "推送国美定制黑名单数据下发消息异常！"), e);
        }
    }
}
