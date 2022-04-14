package com.br.marketing.client.yiqianbao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.yiqianbao.input.RequestYqbDTO;
import com.br.marketing.client.yiqianbao.input.YqbDetailVo;
import com.br.marketing.client.yiqianbao.output.ResponseYqbDTO;
import com.br.marketing.client.yiqianbao.utils.RSAUtil;
import com.br.marketing.client.yiqianbao.utils.SignUtil;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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


    public Result<ResponseYqbDTO> pushMarketingData(YqbDetailVo yqbDetailVo) {
        try {
            RequestYqbDTO requestYqbDTO = new RequestYqbDTO();
            requestYqbDTO.setBizContent(RSAUtil.encrypt(JSON.toJSONString(yqbDetailVo.getUserInfoList()), yqbPubKey));
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
                ResponseYqbDTO content = JSON.parseObject(RSAUtil.decrypt(jsonResult.getString("bizContent"), brPrivateKey), new TypeReference<ResponseYqbDTO>() {
                }.getType());
                if (!"000000".equals(content.getRespCode())) {
                    log.error(String.format("调用壹钱包返回状态码异常：%s", response.get("content")));
                }
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(content);
            } else {
                return new Result<>().setCode(ResultCode.FAIL.getValue());
            }
        } catch (Exception ex) {
            log.error(String.format("调用壹钱包接口异常：%s", ex.getMessage()), ex);
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
    }

    private String getRequestSign(RequestYqbDTO req, String salt) {
        req.setSign(null);
        String plainContent = JSON.toJSONString(req, SerializerFeature.SortField);
        return SignUtil.getSign(plainContent, salt);

    }


}
