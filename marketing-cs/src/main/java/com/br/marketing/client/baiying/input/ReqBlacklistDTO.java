package com.br.marketing.client.baiying.input;

import lombok.Data;

import java.util.List;

/**
 * @ClassName ReqBlacklistDTO
 * @Author kongbx
 * @Date 2024/5/27 13:49
 */
@Data
public class ReqBlacklistDTO {
    /**
     * 请求固定值 请求方法标识 blackData
     */
    private String method;
    /**
     * apiCode 的黑名单
     */
    private String apiCode;
    /**
     * 黑名单列表
     */
    private List<BlacklistDataDTO> data;
}
