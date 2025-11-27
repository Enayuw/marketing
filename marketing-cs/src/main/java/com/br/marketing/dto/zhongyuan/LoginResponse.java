package com.br.marketing.dto.zhongyuan;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应DTO
 *
 * @author kongbx
 * @date 2025/11/14
 */
@Data
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 访问令牌
     */
    private String token;
}

