package com.br.marketing.client.robotaiapi.input;

import lombok.Data;

@Data
public class BlackQueryDetailDTO {

    /**
     * 数据ID(必传)
     */
    private String dataId;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * cid，apiCode二选一必填
     */
    private String cid;
    /**
     * 案件编号
     */
    private String caseNum;
    /**
     * 手机号(案件编号手机号二选一必填)
     */
    private String phone;

    /**
     * 手机号必传加密方式(见：PhoneEncryptTypeEnum)
     */
    private String encryptType;
    /**
     * 拓展字段
     */
    private String extraData;

}
