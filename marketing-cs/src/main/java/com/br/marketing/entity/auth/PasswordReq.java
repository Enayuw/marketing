package com.br.marketing.entity.auth;

import lombok.Data;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 密码
 * @Date 2022/3/12 2:42 PM
 * ------------------------------
 */
@Data
public class PasswordReq {
    private String newPassword;
    private String oldPassword;
    private String md5Password;
    private String username;
}
