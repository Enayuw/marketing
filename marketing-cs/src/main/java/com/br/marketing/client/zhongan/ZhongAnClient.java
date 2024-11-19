package com.br.marketing.client.zhongan;

import com.alibaba.fastjson.JSON;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.encryption.BrCipherMaker;
import com.br.common.log.AlertLog;
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
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class ZhongAnClient {

    @Value("${api.zhongAn.api:00}")
    String url;

    @Value("${api.zhongAn.isProxy:false}")
    Boolean isProxy;

    @Value("${api.zhongAn.rsapKey:00}")
    String rsapKey;

    @Autowired
    HttpProxyClient httpProxyClient;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;


    String signKey = "59018a92ca1e0d1e38f7da0617491abe";

    public static String XdChannelCode;

    public static String BxChannelCode;

    String xinDaiDetailApiKey = "channel.marketDetail.07brdyy01";

    String xinDaiZKApiKey = "zadpreloan.nexusmetric.07brdyy01";

    String bXZKApiKey = "zadpreloan.nexusmetric.br.3360001";

    final static String zanPushDetail = "zanPushDetail";

    final static String zanZk = "zanZk";

    @Value("${api.zhongAn.xdCode:07brdyy01}")
    public void setXdCode(String xdCode) {
        XdChannelCode = xdCode;
    }

    @Value("${api.zhongAn.bxCode:3360001}")
    public void setBxCode(String bxCode) {
        BxChannelCode = bxCode;
    }

    /**
     * 推送明细
     * 判断code
     * 1-接口请求成功
     * 0-接口请求失败
     * 500-需要重试
     *
     * @param dto
     * @return
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result pushDetail(ZaMarketDataDTO dto) {
        try {
            HashMap<String, List<Boolean>> isLog = getIsLog();
            List<Boolean> islogs = isLog.get(zanPushDetail);
            ZhongAnRequestDTO zhongAnRequestDTO = new ZhongAnRequestDTO();
            zhongAnRequestDTO.setApiKey(xinDaiDetailApiKey);
            zhongAnRequestDTO.setReqNo(UUID.randomUUID().toString().replaceAll("-", ""));
            zhongAnRequestDTO.setReqDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            zhongAnRequestDTO.setGatewayVersion("1.0.0");
            zhongAnRequestDTO.setBizParam(RSAEncrypt.encrypt(JSON.toJSONString(dto), rsapKey));
            BeanMap beanMap = BeanMap.create(zhongAnRequestDTO);
            zhongAnRequestDTO.setSign(getSignature(beanMap, signKey));
            HashMap<String, String> resMap = httpProxyClient.sendByCodeWithLog(zhongAnRequestDTO, url, isProxy, MediaType.APPLICATION_JSON_UTF8_VALUE, JSON.toJSONString(dto), islogs.get(0), islogs.get(1));
            if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
                if (!islogs.get(1)) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_INTERFACEERROR.getCode(),
                            "众安推送明细失败-请求参数:" + JSON.toJSONString(dto) + ";返回:" + JSON.toJSONString(resMap)));
                }
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }
            ZhongAnResponseVO resVo = JSON.parseObject(resMap.get("content"), ZhongAnResponseVO.class);
            Result result = checkGateWay(resVo);
            if (ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())) {
                if (!islogs.get(1)) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_INTERFACEERROR.getCode(),
                            "众安推送明细失败-请求参数:" + JSON.toJSONString(dto) + ";返回:" + JSON.toJSONString(resMap)));
                }
                return result;
            }
            MarketDetailVO marketDetailVO = JSON.parseObject(resVo.getBizData(), MarketDetailVO.class);
            if ("1".equals(marketDetailVO.getRespCode())) {
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            }
            if (!islogs.get(1)) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_INTERFACEERROR.getCode(),
                        "众安推送明细失败-请求参数:" + JSON.toJSONString(dto) + ";返回:" + JSON.toJSONString(resMap)));
            }
            if ("0".equals(marketDetailVO.getRespCode())
                    || "3".equals(marketDetailVO.getRespCode())
                    || "6".equals(marketDetailVO.getRespCode())
                    || "9999".equals(marketDetailVO.getRespCode())) {
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_INTERFACEERROR.getCode(),
                    "无需重试 众安推送明细结果：" + JSON.toJSONString(resVo)));
            return new Result().setCode(ResultCode.FAIL.getValue());
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_INTERFACEERROR.getCode(),
                    "众安推送明细异常！"), ex);
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

    /**
     * 保险撞库
     * 判断code
     * 1-接口请求成功
     * 0-接口请求失败
     * 500-需要重试
     *
     * @param zkReqDTO
     * @return
     */
    public Result<ZkReponseVO> zkBx(ZkReqDTO zkReqDTO) {
        return zk(zkReqDTO, bXZKApiKey);
    }

    /**
     * 信贷撞库
     * 判断code
     * 1-接口请求成功
     * 0-接口请求失败
     * 500-需要重试
     *
     * @param zkReqDTO
     * @return
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<ZkReponseVO> zkXd(ZkReqDTO zkReqDTO) {
//        上线需要去除掉,模拟调用不可营销的数据
       /* if(Arrays.asList("baca0e6571447795d04c60b9388c5a9d","9acb88dc59a878889d25aba379a844c5").contains(zkReqDTO.getCustMobileMd5())){
            ZkReponseVO zkReponseVO = new ZkReponseVO();
            zkReponseVO.setAccess(false);
            zkReponseVO.setRespNo("bbbe484f3bd94cb296fa3127be55d0c8");
            zkReponseVO.setRespCode("1");
            zkReponseVO.setStatus("SUCCESS");
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(zkReponseVO);
        }*/
        return zk(zkReqDTO, xinDaiZKApiKey);
    }

    public Result<ZkReponseVO> zk(ZkReqDTO zkReqDTO, String apiKey) {
        try {
            HashMap<String, List<Boolean>> isLog = getIsLog();
            List<Boolean> islogs = isLog.get(zanZk);
            ZhongAnRequestDTO zhongAnRequestDTO = new ZhongAnRequestDTO();
            zhongAnRequestDTO.setApiKey(apiKey);
            zhongAnRequestDTO.setReqNo(UUID.randomUUID().toString().replaceAll("-", ""));
            zhongAnRequestDTO.setReqDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            zhongAnRequestDTO.setGatewayVersion("1.0.0");
            zhongAnRequestDTO.setBizParam(RSAEncrypt.encrypt(JSON.toJSONString(zkReqDTO), rsapKey));
            BeanMap beanMap = BeanMap.create(zhongAnRequestDTO);
            zhongAnRequestDTO.setSign(getSignature(beanMap, signKey));
            HashMap<String, String> resMap = httpProxyClient.sendByCodeWithLog(zhongAnRequestDTO, url, isProxy, MediaType.APPLICATION_JSON_UTF8_VALUE, JSON.toJSONString(zkReqDTO), islogs.get(0), islogs.get(1));
            if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
                if (!islogs.get(1)) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_INTERFACEERROR.getCode(),
                            "撞库失败请求参数：zkreq:" + JSON.toJSONString(zkReqDTO) + ",apikey:" + apiKey
                                    + ";撞库返回:" + JSON.toJSONString(resMap)));
                }
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }
            ZhongAnResponseVO resVo = JSON.parseObject(resMap.get("content"), ZhongAnResponseVO.class);
            Result result = checkGateWay(resVo);
            if (ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(result.getCode())) {
                if (!islogs.get(1)) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_INTERFACEERROR.getCode(),
                            "撞库失败请求参数：zkreq:" + JSON.toJSONString(zkReqDTO) + ",apikey:" + apiKey
                                    + ";撞库返回:" + JSON.toJSONString(resMap)));
                }
                return result;
            }
            ZkReponseVO zkVo = JSON.parseObject(resVo.getBizData(), ZkReponseVO.class);
            if ("1".equals(zkVo.getRespCode())) {
                return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(zkVo);
            }
            if (!islogs.get(1)) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_INTERFACEERROR.getCode(),
                        "撞库失败请求参数：zkreq:" + JSON.toJSONString(zkReqDTO) + ",apikey:" + apiKey
                                + ";撞库返回:" + JSON.toJSONString(resMap)));
            }
            if ("0".equals(zkVo.getRespCode())
                    || "3".equals(zkVo.getRespCode())
                    || "6".equals(zkVo.getRespCode())
                    || "9999".equals(zkVo.getRespCode())
                    || "0032".equals(zkVo.getRespCode())
            ) {
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }
            if (bXZKApiKey.equals(apiKey) && "9998".equals(zkVo.getRespCode())) {
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_INTERFACEERROR.getCode(),
                    "无需重试 众安撞库结果：" + JSON.toJSONString(resVo)));
            return new Result().setCode(ResultCode.FAIL.getValue());
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ZHONGAN_INTERFACEERROR.getCode(), "众安撞库异常!"), ex);
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

    /**
     * {"zanPushDetail":[false,true],"zanZk":[false,true]}
     *
     * @return
     */
    private HashMap<String, List<Boolean>> getIsLog() {
        HashMap<String, List<Boolean>> res = new HashMap<>();
        HashMap<String, List<Boolean>> apiLogMark = marketingCommonConfig.getApiLogMark();
        if (apiLogMark == null || !apiLogMark.containsKey(zanPushDetail)) {
            ArrayList<Boolean> mark = new ArrayList<>();
            mark.add(false);
            mark.add(true);
            res.put(zanPushDetail, mark);
        } else {
            res.put(zanPushDetail, apiLogMark.get(zanPushDetail));
        }
        if (apiLogMark == null || !apiLogMark.containsKey(zanZk)) {
            ArrayList<Boolean> mark = new ArrayList<>();
            mark.add(false);
            mark.add(true);
            res.put(zanZk, mark);
        } else {
            res.put(zanZk, apiLogMark.get(zanZk));
        }
        return res;
    }
}
