package com.br.marketing.client.carclue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.carclue.dto.HxInterfaceConfigDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class CarClueClient {

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Value(value = "${api.hxCar.cityList:'http://haoyunlailai.cn:6001/open_api/v1/city_list'}")
    private String cityList;

    @Value(value = "${api.hxCar.carList:'http://haoyunlailai.cn:6001/open_api/v1/car_list'}")
    private String carList;

    @Value("${api.biocloo.isProxy:true}")
    private Boolean isProxy;

    @Resource
    private HttpProxyClient httpProxyClient;

    public Result<JSONArray> getZjCity() {
        JSONObject jo = marketingCommonConfig.getHxClientConfig();
        String zjChannelId = jo.getString("zjChannelId");
        String zjChannelKey = jo.getString("zjChannelKey");
        return getCity(zjChannelId, zjChannelKey, "xsc");
    }

    /**
     * @return 格式
     * {
     * "code": 1,
     * "data": [
     * {
     * "cityName": "福州",
     * "provinceName": "福建省",
     * "cityId": 301,
     * "provinceId": 350000
     * }]
     * }
     */
    public Result<JSONArray> getYcCity() {
        JSONObject jo = marketingCommonConfig.getHxClientConfig();
        String ycChannelId = jo.getString("ycChannelId");
        String ycChannelKey = jo.getString("ycChannelKey");
        return getCity(ycChannelId, ycChannelKey, "6+");
    }

    private Result<JSONArray> getCity(String channelId, String channelKey, String task) {

        try {
            Map<String, String> data = new HashMap<>();
            data.put("channel_id", channelId);
            data.put("task", task);

            String sign = generateSign(data, channelKey);
            data.put("sign", sign);
            String param = param(data);
            String reqUrl = String.format("%s?%s", cityList, param);
            HashMap<String, String> resMap = httpProxyClient.get(reqUrl, isProxy, null);
            // 请求异常
            if (!"200".equals(resMap.get("httpcode"))
                    || StringUtils.isBlank(resMap.get("content"))) {
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
            }

            String content = resMap.get("content");
            JSONObject resJo = JSON.parseObject(content);
            if (Integer.valueOf("20000").equals(resJo.getInteger("code"))) {
                return new Result<>()
                        .setCode(ResultCode.SUCCESS.getValue())
                        .setDate(resJo.getJSONArray("data"));
            }
            return new Result<>()
                    .setCode(ResultCode.FAIL.getValue());
        } catch (Exception ex) {
            return new Result<>()
                    .setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

    public Result<JSONArray> getZjCar() {
        JSONObject jo = marketingCommonConfig.getHxClientConfig();
        String zjChannelId = jo.getString("zjChannelId");
        String zjChannelKey = jo.getString("zjChannelKey");
        String zjTask = jo.getString("zjTask");
        return getCar(zjChannelId, zjChannelKey, zjTask);
    }

    public Result<JSONArray> getYcCar() {
        JSONObject jo = marketingCommonConfig.getHxClientConfig();
        String ycChannelId = jo.getString("ycChannelId");
        String ycChannelKey = jo.getString("ycChannelKey");
        String ycTask = jo.getString("ycTask");
        return getCar(ycChannelId, ycChannelKey, ycTask);
    }

    private Result<JSONArray> getCar(String channelId, String channelKey, String task) {

        try {
            Map<String, String> data = new HashMap<>();
            data.put("channel_id", channelId);
            data.put("task", task);
            data.put("page", "1");
            data.put("limit", "10000");

            String sign = generateSign(data, channelKey);
            data.put("sign", sign);
            String param = param(data);
            String reqUrl = String.format("%s?%s", carList, param);
            HashMap<String, String> resMap = httpProxyClient.get(reqUrl, isProxy, null);
            // 请求异常
            if (!"200".equals(resMap.get("httpcode"))
                    || StringUtils.isBlank(resMap.get("content"))) {
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
            }

            String content = resMap.get("content");
            JSONObject resJo = JSON.parseObject(content);
            if (Integer.valueOf("20000").equals(resJo.getInteger("code"))) {
                return new Result<>()
                        .setCode(ResultCode.SUCCESS.getValue())
                        .setDate(resJo.getJSONArray("data"));
            }
            return new Result<>()
                    .setCode(ResultCode.FAIL.getValue());
        } catch (Exception ex) {
            return new Result<>()
                    .setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

    private static String param(Map<String, String> data) {
        StringBuilder sb = new StringBuilder();
        for (String key : data.keySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(key)
                    .append("=")
                    .append("task".equals(key)
                            ? URLEncoder.encode(data.get(key))
                            : data.get(key));
        }
        return sb.toString();
    }


    public static String generateSign(Map<String, String> data, String channelKey) {
        // 1. 过滤掉空值和 sign 字段
        Map<String, String> filteredData = new HashMap<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty() && !"sign".equalsIgnoreCase(entry.getKey())) {
                filteredData.put(entry.getKey().toUpperCase(), entry.getValue()); // 转大写
            }
        }

        // 2. 按 ASCII 从小到大排序（字典序）
        List<String> keys = new ArrayList<>(filteredData.keySet());
        Collections.sort(keys);

        // 3. 使用 URL 键值对格式拼接成字符串 A
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(key).append("=").append(filteredData.get(key));
        }

        // 4. 拼接 channelKey
        if (sb.length() > 0) {
            sb.append("&");
        }
        sb.append("key=").append(channelKey);
        String signTemp = sb.toString();

        // 5. 对 signTemp 进行 MD5 运算并转换为大写
        return DigestUtils.md5DigestAsHex(signTemp.getBytes(StandardCharsets.UTF_8)).toUpperCase();
    }

}
