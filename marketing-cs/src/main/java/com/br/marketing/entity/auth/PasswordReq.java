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
    String newPassword;
    String oldPassword;
}
