package com.br.marketing.client.zhongan;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.zhongan.input.ZaMarketDataDTO;
import com.br.marketing.client.zhongan.input.ZaMarketDetail;
import com.br.marketing.client.zhongan.input.ZhongAnRequestDTO;
import com.br.marketing.client.zhongan.utils.Md5Utils;
import com.br.marketing.client.zhongan.utils.RSAEncrypt;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service
public class ZhongAnClient {

    @Value("${api.zhongAn.api:00}")
    String url;

    @Autowired
    HttpProxyClient httpProxyClient;

    String signKey = "59018a92ca1e0d1e38f7da0617491abe";

    public static String XdChannelCode = "07brdyy01";

    public static String BxChannelCode = "3360001";

    String xinDaiDetailApiKey = "channel.marketDetail.07brdyy01";

    String xinDaiZKApiKey = "zadpreloan.nexusmetric.07brdyy01";

    String xXZKApiKey = "zadpreloan.nexusmetric.br.3360001 ";

    String RSApKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCVuaxc7ZznPdvsH0nhd2eQ/uhu/LewJqUVMvdYUKwXPxzGBUz8cVKyltwpMJ03uMPx+RStWnkWcmCSeQdqiw27FtaPELOxxQQc06OGBXfp5R86MKp2+bkdPRSkpUKK2X8vCyiopQojBXbaVzRUwjPPsQAsDinCGkSUirWETxfCxwIDAQAB";

    @RetryMethod(isOrNoDbRetry = true)
    public Result pushDetail(ZaMarketDataDTO dto, Integer retry){
        ZhongAnRequestDTO zhongAnRequestDTO = new ZhongAnRequestDTO();
        zhongAnRequestDTO.setApiKey(xinDaiDetailApiKey);
        zhongAnRequestDTO.setReqNo(dto.getReqNo());
        zhongAnRequestDTO.setReqDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        zhongAnRequestDTO.setGatewayVersion("1.0.0");
        HashMap<String, List<ZaMarketDetail>> bizData = new HashMap<>();
        bizData.put("data",dto.getData());
        zhongAnRequestDTO.setBizParam(RSAEncrypt.encrypt(JSON.toJSONString(bizData), RSApKey));
        BeanMap beanMap = BeanMap.create(zhongAnRequestDTO);
        zhongAnRequestDTO.setSign(getSignature(beanMap,signKey));

        HashMap<String, String> stringStringHashMap = httpProxyClient.sendByCode(zhongAnRequestDTO, url, false, MediaType.APPLICATION_JSON_UTF8_VALUE, null);
        System.out.println(stringStringHashMap);
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }


    /**
     * 生产签名串
     *
     * @param params 参与签名的字段集合
     * @param appId  appId
     * @return 签名串
     */
    public static String getSignature(BeanMap params, String appId) {
        Object[] keySet = params.keySet().toArray();
        Arrays.sort(keySet);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keySet.length; i++) {
            if (!"sign".equals(keySet[i])) {
                sb.append(keySet[i]).append("=");
                sb.append(params.get(keySet[i])).append("&");
            }
        }
        String str = sb.deleteCharAt(sb.lastIndexOf("&")).toString() + appId;
        System.out.println("签名字符串===>>  "+str);
        return Md5Utils.getMD5(str);
    }

}
