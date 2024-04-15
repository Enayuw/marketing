package com.br.marketing.client.haier;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.haier.input.HaierCollidingDataDTO;
import com.br.marketing.client.haier.input.HaierReqDTO;
import com.br.marketing.client.haier.output.PushDTO;
import com.br.marketing.client.haier.output.Response2Entity;
import com.br.marketing.client.haier.output.ResponseInfoEntity;
import com.br.marketing.client.haier.output.ResultQueryDTO;
import com.br.marketing.client.haier.utils.AESUtil;
import com.br.marketing.client.haier.utils.RsaUtil;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.HaierData;
import com.br.marketing.entity.HaierDataExample;
import com.br.marketing.entity.HaierReq;
import com.br.marketing.mapper.HaierDataMapper;
import com.br.marketing.mapper.HaierReqMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 海尔消金客户端
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/12/2 15:14
 */
@Component
@Slf4j
public class HaierServiceClient {


    @Value("${api.haier.custData.receive.url:}")
    private String url;

    @Value("${api.haier.custData.receive.info.url:}")
    private String urlInfo;

    @Value("${api.haier.apiCode:2a43ad9e9b6a247ef9613761dcb191d7}")
    private String apiCode;

    /**
     * 加密公钥
     */
    @Value("${api.haier.apiKey:}")
    private String apiKey;


    @Resource
    private HttpProxyClient httpProxyClient;

    @Resource
    HaierDataMapper haierDataMapper;

    @Resource
    HaierReqMapper haierReqMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Value("${api.haier.colliding.isProxy:true}")
    private Boolean collidingIsProxy;
    @Value("${api.haier.colliding.rsaPublicKey:00}")
    private String collidingRsaPublicKey;
    @Value("${api.haier.colliding.rsaPrivateKey:0}")
    private String collidingRsaPrivateKey;
    @Value("${api.haier.colliding.url:0}")
    private String collidingUrl;


    /**
     * 推送数据客户端
     *
     * @param formData 发送的数据
     * @return Response2Entity 响应信息
     * @throws Exception 大概率序列化异常，具体请自行打印异常信息
     * @author Guo Zeqiang
     * @dateTime 2021/12/3 16:43
     */
//    public <T> Response2Entity pushToTeleSales(List<T> list, Function<List<T>, Set<PushDTO.DataItems>> function
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<Response2Entity> pushToTeleSales(PushDTO.FormData formData, int retr) throws Exception {
        Result<Response2Entity> result = new Result<>();
        Assert.notNull(formData, "\"List\" is not null");
//        log.warn("##地址：{}；apicode：{}；apikey：{}", url, apiCode, apiKey);
        PushDTO pushDTO = new PushDTO(apiCode, formData, apiKey);
//        log.warn("&&发送内容：[{}]", pushDTO);
        final HashMap<String, String> stringStringHashMap = httpProxyClient.sendByCodeZw(pushDTO, url, true, MediaType.APPLICATION_JSON_UTF8_VALUE, "");
        final String httpCode = stringStringHashMap.getOrDefault("httpcode", "5000");
        if (httpCode.equals("200")) {
            final String respStr = stringStringHashMap.getOrDefault("content", "");
//            log.warn("%%应答内容：[{}]", respStr);
            if (StringUtils.isEmpty(respStr)) {
                result.setCode(ResultCode.FAIL.getValue()).setMessage("无应答消息");
                return result;
            }
            result.setCode(ResultCode.SUCCESS.getValue()).setDate(JSONObject.parseObject(respStr, Response2Entity.class));
        } else {
            result.setCode(ResultCode.FAIL.getValue()).setMessage(stringStringHashMap.getOrDefault("content", ""));
        }
        return result;
    }


    //    @RetryMethod
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<Response2Entity> pushToTeleSalesWithIds(HaierReqDTO haierReqDTO, int retr) throws Exception {
        PushDTO.FormData formData = haierReqDTO.getFormData();
        List<Long> ids = haierReqDTO.getIds();
        Result<Response2Entity> result = new Result<>();
        Assert.notNull(formData, "\"List\" is not null");
//        log.warn("##地址：{}；apicode：{}；apikey：{}", url, apiCode, apiKey);
        if (log.isWarnEnabled()) {
            log.warn(String.format("推送海尔数据：%s", JSON.toJSONString(formData)));
        }
        PushDTO pushDTO = new PushDTO(apiCode, formData, apiKey);
//        log.warn("&&发送内容：[{}]", pushDTO);
        final HashMap<String, String> stringStringHashMap = httpProxyClient.sendByCodeZw(pushDTO, url, true, MediaType.APPLICATION_JSON_UTF8_VALUE, "");
        final String httpCode = stringStringHashMap.getOrDefault("httpcode", "5000");
        if (httpCode.equals("200")) {
            final String respStr = stringStringHashMap.getOrDefault("content", "");
            log.warn("%%应答内容：[{}]", respStr);
            if (StringUtils.isEmpty(respStr)) {
                result.setCode(ResultCode.FAIL.getValue()).setMessage("无应答消息");
                return result;
            }
            Response2Entity response2Entity = JSONObject.parseObject(respStr, Response2Entity.class);
            if (response2Entity != null
                && response2Entity.getHead() != null
                && "00000".equals(response2Entity.getHead().getRetFlag())) {
                HaierData record = new HaierData();
                record.setPushStatus(2);
                record.setRuleType("2".equals(haierReqDTO.getFormData().getType()) ? "3" : ("3".equals(haierReqDTO.getFormData().getType()) ? "4" : null));
                HaierDataExample updateExample = new HaierDataExample();
                updateExample.createCriteria().andIdIn(ids);
                haierDataMapper.updateByExampleSelective(record, updateExample);

                HaierReq req = new HaierReq();
                req.setReqId(formData.getRequestId());
                req.setDataId(Joiner.on(",").join(ids));
                req.setCreateTime(new Date());
                req.setNum(ids.size());
                haierReqMapper.insertSelective(req);
                result.setCode(ResultCode.SUCCESS.getValue()).setDate(response2Entity);
            } else {
                result.setCode(ResultCode.FAIL.getValue()).setDate(response2Entity);
            }
        } else {
            result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(stringStringHashMap.getOrDefault("content", ""));
        }
        return result;
    }


