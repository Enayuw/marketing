package com.br.marketing.client;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**调用流失预警api客户端
 * Created by Bairong on 2019/8/21.
 */
@Service
@Slf4j
public class LoanWarningClient {


    @Value("${otherConfig.warning.apiUrl:00}")
    private String apiUrl;
    @Resource
    RestTemplate restTemplate;

    /**
     * 远程调用流失预警api接口，
     *
     * @param param param
     */
    public String queryApi(JSONObject param,String apiCode) {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("apiCode",apiCode);
        form.add("jsonData", param.toJSONString());
        //log.info("流失预警入参---{}",form.toString());
        String re ="";
        try {
            re = restTemplate.postForObject(apiUrl, form, String.class);
        }catch (Exception e){
            log.warn("queryApi error",e);
            try{
                re = restTemplate.postForObject(apiUrl, form, String.class);
            }catch (Exception se){
                log.error("queryApi error",se);
                return re;
            }
        }
        log.info("流失预警返回 --- {}", re);
        return re;
    }

    public String signFileAlarm(String apiCode){
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("apiCode",apiCode);
        String re ="";
        try {
            re =restTemplate.getForObject("http://localhost:19707/alarm/signFileAlarm?apiCode="+apiCode,String.class);
        }catch (Exception e){
            log.warn("signFileAlarm error",e);
            try{
                re =restTemplate.getForObject("http://localhost:19707/alarm/signFileAlarm?apiCode="+apiCode,String.class);
            }catch (Exception se){
                log.error("signFileAlarm error",se);
                return re;
            }
        }
        return re;
    }

    public String ftpToSftpCheck(String apiCode){
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("apiCode",apiCode);
        String re ="";
        try {
             re = restTemplate.getForObject("http://localhost:19707/alarm/ftpToSftpCheck?apiCode=" + apiCode, String.class);
        }catch (Exception e){
            log.warn("ftpToSftpCheck error",e);
            try{
                re=restTemplate.getForObject("http://localhost:19707/alarm/ftpToSftpCheck?apiCode="+apiCode,String.class);
            }catch (Exception se){
                log.error("ftpToSftpCheck error",se);
                return re;
            }
        }
        return re;
    }
}
