package com.br.marketing.client.xiecheng;

import com.alibaba.fastjson.annotation.JSONField;
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

    /**
     ApiCode
     */
    @JSONField(serialize = false)
    private String apiCode;

    public SmsQuitReq(String cipherMobile, String blackListType ,String apiCode) {
        this.cipherMobile = cipherMobile;
        this.blackListType = blackListType;
        this.apiCode = apiCode;
    }


}
