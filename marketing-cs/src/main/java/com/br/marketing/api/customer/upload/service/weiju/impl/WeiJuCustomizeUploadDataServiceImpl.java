package com.br.marketing.api.customer.upload.service.weiju.impl;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Resource;

import com.br.marketing.common.constants.rocketmq.MarketingUploadConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.api.customer.upload.adapter.BaseUploadDataAdaptee;
import com.br.marketing.api.customer.upload.handler.CustomerUploadHandlerEnum;
import com.br.marketing.api.customer.upload.service.weiju.WeiJuCustomizeUploadDataService;
import com.br.marketing.api.customer.upload.service.weiju.dto.WeiJuUploadJsonDTO;
import com.br.marketing.api.customer.upload.service.weiju.dto.WeiJuUploadResponseDTO;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.dto.CustomerResponseDTO;
import com.br.marketing.rabbitmq.RabbitMqProducter;

import lombok.extern.slf4j.Slf4j;

/**
 * 微聚自定义上传策略实现
 *
 * @author senyang.zheng
 * @date 2024/09/11
 */
@Service
@Slf4j
public class WeiJuCustomizeUploadDataServiceImpl implements WeiJuCustomizeUploadDataService {

    @Resource
    private RabbitMqProducter rabbitMqProducter;
    @Resource
    private RocketMqSwitch rocketMqSwitch;
    @Resource
    private RocketMqTemplate template;

    /**
     * 解密JsonData
     *
     * @param apiCode apiCode
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
    public CustomerUploadHandlerEnum customer() {
        return CustomerUploadHandlerEnum.U_WEIJU;
    }

    /**
     * 2023-10-18 16:45 反序列化客户定制数据
     *
     * @param jsonData json 字符串
     * @return 转化适配者
     */
    @Override
    public BaseUploadDataAdaptee parseObject(String jsonData) {
        return JSONObject.parseObject(jsonData, WeiJuUploadJsonDTO.class);
    }

    /**
     * 2023-10-23 17:37 校验字段
     *
     * @param adaptee 客户定制数据
     * @return 封装了响应结果与标记客户数据的状况
     */
    @Override
    public CustomerResponseDTO verifyFields(BaseUploadDataAdaptee adaptee) {
        WeiJuUploadJsonDTO uploadJsonDTO = (WeiJuUploadJsonDTO)adaptee;
        WeiJuUploadResponseDTO weiJuUploadResponseDTO = new WeiJuUploadResponseDTO();
        StringBuilder errorMessage = new StringBuilder();
        if (StringUtils.isBlank(uploadJsonDTO.getExecuteBatchNo())) {
            errorMessage.append(",executeBatchNo不可为空");
        } else if (uploadJsonDTO.getUserInfoList() == null || uploadJsonDTO.getUserInfoList().isEmpty()) {
            errorMessage.append(",userList不可为空");
        }
        if (errorMessage.length() > 0) {
            weiJuUploadResponseDTO.failed(WeiJuUploadResponseDTO.ResultEnum.FAILED_FIELD_CHECK_ERROR, errorMessage.toString());
            return new CustomerResponseDTO(weiJuUploadResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, weiJuUploadResponseDTO.getCode());
        } else {
            weiJuUploadResponseDTO.success();
        }
        return new CustomerResponseDTO(weiJuUploadResponseDTO, CustomerResponseDTO.StatusEnum.VALID, weiJuUploadResponseDTO.getCode());
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
    public String getRequestId(String apiCode, BaseUploadDataAdaptee adaptee) {
        WeiJuUploadJsonDTO uploadJsonDTO = (WeiJuUploadJsonDTO)adaptee;
        return uploadJsonDTO.getExecuteBatchNo();
    }

    /**
     * 2023-10-23 17:37 获取业务数据量
     *
     * @param adaptee 客户定制数据
     * @return 传输的业务数据量
     */
    @Override
    public int countBizDataNumber(BaseUploadDataAdaptee adaptee) {
        WeiJuUploadJsonDTO uploadJsonDTO = (WeiJuUploadJsonDTO)adaptee;
        return uploadJsonDTO.getUserInfoList() != null ? uploadJsonDTO.getUserInfoList().size() : 0;
    }

    /**
     * 2023-10-24 19:24 获取全部的业务字段,用于检查是否有新增的字段
     *
     * @param jsonData 客户json字符串
     * @return 业务中要提示的新增字段
     */
    @Override
    public Set<String> getBizAllFields(String jsonData) {
        // TODO 强总处理@zeqiang.guo
        return Collections.emptySet();
    }

    /**
     * 2023-10-24 19:17 json解析错误,对应响应
     *
     * @param e 业务异常
     * @return 定制化客户响
     */
    @Override
    public CustomerResponseDTO jsonErrorResponse(Exception e) {
        WeiJuUploadResponseDTO weiJuUploadResponseDTO = new WeiJuUploadResponseDTO();
        weiJuUploadResponseDTO.failed(WeiJuUploadResponseDTO.ResultEnum.FAILED_JSON_ERROR);
        return new CustomerResponseDTO(weiJuUploadResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, weiJuUploadResponseDTO.getCode());
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
        WeiJuUploadResponseDTO weiJuUploadResponseDTO = new WeiJuUploadResponseDTO();
        weiJuUploadResponseDTO.failed();
        return new CustomerResponseDTO(weiJuUploadResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, weiJuUploadResponseDTO.getCode());
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
        WeiJuUploadResponseDTO weiJuUploadResponseDTO = new WeiJuUploadResponseDTO();
        weiJuUploadResponseDTO.success();
        return new CustomerResponseDTO(weiJuUploadResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, weiJuUploadResponseDTO.getCode());
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
            String msg = json.toJSONString();
            if(rocketMqSwitch.rocketMQSwitchFlag(null, MarketingUploadConstants.TAG_MARKETING_WEIJU_DATA_CLEAN)){
                rocketMqSwitch.syncSend(MarketingUploadConstants.TOPIC
                        , MarketingUploadConstants.TAG_MARKETING_WEIJU_DATA_CLEAN, msg);
            }else{
                rabbitMqProducter.send(MQConstants.ROUTING_KEY_MARKETING_WEIJU_DATA_CLEAN, msg);
            }
            log.warn("微聚定制数据下发 tCid:{},sourceId:{}", tCid, sourceId);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.WEIJU_SERVICEERROR.getCode(), e.getMessage()
                    , "推送微聚定制数据下发消息异常！"), e);
        }
    }
}
