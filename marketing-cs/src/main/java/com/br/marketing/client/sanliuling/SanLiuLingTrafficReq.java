package com.br.marketing.client.sanliuling;

import lombok.Data;

import java.util.List;

/**
 * @ClassName SanLiuLingTrafficReq
 * @Author kongbx
 * @Date 2025/6/20 16:32
 */
@Data
public class SanLiuLingTrafficReq {

    private List<String> mobileMd5;

    private String channel;
}
