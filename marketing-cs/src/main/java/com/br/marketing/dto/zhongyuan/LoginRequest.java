package com.br.marketing.dto.zhongyuan;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求DTO
 *
 * @author kongbx
 * @date 2025/11/14
 */
@Data
public class LoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应用用户
     */
    private String appUser;

    /**
     * 应用密钥
     */
    private String appKey;
}

