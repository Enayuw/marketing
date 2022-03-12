package com.br.marketing.innerapi.controller.auth;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.client.RedisAuthService;
import com.br.marketing.common.commondto.ApiResult;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
    RedisAuthService redisAuthService;

    /**
     * 验证码
     *
     */
    @GetMapping("/captcha")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ApiResult<JSONObject> captcha(HttpSession session) {
        //定义图形验证码的长、宽、验证码字符数、干扰元素个数
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(206, 41, 4, 20);
        String code = captcha.getCode();
        String image = captcha.getImageBase64();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", session.getId());
        jsonObject.put("captcha", image);
        redisAuthService.set(session.getId(), code.toLowerCase());
        //过期时间3分钟
        redisAuthService.expire(session.getId(), 3 * 60);
        return new ApiResult<JSONObject>().success(jsonObject);
    }


}
