package com.br.marketing.client.zhongan;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.zhongan.input.ZaMarketDataDTO;
import com.br.marketing.client.zhongan.input.ZaMarketDetail;
import com.br.marketing.client.zhongan.input.ZhongAnRequestDTO;
import com.br.marketing.client.zhongan.input.ZkReqDTO;
import com.br.marketing.client.zhongan.output.MarketDetailVO;
import com.br.marketing.client.zhongan.output.ZhongAnResponseVO;
import com.br.marketing.client.zhongan.output.ZkReponseVO;
import com.br.marketing.client.zhongan.utils.Md5OfZanUtils;
import com.br.marketing.client.zhongan.utils.RSAEncrypt;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
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
import java.util.UUID;

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

    String bXZKApiKey = "zadpreloan.nexusmetric.br.3360001";

    String RSApKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCVuaxc7ZznPdvsH0nhd2eQ/uhu/LewJqUVMvdYUKwXPxzGBUz8cVKyltwpMJ03uMPx+RStWnkWcmCSeQdqiw27FtaPELOxxQQc06OGBXfp5R86MKp2+bkdPRSkpUKK2X8vCyiopQojBXbaVzRUwjPPsQAsDinCGkSUirWETxfCxwIDAQAB";

    /**
     * 推送明细
     * @param dto
     * @return
     */
    public Result pushDetail(ZaMarketDataDTO dto) {
        try {
            ZhongAnRequestDTO zhongAnRequestDTO = new ZhongAnRequestDTO();
            zhongAnRequestDTO.setApiKey(xinDaiDetailApiKey);
            zhongAnRequestDTO.setReqNo(UUID.randomUUID().toString().replaceAll("-", ""));
            zhongAnRequestDTO.setReqDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            zhongAnRequestDTO.setGatewayVersion("1.0.0");
            zhongAnRequestDTO.setBizParam(RSAEncrypt.encrypt(JSON.toJSONString(dto), RSApKey));
            BeanMap beanMap = BeanMap.create(zhongAnRequestDTO);
            zhongAnRequestDTO.setSign(getSignature(beanMap, signKey));
            HashMap<String, String> resMap = httpProxyClient.sendByCode(zhongAnRequestDTO, url, false, MediaType.APPLICATION_JSON_UTF8_VALUE, null);
            if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }
            ZhongAnResponseVO resVo = JSON.parseObject(resMap.get("content"), ZhongAnResponseVO.class);
            Result result = checkGateWay(resVo);
            if(!ResultCode.SUCCESS.getValue().equals(result.getCode())){
                return result;
            }
            MarketDetailVO marketDetailVO = JSON.parseObject(resVo.getBizData(), MarketDetailVO.class);
            if("1".equals(marketDetailVO.getRespCode())){
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            }
            if("0".equals(marketDetailVO.getRespCode())||"3".equals(marketDetailVO.getRespCode())||"6".equals(marketDetailVO.getRespCode())){
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }
            return new Result().setCode(ResultCode.FAIL.getValue());
        } catch (Exception ex) {
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

    /**
     * 保险撞库
     * @param zkReqDTO
     * @return
     */
    public Result<Boolean> zkBx(ZkReqDTO zkReqDTO){
        return zk(zkReqDTO,bXZKApiKey);
    }

    /**
     * 信贷撞库
     * @param zkReqDTO
     * @return
     */
    public Result<Boolean> zkXd(ZkReqDTO zkReqDTO){
        return zk(zkReqDTO,xinDaiZKApiKey);
    }


    public Result<Boolean> zk(ZkReqDTO zkReqDTO,String apiKey){
        try {
            ZhongAnRequestDTO zhongAnRequestDTO = new ZhongAnRequestDTO();
            zhongAnRequestDTO.setApiKey(apiKey);
            zhongAnRequestDTO.setReqNo(UUID.randomUUID().toString().replaceAll("-", ""));
            zhongAnRequestDTO.setReqDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            zhongAnRequestDTO.setGatewayVersion("1.0.0");
            zhongAnRequestDTO.setBizParam(RSAEncrypt.encrypt(JSON.toJSONString(zkReqDTO), RSApKey));
            BeanMap beanMap = BeanMap.create(zhongAnRequestDTO);
            zhongAnRequestDTO.setSign(getSignature(beanMap, signKey));
            HashMap<String, String> resMap = httpProxyClient.sendByCode(zhongAnRequestDTO, url, false, MediaType.APPLICATION_JSON_UTF8_VALUE, null);
            if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }
            ZhongAnResponseVO resVo = JSON.parseObject(resMap.get("content"), ZhongAnResponseVO.class);
            Result result = checkGateWay(resVo);
            if(!ResultCode.SUCCESS.getValue().equals(result.getCode())){
                return result;
            }
            ZkReponseVO zkVo = JSON.parseObject(resVo.getBizData(), ZkReponseVO.class);
            if("1".equals(zkVo.getRespCode())){
                return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(zkVo.getAccess());
            }
            if("0".equals(zkVo.getRespCode())||"3".equals(zkVo.getRespCode())||"6".equals(zkVo.getRespCode())){
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }
            return new Result().setCode(ResultCode.FAIL.getValue());
        } catch (Exception ex) {
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
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
        System.out.println("签名字符串===>>  " + str);
        return Md5OfZanUtils.getMD5(str);
    }

    private Result checkGateWay(ZhongAnResponseVO responseVO) {
        //成功
        if (responseVO.getSuccess()) {
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }

        //进行重试
        if ("GW_0008".equals(responseVO.getResultCode())
                || "GW_0018".equals(responseVO.getResultCode())
                || "GW_0019".equals(responseVO.getResultCode())) {
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }

        return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(JSON.toJSONString(responseVO));
    }
}
