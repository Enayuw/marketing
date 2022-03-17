package com.br.marketing.client.dassservice.input.userdata;

import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.entity.PhoneSaleExtendShuhe;
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
    private DassImportAdapDTO dassImportAdapDTO;

    /**
     * 插入b_phone_sale_extend_shuhu表入参
     */
    private PhoneSaleExtendShuhe phoneSaleExtendShuhe;

}
