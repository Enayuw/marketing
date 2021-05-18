package com.br.marketing.api.client;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.entities.client.HxResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;

/** 画像远程调用客户端
 * @author Wang Weiwei
 * @since 2018/3/17
 */

@Component
@Slf4j
public class HxClient {
    @Value("${otherConfig.huaXiangInterface.getReport}")
    private String reportUrl;
    @Value("${loaning.apicode}")
    private String apiCode;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * Report hx result.
     *
     * @param param the param
     * @return the hx result
     */
    public HxResult report(JSONObject param)  {
        param.put("ifDeactivated", "1");
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("jsonData", param.toJSONString());
        paramMap.add("apiCode",apiCode);

        String hxResponse ="";
        try {
             hxResponse = restTemplate.postForObject(reportUrl, paramMap, String.class);
        }catch (Exception e){
            log.warn(" 画像错误",e);
        }
        log.info(" 画像返回流水 ---{}", JSONObject.parseObject(hxResponse).getString("swift_number"));
        return new HxResult(hxResponse);
    }




}
