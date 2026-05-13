package com.br.marketing.dto.zhongyuan;

import lombok.Getter;
import lombok.Setter;

/**
 * 移动端标准对接响应数据实体
 *
 * @author xuxiao
 * @version 1.0
 * @date 2025/11/7 上午9:16
 */
@Getter
@Setter
public class MtStandardResponse {

    // 请求流水号
    // 渠道编号+yyyymmddhhmmss（年月日时分秒）+6位流水号（不足位前补0）
    private String responseNo;

    // 响应码
    private String errorCode;

    // 响应码描述
    private String errorMsg;

    // 签名
    private String sign;

    // 加密后AES key
    private String key;

    // 加密后的响应，业务数据
    private String responseData;

    public String signData() {
        return "errorCode=" + errorCode +
                "&errorMsg=" + errorMsg +
                "&key=" + key +
                "&responseNo=" + responseNo;
    }

    @Override
    public String toString() {
        return "MtStandardResponseEntity{" +
                "responseNo='" + responseNo + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                ", sign='" + sign + '\'' +
                ", key='" + key + '\'' +
                ", responseData='" + responseData + '\'' +
                '}';
    }

}
