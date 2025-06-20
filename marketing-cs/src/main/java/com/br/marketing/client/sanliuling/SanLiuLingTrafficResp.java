package com.br.marketing.client.sanliuling;

import lombok.Data;

import java.util.List;

/**
 * @ClassName SanLiuLingTrafficReq
 * @Author kongbx
 * @Date 2025/6/20 16:32
 */
@Data
public class SanLiuLingTrafficResp {

    private String code;
    private String msg;
    private String flag;
    private SanLiuLingResult data;

    @Data
    public static class SanLiuLingResult {
        private List<String> mobile_md5;
        private String request_id;
    }
}
