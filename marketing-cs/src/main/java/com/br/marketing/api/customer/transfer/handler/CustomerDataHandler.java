package com.br.marketing.api.customer.transfer.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.br.arch.geo.pulsar.ProductPulsarClientManager;
import com.br.arch.geo.pulsar.ProductPulsarProducer;
import com.br.marketing.api.customer.transfer.adapter.TransferDataAdaptee;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.PulsarTopic;
import com.br.marketing.dto.CustomerResponseDTO;
import com.br.marketing.util.ApiFieldCheckUtils;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 客户数据处理
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-18 16:41
 */
public interface CustomerDataHandler {

    /**
     * 2023-10-18 16:45
     * 客户
     *
     * @return 客户枚举
     */
    CustomerHandlerEnum customer();

    /**
     * 2023-10-18 16:45
     * 反序列化客户定制数据
     *
     * @param jsonData json 字符串
     * @return 转化适配者
     */
    TransferDataAdaptee parseObject(String jsonData);

    /**
     * 2023-10-23 17:37
     * 校验字段
     *
     * @param adaptee 客户定制数据
     * @return 封装了响应结果与标记客户数据的状况
     */
    CustomerResponseDTO verifyFields(TransferDataAdaptee adaptee);

    /**
     * 2023-10-23 17:37
     * 获取业务数据量
     *
     * @param adaptee 客户定制数据
     * @return 传输的业务数据量
     */
    int countBizDataNumber(TransferDataAdaptee adaptee);

    /**
     * 2023-10-24 19:24
     * 获取全部的业务字段,用于检查是否有新增的字段
     *
     * @param jsonData 客户json字符串
     * @return 业务中要提示的新增字段
     */
    Set<String> getBizAllFields(String jsonData);

    /**
     * 2023-10-24 19:17
     * json解析错误,对应响应
     *
     * @param e 业务异常
     * @return 定制化客户响
     */
    CustomerResponseDTO jsonErrorResponse(Exception e);

    /**
     * 2023-10-24 19:17
     * 业务中发生异常时,对应响应
     *
     * @param e 业务异常
     * @return 定制化客户响
     */
    CustomerResponseDTO bizErrorResponse(Exception e);

    /**
     * 2023-10-24 19:17
     * 回退响应,未知异常时,提升客户体验
     *
     * @param e 未知异常
     * @return 定制化客户响
     */
    CustomerResponseDTO fallbackResponse(Exception e);

    /**
     * 2023-10-26 11:44
     * 封装原始信息
     *
     * @param apiCode  apiCode
     * @param jsonData 客户json字符串
     * @param adaptee  适配
     */
    default void setSourceParam(String apiCode, String jsonData, TransferDataAdaptee adaptee) {
        if (adaptee == null) {
            return;
        }
        adaptee.setJsonData(jsonData);
        adaptee.setApiCode(apiCode);
    }

    /**
     * 2023-10-26 11:44
     * json结构判断
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
     * 2023-10-23 17:37
     * 获取业务数据量
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

    @Deprecated
    default void sendQueue(Object message) throws PulsarClientException {
        ProductPulsarProducer producer = ProductPulsarClientManager.newProducer(PulsarTopic.transferCustomTopic);
        String jsonString = JSON.toJSONString(message);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("enum", customer());
        jsonObject.put("jsonData", jsonString);
        byte[] messageByte = JSON.toJSONString(jsonObject).getBytes();
        producer.send(messageByte);
    }
}
