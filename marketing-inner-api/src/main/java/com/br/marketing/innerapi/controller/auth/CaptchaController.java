package com.br.marketing.innerapi.controller.auth;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.client.RedisAuthService;
import com.br.marketing.common.commondto.ApiResult;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
@Slf4j
public class CaptchaController {
    @Resource
    RedisAuthService redisAuthService;

    /**
     * 验证码
     */
    @GetMapping("/captcha")
    public ApiResult<JSONObject> captcha(HttpSession session) {
        //定义图形验证码的长、宽、验证码字符数、干扰元素个数
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(206, 41, 4, 0);
        String code = captcha.getCode();
        String image = captcha.getImageBase64();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", session.getId());
        jsonObject.put("captcha", image);
        redisAuthService.set(session.getId(), code.toLowerCase(), 3 * 60,"app_captcha_prefix");
        //过期时间3分钟
        return new ApiResult<JSONObject>().success(jsonObject);
    }

    /** 验证码
     */
    @GetMapping("/ipCaptcha")
    public ApiResult<JSONObject> ipCaptcha(HttpSession session, HttpServletRequest request) {
        String ipAddr = getIpAddr(request);
        log.warn("请求IP:{}", ipAddr);
        //定义图形验证码的长、宽、验证码字符数、干扰元素个数
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(206, 41, 4, 0);
        String code = captcha.getCode();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sessionId", session.getId());
        jsonObject.put("code", code);
        redisAuthService.set(session.getId(), code.toLowerCase(), 3 * 60,"app_captcha_prefix");
        //过期时间3分钟
        return new ApiResult<JSONObject>().success(jsonObject);
    }
    /**
     * 获取IP地址
     *
     * 使用Nginx等反向代理软件， 则不能通过request.getRemoteAddr()获取IP地址
     * 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP地址，X-Forwarded-For中第一个非unknown的有效IP字符串，则为真实IP地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
            log.warn("Proxy-Client-IP:{}",ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
            log.warn("X-Forwarded-For:{}",ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
            log.warn("WL-Proxy-Client-IP:{}",ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
            log.warn("X-Real-IP:{}",ip);
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
