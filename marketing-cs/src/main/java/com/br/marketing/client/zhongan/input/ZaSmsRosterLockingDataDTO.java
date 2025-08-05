package com.br.marketing.client.zhongan.input;

import com.br.marketing.rule.InterfaceParams;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ZaSmsRosterLockingDataDTO extends InterfaceParams {

    /**
     * apicode
     */
    private String apiCode;

    /**
     * 案件编号
     */
    private String caseNum;

    /**
     * MD5手机号
     */
    private String mobileMd5;

    /**
     * 营销日期,yyyy-MM-dd
     */
    private String bizDate;

    /**
     * 机构运营场景
     */
    private String userType;

    /**
     * 是否发送成功(0-否;1-是)
     */
    private Integer smsSendStatus;

}