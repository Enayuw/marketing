package com.br.marketing.client.zhijia;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.zhijia.input.ReqAddZhiJiaClueDTO;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * @ClassName ZhiJiaClient
 * @Description 推送之家创建接口
 * @Author kongbx
 * @Date 2024/7/10 16:49
 */
@Component
@Slf4j
public class ZhiJiaClient {

    @Value("${api.zhongAn.isProxy:false}")
    Boolean isProxy;

    @Value("${api.zhijia.addC1HiqClueUrl:00}")
    private String addC1HiqClueUrl;

    @Value("${api.zhijia.zhiJiaClientId:00}")
    private String clientId;

    @Value("${api.zhijia.zhiJiaClientSecret:00}")
    private String clientSecret;

    @Value("${api.zhijia.brandUrl:00}")
    private String brandUrl;

    @Value("${api.zhijia.seriesUrl:00}")
    private String seriesUrl;

    @Value("${api.zhijia.appId:0}")
    private String appId;

    @Value("${api.zhijia.querykey:0}")
    private String querykey;

    @Autowired
    HttpProxyClient httpProxyClient;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    private final static String TITLE = "【推送之家创建接口】";

    @RetryMethod(retryNowNum = 3)
    public Result addZhiJiaClue(ReqAddZhiJiaClueDTO dto) {

        HashMap<String, String> resMap = new HashMap<>();
        // 获取挡板开关
        HashMap<String, Object> mock = marketingCommonConfig.getZhiJiaUndoMock();
        if (mock.get("switch") == Boolean.FALSE) {
            long start = System.currentTimeMillis();
            log.warn(TITLE + "调度开始, 入参:{}", JSONObject.toJSONString(dto));

            resMap = httpProxyClient.sendByCodeWithLog(dto, addC1HiqClueUrl, isProxy,
                    MediaType.APPLICATION_JSON_UTF8_VALUE,
                    JSON.toJSONString(dto), true, true);

            log.warn(TITLE + "调度结束, 返回值:{}, 耗时:{}", resMap, System.currentTimeMillis() - start);
        } else {
            JSONObject mockJson = new JSONObject();
            mockJson.put("returncode", mock.get("code"));
            mockJson.put("message", "处理成功");
            resMap.put("content", JSON.toJSONString(mockJson));
            resMap.put("httpcode", mock.get("httpcode").toString());
        }

        // 请求异常
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.error(TITLE + "接口异常-请求参数:{};返回:{}", JSON.toJSONString(dto), JSON.toJSONString(resMap));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
        }

        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        JSONObject data = resultJson.getJSONObject("result");
        String returncode = resultJson.getString("returncode");

        if ("0".equals(returncode)) {
            log.warn(TITLE + "接口，返回returncode为0，请求正常");
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(data.getString("cclid"));
        } else {
            log.error(TITLE + "接口异常，返回returncode非0，最多重试三次");
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
        }
    }

    @RetryMethod(retryNowNum = 3)
    public Result getToken() {

        HashMap<String, String> resMap = new HashMap<>();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("response_type", "token");
        jsonObject.put("client_id", clientId);
        jsonObject.put("dataList", clientSecret);
        resMap = httpProxyClient.sendByCodeWithLog(jsonObject, addC1HiqClueUrl, isProxy,
                MediaType.APPLICATION_JSON_UTF8_VALUE,
                JSON.toJSONString(jsonObject), true, true);

        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.error("之家获取token接口异常-请求参数:{};返回:{}", JSON.toJSONString(jsonObject), JSON.toJSONString(resMap));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
        }
        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        JSONObject data = resultJson.getJSONObject("data");
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(data.getString("access_token"));

    }


    @RetryMethod(retryNowNum = 3)
    public Result getCityAndCounty(String token) {

        HashMap<String, String> resMap = new HashMap<>();
        String url = "".concat("?access_token=").concat(token);
        resMap = httpProxyClient.get(url, isProxy);
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.error("之家获取省市县接口异常-请求url:{};返回:{}", url, JSON.toJSONString(resMap));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
        }
        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        String returncode = resultJson.getString("returncode");
        if ("0".equals(returncode)) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(resultJson);
        } else {
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
        }
    }

    @RetryMethod(retryNowNum = 3)
    public Result getBrand(String token) {

        HashMap<String, String> resMap = new HashMap<>();
        String url = brandUrl.concat("?access_token=").concat(token).concat("&appid=").concat(appId) .concat("&querykey=").concat(querykey);
        resMap = httpProxyClient.get(url, isProxy);
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_ZHIJIA_ERROR.getCode()
                    ,"之家获取车辆品牌接口异常-请求url:" + url + ";返回:" + JSON.toJSONString(resMap)));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
        }
        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        String returncode = resultJson.getString("returncode");
        if ("0".equals(returncode)) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(resultJson);
        } else {
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
        }
    }

    @RetryMethod(retryNowNum = 3)
    public Result getSeries(String token, String brandId) {
        HashMap<String, String> resMap = new HashMap<>();
        String url = seriesUrl.concat("?access_token=").concat(token)
                .concat("&appid=").concat(appId)
                .concat("&querykey=").concat(querykey)
                .concat("&brandId=").concat(brandId);
        resMap = httpProxyClient.get(url, isProxy);
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_ZHIJIA_ERROR.getCode()
                    ,"之家获取车辆车系接口异常-请求url:" + url + ";返回:" + JSON.toJSONString(resMap)));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
        }
        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        String returncode = resultJson.getString("returncode");
        if ("0".equals(returncode)) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(resultJson);
        } else {
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
        }
    }

}
