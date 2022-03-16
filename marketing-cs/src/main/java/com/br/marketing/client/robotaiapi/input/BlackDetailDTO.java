package com.br.marketing.client.robotaiapi.input;

import com.br.marketing.rule.InterfaceParams;
import lombok.Data;

@Data
public class BlackDetailDTO extends InterfaceParams {
    /**
     * 数据ID(必传)
     */
    private String dataId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 生效开始时间
     */
    private String effectiveDate;

    /**
     * 生效截至日期
     */
    private String expireDate;

    /**
     * 备注
     */
    private String remark;
}
