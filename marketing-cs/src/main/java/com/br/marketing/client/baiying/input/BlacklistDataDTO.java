package com.br.marketing.client.baiying.input;

import lombok.Data;

/**
 * @ClassName BlacklistDataDTO
 * @Author kongbx
 * @Date 2024/5/27 13:50
 */
@Data
public class BlacklistDataDTO {
    /**
     * 姓名
     */
    private String name;
    /**
     * 手机号 MD5 小写 32 位/或者 sha266
     */
    private String phone;
    /**
     * 案件编号 必填
     */
    private String caseNum;
    /**
     * 生效开始时间
     */
    private String effectiveDate;
    /**
     * 生效截止时间
     */
    private String expireDate;
    /**
     *
     */
    private String remark;
}
