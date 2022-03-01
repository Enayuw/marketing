package com.br.marketing.client.yixinapi.black;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.yixinapi.black.input.BlackListAbstract;
import com.br.marketing.client.yixinapi.black.input.PushBlackListRequest;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.InterfaceLog;
import com.br.marketing.mapper.InterfaceLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * 黑名单数据推送 客户端
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/1 11:10
 */
@Service
@Slf4j
public class BlackApiServiceClient {
    @Resource
    private HttpProxyClient httpProxyClient;

    @Value("${api.dass.aesKey:00}")
    private String ascKey;

    @Value("${api.dass.SecretKey:00}")
    private String secretKey;

    @Value("${api.dass.postBlackList:call/postBlackList}")
    private String postHermesUserDataUrl;

    @Resource
    private InterfaceLogMapper interfaceLogMapper;


    /**
     * 2022/3/1 15:00
     * 黑名单数据推送
     */
    @SuppressWarnings("all")
    public Result<PushBlackListResponse> postBlackList(List<? extends BlackListAbstract> list) {
        PushBlackListRequest pushBlackListRequest = new PushBlackListRequest(list, secretKey, ascKey);
        String jsonData = JSON.toJSONString(pushBlackListRequest);
        Result<PushBlackListResponse> result = new Result<>();
        InterfaceLog interfaceLog = new InterfaceLog();
        interfaceLog.setRequestId(UUID.randomUUID().toString());
        interfaceLog.setRequestParam(jsonData);
        interfaceLog.setUrl(postHermesUserDataUrl);
        interfaceLog.setCreateTime(new Date());
        long start = System.currentTimeMillis();
        try {
            log.warn("#postBlackList#Request:\n{}", jsonData);
            HashMap<String, String> hashMap = httpProxyClient.sendByCode(jsonData, postHermesUserDataUrl, false);
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
                } else {
                    result.setCode(ResultCode.FAIL.getValue());
                }
            } else {
                result.setCode(ResultCode.FAIL.getValue());
            }
        } catch (Exception ex) {
            interfaceLog.setResult(ex.getMessage());
            log.error(ex.getMessage(), ex);
            result.setCode(ResultCode.FAIL.getValue());
        } finally {
            long end = System.currentTimeMillis();
            interfaceLog.setExpire(String.valueOf(end - start));
            log.warn("postBlackList耗时：{}ms", end);
        }
        interfaceLogMapper.insertSelective(interfaceLog);
        return result;
    }

}
