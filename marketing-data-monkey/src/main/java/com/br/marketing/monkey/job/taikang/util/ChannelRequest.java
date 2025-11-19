package com.br.marketing.monkey.job.taikang.util;

import org.apache.commons.lang3.StringUtils;
import lombok.Data;

/**
 * 渠道数据请求参数
 *
 * @author itw_xuzw01
 * @date 2025/7/24 15:43
 */
@Data
public class ChannelRequest {

    /**
     * 密钥Key
     */
    private String key;
    /**
     * 请求内容
     */
    private String content;
    /**
     * 时间戳
     */
    private Long ts;
    /**
     * 签名
     */
    private String sign;

    public static ChannelRequest of(String key, String content, Long ts, String sign){
        ChannelRequest request = new ChannelRequest();
        request.setKey(key);
        request.setTs(ts);
        request.setContent(content);
        request.setSign(sign);
        return request;
    }

    public boolean validate(){
        return StringUtils.isNotBlank(key) &&
                ts != null &&
                StringUtils.isNotBlank(content) &&
                StringUtils.isNotBlank(sign);
    }
}
