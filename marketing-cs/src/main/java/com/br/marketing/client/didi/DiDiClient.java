package com.br.marketing.client.didi;

import com.alibaba.fastjson.JSON;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.didi.input.*;
import com.br.marketing.client.didi.output.DiDiFailUserVO;
import com.br.marketing.client.didi.output.DiDiJMassResponseTO;
import com.br.marketing.client.didi.output.DiDiResponseTO;
import com.br.marketing.client.didi.utils.MD5Util;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.Impl.MockConfigServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description DiDiClient
 * @Author hong.chen
 * @CreateTime 2023/04/23
 */

@Service
@Slf4j
public class DiDiClient {
    @Value("${api.didi.smsUrl:https://admarketing-manhattan.xiaojukeji.com/crow/collision/bairong}")
    String smsUrl;
    @Value("${api.didi.reachUrl:https://admarketing-manhattan.xiaojukeji.com/crow/user/success/bairong}")
    String reachUrl;
    @Value("${api.didi.jmassSUrl:https://admarketing-manhattan.xiaojukeji.com/model/sample/bairong}")
    String jmassSUrl;
    @Value("${api.didi.failedUrl:https://admarketing-manhattan.xiaojukeji.com/crow/faileduser/mediaName}")
    String failUserUrl;

    @Value("${api.didi.token:DK&SgWl!fZ%WVSXe}")
    String token;

    @Value("${api.didi.scas:0001}")
    String scas;

    @Value("${api.didi.channelId:3140738836439875}")
    String channelId;

    @Value("#{${api.didi.channelIdMap:{bairong:'3140738836439875',bairongA:'3140738898634899'}}}")
    private Map<String, String> channelIdMap;

    @Value("${api.didi.isProxy:false}")
    Boolean isProxy;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    MockConfigServiceImpl mockConfigService;

    @Autowired
    HttpProxyClient httpProxyClient;

    public static final String PUSH_SMS_TRAFFIC_ACCESS = "pushSmsTrafficAccess";
    public static final String PUSH_REACH_SUCCESS = "pushReachSuccess";
    public static final String PUSH_JMASS = "pushJMASS";
    public static final String PUSH_FAIL = "pushFail";