    public Result<Response2Entity> pushToTeleSalesWithSave(HaierReqDTO haierReqDTO) {
        PushDTO.FormData formData = haierReqDTO.getFormData();
        HashMap<String, String> ruleMap = haierReqDTO.getRuleMap();
        Result<Response2Entity> result = new Result<>();
        Assert.notNull(formData, "\"List\" is not null");
//        log.warn("##地址：{}；apicode：{}；apikey：{}", url, apiCode, apiKey);
        if (log.isWarnEnabled()) {
            log.warn(String.format("推送海尔数据：%s", JSON.toJSONString(formData)));
        }
        PushDTO pushDTO = null;
        try {
            pushDTO = new PushDTO(apiCode, formData, apiKey);
        } catch (Exception e) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage(e.getMessage());
        }
//        log.warn("&&发送内容：[{}]", pushDTO);
        final HashMap<String, String> stringStringHashMap = httpProxyClient.sendByCodeZw(pushDTO, url, true, MediaType.APPLICATION_JSON_UTF8_VALUE, "");
        final String httpCode = stringStringHashMap.getOrDefault("httpcode", "5000");
        if (httpCode.equals("200")) {
            final String respStr = stringStringHashMap.getOrDefault("content", "");
            log.warn("%%应答内容：[{}]", respStr);
            if (StringUtils.isEmpty(respStr)) {
                result.setCode(ResultCode.FAIL.getValue()).setMessage("无应答消息");
                return result;
            }
            Response2Entity response2Entity = JSONObject.parseObject(respStr, Response2Entity.class);
            Date date = new Date();
            if (response2Entity != null
                && response2Entity.getHead() != null
                && "00000".equals(response2Entity.getHead().getRetFlag())) {
                ArrayList<Long> ids = new ArrayList<>();
                String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                for (PushDTO.DataItems dataItem : haierReqDTO.getFormData().getDataItems()) {
                    HaierData record = new HaierData();
                    record.setApiCode(haierReqDTO.getApiCode());
                    record.setCustNum(dataItem.getCustNum());
                    record.setTaskId(dataItem.getTaskId());
                    record.setCreateTime(date);
                    record.setLocalId(666L);
                    record.setPushStatus(2);
                    record.setRuleType(ruleMap.get(dataItem.getTaskId().concat(":").concat(dataItem.getCustNum())));
                    record.setType(haierReqDTO.getFormData().getType());
                    record.setCreateDate(Integer.valueOf(yyyyMMdd));
                    haierDataMapper.insertSelective(record);
                    ids.add(record.getId());
                }
                HaierReq req = new HaierReq();
                req.setReqId(formData.getRequestId());
                req.setDataId(Joiner.on(",").join(ids));
                req.setCreateTime(new Date());
                req.setNum(ids.size());
                haierReqMapper.insertSelective(req);
                result.setCode(ResultCode.SUCCESS.getValue()).setDate(response2Entity);
            } else {
                result.setCode(ResultCode.FAIL.getValue()).setDate(response2Entity);
            }
        } else {
            result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(stringStringHashMap.getOrDefault("content", ""));
        }
        return result;
    }

    public Result<Response2Entity> pushToTeleSales(PushDTO.FormData formData) throws Exception {
        return pushToTeleSales(formData, 0);
    }

    /**
     * 推送数据结果查询客户端
     *
     * @param requestId 推送数据的批次号（自己葛的）
     * @return ResponseInfoEntity 查询响应信息
     * @throws Exception 请自行打印异常信息
     * @author Guo Zeqiang
     * @dateTime 2021/12/6 12:14
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<ResponseInfoEntity> resultQueryPushToTeleSales(String requestId, int retr) throws Exception {
        Result<ResponseInfoEntity> result = new Result<>();
        log.warn("##查询接口地址：{}；apicode：{}；apikey：{}", urlInfo, apiCode, apiKey);
        ResultQueryDTO resultQueryDTO = new ResultQueryDTO(apiCode, requestId, apiKey);
        log.warn("&&查询接口发送内容：[{}]", resultQueryDTO);
        final HashMap<String, String> returnMap = httpProxyClient.sendByCodeZw(resultQueryDTO, urlInfo, true, MediaType.APPLICATION_JSON_UTF8_VALUE, "");
        final String httpCode = returnMap.getOrDefault("httpcode", "5000");
        if (httpCode.equals("200")) {
            final String respStr = returnMap.getOrDefault("content", "");
            log.warn("%%查询接口应答内容：[{}]", respStr);
            if (StringUtils.isEmpty(respStr)) {
                result.setCode(ResultCode.FAIL.getValue()).setMessage("无应答消息");
                return result;
            }
            result.setCode(ResultCode.SUCCESS.getValue()).setDate(JSONObject.parseObject(respStr, ResponseInfoEntity.class));
        } else {
            result.setCode(ResultCode.FAIL.getValue()).setMessage(returnMap.getOrDefault("content", ""));
        }
        return result;
    }

    public Result<ResponseInfoEntity> resultQueryPushToTeleSales(String requestId) throws Exception {
        return resultQueryPushToTeleSales(requestId, 0);
    }

    @RetryMethod(retryNowNum = 1)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<String> pushHaierCollidingData(String mobileDigest ,String apiCode) {
        String aesKey = RandomStringUtils.randomAlphabetic(16);
        String haierPublicKey = marketingCommonConfig.getHaierApiPublicKey();
        HaierCollidingDataDTO dto = buildHaierCollidingDataDTO(aesKey, mobileDigest ,apiCode);
        Map<String, String> retMap = Maps.newHashMap();
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(dto);
            String encryptData = AESUtil.encrypt(json, aesKey);
            String encryptKey = RsaUtil.encrypt(aesKey.getBytes(StandardCharsets.UTF_8), haierPublicKey);
            retMap.put("key", encryptKey);
            retMap.put("data", encryptData);
        } catch (Exception e) {
            log.error("海尔撞库接口拼装参数AES加密异常", e);
        }
        HashMap<String, String> resMap = Maps.newHashMap();
        HashMap<String, Object> mock = marketingCommonConfig.getHaierCollidingDataMock();
        if (mock.get("switch") == Boolean.TRUE) {
            resMap.put("content", mock.get("content").toString());
            resMap.put("httpcode", mock.get("httpcode").toString());
        } else {
            resMap = httpProxyClient.sendByCodeWithLog(retMap, collidingUrl, collidingIsProxy, MediaType.APPLICATION_JSON_UTF8_VALUE,
                JSON.toJSONString(dto), true, false);
        }
        String content = resMap.get("content");
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isEmpty(content)) {
            log.error("海尔撞库接口请求返回 httpcode 非200异常。立即重试1次，mobileDigest:{}", mobileDigest);
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setDate(content);
        }
        JSONObject resultJson = JSONObject.parseObject(content);
        String retCode = resultJson.getString("retCode");
        if ("00000".equals(retCode)) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(content);
        } else {
            log.error("海尔撞库接口请求返回 code 非00000异常。立即重试1次，mobileDigest:{},返回报文:{}", mobileDigest,content);
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setDate(content);
        }
    }

    private HaierCollidingDataDTO buildHaierCollidingDataDTO(String aesKey, String mobileDigest,String apiCode) {
        HaierCollidingDataDTO dto = new HaierCollidingDataDTO();
        try {
            String pid = marketingCommonConfig.getHaierCollidingDataConfig().get("pid");
            String channelNo = marketingCommonConfig.getHaierCollidingDataConfig().get("channelNo");
            String utmNo = marketingCommonConfig.getHaierCollidingDataConfig().get("utmNo");
            String encryptAlg = marketingCommonConfig.getHaierCollidingDataConfig().get("encryptAlg");
            long timestamp = System.currentTimeMillis();
            String currentDate = DateUtil.formatDate(new Date());
            String requestId = apiCode + "_" + currentDate + "_" + RandomStringUtils.randomAlphabetic(16) + UUID.randomUUID();
            // 拼接公共参数
            String sb = "pid=" + pid + "&" + "requestId=" + requestId + "&" + "timestamp=" + timestamp + aesKey;
            String encryptSign = RsaUtil.sign(sb, collidingRsaPrivateKey);
            // 构造业务参数
            Map<String, Object> data = new HashMap<>();
            data.put("mobileDigest", mobileDigest);
            data.put("channelNo", channelNo);
            data.put("utmNo", utmNo);
            data.put("encryptAlg", encryptAlg);

            dto.setPid(pid);
            dto.setTimestamp(timestamp);
            dto.setRequestId(requestId);
            dto.setSign(encryptSign);
            dto.setData(data);
        } catch (Exception e) {
            log.error("海尔撞库接口拼装参数异常", e);
        }
        return dto;
    }
}
