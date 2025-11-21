package com.br.marketing.client.taikang.util;

import lombok.Data;

/**
 * @author itw_xuzw01
 * @date 2025/7/25 10:00
 */
@Data
public class ChannelResponse {

    private String key;
    private String content;

    public static ChannelResponse of(String key, String content) {
        ChannelResponse response = new ChannelResponse();
        response.setKey(key);
        response.setContent(content);
        return response;
    }
}
