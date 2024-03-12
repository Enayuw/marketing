package com.br.marketing.client.didi.input;

import lombok.Data;

/**
 * @Description SmsReqDTO
 * @Author hong.chen
 * @CreateTime 2023/04/23
 */
@Data
public class DiDiReqVO {
    // md5手机号
    private String custMobileMd5;

    // 媒体名称
    // bairong/bairongA
    private String mediaName;

}
