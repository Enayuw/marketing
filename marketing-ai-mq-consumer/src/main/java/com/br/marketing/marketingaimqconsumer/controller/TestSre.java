package com.br.marketing.marketingaimqconsumer.controller;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.AiMQConstants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.rpcclient.rpcclientImpl.DecodeGrpcClient;
import com.br.marketing.rpcclient.rpcclientImpl.UserCenterGrpcClient;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 不包含MOM发送的测试
 * 后面遇到架构升级不方便测试或自测的时候可以在这里提供调用的入口
 * @author yu.xia@brgroup.com
 * @Date 2024/1/23 11:15
 */
@RestController
@RequestMapping("/sre/")
@Slf4j
public class TestSre {
    @Resource
    RedisChgService redisChgService;
    @Resource
    private RabbitMqProducter rabbitMqProducter;
    @Resource
    private MarketingCustomerMapper marketingCustomerMapper;
    @Resource
    MarketingCommonConfig marketingCommonConfig;

    /**
     * 验证tidb、rabbitmq、redis、grpc、speed
     * @param all
     * @param key
     * @return
     */
    @GetMapping("/testSre")
    public String testApiToDb(@RequestParam("all") String all, @RequestParam("key") String key, @RequestParam("value") String value) {
        log.warn("all:[{}],key:{},value:{}", all, key, value);
        boolean allFlag = false;
        if (null != all && "WhoAreYou".equals(all)) {
            allFlag = true;
        }
        if (allFlag || "log".equals(key)) {
            log.warn("warn-testSre-key:[{}]", key);
            log.error("error-testSre-key:[{}]", key);
        }

        if (allFlag || "speed".equals(key)) {
            log.warn("testSre-speed-key:[{}]-value:[{}]", key, JSON.toJSONString(marketingCommonConfig));
        }
        if (allFlag || "grpc".equals(key)) {
            // 解密grpc
            String query = DecodeGrpcClient.query("913fb4b537fb433d437edabdfe23b256", "cell"
                    , "md5", "7410086_20240123135700_1234");
            log.warn("解密grpc-:[{}]", query);
            // 商户中心
            String companyMsg = UserCenterGrpcClient.getCompanyMsg("7410086");
            log.warn("商户中心grpc-:[{}]", companyMsg);
        }

        if ("redis".equals(key)) {
            log.warn("redis-key:[{}]", value);
            log.warn("redis-value:{}", redisChgService.get(value));
        }
        if ("rabbitmq".equals(key)) {
            rabbitMqProducter.send(AiMQConstants.ROUTING_KEY_MARKETING_AI_PRE_USER_RECEIVE, String.valueOf(value));
            log.warn("rabbitmq-成功");
        }
        if ("tidb".equals(key)) {
            List<MarketingCustomer> customers = marketingCustomerMapper.getNameByApiCodeList(value);
            log.warn("tidb-:[{}]", JSON.toJSONString(customers));
        }
        return "success";
    }

}