package com.br.marketing.entity.auth;

import lombok.Data;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 登录入参实体类
 * @Date 2022/3/11 9:43 AM
 * ------------------------------
 */
@Data
public class LoginReqObj {
    private String username;
    private String password;
    private String md5Password;
    private String captcha;
    private String sessionId;
    private String ticket;
}