    /**
     * 短信流量准入接口
     *
     * @return
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<DiDiResponseTO> pushSmsTrafficAccess(DiDiReqVO smsReqVO) {

//        if("4422e2da50db10f8375baf36b19c4113".equals(smsReqVO.getCustMobileMd5())||"a9cd0a1156768417143d154c2f181c06".equals(smsReqVO.getCustMobileMd5())){
//            return new Result<>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
//        }
//
//        if("2d3e557b00820dc73d0cc71cfd91f75b".equals(smsReqVO.getCustMobileMd5())){
//            DiDiResponseTO mock = JSON.parseObject("{\"errorCode\":10000,\"errorMessage\":\"成功\",\"data\":{\"result\":false}}", DiDiResponseTO.class);
//            return new Result<>().setCode(1).setDate(mock);
//        }

        try {
            String allowUrl = smsUrl;
            if (marketingCommonConfig.getDidiMediaNm() != null && StringUtils.isNotBlank(marketingCommonConfig.getDidiMediaNm().get(PUSH_SMS_TRAFFIC_ACCESS))) {
                allowUrl = allowUrl.replace("bairong", marketingCommonConfig.getDidiMediaNm().get(PUSH_SMS_TRAFFIC_ACCESS));
            }

            // 获取是否记录日志
            HashMap<String, List<Boolean>> isLog = getIsLog();
            List<Boolean> islogs = isLog.get(PUSH_SMS_TRAFFIC_ACCESS);

            // 构建请求参数
            DiDiSmsRequestTO smsRequestTO = new DiDiSmsRequestTO();
            smsRequestTO.setSign(smsReqVO.getCustMobileMd5());
            String timestamp = String.valueOf(System.currentTimeMillis());
            smsRequestTO.setTimestamp(timestamp);
            String signature = getSignature(smsReqVO.getCustMobileMd5(), timestamp);
            smsRequestTO.setSignature(signature);

            HashMap<String, String> resMap = new HashMap<>();
            // 获取挡板开关
            if (marketingCommonConfig.getDidiMockSwitch().get(PUSH_SMS_TRAFFIC_ACCESS)) {
                resMap.put("content", "{\"errorCode\":10000,\"errorMessage\":\"成功\",\"data\":{\"result\":true}}");
                resMap.put("httpcode", "200");
                mockConfigService.disappearTime(100, 50);
            } else {
                // 发送请求
                resMap = httpProxyClient.sendByCodeWithLog(smsRequestTO, allowUrl, isProxy, MediaType.APPLICATION_JSON_UTF8_VALUE,
                        JSON.toJSONString(smsReqVO), islogs.get(0), islogs.get(1));
            }

            // 1.httpcode不为200，需要重试
            if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
                if (!islogs.get(1)) {
                    log.error("调用滴滴短信流量接口异常-请求参数:{};返回:{}", JSON.toJSONString(smsReqVO), JSON.toJSONString(resMap));
                }
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }

            // 解析返回结果
            DiDiResponseTO smsResponseTO = JSON.parseObject(resMap.get("content"), DiDiResponseTO.class);

            // 2.errorCode=20000，需要重试
            if ("20000".equals(smsResponseTO.getErrorCode())) {
                if (!islogs.get(1)) {
                    log.error("调用滴滴短信流量接口异常-请求参数:{};返回:{}", JSON.toJSONString(smsReqVO), JSON.toJSONString(resMap));
                }
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }

            // 3.返回成功，无需重试
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(smsResponseTO);
        } catch (Exception e) {
            // 4.异常，需要重试
            log.error("调用滴滴短信流量接口异常" + e.getMessage(), e);
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

    /**
     * 触达成功接口
     *
     * @return
     */
    public Result<DiDiResponseTO> pushReachSuccess(DiDiReqVO smsReqVO) {
        try {
            // 获取是否记录日志
            HashMap<String, List<Boolean>> isLog = getIsLog();
            List<Boolean> islogs = isLog.get(PUSH_REACH_SUCCESS);

            // 构建请求参数
            DiDiReachRequestTO reachRequestTO = new DiDiReachRequestTO();
            reachRequestTO.setSign(smsReqVO.getCustMobileMd5());
            String timestamp = String.valueOf(System.currentTimeMillis());
            reachRequestTO.setTimestamp(timestamp);
            String signature = getSignature(smsReqVO.getCustMobileMd5(), timestamp);
            reachRequestTO.setSignature(signature);
            reachRequestTO.setScas(scas);
            reachRequestTO.setChannelId(channelId);

            HashMap<String, String> resMap = new HashMap<>();
            // 获取挡板开关
            if (marketingCommonConfig.getDidiMockSwitch().get(PUSH_REACH_SUCCESS)) {
                resMap.put("content", "{\"errorCode\":10000,\"errorMessage\":\"成功\",\"data\":{\"result\":true}}");
                resMap.put("httpcode", "200");
                mockConfigService.disappearTime(80, 30);
            } else {
                // 发送请求
                resMap = httpProxyClient.sendByCodeWithLog(reachRequestTO, reachUrl, isProxy,
                        MediaType.APPLICATION_JSON_UTF8_VALUE,
                        JSON.toJSONString(smsReqVO), islogs.get(0), islogs.get(1));
            }

            // 1.httpcode不为200，需要重试
            if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
                if (!islogs.get(1)) {
                    log.error("调用滴滴触达成功接口异常-请求参数:{};返回:{}", JSON.toJSONString(smsReqVO), JSON.toJSONString(resMap));
                }
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }

            // 解析返回结果
            DiDiResponseTO smsResponseTO = JSON.parseObject(resMap.get("content"), DiDiResponseTO.class);

            // 2.errorCode=20000，需要重试
            if ("20000".equals(smsResponseTO.getErrorCode())) {
                if (!islogs.get(1)) {
                    log.error("调用滴滴触达成功接口异常-请求参数:{};返回:{}", JSON.toJSONString(smsReqVO), JSON.toJSONString(resMap));
                }
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }

            // 3.返回成功，无需重试
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(smsResponseTO);
        } catch (Exception e) {
            // 4.异常，需要重试
            log.error("调用滴滴触达成功接口异常" + e.getMessage(), e);
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }


