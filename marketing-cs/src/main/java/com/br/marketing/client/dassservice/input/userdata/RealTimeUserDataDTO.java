package com.br.marketing.client.dassservice.input.userdata;

import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.rule.InterfaceParams;
import lombok.Data;

/**
 * 实时推送用户名单 接口入参
 *
 * @author lizhen
 * @dateTime 2022/3/17 13:36
 */

@Data
public class RealTimeUserDataDTO extends InterfaceParams {

    /**
     * 调用Dass 入参
     */
    private DassSingleImportAdapDTO dassSingleImportAdapDTO;

    /**
     * 插入b_phone_sale_extend_info表入参
     */
    private PhoneSaleExtendInfo phoneSaleExtendInfo;

}
