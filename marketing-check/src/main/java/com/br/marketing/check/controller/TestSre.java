package com.br.marketing.check.controller;

import com.alibaba.fastjson.JSON;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.entity.RequestLog;
import com.br.marketing.rpcclient.rpcclientImpl.BrokerGrpcClient;
import com.br.marketing.rpcclient.rpcclientImpl.DecodeGrpcClient;
import com.br.marketing.rpcclient.rpcclientImpl.UserCenterGrpcClient;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 后面遇到架构升级不方便测试或自测的时候可以在这里提供调用的入口
 * @author yu.xia@brgroup.com
 * @Date 2024/1/23 11:15
 */
@RestController
@RequestMapping("/sre/")
@Slf4j
public class TestSre {

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @GetMapping("/testSre")
    public String testApiToDb(@RequestParam("all") String all,@RequestParam("key") String key){
        boolean allFlag = false;
        if(null != all && "WhoAreYou".equals(all)){
            allFlag = true;
        }
        if(allFlag || "log".equals(key)){
            log.warn("warn-testSre-key:[{}]", key);
            log.error("error-testSre-key:[{}]", key);
        }
        if(allFlag || "speed".equals(key)){
            log.warn("testSre-speed-key:[{}]-value:[{}]", key, JSON.toJSONString(marketingCommonConfig));
        }
        if(allFlag || "grpc".equals(key)){
            // 解密grpc
            String query = DecodeGrpcClient.query("913fb4b537fb433d437edabdfe23b256", "cell"
                    , "md5", "7410086_20240123135700_1234");
            log.warn("解密grpc-:[{}]", query);
            // 商户中心
            String companyMsg = UserCenterGrpcClient.getCompanyMsg("7410086");
            log.warn("商户中心grpc-:[{}]", companyMsg);
            // 用户中心
            MerchantParam merchantParam = UserCenterGrpcClient.getMerchantParam("7410785");
            log.warn("用户中心grpc-:[{}]", JSON.toJSONString(merchantParam));
            // 给MOM发送上传数据
            BrokerGrpcClient.sendUploadLog("{\"one\":\"value1\",\"two\":\"value2\"}");
            // 给MOM发送请求数据
            RequestLog requestLog = new RequestLog();
            requestLog.setRequestTime(new Date());
            requestLog.setApiCode("111111");
            requestLog.setRequestStr("{\"proxy_source\":\"1\",\"name\":\"C11TBlBcVFUΒ4FDlBSUwlXAA5cU1AOUgUECV0CUVcEBwc\"" +
                    ",\"strategy_id\":\"DTA_BR0002113\",\"id\":\"CwxRVgJRBlhdBQEIV1xUAΒ7QUNDARWVFUKW1QHAgBeAAI\"," +
                    "\"cell\":\"Ww9SDgZdAwMCDAJTVgQFBQAOWgQCAwgCVF8DΒ4UlACUQQ\",\"custom_request\":\"Md5\"}");
            requestLog.setResponseStr("{\"code\":\"00\",\"swift_number\":\"3030994_20240117143038_37761B55A19\"," +
                    "\"DataStrategy\":{\"strategy_version\":\"1.1\",\"product_type\":\"100081\"," +
                    "\"strategy_id\":\"DTA_BR0002113\",\"product_name\":\"预置_借贷意向验证\",\"scene\":\"lend\"}," +
                    "\"Flag\":{\"applyloanstr\":\"1\",\"datastrategy\":\"1\"}}");
            requestLog.setResponseTime(new Date());
            requestLog.setCostTime(10);
            requestLog.setSwiftNumber("111111_20240117_2222");
            requestLog.setUrl("test");
            requestLog.setCode("00");
            BrokerGrpcClient.sendRequestLog(requestLog);
        }
        return "success";
    }

}