    /**
     * 2023-08-08 14:10
     * sftp文件方式推送触达成功接口
     *
     * @param diDiReachBO 业务封装
     * @return DiDiResponseTO
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<DiDiResponseTO> pushReachSuccess(DiDiReachBO diDiReachBO) {
        Result<DiDiResponseTO> result = new Result<>();
        try {
            // 获取是否记录日志
            HashMap<String, List<Boolean>> isLog = getIsLog();
            List<Boolean> islogs = isLog.get(PUSH_REACH_SUCCESS);
            DiDiReqVO reqVO = diDiReachBO.getDiDiReqVO();
            // 构建请求参数
            DiDiReachRequestTO reachRequest = diDiReachBO.getDiDiReachRequestTO();
            reachRequest.setSign(reqVO.getCustMobileMd5());
            String timestamp = String.valueOf(System.currentTimeMillis());
            reachRequest.setTimestamp(timestamp);
            String signature = getSignature(reqVO.getCustMobileMd5(), timestamp);
            reachRequest.setSignature(signature);
            reachRequest.setChannelId(channelIdMap.getOrDefault(reqVO.getMediaName(), channelIdMap.get("bairongA")));
            HashMap<String, String> resMap = new HashMap<>();
            String httpcode = "200";
            String codeKey = "httpcode";
            String contentKey = "content";
            // 获取挡板开关
            if (marketingCommonConfig.getDidiMockSwitch().get(PUSH_REACH_SUCCESS)) {
                resMap.put(contentKey, "{\"errorCode\":10000,\"errorMessage\":\"成功\",\"data\":\"\"}");
                resMap.put(codeKey, httpcode);
                mockConfigService.disappearTime(80, 30);
            } else {
                String url = reachUrl.replace("bairong", reqVO.getMediaName());
                // 发送请求
                resMap = httpProxyClient.sendByCodeWithLog(reachRequest, url, isProxy,
                        MediaType.APPLICATION_JSON_UTF8_VALUE,
                        JSON.toJSONString(reqVO), islogs.get(0), islogs.get(1));
            }

            // 1.httpcode不为200，需要重试
            if (!httpcode.equals(resMap.get(codeKey)) || StringUtils.isBlank(resMap.get(contentKey))) {
                if (!islogs.get(1)) {
                    log.error("调用滴滴触达成功接口异常-请求参数:{};返回:{}", JSON.toJSONString(reqVO), JSON.toJSONString(resMap));
                }
                result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
                return result;
            }
            // 解析返回结果
            DiDiResponseTO smsResponseTO = JSON.parseObject(resMap.get(contentKey), DiDiResponseTO.class);
            // 2.errorCode=20000，需要重试
            if ("20000".equals(smsResponseTO.getErrorCode())) {
                if (!islogs.get(1)) {
                    log.error("调用滴滴触达成功接口异常-请求参数:{};返回:{}", JSON.toJSONString(reqVO)
                            , JSON.toJSONString(resMap));
                }
                result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
                return result;
            }

            // 3.返回成功，无需重试
            result.setCode(ResultCode.SUCCESS.getValue());
            result.setDate(smsResponseTO);
            return result;
        } catch (Exception e) {
            // 4.异常，需要重试
            log.error("调用滴滴触达成功接口异常" + e.getMessage(), e);
            result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            return result;
        }
    }

    /**
     * 联合建模接口
     *
     * @return
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d,0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<DiDiJMassResponseTO> pushJMASS(DiDiReqVO smsReqVO) {
        try {
            String url = jmassSUrl.replace("mediaName", smsReqVO.getMediaName());
            // 获取是否记录日志
            HashMap<String, List<Boolean>> isLog = getIsLog();
            List<Boolean> islogs = isLog.get(PUSH_JMASS);

            DiDiJmassRequestTO jmassRequestTO = new DiDiJmassRequestTO();
            jmassRequestTO.setSign(smsReqVO.getCustMobileMd5());

            HashMap<String, String> resMap = new HashMap<>();
            // 获取挡板开关
            if (marketingCommonConfig.getDidiMockSwitch().get(PUSH_JMASS)) {
                resMap.put("content", "{\"errorCode\":10000,\"errorMessage\":\"成功\",\"data\":\"\"}");
                resMap.put("httpcode", "200");
            } else {
                // 发送请求
                resMap = httpProxyClient.sendByCodeWithLog(jmassRequestTO, url, isProxy,
                        MediaType.APPLICATION_JSON_UTF8_VALUE,
                        JSON.toJSONString(smsReqVO), islogs.get(0), islogs.get(1));
            }

            // 1.httpcode不为200，需要重试
            if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
                if (!islogs.get(1)) {
                    log.error("调用滴滴联合建模接口异常-请求参数:{};返回:{}", JSON.toJSONString(smsReqVO), JSON.toJSONString(resMap));
                }
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }

            // 解析返回结果
            DiDiJMassResponseTO jMassResponseTO = JSON.parseObject(resMap.get("content"), DiDiJMassResponseTO.class);

            // 2.errorCode=20000，需要重试
            if ("20000".equals(jMassResponseTO.getErrorCode())) {
                if (!islogs.get(1)) {
                    log.error("调用滴滴联合建模接口异常-请求参数:{};返回:{}", JSON.toJSONString(smsReqVO), JSON.toJSONString(resMap));
                }
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }

            // 3.返回成功，无需重试
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(jMassResponseTO);
        } catch (Exception e) {
            // 4.异常，需要重试
            log.error("调用滴滴短信流量接口异常" + e.getMessage(), e);
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

//    public static void main(String[] args) {
//        //流量准入
//        HashMap<String,String> reqMap = new HashMap<>();
////        String cell = MD5Util.encode("12312562248");
//        String cell = "10b3ac05cd4affc7e19e6d624ac6f2e7";
//        reqMap.put("sign",cell);
//        String timestamp = String.valueOf(System.currentTimeMillis());
//        reqMap.put("timestamp",timestamp);
//        StringBuilder sb = new StringBuilder();
//        sb.append(cell).append(timestamp).append("DK&SgWl!fZ%WVSXe");
//        reqMap.put("signature",MD5Util.encode(String.valueOf(sb)));
//        System.out.println(JSON.toJSONString(reqMap));
//
//        //触达
//        HashMap<String,String> reqMap = new HashMap<>();
//        String cell = MD5Util.encode("12312341234");
//        reqMap.put("sign",cell);
//        String timestamp = String.valueOf(System.currentTimeMillis());
//        reqMap.put("timestamp",timestamp);
//        StringBuilder sb = new StringBuilder();
//        sb.append(cell).append(timestamp).append("DK&SgWl!fZ%WVSXe");
//        reqMap.put("signature",MD5Util.encode(String.valueOf(sb)));
//        reqMap.put("scas","1001");
//        reqMap.put("channelId","3140738898634899");
//        System.out.println(JSON.toJSONString(reqMap));
//
//        //失效
//    }


    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<DiDiFailUserVO> failUser(DiDiReqVO smsReqVO) {
        try {
            // 获取是否记录日志
            HashMap<String, List<Boolean>> isLog = getIsLog();
            List<Boolean> islogs = isLog.get(PUSH_FAIL);

            // 构建请求参数
            HashMap<String,String> reqMap = new HashMap<>();
            reqMap.put("sign",smsReqVO.getCustMobileMd5());
            String timestamp = String.valueOf(System.currentTimeMillis());
            reqMap.put("timestamp",timestamp);
            reqMap.put("signature",getSignature(smsReqVO.getCustMobileMd5(), timestamp));

            HashMap<String, String> resMap = new HashMap<>();
            // 获取挡板开关
            if (marketingCommonConfig.getDidiMockSwitch().get(PUSH_FAIL)) {
                resMap.put("content", "{\"errorCode\":10000,\"errorMessage\":\"成功\",\"data\":{\"result\":true}}");
                resMap.put("httpcode", "200");
                mockConfigService.disappearTime(100, 50);
            } else {
                String failUrl = failUserUrl;
                if (marketingCommonConfig.getDidiMediaNm() != null && StringUtils.isNotBlank(marketingCommonConfig.getDidiMediaNm().get(PUSH_FAIL))) {
                    failUrl = failUrl.replace("mediaName", marketingCommonConfig.getDidiMediaNm().get(PUSH_FAIL));
                }
                // 发送请求
                resMap = httpProxyClient.sendByCodeWithLog(reqMap, failUrl, isProxy,
                        MediaType.APPLICATION_JSON_UTF8_VALUE,
                        JSON.toJSONString(smsReqVO), islogs.get(0), islogs.get(1));
            }

            // 1.httpcode不为200，需要重试
            if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
                if (!islogs.get(1)) {
                    log.error("调用滴滴营销失败接口异常-请求参数:{};返回:{}", JSON.toJSONString(smsReqVO), JSON.toJSONString(resMap));
                }
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }

            // 解析返回结果
            DiDiFailUserVO failReq = JSON.parseObject(resMap.get("content"), DiDiFailUserVO.class);

            // 2.errorCode=20000，需要重试
            if ("20000".equals(failReq.getErrorCode())) {
                if (!islogs.get(1)) {
                    log.error("调用滴滴营销失败接口异常-请求参数:{};返回:{}", JSON.toJSONString(smsReqVO), JSON.toJSONString(resMap));
                }
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }

            if(new Integer(10000).equals(failReq.getErrorCode())){
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            }else{
                return new Result().setCode(ResultCode.FAIL.getValue()).setDate(failReq);
            }
        } catch (Exception e) {
            // 4.异常，需要重试
            log.error("调用滴滴营销失败接口异常" + e.getMessage(), e);
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

    /**
     * 获取签名串
     * MD5(sign + timestamp + token) 32位小写
     *
     * @param sign      手机号的md5值
     * @param timestamp 时间戳
     * @return 签名串
     */
    private String getSignature(String sign, String timestamp) {
        StringBuilder sb = new StringBuilder();
        sb.append(sign).append(timestamp).append(token);
        return MD5Util.encode(String.valueOf(sb));
    }

