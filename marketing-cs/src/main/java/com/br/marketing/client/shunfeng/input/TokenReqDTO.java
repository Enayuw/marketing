package com.br.marketing.client.shunfeng.input;

import lombok.Data;

/**
 * @author zhen.li1
 */
@Data
public class TokenReqDTO {


    /**
     * 合作伙伴编码（即顾客编码）
     */
    private String  partnerID;


    /**
     * 合作伙伴密钥 （即校验码）
     */
    private String  secret;


    /**
     * 申请类型，填 password
     */
    private String  grantType;



}
