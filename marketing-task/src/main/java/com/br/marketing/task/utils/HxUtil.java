package com.br.marketing.task.utils;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.Customer;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.task.Scheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;

/**
 * Created by Bairong on 2020/4/16.
 */
@Slf4j
public class HxUtil {
    private static RestTemplate restTemplate = Scheduler.ac.getBean(RestTemplate.class);

    public static String getReport(Customer customer, JSONObject jsonData, JSONObject jsonMeal, boolean firstTime, String url) {
        log.info("jsonData:{},jsonMeal:{},url:{}", jsonData, jsonMeal, url);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Pinpoint-Sampled", "s0");
        JSONObject json = new JSONObject();
        JSONObject extendConfigInfoJson = new JSONObject();
        String extendConfigInfo = customer.getExtendConfigInfo();
        if (StringUtils.isNotBlank(extendConfigInfo)) {
            try {
                extendConfigInfoJson = JSONObject.parseObject(extendConfigInfo);
            } catch (Exception e) {
                log.error("客户扩展字段格式化异常,apiCode={},data={}", customer.getApiCode(), extendConfigInfo);
            }
        }
        String replaceApiCode = extendConfigInfoJson.getString("replaceApiCode");
        String userType = jsonData.getString("userType");
        if (isReplace(extendConfigInfoJson, customer, userType)) {
            StringBuilder meal = new StringBuilder();
            jsonMeal.keySet().forEach(product -> meal.append(product));
            json.put("meal", meal);
        } else {
            json.put("jsonMeal", jsonMeal);
            json.put("originApiCode", customer.getApiCode());
        }
        json.put("id", jsonData.getString("idCard"));
        json.put("name", jsonData.getString("name"));
        json.put("cell", jsonData.getString("cell"));

        if (StringUtils.isNotEmpty(jsonData.getString("passDate"))) {
            json.put("pass_date", jsonData.getString("passDate"));
        }
        if (StringUtils.isNotEmpty(jsonData.getString("user_date"))) {
            json.put("user_date", jsonData.getString("user_date"));
        }
        if (StringUtils.isNotEmpty(jsonData.getString("decodeFailType"))) {
            json.put("decodeFailType", jsonData.getString("decodeFailType"));
        }

        JSONObject extDataJson = new JSONObject();
        if (StringUtils.isNotEmpty(jsonData.getString("isRepair"))) {
            extDataJson.put("isRepair", jsonData.getString("isRepair"));
        }
        //渠道标识 计费需要
        extDataJson.put("channelType", jsonData.getString("userType"));

        /**
         * 0不留存，1留存
         */
        if (firstTime || customer.getSaveLog() == 1) {
            extDataJson.put("isSaveLog", "1");
        } else {
            extDataJson.put("isSaveLog", "0");
        }
        json.put("ExtData", extDataJson);
        json.put("ifDeactivated", "0");
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("jsonData", json.toString());
        //公共apicode
        paramMap.add("apiCode", Constants.PUBLIC_APICODE);
        if (isReplace(extendConfigInfoJson, customer, userType)) {
            paramMap.add("customerId", replaceApiCode);
        }

        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<MultiValueMap>(paramMap, requestHeaders);
        // log.info("画像请求参数 --- {}", paramMap.toString());
        String result = "";
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
        } catch (Exception e) {
            log.warn(" 画像错误 ---{}---重试", paramMap.toString(), e);
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
                log.warn(" 画像重试返回结果 ---{}", result);
            } catch (Exception e1) {
                log.error(" 画像重试错误 -{}--{}", jsonData.getString("batch_number"), jsonData.getString("cusNum"), e);
            }
        }
        log.info("画像结果 --- {}", result);
        return result;
    }

    public static String getReport(MarketingCustomer customer, JSONObject jsonData, JSONObject jsonMeal, boolean firstTime, String url,
                                   List<String> noflagproductlist, List<String> flagProductList) {
        log.info("jsonData:{},jsonMeal:{},url:{}", jsonData, jsonMeal, url);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Pinpoint-Sampled", "s0");
        JSONObject json = new JSONObject();
        JSONObject extendConfigInfoJson = new JSONObject();
        String extendConfigInfo = customer.getExtendConfigInfo();
        if (StringUtils.isNotBlank(extendConfigInfo)) {
            try {
                extendConfigInfoJson = JSONObject.parseObject(extendConfigInfo);
            } catch (Exception e) {
                log.error("客户扩展字段格式化异常,apiCode={},data={}", customer.getApiCode(), extendConfigInfo);
            }
        }
        String replaceApiCode = extendConfigInfoJson.getString("replaceApiCode");
        String userType = jsonData.getString("userType");
        if (isReplace(extendConfigInfoJson, customer, userType)) {
            StringBuilder meal = new StringBuilder();
            jsonMeal.keySet().forEach(product -> meal.append(product));
            json.put("meal", meal);
        } else {
            json.put("jsonMeal", jsonMeal);
            json.put("originApiCode", customer.getApiCode());
        }
        json.put("id", jsonData.getString("idCard"));
        json.put("name", jsonData.getString("name"));
        json.put("cell", jsonData.getString("cell"));

//        if(StringUtils.isNotEmpty(jsonData.getString("passDate"))){
//            json.put("pass_date", jsonData.getString("passDate"));
//        }
//        if(StringUtils.isNotEmpty(jsonData.getString("user_date"))){
//            json.put("user_date", jsonData.getString("user_date"));
//        }
//        if(StringUtils.isNotEmpty(jsonData.getString("decodeFailType"))){
//            json.put("decodeFailType", jsonData.getString("decodeFailType"));
//        }

        JSONObject extDataJson = new JSONObject();
        if (StringUtils.isNotEmpty(jsonData.getString("isRepair"))) {
            extDataJson.put("isRepair", jsonData.getString("isRepair"));
        }
        JSONObject extData = jsonData.getJSONObject("extData");
        if (extData != null) {
            extData.keySet().forEach(t -> extDataJson.put(t, extData.get(t)));
        }
        //渠道标识 计费需要
        extDataJson.put("channelType", jsonData.getString("userType"));

        /**
         * 0不留存，1留存
         */
        if (customer.getSaveLog() == 1) {
            extDataJson.put("isSaveLog", "1");
        } else {
            extDataJson.put("isSaveLog", "0");
        }
        json.put("ExtData", extDataJson);
        json.put("ifDeactivated", "0");
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("jsonData", json.toString());
        //公共apicode
        paramMap.add("apiCode", Constants.PUBLIC_APICODE);
        if (isReplace(extendConfigInfoJson, customer, userType)) {
            paramMap.add("customerId", replaceApiCode);
        }

        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<MultiValueMap>(paramMap, requestHeaders);
        // log.info("画像请求参数 --- {}", paramMap.toString());
        String result = "";
        try {
            result = restTemplate.postForObject(url, requestEntity, String.class);
            //调用画像结果重试
            handlerResult(result,url,requestEntity,noflagproductlist,jsonMeal,flagProductList);
        } catch (Exception e) {
            log.warn(" 画像错误 ---api_code={}---重试", customer.getApiCode(), e);
            try {
                result = restTemplate.postForObject(url, requestEntity, String.class);
                log.warn(" 画像重试返回结果 ---{}", result);
            } catch (Exception e1) {
                log.error(" 画像重试错误 -{}--{}", jsonData.getString("batch_number"), jsonData.getString("cusNum"), e);
            }
        }
        log.info("画像结果 --- {}", result);
        return result;
    }

    /**
     * 调用画像结果重试处理
     *
     * @param result 画像的结果
     */
    private static void handlerResult(String result, String url, HttpEntity<MultiValueMap> requestEntity, List<String> noflagproductlist,
                                      JSONObject jsonMeal, List<String> flagProductList) {
        //重试三次
        for (int i = 0; i < 3; i++) {
            if (isRetry(result, jsonMeal, noflagproductlist, flagProductList)) {
                result = restTemplate.postForObject(url, requestEntity, String.class);
                log.warn("调用画像重试结果result={}", result);
            } else {
                return;
            }
        }
    }

    public static Boolean isRetry(String hxResult, JSONObject jsonMeal, List<String> noflagproductlist, List<String> flagProductList) {
        //返回空，需要重试
        if (StringUtils.isEmpty(hxResult)) {
            log.error("hxResult isEmpty");
            return true;
        }
        ;
        JSONObject resultJson = JSONObject.parseObject(hxResult);
        //非00且非100002 重试
        if (!"00".equals(resultJson.getString("code"))
                && !"100002".equals(resultJson.getString("code"))) {
            return true;
        }
        Set<String> strings = jsonMeal.keySet();
        for (String key : strings) {
            if (noflagproductlist.contains(key.toLowerCase())) {
                continue;
            }
            String flag = "";
            String s = Constants.flagMap.get(key.toLowerCase());
            String string = "";
            if (StringUtils.isNotBlank(s)) {
                flag = "flag_" + s;
                string = resultJson.getString(flag);
            } else {
                if (flagProductList.contains(key)) {
                    flag = "flag_score";
                    string = resultJson.getString(flag);
                } else {
                    flag = "flag_" + key.toLowerCase();
                    string = resultJson.getString(flag);
                }
            }
            if ("100002".equals(resultJson.getString("code")) && StringUtils.isBlank(string)) {
                return true;
            }
            if (!"0".equals(string) && !"1".equals(string)) {
                /**
                 * ScoreData未命中时不返回flag
                 * 需要特殊处理
                 */
                if (StringUtils.isEmpty(string)) {
                    if ("ScoreData".equals(key)) {
                        continue;
                    } else {
                        return true;
                    }
                }

                if ("99".equals(string)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String hauXiangFlat(String json) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Pinpoint-Sampled", "s0");
        MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("tokenid", "aa");
        paramMap.add("jsonData", json);
        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<MultiValueMap>(paramMap, requestHeaders);
        String paramJson = "";
        try {
            paramJson = restTemplate.postForObject("http://k8s.brapp.com/huaxiang-api2/huaxiang/flat", requestEntity, String.class);
        } catch (Exception e) {
            log.error("hauXiangFlat出错了", e);
        }
        return paramJson;
    }

    private static Boolean isReplace(JSONObject extendConfigInfoJson, Customer customer, String userType) {
        String replaceApiCode = extendConfigInfoJson.getString("replaceApiCode");
        if (StringUtils.isNotBlank(replaceApiCode) &&
                !customer.getApiCode().equals(replaceApiCode) &&
                ("S01".equalsIgnoreCase(userType) || "S03".equalsIgnoreCase(userType) || "S05".equalsIgnoreCase(userType))) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private static Boolean isReplace(JSONObject extendConfigInfoJson, MarketingCustomer customer, String userType) {
        String replaceApiCode = extendConfigInfoJson.getString("replaceApiCode");
        if (StringUtils.isNotBlank(replaceApiCode) &&
                !customer.getApiCode().equals(replaceApiCode) &&
                ("S01".equalsIgnoreCase(userType) || "S03".equalsIgnoreCase(userType) || "S05".equalsIgnoreCase(userType))) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
