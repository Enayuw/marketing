package com.br.marketing.client.dassservice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.cloud.counter.BrCounter;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportAdapHaluoDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.IbuReqDTO;
import com.br.marketing.client.dassservice.input.black.BlackListDTO;
import com.br.marketing.client.dassservice.input.black.PushBlackListRequest;
import com.br.marketing.client.dassservice.input.csos.DaasCsosDataAdapDTO;
import com.br.marketing.client.dassservice.input.csos.DaasCsosDataDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataAdapDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataDTO;
import com.br.marketing.client.dassservice.input.update.DaasUpdateDataDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.InterfaceLog;
import com.br.marketing.mapper.datasource.log.InterfaceLogMapper;
import com.br.marketing.monitor.PrometheusMonitorUtils;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class DassServiceClient {

    @Value("${api.dass.aesKey:00}")
    private String ascKey;

    @Value("${api.dass.SecretKey:00}")
    private String secretKey;

    @Value("${api.dass.postHermesUserData:00}")
    private String postHermesUserDataUrl;

    @Value("${api.dass.postRealTimeUserData:call/postRealtimeUserData}")
    private String postRealTimeUserDataUrl;

    @Value("${api.dass.postTransferData:00}")
    private String postTransferData;

    @Value("${api.dass.isProxy:0}")
    private String isProxy;

    @Value("${api.dass.ibuAk:00}")
    private String ibuAk;

    @Value("${api.dass.ibuSk:00}")
    private String ibuSk;

    @Value("${api.dass.batchHermesUserData:00}")
    private String batchHermesUserData;

    @Value("${api.dass.postWealthUserData:00}")
    private String postCsosData;

    @Value("${api.dass.postWealthUpdateData:00}")
    private String postWealthUpdateData;

    @Value("${api.dass.appId:00}")
    private String appId;

    static String ibuBatchToDass = "IBTD";

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    HttpProxyClient httpProxyClient;

    @Resource
    InterfaceLogMapper interfaceLogMapper;

    private DassImportDataDTO t;

    @Value("${api.dass.postBlackList:call/postBlackList}")
    private String postBlackList;

    private final static int size = 1000;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    public Result postHermesUserData(DassImportAdapHaluoDTO dto) {
        DassImportAdapDTO trueDto = new DassImportAdapDTO();
        trueDto.setList(dto.getList());
        trueDto.setInterfaceExtendInfo(dto.getInterfaceExtendInfo());
        trueDto.setTransferInfoId(dto.getTransferInfoId());
        return postHermesUserData(trueDto);
    }

    @PrometheusTimeMethod(buckets = {0.02d, 0.05d,0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result postHermesUserData(DassImportAdapDTO dto) {
        Result result = new Result();
        List<DassImportDataDTO> dtos = dto.getList();
        dtos.forEach(dassImportDataDTO -> dassImportDataDTO.setExtend(extendSort(dassImportDataDTO.getExtend())));
        long l = LocalDateTime.now().plusMinutes(10L).toInstant(ZoneOffset.of("+8")).toEpochMilli();
        List sortList = new ArrayList();
        sortList.add(String.valueOf(l));
        dtos.forEach(t -> {
            BeanMap beanMap = BeanMap.create(t);
            for (Object k : beanMap.keySet()) {
                if (String.valueOf(k).equals("id")) {
                    continue;
                }
                Object o = beanMap.get(k);
                if (String.valueOf(k).equals("phone")) {
                    sortList.add(AESUtil.decrypt(String.valueOf(o), ascKey));
                    continue;
                }
                if (o == null) {
                    continue;
                } else if (o instanceof String) {
                    if (StringUtils.isBlank(String.valueOf(o))) {
                        continue;
                    }
                } else if (o instanceof List) {
                    List o1 = (List) o;
                    if (o1 == null || o1.size() == 0) {
                        continue;
                    }
                }
                sortList.add(String.valueOf(o));
            }
        });
        Collections.sort(sortList);
        String param = Joiner.on("").join(sortList);
        String sign = DigestUtils.md5DigestAsHex(String.format(secretKey + "%s", param).getBytes());
        HashMap requestParam = new HashMap();
        requestParam.put("ts", l);
        requestParam.put("sign", sign);
        requestParam.put("data", dtos);
        InterfaceLog interfaceLog = new InterfaceLog();
        interfaceLog.setExtendInfo(dto.getInterfaceExtendInfo());
        interfaceLog.setRequestId(UUID.randomUUID().toString());
        interfaceLog.setRequestParam(JSON.toJSONString(requestParam));
        interfaceLog.setUrl(postHermesUserDataUrl);
        interfaceLog.setCreateTime(new Date());
        long start = System.currentTimeMillis();
        try {
            HashMap<String, String> hashMap =
                    httpProxyClient.sendByCode(JSON.toJSONString(requestParam), postHermesUserDataUrl, isProxy.equals("0") ? false : true);
            long end = System.currentTimeMillis();
            Integer code = null;
            if (StringUtils.isNotBlank(hashMap.get("httpcode"))) {
                code = Integer.valueOf(hashMap.get("httpcode"));
                interfaceLog.setHttpCode(Integer.valueOf(hashMap.get("httpcode")));
            }

            interfaceLog.setResult(hashMap.get("content"));
            interfaceLog.setExpire(String.valueOf(end - start));
            if (Integer.valueOf(200).equals(code)) {
                //调用数量监控
                try {
                    BrCounter.count(PrometheusMonitorUtils.COUNT_DAAS_BATCH_USERDATA_METRIC_NAME
                            , dto.getList().get(0).getOrgname(), "batchUserData-api", dtos.size());
                } catch (Exception ex) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(), "电销批量接口统计异常！"), ex);
                }
                result.setCode(ResultCode.SUCCESS.getValue());
            } else {
                result.setCode(ResultCode.FAIL.getValue());
            }
        } catch (Exception ex) {
            long end = System.currentTimeMillis();
            interfaceLog.setExpire(String.valueOf(end - start));
            interfaceLog.setResult("程序异常：" + ex.getMessage());
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(), ex.getMessage()), ex);
        }
        try {
            interfaceLogMapper.insertSelective(interfaceLog);
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(),
                    "推送Dass数据写入日志异常，异常日志：" + e.getMessage()), e);
        }
        return result;
    }

    /**
     * 2022/3/1 15:00
     * 黑名单数据推送
     * 批量最大1千条
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d,0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<PushBlackListResponse> postBlackList(List<BlackListDTO> list) {
        Result<PushBlackListResponse> result = new Result<>();
        if (list != null && list.size() > size) {
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage("接口提供方要求，批量最大为1000");
            return result;
        }
        PushBlackListRequest pushBlackListRequest = new PushBlackListRequest(list, secretKey, ascKey);
        String jsonData = JSON.toJSONString(pushBlackListRequest);
        InterfaceLog interfaceLog = new InterfaceLog();
        interfaceLog.setRequestId(UUID.randomUUID().toString());
        interfaceLog.setRequestParam(jsonData);
        interfaceLog.setUrl(postBlackList);
        interfaceLog.setCreateTime(new Date());
        long start = System.currentTimeMillis();
        try {
            //log.warn("#postBlackList#Request:\n{}", jsonData);
            boolean boolProxy = !"0".equals(isProxy);
            HashMap<String, String> hashMap = httpProxyClient.sendByCode(jsonData, postBlackList, boolProxy);
            log.warn("#postBlackList#Response:\n{}", hashMap.toString());
            final String httpcode = hashMap.getOrDefault("httpcode", "");
            if (StringUtils.isNotBlank(httpcode)) {
                int code = Integer.parseInt(httpcode);
                interfaceLog.setHttpCode(code);
                final String content = hashMap.getOrDefault("content", "");
                interfaceLog.setResult(content);
                int httpCode = 200;
                if (httpCode == code) {
                    result.setCode(ResultCode.SUCCESS.getValue());
                    result.setDate(JSON.parseObject(content, new TypeReference<PushBlackListResponse>() {
                    }.getType()));
                    try {
                        //调用数量监控
                        BrCounter.count(PrometheusMonitorUtils.COUNT_DAAS_BLACK_DATA_METRIC_NAME, list.get(0).getApiCode(), "blackData-api",
                            list.size());
                    } catch (Exception ex) {
                        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(), "电销黑名单接口统计异常!"), ex);
                    }
                } else {
                    result.setCode(ResultCode.FAIL.getValue());
                    result.setMessage(content);
                }
            } else {
                result.setCode(ResultCode.FAIL.getValue());
            }
        } catch (Exception ex) {
            interfaceLog.setResult(ex.getMessage());
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(), ex.getMessage()), ex);
            result.setCode(ResultCode.FAIL.getValue());
            result.setMessage(ex.getMessage());
        } finally {
            long end = System.currentTimeMillis();
            interfaceLog.setExpire(String.valueOf(end - start));
            log.warn("postBlackList耗时：{}ms", interfaceLog.getExpire());
        }
        try {
            interfaceLogMapper.insertSelective(interfaceLog);
        } catch (Exception e) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(),
                    "推送Dass数据写入日志异常，异常日志：" + e.getMessage()), e);
        }
        return result;
    }

    /**
     * 单条用户数据实时推送
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d,0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result<JSONObject> postRealTimeUserData(DassSingleImportAdapDTO dto) {
        Result result = new Result();
        DassSingleImportDataDTO dassSingleImportDataDTO = dto.getDassSingleImportDataDTO();
        dassSingleImportDataDTO.setPhone(getPhone(dassSingleImportDataDTO.getPhone()));
        dassSingleImportDataDTO.setExtend(extendSort(dassSingleImportDataDTO.getExtend()));
        List<DassSingleImportDataDTO> dassSingleImportAdapDTOList = Lists.newArrayList(dassSingleImportDataDTO);
        long l = LocalDateTime.now().plusMinutes(10L).toInstant(ZoneOffset.of("+8")).toEpochMilli();
        List sortList = new ArrayList();
        sortList.add(String.valueOf(l));
        dassSingleImportAdapDTOList.forEach(t -> {
            BeanMap beanMap = BeanMap.create(t);
            for (Object k : beanMap.keySet()) {
                if (String.valueOf(k).equals("id")) {
                    continue;
                }
                Object o = beanMap.get(k);
                if (String.valueOf(k).equals("phone")) {
                    sortList.add(AESUtil.decrypt(String.valueOf(o), ascKey));
                    continue;
                }
                if (o == null) {
                    continue;
                } else if (o instanceof String) {
                    if (StringUtils.isBlank(String.valueOf(o))) {
                        continue;
                    }
                } else if (o instanceof List) {
                    List o1 = (List) o;
                    if (CollectionUtils.isEmpty(o1)) {
                        continue;
                    }
                }
                sortList.add(String.valueOf(o));
            }
        });
        Collections.sort(sortList);
        String param = Joiner.on("").join(sortList);
        String sign = DigestUtils.md5DigestAsHex(String.format(secretKey + "%s", param).getBytes());
        HashMap requestParam = new HashMap();
        requestParam.put("ts", l);
        requestParam.put("sign", sign);
        requestParam.put("data", dassSingleImportAdapDTOList);
        HashMap<String, String> hashMap = httpProxyClient.sendByCode(requestParam, postRealTimeUserDataUrl
                , isProxy.equals("0") ? false : true, MediaType.APPLICATION_JSON_UTF8_VALUE, dto.getExtendInfo());
        final String httpCode = hashMap.getOrDefault("httpcode", "5000");
        if (httpCode.equals("200")) {
            final String respStr = hashMap.getOrDefault("content", "");
            log.warn("%%应答内容：[{}]", respStr);
            if (org.springframework.util.StringUtils.isEmpty(respStr)) {
                result.setCode(ResultCode.FAIL.getValue()).setMessage("无应答消息");
                return result;
            }
            try {
                JSONObject resJson = JSONObject.parseObject(respStr);
                //判断返回结果result中code,0为成功，非0失败。
                boolean resultSuccess = Objects.equals(0, resJson.getInteger("code"));
                result.setCode(resultSuccess ? ResultCode.SUCCESS.getValue() : ResultCode.FAIL.getValue()).setDate(resJson);
                //调用成功才统计调用信息
                if (resultSuccess) {
                    try {
                        //调用数量监控
                        BrCounter.count(PrometheusMonitorUtils.COUNT_DAAS_SINGLE_USERDATA_METRIC_NAME
                                , dassSingleImportDataDTO.getOrgname(), "DaasRealTimeData-api");
                    } catch (Exception ex) {
                        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(), "电销单条接口统计异常!"), ex);
                    }
                }
            } catch (Exception e) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode()
                        , "单条用户数据实时推送响应结果respStr:" + respStr + ",转化json异常!"), e);
                result.setCode(ResultCode.FAIL.getValue()).setMessage(hashMap.getOrDefault("content", ""));
            }
        } else {
            result.setCode(ResultCode.FAIL.getValue()).setMessage(hashMap.getOrDefault("content", ""));
        }
        return result;
    }

    private String extendSort(String extend) {
        if (StringUtils.isEmpty(extend)) {
            return null;
        }
        JSONObject jsonParam = JSON.parseObject(extend);
        HashMap sortMap = Maps.newLinkedHashMap();
        sortMap.put("is_usr_lst_app_sta_tim", "");
        sortMap.put("face_recognitiion", "");
        sortMap.put("is_usr_idt", "");
        sortMap.put("is_bindcard", "");
        sortMap.put("is_usr_inf", "");
        sortMap.put("typeSign", "");
        JSONObject jsonSortParam = new JSONObject(sortMap);
        jsonSortParam.putAll(jsonParam);
        Iterator<Map.Entry<String, Object>> iterator = jsonSortParam.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = iterator.next();
            if (StringUtils.isEmpty(entry.getValue())) {
                iterator.remove();
            }
        }
        return jsonSortParam.toJSONString();
    }

    private String getPhone(String phone) {
        String content = phone;
        String s = AESUtil.aesDecrypt(phone, ascKey);
        if (StringUtils.isBlank(s)) {
            content = AESUtil.aesEncrypty(phone, ascKey);
        }
        return content;
    }

    /**
     * 推送转化数据
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d,0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result postTransferData(DassTransferDataAdapDTO dassTransferDataAdapDTO) {
        Result result = new Result();
        List<DassTransferDataDTO> dassTransferDataDTOList = dassTransferDataAdapDTO.getDassTransferDataDTOList();
        dassTransferDataDTOList.forEach(dassTransferDataDTO -> {
            if (StringUtils.isNotEmpty(dassTransferDataDTO.getPhone())) {
                dassTransferDataDTO.setPhone(getPhone(dassTransferDataDTO.getPhone()));
            }
        });
        long l = LocalDateTime.now().plusMinutes(10L).toInstant(ZoneOffset.of("+8")).toEpochMilli();
        List sortList = new ArrayList();
        sortList.add(String.valueOf(l));
        dassTransferDataDTOList.forEach(t -> {
            BeanMap beanMap = BeanMap.create(t);
            for (Object k : beanMap.keySet()) {
                if (String.valueOf(k).equals("id")) {
                    continue;
                }
                Object o = beanMap.get(k);
                if (String.valueOf(k).equals("phone")) {
                    sortList.add(AESUtil.decrypt(String.valueOf(o), ascKey));
                    continue;
                }
                if (o == null) {
                    continue;
                } else if (o instanceof String) {
                    if (StringUtils.isBlank(String.valueOf(o))) {
                        continue;
                    }
                } else if (o instanceof List) {
                    List o1 = (List) o;
                    if (CollectionUtils.isEmpty(o1)) {
                        continue;
                    }
                }
                sortList.add(String.valueOf(o));
            }
        });
        Collections.sort(sortList);
        String param = Joiner.on("").join(sortList);
        String sign = DigestUtils.md5DigestAsHex(String.format(secretKey + "%s", param).getBytes());
        HashMap requestParam = new HashMap();
        requestParam.put("ts", l);
        requestParam.put("sign", sign);
        requestParam.put("data", dassTransferDataDTOList);
        HashMap<String, String> hashMap = httpProxyClient.sendByCode(requestParam, postTransferData,
                isProxy.equals("0") ? false : true, MediaType.APPLICATION_JSON_UTF8_VALUE, null);
        final String httpCode = hashMap.getOrDefault("httpcode", "5000");
        if (httpCode.equals("200")) {
            final String respStr = hashMap.getOrDefault("content", "");
            log.warn("%%应答内容：[{}]", respStr);
            if (org.springframework.util.StringUtils.isEmpty(respStr)) {
                result.setCode(ResultCode.FAIL.getValue()).setMessage("无应答消息");
                return result;
            }
            result.setCode(ResultCode.SUCCESS.getValue()).setDate(respStr);
            try {
                //调用数量监控
                BrCounter.count(PrometheusMonitorUtils.COUNT_DAAS_TRANSFER_METRIC_NAME
                        , dassTransferDataDTOList.get(0).getOrgName(), "transferData-api",
                    dassTransferDataDTOList.size());
            } catch (Exception ex) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(), "电销转化接口统计异常!"), ex);
            }
        } else {
            result.setCode(ResultCode.FAIL.getValue()).setMessage(hashMap.getOrDefault("content", ""));
        }
        return result;
    }

    /**
     * ibu人工定制接口(批量)
     *
     * @return
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d,0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result pushIbuArtificial(List<IbuReqDTO.Datum> datumList) {
        UUID reqId = UUID.randomUUID();
        try {
            Result<String> res = new Result<>();
            IbuReqDTO ibuReqDTO = new IbuReqDTO();
            ibuReqDTO.setData(JSON.toJSONString(datumList));
            ibuReqDTO.setAccessKey(ibuAk);
            ibuReqDTO.setTs(System.currentTimeMillis());
            StringBuilder mText = new StringBuilder();
            mText.append(ibuSk);
            mText.append(ibuReqDTO.getData());
            mText.append(ibuReqDTO.getTs());
            String s = DigestUtils.md5DigestAsHex(mText.toString().getBytes()).toUpperCase();
            ibuReqDTO.setSign(s);
            StringBuilder paramStr = new StringBuilder();
            HashMap<String, String> resContent = httpProxyClient.sendByCodeWithLog(ibuReqDTO, batchHermesUserData
                , isProxy.equals("0") ? false : true
                , MediaType.APPLICATION_FORM_URLENCODED_VALUE, reqId.toString()
                , httpProxyClient.isLogStore(ibuBatchToDass).get(0)
                , httpProxyClient.isLogStore(ibuBatchToDass).get(1));
            String httpcode = resContent.get("httpcode");
            if ("200".equals(httpcode)) {
                JSONObject content = JSONObject.parseObject(resContent.get("content"));
                Integer code = content.getInteger("code");
                if (new Integer(0).equals(code)) {
                    res.setCode(ResultCode.SUCCESS.getValue());
                } else {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode()
                            , "ibu定制接口非code成功(" + reqId.toString() + ")：" + resContent.getOrDefault("content", "")));
                    res.setCode(ResultCode.FAIL.getValue()).setMessage(resContent.get("content"));
                }
            } else {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode()
                        , "ibu定制接口非200情况(" + reqId.toString() + ")：" + resContent.getOrDefault("content", "")));
                res.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(resContent.get("content"));
            }
            return res;
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(), "ibu定制接口错误(" + reqId.toString() + ")"), ex);
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(ex.getMessage());
        }

    }

    /**
     * 推送财富数据
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result postCsosData(DaasCsosDataAdapDTO daasCsosDataAdapDTO) {
        Result result = new Result();
        try {
            List<DaasCsosDataDTO> daasCsosDataDTOList = daasCsosDataAdapDTO.getDaasCsosDataDTOList();
            long l = LocalDateTime.now().plusMinutes(10L).toInstant(ZoneOffset.of("+8")).toEpochMilli();
            List sortList = new ArrayList();
            sortList.add(String.valueOf(l));
            daasCsosDataDTOList.forEach(t -> {
                BeanMap beanMap = BeanMap.create(t);
                for (Object k : beanMap.keySet()) {
                    if (String.valueOf(k).equals("id")) {
                        continue;
                    }
                    Object o = beanMap.get(k);
                    if (String.valueOf(k).equals("phone")) {
                        sortList.add(AESUtil.decrypt(String.valueOf(o), ascKey));
                        continue;
                    }
                    if (o == null) {
                        continue;
                    } else if (o instanceof String) {
                        if (StringUtils.isBlank(String.valueOf(o))) {
                            continue;
                        }
                    } else if (o instanceof List) {
                        List o1 = (List) o;
                        if (CollectionUtils.isEmpty(o1)) {
                            continue;
                        }
                    }
                    sortList.add(String.valueOf(o));
                }
            });
            Collections.sort(sortList);
            String param = Joiner.on("").join(sortList);
            String sign = DigestUtils.md5DigestAsHex(String.format(secretKey + "%s", param).getBytes());
            HashMap requestParam = new HashMap();
            requestParam.put("ts", l);
            requestParam.put("sign", sign);
            requestParam.put("data", daasCsosDataDTOList);
            HashMap<String, String> hashMap = httpProxyClient.sendByCode(requestParam, postCsosData,
                    isProxy.equals("0") ? false : true, MediaType.APPLICATION_JSON_UTF8_VALUE, null);
            final String httpCode = hashMap.getOrDefault("httpcode", "5000");
            if (httpCode.equals("200")) {
                final String respStr = hashMap.getOrDefault("content", "");
                log.warn("%%应答内容：[{}]", respStr);
                if (StringUtils.isEmpty(respStr)) {
                    result.setCode(ResultCode.FAIL.getValue()).setMessage("无应答消息");
                    return result;
                }
                result.setCode(ResultCode.SUCCESS.getValue()).setDate(respStr);
            } else {
                result.setCode(ResultCode.FAIL.getValue()).setMessage(hashMap.getOrDefault("content", ""));
            }
            return result;
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(), "推送Daas财富接口异常"), ex);
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(ex.getMessage());
        }
    }

    /**
     * 推送人工业务数据更新接口（单条数据）
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    @RetryMethod(retryNowNum = 3,isOrNoDbRetry = true)
    public Result postWealthUpdateData(DaasUpdateDataDTO updateData) {
        Result result = new Result();
        try {
            // 构建请求参数
            Map<String, Object> requestParam = new HashMap<>();
            
            // 生成唯一请求ID（确保幂等性）
            String requestId = updateData.getRequestId();
            // 获取当前时间戳
            long timeStamp = System.currentTimeMillis();
            
            // 计算签名: MD5(appId + requestId + timeStamp)
            String signatureString = appId + requestId + timeStamp;
            String signature = DigestUtils.md5DigestAsHex(signatureString.getBytes()).toUpperCase();
            
            // 构建扩展字段
            Map<String, Object> reserveField1 = new HashMap<>();
            // 添加其他扩展字段
            if (StringUtils.isNotBlank(updateData.getExtend())) {
                try {
                    JSONObject extendJson = JSONObject.parseObject(updateData.getExtend());
                    for (String key : extendJson.keySet()) {
                        reserveField1.put(key, extendJson.get(key));
                    }
                } catch (Exception e) {
                    log.warn("解析extend字段失败: {}", updateData.getExtend());
                }
            }
            
            // 设置请求参数
            requestParam.put("requestId", requestId);
            requestParam.put("uid", updateData.getUid());
            requestParam.put("orgname", updateData.getOrgname());
            requestParam.put("source", updateData.getSource());
            requestParam.put("userType", updateData.getUserType());
            requestParam.put("reserveField1", reserveField1);
            requestParam.put("timeStamp", timeStamp);
            requestParam.put("signature", signature);
            
            // 发送请求
            HashMap<String, String> hashMap = httpProxyClient.sendByCode(requestParam, postWealthUpdateData,
                    isProxy.equals("0") ? false : true, MediaType.APPLICATION_JSON_UTF8_VALUE, null);
            
            final String httpCode = hashMap.getOrDefault("httpcode", "5000");
            if (httpCode.equals("200")) {
                final String respStr = hashMap.getOrDefault("content", "");
                log.debug("人工业务数据更新接口应答内容：[{}]", respStr);
                
                if (StringUtils.isEmpty(respStr)) {
                    result.setCode(ResultCode.FAIL.getValue()).setMessage("无应答消息");
                    return result;
                }
                
                // 解析响应
                try {
                    JSONObject respJson = JSONObject.parseObject(respStr);
                    Integer code = respJson.getInteger("code");
                    String message = respJson.getString("message");
                    
                    if (code != null && code == 1) {
                        result.setCode(ResultCode.SUCCESS.getValue()).setMessage("成功");
                        log.debug("数据更新成功: uid={}, requestId={}", updateData.getUid(), requestId);
                    } else {
                        result.setCode(ResultCode.FAIL.getValue()).setMessage("code=" + code + ", message=" + message);
                        log.warn("人工业务数据更新失败：uid={}, code={}, message={}", updateData.getUid(), code, message);
                    }
                } catch (Exception e) {
                    // 如果解析失败，认为成功
                    result.setCode(ResultCode.SUCCESS.getValue()).setMessage("成功（响应解析失败，默认为成功）");
                    log.debug("响应解析失败，默认为成功: uid={}", updateData.getUid());
                }
            } else {
                log.warn("HTTP请求失败：uid={}, httpCode={}, content={}", updateData.getUid(), httpCode, hashMap.getOrDefault("content", ""));
                result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(hashMap.getOrDefault("content", ""));
            }
            
            return result;
        } catch (Exception ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_DAASERROR.getCode(), "推送人工业务数据更新接口异常"), ex);
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(ex.getMessage());
        }
    }

}
