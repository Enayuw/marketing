package com.br.marketing.api.customer.black.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.api.customer.black.adapter.BaseBlackDataAdaptee;
import com.br.marketing.api.customer.upload.handler.CustomerUploadHandlerEnum;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.CustomerResponseDTO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;


/**
 * 客户黑数据处理程序
 *
 * @author senyang.zheng
 * @date 2024/10/30
 */
public interface CustomerBlackDataHandler {

    /**
     * 解密jsonData
     *
     * @param apiCode  apiCode
     * @param jsonData jsonData
     * @return {@link String }
     * @author senyang.zheng
     * @date 2024/10/30
     */
    String decryptJsonData(String apiCode, String jsonData);


    /**
     * 定制策略
     *
     * @return {@link CustomerUploadHandlerEnum }
     * @author senyang.zheng
     * @date 2024/10/30
     */
    CustomerBlackHandlerEnum customer();

    /**
     * 2023-10-18 16:45 反序列化客户定制数据
     *
     * @param jsonData json 字符串
     * @return 转化适配者@author senyang.zheng
     * @date 2024/10/30
     */
    BaseBlackDataAdaptee parseObject(String jsonData);


    /**
     * 校验字段
     *
     * @param adaptee 适应者
     * @return {@link CustomerResponseDTO }
     * @author senyang.zheng
     * @date 2024/10/30
     */
    <T>  CustomerResponseDTO verifyFields(BaseBlackDataAdaptee<T> adaptee);

    /**
     * 获取requestId
     *
     * @param adaptee 适配器
     * @return {@link String }
     * @author senyang.zheng
     * @date 2024/10/30
     */
    String getRequestId(String apiCode, BaseBlackDataAdaptee adaptee);


    /**
     * 获取业务数据量
     *
     * @param adaptee 适配器
     * @return int
     * @author senyang.zheng
     * @date 2024/10/30
     */
    int countBizDataNumber(BaseBlackDataAdaptee adaptee);


    /**
     * json解析错误,对应响应
     *
     * @param e e
     * @return {@link CustomerResponseDTO }
     * @author senyang.zheng
     * @date 2024/10/30
     */
    CustomerResponseDTO jsonErrorResponse(Exception e);


    /**
     * 业务中发生异常时,对应响应
     *
     * @param e e
     * @return {@link CustomerResponseDTO }
     * @author senyang.zheng
     * @date 2024/10/30
     */
    CustomerResponseDTO bizErrorResponse(Exception e);


    /**
     * 回退响应,未知异常时,提升客户体验
     *
     * @param e e
     * @return {@link CustomerResponseDTO }
     * @author senyang.zheng
     * @date 2024/10/30
     */
    CustomerResponseDTO fallbackResponse(Exception e);

    /**
     * 入库异常默认成功响应
     *
     * @return {@link CustomerResponseDTO }
     * @author senyang.zheng
     * @date 2024/10/30
     */
    CustomerResponseDTO defaultSuccessResponse();

    /**
     * 数据下发
     *
     * @param tCid tCid
     * @param sourceId 数据源主键id
     * @author senyang.zheng
     * @date 2024/10/30
     */
    default void dataDirection(String tCid, Long sourceId) {

    }


    /**
     * 封装原始信息
     *
     * @param apiCode  API代码
     * @param jsonData json数据
     * @param adaptee  适应者
     * @author senyang.zheng
     * @date 2024/10/30
     */
    default void setSourceParam(String apiCode, String jsonData, BaseBlackDataAdaptee adaptee) {
        if (adaptee == null) {
            return;
        }
        adaptee.setJsonData(jsonData);
        adaptee.setApiCode(apiCode);
    }


    /**
     * json结构判断
     *
     * @param jsonData json数据
     * @author senyang.zheng
     * @date 2024/10/30
     */
    default void isValidJson(String jsonData) {
        if (JSON.isValid(jsonData)) {
            return;
        }
        throw new JSONException("非json结构");
    }


    /**
     * 新增字段检查
     *
     * @param fieldSet           字段集
     * @param localCacheFieldSet 本地缓存字段集
     * @param redisChgService    Redis 更改服务
     * @param apiCode            API代码
     * @param requestId          请求ID
     * @return {@link String }
     * @author senyang.zheng
     * @date 2024/10/30
     */
    default String checkField(Set<String> fieldSet, final Set<String> localCacheFieldSet, final RedisChgService redisChgService, String apiCode,
        String requestId) {
        StringBuilder fieldStr = new StringBuilder();
        String redisKey = RedisKeyConstant.CUSTOMER_TRANSFER_FIELD_KEY.concat(":").concat(apiCode);
        String separator = "、";
        for (String field : fieldSet) {
            if (localCacheFieldSet.add(field)) {
                Long aLong = redisChgService.saddMember(redisKey, field);
                if (aLong == 1) {
                    fieldStr.append(fieldStr.length() > 0 ? separator : "\n").append(field);
                }
            }
        }
        if (fieldStr.length() > 0) {
            String msg = AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_NEW_FIELD_CHECK.getCode(),
                customer().getName() + "(" + apiCode + ")在请求(" + requestId + ")中有新增字段：".concat(fieldStr.toString()).concat("\n请及时与客户沟通确认^_^"),
                customer().getName() + "(" + apiCode + ")定制化" + AlarmSendCodeEnum.EXCEPTION_NEW_FIELD_CHECK.getMessage());
            Long rSum = redisChgService.scard(redisKey);
            if (rSum == null || rSum < localCacheFieldSet.size()) {
                redisChgService.sadd(redisKey, new ArrayList<>(localCacheFieldSet));
            }
            return msg;
        }
        return null;
    }


    /**
     * 获取业务数据量
     *
     * @param jsonData json数据
     * @return int
     * @author senyang.zheng
     * @date 2024/10/30
     */
    default int countBizDataNumber(String jsonData) {
        if (JSON.isValid(jsonData)) {
            if (JSON.isValidObject(jsonData)) {
                JSONObject jsonObject = JSONObject.parseObject(jsonData);
                Collection<Object> values = jsonObject.values();
                for (Object value : values) {
                    int number = countBizDataNumber(JSON.toJSONString(value));
                    if (number > 0) {
                        return number;
                    }
                }
            } else {
                if (JSON.isValidArray(jsonData)) {
                    JSONArray jsonArray = JSONArray.parseArray(jsonData);
                    return jsonArray.size();
                }
            }
        }
        return 1;
    }

}
