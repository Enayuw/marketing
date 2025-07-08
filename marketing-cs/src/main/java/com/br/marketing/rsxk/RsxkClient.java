package com.br.marketing.rsxk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.rsxk.CallStatusDTO;
import com.br.marketing.dto.rsxk.Resp;
import com.br.marketing.entity.MarketingSyncUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class RsxkClient {

    @Value(value = "${api.rsxk.queryCallStatus:'https://mdp.shurongdai.cn/mdp-thinker-facade/api/queryCallStatus'}")
    private String queryCallStatusUrl;

    @Value("${api.rsxk.isProxy:true}")
    private Boolean isProxy;

    @Resource
    private HttpProxyClient httpProxyClient;

    public Result<CallStatusDTO> queryCallStatus(MarketingSyncUser syncUser) {
        try{
            JSONObject parseObject = JSON.parseObject(syncUser.getReserveField1());
            int planId = Integer.parseInt(parseObject.getOrDefault("planId", "0").toString());
            Map<String, Object> data = new HashMap<>();
            data.put("uid", syncUser.getCustNum());
            data.put("planId", planId);
            data.put("bizSource", "01");
            data.put("operateType", 2);
            data.put("sign", DigestUtils.md5Hex(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
            String reqUrl = String.format("%s?%s", queryCallStatusUrl, param(data));
            HashMap<String, String> resMap = httpProxyClient.getWithLog(reqUrl, isProxy, null);
            if (!"200".equals(resMap.get("httpcode"))
                    || StringUtils.isBlank(resMap.get("content"))) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.RSXK_INTERFACE.getCode(),
                        String.format("请求参数:%s,返回:%s", JSON.toJSONString(data), JSON.toJSONString(resMap))
                        , "榕树新客推送Daas，调用queryCallStatus接口异常"));
                return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(JSON.toJSONString(resMap));
            }
            Resp<CallStatusDTO> resp = JSON.parseObject(resMap.get("content"), new TypeReference<Resp<CallStatusDTO>>(){}.getType());
            if(Resp.Code.SUCCESS.getCode() == (resp.getCode())) {
                return new Result<>()
                        .setCode(ResultCode.SUCCESS.getValue())
                        .setDate(resp.getData());
            }
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.RSXK_SERVICEERROR.getCode(),
                    "榕树新客推送Daas，前置过滤不能推送，msg=" + resp.getMsg()));
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.RSXK_SERVICEERROR.getCode(), e.getMessage()
                    , "榕树新客推送Daas，前置过滤异常"));
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

    private static String param(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        for (String key : data.keySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(key)
                    .append("=")
                    .append(data.get(key));
        }
        return sb.toString();
    }
}
