package com.br.marketing.client.yiqianbao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.yiqianbao.input.RequestYqbDTO;
import com.br.marketing.client.yiqianbao.input.YqbDetailVo;
import com.br.marketing.client.yiqianbao.output.ResponseYqbDTO;
import com.br.marketing.client.yiqianbao.utils.RSAUtil;
import com.br.marketing.client.yiqianbao.utils.SignUtil;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class YiQianBaoService {

    @Value("${api.yiQianBao.pushMarketingData:00}")
    private String url;

    @Value("${api.yiQianBao.isProxy:0}")
    private String isProxy;

    @Value("${api.yiQianBao.rsaPubKey:0}")
    private String yqbPubKey;

    @Value("${api.yiQianBao.salt:0}")
    private String salt;

    @Value("${api.yiQianBao.brPrivateKey:0}")
    private String brPrivateKey;

    @Autowired
    HttpProxyClient httpProxyClient;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    public static final List<String> RESULT_CODE = Lists.newArrayList("000000", "465001", "465002", "465003", "465004", "465005", "465999");

    @RetryMethod(retryNowNum = 3)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<ResponseYqbDTO> pushMarketingData(YqbDetailVo yqbDetailVo) {
        try {
            log.warn("壹钱包明文参数 para={}", JSON.toJSONString(yqbDetailVo));
            RequestYqbDTO requestYqbDTO = new RequestYqbDTO();
            //公钥可配置
            if(StringUtils.isNotEmpty(marketingCommonConfig.getYiQianBaoPubKey())){
                yqbPubKey = marketingCommonConfig.getYiQianBaoPubKey();
            }
            requestYqbDTO.setBizContent(RSAUtil.encrypt(JSON.toJSONString(yqbDetailVo), yqbPubKey));
            requestYqbDTO.setReqSeqNo(UUID.randomUUID().toString());
            requestYqbDTO.setSign(getRequestSign(requestYqbDTO, salt));
            HashMap<String, String> response = httpProxyClient.sendByCode(requestYqbDTO
                    , url
                    , isProxy.equals("1") ? true : false
                    , MediaType.APPLICATION_JSON_UTF8_VALUE
                    , null);
            String code = response.get("httpcode");
            if ("200".equals(code)) {
                JSONObject jsonResult = JSONObject.parseObject(response.get("content"));
                if (!RESULT_CODE.contains(jsonResult.getString("respCode"))) {
                    return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
                }
                if (!"000000".equals(jsonResult.getString("respCode"))) {
                    log.error(String.format("调用壹钱包返回状态码异常：%s", response.get("content")));
                    return new Result().setCode(ResultCode.FAIL.getValue());
                }
                ResponseYqbDTO content = JSON.parseObject(RSAUtil.decrypt(jsonResult.getString("bizContent"), brPrivateKey), new TypeReference<ResponseYqbDTO>() {
                }.getType());
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(content);
            } else {
                return new Result<>().setCode(ResultCode.FAIL.getValue());
            }
        } catch (Exception ex) {
            log.error(String.format("调用壹钱包接口异常：%s", ex.getMessage()), ex);
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

    private String getRequestSign(RequestYqbDTO req, String salt) {
        req.setSign(null);
        String plainContent = JSON.toJSONString(req, SerializerFeature.SortField);
        return SignUtil.getSign(plainContent, salt);
    }

}
