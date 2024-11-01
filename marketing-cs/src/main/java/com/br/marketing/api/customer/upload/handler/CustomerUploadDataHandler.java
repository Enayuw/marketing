package com.br.marketing.api.customer.upload.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.api.customer.upload.adapter.BaseUploadDataAdaptee;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.CustomerResponseDTO;

/**
 * 客户上传数据处理
 *
 * @author senyang.zheng
 * @date 2024/08/07
 */
public interface CustomerUploadDataHandler {

    /**
     * 解密jsonData
     *
     * @param apiCode apiCode
     * @param jsonData jsonData
     * @return {@link String }
     * @author senyang.zheng
     * @date 2024/09/11
     */
    String decryptJsonData(String apiCode, String jsonData);

    /**
     * 2023-10-18 16:45 客户
     *
     * @return 客户枚举
     */
    CustomerUploadHandlerEnum customer();

    /**
     * 2023-10-18 16:45 反序列化客户定制数据
     *
     * @param jsonData json 字符串
     * @return 转化适配者
     */
    BaseUploadDataAdaptee parseObject(String jsonData);

    /**
     * 2023-10-23 17:37 校验字段
     *
     * @param adaptee 客户定制数据
     * @return 封装了响应结果与标记客户数据的状况
     */
   <T>  CustomerResponseDTO verifyFields(BaseUploadDataAdaptee<T> adaptee);

    /**
     * 获取requestId
     *
     * @param adaptee 适配器
     * @return {@link String }
     * @author senyang.zheng
     * @date 2024/08/07
     */
    String getRequestId(String apiCode, BaseUploadDataAdaptee adaptee);

    /**
     * 2023-10-23 17:37 获取业务数据量
     *
     * @param adaptee 客户定制数据
     * @return 传输的业务数据量
     */
    int countBizDataNumber(BaseUploadDataAdaptee adaptee);

    /**
     * 2023-10-24 19:24 获取全部的业务字段,用于检查是否有新增的字段
     *
     * @param jsonData 客户json字符串
     * @return 业务中要提示的新增字段
     */
    Set<String> getBizAllFields(String jsonData);

    /**
     * 2023-10-24 19:17 json解析错误,对应响应
     *
     * @param e 业务异常
     * @return 定制化客户响
     */
    CustomerResponseDTO jsonErrorResponse(Exception e);

    /**
     * 2023-10-24 19:17 业务中发生异常时,对应响应
     *
     * @param e 业务异常
     * @return 定制化客户响
     */
    CustomerResponseDTO bizErrorResponse(Exception e);

    /**
     * 2023-10-24 19:17 回退响应,未知异常时,提升客户体验
     *
     * @param e 未知异常
     * @return 定制化客户响
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
     * @date 2024/09/25
     */
    default void dataDirection(String tCid, Long sourceId) {

    }

    /**
     * 2023-10-26 11:44 封装原始信息
     *
     * @param apiCode apiCode
     * @param jsonData 客户json字符串
     * @param adaptee 适配
     */
    default void setSourceParam(String apiCode, String jsonData, BaseUploadDataAdaptee adaptee) {
        if (adaptee == null) {
            return;
        }
        adaptee.setJsonData(jsonData);
        adaptee.setApiCode(apiCode);
    }

    /**
     * 2023-10-26 11:44 json结构判断
     *
     * @param jsonData 客户json字符串
     */
    default void isValidJson(String jsonData) {
        if (JSON.isValid(jsonData)) {
            return;
        }
        throw new JSONException("非json结构");
    }

    /**
     * 2023-10-24 19:17 新增字段检查
     *
     * @param fieldSet 需要检查的字段集合
     * @param localCacheFieldSet 本地缓存的字段集合
     * @param redisChgService redis bean
     * @param apiCode 客户编号
     * @param requestId 请求流水号
     * @return 组装的消息, 无时为null
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
     * 2023-10-23 17:37 获取业务数据量
     *
     * @param jsonData 客户json字符串
     * @return 传输的业务数据量
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