    /**
     * {"pushSmsTrafficAccess":[false,true],"pushReachSuccess":[false,true],"pushJMASS":[false,true]}
     *
     * @return
     */
    private HashMap<String, List<Boolean>> getIsLog() {
        HashMap<String, List<Boolean>> res = new HashMap<>();
        HashMap<String, List<Boolean>> apiLogMark = marketingCommonConfig.getApiLogMark();
        if(apiLogMark!=null){
            res.putAll(apiLogMark);
        }
        if (apiLogMark == null || !apiLogMark.containsKey(PUSH_SMS_TRAFFIC_ACCESS)) {
            ArrayList<Boolean> mark = new ArrayList<>();
            mark.add(false);
            mark.add(true);
            res.put(PUSH_SMS_TRAFFIC_ACCESS, mark);
        }
        if (apiLogMark == null || !apiLogMark.containsKey(PUSH_REACH_SUCCESS)) {
            ArrayList<Boolean> mark = new ArrayList<>();
            mark.add(false);
            mark.add(true);
            res.put(PUSH_REACH_SUCCESS, mark);
        }
        if (apiLogMark == null || !apiLogMark.containsKey(PUSH_JMASS)) {
            ArrayList<Boolean> mark = new ArrayList<>();
            mark.add(false);
            mark.add(true);
            res.put(PUSH_JMASS, mark);
        }
        return res;
    }
}
