package com.br.marketing.dto.zhongyuan;

import lombok.Getter;
import lombok.Setter;

/**
 * 移动端标准对接请求数据实体
 *
 * @author xuxiao
 * @version 1.0
 * @date 2025/11/7 上午9:16
 */
@Getter
@Setter
public class MtStandardRequest {

    // 请求流水号
    // 渠道编号+yyyymmddhhmmss（年月日时分秒）+6位流水号（不足位前补0）
    private String requestNo;

    // 请求时间戳
    private String timestamp;

    // 签名
    private String sign;

    // 加密后AES key
    private String key;

    // 加密后的请求，业务数据
    private String requestData;

    public String signData() {
        return "key=" + key +
                "&requestNo=" + requestNo +
                "&timestamp=" + timestamp;
    }

    @Override
    public String toString() {
        return "MtStandardRequestEntity{" +
                "requestNo='" + requestNo + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", sign='" + sign + '\'' +
                ", key='" + key + '\'' +
                ", requestData='" + requestData + '\'' +
                '}';
    }

}
