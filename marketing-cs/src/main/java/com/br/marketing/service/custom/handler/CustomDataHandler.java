package com.br.marketing.service.custom.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.arch.geo.pulsar.ProductPulsarClientManager;
import com.br.arch.geo.pulsar.ProductPulsarProducer;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.PulsarTopic;
import com.br.marketing.dto.ResponseCustomDTO;
import org.apache.pulsar.client.api.PulsarClientException;

/**
 * 客户数据处理
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-18 16:41
 */
public interface CustomDataHandler {

    /**
     * 2023-10-18 16:45
     * 客户
     *
     * @return 客户枚举
     */
    CustomCodeEnum custom();


    /**
     * 2023-10-18 17:41
     * 客户接收处理
     *
     * @param apiCode  apiCode
     * @param jsonData 业务数据
     * @return 响应抽象类
     */
    ResponseCustomDTO receiveCustomDataHandler(String apiCode, String jsonData);

    /**
     * 2023-10-17 17:56
     * 异常消息重新入库
     * 补偿数据
     *
     * @param msg mq中的消息
     * @return 结果
     */
    Result<Boolean> consumerPayData(String msg);

    /**
     * 2023-10-18 20:16
     * 发送消息到消息队列
     *
     * @param apiCode apiCode
     * @param message 消息
     */
    default void sendQueue(String apiCode, Object message) throws PulsarClientException {
        ProductPulsarProducer producer = ProductPulsarClientManager.newProducer(PulsarTopic.transferCustomTopic);
        String jsonString = JSON.toJSONString(message);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("apiCode", apiCode);
        jsonObject.put("jsonData", jsonString);
        byte[] messageByte = JSON.toJSONString(jsonObject).getBytes();
        producer.send(messageByte);
    }
}
