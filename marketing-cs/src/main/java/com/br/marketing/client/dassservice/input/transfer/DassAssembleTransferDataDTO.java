package com.br.marketing.client.dassservice.input.transfer;

import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.rule.InterfaceParams;
import lombok.Data;

/**
 *
 * @Description :调电销推转化数据入参
 * ---------------------------------
 * @Author : lizhen
 * @Date : Create in 2022/4/21 16:39
 */

@Data
public class DassAssembleTransferDataDTO extends InterfaceParams {

    /**
     * 调用电销转化入参
     */
    private DassTransferDataDTO dassTransferDataDTO;

    /**
     * 插入b_phone_sale_extend_info表入参
     */
    private PhoneSaleExtendInfo phoneSaleExtendInfo;

}
