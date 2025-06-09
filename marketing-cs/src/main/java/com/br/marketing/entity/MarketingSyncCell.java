package com.br.marketing.entity;

import lombok.Data;

/**
 * @author peng.kang
 * @date 2025/5/25 13:54
 */
@Data
public class MarketingSyncCell {
    private Long id;
    /**
     * 电话
     */
    private String cell;

    /**
     * 手机号md5
     */
    private String cellMd5;

    /**
     * 手机号sha256
     */
    private String cellSha256;
    /**
     * 手机号sha1
     */
    private String cellSha1;
}
