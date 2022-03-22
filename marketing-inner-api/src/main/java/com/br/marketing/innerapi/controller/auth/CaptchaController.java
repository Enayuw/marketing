package com.br.marketing.innerapi.controller.auth;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.LineCaptcha;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.client.RedisAuthService;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 图片验证码
 * @Date 2022/3/10 10:38 AM
 * ------------------------------
 */
@RestController
@Api(value = "验证码", tags = "captcha")
public class CaptchaController {
    @Resource
    RedisChgService redisChgService;

    @Resource
    RestTemplate restTemplate;
    private static String url = "http://k8s.brapp.com/compass-api/api/strategy-distribution/strategy-customizer/distributeList?" +
            "apiCode={apiCode}&strategyCategory={strategyCategory}&distributeType={distributeType}&strategyType={strategyType}";
    private static String url1="http://k8s.brapp.com/compass-api/api/strategy-distribution/inside/sendEmailTest";

    @GetMapping("/authTest")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ApiResult<JSONObject> authTest(HttpSession session) {
        String id = session.getId();
        Map<String, Object> urlVariables = new HashMap<>();
        urlVariables.put("apiCode", "7492629");
        urlVariables.put("strategyCategory", "7");
        urlVariables.put("distributeType", "3");
        urlVariables.put("strategyType", "8");

        //restTemplate.getForObject("",);
        String result = restTemplate.getForObject(url, String.class, urlVariables);

        return new ApiResult<JSONObject>().success(id);
    }

    /**
     * 验证码
     */
    @GetMapping("/captcha")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ApiResult<JSONObject> captcha(HttpSession session) {
        //定义图形验证码的长、宽、验证码字符数、干扰元素个数
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(206, 41, 4, 20);
        String code = captcha.getCode();
        String image = captcha.getImageBase64();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", session.getId());
        jsonObject.put("captcha", image);
        redisChgService.setnx(session.getId(), code.toLowerCase(), 3 * 60);
        //过期时间3分钟
        return new ApiResult<JSONObject>().success(jsonObject);
    }


}
