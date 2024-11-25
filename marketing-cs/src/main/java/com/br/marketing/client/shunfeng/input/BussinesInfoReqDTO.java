package com.br.marketing.client.shunfeng.input;

import lombok.Data;

/**
 * @author zhen.li1
 */
@Data
public class BussinesInfoReqDTO {

    /**
     * 合作伙伴编码（即顾客编码）
     */
    private String  partnerID;


    /**
     * 请求接口编码 COM_RECE_FEC_GET_COMPANY_PUBLIC_INFO
     */
    private String  serviceCode;

    /**
     * 登录获取到的 token
     */
    private String  accessToken;

    /**
     * 用户请求标识，可输入任意字符串
     */
    private String  requestID;

    /**
     * 业务参数 格式为 json 字符串
     */
    private String  msgData;


    /**
     * 当前时间戳 13 位
     */
    private Long  timestamp;

}
