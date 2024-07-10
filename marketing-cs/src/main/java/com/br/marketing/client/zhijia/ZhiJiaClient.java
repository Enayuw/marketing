package com.br.marketing.client.zhijia;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.zhijia.input.ReqAddZhiJiaClueDTO;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
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
 * @Description TODO
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

    @Autowired
    HttpProxyClient httpProxyClient;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    private final static String TITLE = "【推送之家创建接口】";

    @RetryMethod(retryNowNum = 3)
    public Result addZhiJiaClue(ReqAddZhiJiaClueDTO dto){

        HashMap<String, String> resMap = new HashMap<>();
        // 获取挡板开关
        HashMap<String, Object> mock = marketingCommonConfig.getZhiJiaUndoMock();
        if (mock.get("switch") == Boolean.TRUE) {
            JSONObject mockJson = new JSONObject();
            mockJson.put("returncode", mock.get("code"));
            mockJson.put("message", "处理成功");
            resMap.put("content", JSON.toJSONString(mockJson));
            resMap.put("httpcode", mock.get("httpcode").toString());
        } else {
            long start = System.currentTimeMillis();
            log.warn(TITLE+"调度开始, requestParam{}", JSONObject.toJSONString(dto));
            resMap = httpProxyClient.sendByCodeWithLog(dto, addC1HiqClueUrl, isProxy,
                    MediaType.APPLICATION_JSON_UTF8_VALUE,
                    JSON.toJSONString(dto), true, true);
            long end = System.currentTimeMillis();
            log.warn(TITLE+"调度结束, result:{}, 耗时:{}", resMap, end - start);
        }

        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.error(TITLE+"接口异常-请求参数:{};返回:{}", JSON.toJSONString(dto), JSON.toJSONString(resMap));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
        }

        String content = resMap.get("content");
        JSONObject resultJson = JSONObject.parseObject(content);
        String returncode = resultJson.getString("returncode");

        if ("0".equals(returncode)) {
            log.warn(TITLE+"接口，返回returncode为0，请求正常");
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage(content);
        }else {
            log.error(TITLE+"接口异常，返回returncode非0，最多重试三次");
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

}
