package com.br.marketing.client.xiecheng;

import lombok.Data;

/**
 * @author zhen.li1
 */
@Data
public class SmsQuitReq {

    /**
     手机号 （sha256）
     */
    private String cipherMobile;


    /**
     通讯投诉
     */
    private String blackListType;

    public SmsQuitReq(String cipherMobile, String blackListType) {
        this.cipherMobile = cipherMobile;
        this.blackListType = blackListType;
    }


}
