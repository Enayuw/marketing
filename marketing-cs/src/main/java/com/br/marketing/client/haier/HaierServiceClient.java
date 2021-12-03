package com.br.marketing.client.haier;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.haier.output.PushDTO;
import com.br.marketing.client.haier.output.Response2Entity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

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

    /**
     * 推送数据客户端
     *
     * @param list      推送的数据
     * @param function  实现封装的函数
     * @param requestId 批次
     * @param type      待转化状态 码值：促注册 1 促申额 2 促首贷 3
     * @return Response2Entity 响应信息
     * @throws Exception 大概率序列化异常，具体请自行打印异常信息
     * @author Guo Zeqiang
     * @dateTime 2021/12/3 16:43
     */
    public <T> Response2Entity pushToTeleSales(List<T> list, Function<List<T>, Set<PushDTO.DataItems>> function
            , String requestId, String type) throws Exception {
        Assert.notNull(list, "\"List\" is not null");
        Assert.notNull(requestId, "\"requestId\" is not null");
        Assert.notNull(type, "\"requestId\" is not null");
        log.warn("##地址：{}；apicode：{}；apikey：{}", url, apiCode, apiKey);
        final Set<PushDTO.DataItems> dataItemsSet = function.apply(list);
        PushDTO pushDTO = new PushDTO(apiCode, dataItemsSet, itemsSet -> new PushDTO.FormData(requestId, type, itemsSet), apiKey);
        log.warn("&&发送内容：[{}]", pushDTO);
        final HashMap<String, String> stringStringHashMap = httpProxyClient.sendByCode(pushDTO, url, true, MediaType.APPLICATION_JSON_UTF8_VALUE, "");
        final String httpCode = stringStringHashMap.getOrDefault("httpcode", "5000");
        if (httpCode.equals("200")) {
            final String respStr = stringStringHashMap.getOrDefault("content", "");
            log.warn("%%应答内容：[{}]", respStr);
            if (StringUtils.isEmpty(respStr)) {
                return null;
            }
            return JSONObject.parseObject(respStr, Response2Entity.class);
        }
        return null;
    }

}
